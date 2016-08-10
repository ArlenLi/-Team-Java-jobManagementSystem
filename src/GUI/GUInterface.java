package GUI;

import Core.*;

import javax.swing.JFrame;

public class GUInterface extends JFrame {

	public static final String title = "Master GUI";
	public static final int width = 1024;
	public static final int height = 700;
	
	private MainPanel cc;
	private Master master;
	
	
	public GUInterface()
	{		
		//Core work
		master = new Master(this);
		
		
		//GUI work
		JFrame cloudFrame = new JFrame(title);
		cc = new MainPanel(this,master);
		
		//
		cloudFrame.setJMenuBar(new MenuBar(this,cc,master));
			
		
		//
		cloudFrame.setContentPane(cc);
		cloudFrame.setVisible(true);
		
		cloudFrame.setSize(width,height);
		cloudFrame.setLocationRelativeTo(null);
		cloudFrame.setResizable(false);
		cloudFrame.setAlwaysOnTop(false);
		cloudFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		master.readFile();
	}	
	
	public void log(String s){cc.log(s);}
	public void addWorker(String add, int port)
	{
		cc.addWorker(add, port);
		}
	public void setWorkerStatus(String workerName, String IP, String status)
	{
		cc.setWorkerStatus(workerName,IP,status);
	}
	
	public void addJob(String workerName,String id, String stat, String jar, String input, String time, String mem)
	{
		cc.addJob(workerName, id, stat, jar, input, time, mem);
	}
	
	public void updateJobStat(String workerName, int jobID, String stat)
	{
		cc.updateJobStat(workerName,jobID, stat);
	}
	
	public void removeJob(int jobID)
	{
		cc.removeJob(jobID);
	}
}
