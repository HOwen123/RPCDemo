import java.net.InetSocketAddress;

import client.RpcImporter;
import server.EchoService;
import server.EchoServiceImpl;
import server.RpcExporter;

public class RpcTest {
	public static void main(String[] args) {
		new Thread(new Runnable() {	
			@Override
			public void run() {
				try {
					System.out.println("服务端启用");
					RpcExporter.exporter("localhost", 8088);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
		RpcImporter<EchoService> importer = new RpcImporter<>();
		EchoService echoService = importer.importer(EchoServiceImpl.class, 
				new InetSocketAddress("localhost", 8088));
		System.out.println(echoService.echo("Are you ok?"));
	}
}
