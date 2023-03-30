package RMIClient;

import java.io.Serializable;
import java.rmi.*;

public interface Hello_C_I extends Remote , Serializable {
	public void print_on_client(String s) throws java.rmi.RemoteException;
	public void ping() throws  java.rmi.RemoteException;
}