package com.hirrr.jobsnatcher.Object;

import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
@Component
public class ScrapFields {
	public Elements elements;
	public Integer titleIndexFlag;
	public Integer title1Index;
	public Integer title2Index;
	public String titleRemainStr;
	public String titleIncStrElemnts;
	public Elements getElements() {
		return elements;
	}
	public void setElements(Elements elements) {
		this.elements = elements;
	}
	public Integer getTitleIndexFlag() {
		return titleIndexFlag;
	}
	public void setTitleIndexFlag(Integer titleIndexFlag) {
		this.titleIndexFlag = titleIndexFlag;
	}
	public Integer getTitle1Index() {
		return title1Index;
	}
	public void setTitle1Index(Integer title1Index) {
		this.title1Index = title1Index;
	}
	public Integer getTitle2Index() {
		return title2Index;
	}
	public void setTitle2Index(Integer title2Index) {
		this.title2Index = title2Index;
	}
	public String getTitleRemainStr() {
		return titleRemainStr;
	}
	public void setTitleRemainStr(String titleRemainStr) {
		this.titleRemainStr = titleRemainStr;
	}
	public String getTitleIncStrElemnts() {
		return titleIncStrElemnts;
	}
	public void setTitleIncStrElemnts(String titleIncStrElemnts) {
		this.titleIncStrElemnts = titleIncStrElemnts;
	}

}
