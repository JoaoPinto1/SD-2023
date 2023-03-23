package URLQueue;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface QueueInterface extends Remote {
    void addToQueue(URLObject url) throws Exception;
    URLObject removeFromQueue() throws RemoteException, InterruptedException;
    boolean isQueueEmpty() throws RemoteException;
}
