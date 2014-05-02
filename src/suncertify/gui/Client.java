package suncertify.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
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

    // Pre defined contants to use in the interface.
    // Used to make changes to control text easier
    private static final int SEARCH_FIELDS_WIDTH = 30;
    private static final String HOTEL_SEARCH_LABEL = "    Hotel";
    private static final String HOTEL_SEARCH_TOOLTIP
            = "Name of hotel to search for";
    private static final String CITY_SEARCH_LABEL = "    City";
    private static final String CITY_SEARCH_TOOLTIP
            = "City location of hotel to search for";
    private static final String SEARCH_BUTTON_TEXT = "Search";
    private static final String SEARCH_BUTTON_TOOLTIP
            = "Enter name of hotel or city and click search";
    private static final String REFRESH_BUTTON_TEXT = "Show all";
    private static final String REFRESH_BUTTON_TOOLTIP
            = "Refresh table, and show all occupancies";
    private static final String BOOK_BUTTON_TEXT = "Book";
    private static final String UNBOOK_BUTTON_TEXT = "Unbook";
    private static final int TABLE_HEIGHT = 330;

    /**
     * The application mode that is related the client mode.
     */
    private final Application.Mode appMode;
    /**
     * The type of connection to be used to the database.
     */
    private final DBConnection.Type connType;
    /**
     * The panel that accepts the start up configuration information.
     */
    private final ConfigPanel configPanel;

    /**
     * The panel that contains the database table.
     */
    private final JPanel tablePanel;
    private OccupancyTable table;

    /**
     * The panel that contains the form action controls.
     */
    private final JPanel ctrlPanel;
    private final JButton bookButton;
    private final JButton unbookButton;

    /**
     * The panel that contains the form search controls.
     */
    private final JPanel searchPanel;
    private final JTextField hotelSearchField;
    private final JTextField citySearchField;
    private final JButton searchButton;
    private final JButton refreshButton;

    /**
     * The communicator from the user interface to the database.
     */
    private Controller controller;
    /**
     * The location of the database.
     */
    private String location;
    /**
     * The port used for the connection, in Client mode.
     */
    private String port;

    /**
     * The Logger instance. All log messages from this class are routed through
     * this member. The Logger namespace is <code>suncertify.gui</code>.
     */
    private static final Logger log = Logger.getLogger("suncertify.gui");

    /**
     * Instantiates the Client GUI and handles initial configurations. Takes
     * arguments which determine the kind of client to initialise.
     *
     * @param args no argument in the list starts up the network client, while
     * "alone" as the only argument starts the local client
     */
    public Client(String[] args) {
        super("URLyBird Client");
        this.setDefaultCloseOperation(Server.EXIT_ON_CLOSE);
        this.setResizable(false);

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

        // instantiate the search & action controls
        hotelSearchField = new JTextField(SEARCH_FIELDS_WIDTH);
        citySearchField = new JTextField(SEARCH_FIELDS_WIDTH);
        searchButton = new JButton(SEARCH_BUTTON_TEXT);
        refreshButton = new JButton(REFRESH_BUTTON_TEXT);
        refreshButton.setToolTipText(REFRESH_BUTTON_TOOLTIP);
        searchPanel = searchPanel();
        this.add(searchPanel, BorderLayout.NORTH);

        tablePanel = setupTable();
        this.add(tablePanel, BorderLayout.CENTER);

        bookButton = new JButton(BOOK_BUTTON_TEXT);
        unbookButton = new JButton(UNBOOK_BUTTON_TEXT);
        ctrlPanel = ctrlPanel();
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

    /**
     * Configures the search panel. Adding the search fields and buttons to it
     * and setting their actions.
     *
     * @return JPanel with the search fields and buttons already configured
     */
    private JPanel searchPanel() {
        JPanel panel = new JPanel();
        FlowLayout layout = new FlowLayout(FlowLayout.TRAILING);
        panel.setLayout(layout);

        JLabel hotelSearchLabel = new JLabel(HOTEL_SEARCH_LABEL);
        panel.add(hotelSearchLabel);
        hotelSearchField.setToolTipText(HOTEL_SEARCH_TOOLTIP);
        hotelSearchField.addKeyListener(new SearchHandler());
        panel.add(hotelSearchField);

        JLabel citySearchLabel = new JLabel(CITY_SEARCH_LABEL);
        panel.add(citySearchLabel);
        citySearchField.setToolTipText(CITY_SEARCH_TOOLTIP);
        citySearchField.addKeyListener(new SearchHandler());
        panel.add(citySearchField);

        searchButton.setEnabled(false);
        searchButton.setToolTipText(SEARCH_BUTTON_TOOLTIP);
        searchButton.addActionListener(new SearchHandler());
        panel.add(searchButton);

        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int rowIdx = table.getSelectedRow();
                hotelSearchField.setText("");
                citySearchField.setText("");
                searchButton.setEnabled(false);
                table.setData(controller.getTable().getData());
                // reselect previous index item
                if (rowIdx > 0 && rowIdx < table.getRowCount()) {
                    table.setRowSelectionInterval(rowIdx, rowIdx);
                }
            }
        });
        panel.add(refreshButton);

        return panel;
    }

    /**
     * Configures the control panel. Adding the book and un-book buttons to it
     * and setting their actions.
     *
     * @return JPanel with the search fields and buttons already configured
     */
    private JPanel ctrlPanel() {
        JPanel panel = new JPanel();
        FlowLayout layout = new FlowLayout(FlowLayout.RIGHT);
        panel.setLayout(layout);

        unbookButton.setEnabled(false);
        unbookButton.addActionListener(new BookingHandler());
        panel.add(bookButton);

        bookButton.setEnabled(false);
        bookButton.addActionListener(new BookingHandler());
        panel.add(unbookButton);

        return panel;
    }

    /**
     * Sets up the panel that hold the table for representing the occupancies.
     * The table is setup with column specifications gotten from the database
     *
     * @return JPanel containing the occupancies table
     */
    private JPanel setupTable() {
        JPanel panel = new JPanel();
        BorderLayout layout = new BorderLayout();
        panel.setLayout(layout);

        table = controller.getTable();
        ListSelectionModel rowSelectionModel = table.getSelectionModel();
        rowSelectionModel.addListSelectionListener(new BookingHandler());

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(0, TABLE_HEIGHT));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Class to handle searching of the table data.
     */
    private class SearchHandler implements ActionListener, KeyListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            int rowIdx = table.getSelectedRow();
            String hotel = hotelSearchField.getText();
            String city = citySearchField.getText();
            table.setData(controller.searchTable(hotel, city).getData());
            // reselect previous index item
            if (rowIdx > 0 && rowIdx < table.getRowCount()) {
                table.setRowSelectionInterval(rowIdx, rowIdx);
            }
        }

        @Override
        public void keyTyped(KeyEvent e) {
            // do nothing
        }

        @Override
        public void keyPressed(KeyEvent e) {
            // do nothing
        }

        @Override
        public void keyReleased(KeyEvent e) {
            boolean canSearch = hotelSearchField.getText().length() > 0
                    || citySearchField.getText().length() > 0;
            searchButton.setEnabled(canSearch);
        }

    }

    /**
     * Class to handle selection and booking of occupancy rows.
     */
    private class BookingHandler implements ActionListener, ListSelectionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            // if there is a selected row
            if (table.getSelectedRow() >= 0) {
                int rowIdx = table.getSelectedRow();
                Occupancy o = table.getItem(rowIdx);
                if (o.hasOwner()) {
                    controller.unbook(o);
                } else {
                    String id;
                    boolean notify = false;
                    // prepare dialog to get customer id
                    JPanel panel = new JPanel(new GridLayout(2, 1));
                    panel.add(new JLabel("Enter the customer's id"));
                    NumberField idField = new NumberField(8);
                    panel.add(idField);
                    JOptionPane pane = new JOptionPane(panel,
                            JOptionPane.QUESTION_MESSAGE);
                    JDialog dialog = pane.createDialog("Booking Occupancy");
                    do {
                        // notify the user only after the first failed attempt
                        if (notify) {
                            Application.handleException(
                                    "The customer id must be 8 digits long", null);
                        }
                        dialog.setVisible(true);
                        id = pane.getValue() == null
                                ? null : idField.getText();
                        notify = true;
                        log.info(id);
                    } while (id != null && id.length() != 8);
                    if (id != null) {
                        log.log(Level.INFO, "Book occupancy for customer: {0}", id);
                        controller.book(o, id);
                    }
                }
                String hotel = hotelSearchField.getText();
                String city = citySearchField.getText();
                if (hotel.length() == 0 && city.length() == 0) {
                    table.setData(controller.getTable().getData());
                } else {
                    table.setData(controller.searchTable(hotel, city).getData());
                }
                // reselect previous index item
                if (rowIdx > 0 && rowIdx < table.getRowCount()) {
                    table.setRowSelectionInterval(rowIdx, rowIdx);
                }
            }
        }

        @Override
        public void valueChanged(ListSelectionEvent e) {
            // only recognise final changes
            if (!e.getValueIsAdjusting()) {
                if (table.getSelectedRow() >= 0) {
                    int rowIdx = table.getSelectedRow();
                    Occupancy o = table.getItem(rowIdx);
                    bookButton.setEnabled(!o.hasOwner());
                    unbookButton.setEnabled(o.hasOwner());
                }
            }
        }

    }

}
