package com.tacticalenterprisesltd;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

public class Main {

	public static void main(String[] args)
	{
	  try{
		  	Database db = new Database("editor");
		  	Connection conn = db.getConnection(Database.RDBMS.MYSQL, "org.gjt.mm.mysql.Driver","localhost","3306","webapp", "secret");
		  	Statement stm = conn.createStatement();
		  	ResultSet rs = stm.executeQuery("SELECT * from blobtypes");
		  	ResultSetMetaData rsmd = rs.getMetaData();

			int numCols = rsmd.getColumnCount();

			for (int i = 0; i < numCols; i++) {
				String columnName = rsmd.getColumnName(i + 1);
				int columnType = rsmd.getColumnType(i + 1);
				System.out.println(columnName + " -> " + String.valueOf(columnType));
			}
			/*RETURNS:
			idBlobTypes -> 4
			tiny -> -3
			blob -> -4
			medium -> -4
			large -> -4
		    */
	  }
	  catch(Exception e)
	  {
		e.printStackTrace(); 
	  }
	  
	}
	
	
}
