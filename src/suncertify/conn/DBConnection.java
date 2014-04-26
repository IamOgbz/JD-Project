package suncertify.conn;

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.logging.Level;
import java.util.logging.Logger;
import suncertify.db.Data;

/**
 * Class to handle local and network functionalities.
 *
 * @author Emmanuel
 */
public class DBConnection {

    /**
     * The ways of connecting to the database.
     */
    public enum Type {

        /**
         * For local database connections.
         */
        DIRECT,
        /**
         * For connections through a server.
         */
        NETWORK
    }

    /**
     * The name of the remote binding.
     */
    public static final String REMOTE_NAME = "URLyBird";

    /**
     * Error value when the port is not an integer.
     */
    public static final int ERROR_PORT_VALUE = -1;
    /**
     * Error value when the port is not within valid range.
     */
    public static final int ERROR_PORT_RANGE = -2;

    /**
     * The Logger instance. All log messages from this class are routed through
     * this member. The Logger namespace is <code>suncertify.db</code>.
     */
    private static final Logger log = Logger.getLogger("suncertify.conn");

    /**
     * This class should not be instantiated. Only its utility methods should be
     * used.
     */
    private DBConnection() {
    }

    /**
     * Get a local DBAccess object.
     *
     * @param dbLocation the location of the database
     * @return DBAccess implementation for local use.
     * @throws IOException
     */
    public static Data getLocal(String dbLocation) throws IOException {
        return new Data(dbLocation);
    }

    /**
     * Get the remote access object from the binding on the host name & port.
     *
     * @param hostname the server where the remote object is hosted
     * @param portNumber the port on the server used for the hosting
     * @return RemoteDBAccess for accessing the database remotely
     * @throws RemoteException
     */
    public static RemoteDBAccess getRemote(String hostname, String portNumber)
            throws RemoteException, IllegalArgumentException {
        int port = validatePort(portNumber);
        switch (port) {
            case ERROR_PORT_VALUE:
                throw new IllegalArgumentException(
                        " port number not valid");
            case ERROR_PORT_RANGE:
                throw new IllegalArgumentException(
                        " port number out of range (0..65535)");
            default:
                String url = "rmi://" + hostname + ":" + port + "/" + REMOTE_NAME;
                try {
                    return (RemoteDBAccess) Naming.lookup(url);
                } catch (NotBoundException nbe) {
                    log.log(Level.SEVERE, "{0} not registered: {1}",
                            new Object[]{nbe, REMOTE_NAME});
                    throw new RemoteException(REMOTE_NAME + " not registered: ", nbe);
                } catch (MalformedURLException mue) {
                    log.log(Level.SEVERE, "{0} not valid: {1}",
                            new Object[]{hostname, mue.getMessage()});
                    throw new RemoteException(
                            "Cannot connect to \"" + hostname + "\"", mue);
                }
        }
    }

    /**
     * Register a remote access object on a port.
     *
     * @param dbLocation the database file location
     * @param portNumber the port number to bind the object to
     * @throws RemoteException
     * @throws IllegalArgumentException
     */
    public static void register(String dbLocation, String portNumber)
            throws RemoteException, IllegalArgumentException {
        int port = validatePort(portNumber);
        switch (port) {
            case ERROR_PORT_VALUE:
                throw new IllegalArgumentException(
                        " port number not valid");
            case ERROR_PORT_RANGE:
                throw new IllegalArgumentException(
                        " port number out of range (0..65535)");
            default:
                Registry r = LocateRegistry.createRegistry(port);
                RemoteDBAccess rob = new RemoteData(dbLocation);
                r.rebind(REMOTE_NAME, rob);
        }
    }

    /**
     * Check if string is a valid port.
     * 
     * @param port the port to be validated
     * @return an integer value depicting an error message or the port number
     */
    public static int validatePort(String port) {
        try {
            int number = Integer.parseInt(port);
            if (number < 0 || number > Integer.parseInt("FFFF", 16)) {
                return ERROR_PORT_RANGE;
            } else {
                return number;
            }
        } catch (NumberFormatException nfe) {
            return ERROR_PORT_VALUE;
        }
    }

}
