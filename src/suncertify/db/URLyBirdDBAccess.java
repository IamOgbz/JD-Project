package suncertify.db;

import java.util.Collection;

/**
 * Interface to define methods for accessing the database.
 * 
 * @author Emmanuel
 */
public interface URLyBirdDBAccess {
    
    /**
     * Get the occupancy entry at the specified location.
     * 
     * @param address the location in the database file
     * @return the occupancy or null if there is none at that index
     */
    public Occupancy getOccupancy(long address);

    /**
     * Get all the occupancies in the database.
     * 
     * @return a collection of Occupancy objects
     */
    public Collection<Occupancy> getOccupancies();
    
    /**
     * Takes search parameters and returns records that match. The parameters
     * given should coincide with the arrangement of the fields in each record.
     * Null values match nothing and empty strings match all.
     *
     * @param params the search parameters
     * @return the resulting records from the search
     */
    public Collection<Occupancy> searchOccupancies(String... params);

}
