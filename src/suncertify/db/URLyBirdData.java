/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package suncertify.db;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;
import static suncertify.db.Data.dbFile;
import static suncertify.db.Data.dbRWLock;

/**
 * The class for reading and writing the URLyBird specific database file. Uses
 * the wrapper design pattern to give specific
 *
 * @author Emmanuel
 */
public class URLyBirdData extends Data {

    /**
     * Data file cookie identifier.
     */
    public static int magicCookie;

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
     * The Logger instance. All log messages from this class are routed through
     * this member. The Logger namespace is <code>suncertify.db</code>.
     */
    private static final Logger log = Logger.getLogger("suncertify.db");

    /**
     * Creates instance of this class with default values.
     *
     * @param databasePath the database file location
     * @throws FileNotFoundException if file is not found
     * @throws IOException if file cannot be read
     */
    public URLyBirdData(String databasePath) throws FileNotFoundException,
            IOException {
        super(databasePath);
    }

    /**
     * Read the header for URLyBird database and the schema definition.
     *
     * @return the position to start reading the record data i.e after the
     * schema
     * @throws IOException
     */
    @Override
    public long readHeader() throws IOException {
        // the offset storing the read operation cursor
        long offset;
        // set charater encoding used for the data
        encoding = StandardCharsets.US_ASCII;
        // add deleted flag the fields
        fields.put("deleted", 1);
        // the length of the deleted field
        recordLength = 1;
        // prevent write operations from happening while reading
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
        } finally {
            dbRWLock.readLock().unlock();
        }
        return offset;
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
            if ("1".equals(new String(data, 0, 1))) {
                throw new RecordNotFoundException("Record deleted");
            } else {
                record = parseRecord(data);
            }
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

}
