package BalanceServer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class BalanceServer {
	private String confPath;
	private List<String> searchServerIP;
	
	public BalanceServer(String confpath){
		confPath = confpath;
		searchServerIP = new ArrayList<String>();
		InitByXML();
	}
	
	public List<String> GetSearchServerIP(){
		return searchServerIP;
	}
	
	private void InitByXML(){
    	SAXReader saxReader = new SAXReader();
		BufferedReader br;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(confPath)), "utf-8"));
			Document document = saxReader.read(br);
			
			Element root = document.getRootElement();

	        @SuppressWarnings("unchecked")
			List<Element> list = root.elements("SearchServer");
	        for(int i = 0; i < list.size(); i++){
	        	String ip = list.get(i).elementText("IP");
	        	searchServerIP.add(ip);
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

}
