package suncertify.db;

/**
 * Exception thrown when record is not found in db
 *
 * @author Emmanuel
 */
class RecordNotFoundException extends Exception {

    /**
     * The description about the exception.
     */
    public final String description;

    /**
     * Creates instance of this object with default values
     * <code>this.description = "Record Not Found";</code>
     */
    public RecordNotFoundException() {
        this.description = "Record Not Found";
    }

    /**
     * @param desc the description about this exception.
     */
    public RecordNotFoundException(String desc) {
        this.description = desc;
    }

    @Override
    public String getMessage() {
        return this.description;
    }

}
