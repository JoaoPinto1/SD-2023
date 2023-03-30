package RMIClient;
import Downloader.Downloader;

import java.rmi.*;

public interface Hello_S_I extends Remote {
    public void print_on_server(String s, Hello_C_I client) throws java.rmi.RemoteException;

    public void subscribe(String name, Hello_C_I client) throws RemoteException;

    public void unsubscribe(String name, Hello_C_I client) throws RemoteException;

    public void downloader_subscribe(String name, Hello_C_I client) throws RemoteException;
}