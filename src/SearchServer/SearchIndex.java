package SearchServer;

import java.io.IOException;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.RAMDirectory;

public class SearchIndex {
	
//	private String indexPath;
	private String queryWords;
	private int resultNum = 100;
	private TopDocs docResult;
	
	private RAMDirectory ramDir;
	
	public void SetRAMDirectory(RAMDirectory ramdir){
		ramDir = ramdir;
	}
	
	public void SetQueryWords(String querywords){
		queryWords = querywords;
	}
	
	public void Begin(){
		try {
//			 FSDirectory directory = FSDirectory.open(Paths.get(indexPath));
//		      IndexSearcher seacher= new IndexSearcher(DirectoryReader.open(directory));  
		     
			 DirectoryReader ireader = DirectoryReader.open(ramDir);
			 IndexSearcher seacher = new IndexSearcher(ireader);
			
			 QueryParser parser = new QueryParser("content", new StandardAnalyzer());
			
			 Query query;
			
			 query = parser.parse(queryWords);
		        
			 docResult = seacher.search(query, resultNum);  
	        
	         System.out.println("一共搜索到结果:" + docResult.totalHits + "条");  
	         for (ScoreDoc scoreDoc : docResult.scoreDocs)  
	         {  
	            System.out.print("序号为:" + scoreDoc.doc);  
	            System.out.print(" 评分为:" + scoreDoc.score);  
	            org.apache.lucene.document.Document lucenedocument = seacher.doc(scoreDoc.doc);  
	            System.out.print(" 文件名:" + lucenedocument.get("fileName"));  
	            String content = lucenedocument.get("weibo");
	            System.out.print(" 内容为:" + content);  
//		            System.out.print(" 文件大小:" + lucenedocument.get("fileSize"));  
//		            System.out.print(" 文件日期:" + lucenedocument.get("fileLastModified"));  
	            System.out.println();  
	         }  
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public TopDocs GetSearchResult(){
		return docResult;
	}

}
