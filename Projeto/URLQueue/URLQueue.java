package URLQueue;


import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class URLQueue {
    private final URLObject[] queue;  //fila
    private final int size; //tamanho da fila
    private int head; //ponteiro para o inicio da fila
    private int tail; //ponteiro para o fim da fila
    public boolean empty; //fila vazia ou não

    /**
     * Construtor de uma fila FIFO
     *
     * @param size numero maximo de urls na fila
     */
    public URLQueue(int size) throws RemoteException{
        this.queue = new URLObject[size];
        this.size = size;
        this.head = 0;
        this.tail = 0;
        this.empty = true;
    }


    /**
     * Insere url na fila
     *
     * @param url url a ser inserido
     */
    public void add(URLObject url) throws Exception {
        if (head == tail && !empty) {
            throw new Exception("Lista cheia");
        }

        queue[tail] = url;
        tail = (tail + 1) % size;
        empty = false;
    }

    /**
     * Remove elemento da fila
     *
     * @return url
     * @throws Exception Se a lista estiver vazia
     */
    public URLObject remove() throws Exception {
        if (empty) {
            throw new Exception("Lista vazia");
        }

        URLObject url = queue[head];
        head = (head + 1) % size;
        empty = (head == tail);
        return url;
    }


}

