package URLQueue;

import java.rmi.RemoteException;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class URLQueueMain {
    public static void main(String[] args) throws RemoteException {
        try {
            Registry r = LocateRegistry.createRegistry(6000);
            URLQueueServer server = new URLQueueServer();
            r.rebind("Queue", server);
            System.out.println("Queue server ready.");
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
