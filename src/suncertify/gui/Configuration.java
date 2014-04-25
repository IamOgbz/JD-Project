package suncertify.gui;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class to control getting and setting of user configurations.
 *
 * @author Emmanuel
 */
public class Configuration {

    /**
     * Property that indicates database file path.
     */
    public static final String DATABASE_PATH = "database-path";

    /**
     * The valid database file extension
     */
    public static final String DATABASE_FILE_EXTENSION = "db";

    /**
     * Property that indicates database host address.
     */
    public static final String SERVER_ADDRESS = "server-address";

    /**
     * The default server address to use incase of none.
     */
    public static final String DEFAULT_HOST_ADDRESS = "localhost";

    /**
     * Property that indicates the server port.
     */
    public static final String SERVER_PORT = "server-port";

    /**
     * The default port to use for RMI registration in case of none.
     */
    public static final String RMI_REGISTRY_PORT
            = String.valueOf(java.rmi.registry.Registry.REGISTRY_PORT);

    /**
     * The location where our configuration file will be saved.
     */
    public static final String CONFIG_DIRECTORY = System.getProperty("user.dir");

    /**
     * The name of our properties file.
     */
    public static final String CONFIG_FILENAME = "suncertify.properties";

    /**
     * the Properties for this application.
     */
    private final Properties props;

    /**
     * The properties file object.
     */
    private static final File configFile
            = new File(CONFIG_DIRECTORY, CONFIG_FILENAME);

    /**
     * The Logger instance. All log messages from this class are routed through
     * this member. The Logger namespace is <code>suncertify.gui</code>.
     */
    private static final Logger log = Logger.getLogger("suncertify.gui");

    /**
     * Creates instance of this class with default values.
     */
    public Configuration() {
        props = loadProperties();
        if (props.isEmpty()) {
            props.setProperty(SERVER_ADDRESS, DEFAULT_HOST_ADDRESS);
            props.setProperty(SERVER_PORT, RMI_REGISTRY_PORT);
        }
    }

    /**
     * Get a specific property
     *
     * @param name the name of the property to return
     * @return the value of the property specified in name
     */
    public String getProperty(String name) {
        return props.getProperty(name);
    }

    /**
     * Set a configuration property of name to the specified value. Then save
     * the configuration.
     *
     * @param name the property name
     * @param value the property value
     */
    public void setProperty(String name, String value) {
        props.setProperty(name, value);
        saveProperties();
    }

    /**
     * Load the application properties from the configuration file.
     *
     * @return the loaded properties
     */
    private Properties loadProperties() {
        Properties loadedProps = new Properties();
        if (configFile.exists() && configFile.canRead()) {
            synchronized (configFile) {
                try (FileInputStream fis = new FileInputStream(configFile)) {
                    loadedProps.load(fis);
                    fis.close();
                } catch (FileNotFoundException ex) {
                    log.log(Level.SEVERE,
                            "File not found after existence verfied.", ex);
                } catch (IOException ex) {
                    log.log(Level.SEVERE, "File data could not be read.", ex);
                } finally {
                    if (loadedProps.isEmpty()) {
                        Application.handleException(
                                "No user configurations loaded.", null);
                    }
                }
            }
        }
        return loadedProps;
    }

    /**
     * Save the application properties in the configuration file.
     */
    private void saveProperties() {
        try (FileOutputStream fos = new FileOutputStream(configFile);) {
            if (configFile.canWrite()) {
                synchronized (configFile) {
                    if (configFile.exists()) {
                        configFile.delete();
                    }
                    configFile.createNewFile();
                    if (props.getProperty(SERVER_PORT).isEmpty()) {
                        props.setProperty(SERVER_PORT, RMI_REGISTRY_PORT);
                    }
                    if (props.getProperty(SERVER_ADDRESS).isEmpty()) {
                        props.setProperty(SERVER_ADDRESS, DEFAULT_HOST_ADDRESS);
                    }
                    props.store(fos, "URLyBird User Configuration");
                    fos.close();
                }
            } else {
                throw new IOException("Cannot write to config file");
            }
        } catch (IOException ex) {
            Application.handleException(
                    "Unable to save changes to configuration.", ex);
        }
    }

    /**
     * Provides automatic update of configuration save file on option change.
     */
    private class AutoSaveConfig implements FocusListener {

        private final ConfigPanel optPanel;

        /**
         * Create configuration options saver.
         *
         * @param optionPanel the option panel to retrieve values from
         */
        public AutoSaveConfig(ConfigPanel optionPanel) {
            this.optPanel = optionPanel;
        }

        @Override
        public void focusLost(FocusEvent fe) {
            switch (fe.getComponent().getName()) {
                case ConfigPanel.DB_LOCATION_LABEL:
                    String location = optPanel.getLocationFieldText().trim();
                    optPanel.setLocationFieldText(location);
                    switch (optPanel.getApplicationMode()) {
                        case CLIENT:
                            if (!location.equals(getProperty(SERVER_ADDRESS))) {
                                setProperty(SERVER_ADDRESS, location);
                            }
                            break;
                        case STANDALONE:
                        case SERVER:
                            if (!location.equals(getProperty(DATABASE_PATH))) {
                                setProperty(DATABASE_PATH, location);
                            }
                            break;
                    }
                    break;
                case ConfigPanel.SERVER_PORT_LABEL:
                    String portNumber = optPanel.getPortNumberText();
                    if (!portNumber.equals(getProperty(SERVER_PORT))) {
                        setProperty(SERVER_PORT, portNumber);
                    }
                    break;
            }
        }

        @Override
        public void focusGained(FocusEvent fe) {
            // do nothing
        }

    }

}
