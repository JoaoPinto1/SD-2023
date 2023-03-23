package URLQueue;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Interface extends Remote {
    URLObject getNextItem(Callback callback) throws Exception;
}

interface Callback extends Remote {
    void onNewItem(URLObject item) throws RemoteException;
}