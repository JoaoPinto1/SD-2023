package Downloader;

import URLQueue.URLQueue;
import URLQueue.URLObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


//import URLQueue.Interface.Callback;

import java.net.MulticastSocket;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import java.io.IOException;
import java.net.SocketException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.StringTokenizer;

public class Downloader extends Thread{
    private final String MULTICAST_ADDRESS = "224.3.2.1";
    private final int PORT = 4321;

    public static void main(String[] args) throws Exception{
        Downloader server = new Downloader();
        server.start();
        String url = "https://www.worten.pt/";
        Document doc = Jsoup.connect(url).get();
        StringTokenizer tokens = new StringTokenizer(doc.text());
        int countTokens = 0;
        while (tokens.hasMoreElements() && countTokens++ < 100)
            System.out.println(tokens.nextToken().toLowerCase());
        Elements links = doc.select("a[href]");
        for (Element link : links)
            System.out.println(link.text() + "\n" + link.attr("abs:href") + "\n");

    }

    public void run(){
        try (MulticastSocket socket = new MulticastSocket()) {
            // create socket without binding it (only for sending)

            while (true) {
                URLQueue queue = (URLQueue) Naming.lookup("Queue");
                //queue.getNextItem(new CallbackImpl());
                String message = this.getName() + " packet ";
                byte[] buffer = message.getBytes();

                InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, PORT);
                socket.send(packet);
                sleep(5000);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            throw new RuntimeException(e);
        }
    }
    /*
    static class CallbackImpl implements Callback {
        public void onNewItem(String item) throws RemoteException {
            System.out.println("New item: " + item);
        }
    }
    */
}
