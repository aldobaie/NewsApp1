package com.akaldobaie.udacity.abnd.newsapp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by Abdullah Aldobaie (akdPro) on 1/14/18 at 5:51 PM.
 * <p>
 * This file is created for and owned by SaudiCamp.
 * Visit SaudiCamp.com for more info
 * or contact: Android.SaudiCamp@gmail.com
 * or contact: SaudiCamp@gmail.com
 * <p>
 * This is not an open source code, Please, destroy copy or have a written license from owner.
 * <p>
 * Copyright (c) 2018 SaudiCamp
 */

class NewsAdapter extends ArrayAdapter<News> {
	/**
	 * Constructor
	 *
	 * @param context
	 * 	 The current context.
	 * @param resource
	 * 	 The resource ID for a layout file containing a TextView to use when
	 * 	 instantiating views.
	 * @param objects
	 * 	 The objects to represent in the ListView.
	 */
	public NewsAdapter(@NonNull Context context, int resource, @NonNull List<News> objects) {
		super(context, resource, objects);
		
		
	}
	
	
	@NonNull
	@Override
	public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
		
		View listItemView = convertView;
		
		if (listItemView == null) {
			listItemView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);
		}
		
		News currentNews = getItem(position);
		
		TextView sectionTextView = listItemView.findViewById(R.id.section_textview);
		sectionTextView.setText(currentNews.getSectionName());
		
		TextView dateTextView = listItemView.findViewById(R.id.date_textview);
		dateTextView.setText(currentNews.getFormattedDate());
		
		TextView titleTextView = listItemView.findViewById(R.id.title_textview);
		titleTextView.setText(currentNews.getTitle());
		
		return listItemView;
	}
}
