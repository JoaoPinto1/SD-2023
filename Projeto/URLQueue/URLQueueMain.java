package URLQueue;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Classe de criação da Queue de URLs. è criado um registo RMI para essa Queue
 */
public class URLQueueMain {
    /**
     * Método main para a UrlQueue
     * @param args Argumentos não necessários
     */
    public static void main(String[] args) {
        try {
            Registry r = LocateRegistry.createRegistry(6000);
            URLQueueServer server = new URLQueueServer();
            r.rebind("Queue", server);
            System.out.println("Queue RMISearchModule.server ready.");
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
