package RMIClient;

import java.io.Serializable;
import java.rmi.*;

public interface Hello_C_I extends Remote, Serializable {
    /**
     * Servidor fornece string ao cliente
     *
     * @param s String a enviar
     * @throws java.rmi.RemoteException Erro RMI
     */
    void print_on_client(String s) throws java.rmi.RemoteException;

    /**
     * E realizado um "ping", devolve excecao caso o cliente esteja em baixo e nao devolve nada se tiver ativo
     *
     * @throws java.rmi.RemoteException Erro no RMI
     */
    void ping() throws java.rmi.RemoteException;
}
