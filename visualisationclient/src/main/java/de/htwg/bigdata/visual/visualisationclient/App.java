package de.htwg.bigdata.visual.visualisationclient;

import javax.swing.SwingUtilities;

public class App {

	public static void main(String[] args) throws Exception {
	    SwingUtilities.invokeLater(new Runnable() {
	        public void run() {
	            try {
	                Gui.createAndShowGUI(new Controller());
	            }
	            catch (Exception e) {
	                System.err.println(e);
	                e.printStackTrace();
	            }
	        }
	    });
	}
	


}


