
package Core;



import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

import org.json.simple.parser.JSONParser;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;


import com.google.gson.Gson;

import filesync.BlockUnavailableException;
import filesync.CopyBlockInstruction;
import filesync.Instruction;
import filesync.InstructionFactory;
import filesync.NewBlockInstruction;
import filesync.SynchronisedFile;

public class Worker {
	private static SSLServerSocket listenSocket; 

	@Option(name="-p", usage="Sets a port", required=false)
	private static int serverPort = 4444; // the server port
	private static int currentJobNum;
	protected static final JSONParser parser = new JSONParser();
	protected static final Gson gson = new Gson();


	public synchronized static void incrementCurrentJobNum() {
		Worker.currentJobNum++;
	}

	public synchronized static void decreaseCurrentJobNum() {
		Worker.currentJobNum--;
	}

	public static int getCurrentJobNum() {
		return currentJobNum;
	}

	public static void setCurrentJobNum(int currentJobNum) {
		Worker.currentJobNum = currentJobNum;
	}



	public static void main(String args[]) throws UnknownHostException,
	IOException {

		System.setProperty("javax.net.ssl.keyStore", "test"); //WEN GUO
		System.setProperty("javax.net.ssl.keyStorePassword", "testtest"); //WEN GUO

		SSLServerSocketFactory factory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault(); //WEN GUO		

		Worker worker = new Worker();
		CmdLineParser parser = new CmdLineParser(worker);
		try {
			parser.parseArgument(args);
		} catch (CmdLineException e) {
			e.printStackTrace();
			// handling of wrong arguments
			System.err.println(e.getMessage());
			parser.printUsage(System.err);
		}

		try {

			listenSocket = (SSLServerSocket) factory.createServerSocket(serverPort); //WEN GUOO

			int i = 0;
			while (true) {
				System.out.println("Server listening for a connection on "
						+ serverPort);				

				SSLSocket masterSocket = (SSLSocket) listenSocket.accept();

				System.out.println("Received connection " + ++i);
				worker.new WorkerThread(masterSocket);
			}
		} catch (IOException e) {
			System.out.println("Listen socket:" + e.getMessage());
		}
	}



	class WorkerThread extends Thread {

		private SSLSocket masterSocket;

		private HashMap<Integer, JobMonitor> jobMap = new HashMap<Integer, JobMonitor>();
		private DataInputStream in;
		private DataOutputStream out;
		private String masterAddress;
		private int masterPort;
		private File masterWorkSpace;

		public WorkerThread(SSLSocket masterSocket) {

			this.masterSocket = masterSocket;
			try {
				this.in = new DataInputStream(masterSocket.getInputStream());
				this.out = new DataOutputStream(masterSocket.getOutputStream());
				this.masterAddress = masterSocket.getInetAddress().toString().substring(1);
				this.masterPort = masterSocket.getPort();
				String masterWorkSpaceDirectory = "Jobs" + File.separator + "Master_"+ masterAddress + "_" + String.valueOf(masterPort);
				this.masterWorkSpace = new File(masterWorkSpaceDirectory);
				if (!masterWorkSpace.exists()) {
					masterWorkSpace.mkdir();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			this.start();
		}

		@Override
		public void run() {
			try {
				while (true) {
					String requestMessage = in.readUTF();
					System.out.println(requestMessage);
					Request request = gson.fromJson(requestMessage,
							Request.class);

					int jobId = 0;
					Reply reply;

					switch (request.getRequestType()) {
					// request the current job number
					case WORKERSTATUS:
						reply = new Reply("Connected", 0)	;
						out.writeUTF(gson.toJson(reply));
						break;
					case CURRENTJOBNUM:
						reply = new Reply(String.valueOf(getCurrentJobNum()), 0);
						out.writeUTF(gson.toJson(reply));
						break;
					case CREATE:
						Worker.incrementCurrentJobNum();
						System.out.println("<increment Num>>>>>>>>>"+Worker.currentJobNum);
						jobId = request.getJobId();
						int maxMemory = request.getMaxMemory();
						int timeOut = request.getTimeOut();
						//Create JobProcess
						JobMonitor jobMonitor = new JobMonitor(masterWorkSpace, jobId, this.jobMap);
						jobMonitor.setTimeOut(timeOut);
						jobMonitor.setMaxMemory(maxMemory);

						String jarFileName = in.readUTF();
						jobMonitor.setJarFileName(jarFileName);
						//Receive Jar and Input files
						File targetJar = new File(jobMonitor.getJobDirectory()+ File.separator + jarFileName);
						targetJar.createNewFile();
						receiveFile(targetJar);

						String inputFileName = in.readUTF();
						jobMonitor.setInputFileName(inputFileName);
						File targetInput = new File(jobMonitor.getJobDirectory()+ File.separator + inputFileName);
						targetInput.createNewFile();
						receiveFile(targetInput);
						//Start JobProcess
						jobMonitor.start();
						break;
					case JOBSTATE:
						jobId = request.getJobId();
						// ��ѯ��ǰjob��״̬
						jobMonitor = jobMap.get(jobId);
						String jobDirectory = jobMonitor.getJobDirectory().getPath();
						JobMonitor.JobStatus jobState = jobMonitor.getJobStatus();
						if (JobMonitor.JobStatus.JOB_PENDING == jobState){
							out.writeUTF(gson.toJson(new Reply("JOB_PENDING", jobId)));
						}
						else if (JobMonitor.JobStatus.JOB_RUNNING == jobState){
							out.writeUTF(gson.toJson(new Reply("JOB_RUNNING", jobId)));
						}
						else if(JobMonitor.JobStatus.JOB_FINISHED == jobState){
							out.writeUTF(gson.toJson(new Reply("JOB_FINISHED", jobId)));
							File file = new File(jobDirectory+File.separator+"output.dat");
							transferFile(file);
						}
						else if (JobMonitor.JobStatus.JOB_FAILURE == jobState){
							out.writeUTF(gson.toJson(new Reply("JOB_FAILURE",jobId)));
							File file = new File(jobDirectory+File.separator+"errorOutput.dat");
							transferFile(file);
						}
						break;
						// case ABORT_JOB:
						// break;
					default:
						break;
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
				//TODO: out.writeUTF("");
			}
		}

		// receive file
		private void receiveFile(File file) {
			Gson gson = new Gson();
			try {
				SynchronisedFile synfile = new SynchronisedFile(
						file.getAbsolutePath());
				Instruction inst;
				InstructionFactory instFact = new InstructionFactory();
				boolean flag = true;
				Instruction receivedInst = null;
				while (flag) {
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
							System.exit(-1); // just die at the first sign of
							// trouble
						} catch (BlockUnavailableException e) {
							// The server does not have the bytes referred to by
							// the block hash.
							try {
								out.writeUTF(gson.toJson(new Reply("NEWBLOCK",
										0)));
								String receivedMsg2 = in.readUTF();
								Instruction receivedInst2 = instFact
										.FromJSON(receivedMsg2);
								synfile.ProcessInstruction(receivedInst2);
							} catch (IOException e1) {
								e1.printStackTrace();
								System.exit(-1);
							} catch (BlockUnavailableException e1) {
								assert (false); // a NewBlockInstruction can
								// never throw this exception
							}
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// transfer the file
		private void transferFile(File file) {
			Gson gson = new Gson();
			try {
				SynchronisedFile synfile = new SynchronisedFile(
						file.getAbsolutePath());
				Instruction inst;
				synfile.CheckFileState();

				boolean flag = true;
				if ((inst = synfile.NextInstruction()) != null) {
					while (flag) {
						String msg = inst.ToJSON();
						try {
							out.writeUTF(msg);
							String returnMsg = in.readUTF();
							Reply reply = gson.fromJson(returnMsg, Reply.class);

							if (reply.getContent().equals("NEWBLOCK")) {
								Instruction upgraded = new NewBlockInstruction(
										(CopyBlockInstruction) inst);
								String msg2 = upgraded.ToJSON();
								out.writeUTF(msg2);
							}
						} catch (IOException e) {
							e.printStackTrace();
							System.exit(-1); // just die at the first sign of
							// trouble
						}
						inst = synfile.NextInstruction();
						if (inst.Type() == "EndUpdate") {
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
			} catch (InterruptedException e) {
				e.printStackTrace();
				System.exit(-1);
			}

		}
	}
}