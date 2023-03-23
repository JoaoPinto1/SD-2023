import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;
import java.net.*;
import java.io.*;

public class Storage_Barrels extends UnicastRemoteObject implements Hello_C_I {

    public static Hello_S_I h;
    public static Storage_Barrels c;

    Storage_Barrels() throws RemoteException {
        super();
    }

    /**
     * Realiza diferentes operacoes tendo em conta mensagem recevida pelo servidor
     */
    public void print_on_client(String s) throws RemoteException {

        //"start_search"
        System.out.println("recebi o pedido.");

        h.print_on_server("Esta aqui a mensagem pedida.", c);

    }

    /**
     * Inicia conexao com servidor e pergunta ao cliente o que ele deseja fazer, realiza diferentes operacoes tendo em conta a escolha do cliente
     * @param args
     */
    public static void main(String args[]) {

        try{

            h = (Hello_S_I) LocateRegistry.getRegistry(7001).lookup("XPT");
            c = new Storage_Barrels();
            h.subscribe("Storage Barrel", (Hello_C_I) c);
            System.out.println("Storage Barrel Ready");

            while (true) {
                
            }
        
        } catch (Exception e) {
            System.out.println("Exception in main: " + e);
        }
    }
}
