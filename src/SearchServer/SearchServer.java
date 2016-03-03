package SearchServer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.IOContext;
import org.apache.lucene.store.IOContext.Context;
import org.apache.lucene.store.RAMDirectory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import DiskFileOperation.DiskFileOperation;

public class SearchServer {
	private String confPath; //配置文件路径
	private String serverName; //服务器名称
	private String indexPath; //索引临时存放目录
	private String HDFSPath; //Hadoop路径
	private String indexServerIP;
	
	private SearchIndex searchIndex; //搜索索引
	private RAMDirectory ramDir; //内存索引
	
	@SuppressWarnings("unchecked")
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
		        	String name = list.get(i).elementText("serverName");
		        	if(name.equals(serverName)){
		        		indexPath = list.get(i).elementText("indexTmpPath");
		        		HDFSPath = list.get(i).elementText("hdfsPath");
		        	}
		        }
		        
		        list = root.elements("IndexServer");
		        for(int i = 0; i < list.size(); i++){
		        		indexServerIP = list.get(i).elementText("IP");
		        	
		        }
		        
			} catch (UnsupportedEncodingException | FileNotFoundException | DocumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	
	public SearchServer(String confpath,String servername){
		confPath = confpath;
		serverName = servername;
		
		InitByXML();
		
		searchIndex = new SearchIndex();			
	}
	
	public String GetIndexServerIP(){
		return indexServerIP;
	}
	
	public  TopDocs DoSearch(String querywords){
		searchIndex.SetQueryWords(querywords);
		searchIndex.Begin();
		return searchIndex.GetSearchResult();		
	}
	
	private String GetHDFSFileName(String hdfspath){
		String []items = hdfspath.split("/");
		return items[items.length-1];
	}
	
	public boolean GetIndexFromHDFS(){
		try {
			Configuration conf = new Configuration();
			FileSystem fs = FileSystem.get(URI.create(HDFSPath), conf);
			
			Path listf =new Path(HDFSPath);
			  
            FileStatus stats[]=fs.listStatus(listf);
            
            for(int i = 0; i < stats.length; i++){
            	String hdfsPath = stats[i].getPath().toString();
            	Path p1=new Path(hdfsPath);            	
                Path p2 =new Path(indexPath + GetHDFSFileName(hdfsPath));  
                fs.copyToLocalFile(p1, p2);    
            }
            fs.close();//释放资源
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}	
	
	public boolean ReadHDFSIndex(){
//		GetIndexFromHDFS();
		java.nio.file.Paths tmpPath = null;
		try {
			FSDirectory fsDir = FSDirectory.open(tmpPath.get(indexPath));
			IOContext ioContext = new IOContext(Context.DEFAULT);
			ramDir = new RAMDirectory(fsDir,ioContext);
			searchIndex.SetRAMDirectory(ramDir);
//删除硬盘临时索引
//			DiskFileOperation.GetDiskFileOperation().delAllFile(indexPath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return true;		
	}
}
