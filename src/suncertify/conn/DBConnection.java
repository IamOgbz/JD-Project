package suncertify.conn;

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteObject;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class to handle local and network functionalities.
 *
 * @author Emmanuel
 */
public class DBConnection {

    /**
     * The name of the remote binding.
     */
    public static final String REMOTE_NAME = "URLyBird";

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
    public static URLyBirdRemote getLocal(String dbLocation) throws IOException {
        return new URLyBird(dbLocation);
    }

    /**
     * Get the remote access object from the binding on the host name & port.
     *
     * @param hostname the server where the remote object is hosted
     * @param port the port on the server used for the hosting
     * @return RemoteDBAccess for accessing the database remotely
     * @throws RemoteException
     */
    public static URLyBirdRemote getRemote(String hostname, String port)
            throws RemoteException {
        String url = "rmi://" + hostname + ":" + port + "/" + REMOTE_NAME;
        try {
            return (URLyBirdRemote) Naming.lookup(url);
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

    /**
     * Register a remote access object on a port.
     *
     * @param dbLocation the database file location
     * @param port the port number to bind the object to
     * @throws RemoteException
     * @throws IllegalArgumentException
     */
    public static void register(String dbLocation, int port)
            throws RemoteException, IllegalArgumentException {
        if (port >= 0 && port <= Integer.parseInt("FFFF", 16)) {
            Registry r = LocateRegistry.createRegistry(port);
            URLyBirdRemote rob = new URLyBird(dbLocation);
            r.rebind(REMOTE_NAME, rob);
        } else {
            throw new IllegalArgumentException(
                    " port number out of range (0..65535)");
        }
    }

}
