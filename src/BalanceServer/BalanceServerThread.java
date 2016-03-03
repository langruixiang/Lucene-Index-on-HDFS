package BalanceServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import Client.ClientSearch;
import Net.NetCmd;
import Net.NetCmd.CmdType;
import Net.NetUtility;

public class BalanceServerThread extends Thread {
	private static final int port = 7777;
	private Charset cs = Charset.forName("utf-8");//解码buffer	    
    private ByteBuffer sBuffer = ByteBuffer.allocate(1024); //发送数据缓冲区      
    private ByteBuffer rBuffer = ByteBuffer.allocate(1024);//接收数据缓冲区
	private Selector selector; //选择器
	
	private String balanceServerIP;//负载均衡服务器IP
	private BalanceServer balanceServer;//负载均衡服务器

	private Map<String,Integer> loadCounter = new HashMap<String,Integer>();//全局计数器 
    private int transferSum = 0;
    private int numOfSearchServer;
    
    public BalanceServerThread(BalanceServer balanceserver){
		balanceServer = balanceserver;
		List<String> searchServerIP = balanceServer.GetSearchServerIP();
		numOfSearchServer = searchServerIP.size();
		for(int i = 0; i < searchServerIP.size(); i++){
			loadCounter.put(searchServerIP.get(i), 0);
		}
	}
    
    public String GetSearchServerIP(){
    	String ip = null;
    	for (Map.Entry<String,Integer> entry : loadCounter.entrySet()) {  
    	    if(entry.getValue() <= transferSum*1.0/numOfSearchServer){
    	    	ip = (String)entry.getKey();
    	    	entry.setValue(entry.getValue()+1);
    	    	transferSum++;
    	    	break;
    	    }
    	}
    	return ip;
    }
	
	@Override
	public void run(){
		System.out.println("负载均衡服务器检测端口是否可用...");
		if(!(NetUtility.GetNetUtility().isPortAvailable(port))){
			System.out.println(port+"端口已被占用,负载均衡服务器正在退出...");
			return ;
		}
		
		try {
			balanceServerIP = NetUtility.GetNetUtility().getLocalIpAddress();
			System.out.println("负载均衡服务器IP地址:"+ balanceServerIP);
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
			
			System.out.println("负载均衡服务器启动成功");
		    
			while(true){   
		        selector.select();   
		        for (Iterator<SelectionKey> itor = selector.selectedKeys().iterator(); itor.hasNext();){
		        	SelectionKey key = (SelectionKey) itor.next();  
	                itor.remove(); 
	                
	                try {  
	                    if (key.isAcceptable()) {  
	                        ServerSocketChannel server = (ServerSocketChannel) key.channel();  
	                        SocketChannel client = server.accept();  
	                        System.out.println("接受客户端连接:" + client);  
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
	                            System.out.println("接收到客户端信息:" + message); 
	                            key.interestOps(SelectionKey.OP_WRITE);
	                        }    
	                    }  
	                    if (key.isWritable()) {  
	                      System.out.println("is writable...");
	                        SocketChannel client = (SocketChannel) key.channel();  
	                        sBuffer.clear();
	                        NetCmd netCmd = new NetCmd();
	                        String searchServerIP = GetSearchServerIP();
	                        String message = netCmd.CreateMessage(balanceServerIP, searchServerIP);
	                        sBuffer.put(message.getBytes());  		                        
	                        sBuffer.flip(); 		                        
	                        client.write(sBuffer);  
	                       
	                        if (sBuffer.remaining() == 0) {  // write finished, switch to OP_READ  
	                            key.interestOps(SelectionKey.OP_READ);
	                            key.channel().close();
	                            key.cancel();
	                            System.out.println("向客户端发送消息:" + message);
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
