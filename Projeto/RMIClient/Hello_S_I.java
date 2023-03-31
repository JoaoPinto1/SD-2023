package RMIClient;
import Downloader.Downloader;

import java.rmi.*;

public interface Hello_S_I extends Remote {
    //Cliente fornece string ao server
    public void print_on_server(String s, Hello_C_I client) throws java.rmi.RemoteException;
    //cliente subscreve ao server, e adicionado a lista de clientes
    public void subscribe(String name, Hello_C_I client) throws RemoteException;
    //cliente unsubscreve do server, e retirado da lista de clientes.
    public void unsubscribe(String name, Hello_C_I client) throws RemoteException;
    //um downloader subscreve ao servidor, e adicionada a lista de downloaders.
    public void downloader_subscribe(String name, Hello_C_I client) throws RemoteException;
}