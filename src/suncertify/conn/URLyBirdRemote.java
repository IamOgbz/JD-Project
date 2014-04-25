package suncertify.conn;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface to define methods for accessing the database.
 * 
 * @author Emmanuel
 */
public interface URLyBirdRemote extends Remote {
    
    public RemoteDBAccess getClient() throws RemoteException;
    
}
