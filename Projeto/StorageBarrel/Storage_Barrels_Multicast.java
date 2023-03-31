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


/**
 * Classe da Thread principal do multicast associado ao Barrel
 */
public class Storage_Barrels_Multicast extends Thread implements Runnable {
    private final String MULTICAST_ADDRESS = "224.3.2.1";
    private final int PORT = 4321;
    private final int nBarrel;

    /**
     * Contrutor da Class Storage_Barrels_Multicast
     * @param nBarrel Identificação do Barrel
     */
    public Storage_Barrels_Multicast(int nBarrel) {
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
                        //ignora todos os ACKs e NACKs
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
