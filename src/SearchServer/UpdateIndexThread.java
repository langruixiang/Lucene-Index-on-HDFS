package SearchServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;

import Net.NetCmd;
import Net.NetCmd.CmdType;

public class UpdateIndexThread extends Thread {
	    private SearchServer searchServer;
	
	    private static final int port = 9999;	     
	    private Charset cs = Charset.forName("gbk");//解码buffer
	    /*发送数据缓冲区*/
	    private ByteBuffer sBuffer = ByteBuffer.allocate(1024);  
	    /*接收数据缓冲区*/  
	    private ByteBuffer rBuffer = ByteBuffer.allocate(1024);
	    
	    private String indexServerIP;
	    
	    private Selector selector;
	    
	    public UpdateIndexThread(SearchServer searchserver){
	    	searchServer = searchserver;
	    	searchServer.ReadHDFSIndex();//读取索引
	    	indexServerIP = searchserver.GetIndexServerIP();
	    }
	    
	    @Override
	    public void run(){
	    	/* 
             * 客户端向服务器端发起建立连接请求 
             */  
            SocketChannel socketChannel;
			try {
				socketChannel = SocketChannel.open();
				socketChannel.configureBlocking(false);  
	            selector = Selector.open();  
	            socketChannel.register(selector, SelectionKey.OP_CONNECT);  
	            socketChannel.connect(new InetSocketAddress(indexServerIP, port)); 
		    		            
	            while(true){
	            	selector.select();   
			        for (Iterator<SelectionKey> itor = selector.selectedKeys().iterator(); itor.hasNext();){
			        	SelectionKey key = (SelectionKey) itor.next();  
		                itor.remove(); 
		                
		                try {  
		                    if (key.isConnectable()) {  
		                    	SocketChannel client = (SocketChannel)key.channel();
		                    	client.finishConnect();  
		                        System.out.println("成功连接索引服务器");  
		                        key.interestOps(SelectionKey.OP_READ);    
		                    }  
		                    if (key.isReadable()) {  
		                    	SocketChannel client = (SocketChannel) key.channel();  
		                        rBuffer.clear();  
		                        int count = client.read(rBuffer);
		                        String message;
		                        if (count > 0) {  
		                            rBuffer.flip();  
		                            message = String.valueOf(cs.decode(rBuffer).array());  
		                            System.out.println("接收来自索引服务器消息:" + message); 
                                    searchServer.ReadHDFSIndex();//更新索引		                      
		                            key.interestOps(SelectionKey.OP_WRITE); 
		                        }    
		                    }  
		                    if (key.isWritable()) {  
		                        // System.out.println("is writable...");  
		                        SocketChannel client = (SocketChannel) key.channel();  
		                        sBuffer.clear();
		                        NetCmd netCmd = new NetCmd();
		                        String message = netCmd.CreateMessage(indexServerIP, CmdType.UPDATESUCESS);
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
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}  
            
	    }

}
