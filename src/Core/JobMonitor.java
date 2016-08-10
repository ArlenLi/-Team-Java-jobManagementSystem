package Core;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/**
 * JobMonitor
 * Process for job is included in this Thread JobMonitor
 * Default Value of Timeout is 20s
 * Default Value of maxMemory is 8m
 * @author xingyuji
 *
 */
public class JobMonitor extends Thread {
	private String maxMemory = "-Xmx8m";
	private File jobDirectory;
	private JobStatus jobStatus = JobStatus.JOB_PENDING;
	private String jarFileName;
	private int timeOut = 20;
	private Scanner scanner;
	private BufferedWriter writeFile;
	private String inputFileName = "input.dat";
	private String outputFileName = "output.dat";

	protected enum JobStatus {
		JOB_PENDING, JOB_RUNNING, JOB_FAILURE, JOB_FINISHED
	}

	public JobMonitor(File masterWorkSpace, int jobID, HashMap<Integer, JobMonitor> jobMap) {
		jobMap.put(jobID, this);
		this.jobDirectory = new File(masterWorkSpace.getPath() + File.separator + "Job_"+String.valueOf(jobID));
		if (!jobDirectory.exists()) {
			jobDirectory.mkdirs();
		}
		this.setDaemon(true);
	}

	@Override
	public void run() {
		ProcessBuilder jobProcess = new ProcessBuilder("java", maxMemory, "-jar", jarFileName, inputFileName, outputFileName);
		// Set workspace
		jobProcess.directory(new File(jobDirectory.getPath()));
		// merge std-output and error-output
		jobProcess.redirectErrorStream(true);
		try {
			Process jobRuntime = jobProcess.start();
			//update Worker and WorkerThread status
			this.setJobStatus(JobStatus.JOB_RUNNING);
			if(jobRuntime.waitFor(timeOut, TimeUnit.SECONDS)) {
				if (0 == jobRuntime.exitValue()) {
					this.setJobStatus(JobStatus.JOB_FINISHED);
					File output = new File(jobDirectory.getPath()+File.separator+outputFileName);
					if(!output.exists()){
						output.createNewFile();
					}
				} else {
					this.setJobStatus(JobStatus.JOB_FAILURE);
					String errorOutput = recordOutputInfo(jobRuntime.getInputStream(), "errorOutput.dat", jobRuntime.exitValue());
					System.out.println("errorOutput: "+errorOutput);
				}
			}else{
				//Time out destroy job process and set job failure
				this.setJobStatus(JobStatus.JOB_FAILURE);
				String info = recordOutputInfo("Job Process Time Out!"+ System.getProperty("line.separator") 
						+ "Time Out Convention (SECONDS):" + timeOut, "errorOutput.dat");
				System.out.println(info);
				jobRuntime.destroyForcibly();
			}
		} catch (IOException | InterruptedException e) {
			try {
				e.printStackTrace();
				this.setJobStatus(JobStatus.JOB_FAILURE);
				recordOutputInfo("External Exception Occur When Job In Process! " + System.getProperty("line.separator")
						+e.getMessage().toString(), "errorOutput.dat");
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} finally{
			Worker.decreaseCurrentJobNum();
		}
	}

	private String recordOutputInfo (InputStream stream, String fileName, int exitValue) throws IOException{
		StringBuffer info = new StringBuffer();
		scanner = new Scanner(stream);
		File infoFile = new File(jobDirectory.getPath()+File.separator+fileName);
		infoFile.createNewFile();
		info.append("Job Process Exit Code: "+exitValue+System.getProperty("line.separator"));
		while (scanner.hasNextLine()) {
			info.append(scanner.nextLine());
			info.append(System.getProperty("line.separator"));
		}
		writeFile = new BufferedWriter(new FileWriter(infoFile));
		writeFile.write(info.toString());
		writeFile.flush();
		writeFile.close();
		scanner.close();
		return info.toString();
	}

	private String recordOutputInfo (String content, String fileName) throws IOException{
		StringBuffer info = new StringBuffer();
		File infoFile = new File(jobDirectory+File.separator+fileName);
		infoFile.createNewFile();
		info.append(content);
		writeFile = new BufferedWriter(new FileWriter(infoFile));
		writeFile.write(info.toString());
		writeFile.flush();
		writeFile.close();
		return info.toString();
	}

	public File getJobDirectory() {
		return jobDirectory;
	}

	public void setJobDirectory(File jobDirectory) {
		this.jobDirectory = jobDirectory;
	}

	public JobStatus getJobStatus() {
		return jobStatus;
	}

	public void setJobStatus(JobStatus jobStatus) {
		this.jobStatus = jobStatus;
	}

	public String getMaxMemory() {
		return maxMemory;
	}

	public void setMaxMemory(int maxMemory) {
		maxMemory = maxMemory <= 0?8:maxMemory;
		this.maxMemory = "-Xmx"+maxMemory+"m";
	}

	public int getTimeOut() {
		return timeOut;
	}

	public void setTimeOut(int timeOut) {
		this.timeOut = timeOut <= 0?20:timeOut;
	}

	public String getInputFileName() {
		return inputFileName;
	}

	public void setInputFileName(String inputFileName) {
		this.inputFileName = inputFileName;
	}

	public String getOutputFileName() {
		return outputFileName;
	}

	public void setOutputFileName(String outputFileName) {
		this.outputFileName = outputFileName;
	}

	public String getJarFileName() {
		return jarFileName;
	}

	public void setJarFileName(String jarFileName) {
		this.jarFileName = jarFileName;
	}
}
