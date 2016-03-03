package IndexServer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.file.Paths;
import java.util.Iterator;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.FSDirectory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class CreateIndex {
	private Analyzer analyzer;	
	private FSDirectory directory;
	private IndexWriterConfig conf;
	private IndexWriter indexWriter;
	
	private SAXReader saxReader;
	private Document document;
	
	private String indexPath;
	private String dataFile;
	
	public String GetIndexPath(){
		return indexPath;
	}
	
	public CreateIndex(String datafile,String indexpath){
			indexPath = indexpath;
			dataFile = datafile;
	}
	
	public void Begin(){
	    try{
			analyzer = new StandardAnalyzer();
			directory = FSDirectory.open(Paths.get(indexPath));
			conf = new IndexWriterConfig(analyzer);
			conf.setOpenMode(OpenMode.CREATE);
			indexWriter = new IndexWriter(directory, conf);
			
			saxReader = new SAXReader();
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(dataFile)), "utf-8")); 
			document = saxReader.read(br);
			
			Element root = document.getRootElement();
	        System.out.println("Root: " + root.getName());
	
	        // 获取名字为指定名称的第一个子元素
	        Element firstElement = root.element("RECORD");
	        
	        //建立倒排索引
	        String userID,text;
	        userID = firstElement.elementText("id");
	        text = firstElement.elementText("article");
	        System.out.println("id:"+userID+"\narticle:"+text);
	        
	        org.apache.lucene.document.Document luceneDocument = new org.apache.lucene.document.Document();
	        Field field = new StringField("fileName", userID , Store.YES);  
	        luceneDocument.add(field);  
	//        Field field2 = new LongField("fileSize", userID.length(), Store.YES);  
	//        luceneDocument.add(field2);  
	//        Field field3 = new LongField("fileLastModified",0, Store.YES);  
	//        luceneDocument.add(field3);   
	        Field field4 = new TextField("contents", new BufferedReader(new StringReader(new String(text.getBytes(),"utf-8")))); 
	        luceneDocument.add(field4);
	        Field field5 = new StoredField("weibo", text);
	        luceneDocument.add(field5);
		    indexWriter.addDocument(luceneDocument);
	
	        for (Iterator<Element> iter = root.elementIterator(); iter.hasNext();)
	        {
	        	luceneDocument = new org.apache.lucene.document.Document();
	            Element e = iter.next();
	            userID = e.elementText("id");
		        text = e.elementText("article");
		        System.out.println("id:"+userID+"\narticle:"+text);
		        field = new StringField("fileName", userID , Store.YES);  
		        luceneDocument.add(field);  
	//	        field2 = new LongField("fileSize", userID.length(), Store.YES);  
	//            luceneDocument.add(field2);  
	//            field3 = new LongField("fileLastModified",0, Store.YES);  
	//            luceneDocument.add(field3);  
	            field4 = new TextField("content", new BufferedReader(new StringReader(new String(text.getBytes(),"utf-8"))));  
	            luceneDocument.add(field4);
	            field5 = new StoredField("weibo", text);
	            luceneDocument.add(field5);
	            indexWriter.addDocument(luceneDocument);
	        }
	        
	        indexWriter.commit();
	        indexWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
