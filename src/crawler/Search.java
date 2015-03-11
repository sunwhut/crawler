package crawler;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class Search extends HttpServlet {
	public void doGet(HttpServletRequest request,HttpServletResponse response)throws ServletException,IOException{
			//用户输入的内容
			String userInput = new String(request.getParameter("searchName").getBytes("ISO-8859-1"),"utf-8");
			//查询结果的数量
			int count = 0;
			int length = 0;
			String str = "";
			//html代码
			response.setContentType("text/html;charset=GBK");
			PrintWriter out = response.getWriter();
			out.println("<html>");
			out.println("<body>");
			out.println("<body bgcolor='#00FFFF'>");
			out.println("<form name = 'form1' action = 'Search.bin' accept-charset='utf-8'>");
			out.println("<input type='text' name = 'searchName'>");
			out.println("<input type='submit' value = '继续搜索'>");
			out.println("</form>");
			out.println("<br><br>");
			// 驱动程序名
			String DBDRIVER = "com.mysql.jdbc.Driver";

			// URL指向要访问的数据库名
			String DBURL = "jdbc:mysql://127.0.0.1:3306/score?useUnicode=true&characterEncoding=GBK";

			// MySQL配置时的用户名
			String USERNAME = "root";

			// MySQL配置时的密码
			String PASSWORD = "root";

			 try { 
		       // 加载驱动程序
		        Class.forName(DBDRIVER); 

		       // 连续数据库
		        Connection conn = DriverManager.getConnection(DBURL, USERNAME, PASSWORD); 
		        
		       // statement用来执行SQL语句
		       Statement stmt = conn.createStatement();
		        
    			// 要执行的SQL语句
                String sql = "select * from webpage";
				ResultSet rs = stmt.executeQuery(sql);
				while( rs.next() )
			    {
			    //从数据库获取相应字段
			    String title = rs.getString("title");			        
			    String href = rs.getString("url");
			    String keywords = rs.getString("keywords");
			    String text = rs.getString("text");
	            
	            //去除字符串中多余的空格
			    userInput = userInput.replace(" ","");
	            
	            //进行字符串的匹配
	            Integer isKeywordsFind =keywords.indexOf(userInput);
	            Integer isTextFind = text.indexOf(userInput);	 
	            
	            
		        if ( (isKeywordsFind != -1 || isTextFind != -1) && !userInput.trim().equals("") ) {
		            //将匹配的记录显示出来
		        	length = text.length();
		        	if(length>100){
		        		str = text.substring(0 , 100);
		        		out.println("<a href= "+href+">"+title+"</a>");
		    			out.println("<p>"+str+"</p>");   
		    			out.println("<br><br><br>"); 
		        	}
		        	else{
	    			out.println("<a href= "+href+">"+title+"</a>");
	    			out.println("<p>"+text+"</p>");   
	    			out.println("<br><br><br>"); 
		        	}
	    		 	count ++;
		          }
			    }
			} catch(ClassNotFoundException e) { 
	            System.out.println("Sorry,can`t find the Driver!"); 
	            e.printStackTrace(); 
	           } catch(SQLException e) { 
	            e.printStackTrace(); 
	           } catch(Exception e) { 
	            e.printStackTrace(); 
	           } 
	    out.println("<h1>"+userInput+"</h1>");
		out.println("<h1>"+"结果数为："+count+"</h1>");
		out.println("</body>");
		out.println("</html>");
	}
}
