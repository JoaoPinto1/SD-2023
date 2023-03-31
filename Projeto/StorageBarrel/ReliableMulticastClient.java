package StorageBarrel;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.HashMap;

/**
 * Classe para a implementação do multicast relativo aos Barrels
 */
public class ReliableMulticastClient {
    private final String MULTICAST_ADDRESS = "224.3.2.1";
    private final int PORT = 4321;
    public final MulticastSocket socket;
    private final InetAddress group;
    private static int PACKET_SIZE = 5000;
    private final Connection c;
    private HashMap<Integer, Integer> expectedSeqNumbers;
    private InetSocketAddress receiveGroup;

    /**
     * Contrutor da Classe de multicast relativa aos Barrels. Conecta-se a um grupo multicast e agurda a receção de mensagens.
     * É capaz de enviar informações ACK e NACK quando necessário. Conecta-se à base de dados especifica ao Barrel
     * @param nBarrel Identificador do Barrel
     * @throws IOException Se ocorrer um erro de IO ao enviar a mensagem
     * @throws SQLException Se ocorrer um erro na inserção da base de dados
     */
    public ReliableMulticastClient(int nBarrel) throws IOException, SQLException {
        socket = new MulticastSocket(PORT);
        group = InetAddress.getByName(MULTICAST_ADDRESS);
        receiveGroup = new InetSocketAddress(MULTICAST_ADDRESS, PORT);
        NetworkInterface netIf = NetworkInterface.getByName("bge0");
        socket.joinGroup(receiveGroup, netIf);
        String db = "jdbc:postgresql://localhost:5432/db" + nBarrel;
        c = DriverManager.getConnection(db, "test", "test");
        c.setAutoCommit(false);
        expectedSeqNumbers = new HashMap<>();
    }

    /**
     * Método de receção de mensagens vindas dos Downloaders
     * @return Packet recebido
     * @throws IOException Erro ao receber a mensagem
     */
    public DatagramPacket receive() throws IOException {
        byte[] buffer = new byte[5000];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        socket.receive(packet);
        return packet;
    }

    /**
     * Verificação do packet recebido. Se for o packet esperado insere na base de dados as informaçoes recebidas.
     * Se não for o esperado, envia um NACK com a informação dos packets que está em falta e aguarda por eles.
     * Quando recebe um desses packets perdidos, envia um ACK para o Downloader.
     * @param packet Packet a ser verificado
     * @param nDownloader Identificação do Downloader que enviou esse packet
     * @return Auxilio para a terminação do programa
     * @throws IOException Se ocorreu algum erro de envio ou receção de mensagens
     * @throws SQLException Se ocorreu algum erro na inserção da base de dados
     */
    public int checkPacket(DatagramPacket packet, int nDownloader) throws IOException, SQLException {
        int seqNum = decodePacketSequenceNumber(packet.getData());
        int expectedPacket = 0;
        if (!expectedSeqNumbers.containsKey(nDownloader)) {
            expectedSeqNumbers.put(nDownloader, 0);
        } else {
            expectedPacket = expectedSeqNumbers.get(nDownloader) + 1;
            expectedSeqNumbers.replace(nDownloader, expectedPacket);
        }
        if (expectedPacket == seqNum) {
            insertDB(decodePacketMessage(packet.getData()));
            return 1;
        }
        else if (expectedPacket < seqNum) {
            System.out.println("ohoh sending nack");
            sendNACK(expectedPacket, seqNum, nDownloader);
            while (expectedPacket <= seqNum) {
                byte[] buffer = new byte[PACKET_SIZE];
                DatagramPacket new_packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(new_packet);
                String[] message = new String(new_packet.getData()).trim().split("--");
                if(!message[0].equals("ACK") && Integer.parseInt(message[0])>-1) {
                    if (decodePacketSequenceNumber(new_packet.getData()) == expectedPacket && decodeDownloaderNumber(new_packet.getData()) == nDownloader) {
                        sendACK(nDownloader);
                        insertDB(decodePacketMessage(new_packet.getData()));
                        expectedPacket++;
                    }
                }
            }
            return 1;
        } else {
            return 0;
        }
    }

    /**
     * Envia ACK da receção do packet perdido ao Downloader em causa
     * @param nDownloader Identificação do Downloader a enviar o ACK
     * @throws IOException Se ocorreu algum erro no envio do ACK
     */
    private void sendACK(int nDownloader) throws IOException {
        byte[] buffer = ("ACK--" + nDownloader).getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, PORT);
        socket.send(packet);
        System.out.println("ACK--" + nDownloader);
    }

    /**
     * Envia NACK com a informação de inicio de perda de informação até ao ultimo packet a ser recebido
     * @param expectedPacket Numero de sequencia de packet esperado pelo Barrel
     * @param seqNum Numero de sequencia de packet recidido
     * @param nDownloader Identificação do Downloader a enviar o NACK
     * @throws IOException Se ocorreu algum erro no envio do NACK
     */
    private void sendNACK(int expectedPacket, int seqNum, int nDownloader) throws IOException {
        byte[] buffer = String.format(-1 + "--%d--%d--%d", expectedPacket, seqNum, nDownloader).getBytes(StandardCharsets.UTF_8);
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, PORT);
        socket.send(packet);
        System.out.printf(-1 + "--%d--%d--%d", expectedPacket,seqNum, nDownloader);
    }

    /**
     * Descodificação do numero de sequencia da mensagem recebida
     * @param data Mensagem recebida
     * @return Numero de sequencia da mensagem
     */
    private int decodePacketSequenceNumber(byte[] data) {
        String[] tokens = new String(data).trim().split("--");
        return Integer.parseInt(tokens[0]);
    }

    /**
     * Descodificação do numero de identificação do Downloader que enviou a mensagem
     * @param data Mensagem recebida
     * @return Indentificação do Downloader
     */
    public int decodeDownloaderNumber(byte[] data) {
        String[] tokens = new String(data).trim().split("--");
        return Integer.parseInt(tokens[1]);
    }

    /**
     * Descodificação da mensagem recebida
     * @param data Mensagem codificada recebida
     * @return String com o conteúdo da mensagem
     */
    public String decodePacketMessage(byte[] data) {
        String[] tokens = new String(data).trim().split("--");
        return tokens[2];
    }

    /**
     * Inserção das informações na base de dados.
     * @param message Mensagem recebida a ser processada para a inserção
     * @throws SQLException Erro na inserção na base de dados
     */
    private void insertDB(String message) throws SQLException {
        System.out.println(message);
        String[] split_message = message.split("[|;]+");
        PreparedStatement pre_stmt;
        ResultSet rs;
        //url que foi processado pelo downloader
        if (split_message[1].equals("url")) {
            pre_stmt = c.prepareStatement("SELECT url FROM url WHERE url = ?;");
            pre_stmt.setString(1, split_message[3]);
            rs = pre_stmt.executeQuery();
            if (rs.next()) {
                pre_stmt = c.prepareStatement("UPDATE url set title=?,citation=? where url=?;");
                pre_stmt.setString(1, split_message[5]);
                pre_stmt.setString(2, split_message[7]);
                pre_stmt.setString(3, split_message[3]);
                pre_stmt.executeUpdate();
                c.commit();
            } else {
                pre_stmt = c.prepareStatement("INSERT INTO url VALUES (?,?,?);");
                pre_stmt.setString(1, split_message[3]);
                pre_stmt.setString(2, split_message[5]);
                pre_stmt.setString(3, split_message[7]);
                pre_stmt.executeUpdate();
                c.commit();
            }
        }
        //palavras encontradas no url
        if (split_message[1].equals("word_list")) {
            for (int i = 5; i < split_message.length; i += 2) {
                pre_stmt = c.prepareStatement("SELECT word FROM word WHERE word = ?;");
                pre_stmt.setString(1, split_message[i]);
                rs = pre_stmt.executeQuery();
                if (!rs.next()) {
                    pre_stmt = c.prepareStatement("INSERT INTO word VALUES (?);");
                    pre_stmt.setString(1, split_message[i]);
                    pre_stmt.executeUpdate();
                }
                pre_stmt = c.prepareStatement("SELECT word_word,url_url from word_url where word_word=? and url_url=?;");
                pre_stmt.setString(1, split_message[i]);
                pre_stmt.setString(2, split_message[3]);
                rs = pre_stmt.executeQuery();
                if (!rs.next()) {
                    pre_stmt = c.prepareStatement("INSERT INTO word_url VALUES (?,?);");
                    pre_stmt.setString(1, split_message[i]);
                    pre_stmt.setString(2, split_message[3]);
                    pre_stmt.executeUpdate();
                    c.commit();
                }
            }
        }

        //Urls presentes no ultimo url processado
        if (split_message[1].equals("url_list")) {
            for (int i = 5; i < split_message.length; i += 2) {
                pre_stmt = c.prepareStatement("select url from url where url=?;");
                pre_stmt.setString(1, split_message[i]);
                rs = pre_stmt.executeQuery();
                if (!rs.next()) {
                    pre_stmt = c.prepareStatement("insert into url values (?,null,null);");
                    pre_stmt.setString(1, split_message[i]);
                    pre_stmt.executeUpdate();
                    c.commit();
                }
                pre_stmt = c.prepareStatement("select url_url.url_url from url_url where url_url = ? and url_url1 = ?;");
                pre_stmt.setString(1, split_message[3]);
                pre_stmt.setString(2, split_message[i]);
                rs = pre_stmt.executeQuery();
                if (!rs.next()) {
                    pre_stmt = c.prepareStatement("insert into url_url values (?,?);");
                    pre_stmt.setString(1, split_message[3]);
                    pre_stmt.setString(2, split_message[i]);
                    pre_stmt.executeUpdate();
                    c.commit();
                }
            }
        }
    }
}
