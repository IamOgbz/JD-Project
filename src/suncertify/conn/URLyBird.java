/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package suncertify.conn;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.logging.Logger;

/**
 * Class to access underlying database access class.
 * @author Emmanuel
 */
public class URLyBird implements URLyBirdRemote {

    /**
     * the database location.
     */
    private final String databasePath;

    /**
     * The Logger instance. All log messages from this class are routed through
     * this member. The Logger namespace is <code>suncertify.db</code>.
     */
    private static final Logger log = Logger.getLogger("suncertify.conn");

    /**
     * Instantiates a URLyBird object to be used for accessing the database.
     *
     * @param databasePath the database location.
     * @throws RemoteException
     */
    public URLyBird(String databasePath) throws RemoteException {
        this.databasePath = databasePath;
    }

    @Override
    public RemoteDBAccess getClient() throws RemoteException {
        try {
            return new RemoteData(databasePath);
        } catch (IOException ex) {
            throw new RemoteException("Unable to access database file.");
        }
    }

}
