/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package suncertify.conn;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collection;
import java.util.logging.Logger;
import suncertify.gui.Occupancy;
import suncertify.db.URLyBirdDBAccess;
import suncertify.db.URLyBirdData;

/**
 * For client to remotely access database.
 *
 * @author Emmanuel
 */
public class RemoteData extends UnicastRemoteObject implements RemoteDBAccess {

    /**
     * The database access object being wrapped.
     */
    private final URLyBirdDBAccess database;

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
    public RemoteData(String databasePath) throws RemoteException {
        try {
            database = new URLyBirdData(databasePath);
        } catch (IOException ex) {
            throw new RemoteException("Unable to access database file.");
        }
    }

    @Override
    public Occupancy getOccupancy(long address)
            throws RemoteException, IOException {
        return database.getOccupancy(address);
    }

    @Override
    public void setOccupancy(Occupancy occupancy, boolean append)
            throws RemoteException, IOException {
        database.setOccupancy(occupancy, append);
    }

    @Override
    public Collection<Occupancy> getOccupancies()
            throws RemoteException, IOException {
        return database.getOccupancies();
    }

    @Override
    public Collection<Occupancy> searchOccupancies(String... params)
            throws RemoteException, IOException {
        return database.searchOccupancies(params);
    }

}
