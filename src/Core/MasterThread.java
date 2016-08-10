
package Core;



import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;

import javax.net.ssl.SSLSocket;

import GUI.GUInterface;

import com.google.gson.Gson;

import filesync.BlockUnavailableException;
import filesync.CopyBlockInstruction;
import filesync.Instruction;
import filesync.InstructionFactory;
import filesync.NewBlockInstruction;
import filesync.SynchronisedFile;

/**
 * Created by lld on 15/5/16.
 */
public class MasterThread implements Runnable{
	File jarFile;
	File inputFile;
	Socket socket;
	int jobId;
	DataInputStream in;
	DataOutputStream out;
	int maxMemory;
	int timeOut;

	private GUInterface gui;

	public MasterThread(File jarFile, File inputFile, SSLSocket socket, int jobId,int maxMemory, int timeOut, GUInterface gui) {
		this.jarFile = jarFile;
		this.inputFile = inputFile;
		this.socket = socket;
		this.jobId = jobId;
		this.maxMemory = maxMemory;
		this.timeOut = timeOut;
		this.gui =  gui;
		try {
			in = new DataInputStream( this.socket.getInputStream());
			out =new DataOutputStream( this.socket.getOutputStream());

		} catch (IOException e) {
			e.printStackTrace();
		}
		
		String workerName = socket.getInetAddress().toString().replaceFirst("/", "") + ":" + new Integer(socket.getPort()).toString();
		gui.addJob(workerName,new Integer(jobId).toString(), "PENDING", jarFile.getAbsolutePath(), inputFile.getAbsolutePath(), new Integer(timeOut).toString(), new Integer(maxMemory).toString());
	}

	// transfer the file
	private void transferFile(File file) {
		Gson gson = new Gson();
		try {
			SynchronisedFile synfile = new SynchronisedFile(file.getAbsolutePath());
			Instruction inst;
			synfile.CheckFileState();

			boolean flag = true;
			if((inst = synfile.NextInstruction()) != null) {
				while(flag) {
					String msg = inst.ToJSON();
					try {
						out.writeUTF(msg);
						String returnMsg = in.readUTF();
						Reply reply = gson.fromJson(returnMsg, Reply.class);

						if (reply.getContent().equals("NEWBLOCK")) {
							Instruction upgraded = new NewBlockInstruction((CopyBlockInstruction) inst);
							String msg2 = upgraded.ToJSON();
							out.writeUTF(msg2);
						}
					} catch (IOException e) {
						e.printStackTrace();
						System.exit(-1); // just die at the first sign of trouble
					}
					inst = synfile.NextInstruction();
					if(inst.Type() == "EndUpdate") {
						flag = false;
						msg = inst.ToJSON();
						try {
							out.writeUTF(msg);
							String returnMsg = in.readUTF();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}catch (InterruptedException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	// receive file
	private void receiveFile(File file) {
		Gson gson = new Gson();
		try {
			SynchronisedFile synfile = new SynchronisedFile(file.getAbsolutePath());
			Instruction inst;
			InstructionFactory instFact=new InstructionFactory();
			boolean flag = true;
			Instruction receivedInst = null;
			while(flag) {
				String receivedMsg = in.readUTF();
				receivedInst = instFact.FromJSON(receivedMsg);
				if (receivedInst.Type() == "StartUpdate") {
					try {
						synfile.ProcessInstruction(receivedInst);
					} catch (BlockUnavailableException e) {
						e.printStackTrace();
					}
					out.writeUTF(gson.toJson(new Reply("OK", 0)));
				} else if (receivedInst.Type() == "EndUpdate") {
					try {
						synfile.ProcessInstruction(receivedInst);

					} catch (BlockUnavailableException e) {
						e.printStackTrace();
					}
					out.writeUTF(gson.toJson(new Reply("OK", 0)));
					// stop the loop
					flag = false;
				} else {
					try {
						// The Server processes the instruction

						synfile.ProcessInstruction(receivedInst);
						out.writeUTF(gson.toJson(new Reply("OK", 0)));

					} catch (IOException e) {
						e.printStackTrace();
						System.exit(-1); // just die at the first sign of trouble
					} catch (BlockUnavailableException e) {
						// The server does not have the bytes referred to by the block hash.
						try {
							out.writeUTF(gson.toJson(new Reply("NEWBLOCK", 0)));
							String receivedMsg2 = in.readUTF();
							Instruction receivedInst2 = instFact.FromJSON(receivedMsg2);
							synfile.ProcessInstruction(receivedInst2);
						} catch (IOException e1) {
							e1.printStackTrace();
							System.exit(-1);
						} catch (BlockUnavailableException e1) {
							assert (false); // a NewBlockInstruction can never throw this exception
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	@Override
	public void run() {
		boolean flag = true;
		Gson gson = new Gson();
		String replyMessage;
		synchronized (socket) {
			
			Request request = new Request(RequestType.CREATE, jobId, maxMemory, timeOut);
			try {
				out.writeUTF(gson.toJson(request));
				out.writeUTF(jarFile.getName());
				transferFile(jarFile);
				out.writeUTF(inputFile.getName());
				transferFile(inputFile);
				System.out.println("**********____________");
				//                Reply reply = gson.fromJson(in.readUTF(),Reply.class); // get the state of the job
				//                System.out.println("**********" + reply.getContent());

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		while(flag) {
			Request request = new Request(RequestType.JOBSTATE, jobId);
			System.out.println(request.toString());
			String workerName = socket.getInetAddress().toString().replaceFirst("/", "") + ":" + new Integer(socket.getPort()).toString();
			
			try {
				synchronized (socket) {
					out.writeUTF(gson.toJson(request));
					replyMessage = in.readUTF();

					Reply reply = gson.fromJson(replyMessage, Reply.class);
					System.out.println(reply.toString());
					System.out.println(reply.getContent());
					File jobDirectory = new File("output" + File.separator +"Job_"+ String.valueOf(jobId));
                    if(!jobDirectory.exists()) {
                        jobDirectory.mkdirs();
                    }
                    
					if ("JOB_RUNNING".equals(reply.getContent())) {
						// set Running
						gui.updateJobStat(workerName,jobId, "RUNNING");  // TODO
					
					}
					else if ("JOB_FAILURE".equals(reply.getContent())) {                    	
						gui.updateJobStat(workerName,jobId, "FAILURE"); // TODO
												
						File file = new File(jobDirectory.getPath() + File.separator +
								"errorInfo.dat");
						file.createNewFile();
						receiveFile(file);
						flag = false;
					}
					else if ("JOB_FINISHED".equals(reply.getContent())) { 
						// TODO
						gui.updateJobStat(workerName,jobId, "FINISHED");									
						
						File file = new File(jobDirectory.getPath() + File.separator +
								"output.dat");
						file.createNewFile();
						receiveFile(file);
						flag = false;


					}
					else if("JOB_PENDING".equals(reply.getContent())) 
					{
						//TODO
						gui.updateJobStat(workerName,jobId, "PENDING");						
					}
				}
				try {
					Thread.sleep(SystemConstant.interval);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} catch (IOException e) {
				// set Disconnected
				flag = false;
				gui.updateJobStat(workerName,jobId, "FAILURE");
			}
		}
	}
}
