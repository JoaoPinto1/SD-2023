package StorageBarrel;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.HashMap;

public class ReliableMulticastClient {
    private final String MULTICAST_ADDRESS = "224.3.2.1";
    private final int PORT = 4321;
    private final MulticastSocket socket;
    private final InetAddress group;
    private static int PACKET_SIZE = 5000;
    private final Connection c;
    private HashMap<Integer, Integer> expectedSeqNumbers;

    public ReliableMulticastClient() throws IOException, SQLException {
        socket = new MulticastSocket();
        group = InetAddress.getByName(MULTICAST_ADDRESS);
        c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/googol", "test", "test");
        c.setAutoCommit(false);
        expectedSeqNumbers = new HashMap<>();
    }

    public int checkPacket(DatagramPacket packet, int nDownloader) throws IOException, SQLException {
        int seqNum = decodePacketSequenceNumber(packet.getData());
        int expectedPacket = 0;
        if(!expectedSeqNumbers.containsKey(nDownloader)){
            expectedSeqNumbers.put(nDownloader,0);
        }
        else {
            expectedPacket = expectedSeqNumbers.get(nDownloader) + 1;
            expectedSeqNumbers.replace(nDownloader,expectedPacket);
        }
        if (expectedPacket == seqNum) {
            sendACK(nDownloader);
            insertDB(decodePacketMessage(packet.getData()));
            return 1;
        } else if (seqNum == -1) {
            return 0;
        } else if(expectedPacket < seqNum) {
            System.out.println("ohoh sending nack");
            sendNACK(expectedPacket, nDownloader);
            return verifyPacket(packet,expectedPacket,nDownloader);
        }
        else{
            return 0;
        }
    }

    private int verifyPacket(DatagramPacket packet, int expectedPacket, int nDownloader) throws IOException, SQLException {
        byte[] buffer = new byte[PACKET_SIZE];
        DatagramPacket new_packet = new DatagramPacket(buffer, buffer.length);
        socket.receive(new_packet);
        if(decodePacketSequenceNumber(new_packet.getData())==expectedPacket && decodeDownloaderNumber(new_packet.getData())==nDownloader) {
            insertDB(decodePacketMessage(packet.getData()));
            return checkPacket(new_packet, nDownloader);
        }
        else{
            return verifyPacket(new_packet,expectedPacket,nDownloader);
        }
    }

    private void sendACK(int nDownloader) throws IOException {
        byte[] buffer = ("ACK--"+nDownloader).getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, PORT);
        socket.send(packet);
    }

    private void sendNACK(int seqNum, int nDownloader) throws IOException {
        byte[] buffer = String.format(-1 + "--%d--%d", seqNum,nDownloader).getBytes(StandardCharsets.UTF_8);
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, PORT);
        socket.send(packet);
        System.out.printf(-1 + "--%d--%d%n", seqNum,nDownloader);
    }

    private int decodePacketSequenceNumber(byte[] data) {
        String[] tokens = new String(data).trim().split("--");
        return Integer.parseInt(tokens[0]);
    }

    public int decodeDownloaderNumber(byte[] data){
        String[] tokens = new String(data).trim().split("--");
        return Integer.parseInt(tokens[1]);
    }

    public String decodePacketMessage(byte[] data) {
        String[] tokens = new String(data).trim().split("--");
        return tokens[2];
    }

    private void insertDB(String message) throws SQLException {
        System.out.println(message);
        String[] split_message = message.split("[|;]+");
        PreparedStatement pre_stmt;
        ResultSet rs;
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
        if (split_message[1].equals("word_list")) {
            for (int i = 5; i < split_message.length; i += 2) {
                pre_stmt = c.prepareStatement("SELECT word FROM word WHERE word = ?;");
                pre_stmt.setString(1, split_message[i]);
                rs = pre_stmt.executeQuery();
                if (!rs.next()) {
                    pre_stmt = c.prepareStatement("INSERT INTO word VALUES (?);");
                    pre_stmt.setString(1, split_message[i]);
                    pre_stmt.executeUpdate();

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
        }

        if (split_message[1].equals("url_list")) {
            for (int i = 5; i < split_message.length; i += 2) {
                pre_stmt = c.prepareStatement("select url from url where url=?;");
                pre_stmt.setString(1,split_message[i]);
                rs = pre_stmt.executeQuery();
                if(!rs.next()) {
                    pre_stmt = c.prepareStatement("insert into url values (?,null,null);");
                    pre_stmt.setString(1, split_message[i]);
                    pre_stmt.executeUpdate();
                    c.commit();
                }
                pre_stmt = c.prepareStatement("select url_url.url_url from url_url where url_url = ? and url_url1 = ?;");
                pre_stmt.setString(1,split_message[3]);
                pre_stmt.setString(2,split_message[i]);
                rs = pre_stmt.executeQuery();
                if(!rs.next()) {
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
