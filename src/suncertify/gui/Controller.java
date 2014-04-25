package suncertify.gui;

import java.util.logging.Logger;
import suncertify.db.DBAccess;

/**
 *
 * @author Emmanuel
 */
public class Controller {

    /**
     * the object that accesses out database
     */
    private DBAccess db;

    /**
     * The Logger instance. All log messages from this class are routed through
     * this member. The Logger namespace is <code>suncertify.db</code>.
     */
    private static final Logger log = Logger.getLogger("suncertify.conn");

    /**
     * Creates instance of this class with default values.
     */
    public Controller() {
    }

}
