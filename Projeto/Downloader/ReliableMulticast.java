package Downloader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.*;

public class ReliableMulticast {
    private final InetAddress groupAddress;
    private final int groupPort;
    private final MulticastSocket socket;

    public ReliableMulticast(String groupAddress, int groupPort) throws Exception {
        this.socket = new MulticastSocket(groupPort);
        this.groupAddress = InetAddress.getByName(groupAddress);
        this.groupPort = groupPort;
    }

    public void send(String message, int seqNum) throws Exception{
        byte[] messageByte = message.getBytes();
        ByteBuffer seqNumBuffer = ByteBuffer.allocate(4);
        seqNumBuffer.putInt(seqNum);
        byte[] seqNumByte = seqNumBuffer.array();
        byte[] buffer = new byte[messageByte.length + seqNumByte.length];
        //adiciona o seqNum ao inicio do buffer
        System.arraycopy(seqNumByte, 0, buffer, 0, seqNumByte.length);
        System.arraycopy(messageByte, 0, buffer, seqNumByte.length, messageByte.length);

        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, groupAddress, groupPort);

        socket.send(packet);

        //waitForAck(buffer);
    }

    public void receive() throws IOException {
        byte[] buffer = new byte[100];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        socket.receive(packet);
        String message = new String(packet.getData(), 0, packet.getLength());
        System.out.println("Recebi: "+message);
    }

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


}
