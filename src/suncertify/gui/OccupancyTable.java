package suncertify.gui;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Custom table to handle displaying of occupancy records.
 *
 * @author Emmanuel
 */
public class OccupancyTable extends JTable {

    /**
     * Predefined column heads for the occupancies table.
     */
    private static final String[] COLUMNS = new String[]{"", "Hotel", "City",
        "Size", "Smoking", "Rate", "Date", "Owner"};
    private static final int ROW_HEIGHT = 20;

    /**
     * The table data.
     */
    private List<Occupancy> data;

    /**
     * Creates instance of this class with default values.
     */
    public OccupancyTable() {
        super(new DefaultTableModel(null, COLUMNS));
        this.data = new ArrayList<>();
        configure();
    }

    /**
     * Creates instance of this class with occupancies added.
     *
     * @param os the collection of occupancies to add to the table
     */
    public OccupancyTable(Collection<Occupancy> os) {
        super(new DefaultTableModel(null, COLUMNS));
        this.data = new ArrayList<>();
        configure();
        setData(os);
    }

    /**
     * Replaces the list of occupancies displayed by the table and reselects
     * previous row selected.
     *
     * @param os the new collection of occupancies
     */
    synchronized public final void setData(Collection<Occupancy> os) {
        Occupancy p = null;
        int i = -1;
        if (getSelectedRow() >= 0) {
            p = data.get(getSelectedRow());
        }
        clearData();
        data = (List) os;
        for (Occupancy o : data) {
            // uses the record address as the uid
            if (p != null && o.getAddress() == p.getAddress()) {
                i = data.indexOf(o);
            }
            addOccupancy(o);
        }
        // reselect previous index item if there was one and it still exists
        if (i >= 0 && i < getRowCount()) {
            setRowSelectionInterval(i, i);
        }
    }

    /**
     * Sets the default properties of the table.
     */
    private void configure() {
        tableHeader.setResizingAllowed(false);
        tableHeader.setReorderingAllowed(false);

        // center the column headers and the row number
        DefaultTableCellRenderer headerRndrr
                = (DefaultTableCellRenderer) tableHeader.getDefaultRenderer();
        headerRndrr.setHorizontalAlignment(JLabel.CENTER);
        columnModel.getColumn(0).setCellRenderer(headerRndrr);

        DefaultTableCellRenderer cellRndrr = new DefaultTableCellRenderer();
        cellRndrr.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 1; i < columnModel.getColumnCount(); i++) {
            columnModel.getColumn(i).setCellRenderer(cellRndrr);
        }

        columnModel.getColumn(0).setPreferredWidth(25);
        columnModel.getColumn(1).setPreferredWidth(200);
        columnModel.getColumn(2).setPreferredWidth(120);
        columnModel.getColumn(3).setPreferredWidth(30);
        columnModel.getColumn(4).setPreferredWidth(55);
        columnModel.getColumn(5).setPreferredWidth(60);
        columnModel.getColumn(6).setPreferredWidth(100);
        columnModel.getColumn(7).setPreferredWidth(100);

        setRowHeight(ROW_HEIGHT);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        setToolTipText("Select an occupancy record to book or unbook customer");
    }

    /**
     * Remove all rows from the table.
     */
    public void clearData() {
        DefaultTableModel model = (DefaultTableModel) getModel();
        while (model.getRowCount() > 0) {
            model.removeRow(0);
        }
        data.clear();
    }

    /**
     * Utility method to add an occupancy to the table.
     *
     * @param o the occupancy being added
     */
    private void addOccupancy(Occupancy o) {
        DefaultTableModel model = (DefaultTableModel) getModel();
        model.addRow(new Object[]{getRowCount() + 1, o.getName(),
            o.getLocation(), o.getSize(), o.isSmoking() ? "Yes" : "No",
            o.getRate(), o.getFormattedDate("EEE, d MMM yyyy"), o.getOwner()});
    }

    /**
     * Get the data displayed in the table.
     *
     * @return List copy of the table data
     */
    public List<Occupancy> getData() {
        return new ArrayList<>(data);
    }

    /**
     * Returns the occupancy object at that position in the table.
     *
     * @param index the index of the occupancy in the table.
     * @return Occupancy object at the table index position.
     */
    public Occupancy getItem(int index) {
        return data.get(index);
    }

    /**
     * Prevent cells from being editable.
     *
     * @param row the table row
     * @param column the table column
     * @return false for all positions to stop cell edition
     */
    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }

}
