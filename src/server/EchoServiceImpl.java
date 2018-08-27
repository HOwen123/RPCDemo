package server;

public class EchoServiceImpl implements EchoService{

	@Override
	public String echo(String ping) {
		return ping != null ? ping +"----> I'm ok":"I'm ok";
	}

}
