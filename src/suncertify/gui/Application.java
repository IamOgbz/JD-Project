package suncertify.gui;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.INFORMATION_MESSAGE;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * Application main class. Chooses which application module to run based on the
 * command lone arguments.
 *
 * @author Emmanuel
 */
public class Application {

    /**
     * The valid application modes
     */
    public static enum Mode {

        /**
         * the client mode
         */
        CLIENT,
        /**
         * the server mode
         */
        SERVER,
        /**
         * the local database mode
         */
        STANDALONE;
    }
    /**
     * The Logger instance. All log messages from this class are routed through
     * this member. The Logger namespace is <code>suncertify</code>.
     */
    private static final Logger log = Logger.getLogger("suncertify.gui");

    /**
     * The method called upon Application execution.
     *
     * @param args the command line arguments, which should be either "server",
     * "alone" or empty
     */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (UnsupportedLookAndFeelException uex) {
            log.warning("Unsupported look and feel specified");
        } catch (ClassNotFoundException cex) {
            log.warning("Look and feel could not be located");
        } catch (InstantiationException iex) {
            log.warning("Look and feel could not be instanciated");
        } catch (IllegalAccessException iaex) {
            log.warning("Look and feel cannot be used on this platform");
        }

        // test for which type of application to run
        //Server server = new Server();
        Client client = new Client(args);
    }

    /**
     * Handles exceptions that need to be displayed to the user.
     *
     * @param msg the message to be displayed
     * @param ex the exception that was handled
     */
    public static void handleException(String msg, Exception ex) {
        log.log(Level.WARNING, msg, ex);
        String title = ex == null ? "Alert" : "Error";
        int type = ex == null ? INFORMATION_MESSAGE : ERROR_MESSAGE;
        JDialog alertDialog = new JOptionPane(msg, type,
                JOptionPane.DEFAULT_OPTION).createDialog(title);
        alertDialog.setVisible(true);
    }

}
