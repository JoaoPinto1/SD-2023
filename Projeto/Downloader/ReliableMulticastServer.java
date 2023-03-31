package Downloader;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static java.lang.Thread.sleep;

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

    private void receiveACKorNACK() throws IOException, InterruptedException {
        String[] message;
        while (true) {
            System.out.println("tou a espera");
            byte[] buffer = new byte[PACKET_SIZE];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);
            message = new String(packet.getData()).trim().split("--");
            System.out.println(message[0] + message[1]);
            if(!message[0].equals("ACK")) {
                if (Integer.parseInt(message[0]) == -1 && Integer.parseInt(message[3]) == nDownloader) {
                    System.out.println("RECEBI UM NACK");
                    int limit = Integer.parseInt(message[2]);
                    for (int i = Integer.parseInt(message[1]); i <= limit; i++) {
                        byte[] data = "NACK".getBytes();
                        DatagramPacket new_new_packet = new DatagramPacket(data, data.length, group, PORT);
                        new_new_packet.setData(encodePacketData(i, messagesSent.get(i)));
                        socket.send(new_new_packet);
                        System.out.println("--->" + messagesSent.get(i));
                        while (true) {
                            byte[] new_buffer = new byte[PACKET_SIZE];
                            DatagramPacket new_packet = new DatagramPacket(new_buffer, new_buffer.length);
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
