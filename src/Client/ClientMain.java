package Client;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientMain { 
	public static AtomicInteger errorCounter = new AtomicInteger(0);//用于错误计数
	
	public static void main(String []args) throws IOException{
		int TOTAL_THREADS = 200;   
		CountDownLatch mDoneSignal = new CountDownLatch(TOTAL_THREADS);  //结束线程计数
		long beginTime;
	    long endTime;
	    long costTime = 0; //ms为单位
	    
//	    int numOfUsers = 200;
	    
	    beginTime = System.nanoTime();		
		ClientThread []clients  = new ClientThread[TOTAL_THREADS];
		for(int i = 0; i < TOTAL_THREADS; i++){
			clients[i] = new ClientThread(mDoneSignal);
			clients[i].start();
		}  
   
        try  
        {  
            mDoneSignal.await();   
        }  
        catch (InterruptedException e)  
        {  
            // TODO Auto-generated catch block   
            e.printStackTrace();  
        }  
		
		endTime = System.nanoTime();
		costTime = (endTime - beginTime)/1000000/(TOTAL_THREADS*10);
		System.out.println("共计用时:" + costTime);
		System.out.println("错误率:" + (errorCounter.get()/(TOTAL_THREADS*10*1.0)));
	}
}
