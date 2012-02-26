package se.sics.kriging.application.gui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.sics.kriging.application.SessionDataBundle;
import se.sics.kriging.application.SimpleApplication;
import se.sics.kriging.components.RequestExpStart;
import se.sics.stochastic.executors.CloudExecutionManager;

public class SimpleKrigingFrame extends JFrame {

	Logger logger = LoggerFactory.getLogger(SimpleKrigingFrame.class);

	/**
	 * 
	 */
	private static final long serialVersionUID = -2530395788710514477L;
	private SimpleApplication app;
	private JPanel panelSetting;
	private JTextField tbxJar;
	private JTextField tbxConfLength;
	private JTextField tbxMseThreshold;
	private JTextField tbxMaxEval;
	private JTextArea txtAConsole;
	private JButton btnAddParam;
	private JComboBox cbxConfInt;
	private JTextField tbxInitSamples;
	private JButton btnStart;
	private JButton btnAbort;
	private JButton btnSKContinue;
	private JRadioButton rdbtnRunthrough;
	private JRadioButton rdbtnPrompt;
	private JRadioButton rdbtnIp;
	private JRadioButton rdbtnAs;
	private JRadioButton rdbtnTrr;
	private JRadioButton rdbtnFixedrun;
	private JButton btnSAContinue;
	private JButton btnGetResult;

	private DlgParamsList dlgParam = null;
	private JTextField tbxDimCount;
	private boolean isRunning = false;

	public SimpleKrigingFrame thisFrame;
	private JLabel lblNr;
	private JTextField tbxnRuns;
	private JTextField tbxFixedRun;

	public boolean isRunning() {
		return isRunning;
	}

	public SimpleKrigingFrame(SimpleApplication app) {
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("Simple Kriging Form");
		this.app = app;
		thisFrame = this;
		initComponent();
		loadSession();
		
		OutputStream out = new OutputStream() {
			@Override
			public void write(int b) throws IOException {
				txtAConsole.append((String.valueOf((char) b)));
			}

			@Override
			public void write(byte[] b, int off, int len) throws IOException {
				txtAConsole.append(new String(b, off, len));
			}

			@Override
			public void write(byte[] b) throws IOException {
				write(b, 0, b.length);
			}
		};

		System.setOut(new PrintStream(out, true));
		System.setErr(new PrintStream(out, true));

	}

	public void reset() {
		btnStart.setEnabled(true);
		btnAbort.setEnabled(false);
	}
	
	public void getResultEnable(boolean enable) {
		btnGetResult.setEnabled(enable);
	}

	public void continueSKEnable(boolean enable) {
		btnSKContinue.setEnabled(enable);
	}

	public void continueSAEnable(boolean enable) {
		btnSAContinue.setEnabled(enable);
	}

	// -------------------------------------------------------
	// return the full path name of the JAR file
	// -------------------------------------------------------
	public String getJar() {
		String jarFile = tbxJar.getText();
		if (jarFile.endsWith(".jar")) {
			String[] path = jarFile.split("/");
			return (path[path.length-1]);
		}
		System.out.println("Not a valid JAR file");
		return null;
	}
	
	public String getJarFullPath() {
	    String jarFile = tbxJar.getText();
	       if (jarFile.endsWith(".jar")) {
	           return jarFile;
	       }
	       return null;
	}

	// -------------------------------------------------------
	// return the number of initial sample points
	// -------------------------------------------------------
	public int getNrInitSamples() {
		int n = 0;
		try {
			n = Integer.parseInt(tbxInitSamples.getText());
		} catch (NullPointerException e) {
			System.out.println("Invalid confidence length value");
		}
		return n;
	}

	// -------------------------------------------------------
	// return the specified confidence interval value
	// 0 if CI = 95%
	// 1 if CI = 90%
	// -------------------------------------------------------
	public int getConfidenceInterval() {
		return (cbxConfInt.getSelectedIndex());
	}

	// -------------------------------------------------------
	// return the confidence length
	// -------------------------------------------------------
	public double getConfidenceLength() {
		double cl = 0;
		try {
			cl = Double.parseDouble(tbxConfLength.getText());
		} catch (NullPointerException e) {
			System.out.println("Invalid confidence length value");
		}
		return cl;
	}

	// --------------------------------------------------------
	// return the MSE threshold
	// --------------------------------------------------------
	public double getMSEthreshold() {
		double threshold = 0;
		try {
			threshold = Double.parseDouble(tbxMseThreshold.getText());
		} catch (NullPointerException e) {
			System.out.println("Invalid Mean-Squared-Error value");
		}
		return threshold;
	}

	// ---------------------------------------------------------
	// return the running-mode
	// - false: run-through
	// - true: prompt
	// ---------------------------------------------------------
	public boolean getRunMode() {
		boolean runMode = false;
		if (!rdbtnRunthrough.isSelected())
			runMode = true;
		return runMode;
	}
	
	public boolean isFixedRunMode() {
		boolean isFixed = false;
		if (rdbtnFixedrun.isSelected())
			isFixed = true;
		return isFixed;
	}
	
	public int getnRuns() {
		int nRuns = 0;
		try {
			nRuns = Integer.parseInt(tbxnRuns.getText());
		} catch (Exception e) {
			System.out.println("Invalid number of Runs");
		}
		return nRuns;
	}

	public int getMaxEval() {
		int maxEval = 0;
		try {
			maxEval = Integer.parseInt(tbxMaxEval.getText());
		} catch (Exception e) {
			System.out.println("Invalid Max-Evaluation value");
		}
		return maxEval;
	}

	public int getSKAlgor() {
		int algor = 0;
		if (rdbtnIp.isSelected())
			algor = 0;
		else if (rdbtnTrr.isSelected())
			algor = 1;
		else
			algor = 2;
		return algor;
	}

	public double[] getMin() {
		return dlgParam.getMin();
	}

	public double[] getMax() {
		return dlgParam.getMax();
	}

	public boolean[] getIntVector() {
		return dlgParam.getIsInteger();
	}

	public int getDimCount() {
		int dimCount = 0;
		try {
			dimCount = Integer.parseInt(tbxDimCount.getText());
		} catch (NumberFormatException e) {
			System.out.println("Invalid number of parameter value");
		}
		return dimCount;
	}
	
	public int getnFixedRun() {
		int nFixedRun = 0;
		try {
			nFixedRun = Integer.parseInt(tbxFixedRun.getText());
		} catch (Exception e) {
			System.out.println("Invalid number of fixed runs.");
		}
		return nFixedRun;
	}

	private class HandlerBtnListParam implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (dlgParam == null)
				dlgParam = new DlgParamsList(getDimCount(), null, null, null);
			if (getDimCount() == 0)
				System.out.println("Please specify the number of params");
			else {
				dlgParam.pack();
				dlgParam.setVisible(true);
			}
		}
	}

	private class HandlerBtnJARClick implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			JFileChooser fD = new JFileChooser();
			int returnVal = fD.showOpenDialog(SimpleKrigingFrame.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				String filePath = fD.getSelectedFile().getAbsolutePath()
						.toString();
				tbxJar.setText(filePath);
			}

		}
	}

	private class HandlerBtnStartClick implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			// TODO: implement parameter validation here.
			CloudExecutionManager.mCount = 0;

			System.out.println("-------------------------------");
			System.out.println("Experiment now start.");
			System.out.println("Jar file: " + tbxJar.getText());
			double[] min = dlgParam.getMin();
			double[] max = dlgParam.getMax();
			for (int c = 0; c < Integer.parseInt(tbxDimCount.getText()); c++) {
				System.out.println("Parameter " + Integer.toString(c) + ": ["
						+ Double.toString(min[c]) + ";"
						+ Double.toString(max[c]) + "]");
			}
			System.out.println("Confidence interval: "
					+ cbxConfInt.getSelectedItem().toString());
			System.out.println("Confidence length: " + tbxConfLength.getText());
			System.out.println("Mean-Squared-Error threshold: "
					+ tbxMseThreshold.getText());

			app.startExperimentfromGUI();
			btnStart.setEnabled(false);
			btnAbort.setEnabled(true);
			btnGetResult.setEnabled(false);
			isRunning = true;
			txtAConsole.setText(null);
			panelSetting.setEnabled(false);
		}
	}

	private class HandlerBtnAbortClick implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			// trigger the abortion of the current running experiment.
			isRunning = false;
//			JDialog dlgWait = new JDialog(thisFrame, "Trying to stop",
//					true) {
//				/**
//				 * 
//				 */
//				private static final long serialVersionUID = 1L;
//				{
//					setBounds(100, 100, 200, 100);
//					JLabel lb = new JLabel("Please wait...",
//							SwingConstants.CENTER);
//					getContentPane().add(lb, BorderLayout.CENTER);
//				}
//
//			};
//
//			while (app.isStopped() == false) {
//				dlgWait.setVisible(true);
//			}
//
//			dlgWait.setVisible(false);
//			dlgWait.dispose();
			app.abort();
			System.out.println("USER INTERRUPTION. ABORTED.");

			btnStart.setEnabled(true);
			panelSetting.setEnabled(true);
			btnAbort.setEnabled(false);
			btnSAContinue.setEnabled(false);
			btnSKContinue.setEnabled(false);
		}
	}

	private class HandlerAutoScroll implements AdjustmentListener {

		@Override
		public void adjustmentValueChanged(AdjustmentEvent e) {
			e.getAdjustable().setValue(e.getAdjustable().getMaximum());
		}

	}

	private class HandlerbtnGetResultClick implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent event) {
			app.getResult();
		}
	}
	
	//---------------------------------------------------
	// load all the previous sessions params into the GUI
	//----------------------------------------------------
	private void loadSession() {
	    logger.debug("Start loading session data...");
	    SessionDataBundle data = new SessionDataBundle(null, null, null, 0);
	    RequestExpStart request = new RequestExpStart();
	    try {
            ObjectInput is = new ObjectInputStream(new FileInputStream("session.data"));
            data = (SessionDataBundle) is.readObject();
            request = data.getData();
        } catch (FileNotFoundException e) {
            logger.error("Unable to find the session data for the previous session.");
            return;
        } catch (IOException e) {
            logger.error("Unable to read the session data file.");
            return;
        } catch (ClassNotFoundException e) {
            logger.error("Session data is corrupted. Failed to load.");
            return;
        }
	   
	    tbxConfLength.setText(Double.toString(request.getConfidenceLength()));
	    tbxDimCount.setText(data.getDimCount());
	    tbxFixedRun.setText(Integer.toString(request.getnFixedRun()));
	    tbxInitSamples.setText(Integer.toString(request.getNum()));
	    tbxJar.setText(request.getJarPath());
	    tbxMaxEval.setText(data.getMaxEval());
	    tbxMseThreshold.setText(Double.toString(request.getThreshold()));
	    tbxnRuns.setText(Integer.toString(request.getEffnCores()));
	    
	    dlgParam = new DlgParamsList(Integer.parseInt(data.getDimCount()), request.getLb(), request.getUb(), request.getIntVector());
	    
	}

	private void initComponent() {

		panelSetting = new JPanel();
		panelSetting.setBorder(new TitledBorder(null, "Settings",
				TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panelSetting.setForeground(Color.BLACK);

		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new TitledBorder(null, "Console",
				TitledBorder.LEADING, TitledBorder.TOP, null, null));

		btnStart = new JButton("Start");
		btnStart.addActionListener(new HandlerBtnStartClick());

		btnAbort = new JButton("Abort");
		btnAbort.addActionListener(new HandlerBtnAbortClick());
		btnAbort.setEnabled(false);

		btnSKContinue = new JButton("Run Stochastic Kriging");
		btnSKContinue.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnSKContinue.setEnabled(false);
				btnSAContinue.setEnabled(false);
				app.runSK();
			}
		});
		btnSKContinue.setEnabled(false);

		btnSAContinue = new JButton("Minimize Mean-Squared-Error");
		btnSAContinue.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnSKContinue.setEnabled(false);
				btnSAContinue.setEnabled(false);
				if (app.isSAStep())
					app.iterateSA();
				else
					app.runSA();
			}
		});
		btnSAContinue.setEnabled(false);

		JPanel panel_3 = new JPanel();
		panel_3.setBorder(new TitledBorder(new LineBorder(new Color(184, 207,
				229)), "Stochastic Kriging setting", TitledBorder.LEADING,
				TitledBorder.TOP, null, new Color(51, 51, 51)));

		rdbtnIp = new JRadioButton("interior-points");
		rdbtnIp.setSelected(true);

		rdbtnAs = new JRadioButton("active-set");

		rdbtnTrr = new JRadioButton("trust-reflective-region");

		GroupLayout gl_panel_3 = new GroupLayout(panel_3);
		gl_panel_3.setHorizontalGroup(gl_panel_3.createParallelGroup(
				Alignment.LEADING).addGroup(
				gl_panel_3
						.createSequentialGroup()
						.addGroup(
								gl_panel_3
										.createParallelGroup(Alignment.LEADING)
										.addComponent(rdbtnIp)
										.addComponent(rdbtnAs)
										.addComponent(rdbtnTrr))
						.addContainerGap(54, Short.MAX_VALUE)));
		gl_panel_3.setVerticalGroup(gl_panel_3.createParallelGroup(
				Alignment.LEADING).addGroup(
				gl_panel_3
						.createSequentialGroup()
						.addComponent(rdbtnIp)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(rdbtnAs)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(rdbtnTrr)
						.addContainerGap(GroupLayout.DEFAULT_SIZE,
								Short.MAX_VALUE)));
		panel_3.setLayout(gl_panel_3);

		JPanel panel_4 = new JPanel();
		panel_4.setBorder(new TitledBorder(new LineBorder(new Color(184, 207,
				229)), "SA conf", TitledBorder.LEADING, TitledBorder.TOP, null,
				new Color(51, 51, 51)));

		JLabel lblMaxeval = new JLabel("MaxEval");

		tbxMaxEval = new JTextField();
		tbxMaxEval.setText("20000");
		tbxMaxEval.setColumns(10);
		GroupLayout gl_panel_4 = new GroupLayout(panel_4);
		gl_panel_4.setHorizontalGroup(gl_panel_4.createParallelGroup(
				Alignment.LEADING).addGroup(
				gl_panel_4
						.createSequentialGroup()
						.addContainerGap()
						.addGroup(
								gl_panel_4
										.createParallelGroup(Alignment.LEADING)
										.addComponent(tbxMaxEval,
												GroupLayout.PREFERRED_SIZE,
												GroupLayout.DEFAULT_SIZE,
												GroupLayout.PREFERRED_SIZE)
										.addComponent(lblMaxeval))
						.addContainerGap(58, Short.MAX_VALUE)));
		gl_panel_4.setVerticalGroup(gl_panel_4.createParallelGroup(
				Alignment.LEADING).addGroup(
				gl_panel_4
						.createSequentialGroup()
						.addGap(6)
						.addComponent(lblMaxeval)
						.addPreferredGap(ComponentPlacement.UNRELATED)
						.addComponent(tbxMaxEval, GroupLayout.PREFERRED_SIZE,
								GroupLayout.DEFAULT_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addContainerGap(22, Short.MAX_VALUE)));
		panel_4.setLayout(gl_panel_4);

		btnGetResult = new JButton("Get Optimal Point and Graph");
		btnGetResult.setEnabled(false);
		btnGetResult.addActionListener(new HandlerbtnGetResultClick());

		GroupLayout groupLayout = new GroupLayout(getContentPane());
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(24)
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addGroup(groupLayout.createSequentialGroup()
									.addComponent(panel_3, GroupLayout.PREFERRED_SIZE, 348, GroupLayout.PREFERRED_SIZE)
									.addPreferredGap(ComponentPlacement.RELATED, 69, Short.MAX_VALUE)
									.addComponent(panel_4, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
									.addGap(18))
								.addGroup(groupLayout.createSequentialGroup()
									.addComponent(panelSetting, GroupLayout.PREFERRED_SIZE, 611, GroupLayout.PREFERRED_SIZE)
									.addPreferredGap(ComponentPlacement.RELATED)))
							.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
								.addComponent(btnStart, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(btnSKContinue, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(btnSAContinue, Alignment.LEADING)
								.addComponent(btnGetResult, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 244, Short.MAX_VALUE)
								.addGroup(groupLayout.createSequentialGroup()
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(btnAbort, GroupLayout.DEFAULT_SIZE, 244, Short.MAX_VALUE))))
						.addGroup(groupLayout.createSequentialGroup()
							.addContainerGap()
							.addComponent(panel_1, GroupLayout.DEFAULT_SIZE, 885, Short.MAX_VALUE)))
					.addGap(19))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.TRAILING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(22)
							.addComponent(btnStart)
							.addGap(18)
							.addComponent(btnSKContinue)
							.addGap(18)
							.addComponent(btnSAContinue)
							.addGap(18)
							.addComponent(btnGetResult)
							.addGap(18)
							.addComponent(btnAbort))
						.addGroup(groupLayout.createSequentialGroup()
							.addContainerGap()
							.addComponent(panelSetting, GroupLayout.PREFERRED_SIZE, 283, GroupLayout.PREFERRED_SIZE)))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(panel_4, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(panel_3, GroupLayout.PREFERRED_SIZE, 99, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(panel_1, GroupLayout.PREFERRED_SIZE, 238, GroupLayout.PREFERRED_SIZE)
					.addContainerGap())
		);

		final JScrollPane scrollPane = new JScrollPane();
		GroupLayout gl_panel_1 = new GroupLayout(panel_1);
		gl_panel_1.setHorizontalGroup(gl_panel_1.createParallelGroup(
				Alignment.LEADING).addGroup(
				gl_panel_1
						.createSequentialGroup()
						.addContainerGap()
						.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE,
								705, Short.MAX_VALUE).addContainerGap()));
		gl_panel_1.setVerticalGroup(gl_panel_1.createParallelGroup(
				Alignment.LEADING).addGroup(
				gl_panel_1
						.createSequentialGroup()
						.addComponent(scrollPane, GroupLayout.PREFERRED_SIZE,
								207, GroupLayout.PREFERRED_SIZE)
						.addContainerGap(39, Short.MAX_VALUE)));

		txtAConsole = new JTextArea();
		txtAConsole.setLineWrap(true);
		final HandlerAutoScroll l = new HandlerAutoScroll();
		txtAConsole.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				scrollPane.getVerticalScrollBar().removeAdjustmentListener(l);
			}

			@Override
			public void focusLost(FocusEvent e) {
				scrollPane.getVerticalScrollBar().addAdjustmentListener(l);
			}
		});
		txtAConsole.setEditable(false);
		scrollPane.setViewportView(txtAConsole);

		txtAConsole.setBackground(Color.BLACK);
		txtAConsole.setForeground(Color.GREEN);
		panel_1.setLayout(gl_panel_1);

		JButton btnPickJAR = new JButton("Pick JAR");
		btnPickJAR.addActionListener(new HandlerBtnJARClick());

		tbxJar = new JTextField();
		tbxJar.setEditable(false);
		tbxJar.setColumns(10);

		btnAddParam = new JButton("Show param list");
		btnAddParam.addActionListener(new HandlerBtnListParam());

		JPanel panel_2 = new JPanel();
		panel_2.setBorder(new TitledBorder(null, "Run mode",
				TitledBorder.LEADING, TitledBorder.TOP, null, null));

		JLabel lblNewLabel_1 = new JLabel("Initial samples");

		tbxInitSamples = new JTextField();
		tbxInitSamples.setText("10");
		tbxInitSamples.setColumns(10);

		JLabel lblNumberOfParams = new JLabel("Number of Params");

		tbxDimCount = new JTextField();
		tbxDimCount.setText("2");
		tbxDimCount.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				dlgParam = null;
			}
		});
		tbxDimCount.setColumns(10);
		
		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(null, "Confidence mode", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		
				JLabel lblConfint = new JLabel("Confidence Interval");
				
						cbxConfInt = new JComboBox();
						cbxConfInt.setModel(new DefaultComboBoxModel(new String[] { "95%",
								"90%" }));
						cbxConfInt.setSelectedIndex(0);
										
												JLabel lblConflen = new JLabel("Confidence Length");
												
														tbxConfLength = new JTextField();
														tbxConfLength.setText("0.3");
														tbxConfLength.setColumns(10);
														GroupLayout gl_panel = new GroupLayout(panel);
														gl_panel.setHorizontalGroup(
															gl_panel.createParallelGroup(Alignment.LEADING)
																.addGroup(gl_panel.createSequentialGroup()
																	.addContainerGap()
																	.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
																		.addGroup(gl_panel.createSequentialGroup()
																			.addComponent(lblConfint)
																			.addPreferredGap(ComponentPlacement.UNRELATED)
																			.addComponent(cbxConfInt, 0, 67, Short.MAX_VALUE))
																		.addGroup(gl_panel.createSequentialGroup()
																			.addComponent(lblConflen)
																			.addGap(22)
																			.addComponent(tbxConfLength, GroupLayout.DEFAULT_SIZE, 67, Short.MAX_VALUE)))
																	.addContainerGap())
														);
														gl_panel.setVerticalGroup(
															gl_panel.createParallelGroup(Alignment.LEADING)
																.addGroup(gl_panel.createSequentialGroup()
																	.addContainerGap()
																	.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
																		.addComponent(lblConfint)
																		.addComponent(cbxConfInt, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
																	.addPreferredGap(ComponentPlacement.UNRELATED)
																	.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
																		.addComponent(lblConflen)
																		.addComponent(tbxConfLength, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
																	.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
														);
														panel.setLayout(gl_panel);
		
		rdbtnFixedrun = new JRadioButton("Fixed # runs");
		rdbtnFixedrun.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (rdbtnFixedrun.isSelected()) {
					tbxFixedRun.setEnabled(true);
				} else
					tbxFixedRun.setEnabled(false);
			}
		});
		
				JLabel lblMseThreshold = new JLabel("MSE threshold");
		
				tbxMseThreshold = new JTextField();
				tbxMseThreshold.setText("10");
				tbxMseThreshold.setColumns(10);
		
		tbxFixedRun = new JTextField();
		tbxFixedRun.setEnabled(false);
		tbxFixedRun.setText("10");
		tbxFixedRun.setColumns(10);
		
		lblNr = new JLabel("Effective nCores");
		
		tbxnRuns = new JTextField();
		tbxnRuns.setText("12");
		tbxnRuns.setColumns(10);
		GroupLayout gl_panelSetting = new GroupLayout(panelSetting);
		gl_panelSetting.setHorizontalGroup(
			gl_panelSetting.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelSetting.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panelSetting.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_panelSetting.createSequentialGroup()
							.addComponent(btnPickJAR)
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addComponent(tbxJar, GroupLayout.DEFAULT_SIZE, 477, Short.MAX_VALUE))
						.addGroup(gl_panelSetting.createSequentialGroup()
							.addGroup(gl_panelSetting.createParallelGroup(Alignment.LEADING)
								.addComponent(btnAddParam, GroupLayout.DEFAULT_SIZE, 278, Short.MAX_VALUE)
								.addGroup(gl_panelSetting.createSequentialGroup()
									.addComponent(lblNumberOfParams)
									.addGap(28)
									.addComponent(tbxDimCount, GroupLayout.PREFERRED_SIZE, 58, GroupLayout.PREFERRED_SIZE))
								.addGroup(gl_panelSetting.createSequentialGroup()
									.addComponent(lblNewLabel_1)
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(tbxInitSamples, GroupLayout.DEFAULT_SIZE, 163, Short.MAX_VALUE)
									.addPreferredGap(ComponentPlacement.RELATED))
								.addGroup(gl_panelSetting.createSequentialGroup()
									.addComponent(lblMseThreshold)
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(tbxMseThreshold, GroupLayout.DEFAULT_SIZE, 164, Short.MAX_VALUE)
									.addPreferredGap(ComponentPlacement.RELATED))
								.addGroup(gl_panelSetting.createSequentialGroup()
									.addComponent(panel_2, GroupLayout.DEFAULT_SIZE, 278, Short.MAX_VALUE)
									.addPreferredGap(ComponentPlacement.RELATED)))
							.addGap(43)
							.addGroup(gl_panelSetting.createParallelGroup(Alignment.LEADING)
								.addGroup(gl_panelSetting.createSequentialGroup()
									.addComponent(rdbtnFixedrun)
									.addPreferredGap(ComponentPlacement.UNRELATED)
									.addComponent(tbxFixedRun, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
								.addComponent(panel, GroupLayout.PREFERRED_SIZE, 256, GroupLayout.PREFERRED_SIZE)
								.addGroup(Alignment.TRAILING, gl_panelSetting.createSequentialGroup()
									.addComponent(lblNr)
									.addPreferredGap(ComponentPlacement.UNRELATED)
									.addComponent(tbxnRuns, GroupLayout.PREFERRED_SIZE, 89, GroupLayout.PREFERRED_SIZE)
									.addGap(18)))))
					.addContainerGap())
		);
		gl_panelSetting.setVerticalGroup(
			gl_panelSetting.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelSetting.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panelSetting.createParallelGroup(Alignment.BASELINE)
						.addComponent(btnPickJAR)
						.addComponent(tbxJar, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))
					.addGap(15)
					.addGroup(gl_panelSetting.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblNumberOfParams)
						.addComponent(tbxDimCount, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(rdbtnFixedrun)
						.addComponent(tbxFixedRun, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(gl_panelSetting.createParallelGroup(Alignment.BASELINE)
						.addGroup(gl_panelSetting.createSequentialGroup()
							.addComponent(btnAddParam)
							.addGap(19)
							.addGroup(gl_panelSetting.createParallelGroup(Alignment.BASELINE)
								.addComponent(lblNewLabel_1, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE)
								.addComponent(tbxInitSamples, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
							.addPreferredGap(ComponentPlacement.RELATED, 7, Short.MAX_VALUE)
							.addGroup(gl_panelSetting.createParallelGroup(Alignment.BASELINE)
								.addComponent(lblMseThreshold)
								.addComponent(tbxMseThreshold, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
							.addGap(4))
						.addComponent(panel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addGroup(gl_panelSetting.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_panelSetting.createSequentialGroup()
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(panel_2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addGroup(gl_panelSetting.createSequentialGroup()
							.addGap(26)
							.addGroup(gl_panelSetting.createParallelGroup(Alignment.BASELINE)
								.addComponent(tbxnRuns, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(lblNr))))
					.addGap(17))
		);

		ButtonGroup skAlgor = new ButtonGroup();
		skAlgor.add(rdbtnIp);
		skAlgor.add(rdbtnAs);
		skAlgor.add(rdbtnTrr);

		rdbtnRunthrough = new JRadioButton("Run-through");
		rdbtnRunthrough.setSelected(true);
		rdbtnPrompt = new JRadioButton("Prompt");

		ButtonGroup runMode = new ButtonGroup();
		runMode.add(rdbtnRunthrough);
		runMode.add(rdbtnPrompt);

		GroupLayout gl_panel_2 = new GroupLayout(panel_2);
		gl_panel_2.setHorizontalGroup(
			gl_panel_2.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_2.createSequentialGroup()
					.addComponent(rdbtnRunthrough)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(rdbtnPrompt)
					.addContainerGap(28, Short.MAX_VALUE))
		);
		gl_panel_2.setVerticalGroup(
			gl_panel_2.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_2.createSequentialGroup()
					.addGroup(gl_panel_2.createParallelGroup(Alignment.BASELINE)
						.addComponent(rdbtnRunthrough)
						.addComponent(rdbtnPrompt))
					.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
		);
		panel_2.setLayout(gl_panel_2);
		panelSetting.setLayout(gl_panelSetting);
		getContentPane().setLayout(groupLayout);
	}
}
