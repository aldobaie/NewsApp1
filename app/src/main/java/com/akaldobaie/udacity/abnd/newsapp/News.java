package com.akaldobaie.udacity.abnd.newsapp;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Abdullah Aldobaie (akdPro) on 1/14/18 at 5:44 PM.
 *
 */
class News {
	
	private String title = "";
	private String publicationDate = "";
	private String url = "";
	private String sectionName = "";
	private String sectionID = "";
	private String formattedDate = "";
	
	/**
	 * News model constructor
	 *
	 * @param title
	 * @param publicationDate
	 * @param url
	 * @param sectionName
	 * @param sectionID
	 */
	News(String title, String publicationDate, String url, String sectionName, String sectionID) {
		
		this.title = title;
		this.publicationDate = publicationDate;
		this.url = url;
		this.sectionName = sectionName;
		this.sectionID = sectionID;
		
		DateFormat desiredDateFormat = new SimpleDateFormat("LLL dd, yyyy");
		DateFormat currentDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'Z'");
		Date date = null;
		
		try {
			date = currentDateFormat.parse(publicationDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		if (date != null) {
			this.formattedDate = desiredDateFormat.format(date); //dateFormatter.format(currentNews.getPublicationDate());
		}
	}
	
	String getTitle() {
		return title;
	}
	
	String getPublicationDate() {
		return publicationDate;
	}
	
	String getUrl() {
		return url;
	}
	
	String getSectionName() {
		return sectionName;
	}
	
	String getSectionID() {
		return sectionID;
	}
	
	String getFormattedDate() {
		return formattedDate;
	}
}
