package win.simple;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Date;

public class Main {

	public static void main(String[] args) {
		try {
			ServerSocket serverSocket = new ServerSocket(2555);
			while(true) {
				Socket socket = serverSocket.accept();
				new dataThread(socket);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

class dataThread extends Thread {
	
	private Socket socket = null;
	private Socket WebSocket = null;
	private boolean WebServerConnectStat = false;
	
	public dataThread(Socket socket) {
		this.socket = socket;
		try {
			this.WebSocket = new Socket("127.0.0.1", 80);
			new jieshou(WebSocket, this.socket).start();
			this.start();
			WebServerConnectStat = true;
		} catch (ConnectException e) {
			ConnectError();
		} catch (UnknownHostException e) {
			ConnectError();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void ConnectError() {
		StringBuffer sbhtml = new StringBuffer();
		sbhtml.append("<html>");
		sbhtml.append("<head>");
		sbhtml.append("<title>Connect Error!</title>");
		sbhtml.append("</html>");
		sbhtml.append("<body>");
		sbhtml.append("<p>地址：" + this.socket.getInetAddress().getHostAddress() + " 无法连接至目标服务！</p>");
		sbhtml.append("</body>");
		sbhtml.append("</html>");
		
		StringBuffer sb = new StringBuffer();
		sb.append("HTTP/1.1 200 OK\r\n");
		sb.append("Server: Simple Server/1.0\r\n");
		sb.append("Connection: Keep-Alive\r\n");
		sb.append("Connection-Type: text/html; charset=utf-8\r\n");
		sb.append("Date: " + new Date() + "\r\n");
		sb.append("Content-Length: " + sbhtml.toString().length() + "\r\n");
		sb.append("\r\n");
		sb.append(sbhtml.toString());
		try {
			this.socket.getOutputStream().write(sb.toString().getBytes());
			this.socket.getOutputStream().flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		try {
			DataInputStream datainput = new DataInputStream(this.socket.getInputStream());
			String line = "";
			String Host = "";
			StringBuffer sb = new StringBuffer();
			while((line = datainput.readLine()) != null) {
				if(line.equals(" ")) {
					break;
				}else {
					if(line.indexOf("Host") != -1) {
						Host = line.substring(line.indexOf(" ") + 1, line.length());
						line = line.replace(Host, "127.0.0.1:80");
					}
					sb.append(line + "\r\n");
					this.WebSocket.getOutputStream().write((line + "\r\n").getBytes());
				}
			}
			this.WebSocket.getOutputStream().flush();
			
		}catch(SocketException e) {
			System.out.println("[*] 目标服务异常断开！");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

class jieshou extends Thread {
	
	private Socket socket = null;
	private Socket socket1 = null;
	
	public jieshou(Socket socket, Socket socket1) {
		this.socket = socket;
		this.socket1 = socket1;
	}
	
	@Override
	public void run() {
		byte[] bytes = new byte[1024];
		int len = 0;
		try {
			while((len = this.socket.getInputStream().read(bytes, 0, bytes.length)) > -1) {
				this.socket1.getOutputStream().write(bytes, 0, len);
			}
		}catch(SocketException e) {
			try {
				this.socket1.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
