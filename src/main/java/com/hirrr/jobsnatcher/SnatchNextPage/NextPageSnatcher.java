package com.hirrr.jobsnatcher.SnatchNextPage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.hirrr.jobsnatcher.DBConnection.DataSource;
import com.hirrr.jobsnatcher.Util.TitleAutoScrap;

public class NextPageSnatcher {static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";

private static final Logger LOGGER = Logger.getLogger(NextPageSnatcher.class);

	public static void main(String[] args) throws IOException,InterruptedException {
		
		
		
		FileWriter wr = new FileWriter(new File("/home/worklyf/Desktop/CompanyUrl/NextPageCase2.txt"));
		FileOutputStream fos = new FileOutputStream(new File("/home/worklyf/Desktop/CompanyUrl/ExceptionURL.txt"), true); 
		PrintStream ps = new PrintStream(fos);
		Statement stmt = null;
		String companyurl = null;
		String companyname = null;
		DataSource ds = null;
		Connection con = null;
		try {
			ds = DataSource.getInstance();
			con = ds.getConnection();
		} catch (Exception e2) {
			e2.printStackTrace();
		}
		try {
			stmt = con.createStatement();
			String sql = null;
			sql = "SELECT companyurl , companyname FROM results_provider_db.Next_Page_Case;";
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				companyname = rs.getString("companyname");
				companyurl = rs.getString("companyurl");
				String email = null;
				String emailid = null;
				String nxtpage = null;
				String checkDup = null;
				String matchingtitle = null;
				String pdf = null;
				String urlss = null;
				Document document = null;
				Boolean nextPageFlag = false;
				try {
					if(!companyurl.startsWith("http")){
						checkDup ="Given URL is Wrong";
						matchingtitle = "";
					    email = "";
						nxtpage = "";
						break;
					}
					try{
					document = Jsoup.connect(companyurl).timeout(30000).get();
					}catch(Exception e){  
					document = Jsoup.connect(companyurl).userAgent("Mozilla/5.0").timeout(30000).get();
					}
					URL urls =new URL(companyurl); 
					urlss =urls.getProtocol()+"://"+urls.getHost()+"/";
//					urls.
					if(companyurl.equals(urlss)){
						checkDup ="Pls Chk this URL ";
						matchingtitle = "";
					    email = "";
						nxtpage = "";
						break;
					}else{
					if(companyurl.contains("taleo")||companyurl.contains("brassring")||companyurl.contains("naukri")||companyurl.contains("jobvite")
							||companyurl.contains("applicanttrackingsystem")||companyurl.contains("angel.co")||companyurl.contains("recruiterbox")
							||companyurl.contains("greenhouse")||companyurl.contains("successfactors")||companyurl.contains("smartrecruiters")
							||companyurl.contains("jobscore")||companyurl.contains("icims")||companyurl.contains("silkroad")
							||companyurl.contains("catsone")||companyurl.contains("hirebridge")){
						checkDup = "ATS";
						matchingtitle = "";
					    email = "";
						nxtpage = "";
						break;
					}else{
					
					Elements elem = document.select("a");
					for (Element ele : elem) {

						/**
						 * Email_Id Check
						 */
						 emailid = emailidFieldScrapper(document);
							pdf = ele.select("a").attr("href");
							if(pdf.contains("application")){
							continue;
							}
								 if(pdf.contains(".docx")||pdf.contains(".pdf")){
									checkDup = "PDF";
									matchingtitle = "Match IN TITLE";
								    email = emailid;
									break;
								 }
 
								 /**
								   * Title Match
								   */
						    Integer typeOfMatchFlag  = 0;
						    Integer noofjobs =0;
							String 	typeOfMatchFlagStr = TitleAutoScrap.titleAutoScrap(document);
							if(typeOfMatchFlagStr.contains("~:~")){
								typeOfMatchFlag = Integer.valueOf(typeOfMatchFlagStr.split("~:~")[0]);
								noofjobs = Integer.valueOf(typeOfMatchFlagStr.split("~:~")[1]);
							}else{
								typeOfMatchFlag = Integer.valueOf(typeOfMatchFlagStr);
							}
							
									
								 /**
									 * Next page case	
									 */
									
									if(typeOfMatchFlag == 2){
										 nextPageFlag = checkForNextPage(document);
										 if(nextPageFlag){
											 nxtpage = "More Than 1 Page";
										 }else {
											 nxtpage = "Single Page";
										 }
									    matchingtitle = "Match IN TITLE";
									    email = emailid ;
										checkDup = "N";
										break;
									}
							
							
					
					if (typeOfMatchFlag == 1){
						checkDup = "Y";
						matchingtitle = "Match IN TITLE";
					    email = emailid;
						break;
					}
					if (typeOfMatchFlag == 0){
						checkDup = "No Titles matched";
						matchingtitle = "";
					    email = emailid;
						break;
					}
					if (typeOfMatchFlag == 2){
						checkDup = "N";
						matchingtitle = "Match IN TITLE";
					    email = emailid;
						break;
					}
					if (typeOfMatchFlag == 3){
						checkDup = "PDF";
						matchingtitle = "Match IN TITLE";
					    email = emailid;
						break;
					}
					
					}
					}
					}

					wr.write(companyname+"--->"+companyurl + "--->" + checkDup + "--->"+ nxtpage + "--->" + email + "--->" +matchingtitle + "\n");
					System.out.println(companyname+"--->"+companyurl + "--->" + checkDup + "--->"+ nxtpage + "--->" + email + "--->" +matchingtitle );
					LOGGER.info(companyname+"--->"+companyurl + "--->" + checkDup + "--->"+ nxtpage + "--->" + email + "--->" +matchingtitle);
					String query1 = "update Next_Page_Case set pagetype = ? , nxtpage =? , email = ? ,titlematch = ?   where companyurl = ? AND companyname = ? ";
					PreparedStatement preparedStmt = con.prepareStatement(query1);
					preparedStmt.setString(1, checkDup);
					preparedStmt.setString(2, nxtpage);
					preparedStmt.setString(3, email);
					preparedStmt.setString(4, matchingtitle);
					preparedStmt.setString(5, companyurl);
					preparedStmt.setString(6, companyname);
					int a  = preparedStmt.executeUpdate();

					if(a>0){
						System.out.println("Success");
					}
					else{
						System.out.println("Failed");
					}
					
					String sqlquery = "update Wrapped set checkdup = ? , page =? , emailid = ?  where company_url = ? AND company_name = ? ";
					PreparedStatement st = con.prepareStatement(sqlquery);
					st.setString(1, checkDup);
					st.setString(2, nxtpage);
					st.setString(3, email);
					st.setString(4, companyurl);
					st.setString(5, companyname);
					int b = st.executeUpdate();
					if(b>0){
						System.out.println("Updated Wrapped");
					}
					else{
						System.out.println("Failed to update in Wrapped");
					}
					
				} catch (Exception ex) {
					ex.printStackTrace();
					ex.printStackTrace(ps);
				}
				
			}
			    wr.flush();
			    wr.close();
				rs.close();
				stmt.close();
				con.close();
			
		} catch (SQLException se) {
			se.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * Checking for Email_id the pageUrl 
	 * @param jobDesc
	 * @return
	 */
	
	public static String emailidFieldScrapper(Document document){
		String jobDesc = document.select("body") + "";
		String emailid = "";
		try{
		if(!jobDesc.isEmpty()){
		ArrayList<String> arrayList = new ArrayList<String>();
		Boolean first = true;
		 Pattern p = Pattern.compile("([a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-z]{2,6})");
		 Matcher m = null;                    
		 m = p.matcher(jobDesc);
		 while(m.find()){
			int start = m.start();
			int end = m.end();
			String jobDescStr = jobDesc.substring(start, end);
			arrayList.add(jobDescStr);
		 }
		 for(String arrayElement : arrayList){
			 if(first){
				 first = false;
				 emailid = arrayElement;
//				 continue;
			 }
			 if(!emailid.contains(arrayElement)){
			 emailid = emailid + "~:~" + arrayElement;
			 }
		 }
		}
		}catch(Exception e){emailid = "";}
		 return emailid;
	}
	/**
	 * Checking for more page tags
	 * @param document
	 * @return
	 */
	public static Boolean checkForNextPage(Document document){
		Boolean nextPageFlag = false;
		Elements elements = document.select("a");
		for(Element element : elements){
			String nextPageStr = element.text().trim().toUpperCase();
			/**
			 * next page words check
			 */
			if (nextPageStr.equals("NEXT")|| 
				nextPageStr.equals("›")|| 
				nextPageStr.equals("NEXT »") || 
				nextPageStr.equals("NEXT PAGE")) {
				nextPageFlag = true;
			}
		}
		return nextPageFlag;
}
}