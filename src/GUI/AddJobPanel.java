package GUI;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

import Core.Master;

public class AddJobPanel extends JPanel implements ActionListener{

	private JButton add;
	private JButton cancel;
	private ImageIcon open;

	private JLabel jarL;
	private JTextField jarTF;
	private JButton jarB;
	private JFileChooser jarFC;

	private JLabel inputL;
	private JTextField inputTF;
	private JButton inputB;
	private JFileChooser inputFC;

	//private JLabel outputL;
	//private JTextField outputTF;
	
	private JLabel timeOutL;
	private JTextField timeOutTF;
	
	private JLabel maxMemoryL;
	private JTextField maxMemortTF;
	
	private GUInterface frame;
	private MainPanel parent;
	private JFrame myFrame;
	private Master master;
	
	public AddJobPanel(GUInterface frame,MainPanel parent, Master master)
	{
		//
		this.frame = frame;
		this.parent = parent;
		this.master = master;
		myFrame = new JFrame();

		
		//initialise
		open = new ImageIcon(this.getClass().getResource("/open.gif"));

		jarL = new JLabel(".jar path:");
		jarTF = new JTextField("plz use file chooser");
		jarTF.setEditable(false);
		jarB = new JButton(open);
		jarFC = new JFileChooser();
		jarFC.setAcceptAllFileFilterUsed(false);
		jarFC.addChoosableFileFilter(new JarFileFilter());
		
		inputL = new JLabel("input file path:");
		inputTF = new JTextField("plz use file chooser");
		inputTF.setEditable(false);
		inputB = new JButton(open);
		inputFC = new JFileChooser();

		//outputL = new JLabel("output file name:");
		//outputTF = new JTextField("output file name here");

		timeOutL = new JLabel("Time Out(s): ");
		timeOutTF = new JTextField("60");
		maxMemoryL = new JLabel("Max Memory(MB):");
		maxMemortTF = new JTextField("256");
		
		add = new JButton("Add");
		cancel = new JButton("Cancel");

		//listener
		jarB.addActionListener(this);
		inputB.addActionListener(this);
		add.addActionListener(this);
		cancel.addActionListener(this);
		
		
		//layout
		jarL.setAlignmentX(LEFT_ALIGNMENT);
		add(jarL);
		add(Box.createVerticalStrut(5));

		JPanel p1 = new JPanel();
		jarTF.setMaximumSize(new Dimension(420,32));
		p1.add(jarTF);
		p1.add(Box.createHorizontalStrut(5));
		p1.add(jarB);
		p1.setAlignmentX(LEFT_ALIGNMENT);
		p1.setLayout(new BoxLayout(p1,BoxLayout.X_AXIS));
		add(p1);
		add(Box.createVerticalStrut(5));

		add(inputL);
		add(Box.createVerticalStrut(5));

		JPanel p2 = new JPanel();
		inputTF.setMaximumSize(new Dimension(420,32));
		p2.add(inputTF);
		p2.add(Box.createHorizontalStrut(5));
		p2.add(inputB);
		p2.setAlignmentX(LEFT_ALIGNMENT);
		p2.setLayout(new BoxLayout(p2,BoxLayout.X_AXIS));
		add(p2);
		add(Box.createVerticalStrut(5));

		//outputL.setAlignmentX(LEFT_ALIGNMENT);
		//add(outputL);
		//add(Box.createVerticalStrut(5));

		//outputTF.setAlignmentX(LEFT_ALIGNMENT);
		//outputTF.setMaximumSize(new Dimension(480,32));
		//add(outputTF);
		//add(Box.createVerticalStrut(5));

		JPanel p3 = new JPanel();
		timeOutTF.setMaximumSize(new Dimension(420,32));
		p3.add(this.timeOutL);
		p3.add(Box.createHorizontalStrut(5));
		p3.add(timeOutTF);
		p3.setAlignmentX(LEFT_ALIGNMENT);
		p3.setLayout(new BoxLayout(p3,BoxLayout.X_AXIS));
		add(p3);
		add(Box.createVerticalStrut(5));
		
		JPanel p4 = new JPanel();
		maxMemortTF.setMaximumSize(new Dimension(420,32));
		p4.add(this.maxMemoryL);
		p4.add(Box.createHorizontalStrut(5));
		p4.add(maxMemortTF);
		p4.setAlignmentX(LEFT_ALIGNMENT);
		p4.setLayout(new BoxLayout(p4,BoxLayout.X_AXIS));
		add(p4);
		
		JPanel p5 = new JPanel();
		p5.add(add);
		p5.add(cancel);
		p5.setAlignmentX(LEFT_ALIGNMENT);
		add(p5);

		
		
		//frame
		setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
		myFrame.setTitle("Add Job...");
		myFrame.setResizable(false);
		myFrame.setLocation(500,300);
		myFrame.setContentPane(this);
		myFrame.setMinimumSize(new Dimension(480,240));
		myFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

		myFrame.pack();
	}

	public void setVisible(boolean b)
	{
		myFrame.setVisible(b);
	}
	
	public void clear()
	{
		jarTF.setText("");
		inputTF.setText("");
		//outputTF.setText("");
	}


	public void actionPerformed(ActionEvent e) 
	{
		if(e.getSource() == jarB)
		{
			int returnVal = jarFC.showOpenDialog(this);
			if(returnVal == JFileChooser.APPROVE_OPTION)
			{
				File file = jarFC.getSelectedFile();
				jarTF.setText(file.getAbsolutePath());
			}
			
		}
		else if(e.getSource() == inputB)
		{
			int returnVal = inputFC.showOpenDialog(this);
			if(returnVal == JFileChooser.APPROVE_OPTION)
			{
				File file = inputFC.getSelectedFile();
				inputTF.setText(file.getAbsolutePath());
			}			
		}
		else if(e.getSource() == add)
		{
			this.setVisible(false);
			
			int mem,time;
			String jar = jarTF.getText();
			String input = inputTF.getText();
			try{
				mem = Integer.parseInt(maxMemortTF.getText()); 
			}catch(NumberFormatException nfe)
			{
				mem = 256;
			}
			
			try{
				time =  Integer.parseInt(timeOutTF.getText());
			}catch(NumberFormatException nfe)
			{
				time = 60;
			}
			
			master.addJob(jar,input,mem,time);
			//parent.addJob("0", "RUNNING", jarTF.getText(), inputTF.getText(), timeOutTF.getText(), maxMemortTF.getText());
			
			this.clear();
		}
		else if(e.getSource() == cancel)
		{
			this.setVisible(false);
			this.clear();			
		}
		else
		{
			System.err.println("Uncatch action AddJobPanelListener()@CloudAddJobPanel.java");
		}
	}
	
	
	private class JarFileFilter extends FileFilter
	{
		public boolean accept(File f) {
			 if (f.isDirectory()) {
			        return true;
			    }

			    String extension = getExtension(f);
			    if (extension != null) {
			        if (extension.equals("jar")) {
			                return true;
			        } else {
			            return false;
			        }
			    }
			    return false;
		}

		
		public String getDescription() 
		{			
			return "Jar file only";
		}
		
		private String getExtension(File f)
		{
			String ext = null;
	        String s = f.getName();
	        int i = s.lastIndexOf('.');

	        if (i > 0 &&  i < s.length() - 1) {
	            ext = s.substring(i+1).toLowerCase();
	        }
	        return ext;
		}
	}
}
