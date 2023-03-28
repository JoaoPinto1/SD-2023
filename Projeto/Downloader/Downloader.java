package Downloader;

import URLQueue.URLObject;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


import URLQueue.QueueInterface;

import java.rmi.registry.LocateRegistry;
import java.util.StringTokenizer;

public class Downloader extends Thread{
    private final String MULTICAST_ADDRESS = "224.3.2.1";
    private final int PORT = 4321;
    private static final int PACKET_SIZE = 5000;
    private int nDownloader;

    public Downloader(int n){
        nDownloader = n;
    }

    public static void main(String[] args) throws Exception{
        Downloader server = new Downloader(Integer.parseInt(args[0]));
        server.start();
    }

    public void run() {
        try {
            ReliableMulticastServer multicast = new ReliableMulticastServer(nDownloader);
            // create socket without binding it (only for sending)
            QueueInterface server = (QueueInterface) LocateRegistry.getRegistry(6000).lookup("Queue");
            int seqNum = 0;
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
                catch (HttpStatusException | UnsupportedMimeTypeException e){
                    System.out.println("Erro na procura");
                    continue;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
