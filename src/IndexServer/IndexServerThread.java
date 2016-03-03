package IndexServer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import Net.NetCmd;
import Net.NetCmd.CmdType;
import Net.NetUtility;

public class IndexServerThread extends Thread {
	    private static final int port = 9999;
	    private IndexServer indexServer;
	     
	    private Charset cs = Charset.forName("gbk");//解码buffer	    
	    private ByteBuffer sBuffer = ByteBuffer.allocate(1024); //发送数据缓冲区      
	    private ByteBuffer rBuffer = ByteBuffer.allocate(1024);//接收数据缓冲区
	    
		private String serverIP; //索引服务器ip
    	
    	private Selector selector; //选择器
    	private List<SelectionKey> searchServerChannel;//检索服务器socket对应的key
    	
    	NetCmd netCmd = new NetCmd();
        String message;
	
		public IndexServerThread(IndexServer indexserver){	
			indexServer = indexserver;
			indexServer.CreateIndex();			
//			indexServer.PutIndexToHDFS();
			searchServerChannel = new ArrayList<SelectionKey>(); 
		}
		
		public void SendUpdateIndexSignal(){
			message = netCmd.CreateMessage(serverIP, CmdType.UPDATEINDEX);
			for(int i = 0; i < searchServerChannel.size(); i++){
				searchServerChannel.get(i).interestOps(SelectionKey.OP_WRITE);
			}			
		}
		
		@Override
		public void run(){
			System.out.println("索引服务器检测端口是否可用...");
			if(!(NetUtility.GetNetUtility().isPortAvailable(port))){
				System.out.println(port+"端口已被占用,索引服务器正在退出...");
				return ;
			}
			
			try {
				serverIP = NetUtility.GetNetUtility().getLocalIpAddress();
				System.out.println("索引服务器IP地址:"+ serverIP);
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
				
				System.out.println("索引服务器启动成功");
			    
				while(true){   
			        selector.select();   
			        for (Iterator<SelectionKey> itor = selector.selectedKeys().iterator(); itor.hasNext();){
			        	SelectionKey key = (SelectionKey) itor.next();  
		                itor.remove(); 
		                
		                try {  
		                    if (key.isAcceptable()) {  
		                        ServerSocketChannel server = (ServerSocketChannel) key.channel();  
		                        SocketChannel client = server.accept();  
		                        System.out.println("接受搜索服务器连接:" + client);  
		                        client.configureBlocking(false);  
		                        SelectionKey clientKey =  client.register(selector,SelectionKey.OP_CONNECT);
		                        searchServerChannel.add(clientKey);
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
		                            System.out.println("接收到检索服务器:" + message); 
                                    //接收到某个检索服务器更新索引成功的消息
		                             
		                        }    
		                    }  
		                    if (key.isWritable()) {  
		     //                 System.out.println("is writable...");
		                        SocketChannel client = (SocketChannel) key.channel();  
		                        sBuffer.clear();
		                        
		                        sBuffer.put(message.getBytes());  		                        
		                        sBuffer.flip(); 		                        
		                        client.write(sBuffer);  
		                       
		                        if (sBuffer.remaining() == 0) {  // write finished, switch to OP_READ  
		                            key.interestOps(SelectionKey.OP_READ);
		                            System.out.println("向索引服务器发送消息:" + message);
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
