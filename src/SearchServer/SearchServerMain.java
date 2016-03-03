package SearchServer;

import java.io.IOException;

public class SearchServerMain {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		SearchServer searchServer = new SearchServer("/home/lang/workspace/BigWeb/config.xml","searcherServer2");
				
		UpdateIndexThread searchServerThread = new UpdateIndexThread(searchServer);
		searchServerThread.start();
		
		SearchThread searchThread = new SearchThread(searchServer);
		searchThread.start();
	}
}
