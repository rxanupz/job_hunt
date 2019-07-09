package com.hirrr.jobsnatcher.Test;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Scanner;

import com.hirrr.jobsnatcher.DBConnection.DataSource;
 
public class TxtToDB
{
 
  public static void main(String[] args)
  {
    try{
      // create a mysql database connection
		DataSource ds = null;
		Connection con = null;
		try {
			ds = DataSource.getInstance();
			con =  ds.getConnection();
		} catch (Exception e2) {
			e2.printStackTrace();
		}
      File file1 = new File("/home/worklyf/Desktop/CompanyUrl/CompanyName.txt");
      Scanner sc = new Scanner(file1);
  	
      	while(sc.hasNextLine()){
      		String str = sc.nextLine();
    		String company_name ="";
            String company_url = "";
            String company_status = "";
            String email_id = "";
            String ats_check = "";
            String next_page_check = "";
            String jobtype ="";
            company_name = str.trim();
      // the mysql insert statement
      String query = " insert into jobsnatcher (company_name, career_page_url, job_status, email , site_type ,more_page_status ,checkdup )"
        + " values ( ?, ?, ?, ? , ? ,? , ?)";
      // create the mysql insert preparedstatement
      PreparedStatement preparedStmt = con.prepareStatement(query);
      preparedStmt.setString (1, company_name);
      preparedStmt.setString (2, company_url);
      preparedStmt.setString (3, company_status);
      preparedStmt.setString (4, email_id);
      preparedStmt.setString (5, ats_check);
      preparedStmt.setString (6, next_page_check);
      preparedStmt.setString(7, jobtype);
      // execute the preparedstatement
    int a =  preparedStmt.executeUpdate();
       if(a>0){
    	   System.out.println("Successfully inserted :"+str);
       }
   }
        con.close();
        sc.close();
    }catch (Exception e){
      System.err.println(e.getMessage());
    }
  }
}