package suncertify.conn;

import java.rmi.Remote;
import suncertify.db.URLyBirdDBAccess;

/**
 * Interface for remote database access.
 * 
 * @author Emmanuel
 */
public interface RemoteDBAccess extends Remote, URLyBirdDBAccess {
}
