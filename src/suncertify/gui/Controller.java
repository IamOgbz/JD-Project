package suncertify.gui;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.logging.Logger;
import suncertify.conn.DBConnection;
import suncertify.conn.RemoteDBAccess;
import suncertify.db.URLyBirdDBAccess;
import suncertify.db.URLyBirdData;

/**
 * Class used to send client requests to database object.
 *
 * @author Emmanuel
 */
public class Controller {

    /**
     * The object that accesses our database.
     */
    private URLyBirdDBAccess db;

    /**
     * Connection state of the controller.
     */
    private boolean connected;

    /**
     * The Logger instance. All log messages from this class are routed through
     * this member. The Logger namespace is <code>suncertify.db</code>.
     */
    private static final Logger log = Logger.getLogger("suncertify.gui");

    /**
     * Creates instance of this class using the connection type to retrieve a
     * data object.
     *
     * @param connectionType the type of connection to retrieve
     * @param location the location of the database
     * @param port the port used in a server client connection
     */
    public Controller(DBConnection.Type connectionType,
            String location, String port) {
        connected = false;
        String s = "";
        switch (connectionType) {
            case DIRECT:
                try {
                    db = new URLyBirdData(location);
                    connected = true;
                } catch (IOException ex) {
                    Application.handleException(
                            "Unable to read database file", ex, null);
                }
                break;
            case NETWORK:
                try {
                    RemoteDBAccess remote = DBConnection
                            .getRemote(location, port);
                    db = (URLyBirdDBAccess) remote;
                    connected = true;
                } catch (RemoteException re) {
                    Application.handleException(
                            "Unable to connect to server", re, null);
                } catch (IllegalArgumentException iae) {
                    Application.handleException("Port (" + port + ") error, "
                            + iae.getMessage(), iae, null);
                }
                break;
            default:
                // should never happen
                Exception e = new IllegalStateException("Unknown connection type");
                Application.handleException(
                        "Unable to start connection", e, null);
        }
    }

    /**
     * Checks if the controller is connected to the database.
     *
     * @return true if the controller successfully connected to the database
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * Sets up a table model with column specifications and data gotten from the
     * database.
     *
     * @return TableModel consisting of table specifications and data
     */
    public OccupancyTable getTable() {
        return searchTable(null, null);
    }

    /**
     * Searches and sets up a table with column specifications and data gotten
     * from searching the database.
     *
     * @param hotel the name of the hotel to match
     * @param city the name of the hotel location to match
     * @return OccupancyTable consisting of table specs and search results
     */
    public OccupancyTable searchTable(String hotel, String city) {
        try {
            return new OccupancyTable(db.searchOccupancies(hotel, city));
        } catch (IOException ex) {
            Application.handleException("Database connection lost", ex, null);
            return null;
        }
    }

    /**
     * Book an occupancy by setting a customer id.
     *
     * @param o the occupancy to update
     * @param cid the customer id
     */
    public void book(Occupancy o, String cid) {
        try {
            o = db.getOccupancy(o.getAddress());
            if (o.hasOwner()) {
                Application.handleException(
                        "Occupancy already booked", null, null);
            } else {
                o.setOwner(cid);
                db.setOccupancy(o, false);
            }
        } catch (IOException ex) {
            Application.handleException(
                    "Booking of occupancy failed", ex, null);
        }
    }

    /**
     * Remove a customer holding an occupancy.
     *
     * @param o the occupancy to update
     */
    public void unbook(Occupancy o) {
        try {
            Occupancy dbo = db.getOccupancy(o.getAddress());
            if (dbo.hasOwner() && !dbo.getOwner().equals(o.getOwner())) {
                Application.handleException(
                        "Occupancy booked by another client", null, null);
            } else {
                o.setOwner("");
                db.setOccupancy(o, false);
            }
        } catch (IOException ex) {
            Application.handleException(
                    "Unbooking of occupancy failed", ex, null);
        }
    }
}
