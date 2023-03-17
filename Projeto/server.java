import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Scanner;


public class server extends UnicastRemoteObject implements Hello_S_I {
    public ArrayList<Hello_C_I> clients;


    public server() throws RemoteException {
        super();
        clients = new ArrayList<Hello_C_I>();
    }

    public void print_on_server(String s) throws RemoteException {
        System.out.println("> " + s);
        for (Hello_C_I client : clients) {
            client.print_on_client(s);
        }
    }

    public void subscribe(String name, Hello_C_I c) throws RemoteException {
        System.out.println("Subscribing " + name);
        System.out.print("> ");
        clients.add(c);
    }

    // =======================================================

    public static void main(String args[]) {
        String a;

        /*
        System.getProperties().put("java.security.policy", "policy.all");
        System.setSecurityManager(new RMISecurityManager());
        */

        try (Scanner sc = new Scanner(System.in)) {
            //User user = new User();
            server h = new server();
            Registry r = LocateRegistry.createRegistry(7000);
            r.rebind("XPTO", h);
            System.out.println("Hello Server ready.");
            while (true) {
                System.out.print("> ");
                a = sc.nextLine();
                for (Hello_C_I client : h.clients) {
                    client.print_on_client(a);
                }
            }
        } catch (Exception re) {
            System.out.println("Exception in HelloImpl.main: " + re);
        } 
    }
}