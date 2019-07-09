package com.hirrr.jobsnatcher.Util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.hirrr.jobsnatcher.Object.ScrapFields;

public class ScrapCommonUtil {
	
	/**
	 * To get tag element name from string..
	 * @param document
	 * @param firstTitle
	 * @param lastTitle
	 * @param titleListNo
	 * @return
	 */
	public Element[] getElements(Document document, String firstTitle, String lastTitle){
		Element elementFirst = null;
		Element elementLast = null;
		if(firstTitle.equals(lastTitle)){
			elementFirst = document.select("*:containsOwn("+ firstTitle +")").first();
			elementLast = document.select("*:containsOwn("+ lastTitle +")").last();

		}else {
			elementFirst = document.select("*:containsOwn("+ firstTitle +")").first();
			elementLast = document.select("*:containsOwn("+ lastTitle +")").first();
		}
		Element[] elementArr = {elementFirst, elementLast};
		return elementArr;
	}
	/**
	 * getting parent element tag using childs
	 * @param document
	 * @param elementFirst
	 * @param elementLast
	 * @param firstTitle
	 * @param lastTitle
	 * @param titleListNo
	 * @return
	 */
	public Element getParentElements(Document document,Element elementFirst, Element elementLast, String firstTitle, String lastTitle){
		Element elementFirstCopy = elementFirst;
		Element elementLastCopy = elementLast;
		try{
			while (elementFirst != elementLast) {
				elementFirst = elementFirst.parent();
				elementLast = elementLast.parent();
			}
		}catch(Exception e){
				elementFirst = elementFirstCopy;
				elementLast = elementLastCopy;
				if(!(elementFirst.tagName().equals(elementLast.tagName()))){
					//first and second parent
					if(elementFirst.tagName() == elementLast.parent().tagName()){
						elementLast = elementLast.parent();
						while (elementFirst != elementLast) {
							elementFirst = elementFirst.parent();
							elementLast = elementLast.parent();
							}
					//first parent and second 
					}else if(elementFirst.parent().tagName() == elementLast.tagName()){
						elementFirst = elementFirst.parent();
						while (elementFirst != elementLast) {
							elementFirst = elementFirst.parent();
							elementLast = elementLast.parent();
							}
					}
				}else if(elementFirst.tagName().equals(elementLast.tagName())){
	//				#aboutus-main > div.bg-about > div.career-text > span.style2
	//				#aboutus-main > div.bg-about > div.career-text > p:nth-child(7) > span.style2
					if(elementFirst.hasAttr("class") && elementLast.hasAttr("class")){
						String class1 = elementFirst.className();
						String class2 = elementLast.className();
										while(elementFirst != elementLast){
											if(!(elementFirst.select("." + class1).size() > 1))
												elementFirst = elementFirst.parent();
											if(!(elementLast.select("." + class2).size() > 1))
												elementLast = elementLast.parent();
										}
					}
				}
		}
		return elementFirst;
	}
	/**
	 * Getting XPath/CSSPath of parent elemnt
	 * @param elementFirst
	 * @return
	 */
	public String gettCSSPathOfParent(Element element, Document document){
		String jobElemntsParent = "";
		Boolean first = false; 
		while(jobElemntsParent == ""){
			try{
					//enter to the if statment only after the first while loop..
					if(first){ element = element.parent();}
					first = true;
					/**
					 *  non-empty id..
					 */
					if(!"".equals(element.id())){
					Integer size = document.select( element.tagName() + "#" + element.id()).size();
					if(size == 1){
					jobElemntsParent = "#" + element.id();//Fixed parent with id
					}
					}
					/**
					 *  non-empty class..
					 */
					else if(!"".equals(element.className())){
					//Checking class is unique or not
	//feb 20				//Integer size = document.select( element.tagName() + "[class=" + element.className()+"]").size();
					Integer size = document.select( element.tagName() + "." + element.className()).size();
					if(size == 1){
					jobElemntsParent =  element.tagName() + "." + element.className().replace(" ", ".");//Fixed parent with class
					}
					}
			}catch(Exception e){
				break;
			}
		}
		return  jobElemntsParent;
	}
	/**
	 * getting attribute path of parent
	 * @param element
	 * @param document
	 * @return
	 */
	public String getAttrPathOfParent(Element element, Document document){
		String jobElemntsParent = "";
		Boolean first = false; 
		while(jobElemntsParent == ""){
			try{
				if(first){ element = element.parent();}
				first = true;
				/**
				 * non-empty tag with attributes
				 */
				if(!"".equals(element.attributes().toString())){
				String attributes = element.attributes().toString().replace("\"", "");
				Integer resutelemntSize = document.select(element.tagName() + "[" + attributes.trim().replace(" ", "][") + "]").size();
				//Checking attribute is unique or not
				if( resutelemntSize == 1){
				jobElemntsParent = element.tagName() + "[" + attributes.trim().replace(" ", "][") + "]";
				}
				}
			}catch (Exception e) {
				break;
			}  
		}
		return jobElemntsParent;
	}
	/**
	 * getting increment elemnt for Attribute case
	 * @param scrapFields
	 * @param jobElemntsParent
	 * @param document
	 * @param fisrtCSSSelecter1
	 * @param fisrtCSSSelecter2
	 * @return
	 */
	public ScrapFields getAttrIncrElement(ScrapFields scrapFields, String jobElemntsParent,
									Document document, String fisrtCSSSelecter1,
									String fisrtCSSSelecter2){
		Integer titleIndexFlag = 0;
		Integer title1Index = 0;
		Integer title2Index = 0;
		String titleRemainStr = "";
		String titleIncStrElemnts = "";
		String title1IncStr = "";
		String title2IncStr = "";
		Boolean isRemovCSS = false;
		String titleSubStr = "";

		/**
		 * css of two titles
		 */
		String title1ElementStr = fisrtCSSSelecter1;
		String title2ElementStr = fisrtCSSSelecter2;
		/**
		 * To find title incrimrnt element...
		 */
		if(title1ElementStr.contains(">") && title2ElementStr.contains(">"))
		{
		String[] title1ElementArr = title1ElementStr.split(">");
		String[] title2ElementArr = title2ElementStr.split(">");
		for(int i = 0; i < title1ElementArr.length; i++){
			/**
			 * Loop are skipping here...if it have html and body in its css..
			 */
			if((title1ElementArr[i].trim().equals("html")) || (title1ElementArr[i].trim().equals("body"))){
				continue;
			}
			if(!title1ElementArr[i].equals(title2ElementArr[i])){
					if(title1ElementArr[i].contains("(")){
							title1IncStr = title1ElementArr[i];
							title2IncStr = title2ElementArr[i];
							try{
							String titleInc1 = title1ElementArr[i].substring(0, title1ElementArr[i].indexOf("("));
							String titleInc2 = "";
							if(title2IncStr.contains("(")){
							titleInc2 = title2ElementArr[i].substring(0, title2ElementArr[i].indexOf("("));
							}else{
								titleInc2 = title2ElementArr[i];
							}
							if(!titleInc1.equals(titleInc2)){
							isRemovCSS = true;
							}
							}catch(Exception e){
							e.printStackTrace();
							}
							String str1 = title1ElementArr[i];
							String str2 = title2ElementArr[i];
							try{
							title1Index = Integer.valueOf(str1.substring(str1.indexOf("(") + 1, str1.indexOf(")")));
							}catch(Exception e){
								title1Index = 0;
							}
							try{
							title2Index = Integer.valueOf(str2.substring(str2.indexOf("(") + 1, str2.indexOf(")")));
							}catch(Exception e){
								title2Index = 0;
							}
							break;
					}else if(title2ElementArr[i].contains("(")){
							System.out.println("Check point.. if title element 1 is not working..");
							title2IncStr = title2ElementArr[i];
							isRemovCSS = true;
							titleIndexFlag = 1;
							break;
					}else{
						//editing feb 09									
						title1IncStr = title1ElementArr[i];
						title2IncStr = title2ElementArr[i];
						isRemovCSS = true;
						title1Index = 1;
						break;
					//	titleSubStr = titleSubStr + title1ElementArr[i] + ">" ;

					}
					//	titleSubStr = titleSubStr + title1ElementArr[i] + ">" ;
			}
				titleSubStr = titleSubStr + title1ElementArr[i] + ">" ;
			}
		}
		String title1RemainStr = "";
		String title2RemainStr = "";
		/**
		 * identifying title iterating element..
		 */
		if(title1ElementStr.contains(title1IncStr) && title2ElementStr.contains(title2IncStr)){
			int j = title1ElementStr.indexOf(title1IncStr);
			int k =  title2ElementStr.indexOf(title2IncStr);
			if(j == k){
			title1RemainStr = title1ElementStr.substring(title1ElementStr.indexOf(title1IncStr) + title1IncStr.length(), title1ElementStr.length());
			title2RemainStr =  title2ElementStr.substring(title2ElementStr.indexOf(title2IncStr) + title2IncStr.length(), title2ElementStr.length());
			}else if(j > k){
				title1RemainStr = title1ElementStr.substring( j + title1IncStr.length(), title1ElementStr.length());
				title2RemainStr =  title2ElementStr.substring(j + title2IncStr.length(), title2ElementStr.length());
					
			}else {
				title1RemainStr = title1ElementStr.substring( k + title1IncStr.length(), title1ElementStr.length());
				title2RemainStr =  title2ElementStr.substring(k + title2IncStr.length(), title2ElementStr.length());
			}

			//html > body > table > tbody > tr:nth-child(6) > td.Mattercs > table.careers-table > tbody > tr.careers-tr > td.careers-text1
			//html > body > table > tbody > tr:nth-child(8) > td > table.careers-table > tbody > tr:nth-child(2) > td.careers-text1
			//remain_str different for each jobs...
			/*if(title1RemainStr.contains(">") && title2RemainStr.contains(">")){
				String[] title1RemainStrArr = title1RemainStr.split(">");
				String[] title2RemainStrArr = title2RemainStr.split(">");
//...........................>Title remain cases...	
				for(int i = 0; i < title1RemainStrArr.length; i++ ){
					if(title2RemainStrArr[i].isEmpty()){continue;}//may be titl1 and title2 css are different in length..
					if(!title1RemainStrArr[i].equals(title2RemainStrArr[i])){
						//#content > div.row.fixed > div.col-700.last > table.auto-style3 > tbody > tr:nth-child(2) > td.auto-style5:nth-child(2)
//edited												//#content > div.row.fixed > div.col-700.last > table.auto-style3 > tbody > tr:nth-child(3) > td.auto-style4:nth-child(2)
						//table.auto-style3
						if(title1RemainStrArr[i].contains("(")){
							if(title1RemainStrArr[i].contains(".")){
								if(title2RemainStrArr[i].contains(".")){
									String strRmn1 = title1RemainStrArr[i];
									String strRmn2 = title2RemainStrArr[i];
									String strClass1 = strRmn1.substring(strRmn1.indexOf("."), strRmn1.indexOf(":"));
									String strClass2 = "";
									if(title2RemainStrArr[i].contains("(")){
										strClass2 = strRmn2.substring(strRmn2.indexOf("."), strRmn2.indexOf(":"));
									}else{
										strClass2 = strRmn2.substring(strRmn2.indexOf("."), strRmn2.length());
									}
									if(strClass1.equals(strClass2)){
										 titleRemainStr = titleRemainStr + " > " + title1RemainStrArr[i].trim();
									}else{
										strRmn1 = strRmn1.replace(strClass1, "");
										titleRemainStr = titleRemainStr + " > " + strRmn1.trim();
									}
								}else{
									String strRmn = title1RemainStrArr[i];
									String strReplace = strRmn.substring(strRmn.indexOf("."), strRmn.indexOf(":")); 
									strRmn = strRmn.replace(strReplace, "");
									titleRemainStr = titleRemainStr + " > " + strRmn.trim();
								}
							}else{
							 titleRemainStr = titleRemainStr + " > " + title1RemainStrArr[i].trim();
							}
						}else if(title2RemainStrArr[i].contains("(")){
							if(title1RemainStrArr[i].contains(".") && title2RemainStrArr[i].contains(".")){
								String strRmn1 = title1RemainStrArr[i];
								String strRmn2 =  title2RemainStrArr[i];
								String strClass1 = strRmn1.substring(strRmn1.indexOf("."), strRmn1.length());
								String strClass2 = strRmn2.substring(strRmn2.indexOf("."), strRmn2.indexOf(":"));
								if(strClass1.equals(strClass2)){
									titleRemainStr = titleRemainStr + " > " + title1RemainStrArr[i].trim();
								}else{
									strRmn2 = strRmn2.replace(strClass2, "");
									titleRemainStr = titleRemainStr + " > " + strRmn2.trim();
								}
							}else{
								String strRmn2 = title2RemainStrArr[i];
								if(title1RemainStrArr[i].contains(".")){
									 //strRmn1 =  strRmn1.substring(0, strRmn1.indexOf("."));
									 titleRemainStr = titleRemainStr + " > " + strRmn2.trim();//removing class and take index(same position have difference in css of two titles)
								}else{
									String strClass2 = strRmn2.substring(strRmn2.indexOf("."), strRmn2.indexOf(":"));
									strRmn2 = strRmn2.replace(strClass2, "");
									titleRemainStr = titleRemainStr + " > " + strRmn2.trim();//removing class and take index(same position have difference in css of two titles)
								}
							}
						}else if(title1RemainStrArr[i].contains(".")
								 || title2RemainStrArr[i].contains(".")){
							String title1substr = title1RemainStrArr[i].trim();
							String title2substr = title2RemainStrArr[i].trim();
							String str1 = "";
							String str2 = "";
							if(title1substr.contains(".")){
							str1 = title1substr.substring(0,title1substr.indexOf(".")).trim();//if classes are unequal in css of two titles..we omit class and only take tag
							}else{
								str1 = title1substr.trim();
							}
							if(title2substr.contains(".")){
							str2 = title2substr.substring(0,title2substr.indexOf(".")).trim();//if classes are unequal in css of two titles..we omit class and only take tag
							}else{
								str2 = title2substr.trim();
							}
							if(str1.equals(str2)){
								titleRemainStr = titleRemainStr + " > " + str1.trim();
							}else{
								if(str1.contains(".")){
									str1 = str1.substring(0, str1.indexOf("."));
								}
								titleRemainStr = titleRemainStr + " > " + str1.trim();
							}
						}
					}else{
						titleRemainStr = titleRemainStr + " > " + title1RemainStrArr[i].trim();
					}
				}*/
			if(title1RemainStr.contains(">") && title2RemainStr.contains(">")){
		    	String[] title1RemainStrArr = title1RemainStr.split(">");
				String[] title2RemainStrArr = title2RemainStr.split(">");
				Integer arraylimit1 = title1RemainStrArr.length;
				Integer arraylimit2 = title2RemainStrArr.length;
				Integer limitOfIteration = 0;
				if(arraylimit1 > arraylimit2){ 
					limitOfIteration = arraylimit2;
				}else{
					limitOfIteration = arraylimit1;
				}
//...........................>Title remain cases...	
				for(int i = 0; i < limitOfIteration; i++ ){
					if(title2RemainStrArr[i].isEmpty()){continue;}//may be titl1 and title2 css are different in length..
					String title1StrArrElmnt = title1RemainStrArr[i].trim();
					String title2StrArrElmnt = title2RemainStrArr[i].trim();
					if(!title1StrArrElmnt.equals(title2StrArrElmnt)){
						//#content > div.row.fixed > div.col-700.last > table.auto-style3 > tbody > tr:nth-child(2) > td.auto-style5:nth-child(2)
//edited												//#content > div.row.fixed > div.col-700.last > table.auto-style3 > tbody > tr:nth-child(3) > td.auto-style4:nth-child(2)
						//table.auto-style3
						if(title1RemainStrArr[i].contains("(")){
							if(title1RemainStrArr[i].contains(".")){
								if(title2RemainStrArr[i].contains(".")){
									String strRmn1 = title1RemainStrArr[i];
									String strRmn2 = title2RemainStrArr[i];
									String strClass1 = strRmn1.substring(strRmn1.indexOf("."), strRmn1.indexOf(":"));
									String strClass2 = "";
									if(title2RemainStrArr[i].contains("(")){
										strClass2 = strRmn2.substring(strRmn2.indexOf("."), strRmn2.indexOf(":"));
									}else{
										strClass2 = strRmn2.substring(strRmn2.indexOf("."), strRmn2.length());
									}
									if(strClass1.equals(strClass2)){
										 titleRemainStr = titleRemainStr + " > " + title1RemainStrArr[i].trim();
									}else{
										strRmn1 = strRmn1.replace(strClass1, "");
										titleRemainStr = titleRemainStr + " > " + strRmn1.trim();
									}
								}else{
									String strRmn = title1RemainStrArr[i];
									String strReplace = strRmn.substring(strRmn.indexOf("."), strRmn.indexOf(":")); 
									strRmn = strRmn.replace(strReplace, "");
									titleRemainStr = titleRemainStr + " > " + strRmn.trim();
								}
							}else{
							 titleRemainStr = titleRemainStr + " > " + title1RemainStrArr[i].trim();
							}
						}else if(title2RemainStrArr[i].contains("(")){
							if(title1RemainStrArr[i].contains(".") && title2RemainStrArr[i].contains(".")){
								String strRmn1 = title1RemainStrArr[i];
								String strRmn2 =  title2RemainStrArr[i];
								String strClass1 = strRmn1.substring(strRmn1.indexOf("."), strRmn1.length());
								String strClass2 = strRmn2.substring(strRmn2.indexOf("."), strRmn2.indexOf(":"));
								if(strClass1.equals(strClass2)){
									titleRemainStr = titleRemainStr + " > " + title1RemainStrArr[i].trim();
								}else{
									strRmn2 = strRmn2.replace(strClass2, "");
									titleRemainStr = titleRemainStr + " > " + strRmn2.trim();
								}
							}else{
								String strRmn2 = title2RemainStrArr[i];
								if(title1RemainStrArr[i].contains(".")){
									 //strRmn1 =  strRmn1.substring(0, strRmn1.indexOf("."));
									 titleRemainStr = titleRemainStr + " > " + strRmn2.trim();//removing class and take index(same position have difference in css of two titles)
								}else{
									String strClass2 = strRmn2.substring(strRmn2.indexOf("."), strRmn2.indexOf(":"));
									strRmn2 = strRmn2.replace(strClass2, "");
									titleRemainStr = titleRemainStr + " > " + strRmn2.trim();//removing class and take index(same position have difference in css of two titles)
								}
							}
						}else if(title1RemainStrArr[i].contains(".")
								 || title2RemainStrArr[i].contains(".")){
							String title1substr = title1RemainStrArr[i].trim();
							String title2substr = title2RemainStrArr[i].trim();
							String str1 = "";
							String str2 = "";
							//...................edited 10 mar...
							if(title1substr.contains(".") && title2substr.contains(".")){
									String classRmn = "";
									Boolean fst = true;
									String titrmnArr1[] = title1substr.split("\\.");
									String titrmnArr2[] = title2substr.split("\\.");
									Integer arr1Length = titrmnArr1.length;
									Integer arr2Length = titrmnArr2.length;
									if(arr1Length > arr2Length){
										
										for(Integer pIndex = 0; pIndex < arr2Length; pIndex++){
											for(Integer qIndex = 0; qIndex < arr1Length; qIndex++){
												if(titrmnArr2[pIndex].trim().equals(titrmnArr1[qIndex].trim())){
													if(fst){
													fst= false;
													classRmn = classRmn + titrmnArr2[pIndex];
													}else{
														classRmn = classRmn + "." + titrmnArr2[pIndex];
													}
												}
												
											}
											
										}
										
									}else{
										for(Integer pIndex = 0; pIndex < arr1Length; pIndex++){
											for(Integer qIndex = 0; qIndex < arr2Length; qIndex++){
												if(titrmnArr1[pIndex].trim().equals(titrmnArr1[qIndex].trim())){
													if(fst){
														fst= false;
														classRmn = classRmn + titrmnArr1[pIndex];
													}else{
														classRmn = classRmn + "."+ titrmnArr1[pIndex];
													}
												}
												
											}
											
										}
									}
									titleRemainStr = titleRemainStr + " > " + classRmn.trim();
							}else{
							if(title1substr.contains(".")){
							str1 = title1substr.substring(0,title1substr.indexOf(".")).trim();//if classes are unequal in css of two titles..we omit class and only take tag
							}else{
								str1 = title1substr.trim();
							}
							if(title2substr.contains(".")){
							str2 = title2substr.substring(0,title2substr.indexOf(".")).trim();//if classes are unequal in css of two titles..we omit class and only take tag
							}else{
								str2 = title2substr.trim();
							}
							if(str1.equals(str2)){
								titleRemainStr = titleRemainStr + " > " + str1.trim();
							}else{
								if(str1.contains(".")){
									str1 = str1.substring(0, str1.indexOf("."));
								}
								titleRemainStr = titleRemainStr + " > " + str1.trim();
							}
						}}
					}else{
						titleRemainStr = titleRemainStr + " > " + title1RemainStrArr[i].trim();
					}
				}
//...............................>
			}
		}else if(title2ElementStr.contains(title2IncStr)){
			System.out.println("check point 1.6..");
			titleRemainStr = title2ElementStr.substring(title2ElementStr.indexOf(title2IncStr) + title2IncStr.length(), title2ElementStr.length());
		}else{
			System.out.println("pls do some logic here 1.4..");
		}
		/**
		 * title incrimentig string setting here
		 */
		String titleIncStr = "";
		if(title1IncStr.isEmpty()){
			titleIncStr = title2IncStr.trim();
		}else{
			titleIncStr = title1IncStr.trim();
		}
		//if first title css have class and other one have no class but index on same position...
		Elements elements = null;
		if(titleIncStr.contains(".") && isRemovCSS){
			titleIncStrElemnts = jobElemntsParent +  " > " + titleSubStr + titleIncStr.substring(0,titleIncStr.indexOf("."));
			while(true){//if titleSubStr is not the correct immediate child of job parent element...do following
				elements = document.select(titleIncStrElemnts);
				if(elements.isEmpty()){
					titleSubStr = titleSubStr.substring(titleSubStr.indexOf(">")+ 1, titleSubStr.length());
					titleIncStrElemnts = jobElemntsParent +  " > " + titleSubStr + titleIncStr.substring(0,titleIncStr.indexOf("."));
					}
				else{break;}
			}
		}else{
			titleIncStrElemnts = jobElemntsParent + " > " +titleSubStr + titleIncStr.substring(0,titleIncStr.indexOf(":"));
			while(true){//if titleSubStr is not the correct immediate child of job parent element...do following
				elements = document.select(titleIncStrElemnts);
				if(elements.isEmpty()){ 
					titleSubStr = titleSubStr.substring(titleSubStr.indexOf(">")+ 1, titleSubStr.length());
					titleIncStrElemnts = jobElemntsParent + " > " +titleSubStr + titleIncStr.substring(0,titleIncStr.indexOf(":"));
				}
				else{break;}
			}
		}
		
		/**
		 * setting bean class with fields
		 */
		scrapFields.setElements(elements);
		scrapFields.setTitle1Index(title1Index);
		scrapFields.setTitle2Index(title2Index);
		scrapFields.setTitleIndexFlag(titleIndexFlag);	
		scrapFields.setTitleRemainStr(titleRemainStr);
		scrapFields.setTitleIncStrElemnts(titleIncStrElemnts);

		return scrapFields;
	}
	/**
	 * getting inrement elemnt for only class id tag in css cases..
	 * @param scrapFields
	 * @param document
	 * @param jobElemntsParent
	 * @param fisrtCSSSelecter1
	 * @param fisrtCSSSelecter2
	 * @return
	 */
	public ScrapFields getClassIDIncrElement(ScrapFields scrapFields, Document document, String jobElemntsParent, 
											String fisrtCSSSelecter1, String fisrtCSSSelecter2, String firstTitle, String secondTitle){
		Boolean incAvoidFlag = false;
		
		Integer titleIndexFlag = 0;
		Integer title1Index = 0;
		Integer title2Index = 0;
		String titleRemainStr = "";
		String titleIncStrElemnts = "";
		String title1IncStr = "";
		String title2IncStr = "";
		Boolean isRemovCSS = false;
		String titleSubStr = "";
		Boolean isParentFlag = false;
		String title1ElementStr = "";
		String title2ElementStr = "";
		/**
		 * if first CSS selector have id before parent..we need to travel to parent..
		 */
		while(true){
				Boolean equalityFlag = false;
				if(fisrtCSSSelecter1.contains(">")){
					String[] splitArr = fisrtCSSSelecter1.split(">");
					for(String splitStr: splitArr){
						splitStr = splitStr.trim();
						if(splitStr.equals(jobElemntsParent)){
							equalityFlag = true;
						}
					}
				}else{
					if(fisrtCSSSelecter1.trim().equals(jobElemntsParent)){
						equalityFlag = true;
					}
				}
				if(!equalityFlag){
					String tagId1 = document.select(fisrtCSSSelecter1.split(">")[0].trim()).first().parent().cssSelector();
					String tagId2 = "";
	//feb 20			
					if(!fisrtCSSSelecter2.contains(">")){
						if(!fisrtCSSSelecter2.trim().equals(jobElemntsParent.trim())){
							tagId2 = document.select(fisrtCSSSelecter2.split(">")[0].trim()).last().parent().cssSelector();
							isParentFlag = true;
						}else{
							tagId2 = fisrtCSSSelecter2;
						}
					}else{
						String[] cssArray = fisrtCSSSelecter2.split(">");
						Boolean equalCheckFlag = true;
						for(String cssStr: cssArray){
							cssStr = cssStr.trim();
							if(cssStr.equals(jobElemntsParent)){
								equalCheckFlag = false;
								break;
							}
						}
						if(equalCheckFlag){
							tagId2 = document.select(fisrtCSSSelecter2.split(">")[0].trim()).last().parent().cssSelector();
							isParentFlag = true;
						}else{
							tagId2 = fisrtCSSSelecter2;
						}
						
					}
					/**
					 * if parent id and direct child parent id are same..then we can easily find out titleIncrElement..
					 */
					if(tagId1.equals(jobElemntsParent) && tagId2.equals(jobElemntsParent)){
						System.out.println(".......Check area 0.1................");
					/**
					 * title increment element and title index flag are getting here..
					 */
						/*titleIncStrElemnts = jobElemntsParent + " > " + "*";
						titleRemainStr = "";
						titleIndexFlag = 1;*/
						String fisrtCSSSelecter11 = fisrtCSSSelecter1.substring(0, fisrtCSSSelecter1.indexOf(">")).trim();
						String fisrtCSSSelecter22 = fisrtCSSSelecter2.substring(0, fisrtCSSSelecter2.indexOf(">")).trim();
						fisrtCSSSelecter1 = fisrtCSSSelecter1.substring(fisrtCSSSelecter1.indexOf(">") + 1, fisrtCSSSelecter1.length()).trim();
						fisrtCSSSelecter2 = fisrtCSSSelecter2.substring(fisrtCSSSelecter2.indexOf(">") + 1, fisrtCSSSelecter2.length()).trim();
					
						Integer firstInd = 1;
						Integer secondInd = 1;
						if(fisrtCSSSelecter11.equals(fisrtCSSSelecter22)){
						 firstInd = document.select(fisrtCSSSelecter11).first().elementSiblingIndex() + 1;
						 secondInd = document.select(fisrtCSSSelecter22).first().elementSiblingIndex() + 2;
						}else{
							 firstInd = document.select(fisrtCSSSelecter11).first().elementSiblingIndex() + 1;
							 secondInd = document.select(fisrtCSSSelecter22).first().elementSiblingIndex() + 1;
						}
	//feb 20				
						String className = "";
						if(document.select(fisrtCSSSelecter11).first().hasAttr("class")
								&& document.select(fisrtCSSSelecter22).first().hasAttr("class")){
							String class1 = document.select(fisrtCSSSelecter11).first().className();
							String class2 = document.select(fisrtCSSSelecter22).first().className();
							if(class1.equals(class2) && !class1.isEmpty()){
								className = "." + class1;
							}
						}
						fisrtCSSSelecter1 = tagId1 + " > " + document.select(fisrtCSSSelecter11).first().tagName().trim() + className + ":nth-child("+firstInd+") > " +  fisrtCSSSelecter1.trim();
						fisrtCSSSelecter2 = tagId2 + " > " + document.select(fisrtCSSSelecter22).first().tagName().trim() + className + ":nth-child("+secondInd+") > " + fisrtCSSSelecter2.trim();
					
						incAvoidFlag = false;
						break;
					/**
//edited						 * if title element with id and it needs to travel more than one node to its parent..
					 */
					}else if(tagId1.contains(jobElemntsParent) && tagId2.contains(jobElemntsParent)){
						System.out.println(".......Check area 0.2....."); //eg : tagId1 = #accordion > div.panel.panel-default:nth-child(1) > div.panel-heading > h4.panel-title
																		  //eg : jobElemntsParent =  #accordion
																		  //eg : fisrtCSSSelecter1 =  #myjobpost1
						//css of its parent and travel until css first elemnt equals to the parent element..
						
						if(fisrtCSSSelecter1.contains(">")){
							String fisrtCSSSelecter11 = fisrtCSSSelecter1.substring(0, fisrtCSSSelecter1.indexOf(">")).trim();
							fisrtCSSSelecter1 = fisrtCSSSelecter1.substring(fisrtCSSSelecter1.indexOf(">") + 1, fisrtCSSSelecter1.length()).trim();
							Integer firstInd = document.select(fisrtCSSSelecter11).first().elementSiblingIndex() + 1;
							fisrtCSSSelecter1 = tagId1 + " > " + document.select(fisrtCSSSelecter11).first().tagName().trim() + ":nth-child(" + firstInd +") > " +  fisrtCSSSelecter1.trim();
							
							if(isParentFlag){
							Integer secondInd = 1;
							String fisrtCSSSelecter22 = fisrtCSSSelecter2.substring(0, fisrtCSSSelecter2.indexOf(">")).trim();
							fisrtCSSSelecter2 = fisrtCSSSelecter2.substring(fisrtCSSSelecter2.indexOf(">") + 1, fisrtCSSSelecter2.length()).trim();
							if(fisrtCSSSelecter11.equals(fisrtCSSSelecter22)){
							secondInd = document.select(fisrtCSSSelecter22).last().elementSiblingIndex() + 1;
							}else{
							secondInd = document.select(fisrtCSSSelecter22).first().elementSiblingIndex() + 1;
							}
							fisrtCSSSelecter2 = tagId2 + " > " + document.select(fisrtCSSSelecter22).first().tagName().trim() + ":nth-child(" + secondInd +") > " + fisrtCSSSelecter2.trim();
							}
						}else{
							String fisrtCSSSelecter11 = fisrtCSSSelecter1.trim();
							Integer firstInd = document.select(fisrtCSSSelecter11).first().elementSiblingIndex() + 1;
							fisrtCSSSelecter1 = tagId1 + " > " + document.select(fisrtCSSSelecter11).first().tagName().trim() + ":nth-child(" + firstInd + ")" ;
							if(isParentFlag){
								String fisrtCSSSelecter22 = fisrtCSSSelecter2.trim();
								Integer secondInd = document.select(fisrtCSSSelecter22).first().elementSiblingIndex() + 1;
								fisrtCSSSelecter2 = tagId2 + " > " + document.select(fisrtCSSSelecter22).first().tagName().trim() + ":nth-child(" + secondInd + ")" ;
							}
						}
						incAvoidFlag = false;
						break;
					}else{
						System.out.println("Add some logic here 1.3...");
						String parentCSS1 = "";
						String parentCSS2 = "";
						Boolean loopFlag1 = true;
						Boolean loopFlag2 = true;
						do{
							String cssElement1 = fisrtCSSSelecter1.split(">")[0].trim();
							parentCSS1 = document.select(cssElement1).first().parent().cssSelector();
							if(parentCSS1.contains(">")){
								String[] cssCollection1 = parentCSS1.split(">");
								for(String css1 : cssCollection1){
									if(css1.trim().equals(jobElemntsParent)){loopFlag1 = false;}
								}
								if(!loopFlag1){
									String endCSS = parentCSS1.substring(parentCSS1.indexOf(jobElemntsParent), parentCSS1.length());
									fisrtCSSSelecter1 = endCSS + " > " + fisrtCSSSelecter1;
								}else{
									fisrtCSSSelecter1 = parentCSS1 + " > " + fisrtCSSSelecter1;
								}
							}else{
								if(parentCSS1.trim().equals(jobElemntsParent)){loopFlag1 = false;}
								if(!loopFlag1){
									String endCSS = parentCSS1.substring(parentCSS1.indexOf(jobElemntsParent), parentCSS1.length());
									fisrtCSSSelecter1 = endCSS + " > " + fisrtCSSSelecter1;
								}else{
									fisrtCSSSelecter1 = parentCSS1 + " > " + fisrtCSSSelecter1;
								}
							}
							
							
						}while(loopFlag1);
						
						do{
							String cssElement2= fisrtCSSSelecter2.split(">")[0].trim();
							parentCSS2 = document.select(cssElement2).first().parent().cssSelector();
							if(parentCSS2.contains(">")){
								String[] cssCollection2 = parentCSS2.split(">");
								for(String css2 : cssCollection2){
									if(css2.trim().equals(jobElemntsParent)){loopFlag2 = false;}
								}
								if(!loopFlag2){
									String endCSS = parentCSS2.substring(parentCSS2.indexOf(jobElemntsParent), parentCSS2.length());
									fisrtCSSSelecter2 = endCSS + " > " + fisrtCSSSelecter2;
								}else{
									fisrtCSSSelecter2 = parentCSS2 + " > " + fisrtCSSSelecter2;
								}
							}else{
								if(parentCSS2.trim().equals(jobElemntsParent)){loopFlag2 = false;}
								if(!loopFlag2){
									String endCSS = parentCSS2.substring(parentCSS2.indexOf(jobElemntsParent), parentCSS2.length());
									fisrtCSSSelecter2 = endCSS + " > " + fisrtCSSSelecter2;
								}else{
									fisrtCSSSelecter2 = parentCSS2 + " > " + fisrtCSSSelecter2;
								}
							}
							
							
						}while(loopFlag2);
						
						if(fisrtCSSSelecter1.equals(fisrtCSSSelecter2)){
							Boolean first = true;
							for(int j = 1; j < fisrtCSSSelecter1.split(">").length ; j++){
								String csselmntstr = fisrtCSSSelecter1.split(">")[j];
								if(document.select(csselmntstr).size() > 1 && first){
									fisrtCSSSelecter1 = fisrtCSSSelecter1.replace(csselmntstr, csselmntstr.trim() + ":nth-child(1)");
									fisrtCSSSelecter2 = fisrtCSSSelecter2.replace(csselmntstr, csselmntstr.trim() + ":nth-child(2)");
									first = false;
								}else{
									if(csselmntstr.contains("#")){ 
										Boolean flag = true;
										int i = 0;
										do{
										if(document.select(csselmntstr).get(i).text().contains(firstTitle)){
											fisrtCSSSelecter1 = fisrtCSSSelecter1.replace(csselmntstr, csselmntstr.trim() + ":nth-child("+(i + 1)+")");
											fisrtCSSSelecter2 = fisrtCSSSelecter2.replace(csselmntstr, csselmntstr.trim() + ":nth-child("+(i + 1)+")");
											flag = false;
										}
										 ++i;
										}while(flag);
									}
								}
							}
						}

						incAvoidFlag = false;
						break;
					}
				}else{
					//if css selecter contains job parent element exit the loop..
					break;
				}
			}
		if(!incAvoidFlag){
		/**
		 *  finding job increment element from parent..
		 */
			title2ElementStr = fisrtCSSSelecter2.substring(fisrtCSSSelecter2.indexOf(jobElemntsParent), fisrtCSSSelecter2.length() );
			if(title2ElementStr.isEmpty()){
			title2ElementStr = fisrtCSSSelecter2;
			}
			title1ElementStr = fisrtCSSSelecter1.substring(fisrtCSSSelecter1.indexOf(jobElemntsParent), fisrtCSSSelecter1.length() );
			if(title1ElementStr.isEmpty()){
			title1ElementStr = fisrtCSSSelecter1;
			}
								System.out.println(title1ElementStr);
								System.out.println(title2ElementStr);
								//String titleIncStr = "";
								//identifying iterating element...and its starting index...
								if(title1ElementStr.contains(">") && title2ElementStr.contains(">"))
								{
									String[] title1ElementArr = title1ElementStr.split(">");
									String[] title2ElementArr = title2ElementStr.split(">");
									for(int i = 0; i < title1ElementArr.length; i++){
											if(!title1ElementArr[i].equals(title2ElementArr[i])){
												if(title1ElementArr[i].contains("(")){
													title1IncStr = title1ElementArr[i];
													title2IncStr = title2ElementArr[i];
													try{
													String titleInc1 = title1ElementArr[i].substring(0, title1ElementArr[i].indexOf("("));
													String titleInc2 = "";
													if(title2IncStr.contains("(")){
													titleInc2 = title2ElementArr[i].substring(0, title2ElementArr[i].indexOf("("));
													}else{
														titleInc2 = title2ElementArr[i];
													}
													if(!titleInc1.equals(titleInc2)){
													isRemovCSS = true;
													}
													}catch(Exception e){
													e.printStackTrace();
													}
													String str1 = title1ElementArr[i];
													String str2 = title2ElementArr[i];
													try{
														title1Index = Integer.valueOf(str1.substring(str1.indexOf("(") + 1, str1.indexOf(")")));
														}catch(Exception e){
															title1Index = 0;
														}
														try{
														title2Index = Integer.valueOf(str2.substring(str2.indexOf("(") + 1, str2.indexOf(")")));
														}catch(Exception e){
															title2Index = 0;
														}
													break;
												}else if(title2ElementArr[i].contains("(")){
													title2IncStr = title2ElementArr[i];
													title1IncStr = title1ElementArr[i];
													isRemovCSS = true;
													titleIndexFlag = 1;
													Integer lengthOf1Array = title1ElementArr.length;
													Integer lengthOf2Array = title2ElementArr.length;
													String lastElement1 = title1ElementArr[lengthOf1Array-1];
													String lastElement2 = title2ElementArr[lengthOf2Array-1];
													if(title1ElementArr[i].equals(lastElement1)){
														if(lastElement1.equals(lastElement2) && lastElement1.contains(".") &&
																								lastElement2.contains(".")){
															//#aboutus-main > div.bg-about > div.career-text > span.style2
															//#aboutus-main > div.bg-about > div.career-text > p:nth-child(7) > span.style2
															if(titleSubStr.contains(">")){
																titleSubStr = titleSubStr.substring(0, titleSubStr.lastIndexOf(">")).trim() + " ";
															}
														}
													}
													break;
												}else{
	//editing feb 09									
													title1IncStr = title1ElementArr[i];
													title2IncStr = title2ElementArr[i];
													isRemovCSS = true;
													title1Index = 1;
													break;
												}
											}
											titleSubStr = titleSubStr + title1ElementArr[i] + ">" ;
									}
								}
								
								String title1RemainStr = "";
								String title2RemainStr = "";
								/**
								 * identifying title RemainStr and setting it..
								 */
								if(title1ElementStr.contains(title1IncStr) && title2ElementStr.contains(title2IncStr)
										&& !title1IncStr.isEmpty()){
									int j = title1ElementStr.indexOf(title1IncStr);
									int k =  title2ElementStr.indexOf(title2IncStr);
									if(j == k){
									title1RemainStr = title1ElementStr.substring(title1ElementStr.indexOf(title1IncStr) + title1IncStr.length(), title1ElementStr.length());
									title2RemainStr =  title2ElementStr.substring(title2ElementStr.indexOf(title2IncStr) + title2IncStr.length(), title2ElementStr.length());
									}else if(j > k){
										title1RemainStr = title1ElementStr.substring( j + title1IncStr.length(), title1ElementStr.length());
										title2RemainStr =  title2ElementStr.substring(j + title2IncStr.length(), title2ElementStr.length());
											
									}else {
										title1RemainStr = title1ElementStr.substring( k + title1IncStr.length(), title1ElementStr.length());
										title2RemainStr =  title2ElementStr.substring(k + title2IncStr.length(), title2ElementStr.length());
									}
									if(title1RemainStr.contains(">") && title2RemainStr.contains(">")){
							    	String[] title1RemainStrArr = title1RemainStr.split(">");
									String[] title2RemainStrArr = title2RemainStr.split(">");
									Integer arraylimit1 = title1RemainStrArr.length;
									Integer arraylimit2 = title2RemainStrArr.length;
									Integer limitOfIteration = 0;
									if(arraylimit1 > arraylimit2){ 
										limitOfIteration = arraylimit2;
									}else{
										limitOfIteration = arraylimit1;
									}
	//...........................>Title remain cases...	
									for(int i = 0; i < limitOfIteration; i++ ){
										if(title2RemainStrArr[i].isEmpty()){continue;}//may be titl1 and title2 css are different in length..
										String title1StrArrElmnt = title1RemainStrArr[i].trim();
										String title2StrArrElmnt = title2RemainStrArr[i].trim();
										if(!title1StrArrElmnt.equals(title2StrArrElmnt)){
											//#content > div.row.fixed > div.col-700.last > table.auto-style3 > tbody > tr:nth-child(2) > td.auto-style5:nth-child(2)
//edited												//#content > div.row.fixed > div.col-700.last > table.auto-style3 > tbody > tr:nth-child(3) > td.auto-style4:nth-child(2)
											//table.auto-style3
											if(title1RemainStrArr[i].contains("(")){
												if(title1RemainStrArr[i].contains(".")){
													if(title2RemainStrArr[i].contains(".")){
														String strRmn1 = title1RemainStrArr[i];
														String strRmn2 = title2RemainStrArr[i];
														String strClass1 = strRmn1.substring(strRmn1.indexOf("."), strRmn1.indexOf(":"));
														String strClass2 = "";
														if(title2RemainStrArr[i].contains("(")){
															strClass2 = strRmn2.substring(strRmn2.indexOf("."), strRmn2.indexOf(":"));
														}else{
															strClass2 = strRmn2.substring(strRmn2.indexOf("."), strRmn2.length());
														}
														if(strClass1.equals(strClass2)){
															 titleRemainStr = titleRemainStr + " > " + title1RemainStrArr[i].trim();
														}else{
															strRmn1 = strRmn1.replace(strClass1, "");
															titleRemainStr = titleRemainStr + " > " + strRmn1.trim();
														}
													}else{
														String strRmn = title1RemainStrArr[i];
														String strReplace = strRmn.substring(strRmn.indexOf("."), strRmn.indexOf(":")); 
														strRmn = strRmn.replace(strReplace, "");
														titleRemainStr = titleRemainStr + " > " + strRmn.trim();
													}
												}else{
												 titleRemainStr = titleRemainStr + " > " + title1RemainStrArr[i].trim();
												}
											}else if(title2RemainStrArr[i].contains("(")){
												if(title1RemainStrArr[i].contains(".") && title2RemainStrArr[i].contains(".")){
													String strRmn1 = title1RemainStrArr[i];
													String strRmn2 =  title2RemainStrArr[i];
													String strClass1 = strRmn1.substring(strRmn1.indexOf("."), strRmn1.length());
													String strClass2 = strRmn2.substring(strRmn2.indexOf("."), strRmn2.indexOf(":"));
													if(strClass1.equals(strClass2)){
														titleRemainStr = titleRemainStr + " > " + title1RemainStrArr[i].trim();
													}else{
														strRmn2 = strRmn2.replace(strClass2, "");
														titleRemainStr = titleRemainStr + " > " + strRmn2.trim();
													}
												}else{
													String strRmn2 = title2RemainStrArr[i];
													if(title1RemainStrArr[i].contains(".")){
														 //strRmn1 =  strRmn1.substring(0, strRmn1.indexOf("."));
														 titleRemainStr = titleRemainStr + " > " + strRmn2.trim();//removing class and take index(same position have difference in css of two titles)
													}else{
														String strClass2 = strRmn2.substring(strRmn2.indexOf("."), strRmn2.indexOf(":"));
														strRmn2 = strRmn2.replace(strClass2, "");
														titleRemainStr = titleRemainStr + " > " + strRmn2.trim();//removing class and take index(same position have difference in css of two titles)
													}
												}
											}else if(title1RemainStrArr[i].contains(".")
													 || title2RemainStrArr[i].contains(".")){
												String title1substr = title1RemainStrArr[i].trim();
												String title2substr = title2RemainStrArr[i].trim();
												String str1 = "";
												String str2 = "";
									//...................edited 10 mar...
											if(title1substr.contains(".") && title2substr.contains(".")){
													String classRmn = "";
													Boolean fst = true;
													String titrmnArr1[] = title1substr.split("\\.");
													String titrmnArr2[] = title2substr.split("\\.");
													Integer arr1Length = titrmnArr1.length;
													Integer arr2Length = titrmnArr2.length;
													if(arr1Length > arr2Length){
														
														for(Integer pIndex = 0; pIndex < arr2Length; pIndex++){
															for(Integer qIndex = 0; qIndex < arr1Length; qIndex++){
																if(titrmnArr2[pIndex].trim().equals(titrmnArr1[qIndex].trim())){
																	if(fst){
																	fst= false;
																	classRmn = classRmn + titrmnArr2[pIndex];
																	}else{
																		classRmn = classRmn + "." + titrmnArr2[pIndex];
																	}
																}
																
															}
															
														}
														
													}else{
														for(Integer pIndex = 0; pIndex < arr1Length; pIndex++){
															for(Integer qIndex = 0; qIndex < arr2Length; qIndex++){
																if(titrmnArr1[pIndex].trim().equals(titrmnArr1[qIndex].trim())){
																	if(fst){
																		fst= false;
																		classRmn = classRmn + titrmnArr1[pIndex];
																	}else{
																		classRmn = classRmn + "."+ titrmnArr1[pIndex];
																	}
																}
																
															}
															
														}
													}
													titleRemainStr = titleRemainStr + " > " + classRmn.trim();
													
											}else{
												
												if(title1substr.contains(".")){
												str1 = title1substr.substring(0,title1substr.indexOf(".")).trim();//if classes are unequal in css of two titles..we omit class and only take tag
												}else{
													str1 = title1substr.trim();
												}
												if(title2substr.contains(".")){
												str2 = title2substr.substring(0,title2substr.indexOf(".")).trim();//if classes are unequal in css of two titles..we omit class and only take tag
												}else{
													str2 = title2substr.trim();
												}
												if(str1.equals(str2)){
													titleRemainStr = titleRemainStr + " > " + str1.trim();
												}else{
													if(str1.contains(".")){
														str1 = str1.substring(0, str1.indexOf("."));
													}
													titleRemainStr = titleRemainStr + " > " + str1.trim();
												}
												}
											}
										}else{
											titleRemainStr = titleRemainStr + " > " + title1RemainStrArr[i].trim();
										}
									}
		//....................................>
									}else{
										System.out.println("do some logic here.....");
									}
		//feb 20					
									}else if(title2ElementStr.contains(title2IncStr) && !title2IncStr.isEmpty()){
											titleRemainStr = title2ElementStr.substring(title2ElementStr.indexOf(title2IncStr) + title2IncStr.length(), title2ElementStr.length());
									}else{
											System.out.println("pls do some logic here 1.4..");
									}
								/**
								 * title incrimentig string setting here
								 */
								String titleIncStr = "";
								if(title1IncStr.isEmpty()){
									titleIncStr = title2IncStr.trim();
								}else{
									titleIncStr = title1IncStr.trim();
								}
								if(titleIncStr.contains(".") && isRemovCSS){
		//edited feb 22				
									if(!(title1IncStr.isEmpty() && title2IncStr.isEmpty())){
										if(title1IncStr.contains(".") && title2IncStr.contains(".")){
											String[] title1IncStrArr = title1IncStr.split("\\.");
											String[] title2IncStrArr = title2IncStr.split("\\.");
											Integer lowlimit = 0;
											Integer highlimIt1 = 0;
											if(title1IncStrArr.length > title2IncStrArr.length){
												lowlimit = title2IncStrArr.length;
												highlimIt1 = title1IncStrArr.length;
											}else{
												lowlimit = title1IncStrArr.length;
												highlimIt1 = title2IncStrArr.length;
											}
											StringBuffer stringBuffer = new StringBuffer();
											Boolean first = true;
											for(Integer i = 0; i < lowlimit; i++){
												for(Integer j = 0; j < highlimIt1; j++){
													
													String str1 = "";
													if(title1IncStrArr[i].contains(":")){
														str1 = title1IncStrArr[i].substring(0,title1IncStrArr[i].indexOf(":")).trim();
													}else{
														str1 = title1IncStrArr[i].trim();
													}	
													String str2 = "";
													if(title2IncStrArr[j].contains(":")){
														str2 = title2IncStrArr[j].substring(0,title2IncStrArr[j].indexOf(":")).trim();
													}else{
														str2 = title2IncStrArr[j].trim();
													}	
												if(str1.equals(str2)){
													if(first){
													stringBuffer.append(str1);
													first = false;
													}else{
														stringBuffer.append("." + str1);
													}
													break;
												}
												}
											}
											
											titleIncStrElemnts = titleSubStr + " " + stringBuffer.toString();
										}else{
											titleIncStrElemnts = titleSubStr + " " + titleIncStr.substring(0,titleIncStr.indexOf("."));
										}
									}else{
									   titleIncStrElemnts = titleSubStr + " " + titleIncStr.substring(0,titleIncStr.indexOf("."));
									}
								}else{
			// edited feb 20		
									if(titleIncStr.contains(":")){
									titleIncStrElemnts = titleSubStr + titleIncStr.substring(0,titleIncStr.indexOf(":"));
									}else{
										if(titleIncStr.isEmpty()){
											titleSubStr = titleSubStr.trim();
											if(("" + titleSubStr.charAt(titleSubStr.length()-1)).equals(">")){
												titleSubStr = titleSubStr.substring(0, titleSubStr.lastIndexOf(">"));
											}
												titleIncStrElemnts = titleSubStr;
										}else{
											titleIncStrElemnts = titleSubStr + titleIncStr;
										}
									}
								}
		}
		Elements elements = document.select(titleIncStrElemnts);
		/**
		 * title remain str 
		 */
		if(!titleRemainStr.isEmpty()){
			titleRemainStr = titleRemainStr.trim();
			String charStr = titleRemainStr.charAt(0) + "";
			if(charStr.equals(">")){
				titleRemainStr = titleRemainStr.substring(titleRemainStr.indexOf(">") + 1, titleRemainStr.length());
			}
		}
		/**
		 * setting bean class with fields
		 */
		scrapFields.setElements(elements);
		scrapFields.setTitle1Index(title1Index);
		scrapFields.setTitle2Index(title2Index);
		scrapFields.setTitleIndexFlag(titleIndexFlag);	
		scrapFields.setTitleRemainStr(titleRemainStr);
		scrapFields.setTitleIncStrElemnts(titleIncStrElemnts);
		System.out.println("tite Increment : " + titleIncStrElemnts);
		System.out.println("title Remain : " + titleRemainStr);
		return scrapFields;
	
	}
	/**
	 * Finding increment elements for specific field in single URL case 
	 * @param parentElements
	 * @param jobElemntsParent
	 * @param firstfield
	 * @param limit
	 * @param getIncrementElement
	 * @return
	 */
	public String getFieldIncrementValues(Elements parentElements, String jobElemntsParent, String firstfield, Integer limit, String getIncrementElement){
		String fieldIncStrElemnts1 = "";
		String fieldIncStrElemnts2 = "";
		String fieldRemainStr1 = "";
		String fieldRemainStr2 = "";
		String fieldIncStrElemnts = "";
		String firstfield1 = "";
		String firstfield2 = "";
		String fieldRemainStr = "";
		Element elementFirstfield  = null;
		Element elementLastfield = null;
		String fieldCSSSelecter1 = "";
		String fieldCSSSelecter2 = "";
		try{
			/**
			 * Checking value have '|'
			 */
		if(firstfield.contains("|")){
		 firstfield1 = firstfield.split("\\|")[0];
		 firstfield2 = firstfield.split("\\|")[1];
			elementFirstfield = parentElements.select("*:containsOwn("+ firstfield1 +")").first();	
			elementLastfield = parentElements.select("*:containsOwn("+ firstfield2 +")").last();	
			fieldCSSSelecter1 = elementFirstfield.cssSelector();
			fieldCSSSelecter2 = elementLastfield.cssSelector();
		}else{
			try{
			elementFirstfield = parentElements.select("*:containsOwn("+ firstfield +")").first();
			String elmntTxt = elementFirstfield.text().trim();
			if(!elmntTxt.equals(firstfield.trim())){
			 elementFirstfield = parentElements.select("*:containsOwn("+ firstfield +")").last();
			 elmntTxt = elementFirstfield.text().trim();
			 if(!elmntTxt.equals(firstfield.trim())){
				 elementFirstfield = parentElements.select("*:containsOwn("+ firstfield +")").first();
			 }
			}
			}catch(Exception e){}
			fieldCSSSelecter1 = elementFirstfield.cssSelector();
		}
		//#htmlbody > center > table:nth-child(2) > tbody > tr:nth-child(2) > td:nth-child(2) > ul:nth-child(21) > li:nth-child(4)
		/**
		 *  if posted element is in specific id defined element
		 */
		String fieldcss1 = fieldCSSSelecter1.split(">")[0].trim();
		if(!fieldcss1.equals(jobElemntsParent)){
			//equality check
			Boolean equalityflag = false;
			if(fieldCSSSelecter1.contains(">")){
			String strArr[] = fieldCSSSelecter1.split(">");
			for(String strVal : strArr){
				strVal = strVal.trim();
				if(strVal.equals(jobElemntsParent)){
					equalityflag = true;
					break;
				}
			}
			}else{
				if(fieldCSSSelecter1.trim().equals(jobElemntsParent)){
					equalityflag = true;
				}
			}
			
			if(fieldCSSSelecter1.split(">")[0].contains("#") && !equalityflag){
				String subElement = "";
				String cssToParent = parentElements.select(fieldCSSSelecter1.split(">")[0].trim()).first().parent().cssSelector();
				cssToParent = cssToParent.substring(cssToParent.indexOf(jobElemntsParent), cssToParent.length());
				String subId = fieldCSSSelecter1.split(">")[0].trim();
				String remnElements = fieldCSSSelecter1.substring(fieldCSSSelecter1.indexOf(">") + 1).trim();
				if(parentElements.select(subId).hasAttr("class")){
					subElement = parentElements.select(subId).attr("class").trim();
					if(subElement.contains(" ")){
						String classWithdot = "";
						String classArr[] = subElement.split(" ");
						for(String classStr : classArr){
							if(classStr.isEmpty()){continue;}
							classWithdot = classWithdot + "." + classStr;
						}
						subElement = classWithdot;
					}else{
						subElement = "." + subElement;
					}
					if(parentElements.select(cssToParent.trim() + " > " + subElement.trim()).size() < limit){
						subElement = parentElements.select(subId).first().tagName();
					}
				}else{
					subElement = parentElements.select(subId).first().tagName();
				}
				
				cssToParent = cssToParent + " > " + subElement + " > " + remnElements;
				
				String[] fieldCSSArr = cssToParent.split(">");
				for(int i = 0; i < fieldCSSArr.length ; i++){
					String str  = fieldCSSArr[i].trim();
					if(str.contains(":")){
					str = str.substring(0, str.indexOf(":"));
					}
					Integer count = parentElements.select(jobElemntsParent).select(str).size();
					if(count >= limit){
						fieldIncStrElemnts1 = cssToParent.substring(0, cssToParent.indexOf(str) + str.length());
						fieldRemainStr1 = cssToParent.substring(cssToParent.indexOf(fieldIncStrElemnts1) + fieldIncStrElemnts1.length());
						if((fieldRemainStr1.trim().charAt(0) + "").equals(">") || (fieldRemainStr1.trim().charAt(0) + "").equals(":")){
							fieldRemainStr1 = fieldRemainStr1.substring(fieldRemainStr1.indexOf(">") + 1);
						}
						break;
					}
				}
				/**
				 * if child css  have items before parent element.. 
				 */
			}else{
				Integer incrementerFinder = getIncrementElement.split(">").length;
				//To get CSS string from parent to respective element (eg: #abc > dfg > .jkl > ll) if parent is (.jkl)
				String str = fieldCSSSelecter1.substring(fieldCSSSelecter1.indexOf(jobElemntsParent), fieldCSSSelecter1.length());
				String[] incrStrArr = str.split(">");
				String fieldIncElemnt = "";
				for(int idx = 0 ; idx < incrementerFinder ; idx++){
					String item = incrStrArr[idx];
					if(idx == (incrementerFinder-1)){
						if(item.contains(":")){
						item = item.substring(0, item.indexOf(":"));
						}
						if(item.contains(".")){
							item = item.substring(0, item.indexOf("."));
						}
						fieldIncStrElemnts1 =  fieldIncStrElemnts1 + item;
					}else{
					fieldIncStrElemnts1 =  fieldIncStrElemnts1 + item  + ">";
					}
					fieldIncElemnt = incrStrArr[idx];
				}
				fieldRemainStr1 = str.substring(str.indexOf(fieldIncElemnt) + fieldIncElemnt.length(), str.length());
				//if last level have more than one values
				
				String[] fieldRemainStrArr = fieldRemainStr1.split(">");
				if(fieldRemainStrArr.length > 2 ){
				String rmnStr  = fieldRemainStrArr[fieldRemainStr1.split(">").length-1];
				if(rmnStr.contains(":")){
				fieldRemainStr1 = fieldRemainStr1.substring(0, fieldRemainStr1.lastIndexOf(":"));
				}
				}
			}
		}else{
			System.out.println("Check this case more..");
			// if posted element not in specific id
			String[] pDateCSSArr = fieldCSSSelecter1.split(">");
			for(int i = 0; i < pDateCSSArr.length ; i ++){
				String str  = pDateCSSArr[i].trim();
				if(str.contains(":")){
				str = str.substring(0, str.indexOf(":"));
				}
				if(str.contains(".")){
					str = str.substring(0, str.indexOf("."));
					}
				Integer jobSize = parentElements.select(str).size();
				//eg : http://www.primaccess.com/current-openings.html
				if(jobSize >= limit){

					fieldIncStrElemnts1 = fieldCSSSelecter1.substring(0, fieldCSSSelecter1.indexOf("> " + str) + ("> " + str).length());
					fieldRemainStr1 = fieldCSSSelecter1.substring(fieldCSSSelecter1.indexOf(fieldIncStrElemnts1) + fieldIncStrElemnts1.length(), fieldCSSSelecter1.length());
					fieldRemainStr1 = fieldRemainStr1.substring(fieldRemainStr1.indexOf(">") + 1, fieldRemainStr1.length());
					break;
				}
			}
		}
		/**
		 * Second Field
		 */
		if(!firstfield2.isEmpty()){
		if(!fieldCSSSelecter2.split(">")[0].trim().equals(jobElemntsParent)){
			if(fieldCSSSelecter2.split(">")[0].contains("#") && !(fieldCSSSelecter2.contains(jobElemntsParent))){
				String subElement = "";
				String cssToParent = parentElements.select(fieldCSSSelecter2.split(">")[0].trim()).first().parent().cssSelector();
				cssToParent = cssToParent.substring(cssToParent.indexOf(jobElemntsParent), cssToParent.length());
				String subId = fieldCSSSelecter2.split(">")[0].trim();
				String remnElements = fieldCSSSelecter2.substring(fieldCSSSelecter2.indexOf(">") + 1).trim();
				if(parentElements.select(subId).hasAttr("class")){
					subElement = parentElements.select(subId).attr("class").trim();
					if(subElement.contains(" ")){
						String classWithdot = "";
						String classArr[] = subElement.split(" ");
						for(String classStr : classArr){
							if(classStr.isEmpty()){continue;}
							classWithdot = classWithdot + "." + classStr;
						}
						subElement = classWithdot;
					}else{
						subElement = "." + subElement;
					}
					if(parentElements.select(cssToParent.trim() + " > " + subElement.trim()).size() < limit){
						subElement = parentElements.select(subId).first().tagName();
					}
				}else{
					subElement = parentElements.select(subId).first().tagName();
				}
				
				cssToParent = cssToParent + " > " + subElement + " > " + remnElements;
				
				String[] fieldCSSArr = cssToParent.split(">");
				for(int i = 0; i < fieldCSSArr.length ; i++){
					String str  = fieldCSSArr[i].trim();
					if(str.contains(":")){
					str = str.substring(0, str.indexOf(":"));
					}
					Integer count = parentElements.select(jobElemntsParent).select(str).size();
					if(count >= limit){
						fieldIncStrElemnts2 = cssToParent.substring(0, cssToParent.indexOf(str) + str.length());
						fieldRemainStr2 = cssToParent.substring(cssToParent.indexOf(fieldIncStrElemnts2) + fieldIncStrElemnts2.length());
						if((fieldRemainStr2.trim().charAt(0) + "").equals(">")){
							fieldRemainStr2 = fieldRemainStr2.substring(fieldRemainStr2.indexOf(">") + 1);
						}
						break;
					}
				}
				//if child css  have items before parent element.. 
			}else{
				Integer incrementerFinder = getIncrementElement.split(">").length;
				//To get CSS string from parent to respective element (eg: #abc > dfg > .jkl > ll) if parent is (.jkl)
				String str = fieldCSSSelecter2.substring(fieldCSSSelecter2.indexOf(jobElemntsParent), fieldCSSSelecter2.length());
				String[] incrStrArr = str.split(">");
				String fieldIncElemnt = "";
				for(int idx = 0 ; idx < incrementerFinder ; idx++){
					String item = incrStrArr[idx];
					if(idx == (incrementerFinder-1)){
						if(item.contains(":")){
							item = item.substring(0, item.indexOf(":"));
						}
						if(item.contains(".")){
							item = item.substring(0, item.indexOf("."));
						}
						fieldIncStrElemnts2 =  fieldIncStrElemnts2 + item;
					}else{
					fieldIncStrElemnts2 =  fieldIncStrElemnts2 + item  + ">";
					}
					fieldIncElemnt = incrStrArr[idx];
				}
				fieldRemainStr2 = str.substring(str.indexOf(fieldIncElemnt) + fieldIncElemnt.length(), str.length());
				//if last level have more than one values
				
				String[] fieldRemainStrArr = fieldRemainStr2.split(">");
				if(fieldRemainStrArr.length > 2 ){
				String rmnStr  = fieldRemainStrArr[fieldRemainStr2.split(">").length-1];
				if(rmnStr.contains(":")){
				fieldRemainStr2 = fieldRemainStr2.substring(0, fieldRemainStr2.lastIndexOf(":"));
				}
				}
			}
		}else{
			// if posted element not in specific id
			String[] pDateCSSArr = fieldCSSSelecter2.split(">");
			for(int i = 0; i < pDateCSSArr.length ; i ++){
				String str  = pDateCSSArr[i].trim();
				if(str.contains(":")){
				str = str.substring(0, str.indexOf(":"));
				}
				Integer jobSize =parentElements.select(str).size();
				if(jobSize >= limit){
					fieldIncStrElemnts2 = fieldCSSSelecter2.substring(0, fieldCSSSelecter2.indexOf(str) + str.length());
					fieldRemainStr2 = fieldCSSSelecter2.substring(fieldCSSSelecter2.indexOf(fieldIncStrElemnts2) + fieldIncStrElemnts2.length(), fieldCSSSelecter2.length());
					fieldRemainStr2 = fieldRemainStr2.substring(fieldRemainStr2.indexOf(">") + 1, fieldRemainStr2.length());
					break;
				}
			}
		}
		if(fieldIncStrElemnts1.equals(fieldIncStrElemnts2)){
			fieldIncStrElemnts = fieldIncStrElemnts1;
		}else{
			System.out.println("Pls recheck fields field increment element..");
		}
		
		String[] title1RemainStrArr = fieldRemainStr1.split(">");
		String[] title2RemainStrArr = fieldRemainStr2.split(">");
		for(int i = 0; i < title1RemainStrArr.length; i++ ){
			if(title2RemainStrArr[i].isEmpty()){continue;}//may be titl1 and title2 css are different in length..
			if(!title1RemainStrArr[i].equals(title2RemainStrArr[i])){
				//#content > div.row.fixed > div.col-700.last > table.auto-style3 > tbody > tr:nth-child(2) > td.auto-style5:nth-child(2)
//edited												//#content > div.row.fixed > div.col-700.last > table.auto-style3 > tbody > tr:nth-child(3) > td.auto-style4:nth-child(2)
				//table.auto-style3
				if(title1RemainStrArr[i].contains("(")){
					if(title1RemainStrArr[i].contains(".")){
						if(title2RemainStrArr[i].contains(".")){
							String strRmn1 = title1RemainStrArr[i];
							String strRmn2 = title2RemainStrArr[i];
							String strClass1 = strRmn1.substring(strRmn1.indexOf("."), strRmn1.indexOf(":"));
							String strClass2 = "";
							if(title2RemainStrArr[i].contains("(")){
								strClass2 = strRmn2.substring(strRmn2.indexOf("."), strRmn2.indexOf(":"));
							}else{
								strClass2 = strRmn2.substring(strRmn2.indexOf("."), strRmn2.length());
							}
							if(strClass1.equals(strClass2)){
								fieldRemainStr = fieldRemainStr + " > " + title1RemainStrArr[i].trim();
							}else{
								strRmn1 = strRmn1.replace(strClass1, "");
								fieldRemainStr = fieldRemainStr + " > " + strRmn1.trim();
							}
						}else{
							String strRmn = title1RemainStrArr[i];
							String strReplace = strRmn.substring(strRmn.indexOf("."), strRmn.indexOf(":")); 
							strRmn = strRmn.replace(strReplace, "");
							fieldRemainStr = fieldRemainStr + " > " + strRmn.trim();
						}
					}else{
						fieldRemainStr = fieldRemainStr + " > " + title1RemainStrArr[i].trim();
					}
				}else if(title2RemainStrArr[i].contains("(")){
					if(title1RemainStrArr[i].contains(".") && title2RemainStrArr[i].contains(".")){
						String strRmn1 = title1RemainStrArr[i];
						String strRmn2 =  title2RemainStrArr[i];
						String strClass1 = strRmn1.substring(strRmn1.indexOf("."), strRmn1.length());
						String strClass2 = strRmn2.substring(strRmn2.indexOf("."), strRmn2.indexOf(":"));
						if(strClass1.equals(strClass2)){
							fieldRemainStr = fieldRemainStr + " > " + title1RemainStrArr[i].trim();
						}else{
							strRmn1 = strRmn1.substring(0, strRmn1.indexOf("."));
							fieldRemainStr = fieldRemainStr + " > " + strRmn1.trim();
						}
					}else{
						fieldRemainStr = fieldRemainStr + " > " + title1RemainStrArr[i].trim();//removing class and take index(same position have difference in css of two titles)
					}
				}else if(title1RemainStrArr[i].contains(".")
						 || title2RemainStrArr[i].contains(".")){
					String title1substr = title1RemainStrArr[i].trim();
					String title2substr = title2RemainStrArr[i].trim();
					String str1 = "";
					String str2 = "";
					if(title1substr.contains(".")){
					str1 = title1substr.substring(0,title1substr.indexOf(".")).trim();//if classes are unequal in css of two titles..we omit class and only take tag
					}else{
						str1 = title1substr.trim();
					}
					if(title2substr.contains(".")){
					str2 = title2substr.substring(0,title2substr.indexOf(".")).trim();//if classes are unequal in css of two titles..we omit class and only take tag
					}else{
						str2 = title2substr.trim();
					}
					if(str1.equals(str2)){
						fieldRemainStr = fieldRemainStr + " > " + str1.trim();
					}else{
						if(str1.contains(".")){
							str1 = str1.substring(0, str1.indexOf("."));
						}
						fieldRemainStr = fieldRemainStr + " > " + str1.trim();
					}
				}
			}else{
				fieldRemainStr = fieldRemainStr + " > " + title1RemainStrArr[i].trim();
			}
		 }
		}else{
			fieldRemainStr = fieldRemainStr1;
			fieldIncStrElemnts = fieldIncStrElemnts1;
		}
	}catch(Exception e){
		fieldIncStrElemnts = "";
		fieldRemainStr = "";
		e.getMessage();
	}
		System.out.println("fieldIncStrElemnts : " + fieldIncStrElemnts);
		System.out.println("fieldRemainStr : " + fieldRemainStr);
		return fieldIncStrElemnts + "~:~" + fieldRemainStr;
	}
	
	
	/**
	 * 
	 * @param jobDesc
	 * @return
	 */
	public String emailidFieldScrapper(String jobDesc){
		String email = "";
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
				 email = arrayElement;
				 continue;
			 }
			 if(!email.contains(arrayElement)){
			 email = email + "~:~" + arrayElement;
			 }
		 }
		}
		}catch(Exception e){email = "";}
		 return email;
	}
	/**
	 * 
	 * @param elements
	 * @throws IOException 
	 */
	public String locationFieldScrapWithBigList(String description){
		description = description.toUpperCase();
		description = description.replaceAll("\u00A0", " ").replaceAll("\u00a0", " ");
		StringBuffer locationBuffer = new StringBuffer();
		String locationFrmText = "";
		String pattern = "";
		InputStream inputStream = getClass().getResourceAsStream("/locationsBigList.txt");
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		/**
		 * location matcher
		 */
		try {
			while ((locationFrmText = reader.readLine()) != null) {
				pattern = locationFrmText.toUpperCase();
				Pattern r = Pattern.compile("\\b"+ pattern +"\\b");
				Matcher m = r.matcher(description);
				while(m.find()){ 
					locationBuffer.append(pattern +"/ ");
					}
				}
		} catch (IOException e) {
			e.printStackTrace();
		}
		String resultMatch = locationBuffer.toString();
		return resultMatch;
	}
	
	
}
