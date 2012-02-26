package se.sics.kriging.application.gui;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JDialog;
import javax.swing.JFileChooser;

public class OpenFileDialog extends JDialog {

	/**
     * 
     */
    private static final long serialVersionUID = -8597543655488276176L;

    /**
	 * Create the dialog.
	 */
	public OpenFileDialog() {
		setTitle("Choose the JAR file");
		setBounds(100, 100, 666, 457);
		
		JFileChooser fileChooser = new JFileChooser();
		GroupLayout groupLayout = new GroupLayout(getContentPane());
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addComponent(fileChooser, GroupLayout.DEFAULT_SIZE, 640, Short.MAX_VALUE)
					.addContainerGap())
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addComponent(fileChooser, GroupLayout.DEFAULT_SIZE, 401, Short.MAX_VALUE)
					.addContainerGap())
		);
		getContentPane().setLayout(groupLayout);
	}
}
