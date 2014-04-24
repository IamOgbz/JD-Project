/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package suncertify.gui;

import java.util.logging.Logger;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 *
 * @author Emmanuel
 */
public class Application {

    public enum Mode {

        /**
         * the client mode
         */
        ClIENT,
        /**
         * the server mode
         */
        SERVER,
        /**
         * the local mode
         */
        STANDALONE;
    }
    /**
     * The Logger instance. All log messages from this class are routed through
     * this member. The Logger namespace is <code>suncertify.db</code>.
     */
    private static final Logger log = Logger.getLogger("suncertify.gui");

    public static void main(String[] args){
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
        // if args not valid log and display using system.err
        
    }

}
