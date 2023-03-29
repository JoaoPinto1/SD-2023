package Downloader;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ReliableMulticastServer {
    private final String MULTICAST_ADDRESS = "224.3.2.1";
    private final int PORT = 4321;
    private static final int PACKET_SIZE = 5000;
    private MulticastSocket socket;
    private InetAddress group;
    private InetSocketAddress receiveGroup;
    private int sequenceNumber; // Número de sequência da próxima mensagem a ser enviada
    private List<String> messagesSent;
    private int nDownloader;


    public ReliableMulticastServer(int n) throws IOException {
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
            receiveACKorNACK();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void receiveACKorNACK() throws IOException {
        byte[] buffer = new byte[PACKET_SIZE];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        socket.receive(packet);
        String[] message = new String(packet.getData()).trim().split("--");
        if (message[0].equals("ACK")) {
            if (Integer.parseInt(message[1]) == nDownloader) {
                //System.out.println("RECEBIDO ACK");
            } else {
                receiveACKorNACK();
            }
        } else if (Integer.parseInt(message[0]) == -1) {
            if (Integer.parseInt(message[2]) == nDownloader) {
                System.out.println("RECEBI UM NACK");
                send(messagesSent.get(Integer.parseInt(message[1])));
            }

        }
    }

    private byte[] encodePacketData(int sequenceNumber, String message) {
        return (sequenceNumber + "--" + nDownloader + "--" + message).getBytes();
    }
/*
    private void waitForAck(byte[] message) throws IOException, InterruptedException {
        HashSet<Integer> ackSet = new HashSet<>();
        boolean receivedAcks = false;
        long timeout = 1000;
        while(!receivedAcks){
            byte[] buffer = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            System.out.println("oi");
            socket.receive(packet);
            System.out.println("ola");
            String receivedMessage = new String(packet.getData()).trim();
            if(receivedMessage.equals("ACK")){
                System.out.println("recebi");
                ackSet.add(packet.getPort());
                if (ackSet.size() == 1) { //mudar quando tivermos mais barrels
                    receivedAcks = true;
                }
            }
            if(!receivedAcks){
                socket.send(new DatagramPacket(message, message.length, groupAddress, groupPort));
                // wait for the remaining timeout
                Thread.sleep(timeout);
            }
        }
    }
*/

}
