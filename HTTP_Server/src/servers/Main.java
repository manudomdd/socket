package servers;

public class Main {
		final static int PORT = 9090;
	
	
	public static void main(String[] args) {
		
		
		
		SingleThreadedServer server = new SingleThreadedServer(PORT);
		new Thread(server).start();
		System.out.println("Start server http://localhost:"+PORT);
		try {
			Thread.sleep(10000 * 10000);
		}catch(InterruptedException ex) {
			System.err.print(ex);
		}
		System.out.println("Stopping server");
		server.stop();
	}

}
