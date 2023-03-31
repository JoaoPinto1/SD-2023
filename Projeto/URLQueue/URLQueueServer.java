package URLQueue;

import java.io.Serial;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * Classe dos metodos remotos RMI da URLQueue
 */
public class URLQueueServer extends UnicastRemoteObject implements QueueInterface {
    private final URLQueue queue;
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Contrutor do servidor RMI URLQueue
     *
     * @throws RemoteException Erro na conexao RMI
     */
    public URLQueueServer() throws RemoteException {
        super();
        queue = new URLQueue();
    }

    /**
     * Metodo sincronizado para a adicao de URL na queue
     *
     * @param url URL a adicionar
     * @throws RemoteException Erro na conexao RMI
     */
    public synchronized void addToQueue(URLObject url) throws RemoteException {
        try {
            queue.add(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
        notifyAll();
    }

    /**
     * Metodo sincronizado para a remocao de URLs da queue
     *
     * @return URL removido da fila
     * @throws RemoteException      Erro na conexao RMI
     * @throws InterruptedException Interrupcao do programa durante o wait()
     */
    public synchronized URLObject removeFromQueue() throws RemoteException, InterruptedException {
        while (isQueueEmpty()) {
            wait();
        }
        URLObject url = new URLObject("");
        try {
            url = queue.remove();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return url;
    }

    /**
     * Metodo sincronizado para saber se a fila esta vazia
     *
     * @return True se estiver vazia, False caso contrario
     * @throws RemoteException Erro na conexao RMI
     */
    public synchronized boolean isQueueEmpty() throws RemoteException {
        return queue.isEmpty();
    }
}
