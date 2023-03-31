package URLQueue;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface da URLQueue
 */
public interface QueueInterface extends Remote {
    /**
     * Adiciona um URL à Queue
     * @param url URL a adicionar
     * @throws Exception No caso de algum erro
     */
    void addToQueue(URLObject url) throws Exception;

    /**
     * Remove URL da queue
     * @return URL
     * @throws RemoteException Ocorrência de Erro no RMI
     * @throws InterruptedException Ocorrência de Erro no wait()
     */
    URLObject removeFromQueue() throws RemoteException, InterruptedException;

    /**
     * Se a queue está vazia
     * @return True se estiver vazia, caso contrário False
     * @throws RemoteException Conexão RMI
     */
    boolean isQueueEmpty() throws RemoteException;
}
