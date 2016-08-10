package GUI;

import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.text.DefaultCaret;

import Core.Master;

public class MainPanel extends JPanel{

	private JTabbedPane tp;	
	private ImageIcon red_led;
	private ImageIcon green_led;
	private JTextArea log;
	private GUInterface frame;

	private List<WorkerPanel> workerPanels;
	private Master master;

	public MainPanel(GUInterface frame, Master master)
	{	
		//
		this.frame = frame;
		this.master = master;

		//initialise
		red_led = new ImageIcon(this.getClass().getResource("/red.png"));
		green_led = new ImageIcon(this.getClass().getResource("/green.png"));


		tp = new JTabbedPane();
		//one worker one tab
		//image and tooltip shows the worker status
		workerPanels = new ArrayList<WorkerPanel>();


		/* table model test panel
		WorkerPanel temp =  new WorkerPanel(frame,this,master);
		workerPanels.add(temp);
		tp.addTab("Woker 1",red_led,temp);
		 */
		//tp.addTab("Woker 2",green_led, new WorkerPanel(frame,this));

		tp.setPreferredSize(new Dimension(GUInterface.width, 400));

		log = new JTextArea();
		log.setEditable(true);
		log.setLineWrap(true);		

		JScrollPane jsp = new JScrollPane(log);
		jsp.setPreferredSize(new Dimension(GUInterface.width,200));


		//add component
		add(tp);
		add(Box.createVerticalStrut(5));
		add(jsp);
		setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));

	}

	public void log(String s)
	{
		log.append(s+"\n");
		log.setCaretPosition(log.getDocument().getLength());
	}

	public void addWorker(String address, int port)
	{
		WorkerPanel temp =  new WorkerPanel(frame,this,master,address+":"+port);
		workerPanels.add(temp);
		temp.setStatus(address+":"+port,null);
		tp.addTab(address,red_led,temp);
		tp.repaint();
		this.repaint();	
	}

	public void setWorkerStatus(String workerName, String IP, String status)
	{
		//int index = 0;
		boolean findWorker = false;
		for(int i=0;i<workerPanels.size();i++)
		{
			WorkerPanel temp = workerPanels.get(i);
			String name = temp.getWorkerName();

			if(name.equals(workerName))
			{
				if(status.equals("CONNECTED"))
				{
					tp.setIconAt(i, green_led);
					temp.setAddJobEnabled(true);
				}
				if(status.equals("DISCONNECTED"))
				{
					tp.setIconAt(i, red_led);
					temp.setAddJobEnabled(false);
				}
				temp.setStatus(IP, status);
				findWorker = true;
			}
		}	

		if(!findWorker)
		{
			log("ERROR: " + workerName + " not found in GUI setWorkerStatus()@MainPanel ");
		}

	}

	public void addJob(String workerName,String id, String stat, String jar, String input, String time, String mem)
	{
		//TODO select right worker panel
		boolean findWorker = false;
		for(int i=0;i<workerPanels.size();i++)
		{
			WorkerPanel temp = workerPanels.get(i);
			String name = temp.getWorkerName();
			if(name.equals(workerName))
			{
				temp.addJob(id, stat, jar, input, time, mem);
				findWorker = true;

				log("");
				log("-= Add new job to "+workerName+ " =-");
				log("Job ID = "+id);
				log("Jar path = "+jar);
				log("Input path = "+input);
				log("Time limit(s) = "+time);
				log("Memory limit(MB) = "+mem);
				log("");
			}
		}

		if(!findWorker)
		{
			log("ERROR: " + workerName + "not found in GUI addJob()@MainPanel ");
		}

	}

	public void updateJobStat(String workerName, int jobID, String stat)
	{

		boolean findWorker = false;
		for(int i=0;i<workerPanels.size();i++)
		{
			WorkerPanel temp = workerPanels.get(i);
			String name = temp.getWorkerName();
			if(name.equals(workerName))
			{
				temp.updateJobStat(jobID, stat);
				findWorker = true;
				log(workerName +" 's " + "job " + jobID + " is " + stat);
			}
		}

		if(!findWorker)
		{
			log("ERROR: " + workerName + "not found in GUI updateJobState()@MainPanel ");
		}

	}



	public void removeJob(int jobID)
	{
		//TODO select right worker panel
	}
}
