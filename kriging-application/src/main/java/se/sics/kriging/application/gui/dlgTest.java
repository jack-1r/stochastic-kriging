package se.sics.kriging.application.gui;

import java.awt.EventQueue;

import javax.swing.JDialog;
import javax.swing.JLabel;
import java.awt.BorderLayout;
import javax.swing.SwingConstants;

public class dlgTest extends JDialog {

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					dlgTest dialog = new dlgTest();
					dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
					dialog.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the dialog.
	 */
	public dlgTest() {
		setBounds(100, 100, 1000, 1000);
		
		JLabel lblWrongInput = new JLabel("Wrong Input!");
		lblWrongInput.setHorizontalAlignment(SwingConstants.CENTER);
		getContentPane().add(lblWrongInput, BorderLayout.CENTER);

	}

}
