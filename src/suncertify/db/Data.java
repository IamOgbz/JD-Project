package suncertify.db;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
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
     * The field names and byte length parsed from the file.
     */
    public static final Map<String, Integer> fields = new LinkedHashMap<>();

    /**
     * The offset for reading the records from the database file. Set once after
     * the database file is loaded and parsed.
     */
    public final Long dataOffset;

    /**
     * The length (in bytes) of each record.
     */
    public static int recordLength;

    /**
     * The number of fields in each record.
     */
    public static int numFields;

    /**
     * The read write lock used to maintain database operations concurrency.
     */
    protected static final ReentrantReadWriteLock dbRWLock
            = new ReentrantReadWriteLock(true);

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
        if (databasePath == null) {
            // if database location not specified
            log.log(Level.WARNING, "No database path specified: {0}",
                    new Object[]{databasePath});
        } else if (dbPath == null) {
            dbPath = databasePath; // if no previous database path exists
        } else if (!dbPath.equals(databasePath)) {
            // if there was a previous database location defined
            log.log(Level.WARNING, "Different database path already specified"
                    + "\nExisting: {0}\nSupplied: {1}",
                    new Object[]{dbPath, databasePath});
        }
        // load database file if path exists
        if (dbPath != null) {
            log.log(Level.INFO, "Attempting to use database path: {0}",
                    new Object[]{dbPath});
            dbFile = new RandomAccessFile(dbPath, "rw");
        }
        // parse the header and set the data offset to the start of the records
        dataOffset = readHeader();
    }

    /**
     * To perform read operations on the database file, without the underlying
     * information being changed during the process.
     *
     * @param offset the location to start reading from in the data file
     * @param length the number of bytes to be read
     * @return an array of bytes containing the read file from the offset of the
     * length specified
     * @throws IOException
     */
    public final byte[] read(long offset, int length) throws IOException {
        byte[] data = new byte[length];
        // blocks other users for the ammount of time it takes to read the data
        dbRWLock.readLock().lock();
        try {
            dbFile.seek(offset);
            dbFile.readFully(data);
        } finally {
            dbRWLock.readLock().unlock();
        }
        return data;
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
     * @return list of <code>String[]</code> where each array is a record
     * @throws IOException
     */
    public List<String[]> readData() throws IOException {
        // get the start position in file of the records
        long offset = dataOffset;
        // get the length of the database file
        long dbFileLength = dbFile.length();
        // create list to hold records
        List<String[]> records = new ArrayList<>();
        // calculate length of record from schema defintion
        recordLength = 0;
        // loop through defined fields
        for (int fieldLength : fields.values()) {
            recordLength += fieldLength;
        }
        // prevent database file from being changed while being read
        dbRWLock.readLock().lock();
        try {
            // loop through the database file and read records at each location
            while (offset < dbFileLength && records.add(readRecord(offset))) {
                offset += recordLength;
            }
        } catch (RecordNotFoundException ex) {
            log.log(Level.INFO,
                    "database file might be corrupt."
                    + "\nRecord Address: {0}\n{1}",
                    new Object[]{offset, ex});
        } finally {
            dbRWLock.readLock().unlock();
        }
        return records;
    }

    @Override
    public String[] readRecord(long recNo) throws RecordNotFoundException {
        // index to used to store field values in array positions
        short idx = 0;
        // number of field in each record
        numFields = fields.size();
        // array of string to represent the record
        String[] record = new String[numFields];
        // prevent database file from being changed while being read
        dbRWLock.readLock().lock();
        try {
            // for each field name get the number of bytes
            for (int fieldLength : fields.values()) {
                // read a number of bytes from the database file starting from the
                // offset to position specified by the field length
                byte[] data = read(recNo, fieldLength);
                record[idx] = new String(data);
                recNo += fieldLength;
                idx++;

            }
        } catch (IOException ex) {
            log.log(Level.SEVERE, null, ex);
            return null;
        } finally {
            dbRWLock.readLock().unlock();
        }
        return record;
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
        dbRWLock.writeLock().lock();
        try {
            dbFile.seek(offset);
            dbFile.write(data);
        } finally {
            dbRWLock.writeLock().unlock();
        }
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
