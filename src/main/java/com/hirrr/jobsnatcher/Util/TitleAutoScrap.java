package com.hirrr.jobsnatcher.Util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;


public class TitleAutoScrap {

	public static String titleAutoScrap(Document document){
		String typeOfMatchFlag = "0";
		/**
		 * declarations
		 */
		Elements childElemnts = null;
		Integer levelCount = 0;
		/**
		 * Boolean flags
		 */
		Boolean isEndChild = false;
		/**
		 * Collections used
		 */
		Map<Integer, List<String>> childElemtsMap = new HashMap<Integer, List<String>>();
		/**
		 * Analysis of HTML document..
		 */
		document.select("script").remove();
		document.select("header").remove();
		document.select("footer").remove();
		Elements elements = document.select("body").first().children();
		do{
			isEndChild = false;
			childElemnts = new Elements();
			++levelCount;
			List<String> childList = new ArrayList<String>();
			for(Element element : elements){
			Elements elements2 = element.children();
			if(!element.ownText().isEmpty() && !element.ownText().equals("\u00A0")){
			/**
			 * Only taking titles match tags which have < 50 char size
			 */
			if(element.ownText().trim().length() < 80){
			childList.add(element.ownText().trim());
			}
			}
			if(elements2.size() != 0){
			isEndChild = true;
			childElemnts.addAll(elements2);
			}
			}
			elements = childElemnts;
			if(childList.size() != 0){
			childElemtsMap.put(levelCount, childList);
			}else{
				--levelCount;
			}
		}while(isEndChild);
		TitleAutoScrap obj = new TitleAutoScrap();
		/**
		 * Matching titles and deciding type of matching 
		 */
		try {
			typeOfMatchFlag = obj.titleMatcher(childElemtsMap, document);		
		} catch (Exception e) {
			typeOfMatchFlag = "0";
		}
		return typeOfMatchFlag;
	}
	/**
	 *  Matching titles and deciding type of matching 
	 * @param childElemtsMap
	 * @throws IOException
	 */
	public String titleMatcher(Map<Integer, List<String>>childElemtsMap, Document document) throws IOException{
		Integer noOfJobs = 0;
		String titleFrmText = "";
		/**
		 * type of Match
		 * 0 for No match
		 * 1 for single page
		 * 2 for Next page
		 * 3 for pdf format
		 */
		String typeOfMatchFlag = "0";
		Boolean exactFlag = false;
		Integer higherVal = 0;
		Integer levelCount = 0;
		/**
		 * Exact matched titles list
		 */
		List<String> titleMatchList = new ArrayList<String>();
		/**
		 * Titles list fron text file
		 */
		List<String> titleList = new ArrayList<String>();
		InputStream inputStream = getClass().getResourceAsStream("/jobtitle.txt");
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		while ((titleFrmText = reader.readLine()) != null) {
			if(titleFrmText.length() < 50){
			titleList.add(titleFrmText);
			}
		}
		/**
		 * Check matching for text files to scraped end tags for titles
		 */
		for(Entry<Integer, List<String>> entry:childElemtsMap.entrySet()){
			Integer count = 0;
//			System.out.println(">>>>>>>>>>>>>>>>>>>>...Level..." + entry.getKey() + ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>." );

				for(String scrapTitle : entry.getValue()){
					for(String texTitle:titleList){
						texTitle = texTitle.toUpperCase().trim();
						/**
						 * after title match remove code
						 */
						if(texTitle.contains("|")){continue;}
						scrapTitle = scrapTitle.toUpperCase();
						
						/**
						 * Title exact match
						 */
						if(texTitle.equals(scrapTitle)){
							System.out.println("Exact :: St ::" + scrapTitle + " :: Tt :: " + texTitle);
							exactFlag = true;
							++count;
						}
						/**
						 * Title medium match
						 */
						/*if(isContain(scrapTitle, texTitle)){
							System.out.println("Medium :: St ::" + scrapTitle + " :: Tt :: " + texTitle);
							exactFlag = true;
							++count;
						}
						*//**
						 * Title minor match
						 */
//						if(LetterPairSimilarityfinal.findpercentagematch(scrapTitle, texTitle) > .95){
//							System.out.println("Minor :: St ::" + scrapTitle + " :: Tt :: " + texTitle);
//						}
					}
				}
				/**
				 * Checking level match count
				 * and taking higher matched level
				 */
				if(higherVal < count){
					higherVal = count;
					levelCount = entry.getKey();
					System.out.println("Higher Level " + higherVal + " :: levelCount " + levelCount);
				}
		}
		titleMatchList = childElemtsMap.get(levelCount);
		if(exactFlag){
			int count = 0;
			typeOfMatchFlag = "1";
			String firstTitle = "";
			String secondTitle = "";
			for(String matchingTitle: titleMatchList){
				
				if(matchingTitle.isEmpty()){continue;}
				String tmptitle = matchingTitle;
				matchingTitle = matchingTitle.toUpperCase();
				
				/**
				 * for medium match 
				 */
				Boolean titleExactFlag = false;
				for(String texTitle:titleList){
					if(texTitle.contains("|")){continue;}
					if(isContain(matchingTitle, texTitle)){
						titleExactFlag = true;
						break;
					}else{
						titleExactFlag = false;
						continue;
					}
				}
				if(!titleExactFlag){continue;}
				
				++count;
				if(count == 1){
					firstTitle = tmptitle;
				}
				if(count == 2){
					secondTitle = tmptitle;
				}
				if(count > 2){break;}
				
			}
			Boolean flag1 = false;
			Boolean flag2 = false;
			/**
			 * only selected tags
			 */
			if(!firstTitle.isEmpty()){
				/**
				 * Checking next page here..by checking HREF in tag..
				 */
				try{
					flag1 = document.select("*:matchesOwn("+ firstTitle +")").hasAttr("href");
				}catch(Exception e){flag1 = false;}
				try{
					flag2 = document.select("*:matchesOwn("+ firstTitle +")").first().parent().hasAttr("href");
				}catch(Exception e){flag2 = false;}
//				if(flag1 || flag2){
//					otherwords = document.select("*:matchesOwn("+ firstTitle +")").first().tagName().contains(" h2, h3, h4, h5, h6 , b , a , td");
//			
//				}
			}else{
				typeOfMatchFlag = "0";
				System.out.println("Title Match Flag status :: " + typeOfMatchFlag + " :: Title Matched List ::" + titleMatchList + " :: 0 for No match/ 1 for single page/ 2 for Next page/ 3 for pdf format");
				return typeOfMatchFlag;
			}
				/**
				 * Next page check 
				 */
				if(flag1 || flag2){
						System.out.println("Next page site confirmed");
						typeOfMatchFlag = "2";
						/**
						 * PDF check
						 */
						if(flag1){
							String aTagStr = document.select("*:matchesOwn("+ firstTitle +")").select("a").attr("href");
							if(aTagStr.contains(".pdf")){
							typeOfMatchFlag = "3";
							System.out.println("Title Match Flag status :: " + typeOfMatchFlag + " :: Title Matched List ::" + titleMatchList + " :: 0 for No match/ 1 for single page/ 2 for Next page/ 3 for pdf format");
							return typeOfMatchFlag;
							}
							if(!aTagStr.equals("#")){
								typeOfMatchFlag = "2";
								System.out.println("Title Match Flag status :: " + typeOfMatchFlag + " :: Title Matched List ::" + titleMatchList + " :: 0 for No match/ 1 for single page/ 2 for Next page/ 3 for pdf format");
								return typeOfMatchFlag;
							}
							if(aTagStr.contains("#")){
								typeOfMatchFlag = "1" ;
								System.out.println("Title Match Flag status :: " + typeOfMatchFlag + " :: Title Matched List ::" + titleMatchList + " :: 0 for No match/ 1 for single page/ 2 for Next page/ 3 for pdf format");
								return typeOfMatchFlag;		
							}
						}
						if(flag2){
							String aTagStr = document.select("*:matchesOwn("+ firstTitle +")").first().parent().select("a").attr("href");
							if(aTagStr.contains(".pdf")){
							typeOfMatchFlag = "3";
							System.out.println("Title Match Flag status :: " + typeOfMatchFlag + " :: Title Matched List ::" + titleMatchList + " :: 0 for No match/ 1 for single page/ 2 for Next page/ 3 for pdf format");
							return typeOfMatchFlag;
							}
							if(!aTagStr.equals("#")){
								typeOfMatchFlag = "2";
								System.out.println("Title Match Flag status :: " + typeOfMatchFlag + " :: Title Matched List ::" + titleMatchList + " :: 0 for No match/ 1 for single page/ 2 for Next page/ 3 for pdf format");
								return typeOfMatchFlag;
							}
							if(aTagStr.contains("#")){
								typeOfMatchFlag = "1" ;
								System.out.println("Title Match Flag status :: " + typeOfMatchFlag + " :: Title Matched List ::" + titleMatchList + " :: 0 for No match/ 1 for single page/ 2 for Next page/ 3 for pdf format");
								return typeOfMatchFlag;		
							}
					   }
						
			 }
				
				
		//	Boolean error2Flag = false;	
			if(!secondTitle.isEmpty()){
				String noOfJobsStr = "";
				noOfJobsStr = new SinglePageFieldTitleDescScrap().titleScrap(document, firstTitle, secondTitle);
				try{
					noOfJobs = Integer.valueOf(noOfJobsStr);
				}catch(Exception e){
					noOfJobs = 0;
				}
				System.out.println("Total no of jobs ::" + noOfJobs);
				
				
				
				/*
					System.out.println("do this................................................>");
					Element elementFirst = document.select("*:containsOwn("+ firstTitle +")").first();
			        Element elementLast = document.select("*:containsOwn("+ firstTitle +")").last();
					
					Element[] elementArr = getElements(document, firstTitle, secondTitle);
					Element elementFirst = elementArr[0];
					Element elementLast = elementArr[1];
					if(elementFirst != null && elementLast != null ){
					*//**
					 * CSS of first and second elements..
					 *//*
					String fisrtCSSSelecter1 = elementFirst.cssSelector();
					String fisrtCSSSelecter2 = elementLast.cssSelector();
					*//**
					 * Getting elements with parent element at the top of the tree...
					 *//*
					try{
					elementFirst = getParentElements(document, elementFirst, elementLast, firstTitle, secondTitle);
					}catch(Exception e){
						error2Flag = true;
					}
					
					if(!error2Flag){
						Element elemntFirstForAttributCheck = elementFirst;
						*//**
						 * CSSPath of parent
						 *//*
						jobElemntsParent = "";
						jobElemntsParent = gettCSSPathOfParent(elementFirst, document);
					
					
					
					
					Elements elements = element.getAllElements();
					Boolean nextpageFlag = false;
					for(Element element2 : elements){
						String aTagStr = element2.attr("href");
						if(aTagStr.contains(".pdf")){
							typeOfMatchFlag = 3;
							System.out.println("Title Match Flag status :: " + typeOfMatchFlag + " :: Title Matched List ::" + titleMatchList + " :: 0 for No match/ 1 for single page/ 2 for Next page/ 3 for pdf format");
							return typeOfMatchFlag;
						}
						*//**
						 * check next page words there
						 *//*
						String str = element2.text().toUpperCase();
						if(str.contains("MORE") || 
								str.contains("DETAILS") ||
								str.contains("JOB DESCRIPTION")||
								str.contains("VIEW JOB OPENINGS")||
								str.contains("CLICK HERE")||
								str.contains("CONTINUE READING")||
								str.contains("VIEW")||
								str.contains("KNOW MORE")||
								str.contains("APPLY")
								
								){
							nextpageFlag = true;
						}
					}
					if(nextpageFlag){
						typeOfMatchFlag = 2;
						System.out.println("Title Match Flag status :: " + typeOfMatchFlag + " :: Title Matched List ::" + titleMatchList + " :: 0 for No match/ 1 for single page/ 2 for Next page/ 3 for pdf format");
						return typeOfMatchFlag;
					}
			*/}
			typeOfMatchFlag = "1";
		}else{
			typeOfMatchFlag = "0";
		}
		System.out.println("Title Match Flag status :: " + typeOfMatchFlag + " :: Title Matched List ::" + titleMatchList + " :: 0 for No match/ 1 for single page/ 2 for Next page/ 3 for pdf format");
		return typeOfMatchFlag + "~:~" + noOfJobs;
	}
		
		
	/**
	 * is contain
	 * @param source
	 * @param subItem
	 * @return
	 */
	private static boolean isContain(String source, String subItem){
        String pattern = "\\b"+subItem+"\\b";
        Pattern p=Pattern.compile(pattern);
        Matcher m=p.matcher(source);
        return m.find();
   }
	public static void main(String[] args) throws IOException, InterruptedException {
		
		String url = "http://www.jjwood.in/careers.htm";
		WebDriver driver = new FirefoxDriver();
		driver.get(url);
		Thread.sleep(3000);
		Document document = Jsoup.parse(driver.getPageSource());
		//System.out.println(document);
		titleAutoScrap(document);
		driver.quit();
	}
}