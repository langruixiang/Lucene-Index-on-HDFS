package Client;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientThread extends Thread {	
	CountDownLatch mDoneSignal;//用于统计结束的线程数
	AtomicInteger errorCounter;
	
	public ClientThread(CountDownLatch mdonesignal){
		mDoneSignal = mdonesignal;
	}
	
	@Override
	public void run(){
		//用户连接负载均衡服务器
		ClientBalanceThread clientThread = new ClientBalanceThread("/home/lang/workspace/BigWeb/config.xml");
		clientThread.start();
		
		try {
			clientThread.join();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		String searchServerIP = clientThread.GetSearchServerIP();
		
		ClientSearch searchSearch = new ClientSearch(searchServerIP);
		
		//开始搜索,每个用户10个搜索
		int numOfSearch = 10;
		
		for(int i = 0; i < numOfSearch; i++){
			searchSearch.DoSearch("世界上有三个苹果 一个砸到了牛顿 一个被乔布斯吃了一口 一个改变了音乐界. Iphone6发布 三星s6发布 edge有什么用?!");
		}
		
		mDoneSignal.countDown();
	}

}
