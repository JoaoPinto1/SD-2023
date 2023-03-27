import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.*;

import RMIClient.Hello_C_I;
import RMIClient.Hello_S_I;
import URLQueue.*;


public class server extends UnicastRemoteObject implements Hello_S_I, Runnable {

    public ArrayList<Hello_C_I> clients;
    public HashMap<String, String> registed_users = new HashMap<String, String>();
    public server h;
    public final List<String> results;

    public server(List<String> Result) throws RemoteException {
        super();
        this.results = Result;
        clients = new ArrayList<Hello_C_I>();
    }

    /**
     * Realiza as diferentes funcoes tendo em conta a mensagem recevida.
     */
    public void print_on_server(String s , Hello_C_I c) throws RemoteException {
        System.out.println("> " + s);

        String[] received_string = s.split(" ", 0);
        System.out.println(Arrays.toString(received_string));
        //type | login; username | andre; password | moreira
        if(received_string[2].equals("login;")){
            synchronized (registed_users) {
                if (registed_users.containsKey(received_string[5].replace(";", ""))) {

                    String a = "type | status; logged | on; msg | Login realizado com sucesso!";

                    String user_password = registed_users.get(received_string[5].replace(";", ""));

                    if (user_password.equals(received_string[8])) {
                        c.print_on_client(a);

                        //fazer login ao user
                        String username = received_string[5].replace(";", "");
                        String password = received_string[8];
                        System.out.println("username: " + username + "\npassword: " + password + "\n");
                    } else {
                        a = "type | status; register | failed; msg | Username ou password errados.";
                        c.print_on_client(a);
                    }

                } else {
                    String a = "type | status; register | failed; msg | Username ou password errados.";
                    c.print_on_client(a);
                }
            }

        }
        else if(received_string[2].equals("status;")){

        }
        else if(received_string[2].equals("url_list;")){

        }
        else if(received_string[2].equals("search;")){
            try{
                //usar waits
                synchronized (results){

                    while(results.isEmpty()){
                        results.notify();
                        System.out.println("o que vou mandar:" + s);
                        results.add(s);
                        results.wait();

                    }
                    String resultados = results.get(0);
                    c.print_on_client("type | status; search | result; " + resultados);
                    results.remove(0);
                }

            }catch(Exception re){
                System.out.println("Error");

            }
        }
        else if(received_string[2].equals("regist;")){

            if (registed_users.containsKey(received_string[5].replace(";", ""))) {
                String a = "type | status; register | failed; msg | O username ja se encontra utilizado.";
                c.print_on_client(a);
            } else {
                registed_users.put(received_string[5].replace(";", ""), received_string[8]);
                String a = "type | status; register | sucess; msg | Registo concluido com sucesso!";
                c.print_on_client(a);
            }
        }
        else if(received_string[2].equals("logout;")){

            String a = "type | status; logged | off; msg | Logout realizado com sucesso!";
            c.print_on_client(a);

        }
        //"type | url; url | " + URL;
        else if(received_string[2].equals("url;")){

            URLObject url = new URLObject(received_string[5]);
            try {
                QueueInterface server = (QueueInterface) LocateRegistry.getRegistry(6000).lookup("Queue");
                server.addToQueue(url);
            }catch(Exception re){
                System.out.println("Error");
            }
        }
        else if(received_string[2].equals("information;")){

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
            h = new server(results);
            Registry r = LocateRegistry.createRegistry(7000);
            r.rebind("XPTO", h);

            System.out.println("Hello Server ready.");

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