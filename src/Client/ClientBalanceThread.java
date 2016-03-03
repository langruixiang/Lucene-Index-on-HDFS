package Client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import Net.NetCmd;
import Net.NetCmd.CmdType;

public class ClientBalanceThread extends Thread {	
	private static final int port = 7777;	     
    private Charset cs = Charset.forName("utf-8");//解码buffer
    
    private ByteBuffer sBuffer = ByteBuffer.allocate(1024);//发送数据缓冲区  
      
    private ByteBuffer rBuffer = ByteBuffer.allocate(1024);//接收数据缓冲区
    
    private Selector selector;
    private String balanceServerIP;
	private SelectionKey clientKey;
	
	private String searchServerIP;
	private boolean finish = false;
	
	private String confPath;
    
	private void InitByXML(){
    	SAXReader saxReader = new SAXReader();
		BufferedReader br;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(confPath)), "utf-8"));
			Document document = saxReader.read(br);
			
			Element root = document.getRootElement();

	        @SuppressWarnings("unchecked")
			List<Element> list = root.elements("BalanceServer");
	        for(int i = 0; i < list.size(); i++){
	        	balanceServerIP = list.get(i).elementText("IP");
	        }
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
    }
	
	public ClientBalanceThread(String confpath){
		confPath = confpath;
		InitByXML();
	}
	
	public String GetSearchServerIP(){		
		return searchServerIP;
	}
	
	@Override
	public void run(){
		/* 
         * 客户端向负载均衡服务器发起连接请求 
         */  
        SocketChannel socketChannel;
		try {
			socketChannel = SocketChannel.open();
			socketChannel.configureBlocking(false);  
            selector = Selector.open();  
            socketChannel.register(selector, SelectionKey.OP_CONNECT);  
            socketChannel.connect(new InetSocketAddress(balanceServerIP, port)); 
	    		            
            while(!finish){
            	selector.select();   
		        for (Iterator<SelectionKey> itor = selector.selectedKeys().iterator(); itor.hasNext();){
		        	SelectionKey key = (SelectionKey) itor.next();  
	                itor.remove(); 
	                
	                try {  
	                    if (key.isConnectable()) {  
	                    	SocketChannel client = (SocketChannel)key.channel();
	                    	client.finishConnect();  
	                        System.out.println("成功连接负载均衡服务器");
	                        key.interestOps(SelectionKey.OP_WRITE);
	                    }  
	                    if (key.isReadable()) {  
	                    	SocketChannel client = (SocketChannel) key.channel();  
	                        rBuffer.clear();  
	                        int count = client.read(rBuffer);
	                        String message;
	                        if (count > 0) {  
	                            rBuffer.flip();  
	                            message = String.valueOf(cs.decode(rBuffer).array());  
	                            System.out.println("接收来自负载均衡服务器消息:" + message);
	                            String items[] = message.split(",");
	                            searchServerIP = items[1];
	                            finish = true;
	                        }    
	                    }  
	                    if (key.isWritable()) {  
	                        // System.out.println("is writable...");  
	                        SocketChannel client = (SocketChannel) key.channel();  
	                        sBuffer.clear();
	                        NetCmd netCmd = new NetCmd();
	                        String message = netCmd.CreateMessage("localhost", CmdType.GETSEARCHSERVERIP);
	                        sBuffer = sBuffer.put(message.getBytes());		                        
	                        sBuffer.flip(); 		                        
	                        client.write(sBuffer);  
	                        if (sBuffer.remaining() == 0) {  // write finished, switch to OP_READ  
	                            key.interestOps(SelectionKey.OP_READ);
	                        }    
	                    }  
	                } catch (IOException e) { 
	                	e.printStackTrace();
	                    key.cancel();  
	                    try { key.channel().close(); } catch (IOException ioe) { }  
	                }  
		        }
            	
            }
            selector.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
	}

}
