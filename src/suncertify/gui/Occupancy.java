package suncertify.gui;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Representation of database record data. Unit of measurement for the
 * size/length is bytes
 *
 * @author Emmanuel
 */
public class Occupancy implements Serializable {

    /**
     * The size of the flag that specifies if a record has been deleted.
     */
    public static final byte DELETED_FLAG = 0b1;

    /**
     * The size of the field for the name of the hotel that the room is in.
     */
    public static final short NAME_LENGTH = 64;

    /**
     * The size of the field for the hotel city.
     */
    public static final short LOCATION_LENGTH = 64;

    /**
     * The size of the field for the room occupancy limit.
     */
    public static final short SIZE_LENGTH = 4;

    /**
     * The size of the field for the smoking status of the room.
     */
    public static final short SMOKING_LENGTH = 1;

    /**
     * The size of the field for the room pricing.
     */
    public static final short RATE_LENGTH = 8;

    /**
     * The size of the field for the date in format yyyy/mm/dd.
     */
    public static final short DATE_LENGTH = 10;

    /**
     * The size of the field for the customer id holding the room.
     */
    public static final short OWNER_LENGTH = 8;

    /**
     * The computed size of the record.
     */
    public static final short RECORD_LENGTH = DELETED_FLAG + NAME_LENGTH
            + LOCATION_LENGTH + SIZE_LENGTH + SMOKING_LENGTH + RATE_LENGTH
            + DATE_LENGTH + OWNER_LENGTH;

    /**
     * Data Address. Store the offset location in the database file.
     */
    protected final long address;

    /**
     * The Logger instance. All log messages from this class are routed through
     * this member. The Logger namespace is <code>suncertify.db</code>.
     */
    private static transient final Logger log = Logger.getLogger("suncertify.gui");

    /**
     * Deleted Flag. Store the state of the record.
     */
    private byte deleted;

    /**
     * Hotel Name. Stores the name of the hotel this vacancy record relates to.
     */
    private String name;

    /**
     * City. Stores the location of this hotel.
     */
    private String location;

    /**
     * Maximum occupancy of this room. The maximum number of people permitted in
     * this room, not including infants.
     */
    private int size;

    /**
     * Is the room smoking or non-smoking. Flag indicating if smoking is
     * permitted. Valid values are "Y" indicating a smoking room, and "N"
     * indicating a non-smoking room.
     */
    private char smoking;

    /**
     * Price per night. Charge per night for the room. This field includes the
     * currency symbol.
     */
    private String rate;

    /**
     * Date available. The single night to which this record relates, format
     * returned is yyyy/mm/dd.
     */
    private Date date;

    /**
     * Customer holding this record. The customer id (an 8 digit number). Note:
     * It is assumed that both the customers and the CSRs know the customer id.
     * However the booking must always be made by CSR by entering the customer
     * id against the room reservation.
     */
    private String owner;

    /**
     * Creates instance of this object with field values in the array.
     *
     * Fields values are in the order: deleted a byte to set the deleted flag
     * name the name of the hotel. location the city of the hotel. size the
     * maximum occupancy of the room. smoking 'Y' if smoking, 'N' if
     * non-smoking. rate the price per night, contains currency symbol. date the
     * night to which the record relates to. owner the customer holding this
     * occupancy.
     *
     * @param address the record offset in the database file (recNo)
     * @param fields the values stored in reach record
     */
    public Occupancy(long address, String... fields) {
        this.address = address;
        if (fields == null) {
            fields = new String[0];
        }
        this.name = fields.length > 0 ? fields[0].trim() : null;
        this.location = fields.length > 1 ? fields[1].trim() : null;
        try {
            this.size = fields.length > 2 ? Integer.parseInt(fields[2].trim()) : 0;
        } catch (NumberFormatException ex) {
            log.log(Level.WARNING, "{0} could not be parsed into int {1}",
                    new Object[]{fields[2].trim(), ex});
            this.size = 0;
        }
        this.smoking = fields.length > 3 ? fields[3].charAt(0) : ' ';
        this.rate = fields.length > 4 ? fields[4].trim() : null;
        try {
            this.date = fields.length > 5
                    ? DateFormat.getInstance().parse(fields[5].trim()) : null;
        } catch (ParseException ex) {
            log.log(Level.WARNING, "{0} could not be parsed into Date {1}",
                    new Object[]{fields[5].trim(), ex});
            this.date = null;
        }
        this.owner = fields.length > 6 ? fields[6].trim() : null;
        this.deleted = fields.length > 7 ? fields[7].getBytes()[0] : 0;
    }

    /**
     * Creates instance of this class with parameter values.
     *
     * @param address the record offset in the database file (recNo)
     * @param name the name of the hotel.
     * @param location the city of the hotel.
     * @param size the maximum occupancy of the room.
     * @param smoking 'Y' if smoking, 'N' if non-smoking.
     * @param rate the price per night, contains currency symbol.
     * @param date the night to which the record relates to.
     * @param owner the customer holding this occupancy.
     * @param deleted a byte to set the deleted flag
     */
    public Occupancy(long address, String name, String location, short size,
            char smoking, String rate, Date date, String owner, byte deleted) {
        this.address = address;
        this.name = name;
        this.location = location;
        this.size = size;
        this.smoking = smoking;
        this.rate = rate;
        this.date = date;
        this.owner = owner;
        this.deleted = deleted;
    }

    /**
     * The address of the record in the database file.
     *
     * @return the address
     */
    public long getAddress() {
        return address;
    }

    /**
     * If the record is deleted or not.
     *
     * @return true if the deleted flag is greater than 0
     */
    public boolean isDeleted() {
        return deleted > 0;
    }

    /**
     * Byte representing the deletion status of the occupancy.
     *
     * @return the deleted flag as a string ("1" for deleted)
     */
    public byte getDeleted() {
        return deleted;
    }

    /**
     * Set the byte representing the deletion status of the occupancy.
     *
     * @param deleted set the deleted flag (1 for deleted)
     */
    public void setDeleted(byte deleted) {
        this.deleted = deleted;
    }

    /**
     * Name of the hotel, truncated if value is longer than the record field.
     *
     * @return the name of the hotel
     */
    public String getName() {
        return name.length() <= NAME_LENGTH ? name
                : name.substring(0, NAME_LENGTH);
    }

    /**
     * Sets name of the hotel
     *
     * @param name the name of the hotel
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * City location, truncated if value is longer than the record field.
     *
     * @return the city location of the hotel
     */
    public String getLocation() {
        return location.length() <= LOCATION_LENGTH ? location
                : location.substring(0, LOCATION_LENGTH);
    }

    /**
     * Sets the hotel city.
     *
     * @param location the city of the hotel
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * Maximum room occupancy.
     *
     * @return the maximum occupancy of this room as a String
     */
    public String getSize() {
        return size != 0 ? String.valueOf(size) : null;
    }

    /**
     * Sets the maximum room occupancy.
     *
     * @param size set the maximum occupancy of this room
     */
    public void setSize(int size) {
        this.size = size;
    }

    /**
     * If the room is smoking or not smoking.
     *
     * @return true if the room is a smoking room
     */
    public boolean isSmoking() {
        return smoking == 'Y';
    }

    /**
     * Return the room smoking value.
     *
     * @return the room smoking value
     */
    public String getSmoking() {
        return smoking != ' ' ? String.valueOf(smoking) : null;
    }

    /**
     * Set the room smoking option. Accepted values are 'Y' or 'N'.
     *
     * @param smoking set the room smoking value
     */
    public void setSmoking(char smoking) {
        switch (smoking) {
            case ' ':
            case 'Y':
            case 'N':
                this.smoking = smoking;
                break;
            case 'y':
            case 'n':
                this.smoking = Character.toUpperCase(smoking);
                break;
            case '1':
                this.smoking = 'Y';
                break;
            case '0':
            default:
                this.smoking = 'N';
        }
    }

    /**
     * Return the per night rate of the room, including currency. Value is
     * truncated if value is longer than the record field.
     *
     * @return the price per night
     */
    public String getRate() {
        return rate.length() <= RATE_LENGTH ? rate
                : rate.substring(0, RATE_LENGTH);
    }

    /**
     * @param rate the price per night
     */
    public void setRate(String rate) {
        this.rate = rate;
    }

    /**
     * @return the date room is available in format yyyy/mm/dd
     */
    public String getDate() {
        return date != null
                ? new SimpleDateFormat("yyyy/mm/dd").format(date.getTime()) : null;
    }

    /**
     * @param date the available date to set.
     */
    public void setDate(Date date) {
        this.date = date;
    }

    /**
     * Returns the id of customer holding this occupancy, truncated if value is
     * longer than the record field.
     *
     * @return owner, the customer holding this record
     */
    public String getOwner() {
        return owner.length() <= OWNER_LENGTH ? owner
                : owner.substring(0, OWNER_LENGTH);
    }

    /**
     * Set the id of the customer holding this occupancy.
     *
     * @param owner set the customer holding this record
     */
    public void setOwner(String owner) {
        this.owner = owner;
    }

    /**
     * To create a string array from the occupancy to be used for writing into
     * the database file
     *
     * @return a string array representation of the occupancy
     */
    public String[] toRecord() {
        String[] record = new String[]{getName(), getLocation(), getSize(),
            getSmoking(), getRate(), getDate(), getOwner()};
        return record;
    }

    /**
     * @return a String representation of the Occupancy class
     */
    @Override
    public String toString() {
        return "Occupancy{" + "deleted=" + isDeleted() + ", name=" + name
                + ", location=" + location + ", size=" + size
                + ", smoking=" + isSmoking() + ", rate=" + rate
                + ", date=" + date + ", owner=" + owner + '}';
    }

}
