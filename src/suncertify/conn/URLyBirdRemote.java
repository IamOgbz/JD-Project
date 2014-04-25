package suncertify.conn;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface to define methods for accessing the database.
 *
 * @author Emmanuel
 */
public interface URLyBirdRemote extends Remote {

    /**
     * Gets the DBAcess object for accessing the database file.
     *
     * @return RemoteDBAccess wrapper object
     * @throws RemoteException
     */
    public RemoteDBAccess getClient() throws RemoteException;

}
