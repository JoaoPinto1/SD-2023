package RMISearchModule;

import RMIClient.Hello_C_I;

import java.rmi.server.*;
import java.rmi.*;
import java.util.*;

/**
 * Classe search_module
 */
public class search_module extends UnicastRemoteObject implements Search_Module_Remote {

    private Thread t1, t2;
    private server sc;
    private serverb sb;
    public List<String> results;
    public final List<String> searchs;
    public Map<String, String> top_searchs = new HashMap<>();
    public ArrayList<Hello_C_I> storage_barrels;
    public ArrayList<Hello_C_I> downloaders;

    /**
     * Inicia os dois RMISearchModule.server necessarios, o dos clientes e o dos barrels.
     *
     * @throws RemoteException quando e chamado e nao responde
     */
    public search_module() throws RemoteException {

        super();
        results = new ArrayList<String>();
        searchs = new ArrayList<String>();
        top_searchs = new HashMap<String, String>();
        storage_barrels = new ArrayList<Hello_C_I>();
        downloaders = new ArrayList<Hello_C_I>();
        sb = new serverb(results, searchs, storage_barrels);
        t1 = new Thread(sb);
        sc = new server(results, searchs, top_searchs, storage_barrels, downloaders);
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
