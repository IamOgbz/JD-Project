package suncertify.conn;

import java.rmi.Remote;
import suncertify.db.URLyBirdDBAccess;

/**
 * Interface for client access the database remotely.
 *
 * @author Emmanuel
 */
public interface RemoteDBAccess extends Remote, URLyBirdDBAccess {
}
