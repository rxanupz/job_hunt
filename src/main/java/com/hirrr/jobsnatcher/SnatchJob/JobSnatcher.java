package com.hirrr.jobsnatcher.SnatchJob;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;

import com.hirrr.jobsnatcher.DBConnection.DataSource;
import com.hirrr.jobsnatcher.Object.ResultObjects;
import com.hirrr.jobsnatcher.Util.TitleAutoScrap;

public class JobSnatcher {
	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	private static final Logger LOGGER = Logger.getLogger(JobSnatcher.class);

	public static void main(String[] args) throws IOException,InterruptedException {
		
	/**
	 * type of inputing mode..
	 * if the input data from file put typeOfExec as 'FILE'..
	 * if the input data from DB put typeOfExec as 'DB'..
	 */
	String typeOfExec= "DB"; 
    //typeOfExec = "XLXS";
	new JobSnatcher().jobsnatcherRead(typeOfExec);
	//new JobsnatcherFinal().writeToConsole();
	}
	
	/**
	 * invoking job snatcher..
	 * @param typeOfExec
	 * @throws InterruptedException 
	 * @throws IOException 
	 */
	public void jobsnatcherRead(String typeOfExec) throws IOException, InterruptedException{
		typeOfExec = typeOfExec.toUpperCase();
		if(typeOfExec.equals("DB")){
			jobsnatcherFrmDB();
		}else if(typeOfExec.equals("FILE")){
			jobsnatcherFrmFile();
		}
	}
	
	/**
	 * Company name list reading from DB..
	 * @throws InterruptedException 
	 * @throws IOException 
	 */
	public void jobsnatcherFrmDB() throws IOException, InterruptedException{
		ArrayList<String> companyList = new ArrayList<String>();
		DataSource ds = null;
		Connection con = null;
		Statement stmt = null;
		String compNM = "";
		/**
		 * DB connection creation..
		 */
		try {
			ds = DataSource.getInstance();
			con =  ds.getConnection();
		} catch (Exception e2) {
			e2.printStackTrace();
		}	
		try {
			stmt = con.createStatement();
			String sql = null;
			sql = "SELECT company_name FROM results_provider_db.jobsnatcher;";
			ResultSet rs = stmt.executeQuery(sql);

			while (rs.next()) {
				compNM = rs.getString("company_name").trim();
				companyList.add(compNM);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		/**
		 * if Company name list is not empty invoke job snatching..
		 */
		if(!companyList.isEmpty()){
			jobsSnatching(companyList, con);
		}
		if(stmt != null){try{stmt.close();}catch(Exception e){}}
		if(con != null){try{con.close();}catch(Exception e){}}
	}
	/**
	 * Reading Company names from file..
	 * @throws InterruptedException 
	 * @throws IOException 
	 */
	public void jobsnatcherFrmFile() throws IOException, InterruptedException{
		ArrayList<String> compList = new ArrayList<String>();
		/**
		 * DB connection creation..
		 */
		DataSource ds = null;
		Connection con = null;
		try {
			ds = DataSource.getInstance();
			con =  ds.getConnection();
		} catch (Exception e2) {
			e2.printStackTrace();
		}	
		// add xlsx reading code..
		/**
		 * if Company name list is not empty invoke job snatching..
		 */
		if(!compList.isEmpty()){
			jobsSnatching(compList, con);
		}
		if(con != null){try{con.close();}catch(Exception e){}}
	}
	/**
	 * Job snatching.. 
	 * @param companyList
	 * @throws InterruptedException 
	 * @throws IOException 
	 */
	public void jobsSnatching(ArrayList<String> compList, Connection con) throws IOException, InterruptedException{
		/**
		 * Result Object
		 */
		ResultObjects rsObj = new ResultObjects();
		/**
		 * Boolean variables
		 */
		Boolean firstFlag = true;
		Boolean atsFlag = false;
		Boolean jobBoardFlag = false;
		Boolean careerFlag = false;
//		Boolean exactMatchFlag = false;
//		Boolean noJobflag = false;
		Boolean nextPageFlag = false;
		/**
		 * Comapny career URL variable
		 */
		String companyURL = "";
		String careerPageURL = "";
		Integer typeOfMatchFlag = 0;
		String emailid ="";
		typeOfMatchFlag  = 0;
	    Integer noofjobs =0;
		/**
		 * Looping Company list 
		 */
		for(String companyname : compList){
				/**
				 * google company career searching words
				 */
				String[] searchWords = {"career", "careers", "jobs", "Vaccancy", "Current Opening"};
				/**
				 * Searching company career in google
				 */
				for(String sWords : searchWords){
					// Allow sWord as only careers
					if(!sWords.equals("career")){continue;}
					/**
					 * Google Search URL
					 */
					String googleURL = "https://www.google.co.in/#q=" + companyname + " "+ sWords +"";
					Elements elements = cmpNmSearchInGoogle(googleURL);
					/**
					 * Checking through search results(URL)
					 */
					firstFlag = true;
					for(Element element : elements) {
					//Only take first URL from result.. 	
					if(!firstFlag){ continue;}
					firstFlag = false;
					companyURL = element.select("a").first().attr("href").trim();
					/**
					 * Checking for ATS
					 */
					atsFlag = checkForATS(companyURL);
					/**
					 * if ATS Flag is true break the loop 
					 */
					if(atsFlag){
						rsObj.setSiteType("ATS");
						rsObj.setCareerPageURL(companyURL);
						rsObj.setJobStatus("NA");
						rsObj.setEmail("NA");
						rsObj.setMorePageStatus("NA");
						rsObj.setCheckDup("N");
						break;
					}else{
						/**
						 * if jobBoard Flag is true set career page URL as 'companyURL'
						 */
						jobBoardFlag = checkForJobBoard(companyURL);
					if(jobBoardFlag){
						rsObj.setSiteType("JOB BOARD/SOCIAL MEDIA");
						rsObj.setCareerPageURL(companyURL);
						rsObj.setJobStatus("NA");
						rsObj.setEmail("NA");
						rsObj.setMorePageStatus("NA");
						rsObj.setCheckDup("NA");
						break;
					}else{
										/**
										 * Checking start company URL is career page or not
										 * Getting document from career URL
										 */
										Document document = getDoc(companyURL);
										/**
										 * Checking jobs in career page
										 */
										Elements elements3 = urlFetcher(companyURL);
										for(Element element3 : elements3) {
										/**
										 * URL in web page	
										 */
										String nextPageUrl = element3.select("a").text();
										if(nextPageUrl.isEmpty()){continue;}
										careerFlag = checkForCareerPage(nextPageUrl);
										if(careerFlag){
											nextPageUrl = element3.select("a").attr("href");
										}else{
											/**
											 * if nextPageUrl is not a career page then continue
											 */
											rsObj.setSiteType("NO CAREER");
											rsObj.setCareerPageURL(companyURL);
											rsObj.setJobStatus("NA");
											rsObj.setEmail("NA");
											rsObj.setMorePageStatus("NA");
											rsObj.setCheckDup("NA");
											
											continue;
										}
										}
										/**
										 * perform Title Match
										 * if typeOfMatchFlag = 0 then no match
										 * if typeOfMatchFlag = 1 then single page
										 * if typeOfMatchFlag = 2 then next page
										 * if typeOfMatchFlag = 3 then pdf page
										 */
										 /**
										   * Title Match
										   */
//								    typeOfMatchFlag  = 0;
//								    Integer noofjobs =0;
									String 	typeOfMatchFlagStr = TitleAutoScrap.titleAutoScrap(document);
									if(typeOfMatchFlagStr.contains("~:~")){
										typeOfMatchFlag = Integer.valueOf(typeOfMatchFlagStr.split("~:~")[0]);
										noofjobs = Integer.valueOf(typeOfMatchFlagStr.split("~:~")[1]);
									}else{
										typeOfMatchFlag = Integer.valueOf(typeOfMatchFlagStr);
									}
										/**
										 * Email_Id Check
										 */
										 emailid = emailidFieldScrapper(document);
										/**
										 * Next page case	
										 */
										if(typeOfMatchFlag == 2){
											 nextPageFlag = checkForNextPage(document);
											 if(nextPageFlag){
												 rsObj.setMorePageStatus("YES");
											 }else {
												 rsObj.setMorePageStatus("NO");
											 }
											rsObj.setSiteType("Match IN TITLE");
											rsObj.setCareerPageURL(companyURL);
											rsObj.setJobStatus("YES");
											rsObj.setEmail(emailid);
											rsObj.setCheckDup("N");
											rsObj.setNoofjobs(noofjobs);
											break;
										/**
										 * pdf case	
										 */
										}else if(typeOfMatchFlag == 3){
											 if(nextPageFlag){
												 rsObj.setMorePageStatus("YES");
											 }else {
												 rsObj.setMorePageStatus("NO");
											 }
											rsObj.setSiteType("PDF");
											rsObj.setCareerPageURL(companyURL);
											rsObj.setJobStatus("YES");
											rsObj.setEmail(emailid);
											rsObj.setCheckDup("Y");
											rsObj.setNoofjobs(noofjobs);
											break;
										}else if(typeOfMatchFlag == 1){
											/**
											 * More pages check on single page matches
											 */
											 nextPageFlag = checkForNextPage(document);
											 if(nextPageFlag){
												 rsObj.setMorePageStatus("YES");
											 }else {
												 rsObj.setMorePageStatus("NO");
											 }
											rsObj.setSiteType("Match IN TITLE");
											rsObj.setCareerPageURL(companyURL);
											rsObj.setJobStatus("YES");
											rsObj.setEmail(emailid);
											rsObj.setCheckDup("Y");
											rsObj.setNoofjobs(noofjobs);
											break;
										}else{
													/**
													 * repeat to each page until title match find
													 */
//													while(true){
															/**
															 * Checking jobs in career page
															 */
															Elements elements2 = urlFetcher(companyURL);
															for(Element element2 : elements2) {
															/**
															 * URL in web page	
															 */
															String nextPageUrl = element2.select("a").text();
															if(nextPageUrl.isEmpty()){continue;}
															careerFlag = checkForCareerPage(nextPageUrl);
															if(careerFlag){
																nextPageUrl = element2.select("a").attr("href");
															}else{
																/**
																 * if nextPageUrl is not a career page then continue
																 */
																rsObj.setSiteType("NO CAREER");
																rsObj.setCareerPageURL(companyURL);
																rsObj.setJobStatus("NA");
																rsObj.setEmail("NA");
																rsObj.setMorePageStatus("NA");
																rsObj.setCheckDup("NA");
																rsObj.setNoofjobs(noofjobs);
																continue;
															}
															/**
															 *Go through next page url
															 */
															if(nextPageUrl.startsWith("http")){
																careerPageURL = nextPageUrl.trim();
															}else if(nextPageUrl.trim().isEmpty() || nextPageUrl.trim().startsWith("mailto:") || nextPageUrl.trim().equals("#") ){
																continue;
															}else {
																try{
																careerPageURL = nextPageUrl.trim();
																String newForwardUrl = "";
																/**
																  * If forward URL is not valid..And half of actual URL is in parent URL..
																  */
																String[] urlType = companyURL.split("//")[1].split("/");
																String f_URL = companyURL.split("//")[0] + "//";
																/**
																 * Checking URL..with connecting to description page..
																 */
																for(String urlSelector : urlType){
																	if(careerPageURL.startsWith("/")){
																		f_URL =  f_URL + urlSelector;
																	}else{
																		f_URL =  f_URL + urlSelector + "/";
																	}
																	newForwardUrl = f_URL + careerPageURL;
																	try{
																	System.out.println(newForwardUrl);
																	descScrap(newForwardUrl);
																	careerPageURL = newForwardUrl;
																	
																	break;
																	}catch(Exception e){
																		continue;
																	}
																  }
																}catch(Exception e){
																	continue;
																}
															  }
															if(!careerPageURL.isEmpty()){
																   /**	
																    * Check for ATS
																    */
																	atsFlag = checkForATS(careerPageURL);	
																	if(atsFlag){
																	    rsObj.setMorePageStatus("NA");
																		rsObj.setSiteType("ATS");
																		rsObj.setCareerPageURL(careerPageURL);
																		rsObj.setJobStatus("NA");
																		rsObj.setEmail("NA");
																		rsObj.setCheckDup("N");
																		rsObj.setNoofjobs(noofjobs);
																		break;
																	}else{
																	
																	/**
																	 * Getting document from career URL
																	 */
																	document = getDoc(careerPageURL);	
																	/**
																	 * perform Title Match
																	 * if typeOfMatchFlag = 0 then no match
																	 * if typeOfMatchFlag = 1 then single page
																	 * if typeOfMatchFlag = 2 then next page
																	 * if typeOfMatchFlag = 3 then pdf page
																	 */
																	 /**
																	   * Title Match
																	   */
															    typeOfMatchFlag  = 0;
															    noofjobs =0;
																typeOfMatchFlagStr = TitleAutoScrap.titleAutoScrap(document);
																if(typeOfMatchFlagStr.contains("~:~")){
																	typeOfMatchFlag = Integer.valueOf(typeOfMatchFlagStr.split("~:~")[0]);
																	noofjobs = Integer.valueOf(typeOfMatchFlagStr.split("~:~")[1]);
																}else{
																	typeOfMatchFlag = Integer.valueOf(typeOfMatchFlagStr);
																}
																	/**
																	 * if title Match is false 
																	 * No jobs in career page
																	 */
																	/*if(typeOfMatchFlag == 0){
																		rsObj.setMorePageStatus("NA");
																		rsObj.setSiteType("NO TITLE Match");
																		rsObj.setCareerPageURL(careerPageURL);
																		rsObj.setJobStatus("NA");
																		rsObj.setEmail("NA");
																		rsObj.setCheckDup("NA");
																		noJobflag = checkForNoJobs(document);
																		if(noJobflag){
																			rsObj.setSiteType("NO JOBS");
																			jobStatus = "NO";
																			rsObj.setJobStatus("NO");
																			rsObj.setEmail(emailid);
																		}
																		break;*/
																	/**
																	 * Next page case	
																	 */
																	if(typeOfMatchFlag == 2){
																		 nextPageFlag = checkForNextPage(document);
																		 if(nextPageFlag){
																			 rsObj.setMorePageStatus("YES");
																		 }else {
																			 rsObj.setMorePageStatus("NO");
																		 }
																			rsObj.setSiteType("Match IN TITLE");
																			rsObj.setCareerPageURL(careerPageURL);
																			rsObj.setJobStatus("YES");
																			rsObj.setEmail(emailid);
																			rsObj.setCheckDup("N");
																			rsObj.setNoofjobs(noofjobs);
																			break;
																	/**
																	 * pdf case	
																	 */
																	}else if(typeOfMatchFlag == 3){
																		 nextPageFlag = checkForNextPage(document);
																		 if(nextPageFlag){
																			 rsObj.setMorePageStatus("YES");
																		 }else {
																			 rsObj.setMorePageStatus("NO");
																		 }
																			rsObj.setSiteType("PDF");
																			rsObj.setCareerPageURL(careerPageURL);
																			rsObj.setJobStatus("YES");
																			rsObj.setEmail(emailid);
																			rsObj.setCheckDup("Y");
																			rsObj.setNoofjobs(noofjobs);
																			break;
																	}else if(typeOfMatchFlag == 1){
																		/**
																		 * More pages check on single page matches
																		 */
																		 nextPageFlag = checkForNextPage(document);
																		 if(nextPageFlag){
																			 rsObj.setMorePageStatus("YES");
																		 }else {
																			 rsObj.setMorePageStatus("NO");
																		 }
																			rsObj.setSiteType("Match IN TITLE");
																			rsObj.setCareerPageURL(careerPageURL);
																			rsObj.setJobStatus("YES");
																			rsObj.setEmail(emailid);
																			rsObj.setCheckDup("Y");
																			rsObj.setNoofjobs(noofjobs);
																			break;
																	}
																 }
														      }
//														  }
														}
													}
										}
					  }
					}
				}

				String query = "update jobsnatcher set  career_page_url = ? ,job_status = ? , email= ? , site_type =? , more_page_status = ? , checkdup = ? , noofjobs = ? where company_name = ?";
				PreparedStatement preparedStmt = null;
				try{
				preparedStmt = con.prepareStatement(query);
				preparedStmt.setString(1, rsObj.getCareerPageURL());
				preparedStmt.setString(2, rsObj.getJobStatus());
				preparedStmt.setString(3, rsObj.getEmail());
				preparedStmt.setString(4, rsObj.getSiteType());
				preparedStmt.setString(5, rsObj.getMorePageStatus());
				preparedStmt.setString(6, rsObj.getCheckDup());
				preparedStmt.setInt(7, rsObj.getNoofjobs());
				preparedStmt.setString(8, companyname);
				int a  = preparedStmt.executeUpdate();
				if(a>0){
					System.out.println("Successfully updated..");
				}
				}catch(Exception e){
					System.out.println(e.getMessage());
				}
				if(preparedStmt != null){try{preparedStmt.close();}catch(Exception e){System.out.println(e.getMessage());}}
				System.out.println(companyname + " ---> " + rsObj.getCareerPageURL()+ "---->" + rsObj.getJobStatus() + " ----> " + rsObj.getEmail()+ "---->"+rsObj.getSiteType()+"---->"+rsObj.getMorePageStatus()+"---->"+rsObj.getCheckDup()+"---->"+rsObj.getCheckDup());
				LOGGER.info(companyname + " ---> " + rsObj.getCareerPageURL()+ "---->" + rsObj.getJobStatus() + " ----> " + rsObj.getEmail()+ "---->"+rsObj.getSiteType()+"---->"+rsObj.getMorePageStatus()+"---->"+rsObj.getCheckDup()+"---->"+rsObj.getCheckDup());

		}
	}

	/**
	 * Google search by company name
	 * @param googleSearchURL
	 * @throws InterruptedException 
	 */
	public Elements cmpNmSearchInGoogle(String googleSearchURL) throws InterruptedException{
		/**
		 * Web driver object creation and connecting..
		 */
		Elements elements = null;
		Boolean careerPageLinkFlag = false;
		 String Xport = System.getProperty("lmportal.xvfb.id", ":1");
	        FirefoxBinary firefoxBinary = new FirefoxBinary();
	        firefoxBinary.setEnvironmentProperty("DISPLAY", Xport);
	        WebDriver driver = new FirefoxDriver(firefoxBinary, null);
		driver.get(googleSearchURL);
		Thread.sleep(3000);
		Document document = Jsoup.parse(driver.getPageSource());
		/**
		 * Checking front page have current opening link below the company url
		 */
		if(document.hasClass("nrgt")){
			elements = document.select(".nrgt").select("a");
			for(Element element : elements){
				String urlText = element.text().toUpperCase();
				if(		
						urlText.contains("CURRENT JOB OPENINGS") ||
						urlText.contains("CURRENT OPENINGS")||
						urlText.contains("JOB LISTING")
				){
					careerPageLinkFlag = true;
					elements = element.getAllElements();
				}
			}
		}
		if(!careerPageLinkFlag){
		elements = document.select(".rc").select("h3").select("a");
		}
		driver.quit();
		return elements;
	}
	/**
	 * URLs finding from a web page
	 */
	public Elements urlFetcher(String urlToSearch){
		Elements elements = null;
		 String Xport = System.getProperty("lmportal.xvfb.id", ":1");
	     FirefoxBinary firefoxBinary = new FirefoxBinary();
	     firefoxBinary.setEnvironmentProperty("DISPLAY", Xport);
	     WebDriver driver = new FirefoxDriver(firefoxBinary, null);
		driver.get(urlToSearch);		    
		Document doc = Jsoup.parse(driver.getPageSource());
		elements = doc.select("a");
		driver.quit();
	    return elements;
	}
	/**
	 * parsing a url and taking document
	 * @return
	 * @throws InterruptedException 
	 */
	public Document getDoc(String careerUrl) throws InterruptedException{
		 String Xport = System.getProperty("lmportal.xvfb.id", ":1");
	        FirefoxBinary firefoxBinary = new FirefoxBinary();
	        firefoxBinary.setEnvironmentProperty("DISPLAY", Xport);
	        WebDriver driver = new FirefoxDriver(firefoxBinary, null);
		driver.get(careerUrl);
		Thread.sleep(3000);
		Document document = Jsoup.parse(driver.getPageSource());
		try{
		String loc[]={"Search Openings","Search","Find","View Open Jobs"};
		for(int i=0;i<loc.length;i++){ 
		driver.findElement(By.partialLinkText(loc[i])).click();
		Thread.sleep(2000);
		}
		}catch(Exception e){}
		try{
		driver.findElement(By.tagName("input")).click();
		}catch(Exception e){}
		driver.quit();
		return document;
	}
	/**
	 * Checking URLs for ATS 
	 * @param url
	 */
	public Boolean checkForATS(String compUrl){
		Boolean atsFlag = false;
		compUrl = compUrl.toUpperCase();
		/**
		 * ATS words
		 */
		if(	
				compUrl.contains("TALEO")||
				compUrl.contains("BRASSRING")||
				compUrl.contains("JOBVITE")||
				compUrl.contains("APPLICANTTRACKINGSYSTEM")||
				compUrl.contains("ANGEL.CO")||
				compUrl.contains("RECRUITERBOX")||
				compUrl.contains("GREENHOUSE")||
				compUrl.contains("SUCCESSFACTORS")||
				compUrl.contains("SMARTRECRUITERS")||
				compUrl.contains("JOBSCORE")||
				compUrl.contains("ICIMS")||
				compUrl.contains("OPENHIRE.SILKROAD")||
				compUrl.contains("CATSONE")||
				compUrl.contains("HIREBRIDGE")||
				compUrl.contains("PEOPLECLICK")||
				compUrl.contains("WORKABLE")||
				compUrl.contains("LEVER")||
				compUrl.contains("APPLY2JOBS")||
				compUrl.contains("ZOHO")
		){
			atsFlag = true;
		}
		return atsFlag;
	}
	/**
	 * Checking URLs for job board and social media sites
	 * @param url
	 * @return
	 */
	public Boolean checkForJobBoard(String compUrl){
		Boolean jobBoardFlag = false;
		compUrl = compUrl.toUpperCase();
		/**
		 * Job board check
		 */
		if(
				compUrl.contains("INDEED")||
				compUrl.contains("NAUKRI.COM")||
				compUrl.contains("LINKEDIN")||
				compUrl.contains("WIKIPEDIA")||
				compUrl.contains("JORA")||
				compUrl.contains("CAREESMA")||
				compUrl.contains("FACEBOOK")||
				compUrl.contains("SHINE")||
				compUrl.contains("TIMESJOBS")||
				compUrl.contains("GLASSDOOR")||
				compUrl.contains("INDEED")||
				compUrl.contains("QUIKR")||
				compUrl.contains("RAGNS.COM")
		){
			jobBoardFlag = true;
		}
		return jobBoardFlag;
	}
	/**
	 * Career page confirmation by checking some words in page
	 * @param pageUrl
	 * @return
	 */
	public Boolean checkForCareerPage(String pageUrl){
		Boolean careerFlag = false;
		pageUrl = pageUrl.toUpperCase();
		if(
				pageUrl.contains("CAREER")||
//				pageUrl.contains("JOIN")||
				pageUrl.contains("JOBS")||
				pageUrl.contains("CURRENT OPENING")||
				pageUrl.contains("CURRENT JOBS")||
				pageUrl.contains("OPPORTUNITIES")||
				pageUrl.contains("OPPURTUNITY")||
				pageUrl.contains("VANCANCY")||
				pageUrl.contains("VACCANCIES")||
				pageUrl.contains("VACCANT")||
				pageUrl.contains("SEE JOBS")||
				pageUrl.contains("POSITIONS")||
				pageUrl.contains("FRESHERS")||
				pageUrl.contains("EXPERIENCE")||
				pageUrl.contains("CLICK HERE")||
				pageUrl.contains("POSITIONS IN INDIA")||
				pageUrl.contains("SEE JOB")||
				pageUrl.contains("ENTRY LEVEL")||
				pageUrl.contains("JOIN OUR TEAM")||
				pageUrl.contains("GLOBAL OPENING")||
				pageUrl.contains("INDIA OPENING")||
				pageUrl.contains("OPENING")||
				pageUrl.equals("TEAM")
		){
			careerFlag = true; 
		}
		return careerFlag;
	}
	/**
	 * Checking career page have jobs or not
	 * @param pageUrl
	 * @return
	 */
	public Boolean checkForNoJobs(Document document){
		Boolean careerJobsFlag = false;
		String docTxt = document.text().replaceAll("[^a-zA-Z]", "").replaceAll("\\s+", "").trim().toUpperCase();
		if(
			docTxt.contains("NOJOB")||
			docTxt.contains("NOVACANCY")
		){
			careerJobsFlag = true; 
		}
		return careerJobsFlag;
	}
	/**
	 * Checking for more page tags
	 * @param document
	 * @return
	 */
	public Boolean checkForNextPage(Document document){
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
	 * Find Other URLS
	 * @return
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	public Document descScrap(String url) throws Exception{
		  Document document = null;
	        try {
	            document = Jsoup.connect(url).timeout(30000).get();
	        } catch (Exception e) {
			     document = Jsoup.connect(url).userAgent("Mozilla/5.0 (X11; Linux x86_64; rv:42.0) Gecko/20100101 Firefox/42.0").timeout(30000).get();
	        }
	        
		return document;
	}
	}