package Downloader;

import java.io.IOException;
import java.net.*;
import java.util.*;

/**
 * Classe onde se encontra todos os metodos para a implementacao do multicast da parte dos Downloaders
 */
public class ReliableMulticastServer extends Thread implements Runnable {
    private final String MULTICAST_ADDRESS = "224.3.2.1";
    private final int PORT = 4321;
    private static final int PACKET_SIZE = 5000;
    private MulticastSocket socket;
    private InetAddress group;
    private InetSocketAddress receiveGroup;
    private int sequenceNumber; // Número de sequência da próxima mensagem a ser enviada
    private List<String> messagesSent;
    private int nDownloader;

    /**
     * Construtor da Classe de Multicast utilizado pelos Downloaders.
     * Cria um servidor de multicast confiavel que usa NACK com retransmissao de pacotes perdidos para garantir a entrega confiavel
     *
     * @param n Identificador do Downloader
     */
    public ReliableMulticastServer(int n) {
        try {
            socket = new MulticastSocket(PORT);
            group = InetAddress.getByName(MULTICAST_ADDRESS);
            receiveGroup = new InetSocketAddress(MULTICAST_ADDRESS, PORT);
            NetworkInterface netIf = NetworkInterface.getByName("bge0");
            socket.joinGroup(receiveGroup, netIf);
            sequenceNumber = 0;
            messagesSent = new ArrayList<>();
            nDownloader = n;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Metodo de envio de mensagens multicast para os Barrels
     *
     * @param message Mensagem a ser enviada
     * @throws IOException Se ocorrer um erro de IO ao enviar a mensagem
     */
    public void send(String message) throws IOException {
        byte[] data = message.getBytes();
        DatagramPacket packet = new DatagramPacket(data, data.length, group, PORT);
        packet.setData(encodePacketData(sequenceNumber, message)); // Codifica o número de sequência no pacote
        try {
            socket.send(packet);
            if (!messagesSent.contains(message)) {
                messagesSent.add(message);
                sequenceNumber++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Metodo utilizado na Thread em modo de escuta para o caso de rececao de NACKs, retransmite os pacotes perdidos pelo
     * Barrel em questao e espera a rececao dos ACKs desses mesmos pacotes
     *
     * @throws IOException          Se ocorrer um erro de IO ao receber os pacotes
     * @throws InterruptedException Se a thread for interrompida enquanto estiver a aguardar a rececao de pacotes
     */
    private void receiveACKorNACK() throws IOException, InterruptedException {
        String[] message;
        while (true) {
            byte[] buffer = new byte[PACKET_SIZE];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);
            message = new String(packet.getData()).trim().split("--");
            //ignora ACKs
            if (!message[0].equals("ACK")) {
                //ter a certeza se é para este Downloader utilizando nDownloader
                if (Integer.parseInt(message[0]) == -1 && Integer.parseInt(message[3]) == nDownloader) {
                    System.out.println("RECEBI UM NACK");
                    int limit = Integer.parseInt(message[2]);
                    for (int i = Integer.parseInt(message[1]); i <= limit; i++) {
                        byte[] data = "NACK".getBytes();
                        DatagramPacket new_new_packet = new DatagramPacket(data, data.length, group, PORT);
                        new_new_packet.setData(encodePacketData(i, messagesSent.get(i)));
                        socket.send(new_new_packet);
                        //envia pacotes perdidos
                        System.out.println("--->" + messagesSent.get(i));
                        while (true) {
                            byte[] new_buffer = new byte[PACKET_SIZE];
                            DatagramPacket new_packet = new DatagramPacket(new_buffer, new_buffer.length);
                            //aguarda receção de ACKS
                            socket.receive(new_packet);
                            message = new String(new_packet.getData()).trim().split("--");
                            if (message[0].equals("ACK") && Integer.parseInt(message[1]) == nDownloader) {
                                break;
                            }
                        }
                    }
                }
            }
        }
    }


    /**
     * Responsavel por codificar o numero de sequencia da mensagem e o proprio conteudo da mensagem em um array de bytes,
     * para que possa ser enviado como um pacote de dados.
     *
     * @param sequenceNumber Numero de sequencia do pacote
     * @param message        Mensagem a enviar
     * @return Mensagem codificada em byte[] pronta a enviar
     */
    private byte[] encodePacketData(int sequenceNumber, String message) {
        return (sequenceNumber + "--" + nDownloader + "--" + message).getBytes();
    }

    @Override
    public void run() {
        try {
            receiveACKorNACK();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
