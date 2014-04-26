package suncertify.gui;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import suncertify.conn.DBConnection;
import static suncertify.conn.DBConnection.Type.DIRECT;
import static suncertify.conn.DBConnection.Type.NETWORK;
import static suncertify.gui.Application.Mode.CLIENT;
import static suncertify.gui.Application.Mode.STANDALONE;

/**
 * GUI Client module
 *
 * @author Emmanuel
 */
public class Client extends JFrame {

    private final Application.Mode appMode;
    private final ConfigPanel configPanel;
    private final DBConnection.Type connType;
    private Controller controller;
    private String location;
    private String port;

    /**
     * The Logger instance. All log messages from this class are routed through
     * this member. The Logger namespace is <code>suncertify.gui</code>.
     */
    private static final Logger log = Logger.getLogger("suncertify.gui");

    /**
     * Takes arguments which determine the kind of client to initialise
     *
     * @param args
     */
    public Client(String[] args) {
        super("URLyBird Client");
        appMode = args.length > 0 ? STANDALONE : CLIENT;
        connType = args.length > 0 ? DIRECT : NETWORK;
        configPanel = new ConfigPanel(appMode);
        do {
            boolean config = getConfig();
            if (config) {
                log.info("Attempting to connect using accepted configuration.");
                controller = new Controller(connType, location, port);
            } else {
                System.exit(0);
            }
        } while (!controller.isConnected());
        System.exit(0);
    }

    /**
     * Create a dialog to get the connection configuration.
     *
     * @return true if the user entered a valid value and clicked connect or
     * false if the user cancels out
     */
    private boolean getConfig() {
        int status;
        JOptionPane optionPane = new JOptionPane(configPanel,
                JOptionPane.QUESTION_MESSAGE);
        JDialog connConfig = optionPane.createDialog("Connect to database");
        do {
            connConfig.setVisible(true);
            location = configPanel.getLocationFieldText();
            port = configPanel.getPortNumberText();
            if (optionPane.getValue() != null) {
                if (location.isEmpty()) {
                    status = -2;
                    Application.handleException(
                            "No database location specified", null);
                } else if (port.isEmpty()) {
                    status = -1;
                    Application.handleException(
                            "No server port number specified", null);
                } else {
                    status = 0;
                }
            } else {
                status = 2;
            }
        } while (status < 0);
        return status == 0;
    }

}
