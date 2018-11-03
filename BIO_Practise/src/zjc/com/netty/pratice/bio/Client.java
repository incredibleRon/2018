package zjc.com.netty.pratice.bio;

import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;

public class Client {

	public String name;
	
	public int port;
	
	public Client(String name,int port) {
		this.name = name;
		this.port = port;
	}

	public void start() {
		new Thread(new TcpSender(this)).start();
		new Thread(new UdpReceiver(this)).start();
	}

}

class TcpSender implements Runnable {

	public Client client;

	public TcpSender(Client client) {
		this.client = client;
	}

	@Override
	public void run() {
		Socket socket = null;
		while (true) {
			try {
				Thread.sleep(1000);
				socket = new Socket(Constant.ip, client.port);
				OutputStream out = socket.getOutputStream();
				String words = "Client_" + client.name + ":" + System.currentTimeMillis();
				System.out.println("tcpSend：" + words);
				out.write(words.getBytes());
				out.close();
				socket.close();

			} catch (Exception e) {
		e.printStackTrace(); 
				continue;
			} finally {
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}

class UdpReceiver implements Runnable {
	
	public Client client;
	
	public UdpReceiver(Client client) {
		this.client = client;
	}
	
	@Override
	public void run() {
		DatagramSocket datagramSocket = null;
		try {
			datagramSocket = new DatagramSocket(client.port-1, InetAddress.getByName(Constant.ip));
			while (true) {
				/*** 接收数据 ***/
				byte[] receBuf = new byte[1024];
				DatagramPacket recePacket = new DatagramPacket(receBuf, receBuf.length);
				datagramSocket.receive(recePacket);
				String receStr = new String(recePacket.getData(), 0, recePacket.getLength());
				System.out.println("udpReceive:" + receStr);
			}
		} catch (Exception e) {
			if (null != datagramSocket) {
				datagramSocket.close();
			}
		}
	}

}
