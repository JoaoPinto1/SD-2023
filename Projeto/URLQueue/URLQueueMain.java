package URLQueue;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Classe de criacao da Queue de URLs. E criado um registo RMI para essa Queue
 */
public class URLQueueMain {
    /**
     * Metodo main para a UrlQueue
     *
     * @param args Argumentos nao necessarios
     */
    public static void main(String[] args) {
        try {
            Registry r = LocateRegistry.createRegistry(6000);
            URLQueueServer server = new URLQueueServer();
            r.rebind("Queue", server);
            System.out.println("Queue RMISearchModule.server ready.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
