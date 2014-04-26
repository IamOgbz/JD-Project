/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package suncertify.db;

import suncertify.gui.Occupancy;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

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
    public Occupancy getOccupancy(long address) throws IOException {
        try {
            String[] record = database.readRecord(address);
            return new Occupancy(address, record);
        } catch (RecordNotFoundException ex) {
            throw new IOException("Could not get occupancy");
        }
    }

    @Override
    public void setOccupancy(Occupancy occupancy, boolean append)
            throws IOException {
        long recNo = -1, lockCookie = -1;
        String[] data = occupancy.toRecord();
        if (append) {
            try {
                database.createRecord(data);
            } catch (DuplicateKeyException ex) {
                throw new IOException("Could not insert occupancy");
            }
        } else {
            try {
                recNo = occupancy.getAddress();
                lockCookie = database.lockRecord(recNo);
                database.updateRecord(recNo, data, lockCookie);
            } catch (RecordNotFoundException ex) {
                throw new IOException("Could not find occupancy");
            } finally {
                if (database.isLocked(recNo)) {
                    database.unlock(recNo, lockCookie);
                }
            }
        }
    }

    @Override
    public Collection<Occupancy> getOccupancies() {
        return searchOccupancies();
    }

    @Override
    public Collection<Occupancy> searchOccupancies(String... params) {
        if (params.length > 0) {
            Data.swapNulls(params, "");
        } else {
            params = new String[]{null};
        }
        Map<Long, String[]> result = database.search(params);
        Collection<Occupancy> occupancies = new LinkedList<>();
        for (Long recNo : result.keySet()) {
            occupancies.add(new Occupancy(recNo, result.get(recNo)));
        }
        return occupancies;
    }

}
