package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class RpcExporter {
	//根据cpu内核数创建线程池
	static Executor executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
	
	//暴露接口
	public static void exporter(String hostName,int port) throws IOException {
		ServerSocket server = new ServerSocket();
		//绑定一个网络地址
		server.bind(new InetSocketAddress(hostName, port));
		
		try {
			//监听socket的连接，如果没有连接上则一直属于阻塞状态,连接后返回一个新的socket
			while(true) {
				executor.execute(new ExporterTask(server.accept()));
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			server.close();
		}
	}
	
	//暴露任务
	private static class ExporterTask implements Runnable{
		Socket client = null;
		public ExporterTask(Socket client) {
			this.client = client;
		}
		
		@Override
		public void run() {
			ObjectInputStream input = null;
			ObjectOutputStream output = null;
			
			try {
				//接收客户端发来的信息
				input = new ObjectInputStream(client.getInputStream());
				//从输入流中以UTF-8的字符集读取接口名
				String interfaceName = input.readUTF();
				//根据接口名获取类名
				Class<?> service = Class.forName(interfaceName);
				//读取方法名
				String methodName = input.readUTF();
				//读取参数名
				Class<?>[] parameterTypes = (Class<?>[]) input.readObject();
				//读取参数名
				Object[] arguments = (Object []) input.readObject();
				//获取类参数列表
				Method method = service.getMethod(methodName, parameterTypes);
				//创建实例
				Object result = method.invoke(service.newInstance(), arguments);
				//将实例返回给客户端
				output = new ObjectOutputStream(client.getOutputStream());
				output.writeObject(result);
			}catch (Exception e) {
				e.printStackTrace();
			}finally {
				if (output != null) {
					try {
						output.close();
					} catch (IOException e2) {
						e2.printStackTrace();
					}
					if (input != null) {
						try {
							input.close();
						} catch (IOException e2) {
							e2.printStackTrace();
						}
						
					}
					
					if (client != null) {
						try {
							client.close();
						} catch (Exception e2) {
							e2.printStackTrace();
						}
						
					}
				}
			}
		}
	}
	
}
