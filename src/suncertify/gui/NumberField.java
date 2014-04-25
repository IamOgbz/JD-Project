package suncertify.gui;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

/**
 * A custom text field that only accepts valid port numbers. Reduces the
 * validation needed on user input.
 *
 * @author Emmanuel
 */
public class NumberField extends JTextField {

    /**
     * The Logger instance. All log messages from this class are routed through
     * this member. The Logger namespace is <code>suncertify.gui</code>.
     */
    private static final Logger log = Logger.getLogger("suncertify.gui");

    /**
     * Creates a new number field with 5 columns. Default document only
     * accepts positive integers.
     */
    public NumberField() {
        super(5);
    }

    /**
     * Creates a text field of column specified that only accepts positive
     * integers.
     *
     * @param columns the number of columns for the text field
     */
    public NumberField(int columns) {
        super(columns);
    }

    /**
     * Creates the default implementation of the model to be used at
     * construction.
     *
     * @return <code>NumberDocument</code> a document which only allows positive
     * integers
     * @see NumberDocument
     */
    @Override
    protected Document createDefaultModel() {
        return new NumberDocument();
    }

    /**
     * Class used to verify that all strings inserted are positive integers.
     */
    private class NumberDocument extends PlainDocument {

        @Override
        public void insertString(int i, String s, AttributeSet as)
                throws BadLocationException {
            if (s != null) {
                try {
                    // get the positive integer
                    Integer si = Math.abs(Integer.parseInt(s));
                    if (si >= 0) {
                        // if string is a valid positive integer
                        // insert the string representation of the integer
                        super.insertString(i, si.toString(), as);
                    }
                } catch (NumberFormatException nfe) {
                    log.log(Level.WARNING,
                            "String \"{0}\" is not a valid number{1}", 
                            new Object[]{s, nfe.getMessage()});
                }
            }
        }
    }

}
