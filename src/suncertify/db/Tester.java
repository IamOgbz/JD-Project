package suncertify.db;

import java.io.IOException;
import java.util.Collection;
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
            Data data = new Data(dbPath);
            log.log(Level.INFO, "magic cookie: {0}", Data.magicCookie);
            log.log(Level.INFO, "record length: {0}", Data.recordLength);
            log.log(Level.INFO, "num fields: {0}", Data.numFields);
            log.log(Level.INFO, "fields: {0}", data.fields.toString());
            log.log(Level.INFO, "data offset: {0}",
                    String.valueOf(data.dataOffset));
            String table = "";
            Collection<String[]> records = data.search("");
            long offset = data.dataOffset;
            for (String[] record : records) {
                offset += Data.recordLength;
                table += "\n"+ offset + " " + toArrayString(record);
            }
            log.info(table);
            // test update
            // test delete
            // test create
        } catch (IOException ex) {
            log.log(Level.SEVERE, null, ex);
        }
    }

    public static String toArrayString(Object[] array) {
        String s = "";
        for (Object o : array) {
            s += "[" + o.toString() + "](" + o.toString().length() + ") ";
        }
        return s;
    }

}
