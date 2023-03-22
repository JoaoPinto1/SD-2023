import java.io.Serializable;
import java.rmi.registry.LocateRegistry;
import java.util.*;
import java.rmi.registry.Registry;
import java.rmi.server.*;
import java.rmi.*;


public class search_module extends UnicastRemoteObject implements Search_Module_Remote{

    private Thread t1,t2;
    private server sc;
    private serverb sb;   
    
    /**
     * Inicia os dois server necessarios, o que espera do search module e o dos barrels.
     * @throws RemoteException
     */
    public search_module() throws RemoteException{

        super();
        sb = new serverb();
        t1 = new Thread(sb);
        sc = new server();
        t2 = new Thread(sc);
        t1.start();
        t2.start();

    }

    public static void main(String[] args)
    {
        try{
            search_module s = new search_module();
        }
        catch(Exception re){

        }
    }

}
