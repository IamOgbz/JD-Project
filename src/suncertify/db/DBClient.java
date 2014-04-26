package suncertify.db;

import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Command line client for testing operations
 *
 * @author Emmanuel
 */
public class DBClient {

    private static final Logger log = Logger.getLogger("suncertify.db");

    /**
     * Main entry.
     * 
     * @param args the initial command line arguments
     */
    public static void main(String[] args) {
        try {
            Data database = new Data("C:\\Users\\Emmanuel\\Documents\\Training\\Koenig\\jd\\Project\\instructions-121\\db-1x1.db");
            Collection<String[]> data = database.search(new String[]{null}).values();
            String results = "";
            for (String[] record : data) {
                results += "\n" + toArrayString(record);
            }
            log.info(results);
        } catch (IOException ex) {
            log.log(Level.SEVERE, null, ex);
        }

    }

    /**
     * Take an array and returns a string formatted for printing.
     *
     * @param array the array to convert
     * @return a string representation of the array
     */
    public static String toArrayString(Object[] array) {
        String as = "";
        for (Object o : array) {
            as += "[" + o.toString() + "](" + o.toString().length() + ") ";
        }
        return as;
    }

}
