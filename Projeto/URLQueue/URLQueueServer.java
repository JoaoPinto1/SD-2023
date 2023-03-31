package URLQueue;

import java.io.Serial;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * Classe dos métodos remotos RMI da URLQueue
 */
public class URLQueueServer extends UnicastRemoteObject implements QueueInterface{
    private final URLQueue queue;
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Contrutor do servidor RMI URLQueue
     * @throws RemoteException Erro na conexão RMI
     */
    public URLQueueServer() throws RemoteException{
        super();
        queue = new URLQueue();
    }

    /**
     * Método sincronizado para a adição de URL na queue
     * @param url URL a adicionar
     * @throws RemoteException Erro na conexão RMI
     */
    public synchronized void addToQueue(URLObject url) throws RemoteException {
        try{
            queue.add(url);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        notifyAll();
    }

    /**
     * Método sincronizado para a remoção de URLs da queue
     * @return URL removido da fila
     * @throws RemoteException Erro na conexão RMI
     * @throws InterruptedException Interrupção do programa durante o wait()
     */
    public synchronized URLObject removeFromQueue() throws RemoteException, InterruptedException {
        while (isQueueEmpty()) {
            wait();
        }
        URLObject url = new URLObject("");
        try {
            url = queue.remove();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return url;
    }

    /**
     * Método sincronizado para saber se a fila está vazia
     * @return True se estiver vazia, False caso contrário
     * @throws RemoteException Erro na conexão RMI
     */
    public synchronized boolean isQueueEmpty() throws RemoteException {
        return queue.isEmpty();
    }
}
