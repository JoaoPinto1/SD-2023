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
    private int nBarrel;

    public Storage_Barrels_Multicast(int nBarrel) throws RemoteException {
        super();
        this.nBarrel = nBarrel;
    }

    @Override
    public void run() {
        try {
            ReliableMulticastClient multicast = new ReliableMulticastClient(nBarrel);
            while (true) {
                System.out.println("Waiting");
                DatagramPacket packet = multicast.receive();
                String[] message = new String(packet.getData()).trim().split("--");
                if (!message[0].equals("ACK")){
                    if(Integer.parseInt(message[0])!=-1) {
                        int nDownloader = multicast.decodeDownloaderNumber(packet.getData());
                        int num = multicast.checkPacket(packet, nDownloader);
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
