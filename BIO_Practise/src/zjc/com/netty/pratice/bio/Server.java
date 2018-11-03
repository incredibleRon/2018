package zjc.com.netty.pratice.bio;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

/**
 * 无法控制上线下线 通话一次即永久发包
 * 
 * 
 */
public class Server {

	public StringBuffer buffer;

	/* 需要广播的对象 */
	public Vector<Address> ipList;

	public ServerSocket server;

	public void start() {
		buffer = new StringBuffer();
		ipList = new Vector<>();
		try {
			server = new ServerSocket(Constant.server_port);
			new Thread(new UdpSender(buffer, ipList)).start();
			while (true) {
				Socket socket = server.accept();
				if (!ipList.contains(socket.getInetAddress())) {
					System.out.println("新增ip:" + socket.getInetAddress().getHostAddress());
					System.out.println(ipList.size());
					Address address = new Address(server.getInetAddress(), server.getLocalPort());
					ipList.add(address);
				}
				new Thread(new TcpReceiver(socket, buffer)).start();
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("服务端初始化失败");
		} finally {
			if (null != server) {
				try {
					server.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}

/* 将消息存到buffer中 */
class TcpReceiver implements Runnable {
	public Socket socket;
	public StringBuffer buffer;

	public TcpReceiver(Socket socket, StringBuffer buffer) {
		this.socket = socket;
		this.buffer = buffer;
	}

	@Override
	public void run() {
		try {
			InputStream inputStream = socket.getInputStream();
			InputStreamReader isr = new InputStreamReader(inputStream, "utf-8");
			char[] buf = new char[1024];
			isr.read(buf);
			// isr.close();
			buffer.append(buf);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (null != socket) {
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}

/* 将buffer中积攒的消息广播出去，并清空buff（同步操作） */
class UdpSender implements Runnable {

	public StringBuffer buffer;

	public Vector<Address> ipList;

	public UdpSender(StringBuffer buffer, Vector<Address> ipList) {
		this.buffer = buffer;
		this.ipList = ipList;
	}

	@Override
	public void run() {
		while (true) {
			if (buffer.length() == 0) {
				continue;
			}
			byte[] contents = buffer.toString().getBytes();
			DatagramSocket datagramSocket = null;
			try {
				datagramSocket = new DatagramSocket(20001);
				synchronized (this) {
					for (Address address : ipList) {
						DatagramPacket response = new DatagramPacket(contents, contents.length, address.inetAddress,
								address.port-1);
						System.out.println("udpSend:" + buffer.toString());
						datagramSocket.send(response);
					}
					/* 清空 */
					buffer.setLength(0);
				}
				datagramSocket.close();
			} catch (Exception e) {
//				e.printStackTrace();
				continue;
			} finally {
				if (null != datagramSocket) {
					datagramSocket.close();
				}
			}
		}
	}
}

class Address{
	public InetAddress inetAddress;
	public int port;
	
	public Address(InetAddress inetAddress,int port) {
		this.inetAddress = inetAddress;
		this.port = port;
	}
	
}


