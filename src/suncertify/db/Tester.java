package suncertify.db;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Emmanuel
 */
public class Tester {

    private static final Logger log = Logger.getLogger("suncertify.db");
    private static final String dbPath = "C:\\Users\\Emmanuel\\Documents\\Training\\Koenig\\jd\\Project\\instructions-121\\db-1x1.db";

    public static void main(String[] args) {
        log.log(Level.INFO, "Occupancy Record Length: {0}",
                String.valueOf(Occupancy.RECORD_LENGTH));
        try {
            Data data = new URLyBirdData(dbPath);
            log.log(Level.INFO, "magic cookie: {0}", URLyBirdData.magicCookie);
            log.log(Level.INFO, "record length: {0}",
                    URLyBirdData.recordLength);
            log.log(Level.INFO, "num fields: {0}", URLyBirdData.numFields);
            log.log(Level.INFO, "fields: {0}", Data.fields.toString());
            log.log(Level.INFO, "data offset: {0}",
                    String.valueOf(data.dataOffset));
        } catch (IOException ex) {
            Logger.getLogger(Tester.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
