package StorageBarrel;

import java.io.*;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.*;

import Downloader.ReliableMulticastServer;


public class Storage_Barrels_Multicast extends Thread implements Runnable {
    private final String MULTICAST_ADDRESS = "224.3.2.1";
    private final int PORT = 4321;

    public Storage_Barrels_Multicast() throws RemoteException {
        super();
    }

    @Override
    public void run() {
        try (MulticastSocket socket = new MulticastSocket(PORT)) {
            InetSocketAddress group = new InetSocketAddress(MULTICAST_ADDRESS, PORT);
            NetworkInterface netIf = NetworkInterface.getByName("bge0");
            socket.joinGroup(group, netIf);
            ReliableMulticastClient multicast = new ReliableMulticastClient();
            while (true) {
                System.out.println("Waiting");
                byte[] buffer = new byte[5000];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                String message = new String(packet.getData()).trim();
                System.out.println(message);
                if (!message.equals("ACK")){
                    if(Integer.parseInt(message.split("--")[0])!=-1) {
                        int nDownloader = multicast.decodeDownloaderNumber(message.getBytes());
                        int num = multicast.checkPacket(packet, nDownloader);
                        if (num == 0)
                            continue;
                    }
                }
            }
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
