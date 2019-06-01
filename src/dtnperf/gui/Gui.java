package dtnperf.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.temporal.ChronoUnit;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

import dtnperf.client.Client;
import dtnperf.client.ClientCongestionControl;
import dtnperf.client.ClientCongestionControlRate;
import dtnperf.client.ClientCongestionControlWindow;
import dtnperf.client.DataMode;
import dtnperf.client.DataUnit;
import dtnperf.client.Mode;
import dtnperf.client.RateUnit;
import dtnperf.client.TimeMode;
import dtnperf.event.BundleReceivedListener;
import dtnperf.event.BundleSentListener;
import dtnperf.server.Server;
import it.unibo.dtn.JAL.Bundle;
import it.unibo.dtn.JAL.BundleEID;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.JScrollPane;
import java.awt.Font;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.ScrollPaneConstants;

public class Gui {

	private ServerGuiController serverGuiController = new ServerGuiController();
	private ClientStatusUpdater statusUpdater;

	private JFrame frame;
	private JTextField textDestination;
	private JTextField textReplyTo;
	private final ButtonGroup buttonGroupCongestionControl = new ButtonGroup();
	private final ButtonGroup buttonGroupMode = new ButtonGroup();

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Gui window = new Gui();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Gui() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		JButton buttonStopServer = new JButton("Stop server");

		frame = new JFrame();
		frame.setBounds(100, 100, 800, 500);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JPanel containerPanel = new JPanel();
		frame.getContentPane().add(containerPanel, BorderLayout.CENTER);
		containerPanel.setLayout(new BorderLayout(0, 0));

		JPanel mainPanel = new JPanel();
		containerPanel.add(mainPanel, BorderLayout.CENTER);
		mainPanel.setLayout(new BorderLayout(5, 5));

		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		mainPanel.add(tabbedPane);

		JPanel panelClient = new JPanel();
		tabbedPane.addTab("Client", null, panelClient, null);
		panelClient.setLayout(new BorderLayout(0, 0));

		JPanel panelClientWest = new JPanel();
		panelClient.add(panelClientWest, BorderLayout.CENTER);
		panelClientWest.setLayout(new BorderLayout(0, 0));

		JPanel panelClientConfiguration = new JPanel();
		panelClientWest.add(panelClientConfiguration, BorderLayout.CENTER);
		panelClientConfiguration.setLayout(new FormLayout(new ColumnSpec[] {
				ColumnSpec.decode("max(88dlu;pref):grow"),
				ColumnSpec.decode("max(140dlu;pref):grow"),},
				new RowSpec[] {
						FormSpecs.LABEL_COMPONENT_GAP_ROWSPEC,
						RowSpec.decode("50px"),
						RowSpec.decode("50px"),
						RowSpec.decode("80px"),
						RowSpec.decode("65px"),
						RowSpec.decode("max(50px;default)"),}));

		JLabel labelClientDestination = new JLabel("Destination");
		labelClientDestination.setHorizontalAlignment(SwingConstants.CENTER);
		panelClientConfiguration.add(labelClientDestination, "1, 2, fill, fill");

		textDestination = new JTextField();
		textDestination.setFont(new Font("Dialog", Font.BOLD, 16));
		textDestination.setHorizontalAlignment(SwingConstants.CENTER);
		panelClientConfiguration.add(textDestination, "2, 2, fill, fill");
		textDestination.setColumns(10);

		JLabel labelClientReplyTo = new JLabel("Reply to");
		labelClientReplyTo.setHorizontalAlignment(SwingConstants.CENTER);
		panelClientConfiguration.add(labelClientReplyTo, "1, 3, fill, fill");

		textReplyTo = new JTextField();
		textReplyTo.setFont(new Font("Dialog", Font.BOLD, 16));
		textReplyTo.setHorizontalAlignment(SwingConstants.CENTER);
		textReplyTo.setColumns(10);
		panelClientConfiguration.add(textReplyTo, "2, 3, fill, fill");

		JLabel labelCongestionControlMode = new JLabel("Congestion control mode");
		labelCongestionControlMode.setHorizontalAlignment(SwingConstants.CENTER);
		panelClientConfiguration.add(labelCongestionControlMode, "1, 4, fill, fill");

		JPanel panelClientCongestionControl = new JPanel();
		panelClientConfiguration.add(panelClientCongestionControl, "2, 4, fill, fill");
		panelClientCongestionControl.setLayout(new FormLayout(new ColumnSpec[] {
				ColumnSpec.decode("7dlu:grow"),
				ColumnSpec.decode("35px:grow"),
				ColumnSpec.decode("3dlu:grow"),
				ColumnSpec.decode("71px:grow"),
				ColumnSpec.decode("3dlu:grow"),
				ColumnSpec.decode("71px:grow"),
				ColumnSpec.decode("3dlu:grow"),
				ColumnSpec.decode("90px:grow"),},
				new RowSpec[] {
						FormSpecs.LINE_GAP_ROWSPEC,
						RowSpec.decode("24px"),
						FormSpecs.LINE_GAP_ROWSPEC,
						RowSpec.decode("20px"),}));

		JRadioButton radioClientCongestionControlRate = new JRadioButton("");
		buttonGroupCongestionControl.add(radioClientCongestionControlRate);
		panelClientCongestionControl.add(radioClientCongestionControlRate, "2, 2, fill, default");

		JLabel labelClientCongestionControlRate = new JLabel("Rate");
		labelClientCongestionControlRate.setLabelFor(radioClientCongestionControlRate);
		labelClientCongestionControlRate.setHorizontalAlignment(SwingConstants.CENTER);
		panelClientCongestionControl.add(labelClientCongestionControlRate, "4, 2, center, center");

		JSpinner rateValue = new JSpinner();
		rateValue.setModel(new SpinnerNumberModel(1, 1, 9999, 1));
		panelClientCongestionControl.add(rateValue, "6, 2, fill, fill");

		JComboBox<RateUnit> rateUnits = new JComboBox<>();
		rateUnits.setMaximumRowCount(10);
		rateUnits.setModel(new DefaultComboBoxModel<>(RateUnit.values()));
		rateUnits.setSelectedIndex(0);
		panelClientCongestionControl.add(rateUnits, "8, 2, fill, fill");

		JRadioButton radioClientCongestionControlWindow = new JRadioButton("");
		radioClientCongestionControlWindow.setSelected(true);
		buttonGroupCongestionControl.add(radioClientCongestionControlWindow);
		panelClientCongestionControl.add(radioClientCongestionControlWindow, "2, 4, fill, default");

		JLabel labelClientCongestionControlWindow = new JLabel("Window");
		labelClientCongestionControlWindow.setLabelFor(radioClientCongestionControlWindow);
		panelClientCongestionControl.add(labelClientCongestionControlWindow, "4, 4, center, center");

		JSpinner windowValue = new JSpinner();
		windowValue.setModel(new SpinnerNumberModel(1, 1, 9999, 1));
		panelClientCongestionControl.add(windowValue, "6, 4, 3, 1, fill, fill");

		JLabel labelMode = new JLabel("Mode");
		labelMode.setHorizontalAlignment(SwingConstants.CENTER);
		panelClientConfiguration.add(labelMode, "1, 5, fill, fill");

		JPanel panelClientMode = new JPanel();
		panelClientConfiguration.add(panelClientMode, "2, 5, fill, fill");
		panelClientMode.setLayout(new FormLayout(new ColumnSpec[] {
				FormSpecs.LABEL_COMPONENT_GAP_COLSPEC,
				ColumnSpec.decode("43px"),
				ColumnSpec.decode("66px"),
				FormSpecs.LABEL_COMPONENT_GAP_COLSPEC,
				ColumnSpec.decode("100px"),
				FormSpecs.LABEL_COMPONENT_GAP_COLSPEC,
				ColumnSpec.decode("93px"),},
				new RowSpec[] {
						FormSpecs.LINE_GAP_ROWSPEC,
						RowSpec.decode("24px"),
						FormSpecs.LINE_GAP_ROWSPEC,
						RowSpec.decode("24px"),}));

		JRadioButton radioDataMode = new JRadioButton("");
		buttonGroupMode.add(radioDataMode);
		radioDataMode.setSelected(true);
		panelClientMode.add(radioDataMode, "2, 2, fill, default");

		JLabel labelDataMode = new JLabel("Data");
		labelDataMode.setLabelFor(radioDataMode);
		panelClientMode.add(labelDataMode, "3, 2, left, center");

		JSpinner dataModeValue = new JSpinner();
		dataModeValue.setModel(new SpinnerNumberModel(100, 1, 9999, 1));
		panelClientMode.add(dataModeValue, "5, 2, fill, fill");

		JComboBox<DataUnit> dataModeUnit = new JComboBox<DataUnit>();
		dataModeUnit.setModel(new DefaultComboBoxModel<>(DataUnit.values()));
		dataModeUnit.setSelectedIndex(2);
		dataModeUnit.setMaximumRowCount(10);
		panelClientMode.add(dataModeUnit, "7, 2, fill, fill");

		JRadioButton radioTimeMode = new JRadioButton("");
		buttonGroupMode.add(radioTimeMode);
		panelClientMode.add(radioTimeMode, "2, 4, fill, default");

		JLabel labelTimeMode = new JLabel("Time");
		labelTimeMode.setLabelFor(radioTimeMode);
		panelClientMode.add(labelTimeMode, "3, 4, left, center");

		JSpinner timeModeValue = new JSpinner();
		timeModeValue.setModel(new SpinnerNumberModel(1, 1, 9999, 1));
		panelClientMode.add(timeModeValue, "5, 4, fill, fill");

		JComboBox<ChronoUnit> timeModeUnit = new JComboBox<ChronoUnit>();
		timeModeUnit.setModel(new DefaultComboBoxModel<>(ChronoUnit.values()));
		timeModeUnit.setSelectedIndex(3);
		timeModeUnit.setMaximumRowCount(10);
		panelClientMode.add(timeModeUnit, "7, 4, fill, fill");

		JLabel labelPayload = new JLabel("Payload");
		labelPayload.setHorizontalAlignment(SwingConstants.CENTER);
		panelClientConfiguration.add(labelPayload, "1, 6, fill, fill");

		JPanel panelPayload = new JPanel();
		panelClientConfiguration.add(panelPayload, "2, 6, fill, fill");
		panelPayload.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		JLabel labelPayloadSize = new JLabel("Payload size");
		panelPayload.add(labelPayloadSize);

		JSpinner payloadSizeValue = new JSpinner();
		payloadSizeValue.setModel(new SpinnerNumberModel(10, 1, 9999, 1));
		panelPayload.add(payloadSizeValue);

		JComboBox<DataUnit> payloadUnit = new JComboBox<>();
		payloadUnit.setModel(new DefaultComboBoxModel<>(DataUnit.values()));
		payloadUnit.setSelectedIndex(2);
		payloadUnit.setMaximumRowCount(10);
		panelPayload.add(payloadUnit);

		JPanel panelClientButtons = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panelClientButtons.getLayout();
		flowLayout.setHgap(15);
		flowLayout.setVgap(15);
		panelClientWest.add(panelClientButtons, BorderLayout.SOUTH);

		JButton buttonStartClient = new JButton("Start client");
		panelClientButtons.add(buttonStartClient);

		JButton buttonStopClient = new JButton("Stop client");
		buttonStopClient.setEnabled(false);
		panelClientButtons.add(buttonStopClient);

		JPanel panelClientEast = new JPanel();
		panelClient.add(panelClientEast, BorderLayout.EAST);
		panelClientEast.setLayout(new BorderLayout(0, 0));
		panelClientEast.setMaximumSize(new Dimension(200, 800));

		JList<ClientGuiController> listClientsRunning = new JList<>();
		listClientsRunning.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listClientsRunning.setModel(new DefaultListModel<>());
		
		JScrollPane scrollPaneClientsRunning = new JScrollPane(listClientsRunning);
		scrollPaneClientsRunning.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

		JScrollPane scrollPaneClientsRunning2 = new JScrollPane(scrollPaneClientsRunning);
		scrollPaneClientsRunning2.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		
		panelClientEast.add(scrollPaneClientsRunning2, BorderLayout.CENTER);

		JPanel panelServer = new JPanel();
		tabbedPane.addTab("Server", null, panelServer, null);
		panelServer.setLayout(new BorderLayout(0, 0));

		JPanel panelServerNorth = new JPanel();
		panelServer.add(panelServerNorth, BorderLayout.NORTH);
		panelServerNorth.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		JButton buttonStartServer = new JButton("Start server");
		panelServerNorth.add(buttonStartServer);

		buttonStopServer.setEnabled(false);

		panelServerNorth.add(buttonStopServer);

		JPanel panelServerCenter = new JPanel();
		panelServer.add(panelServerCenter, BorderLayout.CENTER);
		panelServerCenter.setLayout(new BorderLayout(0, 0));

		JTextArea textAreaServer = new JTextArea();
		textAreaServer.setTabSize(4);
		textAreaServer.setEditable(false);
		panelServerCenter.add(textAreaServer, BorderLayout.CENTER);

		JPanel statusPanel = new JPanel();
		containerPanel.add(statusPanel, BorderLayout.SOUTH);
		statusPanel.setLayout(new FormLayout(new ColumnSpec[] {
				ColumnSpec.decode("center:max(200dlu;pref):grow"),
				FormSpecs.LABEL_COMPONENT_GAP_COLSPEC,
				ColumnSpec.decode("center:max(100dlu;pref):grow"),},
				new RowSpec[] {
						FormSpecs.LINE_GAP_ROWSPEC,
						RowSpec.decode("15px"),}));

		JLabel statusClientLabel = new JLabel("");
		statusClientLabel.setHorizontalAlignment(SwingConstants.CENTER);
		statusPanel.add(statusClientLabel, "1, 2, fill, fill");

		JLabel statusServerLabel = new JLabel("Server not running.");
		statusServerLabel.setHorizontalAlignment(SwingConstants.CENTER);
		statusPanel.add(statusServerLabel, "3, 2, fill, fill");


		/*****  BUTTON HANDLERS *****/

		buttonStartServer.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				try {
					Server server = serverGuiController.startServer();
					server.addBundleReceivedListener(new BundleReceivedListener() {
						@Override
						public void bundleReceivedEvent(Bundle bundle) {
							textAreaServer.append("Received bundle from " + bundle.getSource() + " of size " + bundle.getData().length + "\n");
						}
					});
					server.addBundleSentListener(new BundleSentListener() {
						@Override
						public void bundleSentEvent(Bundle bundleSent) {
							textAreaServer.append("Sent ack bundle to " + bundleSent.getDestination() + "\n");
						}
					});
				} catch(Exception e) {
					JOptionPane.showMessageDialog(null, "Error on creating server", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (serverGuiController.isServerRunning()) {
					buttonStartServer.setEnabled(false);
					buttonStopServer.setEnabled(true);
					statusServerLabel.setText("Server is running...");
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {}
					textAreaServer.append("Server opened on " + serverGuiController.getServer().getLocalEID().getEndpointID() + "\n");
				}
			}
		});

		buttonStopServer.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				serverGuiController.stopServer();
				buttonStopServer.setEnabled(false);
				statusServerLabel.setText("Server is stopping...");
				new Thread(new Runnable() {
					@Override
					public void run() {
						serverGuiController.waitForTerminating();
						statusServerLabel.setText("Server stopped.");
						buttonStartServer.setEnabled(true);
					}
				}).start();
			}
		});

		buttonStartClient.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				try {
					BundleEID dest = BundleEID.of(textDestination.getText());
					BundleEID replyTo = BundleEID.NoneEndpoint;
					if (textReplyTo.getText().length() > 0)
						replyTo = BundleEID.of(textReplyTo.getText());
					ClientCongestionControl congestionControl;

					if (radioClientCongestionControlRate.isSelected()) { // Rate
						final int number;
						try {
							rateValue.commitEdit();
						} catch (Exception e) {}
						number = (int) rateValue.getValue();
						RateUnit rateUnit = rateUnits.getItemAt(rateUnits.getSelectedIndex());
						congestionControl = new ClientCongestionControlRate(number, rateUnit );
					} else { // Window
						final int window;
						try {
							windowValue.commitEdit();
						} catch (Exception e) {}
						window = (int) windowValue.getValue();
						congestionControl = new ClientCongestionControlWindow(window);
					}

					Mode mode;

					if (radioDataMode.isSelected()) { // Data mode
						final int number;
						try {
							dataModeValue.commitEdit();
						} catch (Exception e) {}
						number = (int) dataModeValue.getValue();
						DataUnit unit = dataModeUnit.getItemAt(dataModeUnit.getSelectedIndex());
						mode = new DataMode(number, unit);
					} else { // Time mode
						final int time;
						try {
							timeModeValue.commitEdit();
						} catch (Exception e) {}
						time = (int) timeModeValue.getValue();
						ChronoUnit unit = timeModeUnit.getItemAt(timeModeUnit.getSelectedIndex());
						mode = new TimeMode(time, unit);
					}

					try {
						payloadSizeValue.commitEdit();
					} catch (Exception e) {}
					int payloadSizeNumber = (int) payloadSizeValue.getValue();
					DataUnit payloadDataUnit = payloadUnit.getItemAt(payloadUnit.getSelectedIndex());

					if (dest == null || congestionControl == null || mode == null || payloadDataUnit == null)
						throw new IllegalStateException();

					ClientGuiController clientGuiController = new ClientGuiController();
					clientGuiController.startClient(dest, replyTo, congestionControl, mode, payloadSizeNumber, payloadDataUnit);
					DefaultListModel<ClientGuiController> model = (DefaultListModel<ClientGuiController>) listClientsRunning.getModel();
					model.addElement(clientGuiController);
					Thread.sleep(100);
					listClientsRunning.setSelectedValue(clientGuiController, true);
					new Thread(new Runnable() {
						@Override
						public void run() {
							clientGuiController.waitForTerminating();
							buttonStopClient.doClick();
						}
					}).start();

				} catch (Exception e) {
					JOptionPane.showMessageDialog(null, "Error on creating client", "Error", JOptionPane.ERROR_MESSAGE);
					buttonStopClient.doClick();
				}
			}
		});

		buttonStopClient.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				ClientGuiController clientGuiController = listClientsRunning.getSelectedValue();
				buttonStopClient.setEnabled(false);
				if (clientGuiController == null)
					return;
				clientGuiController.stopClient();
				new Thread(new Runnable() {
					@Override
					public void run() {
						clientGuiController.waitForTerminating();
						buttonStartClient.setEnabled(true);
					}
				}).start();
			}
		});

		listClientsRunning.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent event) {
				buttonStopClient.setEnabled(event.getLastIndex() >= 0 && listClientsRunning.getSelectedValue().isClientRuning());
			}
		});

		this.statusUpdater = new ClientStatusUpdater(listClientsRunning, statusClientLabel);
		this.statusUpdater.start();

	} // initialize

}

class ClientStatusUpdater implements Runnable, BundleSentListener {

	private JList<ClientGuiController> listRunningClients;
	private JLabel labelClientStatus;
	
	private ClientGuiController clientGuiController = null;

	public ClientStatusUpdater(JList<ClientGuiController> list, JLabel label) {
		this.listRunningClients = list;
		this.labelClientStatus = label;
	}

	@Override
	public void run() {
		ClientGuiController clientController;
		while (true) {
			try {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					break;
				}
				clientController = this.listRunningClients.getSelectedValue();
				
				if (clientController == this.clientGuiController) {
					if (clientController.isClientRuning())
						continue;
					this.updateStatus();
				}
				
				if (this.clientGuiController != null) { // unhandle from listener
					this.clientGuiController.getClient().removeBundleSentListener(this);
				}
				
				if (clientController == null) {
					this.labelClientStatus.setText("");
				} else { // handle from listener
					clientController.getClient().addBundleSentListener(this);
				}
				
				this.clientGuiController = clientController;
			} catch (Exception e) {}
		}
	}
	
	private void updateStatus() {
		if (this.clientGuiController == null)
			return;
		
		Client client = this.clientGuiController.getClient();
		StringBuilder status = new StringBuilder();
		if (this.clientGuiController.isClientRuning()) {
			status.append(client);
			status.append(" sent bundles: ");
			status.append(client.getSentBundles());
			status.append(" sent data: ");
			status.append(client.getDataSent());
			status.append(" bytes");
		} else {
			status.append(client);
			status.append(' ');
			status.append(client.getResultString());
		}
		this.labelClientStatus.setText(status.toString());
	}

	public void start() {
		Thread t = new Thread(this, "Client status updater");
		t.setDaemon(true);
		t.start();
	}

	@Override
	public void bundleSentEvent(Bundle bundleSent) {
		this.updateStatus();
	}

}
