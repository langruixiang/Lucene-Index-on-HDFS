package Client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import Net.NetCmd;

public class ClientSearch{
    private String searchWords;
    private String searchServerIP;
    
	private static final int port = 8888;	     
    private Charset cs = Charset.forName("utf-8");//解码buffer

    private ByteBuffer sBuffer = ByteBuffer.allocate(1024);//发送数据缓冲区  

    private ByteBuffer rBuffer = ByteBuffer.allocate(1024);//接收数据缓冲区
    
    private Selector selector;
    private boolean finishSearch = false;
    
    public ClientSearch(String searchserverip) {
		searchServerIP = searchserverip;
	}
    
    public void DoSearch(String searchwords){
    	finishSearch = false;
    	searchWords = searchwords;
    	SearchBegin();
    }

    private void SearchBegin(){
    	/* 
         * 客户端向服务器端发起建立连接请求 
         */  
        SocketChannel socketChannel;
		try {
			selector = Selector.open();
            socketChannel = SocketChannel.open();
    		socketChannel.configureBlocking(false);  
                  
            socketChannel.register(selector, SelectionKey.OP_CONNECT); 
            socketChannel.connect(new InetSocketAddress(searchServerIP, port));
	    		            
            while(!finishSearch){
            	selector.select();   
		        for (Iterator<SelectionKey> itor = selector.selectedKeys().iterator(); itor.hasNext();){
		        	SelectionKey key = (SelectionKey) itor.next();  
	                itor.remove(); 
	                
	                try {  
	                    if (key.isConnectable()) {  
	                    	SocketChannel client = (SocketChannel)key.channel();
	                    	client.finishConnect();  
	                        System.out.println("成功连接搜索服务器");
	                        client.configureBlocking(false);  
	                        SelectionKey clientKey =  client.register(selector,SelectionKey.OP_WRITE);
	                    }  
	                    if (key.isReadable()) {  
	                    	SocketChannel client = (SocketChannel) key.channel();  
	                        rBuffer.clear();  
	                        int count = client.read(rBuffer);
	                        String message;
	                        if (count > 0) {  
	                            rBuffer.flip();  
	                            message = String.valueOf(cs.decode(rBuffer).array());  
	                            System.out.println("接收来自搜索服务器消息:" + message);
	                            finishSearch = true;
	                        }    
	                    }  
	                    if (key.isWritable()) {
	                        // System.out.println("is writable...");  
	                        SocketChannel client = (SocketChannel) key.channel();  
	                        sBuffer.clear();
	                        NetCmd netCmd = new NetCmd();
	                        String message = netCmd.CreateMessage("localhost", searchWords);
	                        sBuffer = sBuffer.put(message.getBytes());		                        
	                        sBuffer.flip(); 		                        
	                        client.write(sBuffer);  
	                        if (sBuffer.remaining() == 0) {  // write finished, switch to OP_READ  
	                        	key.interestOps(SelectionKey.OP_READ);  
	                        }    
	                    }  
	                } catch (IOException e) { 
	                	System.out.println("服务器繁忙!");
	                	System.out.println("错误数:" + ClientMain.errorCounter.getAndIncrement());
                        finishSearch = true;
	                }  
		        }
            	
            }
            for (Iterator<SelectionKey> itor = selector.selectedKeys().iterator(); itor.hasNext();){
	        	SelectionKey key = (SelectionKey) itor.next();  
                itor.remove(); 
                key.cancel(); 
                key.channel().close(); 
            }
            
            selector.close();
            
            
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
    }

}
