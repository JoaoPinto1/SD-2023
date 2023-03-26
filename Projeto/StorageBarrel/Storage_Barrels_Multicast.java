package StorageBarrel;

import java.io.*;
import java.net.*;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.sql.*;


public class Storage_Barrels_Multicast extends Thread implements Runnable {
    private final String MULTICAST_ADDRESS = "224.3.2.1";
    private final int PORT = 4321;
    private MulticastSocket socket;
    InetSocketAddress group;

    public Storage_Barrels_Multicast() throws RemoteException {
        super();
    }

    @Override
    public void run() {
        try {
            socket = new MulticastSocket(PORT);
            // create socket and bind it
            group = new InetSocketAddress(MULTICAST_ADDRESS, PORT);
            NetworkInterface netIf = NetworkInterface.getByName("bge0");
            socket.joinGroup(group, netIf);

            Connection c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/googol", "test", "test");
            c.setAutoCommit(false);
            Statement stmt = c.createStatement();
            PreparedStatement pre_stmt;
            ResultSet rs;
            System.out.println("Opened database successfully");

            HashMap<String, HashSet<String>> index = new HashMap<>();
            HashMap<String, HashSet<String>> urls = new HashMap<>();
            urls.put("visited", new HashSet<>());

            while (true) {
                byte[] buffer = new byte[5000];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                System.out.println("Waiting");
                socket.receive(packet);
                System.out.println("Received packet from " + packet.getAddress().getHostAddress() + ":" + packet.getPort() + " with message:");
                String message = new String(packet.getData(), 0, packet.getLength());
                if (message.equals("ACK")) {
                    continue;
                }
                sendAck(packet);
                String[] split_message = message.split("[|;]+");
                if (split_message[1].equals("url")) {
                    pre_stmt = c.prepareStatement("SELECT url FROM url WHERE url = ?;");
                    pre_stmt.setString(1, split_message[3]);
                    rs = pre_stmt.executeQuery();
                    if (rs.next()) {
                        pre_stmt = c.prepareStatement("UPDATE url set title=?,citation=? where url=?;");
                        pre_stmt.setString(1,split_message[5]);
                        pre_stmt.setString(2,split_message[7]);
                        pre_stmt.setString(3,split_message[3]);
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

                    if (split_message[1].equals("url_list")) {
                        for (int i = 5; i < split_message.length; i += 2) {
                            pre_stmt = c.prepareStatement("insert into url values (?,null,null);");
                            pre_stmt.setString(1,split_message[i]);
                            pre_stmt.executeUpdate();
                            c.commit();
                        }
                        for (int i = 5; i < split_message.length; i += 2){
                            pre_stmt = c.prepareStatement("insert into url_url values (?,?);");
                            pre_stmt.setString(1,split_message[3]);
                            pre_stmt.setString(2,split_message[i]);
                            pre_stmt.executeUpdate();
                            c.commit();
                        }
                    }
                }
            }
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }

    private void sendAck(DatagramPacket packet) throws IOException {
        String message = "ACK";
        byte[] ack = message.getBytes();
        InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
        DatagramPacket ackPacket = new DatagramPacket(ack, ack.length, group, PORT);
        socket.send(ackPacket);
        System.out.println("enviei");
    }
}
