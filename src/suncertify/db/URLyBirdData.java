/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package suncertify.db;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
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
     * The character encoding used in the URLyBird database file
     */
    public static final Charset ENCODING = StandardCharsets.US_ASCII;

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
        // add deleted flag the fields
        fields.put("deleted", 1);
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
            // for record length
            dbFile.seek(offset);
            recordLength = dbFile.readInt();
            offset += RECORD_LENGTH_BYTES;
            // for number of fields in record
            dbFile.seek(offset);
            numFields = dbFile.readShort();
            offset += NUM_FIELDS_LENGTH;
            // read schema description
            // loop for number of fields in a record
            String fieldName;
            int fieldLength;
            short fieldNameLength;
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
                fieldName = new String(fieldNameData, ENCODING);
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

}
