package URLQueue;


import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class URLQueue extends UnicastRemoteObject implements Interface {
    private final URLObject[] queue;  //fila
    private final int size; //tamanho da fila
    private int head; //ponteiro para o inicio da fila
    private int tail; //ponteiro para o fim da fila
    private boolean empty; //fila vazia ou não
    private Callback callback;

    /**
     * Construtor de uma fila FIFO
     *
     * @param size numero maximo de urls na fila
     */
    public URLQueue(int size) throws RemoteException{
        super();
        this.queue = new URLObject[size];
        this.size = size;
        this.head = 0;
        this.tail = 0;
        this.empty = true;
    }

    public URLObject getNextItem(Callback callback) throws Exception {
        this.callback = callback;
        return remove_queue();
    }

    private void notifyNewItem(URLObject item) throws RemoteException {
        if (callback != null) {
            callback.onNewItem(item);
        }
    }

    /**
     * Insere url na fila
     *
     * @param url url a ser inserido
     * @throws Exception Quando a lista está cheia
     */
    public void insert_queue(URLObject url) throws Exception {
        if (head == tail && !empty) {
            throw new Exception("Lista cheia");
        }

        queue[tail] = url;
        tail = (tail + 1) % size;
        empty = false;
        notifyNewItem(url);
    }

    /**
     * Remove elemento da fila
     *
     * @return url
     * @throws Exception Se a lista estiver vazia
     */
    public URLObject remove_queue() throws Exception {
        if (empty) {
            throw new Exception("Lista vazia");
        }

        URLObject url = queue[head];
        head = (head + 1) % size;
        empty = (head == tail);
        return url;
    }
}

