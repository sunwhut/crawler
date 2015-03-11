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
		// 驱动程序名
		String DBDRIVER = "com.mysql.jdbc.Driver";

		// URL指向要访问的数据库名
		String DBURL = "jdbc:mysql://127.0.0.1:3306/score?useUnicode=true&characterEncoding=GBK";

		// MySQL配置时的用户名
		String USERNAME = "root";

		// MySQL配置时的密码
		String PASSWORD = "root";
		
		// 数据库连接对象
		Connection conn = null;

		// 数据库操作对象
		PreparedStatement stmt = null;

		// 1、加载驱动程序
		try {

			Class.forName(DBDRIVER);

		} catch (ClassNotFoundException e) {

			e.printStackTrace();

		}

		// 2、连接数据库

		// 通过连接管理器连接数据库

		try {

			// 在连接的时候直接输入用户名和密码才可以连接

			conn = DriverManager.getConnection(DBURL, USERNAME, PASSWORD);

		} catch (SQLException e) {

			e.printStackTrace();

		}

		// 3、向数据库中插入一条数据
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

		// 4、执行语句

		try {

			stmt.executeUpdate();

		} catch (SQLException e) {

			e.printStackTrace();

		}

		// 5、关闭操作，步骤相反哈~

		try {

			stmt.close();

			conn.close();

		} catch (SQLException e) {

			e.printStackTrace();

		}
	}
	
	//抓取网页并解析出标题、关键字、正文和超链接
	public static void getContent(String url , Document doc ,  Queue<String> linkList) {
		//页面title
		String docTitle = doc.select("title").text();
		//页面keywords
		String docKeywords = doc.select("meta[name=keywords]").attr("content").toString().replace("'", "''");
		//页面正文
		String docDetail ="";
		for(Element e:doc.select("p")){
			docDetail += e.text();
		}
		ArrayList<String> keywords = new ArrayList<String>();
		//分解keywords
		for(String e:docKeywords.split(",|，| |||、")) {
			if(e != "") {
				keywords.add(e);
			}		
		}
		//若keywords小于5个，则从页面文本中获取更多关键字
		if(keywords.size() < 5) {
			Reader input = new StringReader(docDetail);
			IKSegmenter iks = new IKSegmenter(input,true);
	        Lexeme lexeme = null;
	        //分词器进行分词并计算词频
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
	        //获取词频较高的字词作为关键字
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
		//关键字
		StringBuilder keyWords = new StringBuilder();
		for(String e:keywords){
			keyWords.append(e);
			}
		
//		显示网页信息：标题、url、关键字、正文
		System.out.println(docTitle);
		System.out.println(url);
//	    System.out.println(keyWords.toString());
//		System.out.println(docDetail);
		
		//向数据库中插入网页信息：标题、url、关键字、正文
		insertRecord(docTitle ,  url , keyWords.toString() , docDetail);
		
		//提取出该网页的所有url
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
		//抓取网页的数量
		int count  = 1;
		System.out.println("第"+count+"个链接");
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
	    		System.out.println("第"+count+"个链接");
	    		hashSet.add(nextUrl);
	    		getContent(nextUrl , nextDoc , queue); 
	    		if(count > 50000)
	    				break;
	    	}catch(Exception e){
	    		continue;
	    		}
	    	}
       }
	    System.out.println("哈希表大小为："+hashSet.size());
	}
}
