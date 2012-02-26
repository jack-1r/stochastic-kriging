package se.sics.kriging.application.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

public class DlgParamsList extends JDialog {
    /**
     * 
     */
    private static final long serialVersionUID = 4560534986939304725L;
    private JTable tblParam;
    private JButton btnOK;
    
    private double[] min;
    private double[] max;
    
    public double[] getMin() {
        return min;
    }
    
    public double[] getMax() {
        return max;
    }
    
    public boolean[] getIsInteger() {
        return isInteger;
    }
    
    private boolean[] isInteger;
    
    /**
     * Create the dialog.
     */
    public DlgParamsList(final int numberOfRow, final double[] lb,
            final double[] ub, final boolean[] check) {
        setModalityType(ModalityType.APPLICATION_MODAL);
        
        final DlgParamsList comp = this;
        min = new double[numberOfRow];
        max = new double[numberOfRow];
        isInteger = new boolean[numberOfRow];
        initComponent(numberOfRow);
        
        if (check != null) {
            for (int count = 0; count < numberOfRow; count++) {
                tblParam.setValueAt(new Double(lb[count]), count, 0);
                tblParam.setValueAt(new Double(ub[count]), count, 1);
                tblParam.setValueAt(new Boolean(check[count]), count, 2);
                min = lb;
                max = ub;
                isInteger = check;
            }
        } else {
            for (int count = 0; count < numberOfRow; count++)
                tblParam.setValueAt(new Boolean(false), count, 2);
        }
        
        btnOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                for (int count = 0; count < numberOfRow; count++) {
                    try {
                        min[count] = (Double) tblParam.getValueAt(count, 0);
                        max[count] = (Double) tblParam.getValueAt(count, 1);
                        isInteger[count] = (Boolean) tblParam.getValueAt(count,
                                2);
                    } catch (Exception e2) {
                        JDialog dlgError = new JDialog(comp, "Wrong input",
                                true) {
                            /**
							 * 
							 */
                            private static final long serialVersionUID = 1L;
                            {
                                setBounds(100, 100, 200, 100);
                                JLabel lb = new JLabel("WRONG INPUT!",
                                        SwingConstants.CENTER);
                                getContentPane().add(lb, BorderLayout.CENTER);
                            }
                            
                        };
                        // dlgError.pack();
                        dlgError.setVisible(true);
                        return;
                    }
                    
                }
                comp.setVisible(false);
            }
        });
        
    }
    
    private void initComponent(int numberOfRow) {
        btnOK = new JButton("OK");
        JScrollPane scrollPane = new JScrollPane();
        GroupLayout groupLayout = new GroupLayout(getContentPane());
        groupLayout
                .setHorizontalGroup(groupLayout
                        .createParallelGroup(Alignment.LEADING)
                        .addGroup(
                                groupLayout
                                        .createSequentialGroup()
                                        .addContainerGap()
                                        .addComponent(scrollPane,
                                                GroupLayout.PREFERRED_SIZE,
                                                342, GroupLayout.PREFERRED_SIZE)
                                        .addContainerGap(63, Short.MAX_VALUE))
                        .addGroup(
                                Alignment.TRAILING,
                                groupLayout
                                        .createSequentialGroup()
                                        .addContainerGap(251, Short.MAX_VALUE)
                                        .addComponent(btnOK,
                                                GroupLayout.PREFERRED_SIZE, 80,
                                                GroupLayout.PREFERRED_SIZE)
                                        .addGap(28)));
        groupLayout.setVerticalGroup(groupLayout.createParallelGroup(
                Alignment.LEADING).addGroup(
                groupLayout
                        .createSequentialGroup()
                        .addContainerGap()
                        .addComponent(scrollPane, GroupLayout.PREFERRED_SIZE,
                                213, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(ComponentPlacement.UNRELATED)
                        .addComponent(btnOK).addGap(10)));
        
        tblParam = new JTable();
        scrollPane.setViewportView(tblParam);
        tblParam.setModel(new DefaultTableModel(new Object[numberOfRow][3],
                new String[] { "lb", "ub", "isInt" }) {
            /**
							 * 
							 */
            private static final long serialVersionUID = 1L;
            Class[] columnTypes = new Class[] { Double.class, Double.class,
                    Boolean.class };
            
            public Class getColumnClass(int columnIndex) {
                return columnTypes[columnIndex];
            }
        });
        tblParam.getColumnModel().getColumn(0).setResizable(false);
        tblParam.getColumnModel().getColumn(0).setPreferredWidth(115);
        tblParam.getColumnModel().getColumn(1).setResizable(false);
        tblParam.getColumnModel().getColumn(1).setPreferredWidth(115);
        tblParam.getColumnModel().getColumn(2).setResizable(false);
        tblParam.getColumnModel().getColumn(2).setPreferredWidth(40);
        tblParam.getColumnModel().getColumn(2).setMinWidth(10);
        tblParam.getColumnModel().getColumn(2).setMaxWidth(40);
        tblParam.setFillsViewportHeight(true);
        getContentPane().setLayout(groupLayout);
    }
}
