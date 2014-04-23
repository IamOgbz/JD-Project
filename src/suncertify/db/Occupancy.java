package suncertify.db;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

/**
 * Representation of database record data. Unit of measurement for the
 * size/length is bytes
 *
 * @author Emmanuel
 */
public class Occupancy {

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
     * The Logger instance. All log messages from this class are routed through
     * this member. The Logger namespace is <code>suncertify.db</code>.
     */
    private static final Logger log = Logger.getLogger("suncertify.db");

    /**
     * Data Address. Store the offset location in the database file.
     */
    //private long address;
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
     * @param fields the values stored in reach record
     * @throws java.text.ParseException if the date string fails to be parsed
     */
    public Occupancy(String[] fields) throws ParseException {
        this.name = fields[0];
        this.location = fields[1];
        this.size = Integer.parseInt(fields[2]);
        this.smoking = fields[3].charAt(0);
        this.rate = fields[4];
        this.date = DateFormat.getInstance().parse(fields[5]);
        this.owner = fields[6];
        this.deleted = fields[7].getBytes()[0];
    }

    /**
     * Creates instance of this class with parameter values.
     *
     * @param name the name of the hotel.
     * @param location the city of the hotel.
     * @param size the maximum occupancy of the room.
     * @param smoking 'Y' if smoking, 'N' if non-smoking.
     * @param rate the price per night, contains currency symbol.
     * @param date the night to which the record relates to.
     * @param owner the customer holding this occupancy.
     * @param deleted a byte to set the deleted flag
     */
    public Occupancy(String name, String location, short size,
            char smoking, String rate, Date date, String owner, byte deleted) {
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
     * If the record is deleted or not.
     *
     * @return true if the deleted flag is greater than 0
     */
    public boolean isDeleted() {
        return deleted > 0;
    }

    /**
     * @return the deleted flag as a string ("1" for deleted)
     */
    public byte getDeleted() {
        return deleted;
    }

    /**
     * @param deleted set the deleted flag (1 for deleted)
     */
    public void setDeleted(byte deleted) {
        this.deleted = deleted;
    }

    /**
     * @return the name of the hotel
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name of the hotel
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the city location of the hotel
     */
    public String getLocation() {
        return location;
    }

    /**
     * @param location the city of the hotel
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * @return the maximum occupancy of this room as a String
     */
    public String getSize() {
        return String.valueOf(size);
    }

    /**
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
     * @return the room smoking value
     */
    public String getSmoking() {
        return String.valueOf(smoking);
    }

    /**
     * @param smoking set the room smoking value
     */
    public void setSmoking(char smoking) {
        this.smoking = smoking;
    }

    /**
     * @return the price per night
     */
    public String getRate() {
        return rate;
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
        return new SimpleDateFormat("yyyy/mm/dd").format(date.getTime());
    }

    /**
     * @param date the available date to set.
     */
    public void setDate(Date date) {
        this.date = date;
    }

    /**
     * @return owner, the customer holding this record
     */
    public String getOwner() {
        return owner;
    }

    /**
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
