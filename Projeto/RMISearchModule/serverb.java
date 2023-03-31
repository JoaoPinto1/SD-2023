package RMISearchModule;

import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Random;
import java.util.*;

import Downloader.Downloader;
import RMIClient.Hello_C_I;
import RMIClient.Hello_S_I;


public class serverb extends UnicastRemoteObject implements Hello_S_I, Hello_C_I, Runnable {

    public final ArrayList<Hello_C_I> clients_RMI;
    public serverb h;
    public final List<String> results;
    public final List<String> searchs;


    public serverb(List<String> Result ,List<String> Searchs,ArrayList<Hello_C_I>storage_barrels) throws RemoteException {
        super();
        this.results = Result;
        this.searchs = Searchs;
        this.clients_RMI = storage_barrels;
    }

    /**
     * da print a string.
     * @param s string recebida
     * @throws RemoteException quando e acedido e nao responde
     */
    public void print_on_client(String s) throws RemoteException {
        System.out.println(s);
    }

    /**
     * nao realiza nada neste server.
     * @param s string recevida
     * @param client cliente que chamou
     */
    public void downloader_subscribe(String s,Hello_C_I client){

    }

    /**
     * quando chamado significa que vai receber resultados da pesquisa realizada, guarda resultados num array e avisa que foi realizada uma pesquisa
     * @param s pesquisa realizada
     * @param c cliente que realizou a pesquisa
     * @throws RemoteException quando e chamado e nao responde
     */
    public void print_on_server(String s, Hello_C_I c) throws RemoteException {

        System.out.println("RMISearchModule.serverb " + s);

        synchronized (results) {
            results.add(s);
            results.notify();
        }

    }

    /**
     * Recebe a chamada e devolve Remote execption se nao estiver ativo
     * @throws java.rmi.RemoteException quando e chamado e nao responde
     */
    public void ping() throws  java.rmi.RemoteException{

    }

    /**
     * Adiciona o cliente ao array de clientes
     * @param name nome do cliente
     * @param c cliente
     * @throws RemoteException quando e chamado e nao responde
     */
    public void subscribe(String name, Hello_C_I c) throws RemoteException {

        System.out.println("Subscribing " + name);
        System.out.print("> ");


        synchronized (clients_RMI) {
            clients_RMI.add(c);
            System.out.println("Storage Barrel adicionado , " + clients_RMI.size());
        }
    }

    /**
     * sempre que e chamada retira o cliente da lista de clientes.
     * @param name nome do cliente
     * @param c cliente
     * @throws RemoteException quando e chamado e nao responde
     */
    public void unsubscribe(String name, Hello_C_I c) throws RemoteException {
        System.out.println("Unsubscribing " + name);
        System.out.print("> ");


        synchronized (clients_RMI) {
            clients_RMI.remove(c);
            clients_RMI.notifyAll();
        }
    }

    /**
     * escolhe um cliente random da lista de clientes
     * @return devolve um cliente random que foi escolhido
     */
    private Hello_C_I RandomClient() {
        synchronized (h.clients_RMI) {

            if (h.clients_RMI.isEmpty()) {
                return null;
            }

            System.out.println(h.clients_RMI.size());
            Random rand = new Random();
            int rand_int = rand.nextInt(h.clients_RMI.size());
            return h.clients_RMI.get(rand_int);
        }
    }


    // =======================================================
    @Override
    public void run() {
        try {

            h = new serverb(results , searchs , clients_RMI);

            Registry r = LocateRegistry.createRegistry(7001);
            r.rebind("XPT", h);

            System.out.println("Hello Barrel_Server ready.");
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }

        while (true) {

            synchronized (results) {
                while (results.isEmpty()) {
                    try {
                        results.wait();

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            Hello_C_I client = RandomClient();

            while (client == null) {
                try {
                    System.out.println("looking for new barrel!");
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
                client = RandomClient();
            }

            try {

                System.out.println("a lista no momento:" + results);
                String str_received = results.get(0);
                results.remove(0);

                String[] str = str_received.split(" ");
                String new_string = str[2] + "," + str[5];

                int connect = 0;

                while (connect == 0) {

                    try {
                        client.print_on_client(new_string);

                        synchronized (searchs){
                            searchs.add(str[5]);
                        }

                        synchronized (clients_RMI){
                            clients_RMI.notifyAll();
                        }

                        connect = 1;
                    } catch (ConnectException e) {

                        System.out.println("Storage Barrel Invalido!");
                        h.unsubscribe("Storage Barrel", client);

                        synchronized (clients_RMI){
                            clients_RMI.notifyAll();
                        }

                        System.out.println("looking for new barrel!");

                        client = RandomClient();

                        while (client == null) {

                            try {
                                System.out.println("looking for new barrel ...");
                                Thread.sleep(1000);
                            } catch (InterruptedException ex) {
                                throw new RuntimeException(ex);
                            }
                            client = RandomClient();

                        }
                    }

                }

            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }

    }
}