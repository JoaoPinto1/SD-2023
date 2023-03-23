import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Scanner;
import  java.util.Random;
import java.util.*;


public class serverb extends UnicastRemoteObject implements Hello_S_I, Hello_C_I, Runnable {

    public ArrayList<Hello_C_I> clients;
    public HashMap<String, String> registed_users = new HashMap<String, String>();
    public serverb h;


    public serverb() throws RemoteException {
        super();
        clients = new ArrayList<Hello_C_I>();
    }

    public void print_on_client(String s) throws RemoteException{
        System.out.println(s);
    }

    public void print_on_server(String s , Hello_C_I c) throws RemoteException {

        System.out.println("> " + s);

        /**
         * Inicia conexao entre search module e storage barrel
         */
        if(s.equals("search")){

            try{
                Hello_S_I server = (Hello_S_I) LocateRegistry.getRegistry(7000).lookup("XPTO");
                server.subscribe("Barrels Server", (Hello_C_I) h); 

                Random rand = new Random();
                int rand_int = rand.nextInt(clients.size()-1);

                System.out.println(clients.size() + " |||| " + rand_int );

                Hello_C_I client = clients.get(rand_int);
                client.print_on_client("search_start");

                server.print_on_server("Enviei uma mensagem|" , (Hello_C_I) h);
                server.unsubscribe("Barrels Server", (Hello_C_I) h);
            }catch(Exception re){
                System.out.println("Error");
            }

        }


    }
    

    public void subscribe(String name, Hello_C_I c) throws RemoteException {
        System.out.println("Subscribing " + name);
        System.out.print("> ");
        clients.add(c);
    }

    public void unsubscribe(String name, Hello_C_I c) throws RemoteException {
        System.out.println("Unsubscribing " + name);
        System.out.print("> ");
        clients.remove(c);
    }

    // =======================================================
    @Override
    public void run() {
        String a;

        try (Scanner sc = new Scanner(System.in)) {

            h = new serverb();

            Registry r = LocateRegistry.createRegistry(7001);
            r.rebind("XPT", h);
               
            System.out.println("Hello Barrel_Server ready.");

            while (true) {

                System.out.print(">");
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