package suncertify.db;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
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
    private static String dbPath = null;

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
     * The number of bytes to be skipped when reading and writing the fields for
     * each record. Used to skip the deleted flag byte.
     */
    private int recordOffset = 0;

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
    private Charset encoding;

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

        fields = new LinkedHashMap<>();
        dataBuffer = new LinkedHashMap<>();
        parseHeader();
    }

    /**
     * Performs read operations on database file, on instantiation. Parses the
     * header and sets the data offset to the start of the records.
     *
     * @return the position to start reading the data
     * @throws java.io.IOException
     */
    private void parseHeader() throws IOException {
        // starting location of the header
        long offset = 0;
        encoding = StandardCharsets.US_ASCII;
        // the deleted flag
        recordOffset = 1;
        //fields.put("deleted", recordOffset);
        recordLength = recordOffset;

        dbRWLock.readLock().lock();
        try {
            dbFile.seek(offset);
            magicCookie = dbFile.readInt();
            offset += MAGIC_COOKIE_LENGTH;

            dbFile.seek(offset);
            recordLength += dbFile.readInt();
            offset += RECORD_LENGTH_BYTES;

            dbFile.seek(offset);
            numFields = dbFile.readShort();
            offset += NUM_FIELDS_LENGTH;

            // read schema description
            // create variables to be used inside, outside the loop
            String fieldName;
            int fieldLength;
            short fieldNameLength;
            for (int i = 0; i < numFields; i++) {
                dbFile.seek(offset);
                fieldNameLength = dbFile.readShort();
                offset += FIELD_NAME_LENGTH;

                byte[] fieldNameData = read(offset, fieldNameLength);
                fieldName = new String(fieldNameData, encoding);
                offset += fieldNameLength;

                dbFile.seek(offset);
                fieldLength = dbFile.readShort();
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
            // skip the record offset
            int fieldOffset = recordOffset;

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
            throw new IndexOutOfBoundsException();
        }
    }

    @Override
    public String[] readRecord(long recNo) throws RecordNotFoundException {
        String[] record;

        dbRWLock.readLock().lock();
        try {
            if (isDeleted(recNo)) {
                throw new RecordNotFoundException("Record deleted");
            } else if (isLocked(recNo)) {
                throw new RecordNotFoundException("Record locked");
            } else {
                byte[] data = read(recNo, recordLength);
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
        int len = Math.min(criteria.length, record.length);

        for (int i = 0; i < len; i++) {
            if (criteria[i] != null && record[i].startsWith(criteria[i])) {
                matches++;
            }
        }
        return matches;
    }

    /**
     * Utility method to replace nulls in array with swapValue and vice versa
     *
     * @param toSwap array to perform null swap on
     * @param swapValue replacement value for nulls
     * @return the swapped null array
     */
    public static String[] swapNulls(String[] toSwap, String swapValue) {
        for (int i = 0; i < toSwap.length; i++) {
            if (toSwap[i] == null) {
                toSwap[i] = swapValue;
            } else if (swapValue.equals(toSwap[i])) {
                toSwap[i] = null;
            }
        }
        return toSwap;
    }

    @Override
    public long[] findByCriteria(String[] criteria) {
        // findByCritetia definition is the reverse of matchRecord deinition
        criteria = swapNulls(criteria, "");

        Object[] recNos;
        // prevent the dataBuffer from being used while block executes
        synchronized (dataBuffer) {
            dataBuffer.clear();

            String[] record;

            dbRWLock.readLock().lock();
            try {
                // refresh the length of the database file
                getDBFileLength();

                for (long offset = dataOffset; offset < dbFileLength;
                        offset += recordLength) {
                    try {
                        if (!isDeleted(offset)) {
                            record = readRecord(offset);
                            // if there is at least one match
                            if (matchRecord(criteria, record) > 0) {
                                // add the record to the data buffer
                                dataBuffer.put(offset, record);
                            } else {
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
            } finally {
                dbRWLock.readLock().unlock();
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
    }

    /**
     * Takes search parameters and returns records that match. The parameters
     * given should coincide with the arrangement of the fields in each record.
     *
     * @param params the search parameters
     * @return the resulting records from the search
     */
    public Map<Long, String[]> search(String... params) {
        synchronized (dataBuffer) {
            try {
                findByCriteria(params);
                return new LinkedHashMap<>(dataBuffer);
            } finally {
                // ensure that data buffer is cleared after search is done
                //dataBuffer.clear();
            }
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
        short idx = 0;
        int fieldOffset = 0;
        String fieldValue;
        synchronized (recordBuilder) {
            // create the recordbuilder object to be used less record offset
            recordBuilder = new StringBuilder(
                    new String(new byte[recordLength - recordOffset]));
            // for each field name get the number of bytes
            for (int fieldLength : fields.values()) {
                fieldValue = record[idx];
                // ensure the field value in record at least the correct length
                while (fieldValue.length() < fieldLength) {
                    fieldValue += " ";
                }
                recordBuilder.replace(fieldOffset, fieldOffset + fieldLength,
                        fieldValue);

                fieldOffset += fieldLength;
                idx++;
            }
            // if the built record exceeds the specified record length
            if (recordBuilder.length() >= recordLength) {
                throw new IndexOutOfBoundsException("Record built is too long");
            } else {
                return recordBuilder.toString().getBytes();
            }
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
            // write into database file, skipping the record offset bytes
            write(recNo + recordOffset, prepareRecord(data));
        } catch (IndexOutOfBoundsException ex) {
            log.log(Level.SEVERE, "Data exceeds record length"
                    + "\nRecord Length: {0}\nFields: {1}\nData: {2}\n{3}",
                    new Object[]{recordLength, fields, Arrays.toString(data), ex});
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
                // else continue checking until all fields are compared
            }
        }
        // if the record fields were always equal
        return result;
    }

    @Override
    public long createRecord(String[] data) throws DuplicateKeyException {
        long finalOffset = -1; // instantiate to a non usable value
        long offset = dataOffset;

        // prevents code that changes database while block executes
        dbRWLock.readLock().lock();
        try {
            getDBFileLength();

            while (offset < dbFileLength) {

                String[] record = readRecord(offset);

                if (compareRecords(record, data) == 0) {
                    throw new DuplicateKeyException("Duplicate record found");
                } else {
                    // check if the record num is first available free space
                    if (isDeleted(offset) && finalOffset < 0) {
                        finalOffset = offset;
                    }
                }

                offset += recordLength;
            }
        } catch (RecordNotFoundException ex) {
            log.log(Level.WARNING, "Offset: {0}\n{1}",
                    new Object[]{finalOffset, ex});
        } catch (IOException ex) {
            log.log(Level.SEVERE, "Could not read database file\n", ex);
        } finally {
            dbRWLock.readLock().unlock();
        }
        // if there was a free space
        if (finalOffset < 0) {
            finalOffset = offset;
        }

        // prevent code that reads/changes database while block executes
        dbRWLock.writeLock().lock();
        try {
            write(finalOffset, "0".getBytes()); // set deleted byte to false "0"
            writeRecord(finalOffset, data);
        } catch (IOException ex) {
            log.log(Level.SEVERE, "Could not read database file", ex);
        } finally {
            dbRWLock.writeLock().unlock();
        }
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

    /**
     * Checks is a record has been locked using the <code>lockRecord</code>
     * method.
     *
     * @param recNo the record number, its location in the file
     * @return true if the record is locked
     */
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
                log.info("Preparing to lock");
                // use system nano time as seed to generate unique cookie
                byte[] seed = String.valueOf(System.nanoTime()).getBytes();
                long cookie = new SecureRandom(seed).nextLong();

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
    public final int getMagicCookie() {
        return magicCookie;
    }

    /**
     * @return the encoding
     */
    public final Charset getEncoding() {
        return encoding;
    }

    /**
     * @return a string representation of the fields
     */
    public final String getFields() {
        return fields.toString();
    }

    /**
     * @return the numFields
     */
    public final int getNumFields() {
        return numFields;
    }

    /**
     * @return the recordLength
     */
    public final int getRecordLength() {
        return recordLength;
    }

    /**
     * @return the dataOffset
     */
    public final long getDataOffset() {
        return dataOffset;
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

}
