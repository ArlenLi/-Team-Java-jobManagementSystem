package GUI;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import Core.Master;

public class MenuBar extends JMenuBar{

	private JMenu action;
	private JMenuItem addWorker;
	
	private GUInterface frame;
	private MainPanel parent;
	private Master master;
	
	public MenuBar(GUInterface frame,MainPanel parent,Master master)
	{
		//
		this.frame = frame;
		this.parent = parent;
		this.master = master;
		
		//initialise
		action = new JMenu("Action");
		action.setMnemonic(KeyEvent.VK_A);
		
		addWorker = new JMenuItem("Add worker");
		addWorker.setToolTipText("Add a worker");
		addWorker.addActionListener(new CloudMenuItemListener());
		
		
		//add components
		action.add(addWorker);
		this.add(action);
		
	}
	
	//menu item listener
	private class CloudMenuItemListener implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			if(e.getSource() == addWorker)
			{
				JTextField addTF = new JTextField();
				JTextField portTF = new JTextField();
				Object[] options = {"Address:", addTF,"Port:",portTF};

				int option = JOptionPane.showConfirmDialog(
						parent,
						options,
						"Add worker",
						 JOptionPane.OK_CANCEL_OPTION
						);
				if(option == JOptionPane.OK_OPTION)
				{
					//TODO - should make connection here
					System.out.println(addTF.getText().trim()+":"+portTF.getText());
					master.addWorker(addTF.getText().trim(), Integer.parseInt(portTF.getText()));
				}
				
			}
		}
	}
}
