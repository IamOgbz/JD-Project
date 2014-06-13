package suncertify.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import suncertify.conn.DBConnection;

/**
 * The Server module. This is the GUI a user see when the application is started
 * in server mode
 *
 * @author Emmanuel
 */
public class Server extends JFrame {

    // Pre defined string to use in the interface.
    // Used to make changes to control text easier
    private static final String START_BUTTON_TEXT = "Start Server";
    private static final String START_BUTTON_TOOLTIP
            = "Enter server configuration then click to start server";
    private static final String SERVER_STARTED_TOOLTIP   = "Server has started";
    private static final String EXIT_BUTTON_TEXT = "Exit";
    private static final String EXIT_BUTTON_TOOLTIP
            = "Stops the server as soon as it is safe and exits the application";

    /**
     * The Logger instance. All log messages from this class are routed through
     * this member. The Logger namespace is <code>suncertify.gui</code>.
     */
    private static final Logger log = Logger.getLogger("suncertify.gui");
    // configuration panel
    private final ConfigPanel configPanel;
    // control panel
    private final JPanel ctrlPanel;
    private final JButton startButton;
    private final JButton exitButton;
    // server status
    private String port;
    private boolean running;
    private String location;

    /**
     * Instantiates the Server GUI and handles initial configurations.
     */
    public Server() {
        super("URLyBird Server");
        this.setDefaultCloseOperation(Server.EXIT_ON_CLOSE);
        this.setResizable(false);
        
        setIconImage(Application.icon);

        this.running = false;

        this.startButton = new JButton(START_BUTTON_TEXT);
        this.exitButton = new JButton(EXIT_BUTTON_TEXT);

        configPanel = new ConfigPanel(Application.Mode.SERVER);
        this.add(configPanel, BorderLayout.NORTH);
        ctrlPanel = controlPanel();
        this.add(ctrlPanel, BorderLayout.SOUTH);

        this.pack();
        // Center on screen
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) ((d.getWidth() - this.getWidth()) / 2);
        int y = (int) ((d.getHeight() - this.getHeight()) / 2);
        this.setLocation(x, y);
        this.setVisible(true);
    }

    /**
     * Configures the control panel. Adding the start, stop and exit buttons to
     * it and setting their actions.
     *
     * @return JPanel with the start, stop and exit server buttons already
     * configured.
     */
    private JPanel controlPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        startButton.setToolTipText(START_BUTTON_TOOLTIP);
        startButton.addActionListener(new StartServer(this));
        panel.add(startButton);

        exitButton.setToolTipText(EXIT_BUTTON_TOOLTIP);
        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                System.exit(0);
            }
        });
        panel.add(exitButton);

        return panel;
    }

    /**
     * Checks if the server is currently running or not.
     *
     * @return true if the server is running
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Class to handle the action of starting the server.
     */
    private class StartServer implements ActionListener {
        
        /**
         * Reference to the parent component
         */
        private final Component frame;
        
        /**
         * Default Constructor.
         * @param frame the parent component
         */
        public StartServer(Component frame){
            this.frame = frame;
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            // start server
            synchronized (startButton) {
                if (configPanel.getLocationFieldText().isEmpty()) {
                    Application.handleException(
                            "Location not given.", null, frame);
                } else if (configPanel.getPortNumberText().isEmpty()) {
                    Application.handleException(
                            "Port number not given.", null, frame);
                } else {
                    location = configPanel.getLocationFieldText();
                    port = configPanel.getPortNumberText();
                    try {
                        startButton.setEnabled(false);
                        configPanel.setAllFieldsEnabled(false);
                        DBConnection.register(location, port);
                        running = true;
                    } catch (RemoteException rex) {
                        Application.handleException(
                                "Unable to start server", rex, frame);
                    } catch (IllegalArgumentException iex) {
                        Application.handleException(
                                "Illegal port number.\n"+iex.getMessage(), 
                                iex, frame);
                    } finally {
                        if (running) {
                            startButton.setToolTipText(SERVER_STARTED_TOOLTIP);
                            configPanel.save();
                        } else {
                            configPanel.setAllFieldsEnabled(true);
                            startButton.setEnabled(true);
                        }
                    }
                }
            }
        }

    }

}
