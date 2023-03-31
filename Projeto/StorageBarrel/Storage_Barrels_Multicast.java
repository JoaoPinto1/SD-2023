package StorageBarrel;

import java.io.*;
import java.net.DatagramPacket;
import java.sql.*;


/**
 * Classe da Thread principal do multicast associado ao Barrel
 */
public class Storage_Barrels_Multicast extends Thread implements Runnable {
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
                DatagramPacket packet = multicast.receive();
                String[] message = new String(packet.getData()).trim().split("--");
                if (!message[0].equals("ACK")){
                    if(Integer.parseInt(message[0])>-1) {
                        //ignora todos os ACKs e NACKs
                        int nDownloader = multicast.decodeDownloaderNumber(packet.getData());
                        int num = multicast.checkPacket(packet, nDownloader);
                    }
                }
            }
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
