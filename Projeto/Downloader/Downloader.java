package Downloader;

import RMIClient.Hello_C_I;
import RMIClient.Hello_S_I;
import RMISearchModule.pagina_adminstracao;
import URLQueue.URLObject;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


import URLQueue.QueueInterface;

import java.io.IOException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.StringTokenizer;

/**
 * Classe Downloader
 */
public class Downloader extends Thread implements Serializable {
    private final String MULTICAST_ADDRESS = "224.3.2.1";
    private final int PORT = 4321;
    private static final int PACKET_SIZE = 5000;
    private int nDownloader;
    private static Thread t0,t1;
    private static Downloader_RMI drmi;
    private ReliableMulticastServer multicast;

    /**
     * Construtor da Classe Downloader para o processamento de páginas Web e envio multicast para Barrels
     * @param n Identificador do Downloader
     */
    public Downloader(int n){
        nDownloader = n;
        multicast = new ReliableMulticastServer(nDownloader);
        t1 = new Thread(multicast);
        t1.start();
    }

    /**
     * Método main da Class Downloader
     * @param args Identificador do Downloader
     * @throws Exception Exceções encontradas
     */
    public static void main(String[] args) throws Exception{
        Downloader server = new Downloader(Integer.parseInt(args[0]));

        server.start();

        drmi = new Downloader_RMI();
        t0 = new Thread(drmi);
        t0.start();
    }

    public void run() {
        try {

            // create socket without binding it (only for sending)
            QueueInterface server = (QueueInterface) LocateRegistry.getRegistry(6000).lookup("Queue");
            while (true) {
                try {
                    URLObject url = server.removeFromQueue();
                    Document doc = Jsoup.connect(url.getUrl()).get();
                    Element firstArticle = doc.select("article").first();
                    Element titleElement = null;
                    if (firstArticle != null) {
                        titleElement = firstArticle.select("h1 a").first();
                    }
                    String titleText = null;
                    if (titleElement != null) {
                        titleText = titleElement.text();
                    }
                    url.setTitle(titleText);
                    Element citationElement = doc.select("cite").first();
                    String citationText = null;
                    if (citationElement != null) {
                        citationText = citationElement.text();
                    }
                    url.setCitation(citationText);
                    String urlString = "type|url;url|" + url.getUrl() + ";" + "title|" + url.getTitle() + ";" + "citation|" + url.getCitation() + ";";
                    System.out.println(urlString);
                    multicast.send(urlString);
                    StringTokenizer tokens = new StringTokenizer(doc.text());
                    int countTokens = 0;
                    String stringWords = "type|word_list;url|" + url.getUrl() + "|";
                    while (tokens.hasMoreElements() && countTokens++ < 100) {
                        String nextToken = tokens.nextToken();
                        stringWords += "word_" + countTokens + "|" + (nextToken.toLowerCase()) + ";";
                    }
                    int countUrls = 1;
                    String stringUrls = "type|url_list;url_0|" + url.getUrl() + "|";
                    Elements links = doc.select("a[href]");
                    for (Element link : links) {
                        stringUrls += "url_" + countUrls + "|" + link.attr("abs:href") + ";";
                        URLObject new_url = new URLObject(link.attr("abs:href"));
                        server.addToQueue(new_url);
                        countUrls += 1;
                    }
                    multicast.send(stringWords);
                    System.out.println(stringWords);
                    multicast.send(stringUrls);
                    System.out.println(stringUrls);
                    System.out.println("------------");
                }
                catch (IOException | IllegalArgumentException e){
                    System.out.println("Erro na procura");
                    continue;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
