package suncertify.db;

/**
 * The default interface supplied by Oracle defining the compulsory methods for
 * database access
 *
 * @author Oracle
 */
public interface DBAccess {

    /**
     * Reads a record from the file. Performing the read operation all at once
     * is faster than reading individual fields.
     *
     * @param recNo the location of the record in the db file.
     * @return array of string where element is a record value.
     * @throws RecordNotFoundException if valid record is not found at location,
     * has been deleted or is currently locked by the lock method
     */
    public String[] readRecord(long recNo)
            throws RecordNotFoundException;

    /**
     * Modifies the fields of a record. The new value for field n appears in
     * data[n].
     *
     * @param recNo the location of the record in the db file.
     * @param data the array of string where element is a record value.
     * @param lockCookie the cookie returned when record was locked.
     * @throws RecordNotFoundException if valid record is not found at location
     * or has been deleted.
     * @throws SecurityException if the record is locked with a cookie other
     * than lockCookie.
     */
    public void updateRecord(long recNo, String[] data, long lockCookie)
            throws RecordNotFoundException, SecurityException;

    /**
     * Deletes a record, making the record number and associated disk storage
     * available for reuse.
     *
     * @param recNo the record location in the db file.
     * @param lockCookie the cookie returned when locking the record
     * @throws RecordNotFoundException if valid record is not found at location
     * or has been deleted.
     * @throws SecurityException if the record is locked with a cookie other
     * than lockCookie.
     */
    public void deleteRecord(long recNo, long lockCookie)
            throws RecordNotFoundException, SecurityException;

    /**
     * Returns an array of record numbers that match the specified criteria.
     * Field n in the database file is described by criteria[n]. A null value in
     * criteria[n] matches any field value. A non-null value in criteria[n]
     * matches any field value that begins with criteria[n]. (For example,
     * "Fred" matches "Fred" or "Freddy" and "" matches nothing.) The type of
     * search implemented is OR so one null value would return all the records
     * in the table.
     *
     * @param criteria the array of search criteria.
     * @return an array of matched records locations.
     */
    public long[] findByCriteria(String[] criteria);

    /**
     * Creates a new record in the database (possibly reusing a deleted entry).
     * Inserts the given data, and returns the record number of the new record.
     *
     * @param data the array of string data, where each element is record data,
     * to be inserted as a record.
     * @return the record number, i.e the location of the record in db file.
     * @throws DuplicateKeyException if key used for record id is a duplicate.
     */
    public long createRecord(String[] data)
            throws DuplicateKeyException;

    /**
     * Locks a record so that it can only be updated or deleted by this client.
     * If the specified record is already locked by a different client, the
     * current thread gives up the CPU and consumes no CPU cycles until the
     * record is unlocked.
     *
     * @param recNo the record location in the db file.
     * @return the lock cookie to be used when record is unlocked, updated, or
     * deleted.
     * @throws RecordNotFoundException if valid record is not found at location
     * or has been deleted.
     */
    public long lockRecord(long recNo)
            throws RecordNotFoundException;

    /**
     * Releases the lock on a record. Cookie must be the cookie returned when
     * the record was locked; otherwise throws SecurityException.
     *
     * @param recNo the record location in the db file.
     * @param cookie the lock cookie used when record was locked.
     * @throws SecurityException if the record is locked with a cookie other
     * than cookie.
     */
    public void unlock(long recNo, long cookie)
            throws SecurityException;

}
