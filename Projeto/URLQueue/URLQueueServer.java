package URLQueue;

import java.io.Serial;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class URLQueueServer extends UnicastRemoteObject implements QueueInterface{
    private final URLQueue queue;
    private static final long serialVersionUID = 1L;

    public URLQueue getQueue() {
        return queue;
    }

    public URLQueueServer() throws RemoteException{
        super();
        queue = new URLQueue(100);
    }

    public synchronized void addToQueue(URLObject url) throws RemoteException {
        try{
            queue.add(url);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        notifyAll();
    }
    public synchronized URLObject removeFromQueue() throws RemoteException, InterruptedException {
        while (queue.empty) {
            wait();
        }
        URLObject url = new URLObject("");
        try {
            url = queue.remove();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return url;
    }

    public synchronized boolean isQueueEmpty() throws RemoteException {
        return queue.empty;
    }
}
