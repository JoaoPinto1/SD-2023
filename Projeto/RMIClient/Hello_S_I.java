package RMIClient;

import Downloader.Downloader;

import java.rmi.*;

public interface Hello_S_I extends Remote {
    /**
     * Cliente fornece string ao server
     *
     * @param s      String a enviar
     * @param client Cliente a enviar
     * @throws java.rmi.RemoteException Erro RMI
     */
    public void print_on_server(String s, Hello_C_I client) throws java.rmi.RemoteException;

    /**
     * Cliente subscreve ao server, e adicionado a lista de clientes
     *
     * @param name   Nome
     * @param client Client
     * @throws RemoteException Erro RMI
     */
    public void subscribe(String name, Hello_C_I client) throws RemoteException;

    /**
     * Cliente desinscreve do server, e retirado da lista de clientes.
     *
     * @param name   Nome
     * @param client Cliente
     * @throws RemoteException Erro RMI
     */
    public void unsubscribe(String name, Hello_C_I client) throws RemoteException;

    /**
     * Um downloader subscreve ao servidor, e adicionada a lista de downloaders.
     *
     * @param name   Nome
     * @param client Cliente
     * @throws RemoteException Erro RMI
     */
    public void downloader_subscribe(String name, Hello_C_I client) throws RemoteException;
}