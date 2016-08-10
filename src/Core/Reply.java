
package Core;

/**
 * the reply format
 */
public class Reply {

    protected int jobId;
    protected String content;

    public Reply(String content, int jobId) {
        this.content = content;
        this.jobId = jobId;
    }

    public int getJobId() {
        return jobId;
    }

    public void setJobId(int jobId) {
        this.jobId = jobId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
