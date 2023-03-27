import java.rmi.server.*;
import java.rmi.*;
import java.util.ArrayList;
import java.util.List;


public class search_module extends UnicastRemoteObject implements Search_Module_Remote{

    private Thread t1,t2;
    private server sc;
    private serverb sb;
    public List <String> results;
    /**
     * Inicia os dois server necessarios, o que espera do search module e o dos barrels.
     * @throws RemoteException
     */
    public search_module() throws RemoteException{

        super();
        results = new ArrayList<String>();
        sb = new serverb(results);
        t1 = new Thread(sb);
        sc = new server(results);
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
