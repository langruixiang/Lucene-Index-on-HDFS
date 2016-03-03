package IndexServer;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.util.Progressable;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;


public class IndexServer {
	
    
	private String confPath;	//配置文件路径
    private String serverName;  //服务器名称
    private String dataPath;    //语料路径
	private String indexPath;   //索引路径
	private String HDFSPath;	//Hadoop路径
	
    private List<String> searchServerIP = new ArrayList<String>();  //索引服务器IP
    
    private CreateIndex indexServer; //创建索引    
    
    @SuppressWarnings("unchecked")
	private void InitByXML(){
    	SAXReader saxReader = new SAXReader();
		BufferedReader br;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(confPath)), "utf-8"));
			Document document = saxReader.read(br);
			
			Element root = document.getRootElement();

	        @SuppressWarnings("unchecked")
			List<Element> list = root.elements("IndexServer");
	        for(int i = 0; i < list.size(); i++){
	        	String name = list.get(i).elementText("serverName");
	        	if(name.equals(serverName)){
	        		dataPath = list.get(i).elementText("dataPath");
	        		indexPath = list.get(i).elementText("indexTmpPath");
	        		HDFSPath = list.get(i).elementText("hdfsPath");
	        	}
	        }
	        
	        list = root.elements("SearchServer");
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
    
    public IndexServer(String confpath,String servername){
    	confPath = confpath;
    	serverName = servername;
    	
    	InitByXML();    	
    	
    	indexServer = new CreateIndex(dataPath,indexPath);
    }
    
    public void CreateIndex(){
    	indexServer.Begin();
    }
    
    public String GetIndexPath(){
    	return indexPath;
    }
	
	public boolean PutIndexToHDFS(){
		File indexFile = new File(indexPath);
		File[] indexFiles = indexFile.listFiles();
		
		for (int i = 0; i < indexFiles.length; i++) {
		  if(!indexFiles[i].isDirectory()){
			try {
				 InputStream in = new BufferedInputStream(new FileInputStream(indexFiles[i]));
				 Configuration conf = new Configuration();
				  
				 FileSystem fs = FileSystem.get(URI.create(HDFSPath + indexFiles[i].getName()), conf);
				 OutputStream out = fs.create(new Path(HDFSPath + indexFiles[i].getName()), new Progressable() {
					   public void progress() {
					    System.out.print(".");
			           }
			     });
				  
				 IOUtils.copyBytes(in, out, 4096, true);	
		    } catch (IOException e) {
			// TODO Auto-generated catch block
			   e.printStackTrace();
			}
		  }
		}
		  	
	     return true;	
	}
}
