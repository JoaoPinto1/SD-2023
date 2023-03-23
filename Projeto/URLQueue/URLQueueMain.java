package URLQueue;

import java.rmi.RemoteException;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class URLQueueMain {
    public static void main(String[] args) throws RemoteException {
        URLObject url = new URLObject("https://www.worten.pt/");
        try {
            Registry r = LocateRegistry.createRegistry(7000);
            URLQueueServer server = new URLQueueServer();
            r.rebind("Queue", server);
            System.out.println("Queue server ready.");
            server.addToQueue(url);
            System.out.println("Adicionei alguma coisa");
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
