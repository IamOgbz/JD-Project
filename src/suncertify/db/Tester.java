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
            log.log(Level.INFO, "\nmagic cookie: {0}\nrecord length: {1}"
                    + "\nnum fields: {2}\nfields: {3}\ndata offset\nnum records",
                    new Object[]{Data.magicCookie, Data.recordLength, Data.numFields,
                        data.fields.toString(), String.valueOf(data.dataOffset)});
            String table = "";
            Collection<String[]> records = data.search("");
            long offset = data.dataOffset;
            for (String[] record : records) {
                table += "\n" + Data.toArrayString(record);
                record[6] = "";
                data.updateRecord(offset, record, 0);
                offset += Data.recordLength;
            }
            log.log(Level.INFO, "num records: {0}{1}",
                    new Object[]{records.size(), table});
            // test update
            // test delete
            // test create
        } catch (IOException | RecordNotFoundException | SecurityException ex) {
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
