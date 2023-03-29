import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Random;
import java.util.*;
import RMIClient.Hello_C_I;
import RMIClient.Hello_S_I;


public class serverb extends UnicastRemoteObject implements Hello_S_I, Hello_C_I, Runnable {

    public final ArrayList<Hello_C_I> clients_RMI;
    public serverb h;
    public final List<String> results;
    public final List<String> searchs;
    public Map<String, String> estado_sistema = new HashMap<>();

    public serverb(List<String> Result ,List<String> Searchs,  Map<String, String> Estado) throws RemoteException {
        super();
        this.results = Result;
        this.searchs = Searchs;
        this.estado_sistema = Estado;
        clients_RMI = new ArrayList<Hello_C_I>();
    }

    public void print_on_client(String s) throws RemoteException {
        System.out.println(s);
    }

    public void print_on_server(String s, Hello_C_I c) throws RemoteException {

        System.out.println("serverb " + s);

        synchronized (results) {
            results.add(s);
            results.notify();
        }

    }

    public void subscribe(String name, Hello_C_I c) throws RemoteException {

        System.out.println("Subscribing " + name);
        System.out.print("> ");

        synchronized (estado_sistema) {
            estado_sistema.put((name + clients_RMI.size()), "7001");
            estado_sistema.notifyAll();;
        }

        synchronized (clients_RMI) {
            clients_RMI.add(c);
            System.out.println("Storage Barrel adicionado , " + clients_RMI.size());
        }
    }

    public void unsubscribe(String name, Hello_C_I c) throws RemoteException {
        System.out.println("Unsubscribing " + name);
        System.out.print("> ");
        synchronized (clients_RMI) {
            clients_RMI.remove(c);
        }
    }

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

            h = new serverb(results , searchs , estado_sistema);

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
                    Thread.sleep(1000);
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

                        synchronized (estado_sistema){
                            estado_sistema.notifyAll();
                        }

                        connect = 1;
                    } catch (ConnectException e) {

                        System.out.println("Storage Barrel Invalido!");
                        h.unsubscribe("Storage Barrel", client);
                        System.out.println("looking for new barrel!");

                        client = RandomClient();

                        while (client == null) {

                            try {
                                System.out.println("looking for new barrel!");
                                Thread.sleep(2000);
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