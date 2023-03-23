package URLQueue;

import java.rmi.RemoteException;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;

public class URLQueueMain{

    public static void main(String[] args) throws RemoteException {
        URLQueue queue = new URLQueue(100);
        URLObject url = new URLObject("https://www.worten.pt/");
        try {
            LocateRegistry.createRegistry(7000);
            Naming.rebind("Queue", queue);
            System.out.println("Queue server ready.");
            queue.insert_queue(url);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
