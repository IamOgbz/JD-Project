package suncertify.conn;

import java.io.IOException;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.Logger;
import suncertify.db.DBAccess;
import suncertify.db.Data;
import suncertify.db.DuplicateKeyException;
import suncertify.db.RecordNotFoundException;

/**
 * Used to interface between the client and the data layer
 *
 * @author Emmanuel
 */
public class RemoteData extends UnicastRemoteObject implements RemoteDBAccess {

    /**
     * the object that accesses out database
     */
    private final DBAccess db;

    /**
     * The Logger instance. All log messages from this class are routed through
     * this member. The Logger namespace is <code>suncertify.db</code>.
     */
    private static final Logger log = Logger.getLogger("suncertify.conn");

    /**
     * Instantiates a remote object to be used for invoking the server remotely.
     *
     * @param databasePath the database location.
     * @throws IOException
     */
    public RemoteData(String databasePath) throws IOException {
            this.db = new Data(databasePath);
    }

    @Override
    public String[] readRecord(long recNo) throws RecordNotFoundException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void updateRecord(long recNo, String[] data, long lockCookie) throws RecordNotFoundException, SecurityException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void deleteRecord(long recNo, long lockCookie) throws RecordNotFoundException, SecurityException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public long[] findByCriteria(String[] criteria) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public long createRecord(String[] data) throws DuplicateKeyException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public long lockRecord(long recNo) throws RecordNotFoundException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void unlock(long recNo, long cookie) throws SecurityException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
