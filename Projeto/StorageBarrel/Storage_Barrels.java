package StorageBarrel;
import java.io.IOException;
import java.rmi.server.*;
import java.rmi.*;
import java.sql.SQLException;


public class Storage_Barrels extends UnicastRemoteObject implements Storage_Barrels_Remote {

    private Thread t1,t2;
    private Storage_Barrels_RMI sRMI;
    private Storage_Barrels_Multicast sMUL;

    /**
     * Inicia os dois RMISearchModule.server necessarios, o que espera do search module e o dos barrels.
     * @throws RemoteException
     */
    public Storage_Barrels(int nBarrel) throws IOException, SQLException {

        super();
        sMUL = new Storage_Barrels_Multicast(nBarrel);
        t1 = new Thread(sMUL);
        sRMI = new Storage_Barrels_RMI(nBarrel);
        t2 = new Thread(sRMI);
        t1.start();
        t2.start();

    }

    public static void main(String[] args)
    {
        try{
            Storage_Barrels s = new Storage_Barrels(Integer.parseInt(args[0]));
        }
        catch(Exception re){

        }
    }

}
