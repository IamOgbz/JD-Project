/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package suncertify.db;

import java.io.IOException;
import java.util.Collection;

/**
 * Wrapper class for the database access implementation. For URLyBird clients to
 * access the database.
 *
 * @author Emmanuel
 */
public class URLyBirdData implements URLyBirdDBAccess {

    Data database;

    /**
     * Instantiate the data object.
     *
     * @param databasePath the database location
     * @throws java.io.IOException
     */
    public URLyBirdData(String databasePath) throws IOException {
        database = new Data(databasePath);
    }

    @Override
    public Occupancy getOccupancy(long address) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setOccupancy(Occupancy occupancy, boolean append) {
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
