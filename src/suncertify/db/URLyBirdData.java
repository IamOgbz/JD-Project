/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package suncertify.db;

import java.io.IOException;
import java.util.Collection;
import java.util.logging.Logger;

/**
 * Class to access underlying database access class.
 * @author Emmanuel
 */
public class URLyBirdData implements URLyBirdDBAccess {

    /**
     * the object that accesses out database
     */
    private final DBAccess db;

    /**
     * The Logger instance. All log messages from this class are routed through
     * this member. The Logger namespace is <code>suncertify.db</code>.
     */
    private static final Logger log = Logger.getLogger("suncertify.conn");

    /**
     * Instantiates a URLyBirdData object to be used for accessing the database.
     *
     * @param databasePath the database location.
     * @throws IOException
     */
    public URLyBirdData(String databasePath) throws IOException {
        db = new Data(databasePath);
    }

    @Override
    public Occupancy getOccupancy(long address) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Collection<Occupancy> getOccupancies() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Collection<Occupancy> searchOccupancies(String... params) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
