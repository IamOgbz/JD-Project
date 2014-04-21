package suncertify.db;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class used to access the database file and perform read and write operations.
 * Unit of measurement for the size/length is bytes
 *
 * @author Emmanuel
 */
public class Data implements DBAccess {

    /**
     * The table names and byte length parsed from the file.
     */
    public static final Map<String, Short> fields = new LinkedHashMap<>();

    /**
     * The records read from the database using the record file address as a key
     */
    public final Map<Long, String[]> records;

    /**
     * The offset for reading the records from the database file. Set once after
     * the database file is loaded and parsed.
     */
    public final Long dataOffset;

    /**
     * The physical file on disk containing our data.
     */
    protected static RandomAccessFile dbFile = null;

    /**
     * The Logger instance. All log messages from this class are routed through
     * this member. The Logger namespace is <code>suncertify.db</code>.
     */
    private static final Logger log = Logger.getLogger("suncertify.db");

    /**
     * The location where the database file is stored.
     */
    private static String dbPath = null;

    /**
     * Default constructor that accepts the database path as a parameter.
     *
     * All instances of this class share the same data file.
     *
     * @param databasePath the path to the database file.
     * @throws FileNotFoundException if the database file cannot be found.
     * @throws IOException if the database file cannot be read.
     */
    public Data(String databasePath) throws FileNotFoundException, IOException {
        log.entering("Data", "database path", databasePath);
        if (databasePath == null) {
            log.log(Level.WARNING, "No database path specified: {0}",
                    new Object[]{databasePath});
        } else if (dbPath == null) {
            dbPath = databasePath;
        } else if (!dbPath.equals(databasePath)) {
            log.log(Level.WARNING, "Different database path already specified"
                    + "\nExisting: {0}\nSupplied: {1}",
                    new Object[]{dbPath, databasePath});
        }
        if (dbPath != null) {
            log.log(Level.INFO, "Attempting to use database path: {0}",
                    new Object[]{dbPath});
            dbFile = new RandomAccessFile(dbPath, "rw");
        }
        dataOffset = readHeader();
        records = new LinkedHashMap<>();
        log.exiting("Data", "data offset", dataOffset);
    }

    /**
     * Override this method to perform custom read operations on file load. The
     * readHeader method should by default start from position 0 in the file.
     *
     * @return the position to start reading the data
     * @throws java.io.IOException
     */
    public long readHeader() throws IOException {
        return 0L;
    }

    /**
     * Uses the schema loaded to populate the records. Override this method to
     * perform custom read data operations.
     *
     * @throws IOException
     */
    public void readData() throws IOException {
        synchronized (records) {
            synchronized (dbFile) {
                
            }
            // delete any extra entries
            
        }
    }

    /**
     * To perform read operations on the database file, without the underlying
     * information being changed during the process.
     *
     * @param offset the location to start reading from in the data file
     * @param length the number of bytes to be read
     * @return an array of bytes containing the read file from the offset of the
     * length specified
     * @throws java.io.IOException
     */
    public final byte[] read(long offset, int length) throws IOException {
        byte[] data = new byte[length];
        // blocks other users for the ammount of time it takes to read the data
        synchronized (dbFile) {
            dbFile.seek(offset);
            dbFile.readFully(data);
        }
        return data;
    }

    /**
     * To perform write operations on the database file, without the underlying
     * information being changed during the process.
     *
     * @param offset the location to start writing the file to
     * @param data the bytes to be written
     * @throws IOException
     */
    public final void write(long offset, byte[] data) throws IOException {
        // blocks other users for the ammount of time it takes to write the data
        synchronized (dbFile) {
            dbFile.seek(offset);
            dbFile.write(data);
        }
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
