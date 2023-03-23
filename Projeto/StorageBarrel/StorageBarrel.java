package StorageBarrel;

import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.io.IOException;

public class StorageBarrel extends Thread{
    private final String MULTICAST_ADDRESS = "224.3.2.1";
    private final int PORT = 4321;
    public static void main(String[] args){
        StorageBarrel client = new StorageBarrel();
        client.start();
    }
    public void run() {
        try (MulticastSocket socket = new MulticastSocket(PORT)) {
            // create socket and bind it
            //InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
            InetSocketAddress group = new InetSocketAddress(MULTICAST_ADDRESS, PORT);
            NetworkInterface netIf = NetworkInterface.getByName("bge0");
            socket.joinGroup(group, netIf);
            while (true) {
                byte[] buffer = new byte[5000];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                System.out.println("Waiting");
                socket.receive(packet);

                System.out.println("Received packet from " + packet.getAddress().getHostAddress() + ":" + packet.getPort() + " with message:");
                String message = new String(packet.getData(), 0, packet.getLength());
                System.out.println(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
