import java.awt.Color;
import java.awt.EventQueue;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextPane;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.border.BevelBorder;


public class MainGUI extends JFrame {

	private JPanel contentPane;
	private File file=null;
	private File outFolder=null;
	private MotionDetector md=null;
	private int sensitivity;
	private JSlider sliderProgress;
	/**
	 * Launch the application.
	 */
	public String TimeToString(double timeD){
		StringBuilder sb=new StringBuilder();
	int time_sec = (int) (timeD  % 60);
	int time_min = (int) (timeD  / 60 % 60);
	int time_hour = (int) (timeD  / 3600);
	if (time_hour > 9)
		sb.append(String.valueOf(time_hour));
	else
		sb.append('0' + String.valueOf(time_hour));
	sb.append(':');
	if (time_min > 9)
		sb.append(String.valueOf(time_min));
	else
		sb.append('0' + String.valueOf(time_min));
	sb.append(':');
	if (time_sec > 9)
		sb.append(String.valueOf(time_sec));
	else
		sb.append('0' + String.valueOf(time_sec));

	return sb.toString();
	}
	public static void infoBox(String infoMessage, String titleBar)
    {
        JOptionPane.showMessageDialog(null, infoMessage, titleBar, JOptionPane.INFORMATION_MESSAGE);
    }
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainGUI frame = new MainGUI();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	


	/**
	 * Create the frame.
	 */
	public MainGUI() {
		String workingDir = System.getProperty("user.dir");
		final JFileChooser fc = new JFileChooser(workingDir);
		final JFileChooser dc = new JFileChooser(workingDir);
		//Set mode to select output FOLDER and not file.
		dc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		//Make the fileChooser show only some types of files
		FileNameExtensionFilter vidfilter = new FileNameExtensionFilter(
			     "Video Files (*.mp4,*.avi,*.mov)", "mp4","avi","mov");
		setTitle("Motion Finder V1.0.0");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 586, 336);
		
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);
		
		JMenuItem mntmNewMenuItem = new JMenuItem("Exit");
		mnFile.add(mntmNewMenuItem);
		
		JMenu mnNewMenu = new JMenu("Help");
		menuBar.add(mnNewMenu);
		
		JMenuItem mntmQuickGuide = new JMenuItem("Quick Guide");
		mnNewMenu.add(mntmQuickGuide);
		
		JMenuItem mntmAbout = new JMenuItem("About");
		mnNewMenu.add(mntmAbout);
		contentPane = new JPanel();
		contentPane.setBackground(SystemColor.activeCaption);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JButton btnOpenVideoFile = new JButton("Open video file");
		
		btnOpenVideoFile.setBounds(10, 11, 130, 34);
		contentPane.add(btnOpenVideoFile);
		
		JButton btnAnalayze = new JButton("Analyze");
		
		btnAnalayze.setEnabled(false);
		btnAnalayze.setBounds(10, 56, 130, 34);
		contentPane.add(btnAnalayze);
		
		JSlider sliderStartTime = new JSlider();
		
		sliderStartTime.setForeground(Color.GRAY);
		sliderStartTime.setBounds(164, 28, 175, 15);
		contentPane.add(sliderStartTime);
		
		JLabel lblStartTimeTxt = new JLabel("Start time:");
		lblStartTimeTxt.setBounds(165, 11, 72, 14);
		contentPane.add(lblStartTimeTxt);
		
		JLabel lblStartTime = new JLabel("-");
		lblStartTime.setBounds(258, 11, 65, 14);
		contentPane.add(lblStartTime);
		
		JSlider sliderEndTime = new JSlider();
		
		sliderEndTime.setForeground(Color.GRAY);
		sliderEndTime.setBounds(164, 73, 175, 15);
		contentPane.add(sliderEndTime);
		
		JLabel lblEndTime = new JLabel("-");
		lblEndTime.setBounds(255, 54, 83, 14);
		contentPane.add(lblEndTime);
		
		JLabel lblEndTimeTxt = new JLabel("End time:");
		lblEndTimeTxt.setBounds(164, 54, 58, 14);
		contentPane.add(lblEndTimeTxt);
		
		JButton btnOpenOutputFile = new JButton("Open CSV file");
		btnOpenOutputFile.setEnabled(false);
		btnOpenOutputFile.setBounds(407, 199, 112, 34);
		btnOpenOutputFile.setVisible(false);
		contentPane.add(btnOpenOutputFile);
		
		JTextPane summaryText = new JTextPane();
		summaryText.setEditable(false);
		summaryText.setBackground(SystemColor.activeCaption);
		summaryText.setBounds(10, 199, 387, 53);
		summaryText.setVisible(false);
		contentPane.add(summaryText);
		
		JSlider sliderSensitivity = new JSlider();
		sliderSensitivity.setMinimum(0);
		sliderSensitivity.setMaximum(5000);
		
		sliderSensitivity.setForeground(Color.GRAY);
		sliderSensitivity.setBounds(164, 113, 175, 15);
		contentPane.add(sliderSensitivity);
		
		JLabel lblSensitivityTxt = new JLabel("Sensitivity:");
		lblSensitivityTxt.setBounds(164, 99, 72, 14);
		contentPane.add(lblSensitivityTxt);
		
		JLabel lblSensitivity = new JLabel("-");
		lblSensitivity.setBounds(256, 99, 83, 14);
		contentPane.add(lblSensitivity);
		
		sliderProgress = new JSlider();
		sliderProgress.setMinimum(0);
		sliderProgress.setMaximum(100);
		
		
		sliderProgress.setBackground(new Color(95, 158, 160));
		sliderProgress.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		sliderProgress.setBounds(56, 162, 341, 26);
		contentPane.add(sliderProgress);
		
		JLabel lblProgress = new JLabel("Progress");
		lblProgress.setBounds(68, 143, 72, 14);
		contentPane.add(lblProgress);
		
		
	//Event handlers
		sliderSensitivity.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				lblSensitivity.setText(Double.toString((double)sliderSensitivity.getValue()/5000));
				sensitivity=sliderSensitivity.getValue();
			}
		});
		btnAnalayze.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				
				
				String[] summary = md.motionFinder(sliderStartTime.getValue(), sliderEndTime.getValue(),sensitivity);
				summaryText.setText(summary[0]);
				summaryText.setVisible(true);
				btnOpenOutputFile.setVisible(true);
				btnOpenOutputFile.setEnabled(true);
				btnOpenOutputFile.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent arg0) {
						Runtime run = Runtime.getRuntime();
						String execCmd="cmd.exe /c " + summary[1];
						try {
						    Process pp = run.exec(execCmd);
						} catch(Exception e) {
						    e.printStackTrace();
						}	
					}
				});
				
			}
		});
		btnOpenVideoFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				fc.setDialogTitle("Choose a video file");
				fc.setFileFilter(vidfilter);
				int retVal = fc.showOpenDialog(MainGUI.this);
				if (retVal != JFileChooser.APPROVE_OPTION)
					infoBox("No file selected", "Please select a video file to analyze.");
				else
				{
					file = fc.getSelectedFile();
					md = new MotionDetector(file.getName(), file.getParent(), file.getParent(), 5);
					btnAnalayze.setEnabled(true);
					sliderStartTime.setMinimum(0);
					sliderStartTime.setValue(sliderStartTime.getMinimum());
					sliderStartTime.setMaximum(md.getLength()-1);
					lblStartTime.setText(TimeToString((int)sliderStartTime.getValue()));
					sliderEndTime.setMinimum(1);
					int vidEndTime=md.getLength();
					sliderEndTime.setMaximum(vidEndTime);
					sliderEndTime.setValue(sliderEndTime.getMaximum());
					lblEndTime.setText(TimeToString((int)sliderEndTime.getValue()));
					btnOpenOutputFile.setVisible(true);
					btnAnalayze.setEnabled(true);
					
				}
			}
		});
		sliderStartTime.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				sliderEndTime.setMinimum(sliderStartTime.getValue());
				lblStartTime.setText(TimeToString((int)(sliderStartTime.getValue())));
			}
		});
		sliderEndTime.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				sliderStartTime.setMaximum(sliderEndTime.getValue()-1);
				lblEndTime.setText(TimeToString((int)(sliderEndTime.getValue())));
			}
		});
		
	}
	public void setSliderProgress(int val) {
		sliderProgress.setValue(val);
	}
}
