package RMISearchModule;

import java.io.Serializable;
import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.*;

import Downloader.Downloader;
import RMIClient.Hello_C_I;
import RMIClient.Hello_S_I;
import URLQueue.*;


/**
 * Classe do Server
 */
public class server extends UnicastRemoteObject implements Hello_S_I, Runnable, Serializable {

    private Thread t0;
    private pagina_adminstracao pa;
    public ArrayList<Hello_C_I> clients;
    public HashMap<String, String> registed_users = new HashMap<String, String>();
    public server h;
    public final List<String> results;
    public final List<String> searchs;
    public Map<String, String> top_searchs = new HashMap<>();
    public ArrayList<Hello_C_I> storage_barrels;

    public ArrayList<Hello_C_I> downloaders;


    /**
     * @param Result          resultado obtidos nas pesquisas
     * @param Searchs         pesquisas realizadas
     * @param tsearchs        10 pesquisas mais realizadas
     * @param storage_barrels lista de storage barrels
     * @param downloaders     lista de downloaders
     * @throws RemoteException
     */
    public server(List<String> Result, List<String> Searchs, Map<String, String> tsearchs, ArrayList<Hello_C_I> storage_barrels, ArrayList<Hello_C_I> downloaders) throws RemoteException {
        super();
        this.results = Result;
        this.searchs = Searchs;
        this.top_searchs = tsearchs;
        this.storage_barrels = storage_barrels;
        this.downloaders = downloaders;
        clients = new ArrayList<Hello_C_I>();
    }


    /**
     * Realiza diferentes funcionalidades tendo em conta a string recebida pelo cliente
     *
     * @param s funcao a realizar
     * @param c cliente que pediu para realizar a funcao
     * @throws RemoteException quando e chamado e nao responde
     */
    public void print_on_server(String s, Hello_C_I c) throws RemoteException {

        String[] received_string = s.split(" ", 0);
        System.out.println(Arrays.toString(received_string));

        //type | login; username | andre; password | moreira
        if (received_string[2].equals("login;")) {
            synchronized (registed_users) {
                if (registed_users.containsKey(received_string[5].replace(";", ""))) {

                    String a = "type | status; logged | on; msg | Login realizado com sucesso!";

                    String user_password = registed_users.get(received_string[5].replace(";", ""));

                    if (user_password.equals(received_string[8])) {
                        try {
                            c.print_on_client(a);
                        } catch (java.rmi.RemoteException e) {
                            System.out.println("Erro a enviar ao cliente.");
                        }

                        //fazer login ao user
                        String username = received_string[5].replace(";", "");
                        String password = received_string[8];
                        System.out.println("username: " + username + "\npassword: " + password + "\n");
                    } else {
                        a = "type | status; register | failed; msg | Username ou password errados.";
                        try {
                            c.print_on_client(a);
                        } catch (java.rmi.RemoteException e) {
                            System.out.println("Erro a enviar ao cliente.");
                        }
                    }

                } else {
                    String a = "type | status; register | failed; msg | Username ou password errados.";
                    try {
                        c.print_on_client(a);
                        System.out.println("dei");
                    } catch (java.rmi.RemoteException e) {
                        System.out.println("Erro a enviar ao cliente.");

                    }
                }
            }

        } else if (received_string[2].equals("search;")) {
            try {
                //usar waits
                synchronized (results) {

                    while (results.isEmpty()) {

                        results.notify();
                        System.out.println("o que vou mandar:" + s);
                        results.add(s);
                        results.wait();

                    }

                    String resultados = results.get(0);
                    results.remove(0);
                    System.out.println(results);

                    synchronized (searchs) {
                        searchs.notifyAll();
                    }

                    try {
                        c.print_on_client("type | status; search | result; " + resultados);
                    } catch (java.rmi.RemoteException e) {
                        System.out.println("Erro a enviar ao cliente.");
                    }

                }

            } catch (Exception re) {
                System.out.println("Error");

            }
        } else if (received_string[2].equals("search1;")) {
            try {
                //usar waits
                synchronized (results) {

                    while (results.isEmpty()) {
                        //System.out.println("o que vou mandar:" + s);
                        results.add(s);
                        results.notify();
                        results.wait();

                    }
                    String resultados = results.get(0);
                    results.remove(0);

                    synchronized (searchs) {
                        searchs.notifyAll();
                    }

                    try {
                        c.print_on_client("type | status; search1 | result; " + resultados);
                    } catch (java.rmi.RemoteException e) {
                        System.out.println("Erro a enviar ao cliente.");
                    }
                }

            } catch (Exception re) {
                System.out.println("Error");

            }
        } else if (received_string[2].equals("regist;")) {

            if (registed_users.containsKey(received_string[5].replace(";", ""))) {
                String a = "type | status; register | failed; msg | O username ja se encontra utilizado.";

                try {
                    c.print_on_client(a);
                } catch (java.rmi.RemoteException e) {
                    System.out.println("Erro a enviar ao cliente.");
                }

            } else {
                registed_users.put(received_string[5].replace(";", ""), received_string[8]);
                String a = "type | status; register | sucess; msg | Registo concluido com sucesso!";
                try {
                    c.print_on_client(a);
                } catch (java.rmi.RemoteException e) {
                    System.out.println("Erro a enviar ao cliente.");
                }
            }
        } else if (received_string[2].equals("logout;")) {

            String a = "type | status; logged | off; msg | Logout realizado com sucesso!";
            try {
                c.print_on_client(a);
            } catch (java.rmi.RemoteException e) {
                System.out.println("Erro a enviar ao cliente.");
            }

        }
        //"type | url; url | " + URL;
        else if (received_string[2].equals("url;")) {

            URLObject url = new URLObject(received_string[5]);
            try {
                QueueInterface server = (QueueInterface) LocateRegistry.getRegistry(6000).lookup("Queue");
                server.addToQueue(url);
            } catch (Exception re) {
                System.out.println("Error");
            }
        } else if (received_string[2].equals("information;")) {

            synchronized (storage_barrels) {

                storage_barrels.notifyAll();

                try {
                    storage_barrels.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                synchronized (top_searchs) {
                    String a = "type | status; information | " + storage_barrels.size() + " ;" + downloaders.size() + " ;" + top_searchs.toString();
                    try {
                        c.print_on_client(a);
                    } catch (java.rmi.RemoteException e) {
                        System.out.println("Erro a enviar ao cliente.");
                    }
                }
            }

        }

    }

    /**
     * Adiciona o cliente que chamou a funcao na lista de clientes.
     *
     * @param name nome do cliente
     * @param c    cliente que chamou funcao
     * @throws RemoteException quando e chamado e nao responde
     */
    public void subscribe(String name, Hello_C_I c) throws RemoteException {
        System.out.println("Subscribing " + name);
        System.out.print("> ");
        clients.add(c);
    }

    /**
     * sempre que e chamada retira o cliente da lista de clientes.
     *
     * @param name nome cliente
     * @param c    cliente que chamou funcao
     * @throws RemoteException quando e chamado e nao responde
     */
    public void unsubscribe(String name, Hello_C_I c) throws RemoteException {
        System.out.println("Unsubscribing " + name);
        System.out.print("> ");
        clients.remove(c);
    }

    /**
     * Sempre que um dowloader se connecta adiciona o downloader a lista de downloaders.
     *
     * @param s string recevida
     * @param c cliente que chamou funcao
     * @throws RemoteException quando e chamado e nao responde
     */
    public void downloader_subscribe(String s, Hello_C_I c) throws RemoteException {
        synchronized (downloaders) {
            downloaders.add(c);
        }
        System.out.println("Subscribing Downloader," + downloaders.size());
    }

    // =======================================================
    @Override
    public void run() {
        String a;

        try (Scanner sc = new Scanner(System.in)) {

            h = new server(results, searchs, top_searchs, storage_barrels, downloaders);
            Registry r = LocateRegistry.createRegistry(7000);
            r.rebind("XPTO", h);
            System.out.println("Hello Server ready.");
            pa = new pagina_adminstracao(searchs, storage_barrels, top_searchs, downloaders);
            t0 = new Thread(pa);
            t0.start();

            while (true) {

            }
        } catch (Exception re) {
            System.out.println("Exception in HelloImpl.main: " + re);
        }
    }
}