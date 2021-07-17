package ControllerInputTest;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;

public class ControllerInputTest extends JFrame {
	private static final long serialVersionUID = 1L;
	private JPanel panel = new JPanel();
	private JComboBox<String> strCombo;
	private JButton startLogButton = new JButton("START LOG");
	private JButton stopLogButton = new JButton("STOP LOG");
	private JButton saveLogButton = new JButton("SAVE LOG");
	private JTextArea deviceLog = new JTextArea();
	private JScrollPane scrollPane = new JScrollPane(deviceLog);
	
	Controller[] controllers = {};
	String[] controllerName = {};
	Component[] components = {};
	
	DeviceInputThread deviceInputThread;
	
	public ControllerInputTest() {
		super("Life Enhancing Technology Lab. - Controller Input Test");
		
		this.setUndecorated(false);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		controllers = ControllerEnvironment.getDefaultEnvironment().getControllers();
		controllerName = new String[controllers.length];
				
		for (int i = 0; i<controllerName.length; i++) {
			controllerName[i] = controllers[i].getName() + " / " + controllers[i].getType();
		}
		
		strCombo = new JComboBox<String>(controllerName);
		panel.setLayout(new FlowLayout());
		panel.add(strCombo);
		panel.add(startLogButton);
		panel.add(stopLogButton);
		panel.add(saveLogButton);
		add(panel, "North");
		add(scrollPane, "Center");
		saveLogButton.setEnabled(false);
		
		this.setSize(1500, 400);
		this.setVisible(true);
		this.setFocusable(true);
				
		startLogButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				deviceLog.setText("");
				saveLogButton.setEnabled(false);
				components = controllers[strCombo.getSelectedIndex()].getComponents();
				deviceInputThread = new DeviceInputThread();
				deviceInputThread.setStop(false);
				deviceInputThread.start();
			}
		});

		stopLogButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				deviceInputThread.setStop(true);
				saveLogButton.setEnabled(true);
			}
		});
		
		saveLogButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					int selectedIndex = strCombo.getSelectedIndex();
					Controller controller = controllers[selectedIndex];
					String controllerFileName = "Controller" + selectedIndex + "_" + controller.getType();
					String outputFileName = controllerFileName+".csv";
					File outputFile = new File(outputFileName);
					if(outputFile.exists() == false) 
						outputFile.createNewFile();
					BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFile, true));
					// index 저장
					for (int i=0; i<controller.getComponents().length; i++)
						bufferedWriter.write("Component index " + i + ",");
					bufferedWriter.newLine();
					bufferedWriter.flush();
					
					// deviceLog 저장
					String log = deviceLog.getText().replaceAll("\t", ",");
					bufferedWriter.write(log);
					bufferedWriter.flush();
					
					bufferedWriter.close();
				} catch(FileNotFoundException ex) {
					ex.printStackTrace();
				} catch(IOException ex) {
					ex.printStackTrace();
				}
			}
		});
	}
	
	class DeviceInputThread extends Thread {
		private boolean stop;
		
		public void setStop(boolean stop) {
			this.stop = stop;
		}
		
		public void run() {
			while (!stop) {				
				try {
					Thread.sleep(500);
				} catch (Exception e) {}
				controllers[strCombo.getSelectedIndex()].poll();
				
				for (Component component : components)
					deviceLog.append(component.getPollData()+"\t");
				deviceLog.append("\n");
				deviceLog.setCaretPosition(deviceLog.getDocument().getLength());
			}
		}
	}

	public static void main(String[] args) {
		@SuppressWarnings("unused")
		ControllerInputTest uit = new ControllerInputTest();
	}
}