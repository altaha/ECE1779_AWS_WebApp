package ece1779.jdbc;

import java.io.*;
import java.lang.Integer;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.sql.DataSource;
  
public class Transaction extends HttpServlet {
	
	public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws IOException, ServletException
    {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("<html>");
        out.println("<head>");
        out.println("<title>Transaction Example</title>");
        out.println("</head>");
        out.println("<body>");
        
  
       	Connection con = null;
    	try{ 
            // In real life, you should get these values from a shopping cart
            int showtimeid = 1;
            int userid = 1;
            // In real life, the order id would be unique
            int orderid = 1;
            
		    // Get DB connection from pool
		    DataSource dbcp = (DataSource)this.getServletContext().getAttribute("dbpool");
		    con = dbcp.getConnection();
	
	
		    // Execute SQL query
		    Statement stmt = con.createStatement();

		    String query = "select available from showtimes " +
		    			   "where  showtimeid = " + showtimeid;
            
            ResultSet rs = stmt.executeQuery(query);

            rs.next();
            int available = rs.getInt("available");

            if (available > 0) {
                available--;
                String sql = "update showtimes set available = " + available +
                             " where showtimeid = " + showtimeid;

                // Entering transactional mode
                con.setAutoCommit(false);

                stmt.execute(sql);

                sql = "insert into orders (userid, showtimeid) "+
                      "value(" + userid + "," + showtimeid + ")";

                stmt.execute(sql);

                // Exiting transactional mode
                con.commit();
                con.setAutoCommit(true);	   
                stmt.close();
                
                out.print("Congratulation you bought a ticket!");
                    
                }
                else 
                	out.print("Sorry, we are sold out!");
                
        }
    	catch(Exception ex) {
    		try {
              getServletContext().log(ex.getMessage());  
              
              if (!con.getAutoCommit()) {
            	  con.rollback();
                  con.setAutoCommit(true);
              }
              
              out.println(ex.getMessage());
              
    		}
    		catch (Exception e) {
                out.println(e.getMessage());
    		}
    	}    	
    	finally {
    		try {
    		con.close();
    		}
    		catch (Exception e) {
                out.println(e.getMessage());
    		}
    	}
        
        out.println("</body>");
        out.println("</html>");
    }
}
