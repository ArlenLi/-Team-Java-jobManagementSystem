package GUI;

import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;

public class WorkerStatusPanel extends JPanel{

	//private JLabel workerName;
	//private JLabel workerID;
	private JLabel workerIP;
	private JLabel workerStatus;
	private String name = "WorkerName";
	private String ID = "WorkerID" ;
	private String IP = "WorkerIP";
	private String status = "WorkerStatus";
	
	public WorkerStatusPanel(WorkerPanel parent)
	{
		//initialise
		//workerName = new JLabel(name);
		//workerID = new JLabel(ID);
		workerIP = new JLabel(IP);
		workerStatus = new JLabel(status);
		
		//layout
		//workerName.setAlignmentX(LEFT_ALIGNMENT);
		//workerName.setPreferredSize(new Dimension(320,32));
		//workerID.setAlignmentX(LEFT_ALIGNMENT);
		//workerID.setPreferredSize(new Dimension(320,32));
		workerIP.setAlignmentX(LEFT_ALIGNMENT);
		workerIP.setPreferredSize(new Dimension(320,32));
		workerStatus.setAlignmentX(LEFT_ALIGNMENT);
		workerStatus.setPreferredSize(new Dimension(320,32));
			
		//this.add(workerName);
		//this.add(new JSeparator(JSeparator.VERTICAL));
		//this.add(workerID);
		//this.add(new JSeparator(JSeparator.VERTICAL));
		this.add(workerIP);
		this.add(new JSeparator(JSeparator.VERTICAL));
		this.add(workerStatus);
		this.add(new JSeparator(JSeparator.VERTICAL));
		this.setLayout(new BoxLayout(this,BoxLayout.X_AXIS));
	}
	
	//public void setName(String s){this.name = s;workerName.setText(this.name);this.repaint();}
	//public void setID(String s){this.ID = s;workerID.setText(this.ID);this.repaint();}
	public void setIP(String s){this.IP = s;workerIP.setText(this.IP);this.repaint();}
	public void setStatus(String s){this.status = s;workerStatus.setText(this.status);this.repaint();}
}
