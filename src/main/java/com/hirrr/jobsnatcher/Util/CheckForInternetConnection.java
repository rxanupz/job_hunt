package com.hirrr.jobsnatcher.Util;

import java.io.IOException;

import org.jsoup.Jsoup;

public class CheckForInternetConnection {
	public static boolean isInternetConnected () {
		try {
			Jsoup.connect("http://google.com").timeout(25000).get();
			return true;
		} catch (IOException e) {
			System.out.println("Internet is not connected "+e);
		}
		
		return false;
	}
}
