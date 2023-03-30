package RMISearchModule;

import java.rmi.server.*;
import java.rmi.*;
import java.util.*;


public class search_module extends UnicastRemoteObject implements Search_Module_Remote {

    private Thread t1, t2;
    private server sc;
    private serverb sb;
    public List<String> results;
    public final List<String> searchs;
    public Map<String, String> estado_sistema = new HashMap<>();
    public Map<String, String> top_searchs = new HashMap<>();

    /**
     * Inicia os dois RMISearchModule.server necessarios, o que espera do search module e o dos barrels.
     *
     * @throws RemoteException
     */
    public search_module() throws RemoteException {

        super();
        results = new ArrayList<String>();
        searchs = new ArrayList<String>();
        estado_sistema = new HashMap<String, String>();
        top_searchs = new HashMap<String , String>();
        sb = new serverb(results , searchs , estado_sistema);
        t1 = new Thread(sb);
        sc = new server(results , searchs , estado_sistema , top_searchs);
        t2 = new Thread(sc);
        t1.start();
        t2.start();

    }

    public static void main(String[] args) {
        try {
            search_module s = new search_module();
        } catch (Exception re) {

        }
    }

}
