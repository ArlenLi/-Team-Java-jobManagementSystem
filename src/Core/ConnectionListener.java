package Core;

import GUI.GUInterface;

import com.google.gson.Gson;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;

import javax.net.ssl.SSLSocket;


public class ConnectionListener extends Thread {
	private SSLSocket socket;
	private GUInterface gui;
	private List<SSLSocket> workerList;

	public ConnectionListener(SSLSocket socket,GUInterface gui, List<SSLSocket> workerList) {
		this.socket = socket;
		this.gui = gui;
		this.workerList = workerList;
	}

	public void run() {
		boolean status = true;
		DataInputStream in = null;
		DataOutputStream out = null;
		try {
			in = new DataInputStream( this.socket.getInputStream());
			out =new DataOutputStream( this.socket.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
		while(status){
			//synchronized (socket) {
			try{
				Gson gson = new Gson();
				String replyMessage;
				Request request = new Request(RequestType.WORKERSTATUS);

				synchronized (socket) {

					out.writeUTF(gson.toJson(request));
					in.readUTF();
				}
				Thread.sleep(SystemConstant.interval);
			}  catch (IOException e) {
				// TODO Auto-generated catch block
				// set work down
				System.out.println("Worker disconnected.");

				String workerName = socket.getInetAddress().toString().replaceFirst("/", "") + ":" + new Integer(socket.getPort()).toString();
				gui.setWorkerStatus(workerName, null, "DISCONNECTED");
				gui.log(workerName + " disconnected due to " + e.getMessage());
				status = false;
				for (int i = 0; i < workerList.size(); i++) {
					workerList.remove(socket);
				}

			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}
}

