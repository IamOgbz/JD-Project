package suncertify.conn;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import suncertify.db.Occupancy;
import suncertify.db.URLyBirdDBAccess;
import suncertify.db.URLyBirdData;

/**
 * Used to interface between the client and the data layer
 *
 * @author Emmanuel
 */
public class RemoteData extends UnicastRemoteObject implements RemoteDBAccess {

    /**
     * the object that accesses out database
     */
    private final URLyBirdDBAccess db;

    /**
     * The Logger instance. All log messages from this class are routed through
     * this member. The Logger namespace is <code>suncertify.db</code>.
     */
    private static final Logger log = Logger.getLogger("suncertify.conn");

    /**
     * Instantiates a remote object to be used for invoking the server remotely.
     *
     * @param databasePath the database location.
     * @throws RemoteException
     */
    public RemoteData(String databasePath) throws RemoteException {
        try {
            this.db = new URLyBirdData(databasePath);
        } catch (IOException ex) {
            throw new RemoteException("Unable to access database file");
        }
    }

    @Override
    public Occupancy getOccupancy(long address) {
        return db.getOccupancy(address);
    }

    @Override
    public Collection<Occupancy> getOccupancies() {
        return db.getOccupancies();
    }

    @Override
    public Collection<Occupancy> searchOccupancies(String... params) {
        return db.searchOccupancies(params);
    }

}
