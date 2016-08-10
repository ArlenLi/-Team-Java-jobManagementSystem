package Core;

/**
 * the request format
 */
public class Request {

	protected RequestType requestType;
	protected int jobId;
	protected int maxMemory;
	protected int timeOut;

	public int getTimeOut() {
		return timeOut;
	}

	public void setTimeOut(int timeOut) {
		this.timeOut = timeOut;
	}

	public RequestType getRequestType() {
		return requestType;
	}

	public void setRequestType(RequestType requestType) {
		this.requestType = requestType;
	}

	public int getJobId() {
		return jobId;
	}

	public void setJobId(int jobId) {
		this.jobId = jobId;
	}

	public Request(RequestType requestType, int jobId) {
		this.requestType = requestType;
		this.jobId = jobId;
	}

	public Request(RequestType requestType, int jobId, int maxMemory, int timeOut) {
		this.requestType = requestType;
		this.jobId = jobId;
		this.timeOut = timeOut;
		this.maxMemory = maxMemory;
	}

	public Request(RequestType requestType) {
		this.requestType = requestType;
	}


	public int getMaxMemory() {
		return maxMemory;
	}

	public void setMaxMemory(int maxMemory) {
		this.maxMemory = maxMemory;
	}
}
