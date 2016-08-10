package GUI;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EtchedBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import Core.Master;

public class WorkerPanel extends JComponent {

	private GUInterface frame;
	private MainPanel parent;

	private final String[] columnNames = {"JobID","Status","Jar path","input path","Time limit","Memory limit"};
	private JTable jobTable;
	private WorkerStatusPanel wsp;
	private JButton addJob;
	private AddJobPanel addJobPanel;
	private JobTableModel jtm;
	
	private Master master;
	private String workerName;
	
	Object[][] test = 
		{
			{"1991","RUNNING","C:\\","C:\\","C:\\","",""},
			{"1990","ERROR","C:\\","C:\\","C:\\","",""},
			{"1990","FINISHED","C:\\","C:\\","C:\\","",""},
			{"1990","PENDING","C:\\","C:\\","C:\\","",""}
		};

	public WorkerPanel(GUInterface frame, MainPanel parent, Master master,String workerName)
	{
		//
		this.frame = frame;
		this.parent = parent;
		this.master = master;
		this.workerName = workerName;
		
		//initialise
		jtm = new JobTableModel();
		jobTable = new JTable(jtm)
		{
			//set color
			public Component prepareRenderer(TableCellRenderer renderer, int row, int column)
			{
				Component c = super.prepareRenderer(renderer, row, column);

				String stat = (String)this.getValueAt(row, 1);
				if(!isRowSelected(row))
				{
					//TODO, finished job should be deleted
					//TODO, job sort ERROR > Running > disconnected 
					if(stat.equals("RUNNING"))
					{
						c.setBackground(new Color(120,255,120));
					}
					else if(stat.equals("FINISHED"))
					{
						c.setBackground(Color.WHITE);
					}
					else if(stat.equals("PENDING"))
					{
						c.setBackground(new Color(255,163,80));
					}
					else if(stat.equals("FAILURE"))
					{
						c.setBackground(new Color(255,120,120));
					}
					else
					{
						c.setBackground(Color.BLACK);
					}      				        			        	
				}
				return c;
			}
		};
		JScrollPane sp = new JScrollPane(jobTable);
		addJobPanel = new AddJobPanel(frame,parent,master);

		wsp = new WorkerStatusPanel(this);
		
		//add job button
		addJob = new JButton("addJob");
		addJob.setSize(120,32);
		addJob.setEnabled(false);
		addJob.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				if(addJob == e.getSource())
				{
					addJobPanel.setVisible(true);
				}
			}
		});


		add(sp);
		add(Box.createVerticalStrut(2));

		//status bar
		JPanel status = new JPanel();
		status.setLayout(new BoxLayout(status,BoxLayout.X_AXIS));
		status.add(wsp);
		status.add(Box.createHorizontalStrut(5));
		status.add(addJob);
		status.setBorder( BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
		add(status);

		//layout
		setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
	}
	
	public void setStatus(String IP, String status)
	{
		//if(name != null)wsp.setName(name);
		//if(ID != null)wsp.setID(ID);
		if(IP != null)wsp.setIP(IP);
		if(status != null)wsp.setStatus(status);
		this.repaint();
	}
	
	//TODO: later use
	private class JobTableModel extends AbstractTableModel
	{

		private String[] coloumnNames  = {"JobID","Status","Jar path","input path","Time limit","Memory limit"};
		private Object[][] data = {};
		
		public void addRow(String id, String status, String jar, String input, String time, String mem)
		{
			int newCol = data.length+1;
			Object[][] newData = new Object[newCol][coloumnNames.length];
			for(int j=0; j<data.length;j++)
			{
				for(int i=0; i<coloumnNames.length;i++)
				{
					newData[j][i] = data[j][i];
				}
			}
			newData[newCol-1][0] = id;	
			newData[newCol-1][1] = status;
			newData[newCol-1][2] = jar;
			newData[newCol-1][3] = input;
			newData[newCol-1][4] = time;
			newData[newCol-1][5] = mem;
					
			data = newData;
			this.fireTableDataChanged();
		}
				
		public int getColumnCount()
		{
			return coloumnNames.length;
		}
		
		public String getColumnName(int col)
		{
			return coloumnNames[col];
		}


		public int getRowCount() 
		{
			return data.length;
		}

		public boolean isCellEditable(int row, int col)
		{
			return false; 
		}

		public Object getValueAt(int row, int col)
		{
			return data[row][col];
		}
		
		public void setValueAt(Object o, int row, int col)
		{
			data[row][col] = o;
			this.fireTableCellUpdated(row, col);
		}
	}

	public void addJob(String id, String stat, String jar, String input, String time, String mem)
	{
		
		jtm.addRow(id, stat, jar, input, time, mem);
		
	}
	
	public void updateJobStat(int jobID, String stat)
	{
		for(int i=0;i<jtm.getRowCount();i++)
		{
			if(jtm.getValueAt(i, 0).equals(new Integer(jobID).toString()))
			{
				jtm.setValueAt(stat, i, 1);
				break;
			}
		}
		this.jobTable.repaint();
	}
	
	public void removeJob(int jobID)
	{
		//TODO
	}
	
	public String getWorkerName(){return workerName;}
	public void setAddJobEnabled(boolean b){addJob.setEnabled(b);}
}
