package com.hirrr.jobsnatcher.Util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.hirrr.jobsnatcher.Object.ScrapFields;


public class SinglePageFieldTitleDescScrap {

	/**
	 * Scrap title
	 * @return
	 */
	public String titleScrap(Document document, String firstTitle, String lastTitle){
		System.out.println("Started scrapping tool...");
		/**
		 * String type global variables
		 */
		String jobElemntsParent = "";

		/**
		 * Integer type global variables
		 */

		/**
		 * Boolean type global variables
		 */
		Boolean error2Flag = false;
		
		System.out.println(" title1 :" + firstTitle + " title2 :" + lastTitle );
		/**
		 * Invoking getDocumtResponse and getting document after scrap
		 */

		if(!document.toString().isEmpty()){
		/**
		 * Getting elements which have title
		 */
		Element[] elementArr = new ScrapCommonUtil().getElements(document, firstTitle, lastTitle);
		Element elementFirst = elementArr[0];
		Element elementLast = elementArr[1];
		if(elementFirst != null && elementLast != null ){
		/**
		 * CSS of first and second elements..
		 */
		String fisrtCSSSelecter1 = elementFirst.cssSelector();
		String fisrtCSSSelecter2 = elementLast.cssSelector();
		/**
		 * Getting elements with parent element at the top of the tree...
		 */
		try{
		elementFirst = new ScrapCommonUtil().getParentElements(document, elementFirst, elementLast, firstTitle, lastTitle);
		}catch(Exception e){
			error2Flag = true;
		}
		if(!error2Flag){
		Element elemntFirstForAttributCheck = elementFirst;
		/**
		 * CSSPath of parent
		 */
		jobElemntsParent = "";
		jobElemntsParent = new ScrapCommonUtil().gettCSSPathOfParent(elementFirst, document);
		/**
		 * Checking attribute cases..
		 */
		if(jobElemntsParent.isEmpty()){
			ScrapFields obj = new ScrapFields();
				/**
				 * AttributePath of parent
				 */
				jobElemntsParent = new ScrapCommonUtil().getAttrPathOfParent(elemntFirstForAttributCheck, document);
				/**
				 * In attribute case we were finding title incriment elements here..
				 */
				if(!jobElemntsParent.isEmpty()){
					Document document2 = Jsoup.parse(document.select(jobElemntsParent).toString());
					/**
					 * Getting elements which have title
					 */
					elementArr = new ScrapCommonUtil().getElements(document2, firstTitle, lastTitle);
					elementFirst = elementArr[0];
					elementLast = elementArr[1];
					fisrtCSSSelecter1 = elementFirst.cssSelector();
					fisrtCSSSelecter2 = elementLast.cssSelector();
					/**
					 * Getting attribute increment element..
					 */
					obj = new ScrapCommonUtil().getAttrIncrElement(obj, jobElemntsParent,document, fisrtCSSSelecter1, fisrtCSSSelecter2);
		
//					Integer titleIndexFlag = 0;
//					Integer title1Index = 0;
//					Integer title2Index = 0;
					String titleRemainStr = "";
					String titleIncStrElemnts = "";
					Elements elements = obj.getElements();
					/*title1Index = obj.getTitle1Index();
					title2Index = obj.getTitle2Index();
					titleIndexFlag = obj.getTitleIndexFlag();	*/
					titleRemainStr = obj.getTitleRemainStr();
					titleIncStrElemnts = obj.getTitleIncStrElemnts();
					System.out.println("No Of Jobs :: " + elements.size());
					/*return descFieldsScrap(elements, document, firstTitle, lastTitle, url, 
											noofJob, descRmvElemnt,titleIndexFlag,
											title1Index, title2Index, titleRemainStr,
											titleIncStrElemnts, jobElemntsParent, 
											descCodChooser, fieldVal, fieldNam);*/
					String titleStr = "";
					for(Integer i = 0; i < elements.size(); i++){
						//t!itleStr = titleStr + element.text();
						if(!titleRemainStr.isEmpty()){
						titleStr = document.select(titleIncStrElemnts).get(i).select(titleRemainStr).text().trim();
						}else{
							titleStr = document.select(titleIncStrElemnts).get(i).text().trim();
						}
						System.out.println(titleStr);
					}
					return elements.size() + "";
				}else{
					return "";
				}
		/**
		 * Checking increment element in id and class cases
		 */		
		}else{
			ScrapFields obj1 = new ScrapFields();
					/**
					 * Getting attribute increment element..
					 */
			obj1 = new ScrapCommonUtil().getClassIDIncrElement(obj1, document, jobElemntsParent, 
							fisrtCSSSelecter1, fisrtCSSSelecter2, firstTitle, lastTitle);
		
					/*Integer titleIndexFlag = 0;
					Integer title1Index = 0;
					Integer title2Index = 0;*/
					String titleRemainStr = "";
					String titleIncStrElemnts = "";
					
					Elements elements = obj1.getElements();
					/*title1Index = obj1.getTitle1Index();
					title2Index = obj1.getTitle2Index();
					titleIndexFlag = obj1.getTitleIndexFlag();	*/
					titleRemainStr = obj1.getTitleRemainStr();
					titleIncStrElemnts = obj1.getTitleIncStrElemnts();
					System.out.println("No Of Jobs :: " + elements.size());
					String titleStr = "";
					for(Integer i = 0; i < elements.size(); i++){
						//t!itleStr = titleStr + element.text();
						if(!titleRemainStr.isEmpty()){
						titleStr = document.select(titleIncStrElemnts).get(i).select(titleRemainStr).text().trim();
						}else{
							titleStr = document.select(titleIncStrElemnts).get(i).text().trim();
						}
						System.out.println(titleStr);
					}
				
					
					/*return descFieldsScrap(elements, document, firstTitle, lastTitle, url,
											noofJob, descRmvElemnt,titleIndexFlag,
											title1Index, title2Index, titleRemainStr, 
											titleIncStrElemnts, jobElemntsParent, 
											descCodChooser, fieldVal, fieldNam);*/
					return elements.size() + "";
		}
		}else{
		return "";
		}
		}else{
		return "";
		}
		}else{
		return "";
		}
	}
	
}
