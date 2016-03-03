package Net;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

public class NetUtility {
	public static NetUtility netUtility = null;
	
	public static NetUtility GetNetUtility(){
		if(netUtility == null){
			netUtility = new NetUtility();
		}
		
		return netUtility;
	}
	
	/*
	 * 判断端端口是否被占用
	 */
	private void bindPort(String host, int port) throws Exception {
	    Socket s = new Socket();
	    s.bind(new InetSocketAddress(host, port));
	    s.close();
	}
	public boolean isPortAvailable(int port) {
	    try {
	        bindPort("0.0.0.0", port);
	        bindPort(InetAddress.getLocalHost().getHostAddress(), port);
	        return true;
	    } catch (Exception e) {
	        return false;
	    }
	}		
	/*
	 * end for 判断端口是否被占用
	 */
	
	//获取本身IP地址
    public static String getLocalIpAddress() throws SocketException {  
    	InetAddress addr = null;
		try {
			addr = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	String ip=addr.getHostAddress().toString();//获得本机IP
    	return ip; 
    }  

}
