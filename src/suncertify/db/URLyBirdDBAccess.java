package suncertify.db;

import suncertify.gui.Occupancy;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

/**
 * Interface for client access to database.
 *
 * @author Emmanuel
 */
public interface URLyBirdDBAccess {

    /**
     * Get a particular occupancy record from the database using the record
     * address.
     *
     * @param address the record address in the database
     * @return Occupancy object of the record
     * @throws IOException
     */
    public Occupancy getOccupancy(long address) throws IOException;

    /**
     * Set a particular occupancy to the database records.
     *
     * @param occupancy the new occupancy record values
     * @param append true to create a new record, false to update an old
     * undeleted record
     * @throws IOException
     */
    public void setOccupancy(Occupancy occupancy, boolean append) throws IOException;

    /**
     * Get all the active occupancies (undeleted records) in the database.
     *
     * @return Collection of occupancy records
     * @throws IOException
     */
    public Collection<Occupancy> getOccupancies() throws IOException;

    /**
     * Search for occupancies matching the parameters supplied. The parameters
     * should match the arrangement of the fields in the database records.
     *
     * @param params the search criteria, with record fields in matching order
     * @return Collection of occupancy records that match criteria
     * @throws IOException
     */
    public Collection<Occupancy> searchOccupancies(String... params) throws IOException;

}
