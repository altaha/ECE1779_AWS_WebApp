package ece1779.jdbc;
 
import javax.servlet.http.*;

import org.apache.commons.dbcp.cpdsadapter.*;
import org.apache.commons.dbcp.datasources.*;

public class Initialization extends HttpServlet {
	public void init() {
		try {
		    //Initialize connection pool    

		    DriverAdapterCPDS ds = new DriverAdapterCPDS();
		    ds.setDriver("com.mysql.jdbc.Driver");
		    ds.setUrl("jdbc:mysql://ece1779db.cf2zhhwzx2tf.us-east-1.rds.amazonaws.com/ece1779");
		    
		    ds.setUser("ece1779");
		    ds.setPassword("Sp2012");

		    SharedPoolDataSource dbcp = new SharedPoolDataSource();
		    dbcp.setConnectionPoolDataSource(ds);

		    getServletContext().setAttribute("dbpool",dbcp);
		}
		catch (Exception ex) {
		    getServletContext().log("SQLGatewayPool Error: " + ex.getMessage());
		}
	}
}


