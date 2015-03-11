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
			//�û����������
			String userInput = new String(request.getParameter("searchName").getBytes("ISO-8859-1"),"utf-8");
			//��ѯ���������
			int count = 0;
			int length = 0;
			String str = "";
			//html����
			response.setContentType("text/html;charset=GBK");
			PrintWriter out = response.getWriter();
			out.println("<html>");
			out.println("<body>");
			out.println("<body bgcolor='#00FFFF'>");
			out.println("<form name = 'form1' action = 'Search.bin' accept-charset='utf-8'>");
			out.println("<input type='text' name = 'searchName'>");
			out.println("<input type='submit' value = '��������'>");
			out.println("</form>");
			out.println("<br><br>");
			// ����������
			String DBDRIVER = "com.mysql.jdbc.Driver";

			// URLָ��Ҫ���ʵ����ݿ���
			String DBURL = "jdbc:mysql://127.0.0.1:3306/score?useUnicode=true&characterEncoding=GBK";

			// MySQL����ʱ���û���
			String USERNAME = "root";

			// MySQL����ʱ������
			String PASSWORD = "root";

			 try { 
		       // ������������
		        Class.forName(DBDRIVER); 

		       // �������ݿ�
		        Connection conn = DriverManager.getConnection(DBURL, USERNAME, PASSWORD); 
		        
		       // statement����ִ��SQL���
		       Statement stmt = conn.createStatement();
		        
    			// Ҫִ�е�SQL���
                String sql = "select * from webpage";
				ResultSet rs = stmt.executeQuery(sql);
				while( rs.next() )
			    {
			    //�����ݿ��ȡ��Ӧ�ֶ�
			    String title = rs.getString("title");			        
			    String href = rs.getString("url");
			    String keywords = rs.getString("keywords");
			    String text = rs.getString("text");
	            
	            //ȥ���ַ����ж���Ŀո�
			    userInput = userInput.replace(" ","");
	            
	            //�����ַ�����ƥ��
	            Integer isKeywordsFind =keywords.indexOf(userInput);
	            Integer isTextFind = text.indexOf(userInput);	 
	            
	            
		        if ( (isKeywordsFind != -1 || isTextFind != -1) && !userInput.trim().equals("") ) {
		            //��ƥ��ļ�¼��ʾ����
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
		out.println("<h1>"+"�����Ϊ��"+count+"</h1>");
		out.println("</body>");
		out.println("</html>");
	}
}
