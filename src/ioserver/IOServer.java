package ioserver;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Handler;

public class IOServer {
	public static void main(String[] args) throws IOException {
	   	@SuppressWarnings("resource")
		ServerSocket serverSocket = new ServerSocket();
		serverSocket.bind(new InetSocketAddress("127.0.0.1", 1000));
		Executor executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		
		if (!serverSocket.isClosed()) {
			System.out.println("服务器启动！！！");
		}
		
		while(true) {
			Socket socket = serverSocket.accept();
			System.out.println("有新的任务接入");
			executor.execute(new Runnable() {
				
				@Override
				public void run() {
					try {
						Handler(socket);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});
		}
		
	}

	protected static void Handler(Socket socket) throws IOException {
		try {
			byte[] bytes = new byte[1024];
			InputStream inputStream = socket.getInputStream();
			while(true) {
				int read = inputStream.read(bytes);
				if (read != -1) {
					System.out.println(new String(bytes, 0, read,"GBK"));
				}else {
					break ;
				}
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			System.out.println("socket 关闭");
			socket.close();
		}
	}
}
