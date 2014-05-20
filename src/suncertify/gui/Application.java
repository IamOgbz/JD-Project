package suncertify.gui;

import java.awt.Component;
import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;
import java.util.Arrays;
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
     * The icon image for the application.
     */
    public static final Image icon;

    static {
        URL url = ClassLoader.getSystemResource("suncertify/resources/icon.png");
        Toolkit kit = Toolkit.getDefaultToolkit();
        icon = kit.createImage(url);
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

        log.info(icon.toString());

        // test for which type of application to run
        if (args.length == 0 || "alone".equals(args[0])) {
            new Client(args);
        } else if ("server".equals(args[0])) {
            new Server();
        } else {
            log.log(Level.INFO, "Invalid launch parameter passed: ",
                    Arrays.toString(args));
            System.err.println("Valid launch parameters are: ");
            System.err.println("\"alone\" - to start in standalone mode");
            System.err.println("\"server\" - to start in server mode");
            System.err.println("\"\" (nothing) - to start in client mode");
            System.exit(0);
        }
    }

    /**
     * Handles exceptions that need to be displayed to the user. Exceptions are
     * displayed to the user as errors, while others are displayed as alerts.
     *
     * @param msg the message to be displayed
     * @param ex the exception that was handled
     * @param parent the component that calls the dialog
     */
    public static void handleException(String msg, Exception ex, Component parent) {
        log.log(Level.WARNING, msg, ex);
        String title = ex == null ? "Alert" : "Error";
        int type = ex == null ? INFORMATION_MESSAGE : ERROR_MESSAGE;
        JDialog alertDialog = new JOptionPane(msg, type,
                JOptionPane.DEFAULT_OPTION).createDialog(null, title);
        if (parent == null) {
            alertDialog.setIconImage(icon);
        }
        alertDialog.setVisible(true);
    }

}
