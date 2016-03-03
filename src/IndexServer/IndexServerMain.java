package IndexServer;

public class IndexServerMain {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		IndexServer indexServer = new IndexServer("/home/lang/workspace/BigWeb/config.xml","indexServer1");
				
		IndexServerThread indexServerThread = new IndexServerThread(indexServer);
		indexServerThread.start();		
	}
}
