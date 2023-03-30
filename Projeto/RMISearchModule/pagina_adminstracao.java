package RMISearchModule;

import java.util.*;

public class pagina_adminstracao implements Runnable {

    public final List<String> searchs;
    public Map<String, String> estado_sistema;

    public Map<String, String> top_searchs;

    public pagina_adminstracao(List<String> searchs, Map<String, String> Estado , Map<String, String> tsearchs) {
        super();
        this.searchs = searchs;
        this.estado_sistema = Estado;
        this.top_searchs = tsearchs;
    }

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

    @Override
    public void run() {

        System.out.println("Pagina de adminstracao ativa!");

        while (true) {

            synchronized (estado_sistema) {

                try {
                    estado_sistema.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                synchronized (top_searchs) {
                    synchronized (searchs) {

                        LinkedHashMap<String, String> top10Map = top10(searchs);
                        top_searchs.clear();
                        top_searchs.putAll(top10Map);


                    }

                }

            }
        }
    }
}
