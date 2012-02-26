package se.sics.stochastic.executors.gui;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import se.sics.stochastic.executors.NetExecutorInstance;
import javax.swing.JLabel;
import javax.swing.LayoutStyle.ComponentPlacement;

public class NetExecutorListGUI extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTable tblExecList;
	private JLabel lblnCore;

	/**
	 * Create the frame.
	 */
	public NetExecutorListGUI() {
		setTitle("List of Registered Executors");
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setBounds(100, 100, 631, 345);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		JPanel panel = new JPanel();
		contentPane.add(panel, BorderLayout.SOUTH);

		JScrollPane scrollPane = new JScrollPane();

		JLabel lblTotalNumberOf = new JLabel("Total number of CPU cores: ");

		lblnCore = new JLabel("0");
		GroupLayout gl_panel = new GroupLayout(panel);
		gl_panel.setHorizontalGroup(
			gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panel.createParallelGroup(Alignment.TRAILING)
						.addGroup(gl_panel.createSequentialGroup()
							.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 595, Short.MAX_VALUE)
							.addContainerGap())
						.addGroup(gl_panel.createSequentialGroup()
							.addComponent(lblTotalNumberOf)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(lblnCore)
							.addGap(37))))
		);
		gl_panel.setVerticalGroup(
			gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createSequentialGroup()
					.addContainerGap()
					.addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 243, GroupLayout.PREFERRED_SIZE)
					.addGap(18)
					.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblnCore)
						.addComponent(lblTotalNumberOf))
					.addContainerGap(15, Short.MAX_VALUE))
		);

		tblExecList = new JTable();
		scrollPane.setViewportView(tblExecList);
		tblExecList.setModel(new DefaultTableModel(new Object[][] {},
				new String[] { "ID", "IP", "Port", "nCores", "Status",
						"isEnabled" }) {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;
			boolean[] columnEditables = new boolean[] { false, false, false,
					false, false, true };
			Class[] columnTypes = new Class[] { String.class, Object.class,
					String.class, String.class, String.class, Boolean.class };

			public Class getColumnClass(int columnIndex) {
				return columnTypes[columnIndex];
			}

			public boolean isCellEditable(int row, int column) {
				return columnEditables[column];
			}
		});
		tblExecList.getColumnModel().getColumn(0).setResizable(false);
		tblExecList.getColumnModel().getColumn(0).setPreferredWidth(120);
		tblExecList.getColumnModel().getColumn(0).setMinWidth(120);
		tblExecList.getColumnModel().getColumn(1).setResizable(false);
		tblExecList.getColumnModel().getColumn(1).setPreferredWidth(150);
		tblExecList.getColumnModel().getColumn(1).setMinWidth(150);
		tblExecList.getColumnModel().getColumn(2).setResizable(false);
		tblExecList.getColumnModel().getColumn(2).setMinWidth(75);
		tblExecList.getColumnModel().getColumn(3).setResizable(false);
		tblExecList.getColumnModel().getColumn(3).setMinWidth(75);
		tblExecList.getColumnModel().getColumn(4).setResizable(false);
		tblExecList.getColumnModel().getColumn(4).setMinWidth(75);
		tblExecList.setFillsViewportHeight(true);
		panel.setLayout(gl_panel);

	}

	public void addExec(int id, NetExecutorInstance exec) {
		DefaultTableModel model = (DefaultTableModel) tblExecList.getModel();
		Object[] rowData = { id, exec.getClientSK().getInetAddress(), exec.getClientSK().getPort(),
				exec.getnCores(), new String("OK"), new Boolean(true) };
		model.addRow(rowData);
		int nCore = Integer.parseInt(lblnCore.getText()) + exec.getnCores();
		lblnCore.setText(Integer.toString(nCore));
	}

	public boolean isEnabled(int id) {
		boolean isEnabled = false;
		DefaultTableModel model = (DefaultTableModel) tblExecList.getModel();
		for (int i = 0; i < model.getRowCount(); i++) {
			if (((Integer) model.getValueAt(i, 0)).equals(id))
				isEnabled = (Boolean) model.getValueAt(i, 5);
		}
		return isEnabled;
	}
	
	public void removeExec(int id) {
		DefaultTableModel model = (DefaultTableModel) tblExecList.getModel();
		for (int i = 0; i < model.getRowCount(); i++) {
			if (((Integer) model.getValueAt(i, 0)).equals(id))
				model.removeRow(i);
		}
	}
}
