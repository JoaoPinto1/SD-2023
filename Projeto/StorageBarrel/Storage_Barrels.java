package StorageBarrel;
import java.rmi.server.*;
import java.rmi.*;


public class Storage_Barrels extends UnicastRemoteObject implements Storage_Barrels_Remote {

    private Thread t1,t2;
    private Storage_Barrels_RMI sRMI;
    private Storage_Barrels_Multicast sMUL;

    /**
     * Inicia os dois server necessarios, o que espera do search module e o dos barrels.
     * @throws RemoteException
     */
    public Storage_Barrels() throws RemoteException{

        super();
        sMUL = new Storage_Barrels_Multicast();
        t1 = new Thread(sMUL);
        sRMI = new Storage_Barrels_RMI();
        t2 = new Thread(sRMI);
        t1.start();
        t2.start();

    }

    public static void main(String[] args)
    {
        try{
            Storage_Barrels s = new Storage_Barrels();
        }
        catch(Exception re){

        }
    }

}
