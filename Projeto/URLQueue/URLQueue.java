package URLQueue;


import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedList;
import java.util.Queue;

public class URLQueue {
    private final Queue<URLObject> queue;  //fila

    /**
     * Construtor de uma fila FIFO
     *
     */
    public URLQueue(){
        this.queue = new LinkedList<>();
    }


    public Queue<URLObject> getQueue() {
        return queue;
    }

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

