package SearchServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;

import Net.NetCmd;
import Net.NetCmd.CmdType;
import Net.NetUtility;

public class SearchThread extends Thread {
	private static final int port = 8888;
    
    private Charset cs = Charset.forName("utf-8");//解码buffer	    
    private ByteBuffer sBuffer = ByteBuffer.allocate(1024); //发送数据缓冲区      
    private ByteBuffer rBuffer = ByteBuffer.allocate(1024);//接收数据缓冲区
	
	private SearchServer searchServer;
	private String searchServerIP;
    
	private Selector selector;	
	
	public SearchThread(SearchServer searchserver){
		searchServer = searchserver;
	}
	
	@Override
	public void run(){
		System.out.println("索引服务器检测端口是否可用...");
		if(!(NetUtility.GetNetUtility().isPortAvailable(port))){
			System.out.println(port+"端口已被占用,索引服务器正在退出...");
			return ;
		}
		
		try {
			searchServerIP = NetUtility.GetNetUtility().getLocalIpAddress();
			System.out.println("索引服务器IP地址:"+ searchServerIP);
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		try {				
			selector = Selector.open();  
			
			ServerSocketChannel serverSocketChannel=ServerSocketChannel.open();  
			serverSocketChannel.configureBlocking(false);  
			serverSocketChannel.socket().bind(new InetSocketAddress(port));  
			serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
			
			System.out.println("搜索服务器启动成功");
		    
			while(true){   
		        selector.select();   
		        for (Iterator<SelectionKey> itor = selector.selectedKeys().iterator(); itor.hasNext();){
		        	SelectionKey key = (SelectionKey) itor.next();  
	                itor.remove(); 
	                
	                try {  
	                    if (key.isAcceptable()) {  
	                        ServerSocketChannel server = (ServerSocketChannel) key.channel();  
	                        SocketChannel client = server.accept();  
	                        System.out.println("接受客户端的连接:" + client);  
	                        client.configureBlocking(false);  
	                        SelectionKey clientKey =  client.register(selector,SelectionKey.OP_READ);
	                    }  
	                    if (key.isReadable()) { 
	     //               	System.out.println("is readable....");
	                    	SocketChannel client = (SocketChannel) key.channel();  
	                        rBuffer.clear();  
	                        int count = client.read(rBuffer);
	                        String message;
	                        if (count > 0) {  
	                            rBuffer.flip();  
	                            message = String.valueOf(cs.decode(rBuffer).array());  
	                            System.out.println("接收到客户端检索关键字:" + message); 
                                //检索
	                            String items[] = message.split(",");
	                            if(items.length < 2){
	                            	key.interestOps(SelectionKey.OP_READ);
	                            }else{
	                                searchServer.DoSearch(items[1]);
	                                key.interestOps(SelectionKey.OP_WRITE);
	                            }
	                        }    
	                    }  
	                    if (key.isWritable()) {  
	     //                 System.out.println("is writable...");
	                        SocketChannel client = (SocketChannel) key.channel();  
	                        sBuffer.clear();
	                        NetCmd netCmd = new NetCmd();
	                        String message = netCmd.CreateMessage(searchServerIP, CmdType.SEARCHSUCCESS);
	                        sBuffer.put(message.getBytes());  		                        
	                        sBuffer.flip(); 		                        
	                        client.write(sBuffer);  
	                       
	                        if (sBuffer.remaining() == 0) {  // write finished, switch to OP_READ  
//	                            key.interestOps(SelectionKey.OP_READ);
	                            key.channel().close();
	                            key.cancel();
	                            System.out.println("向索引客户端发送消息:" + message);
	                        }
	                   
	                    }  
	                } catch (IOException e) {  
	                    e.printStackTrace(); 
	                    try { key.channel().close(); } catch (IOException ioe) { }  
	                }  
		        }
		    }				
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}

}
