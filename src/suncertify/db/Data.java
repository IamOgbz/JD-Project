package suncertify.db;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
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
    public final Map<String, Integer> fields = new LinkedHashMap<>();

    /**
     * The offset for reading the records from the database file. Set once after
     * the database file is loaded and parsed.
     */
    protected final long dataOffset;

    /**
     * The number of fields in each record.
     */
    protected static int numFields = 0;

    /**
     * The length (in bytes) of each record.
     */
    protected static int recordLength = 0;

    /**
     * The character encoding used in the URLyBird database file. Default is
     * "UTF-8".
     */
    protected static Charset encoding = StandardCharsets.UTF_8;

    /**
     * The physical file on disk containing our data.
     */
    protected static RandomAccessFile dbFile = null;

    /**
     * The read write lock used to maintain database operations concurrency.
     */
    protected static final ReentrantReadWriteLock dbRWLock
            = new ReentrantReadWriteLock(true);

    /**
     * The Logger instance. All log messages from this class are routed through
     * this member. The Logger namespace is <code>suncertify.db</code>.
     */
    private static final Logger log = Logger.getLogger("suncertify.db");

    /**
     * The object used to construct out record to be written to database file.
     */
    private static StringBuilder recordBuilder = null;

    /**
     * The length of the database file. Update after each call to
     * <code>readData()</code>
     */
    private static long dbFileLength = 0L;

    /**
     * The location where the database file is stored.
     */
    private static String dbPath = null;

    /**
     * A buffer for the database in between subsequent read operations.
     */
    private final Map<Long, String[]> dataBuffer;

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
        // instantiate the data buffer
        dataBuffer = new LinkedHashMap<>();
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
     * Uses the schema loaded to populate the records and skips deleted records.
     * Override this method to perform custom read data operations.
     *
     * @return list of <code>String[]</code> where each array is a record
     * @throws IOException
     */
    public Collection<String[]> readData() throws IOException {
        // to prevent data buffer from being read in the middle of changes
        dbRWLock.writeLock().lock();
        try {
            // retrieve all the data in the database
            findByCriteria(new String[]{""});
            // return just the records from the data buffer
            return dataBuffer.values();
        } finally {
            dbRWLock.writeLock().unlock();
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
     * Convert byte array data into a record represented by string array of
     * field values, using the schema definition stored in <code>fields</code>.
     *
     * @param data the byte array to be parsed
     * @return the record represented by an string array, where every item is a
     * field value
     * @throws IndexOutOfBoundsException if the byte array length does not match
     * the record length
     */
    public final String[] parseRecord(byte[] data)
            throws IndexOutOfBoundsException {
        if (data.length == recordLength) {
            // index used to store field values in array positions
            short idx = 0;
            // index used to place fields in string builder
            int fieldOffset = 0;
            // array of string to represent the record
            String[] record = new String[numFields];
            // for each field name get the number of bytes
            for (int fieldLength : fields.values()) {
                record[idx] = new String(data, fieldOffset, fieldLength, encoding);
                fieldOffset += fieldLength;
                idx++;
            }
            return record;
        } else {
            // byte array length does not match record length
            throw new IndexOutOfBoundsException();
        }
    }

    @Override
    public String[] readRecord(long recNo) throws RecordNotFoundException {
        // array of string to represent the record
        String[] record;
        // -- TODO -- implement record level locking
        // prevent database file from being changed while being read
        dbRWLock.readLock().lock();
        try {
            // read all record bytes from the databse file
            byte[] data = read(recNo, recordLength);
            record = parseRecord(data);
        } catch (IOException | IndexOutOfBoundsException ex) {
            log.log(Level.SEVERE, "\nRecord could not be read"
                    + "\nRecord Number: {0}\nRecord Length: {1}"
                    + "\nFile Length: {2}",
                    new Object[]{recNo, recordLength, getDBFileLength()});
            throw new RecordNotFoundException();
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

    /**
     * Converts a record representation into byte array data ready for writing.
     * Uses the schema definition in fields to position the field values.
     * <!--Truncates field value if it is longer than the definition specified
     * in the schema fields. <br> e.g. if field length is 4, "Happy" will be
     * shortened to "Happ" when record is prepared-->
     *
     * @param record the array of strings that serve as a record representation
     * @return the byte array representation of the record with the field values
     * at their correct offsets
     * @throws IndexOutOfBoundsException when the built record is longer than
     * the record length or if a field value is longer than the definition
     * specified in the schema fields.
     */
    public final byte[] prepareRecord(String[] record)
            throws IndexOutOfBoundsException {
        // index used to iterate over field values in record
        short idx = 0;
        // index used to place fields in string builder
        int fieldOffset = 0;
        dbRWLock.writeLock().lock();
        try {
            // create the recordbuilder object to be used
            recordBuilder = new StringBuilder(new String(new byte[recordLength]));
            // for each field name get the number of bytes
            for (int fieldLength : fields.values()) {
                // replace the empty string at the specified field offset with the 
                // corresponding field value
                recordBuilder.replace(fieldOffset, fieldOffset + fieldLength,
                        record[idx]);
                // increment to the next field offset
                fieldOffset += fieldLength;
                // increment to the next field value
                idx++;
            }
            // if the built record exceeds the specified record length
            if (recordBuilder.length() > recordLength) {
                throw new IndexOutOfBoundsException("Built record longer than record length ");
            } else {
                // return the build record byte array data
                return recordBuilder.toString().getBytes();
            }
        } finally {
            dbRWLock.writeLock().unlock();
        }
    }

    /**
     * Writes a record to the database file at any location.
     *
     * @param recNo the location in the file to write the record
     * @param data array of strings representing a record, where each item is a
     * field value
     * @throws IOException when the data to be written is too long for the fixed
     * record length
     */
    public final void writeRecord(long recNo, String[] data)
            throws IOException {
        // -- TODO -- implement record level locking
        // prevent database file from being read/edited while being writen
        dbRWLock.writeLock().lock();
        try {
            write(recNo, prepareRecord(data));
        } catch (IndexOutOfBoundsException ex) {
            log.log(Level.SEVERE, "\nData exceeds record length"
                    + "\nRecord Length: {0}"
                    + "\nFields: {1}"
                    + "\nData: {2}",
                    new Object[]{recordLength, fields, data});
            throw new IOException();
        } finally {
            dbRWLock.writeLock().unlock();
        }
    }

    @Override
    public void updateRecord(long recNo, String[] data, long lockCookie)
            throws RecordNotFoundException, SecurityException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void deleteRecord(long recNo, long lockCookie)
            throws RecordNotFoundException, SecurityException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Perform matching between the criteria fields and the record fields.
     * Fields are considered to match if the record field [n] starts with or
     * equals to the criteria value [n].<br><br><b>Empty strings match anything
     * and NULL values match nothing</b>
     *
     * @param criteria the field values to check the record against
     * @param record the record to check for matching
     * @return the number of matches made between the criteria and the record
     */
    public static int matchRecord(String[] criteria, String[] record) {
        int matches = 0;
        // the length of the smaller array to use for loop
        int len = Math.min(criteria.length, record.length);
        for (int i = 0; i < len; i++) {
            if (criteria[i] != null && record[i].startsWith(criteria[i])) {
                matches++;
            }
        }
        return matches;
    }

    @Override
    public long[] findByCriteria(String[] criteria) {
        // replace null values with empty string and vice versa
        // this is for use in matchRecord and to fulfil the method definition
        for (int i = 0; i < criteria.length; i++) {
            if (criteria[i] == null) {
                criteria[i] = "";
            } else if (criteria[i].isEmpty()) {
                criteria[i] = null;
            }
        }
        // prevent data from being read while being changed
        dbRWLock.writeLock().lock();
        try {
            // clear the dataBuffer
            dataBuffer.clear();
            // refresh the length of the database file
            getDBFileLength();
            // recalculate length of record from schema defintion
            recordLength = 0;
            // refresh number of field in each record
            numFields = fields.size();
            // loop through defined fields
            for (int fieldLength : fields.values()) {
                recordLength += fieldLength;
            }
            // get the start position in file of the records
            long offset = dataOffset;
            // create record variable for the loop
            String[] record;
            // loop through the database file and read records at each location
            while (offset < dbFileLength) {
                try {
                    // read bytes and get the record at offset 
                    record = readRecord(offset);
                    // if there is at least one matc
                    if (matchRecord(criteria, record) > 0) {
                        // add the record to the data buffer
                        dataBuffer.put(offset, record);
                    }
                } catch (RecordNotFoundException ex) {
                    log.log(Level.INFO, "Record not found.\nRecord Address: {0}"
                            + "\n{1}", new Object[]{offset, ex});
                } finally {
                    // update the offset to the next record location
                    offset += recordLength;
                }
            }

            // retrieve the record numbers from the dataBuffer
            Object[] recNos = dataBuffer.keySet().toArray();
            // create the result array
            long[] result = new long[recNos.length];
            // loop through and cast the record numbers into the primitive long
            for (int i = 0; i < recNos.length; i++) {
                result[i] = (Long) recNos[i];
            }
            // return the primitive array of the result
            return result;
        } finally {
            dbRWLock.writeLock().unlock();
        }
    }

    /**
     * Takes search parameters and returns records that match. The parameters
     * given should coincide with the arrangement of the fields in each record.
     *
     * @param params the search parameters
     * @return the resulting records from the search
     */
    public Collection<String[]> search(String... params) {
        // to prevent data buffer from being read in the middle of changes
        dbRWLock.writeLock().lock();
        try {
            // get the records that match from the data file
            findByCriteria(params);
            // return the matching values stored in the data buffer
            return dataBuffer.values();
        } finally {
            dbRWLock.writeLock().unlock();
        }
    }

    /**
     * Checks if two records have the same values and their ordering<br>
     * Override for a custom comparison of records.
     *
     * @param record1 the first record
     * @param record2 the second record
     * @return an integer value that is negative, zero or positive, depending on
     * if the first record is greater than, equal to or less than, the second
     * record
     */
    public static int compareRecords(String[] record1, String[] record2) {
        // the result of the comparison
        int result = 0;
        // if the num fields are not equal the greater record has more fields
        if (record1.length != record2.length) {
            return record1.length - record2.length;
        } else {
            // the first unequal field is used to determine the greater record
            for (int i = 0; i < record1.length; i++) {
                // use the string compare method to determine the greater field
                result = record1[i].compareTo(record2[i]);
                if (result != 0) {
                    return result;
                }
                // continue checking until all fields are compared
            }
        }
        // if the record fields were always equal
        return result;
    }

    @Override
    public long createRecord(String[] data) throws DuplicateKeyException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public long lockRecord(long recNo) throws RecordNotFoundException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void unlock(long recNo, long cookie) throws SecurityException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * @return the numFields
     */
    public int getNumFields() {
        return numFields;
    }

    /**
     * @return the recordLength
     */
    public int getRecordLength() {
        return recordLength;
    }

    /**
     * @return The location of the database file
     */
    public final String getDBFilePath() {
        return dbPath;
    }

    /**
     * @return The length of the database file or 0 if the database file cannot
     * be accessed
     */
    public final long getDBFileLength() {
        dbFileLength = 0L;
        try {
            dbFileLength = dbFile.length();
        } catch (IOException ex) {
            Logger.getLogger(Data.class.getName()).log(Level.SEVERE, null, ex);
        }
        return dbFileLength;
    }

}
