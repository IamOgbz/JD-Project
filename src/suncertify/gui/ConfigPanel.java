package suncertify.gui;

import java.awt.Component;
import java.awt.DefaultKeyboardFocusManager;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.filechooser.FileFilter;
import static suncertify.gui.Configuration.DATABASE_PATH;
import static suncertify.gui.Configuration.SERVER_ADDRESS;
import static suncertify.gui.Configuration.SERVER_PORT;

/**
 * The panel used to specify configuration options. Using the same panel
 * provides a homogenous GUI look and feel.
 *
 * @author Emmanuel
 */
public class ConfigPanel extends JPanel {

    // Pre defined string to use in the panel.
    // Used to make changes to control text easier.
    static final String DB_LOCATION_LABEL = "Database location: ";
    static final String SERVER_PORT_LABEL = "Server port: ";
    static final String DB_HD_LOCATION_TOOLTIP
            = "The location of the database on an accessible hard drive";
    static final String DB_IP_LOCATION_TOOLTIP
            = "The server where the database is located (IP address)";
    static final String SERVER_PORT_TOOLTIP
            = "The port number the Server uses to listens for requests";

    // application configuration instance
    private static final Configuration config = new Configuration();

    /**
     * The application mode the panel was created for.
     */
    private final Application.Mode appMode;

    /**
     * The location field for the database.
     */
    private final JTextField locationField;

    /**
     * Button to choose database file. Disabled for client mode, since client
     * provides database host.
     */
    private final JButton browseButton;

    /**
     * Port number the database server uses.
     */
    private final JTextField portNumber;

    /**
     * The Logger instance. All log messages from this class are routed through
     * this member. The Logger namespace is <code>suncertify.gui</code>.
     */
    private static final Logger log = Logger.getLogger("suncertify.gui");

    /**
     * Creates a new instance of OptionPanel for configuring database
     * connection.
     *
     * @param applicationMode one of Application.Mode.
     * @see Application.Mode
     */
    public ConfigPanel(Application.Mode applicationMode) {
        this.appMode = applicationMode;
        this.portNumber = new NumberField(5);
        this.locationField = new JTextField(40);
        this.browseButton = new JButton("Browse");

        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints contraints = new GridBagConstraints();
        this.setLayout(layout);

        // separate components from each other
        contraints.insets = new Insets(4, 4, 4, 4);

        // location row
        JLabel dbLocationLabel = new JLabel(DB_LOCATION_LABEL);
        layout.setConstraints(dbLocationLabel, contraints);
        this.add(dbLocationLabel);

        //locationField.addFocusListener(new AutoSaveConfig());
        if (applicationMode == Application.Mode.CLIENT) {
            locationField.setText(config.getProperty(SERVER_ADDRESS));
            locationField.setToolTipText(DB_IP_LOCATION_TOOLTIP);
            contraints.gridwidth = GridBagConstraints.REMAINDER;
        } else {
            locationField.setText(config.getProperty(DATABASE_PATH));
            locationField.setToolTipText(DB_HD_LOCATION_TOOLTIP);
            contraints.gridwidth = GridBagConstraints.RELATIVE;
        }
        locationField.setName(DB_LOCATION_LABEL);
        layout.setConstraints(locationField, contraints);
        this.add(locationField);

        if ((applicationMode == Application.Mode.SERVER)
                || (applicationMode == Application.Mode.STANDALONE)) {
            // to browse for database file location
            browseButton.addActionListener(new ChooseDatabaseFile());
            contraints.gridwidth = GridBagConstraints.REMAINDER;
            layout.setConstraints(browseButton, contraints);
            this.add(browseButton);
        }

        if ((applicationMode == Application.Mode.SERVER)
                || (applicationMode == Application.Mode.CLIENT)) {
            // server port row
            JLabel serverPortLabel = new JLabel(SERVER_PORT_LABEL);
            contraints.gridwidth = 1;
            contraints.anchor = GridBagConstraints.EAST;
            layout.setConstraints(serverPortLabel, contraints);
            this.add(serverPortLabel);

            // portNumber.addFocusListener(new AutoSaveConfig(this));
            portNumber.setText(config.getProperty(SERVER_PORT));
            portNumber.setToolTipText(SERVER_PORT_TOOLTIP);
            portNumber.setName(SERVER_PORT_LABEL);
            // set to end of row
            contraints.gridwidth = GridBagConstraints.REMAINDER;
            contraints.anchor = GridBagConstraints.WEST;
            layout.setConstraints(portNumber, contraints);
            this.add(portNumber);
        } else {
            // port number is not for use
            portNumber.setText("-1");
        }
    }

    /**
     * Returns the mode the application will be running in.
     *
     * @return application mode.
     * @see Application.Mode
     */
    public Application.Mode getApplicationMode() {
        return this.appMode;
    }

    /**
     * Utility method for disabling and enabling all configuration fields.
     *
     * @param enabled true to enable all the fields
     */
    public void setAllFieldsEnabled(boolean enabled) {
        setBrowseButtonEnabled(enabled);
        setLocationFieldEnabled(enabled);
        setPortNumberEnabled(enabled);
    }

    /**
     * Returns the value of the database location field.
     *
     * @return database location field value.
     */
    public String getLocationFieldText() {
        return locationField.getText();
    }

    /**
     * Sets the contents of the database location field.
     *
     * @param locationField the database location field value.
     */
    public void setLocationFieldText(String locationField) {
        this.locationField.setText(locationField);
    }

    /**
     * Configures whether the location field is enabled or not.
     *
     * @param enabled true if the location field is enabled.
     */
    public void setLocationFieldEnabled(boolean enabled) {
        this.locationField.setEnabled(enabled);
    }

    /**
     * Configures whether the browse button is enabled or not.
     *
     * @param enabled true if the browse button is enabled.
     */
    public void setBrowseButtonEnabled(boolean enabled) {
        this.browseButton.setEnabled(enabled);
    }

    /**
     * Returns the contents of the port number text field.
     *
     * @return the contents of the port number text field.
     */
    public String getPortNumberText() {
        return portNumber.getText();
    }

    /**
     * Sets the contents of the port number text field.
     *
     * @param portNumber the contents of the port number text field.
     */
    public void setPortNumberText(String portNumber) {
        this.portNumber.setText(portNumber);
    }

    /**
     * Configures whether the port number field is enabled or not.
     *
     * @param enabled true if the port number field is enabled.
     */
    public void setPortNumberEnabled(boolean enabled) {
        this.portNumber.setEnabled(enabled);
    }

    /**
     * Save the current configurations.
     */
    public void save() {
        switch (appMode) {
            case CLIENT:
                config.setProperty(SERVER_ADDRESS, locationField.getText());
                config.setProperty(SERVER_PORT, portNumber.getText());
                break;
            case SERVER:
                config.setProperty(SERVER_PORT, portNumber.getText());
            case STANDALONE:
                config.setProperty(DATABASE_PATH, locationField.getText());
                break;
        }
    }

    /**
     * The provides the utility to pick a database file from the system drive.
     */
    private class ChooseDatabaseFile implements ActionListener {

        /**
         * Description for the accepted database file formats.
         */
        private static final String DATABASE_FILE_DESCRIPTION
                = "Database files (*." + Configuration.DATABASE_FILE_EXTENSION + ")";

        @Override
        public void actionPerformed(ActionEvent ae) {
            JFileChooser chooser = new JFileChooser(Configuration.CONFIG_DIRECTORY);
            chooser.addChoosableFileFilter(
                    new FileFilter() {
                        /**
                         * Display files ending in ".db" or any other object
                         * (directory or other selectable device).
                         */
                        @Override
                        public boolean accept(File f) {
                            if (f.isFile()) {
                                return f.getName()
                                .endsWith(Configuration.DATABASE_FILE_EXTENSION);
                            } else {
                                return true;
                            }
                        }

                        /**
                         * Provide a description for the types of files we are
                         * allowing to be selected.
                         */
                        @Override
                        public String getDescription() {
                            return DATABASE_FILE_DESCRIPTION;
                        }
                    }
            );

            // if the user selected a file, update the location on screen
            if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(null)) {
                String dbLocation = chooser.getSelectedFile().toString();
                setLocationFieldText(dbLocation);
                locationField.requestFocus();
            }
        }

    }

    /**
     * Provides automatic update of configuration save file on option change.
     */
    private class AutoSaveConfig implements FocusListener {

        @Override
        public void focusLost(FocusEvent fe) {
            log.log(Level.INFO, "Focus lost -> {0}",
                    fe.getComponent().getName());
            handleComponent(fe.getComponent());
        }

        @Override
        public void focusGained(FocusEvent fe) {
            log.log(Level.INFO, "Focus gained -> {0}",
                    fe.getComponent().getName());
            handleComponent(fe.getComponent());
        }

        /**
         * Handle the saving of the configuration panel field.
         *
         * @param comp the comp field that invoked the handler
         */
        private void handleComponent(Component comp) {
            switch (comp.getName()) {
                case ConfigPanel.DB_LOCATION_LABEL:
                    String location = getLocationFieldText().trim();
                    setLocationFieldText(location);
                    switch (getApplicationMode()) {
                        case CLIENT:
                            if (!location.equals(
                                    config.getProperty(SERVER_ADDRESS))) {
                                config.setProperty(SERVER_ADDRESS, location);
                            }
                            break;
                        case STANDALONE:
                        case SERVER:
                            if (!location.equals(
                                    config.getProperty(DATABASE_PATH))) {
                                config.setProperty(DATABASE_PATH, location);
                            }
                            break;
                    }
                    break;
                case ConfigPanel.SERVER_PORT_LABEL:
                    String portNumber = getPortNumberText();
                    if (!portNumber.equals(config.getProperty(SERVER_PORT))) {
                        config.setProperty(SERVER_PORT, portNumber);
                    }
                    break;
            }
        }

    }

}
