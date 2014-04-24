package suncertify.db;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
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
     * The length (in bytes) of the cookie that describes the data file.
     */
    public static final short MAGIC_COOKIE_LENGTH = 4;

    /**
     * The length (in bytes) of the data that contains record length (in bytes).
     */
    public static final short RECORD_LENGTH_BYTES = 4;

    /**
     * The length (in bytes) of the data that contains the number of fields.
     */
    public static final short NUM_FIELDS_LENGTH = 2;

    /**
     * The length (in bytes) of the data the contains each field name.
     */
    public static final short FIELD_NAME_LENGTH = 2;

    /**
     * The length (in bytes) of the data that contains each field length.
     */
    public static final short FIELD_LENGTH_BYTES = 2;

    /**
     * The read write lock used to maintain database operations concurrency.
     */
    private static final ReentrantReadWriteLock dbRWLock
            = new ReentrantReadWriteLock(true);

    /**
     * The map used to lock and unlock records
     */
    private static final Map<Long, Long> lockCookies = new LinkedHashMap<>();

    /**
     * The Logger instance. All log messages from this class are routed through
     * this member. The Logger namespace is <code>suncertify.db</code>.
     */
    private static final Logger log = Logger.getLogger("suncertify.db");

    /**
     * The location where the database file is stored.
     */
    protected static String dbPath = null;

    /**
     * The physical file on disk containing our data.
     */
    private static RandomAccessFile dbFile = null;

    /**
     * The field names and byte length parsed from the file.
     */
    private final Map<String, Integer> fields;

    /**
     * A buffer for the database in between subsequent read operations.
     */
    private final Map<Long, String[]> dataBuffer;

    /**
     * Data file cookie identifier.
     */
    private int magicCookie;

    /**
     * The offset for reading the records from the database file. Set once after
     * the database file is loaded and parsed.
     */
    private long dataOffset;

    /**
     * The number of fields in each record.
     */
    private int numFields;

    /**
     * The length (in bytes) of each record.
     */
    private int recordLength;

    /**
     * The character encoding used in the URLyBird database file. Default is
     * "UTF-8".
     */
    protected Charset encoding;

    /**
     * The length of the database file. Update after each call to
     * <code>readData()</code>
     */
    private long dbFileLength;

    /**
     * The object used to construct out record to be written to database file.
     */
    private StringBuilder recordBuilder;

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
        // instantiate the fields map
        fields = new LinkedHashMap<>();
        // instantiate the data buffer
        dataBuffer = new LinkedHashMap<>();
        // parse the header and set the data offset to the start of the records
        readHeader();
    }

    /**
     * Performs read operations on database file, on instantiation
     *
     * @return the position to start reading the data
     * @throws java.io.IOException
     */
    private void readHeader() throws IOException {
        // the offset storing the read operation cursor
        long offset;
        // set charater encoding used for the data
        encoding = StandardCharsets.US_ASCII;
        // add deleted flag the fields
        //fields.put("deleted", 1);
        // the length of the deleted field
        recordLength = 1;
        // prevents code that changes database while block executes
        dbRWLock.readLock().lock();
        try {
            // starting location of the header
            offset = 0;
            // go to the offset location in the database file
            dbFile.seek(offset);
            // reads 4 bytes from the file in the form of int
            magicCookie = dbFile.readInt();
            // go to the next starting position and repeat
            offset += MAGIC_COOKIE_LENGTH;
            // go to dbFile offset location for record length
            dbFile.seek(offset);
            // add the record length read from the file to the deleted field
            recordLength += dbFile.readInt();
            // go to the next starting position and repeat
            offset += RECORD_LENGTH_BYTES;
            // go to dbFile offset location for number of fields in record
            dbFile.seek(offset);
            // read 2 bytes of data and return as a short
            numFields = dbFile.readShort();
            // go to the next starting position and repeat
            offset += NUM_FIELDS_LENGTH;
            // read schema description
            // create variables to be used inside, outside the loop
            String fieldName;
            int fieldLength;
            short fieldNameLength;
            // loop for number of fields in a record
            for (int i = 0; i < numFields; i++) {
                // go to beginning of the column definition
                dbFile.seek(offset);
                // get the number of bytes used to store the column name
                fieldNameLength = dbFile.readShort();
                // increment offset to starting point of column name
                offset += FIELD_NAME_LENGTH;
                // read the column name data from the file in form of byte array
                byte[] fieldNameData = read(offset, fieldNameLength);
                // convert the byte array into String using the encoding
                fieldName = new String(fieldNameData, encoding);
                // increment offset to starting point of column length data
                offset += fieldNameLength;
                // go to the offset location for column length data
                dbFile.seek(offset);
                // read column length
                fieldLength = dbFile.readShort();
                // add the column definition to fields
                fields.put(fieldName, fieldLength);
                // increment offset to starting point of next column definition 
                // or row data if the loop is over
                offset += FIELD_LENGTH_BYTES;
            }
            dataOffset = offset;
        } finally {
            dbRWLock.readLock().unlock();
        }
    }

    /**
     * Uses the schema loaded to populate the records and skips deleted records.
     * Override this method to perform custom read data operations.
     *
     * @return list of <code>String[]</code> where each array is a record
     * @throws IOException
     */
    public Collection<String[]> readData() throws IOException {
        // retrieve all the data in the database
        search("");
        // return just the records from the data buffer
        return dataBuffer.values();
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
    private byte[] read(long offset, int length) throws IOException {
        byte[] data = new byte[length];
        // prevents code that changes database while block executes
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
     * Checks if a record ha been deleted by reading the deleted flag byte.
     *
     * @param recNo the offset location in file of the record
     * @return true is the byte at the deleted flag is 1
     * @throws IOException
     */
    private boolean isDeleted(long recNo) throws IOException {
        return read(recNo, 1)[0] == 1;
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
    private String[] parseRecord(byte[] data)
            throws IndexOutOfBoundsException {
        if (data.length == recordLength) {
            // index used to store field values in array positions
            short idx = 0;
            // index used to get fields from data bytes
            // ignoring the deleted flag byte
            int fieldOffset = 1;
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
            log.info("Data to parse not equal to record length");
            // byte array length does not match record length
            throw new IndexOutOfBoundsException();
        }
    }

    @Override
    public String[] readRecord(long recNo) throws RecordNotFoundException {
        // array of string to represent the record
        String[] record;
        // prevents code that changes database while block executes
        dbRWLock.readLock().lock();
        try {
            if (isDeleted(recNo)) { // check the deleted flag byte
                throw new RecordNotFoundException("Record deleted");
            } else if (isLocked(recNo)) { // check if record is locked
                throw new RecordNotFoundException("Record locked");
            } else {
                // read all record bytes from the database file
                byte[] data = read(recNo, recordLength);
                // parse bytes read into record
                record = parseRecord(data);
            }
        } catch (IOException | IndexOutOfBoundsException ex) {
            log.log(Level.SEVERE, "Record could not be read"
                    + "\nRecord Number: {0}\nRecord Length: {1}"
                    + "\nFile Length: {2}\n{3}",
                    new Object[]{recNo, recordLength, getDBFileLength(), ex});
            throw new RecordNotFoundException();
        } finally {
            dbRWLock.readLock().unlock();
        }
        return record;
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
        // replace null values with empty string and vice versa, since
        // findByCritetia definition is the reverse of matchRecord deinition
        for (int i = 0; i < criteria.length; i++) {
            if (criteria[i] == null) {
                criteria[i] = "";
            } else if (criteria[i].isEmpty()) {
                criteria[i] = null;
            }
        }
        // prevents code that changes database while searching
        dbRWLock.readLock().lock();
        try {
            Object[] recNos;
            // prevent the dataBuffer from being used while block executes
            synchronized (dataBuffer) {
                // clear the dataBuffer
                dataBuffer.clear();
                // refresh the length of the database file
                getDBFileLength();
                // create record variable for the loop
                String[] record;
                // loop through database file and read records at each location
                for (long offset = dataOffset; offset < dbFileLength;
                        offset += recordLength) {
                    try {
                        // if record not deleted
                        if (!isDeleted(offset)) {
                            // read bytes and get the record at offset 
                            record = readRecord(offset);
                            // if there is at least one matc
                            if (matchRecord(criteria, record) > 0) {
                                // add the record to the data buffer
                                dataBuffer.put(offset, record);
                            }
                        }
                    } catch (RecordNotFoundException ex) {
                        log.log(Level.INFO, "Record not found."
                                + "\nRecord Address: {0}\n{1}",
                                new Object[]{offset, ex});
                    } catch (IOException ex) {
                        log.log(Level.SEVERE, "Could not read database file\n",
                                ex);
                    }
                }
                // retrieve the record numbers from the dataBuffer
                recNos = dataBuffer.keySet().toArray();
            }
            // create the result array
            long[] result = new long[recNos.length];
            // loop through and cast record numbers into the primitive long
            for (int i = 0; i < recNos.length; i++) {
                result[i] = (Long) recNos[i];
            }
            // return the primitive array of the result
            return result;
        } finally {
            dbRWLock.readLock().unlock();
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
        synchronized (dataBuffer) {
            // get the records that match from the data file
            findByCriteria(params);
            // return the matching values stored in the data buffer
            return dataBuffer.values();
        }
    }

    /**
     * To perform write operations on the database file, without the underlying
     * information being changed during the process.
     *
     * @param offset the location to start writing the file to
     * @param data the bytes to be written
     * @throws IOException
     */
    private void write(long offset, byte[] data) throws IOException {
        // prevents code that reads/changes database while block executes
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
    private byte[] prepareRecord(String[] record)
            throws IndexOutOfBoundsException {
        // index used to iterate over field values in record
        short idx = 0;
        // index used to place fields in string builder
        int fieldOffset = 0;
        dbRWLock.writeLock().lock();
        try {
            // field value object to be used
            String fieldValue;
            // create the recordbuilder object to be used less deleted flag byte
            recordBuilder = new StringBuilder(new String(new byte[recordLength - 1]));
            // for each field name get the number of bytes
            for (int fieldLength : fields.values()) {
                fieldValue = record[idx];
                // ensure the field value in record at least the correct length
                while (fieldValue.length() < fieldLength) {
                    // pad end with blank string
                    fieldValue += " ";
                }
                // replace the empty string at the specified field offset with the 
                // corresponding field value
                recordBuilder.replace(fieldOffset, fieldOffset + fieldLength,
                        fieldValue);
                // increment to the next field offset
                fieldOffset += fieldLength;
                // increment to the next field value
                idx++;
            }
            // if the built record exceeds the specified record length
            if (recordBuilder.length() >= recordLength) {
                throw new IndexOutOfBoundsException("Record built is too long");
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
    private void writeRecord(long recNo, String[] data)
            throws IOException {
        // -- TODO -- implement record level locking
        // prevent database file from being read/edited while being writen
        dbRWLock.writeLock().lock();
        try {
            // write into database file, skipping the deleted flag byte
            write(recNo + 1, prepareRecord(data));
        } catch (IndexOutOfBoundsException ex) {
            log.log(Level.SEVERE, "Data exceeds record length"
                    + "\nRecord Length: {0}\nFields: {1}\nData: {2}\n{3}",
                    new Object[]{recordLength, fields, toArrayString(data), ex});
            throw new IOException();
        } finally {
            dbRWLock.writeLock().unlock();
        }
    }

    /**
     * Checks if two records have the same values and their order. <br> Used to
     * check for duplicate records. Override for a custom comparison of records.
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
        // instantiate the final offset to a non usable value
        long finalOffset = -1;
        // prevents code that changes database while block executes
        dbRWLock.readLock().lock();
        try {
            // instantiate variable at start of records in database file
            long offset = dataOffset;
            // loop through the database file and check for a free space at each location
            while (offset < dbFileLength) {
                // read record in position
                String[] record = readRecord(offset);
                // check if it is a duplicate record against all records
                if (compareRecords(record, data) == 0) {
                    throw new DuplicateKeyException("Duplicate record found");
                } else {
                    // check if the record position is first available
                    if (isDeleted(offset) && finalOffset < 0) {
                        finalOffset = offset;
                    }
                }
                // go to next record position
                offset += recordLength;
            }
            if (finalOffset < 0) {
                finalOffset = offset;
            }
        } catch (RecordNotFoundException ex) {
            log.log(Level.WARNING, "Offset: {0}\n{1}", new Object[]{finalOffset, ex});
        } catch (IOException ex) {
            log.log(Level.SEVERE, "Could not read database file\n", ex);
        } finally {
            dbRWLock.readLock().unlock();
        }

        // prevent code that reads/changes database while block executes
        dbRWLock.writeLock().lock();
        try {
            // set deleted byte to false "0"
            write(finalOffset, "0".getBytes());
            // write record at free location
            writeRecord(finalOffset, data);
        } catch (IOException ex) {
            log.log(Level.SEVERE, "Could not read database file", ex);
        } finally {
            dbRWLock.writeLock().unlock();
        }
        // return location where record was written
        return finalOffset;
    }

    @Override
    public void updateRecord(long recNo, String[] data, long lockCookie)
            throws RecordNotFoundException, SecurityException {
        // record level locking
        if (lockCookies.get(recNo) == lockCookie) {
            // prevents code that reads/changes database while block executes
            dbRWLock.writeLock().lock();
            try {
                // if record has been deleted throw
                if (isDeleted(recNo)) {
                    throw new RecordNotFoundException("Record deleted");
                } else {
                    // overwrite the record ignoring the deleted flag byte
                    writeRecord(recNo, data);
                }

            } catch (IOException ex) {
                log.log(Level.SEVERE, "Record update failed.", ex);
            } finally {
                dbRWLock.writeLock().unlock();
            }
        } else {
            throw new SecurityException("Invalid lock cookie");
        }
    }

    @Override
    public void deleteRecord(long recNo, long lockCookie)
            throws RecordNotFoundException, SecurityException {
        // record level locking
        if (lockCookies.get(recNo) == lockCookie) {
            // prevents code that reads/changes database while block executes
            dbRWLock.writeLock().lock();
            try {
                // if record has already been deleted throw
                if (isDeleted(recNo)) {
                    throw new RecordNotFoundException("Record already deleted");
                } else {
                    // overwrite the deleted flag byte ignoring the data
                    write(recNo, new byte[]{1});
                }
            } catch (IOException ex) {
                log.log(Level.SEVERE, "Record update failed.", ex);
                throw new RecordNotFoundException();
            } finally {
                dbRWLock.writeLock().unlock();
            }
        } else {
            throw new SecurityException("Invalid lock cookie");
        }
    }

    public final boolean isLocked(long recNo) {
        return lockCookies.containsKey(recNo);
    }

    @Override
    public long lockRecord(long recNo) throws RecordNotFoundException {
        // for managing concurrent lock requests
        synchronized (lockCookies) {
            // stay in the loop while the record is locked
            while (isLocked(recNo)) {
                try {
                    log.info("Waiting for record lock to be released");
                    // wait for the next time a record is unlocked
                    // then check again
                    lockCookies.wait();
                } catch (InterruptedException ex) {
                    log.log(Level.SEVERE, "Waiting interrupted", ex);
                }
            }
            // this is to prevent any record from being locked 
            // while read/write operations are being performed
            log.info("Trying to acquire write lock");
            dbRWLock.writeLock().lock();
            try {
                // after record has been unlocked
                log.info("Preparing to lock");
                // use system nano time as seed to generate unique cookie
                byte[] seed = String.valueOf(System.nanoTime()).getBytes();
                // generate a secure lock cookie
                long cookie = new SecureRandom(seed).nextLong();
                // lock the record with the cookie
                lockCookies.put(recNo, cookie);
                log.log(Level.INFO, "Locked\nRecord: {0}\nCookie: {1}",
                        new Object[]{recNo, cookie});
                return cookie;
            } finally {
                dbRWLock.writeLock().unlock();
            }
        }
    }

    @Override
    public void unlock(long recNo, long cookie) throws SecurityException {
        synchronized (lockCookies) {
            if (lockCookies.get(recNo) == cookie) {
                lockCookies.remove(recNo);
                lockCookies.notifyAll();
            } else {
                throw new SecurityException("Invalid lock cookie");
            }
        }
    }

    /**
     * @return the magicCookie
     */
    public int getMagicCookie() {
        return magicCookie;
    }

    /**
     * @return the encoding
     */
    public Charset getEncoding() {
        return encoding;
    }

    /**
     * @return a string representation of the fields
     */
    public String getFields() {
        return fields.toString();
    }

    /**
     * @return the numFields
     */
    public int getNumFields() {
        return numFields;
    }

    /**
     * @return the dataOffset
     */
    public long getDataOffset() {
        return dataOffset;
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
            log.log(Level.SEVERE, "Could not read database file", ex);
        }
        return dbFileLength;
    }

    /**
     * Take an array and returns a string formatted for printing.
     *
     * @param array the array to convert
     * @return a string representation of the array
     */
    public static final String toArrayString(Object[] array) {
        String as = "";
        for (Object o : array) {
            as += "[" + o.toString() + "](" + o.toString().length() + ") ";
        }
        return as;
    }

}
