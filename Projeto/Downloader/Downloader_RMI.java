package Downloader;

import RMIClient.Hello_C_I;
import RMIClient.Hello_S_I;

import java.io.Serializable;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

public class Downloader_RMI extends UnicastRemoteObject implements Runnable, Hello_C_I {

    protected Downloader_RMI() throws RemoteException {
        super();
    }

    public void print_on_client(String s) throws RemoteException {

    }

    public void ping() throws RemoteException {

    }

    @Override
    public void run(){


        try {
            Downloader_RMI c = new Downloader_RMI();
            Hello_S_I h = (Hello_S_I) LocateRegistry.getRegistry(7000).lookup("XPTO");
            h.downloader_subscribe("Downloader" + ProcessHandle.current().pid() , (Hello_C_I) c);
        } catch (RemoteException | NotBoundException e) {
            throw new RuntimeException(e);
        }


    }

}

