package suncertify.gui;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;
import suncertify.conn.DBConnection;
import suncertify.conn.RemoteDBAccess;
import suncertify.db.Data;
import suncertify.db.URLyBirdDBAccess;
import suncertify.db.URLyBirdData;

/**
 * Class used to send client requests to database object.
 *
 * @author Emmanuel
 */
public class Controller {

    /**
     * the object that accesses our database
     */
    private URLyBirdDBAccess db;

    /**
     * variable to store the connection state of the controller
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
    public Controller(DBConnection.Type connectionType, String location, String port) {
        connected = false;
        String s = "";
        switch (connectionType) {
            case DIRECT:
                try {
                    db = new URLyBirdData(location);
                    connected = true;
                } catch (IOException ex) {
                    Application.handleException(
                            "Unable to access database file", ex);
                }
                break;
            case NETWORK:
                try {
                    RemoteDBAccess remote = DBConnection
                            .getRemote(location, port);
                    db = (URLyBirdDBAccess) remote;
                    connected = true;
                } catch (RemoteException re) {
                    Application.handleException("Unable to connect to server", re);
                } catch (IllegalArgumentException iae) {
                    Application.handleException("Port (" + port + ") error, "
                            + iae.getMessage(), iae);
                }
                break;
            default:
                // should never happen
                Exception e = new IllegalStateException("Unknown connection type");
                Application.handleException("Unable to start connection", e);
        }
        if (connected) {
            if (db == null) {
                log.info("False connection");
            } else {
                try {
                    log.info(db.getOccupancies().toString());
                } catch (IOException ex) {
                    log.log(Level.SEVERE, "Call to get occupancies failed.", ex);
                }
            }
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

}
