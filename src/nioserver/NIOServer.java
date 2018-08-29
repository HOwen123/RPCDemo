package nioserver;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class NIOServer {
	
	//通道管理器
	private Selector selector;
	/**
	 * 获得一个ServerSocket通道,并对该通道做一些初始化的工作
	 * 
	 * @param port 
	 * 			  绑定的端口号
	 * @throws IOException
	 */
	public void initServer(int port) throws IOException{
		//获得一个ServerSocker通道
		ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
		//设置通道为非阻塞的
		serverSocketChannel.configureBlocking(false);
		//将通道对应的serverSocket绑定到端口
		serverSocketChannel.socket().bind(new InetSocketAddress("127.0.0.1", port));
		//获得一个通道管理器
		this.selector = Selector.open();
		//将通道管理器和通道绑定，并为该通道管理器注册SelectionKey.OP_ACCEPT事件，注册该事件之后
		//当事件到达时，selector.select()会返回，如果该事件没到达selector.select()会一直阻塞
		serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
	}
	
	/**
	 * 采用<b>轮询</b>的方式监听selector上是否有需要处理的事件,如果有就进行处理
	 * @throws IOException
	 */
	public void listen() throws IOException{
		System.out.println("服务端启动成功！");
		
		while(true) {
			//注册的时间到达时，方法返回；否则，该方法会一直阻塞
			selector.select();
			//获得通道管理器中的token
			Iterator<?> ite = this.selector.selectedKeys().iterator();
			
			while(ite.hasNext()) {
				SelectionKey key = (SelectionKey)ite.next();
				//遍历以后就删去，不要影响后面的处理
				ite.remove();
				//将获取的token进行处理
				handler(key);
			}
		}
	}
	
	/**
	 * 处理请求
	 * @param key
	 * @throws IOException
	 */
	public void handler(SelectionKey key) throws IOException {
		//客户端请求连接事件
		if (key.isAcceptable()) {
			handlerAccept(key);
			//获得了可读的事件
		}else if (key.isReadable()) {
			handlerRead(key);
		}
	}
	
	/**
	 * 处理连接请求
	 * @param key
	 * @throws IOException
	 */
	public void handlerAccept(SelectionKey key) throws IOException{
		ServerSocketChannel server = (ServerSocketChannel) key.channel();
		//获得和客户端连接的通道
		SocketChannel channel = server.accept();
		//设置成非阻塞的
		channel.configureBlocking(false);
		
		System.out.println("接入新的客户端");
		//在和客户端连接成功之后，为了可以接收到客户端的信息，需要给通道设置读的token
		channel.register(this.selector, SelectionKey.OP_READ);
	
	}
	
	/**
	 * 处理读的事件
	 * @param key
	 * @throws IOException
	 */
	public void handlerRead(SelectionKey key) throws IOException{
		// 服务器可读取消息:得到事件发生的Socket通道
		SocketChannel channel = (SocketChannel)key.channel();
		// 创建读取的缓冲区
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		int read = channel.read(buffer);
		if (read > 0) {
			byte[] bytes = buffer.array();
			String msg = new String(bytes,0,read);
			
			System.out.println("服务端收的消息："+msg);
			
			ByteBuffer outBuffer = ByteBuffer.wrap("好的".getBytes());
			channel.write(outBuffer);
		}else {
			System.out.println("客户端关闭");
			key.cancel();
		}
	}
	
	public static void main(String [] args) throws IOException {
		NIOServer server = new NIOServer();
		server.initServer(8000);
		server.listen();
	}
}
