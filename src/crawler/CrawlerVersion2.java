package crawler;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.wltea.analyzer.core.IKSegmenter;
import org.wltea.analyzer.core.Lexeme;

public class CrawlerVersion2 {


	public static void insertRecord(String title , String url , String keyWords , String text) {
		// ����������
		String DBDRIVER = "com.mysql.jdbc.Driver";

		// URLָ��Ҫ���ʵ����ݿ���
		String DBURL = "jdbc:mysql://127.0.0.1:3306/score?useUnicode=true&characterEncoding=GBK";

		// MySQL����ʱ���û���
		String USERNAME = "root";

		// MySQL����ʱ������
		String PASSWORD = "root";
		
		// ���ݿ����Ӷ���
		Connection conn = null;

		// ���ݿ��������
		PreparedStatement stmt = null;

		// 1��������������
		try {

			Class.forName(DBDRIVER);

		} catch (ClassNotFoundException e) {

			e.printStackTrace();

		}

		// 2���������ݿ�

		// ͨ�����ӹ������������ݿ�

		try {

			// �����ӵ�ʱ��ֱ�������û���������ſ�������

			conn = DriverManager.getConnection(DBURL, USERNAME, PASSWORD);

		} catch (SQLException e) {

			e.printStackTrace();

		}

		// 3�������ݿ��в���һ������
		String sql = "INSERT INTO webpage2(title , url , keywords , text) VALUES(? , ? , ? ,?)";	
		try {

			stmt = conn.prepareStatement(sql);
			stmt.setString(1 , title); 
            stmt.setString(2,url);
            stmt.setString(3,keyWords); 
            stmt.setString(4,text); 

		} catch (SQLException e) {

			e.printStackTrace();

		}

		// 4��ִ�����

		try {

			stmt.executeUpdate();

		} catch (SQLException e) {

			e.printStackTrace();

		}

		// 5���رղ����������෴��~

		try {

			stmt.close();

			conn.close();

		} catch (SQLException e) {

			e.printStackTrace();

		}
	}
	
	//ץȡ��ҳ�����������⡢�ؼ��֡����ĺͳ�����
	public static void getContent(String url , Document doc ,  Queue<String> linkList) {
		//ҳ��title
		String docTitle = doc.select("title").text();
		//ҳ��keywords
		String docKeywords = doc.select("meta[name=keywords]").attr("content").toString().replace("'", "''");
		//ҳ������
		String docDetail ="";
		for(Element e:doc.select("p")){
			docDetail += e.text();
		}
		ArrayList<String> keywords = new ArrayList<String>();
		//�ֽ�keywords
		for(String e:docKeywords.split(",|��| |||��")) {
			if(e != "") {
				keywords.add(e);
			}		
		}
		//��keywordsС��5�������ҳ���ı��л�ȡ����ؼ���
		if(keywords.size() < 5) {
			Reader input = new StringReader(docDetail);
			IKSegmenter iks = new IKSegmenter(input,true);
	        Lexeme lexeme = null;
	        //�ִ������зִʲ������Ƶ
	        Map<String, Integer> words = new HashMap<String, Integer>();
	        try {
	        	while ((lexeme = iks.next()) != null) {
	        		if (words.containsKey(lexeme.getLexemeText())) {
	        			words.put(lexeme.getLexemeText(), words.get(lexeme.getLexemeText()) + 1);
	        		} else {
	        			words.put(lexeme.getLexemeText(), 1);
	        		}
	        	}
	        }catch(IOException e) {
	            e.printStackTrace();
	        }
	        //��ȡ��Ƶ�ϸߵ��ִ���Ϊ�ؼ���
	        int freqlimit = 10;
	        while(freqlimit-- > 0) {
	        	if(keywords.size() >= 5)break;
	        	for(String word:words.keySet()) {
	        		if(words.get(word) >= freqlimit) {
	        			keywords.add(word);
	        			words.put(word, 0);
	        			if(keywords.size() >= 5)break;
	        		}
	        	}
	        }
		}
		//�ؼ���
		StringBuilder keyWords = new StringBuilder();
		for(String e:keywords){
			keyWords.append(e);
			}
		
//		��ʾ��ҳ��Ϣ�����⡢url���ؼ��֡�����
		System.out.println(docTitle);
		System.out.println(url);
//	    System.out.println(keyWords.toString());
//		System.out.println(docDetail);
		
		//�����ݿ��в�����ҳ��Ϣ�����⡢url���ؼ��֡�����
		insertRecord(docTitle ,  url , keyWords.toString() , docDetail);
		
		//��ȡ������ҳ������url
		org.jsoup.select.Elements elements = doc.select("a[href]");
		for (Element e : elements) {
			String attr = e.attr("href");
			if (attr.startsWith(".")) {
				linkList.add(e.baseUri() + e.attr("href").substring(1));
				//System.out.println(e.baseUri() + e.attr("href").substring(1));
			} else if (attr.startsWith("http")) {
				linkList.add(e.attr("href"));
				//System.out.println(e.attr("href"));
			}
		  }
		}
//	    while(!linkList.isEmpty()){
//	    	System.out.println(linkList.poll());
//	    }

	public static void main(String[] args) {
		String firstUrl = "http://www.qq.com/";
		String nextUrl = "";
		Queue<String> queue = new LinkedList<String>(); 
		HashSet<String> hashSet = new HashSet<String>();
		Document firstDoc = null;
		try {
			firstDoc = Jsoup.connect(firstUrl).get();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		//ץȡ��ҳ������
		int count  = 1;
		System.out.println("��"+count+"������");
		hashSet.add(firstUrl);
		getContent(firstUrl , firstDoc , queue); 
	    while(!queue.isEmpty()){
	    	nextUrl = queue.poll();
	    	if(hashSet.contains(nextUrl))
	    		continue;
	    	else{
	    		try {
				Document nextDoc = Jsoup
						.connect(nextUrl)
						.data("jquery", "java")
						.userAgent(
								"Mozilla/5.0 (Windows; U; Windows NT 5.2) AppleWebKit/525.13 (KHTML, like Gecko) Chrome/0.2.149.27 Safari/525.13")
						.cookie("auth", "token")
						.timeout(6000).get();
	    		count++;
	    		System.out.println("��"+count+"������");
	    		hashSet.add(nextUrl);
	    		getContent(nextUrl , nextDoc , queue); 
	    		if(count > 50000)
	    				break;
	    	}catch(Exception e){
	    		continue;
	    		}
	    	}
       }
	    System.out.println("��ϣ���СΪ��"+hashSet.size());
	}
}
