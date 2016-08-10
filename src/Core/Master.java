
package Core;


import GUI.GUInterface;

import com.google.gson.Gson;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.util.*;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class Master {

	protected int jobId = 0; // the id of the job
	protected List<SSLSocket> workerList = new ArrayList<SSLSocket>(); 	

	private static final String wokerListPath = "workerList";

	private GUInterface gui;

	public Master(GUInterface gui)
	{
		//certificate 
		System.setProperty("javax.net.ssl.trustStore", "test"); //WEN GUO
		System.setProperty("javax.net.ssl.trustStorePassword", "testtest");//WEN GUO

		//
		this.gui = gui;
	}


	public void readFile()
	{
		//read worker list  file
		File file = new File(wokerListPath);
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e) {

			e.printStackTrace();
		}

		Scanner scanner;
		String tempString = null;

		//add workers
		try
		{
			while ((tempString = reader.readLine()) != null)
			{
				try
				{
					//read address and port
					scanner = new Scanner(tempString);
					String internetAdd = scanner.next();
					int port = scanner.nextInt();

					//create socket
					SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();  
					gui.log("Connecting to:" + internetAdd + ":" + port );
					gui.addWorker(internetAdd,port);
					
					SSLSocket s= (SSLSocket) factory.createSocket(internetAdd, port); 

					//add
					workerList.add(s);						
					Thread connectionListener = new Thread(new ConnectionListener(s,gui,workerList));
					connectionListener.setDaemon(true);
					connectionListener.start();
					
					s.setSoTimeout(SystemConstant.timeout);
					
				
					gui.setWorkerStatus(internetAdd+":"+port,null,"CONNECTED");
					gui.log("Connected");
					
				}
				catch(ConnectException ce)
				{
					gui.log(ce.toString());					
					gui.log("Connection failed.");
					ce.printStackTrace();
				}	
				
			}
		}
		catch(IOException ioe)
		{
			gui.log(ioe.toString());
			//ioe.
			ioe.printStackTrace();
		}		
		finally{
			if (reader != null) {
				try{
					reader.close();
				}
				catch (IOException e1)
				{
				}
			}
		}
	}

	//set get
	public List<SSLSocket> getWorkerList() {return workerList;}
	public void setWorkerList(ArrayList<SSLSocket> workerList) {this.workerList = workerList;}

	public int getJobId() {
		jobId++;
		return jobId;
	}

	public void setJobId(int jobId) {
		this.jobId = jobId;
	}

	//helper
	public void addJob(String jarPath, String inputPath,int maxMemory, int timeOut)
	{
		File jar = new File(jarPath);
		File input = new File(inputPath);
		if (jar.isFile() && input.isFile())
		{
			Thread thread = new Thread(
					new MasterThread(
							jar,
							input,
							this.properWorker(),
							this.getJobId(),
							maxMemory,
							timeOut,
							gui));
			thread.setDaemon(true);
			thread.start();
		} else {
			System.err.println("jarFile or inputFile is not valid");
		}
	}

	public void addWorker(String address, int port)
	{
		try {		
			SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
			SSLSocket s = (SSLSocket) factory.createSocket(address, port);
			gui.log("Connecting to:" + address + ":" + port );
			gui.addWorker(address,port);
			
			this.workerList.add(s);
			Thread connectionListener = new Thread(new ConnectionListener(s,gui,workerList));
			connectionListener.setDaemon(true);
			connectionListener.start();
			
			s.setSoTimeout(SystemConstant.timeout);	
				
			gui.setWorkerStatus(address+":"+port,null,"CONNECTED");
			gui.log("Connected");
			
		} catch (IOException e) {
			gui.log(e.toString());
			gui.log("Connection failed.");
			e.printStackTrace();
		}
	}

	public SSLSocket properWorker() 
	{  
		DataOutputStream out = null;
		DataInputStream in = null;
		Gson gson = new Gson();
		int lessJobNum = 0;
		SSLSocket socket = null; 
		String replyMessage;
		for (int i = 0; i < workerList.size(); i++) {
			try {
				synchronized (workerList.get(i)) 
				{
					in = new DataInputStream(workerList.get(i).getInputStream());
					out = new DataOutputStream(workerList.get(i)
							.getOutputStream());
					Request request = new Request(RequestType.CURRENTJOBNUM, 0);
					out.writeUTF(gson.toJson(request));
					replyMessage = in.readUTF();
				}
				Reply reply = gson.fromJson(replyMessage, Reply.class);
				int currentJobNum = Integer.valueOf(reply.getContent())
						.intValue();
				if (i == 0) {
					lessJobNum = currentJobNum;
					socket = workerList.get(i);
				} else {
					if (currentJobNum < lessJobNum) {
						lessJobNum = currentJobNum;
						socket = workerList.get(i);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return socket;
	}
}
