package URLQueue;


import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Classe da Fila URL
 */
public class URLQueue {
    private final Queue<URLObject> queue;  //fila

    /**
     * Construtor da fila de URLs que é uma Lista ligada
     *
     */
    public URLQueue(){
        this.queue = new LinkedList<>();
    }

    /**
     * Getter da fila
     * @return Fila URLs
     */
    public Queue<URLObject> getQueue() {
        return queue;
    }

    /**
     * Se a fila está vazia
     * @return True se vazia, False caso contrário
     */
    public boolean isEmpty(){
        return getQueue().isEmpty();
    }

    /**
     * Insere url na fila
     *
     * @param url url a ser inserido
     */
    public void add(URLObject url){
        getQueue().add(url);
    }

    /**
     * Remove elemento da fila
     *
     * @return url
     */
    public URLObject remove() {
        return getQueue().remove();
    }


}

