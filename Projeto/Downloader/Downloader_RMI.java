package Downloader;

import RMIClient.Hello_C_I;
import RMIClient.Hello_S_I;

import java.io.Serializable;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

/**
 * Classe Downloader_RMI
 */
public class Downloader_RMI extends UnicastRemoteObject implements Runnable, Hello_C_I {

    /**
     * Contrutor Downloader_RMI
     *
     * @throws RemoteException Erro RMI
     */
    protected Downloader_RMI() throws RemoteException {
        super();
    }

    /**
     * Servidor fornece string ao cliente
     *
     * @param s String a enviar
     * @throws RemoteException
     */
    public void print_on_client(String s) throws RemoteException {

    }

    /**
     * e chamado para saber se a funcao se encontra ativa
     *
     * @throws RemoteException se a funcao for chamada e nao responder
     */
    public void ping() throws RemoteException {

    }

    /**
     * Realiza uma conexao RMI com o server
     */
    @Override
    public void run() {


        try {
            Downloader_RMI c = new Downloader_RMI();
            Hello_S_I h = (Hello_S_I) LocateRegistry.getRegistry(7000).lookup("XPTO");
            h.downloader_subscribe("Downloader" + ProcessHandle.current().pid(), (Hello_C_I) c);
        } catch (RemoteException | NotBoundException e) {
            throw new RuntimeException(e);
        }


    }

}

