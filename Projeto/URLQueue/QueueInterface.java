package URLQueue;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface da URLQueue
 */
public interface QueueInterface extends Remote {
    /**
     * Adiciona um URL a Queue
     * @param url URL a adicionar
     * @throws Exception No caso de algum erro
     */
    void addToQueue(URLObject url) throws Exception;

    /**
     * Remove URL da queue
     * @return URL
     * @throws RemoteException Ocorrencia de Erro no RMI
     * @throws InterruptedException Ocorrencia de Erro no wait()
     */
    URLObject removeFromQueue() throws RemoteException, InterruptedException;

    /**
     * Se a queue esta vazia
     * @return True se estiver vazia, caso contrario False
     * @throws RemoteException Conexao RMI
     */
    boolean isQueueEmpty() throws RemoteException;
}
