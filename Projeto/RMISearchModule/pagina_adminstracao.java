package RMISearchModule;

import Downloader.Downloader;
import RMIClient.Hello_C_I;

import java.rmi.RemoteException;
import java.util.*;

public class pagina_adminstracao implements Runnable {

    public final List<String> searchs;

    public ArrayList<Hello_C_I> storage_barrels;
    public ArrayList<Hello_C_I> downloaders;
    public Map<String, String> top_searchs;

    public pagina_adminstracao(List<String> searchs, ArrayList<Hello_C_I> storage_barrels , Map<String, String> tsearchs , ArrayList<Hello_C_I> downloaders) {
        super();
        this.searchs = searchs;
        this.storage_barrels = storage_barrels;
        this.top_searchs = tsearchs;
        this.downloaders = downloaders;
    }

    /**
     * Recebe lista com pesquisas e devolve as 10 pesquisas mais realizadas.
     * @param arr
     * @return lista com as 10 pesquisas mais realizadas.
     */
    public static LinkedHashMap<String, String> top10(List<String> arr) {
        // Count the occurrences of each element in the list
        Map<String, Integer> counts = new HashMap<>();
        for (String elem : arr) {
            counts.put(elem, counts.getOrDefault(elem, 0) + 1);
        }

        // Get the 10 most common elements and their counts
        List<Map.Entry<String, Integer>> entries = new ArrayList<>(counts.entrySet());
        entries.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));
        LinkedHashMap<String, String> top10 = new LinkedHashMap<>();

        for (int i = 0; i < Math.min(entries.size(), 10); i++) {
            Map.Entry<String, Integer> entry = entries.get(i);
            top10.put(entry.getKey(), Integer.toString(entry.getValue()));
        }

        // Return the map of the top 10 elements and their counts as strings
        return top10;
    }
    /**
     * Remove os Storage Barrels que nao estao ativos.
     * Recebe como parametro os barrels nao ativos.
     * @param removedBarrels
     */
    private void RemoveBarrels(List<Hello_C_I> removedBarrels){
        synchronized (storage_barrels) {
            for (Hello_C_I s : removedBarrels) {
                storage_barrels.remove(s);
            }
        }
    }

    /**
     * Remove os downloaders que nao estao ativos.
     * Recebe como parametro os downloaders nao ativos.
     * @param removedDownloader
     */
    private void RemoveDownloader(List<Hello_C_I> removedDownloader){
        synchronized (downloaders) {
            for (Hello_C_I s : removedDownloader) {
                downloaders.remove(s);
            }
        }
    }

    /**
     *Verifica os Downloaders para ver se estao ativos
     * Se o downloader nao estiver ativo removemos da lista.
     */
    private void check_downloaders(){

        List<Hello_C_I> removed_downloaders = new ArrayList<>();

        synchronized (downloaders) {
            for (Hello_C_I s : downloaders) {

                try {
                    s.ping();
                } catch (RemoteException e) {
                    removed_downloaders.add(s);
                }

            }
            RemoveDownloader(removed_downloaders);
        }
    }

    /**
     *Verifica os storage barrels para ver se estao ativos
     * Se o storage barrel nao estiver ativo removemos da lista.
     * */
    private void check_storage_barrels(){

        List<Hello_C_I> removed_barrels = new ArrayList<>();

        synchronized (storage_barrels) {
            for (Hello_C_I s : storage_barrels) {

                try {
                    s.ping();
                } catch (RemoteException e) {
                    removed_barrels.add(s);
                }

            }
            RemoveBarrels(removed_barrels);
        }
    }

    @Override
    public void run() {

        System.out.println("Pagina de adminstracao ativa!");

        while (true) {

            synchronized (storage_barrels) {

                try {
                    storage_barrels.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                synchronized (top_searchs) {
                    synchronized (searchs) {

                        LinkedHashMap<String, String> top10Map = top10(searchs);
                        top_searchs.clear();
                        top_searchs.putAll(top10Map);

                        check_storage_barrels();
                        check_downloaders();

                        storage_barrels.notifyAll();

                    }

                }

            }
        }
    }
}
