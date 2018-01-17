package com.akaldobaie.udacity.abnd.newsapp1;

import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<News>>
{
	
	private static final String LOG_TAG = MainActivity.class.getName();
	private static final String queryStringURL = "http://content.guardianapis.com/search?q=debates&api-key=test&show-tags=contributor";
	private static final int NEWS_LOADER_ID = 1;
	private NewsAdapter newsAdapter;
	private LoaderManager loaderManager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		ListView newsListView = findViewById(R.id.list);
		newsListView.setEmptyView(findViewById(R.id.empty_message_textview));
		
		newsAdapter = new NewsAdapter(
			 this, android.R.layout.simple_list_item_1, new ArrayList<News>());
		
		newsListView.setAdapter(newsAdapter);
		
		newsListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
			{
				
				News currentNews = newsAdapter.getItem(i);
				
				if (currentNews == null)
					return;
				
				String url = currentNews.getUrl();
				
				if (url == null || url.isEmpty())
					return;
				
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
				startActivity(intent);
			}
		});
		
		loaderManager = getLoaderManager();
		loaderManager.initLoader(NEWS_LOADER_ID, null, this);
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		
		if (!isInternetConnected() && loaderManager != null)
		{
			loaderManager.destroyLoader(NEWS_LOADER_ID);
		}
	}
	
	@Override
	public Loader<List<News>> onCreateLoader(int i, Bundle bundle)
	{
		// Create a new loader for the given URL
		return new NewsLoader(this, queryStringURL);
	}
	
	@Override
	public void onLoadFinished(Loader<List<News>> loader, List<News> news)
	{
		newsAdapter.clear();
		
		if (news != null && !news.isEmpty())
		{
			newsAdapter.addAll(news);
			showProgressSpinner(false);
			showMessage(false, 0);
			showTryAgainButton(false);
		} else
		{
			showProgressSpinner(false);
			showMessage(true, R.string.no_data_available);
			showTryAgainButton(true);
		}
	}
	
	@Override
	public void onLoaderReset(Loader<List<News>> loader)
	{
		newsAdapter.clear();
	}
	
	private static class NewsLoader extends AsyncTaskLoader<List<News>>
	{
		
		private final String LOG_TAG = NewsLoader.class.getName();
		private String mUrl;
		
		NewsLoader(Context context, String url)
		{
			super(context);
			
			mUrl = url;
		}
		
		@Override
		protected void onStartLoading()
		{
			forceLoad();
		}
		
		@Override
		public List<News> loadInBackground()
		{
			if (mUrl == null)
			{
				Log.e(LOG_TAG, "Error: URL not provided ");
				
				return null;
			}
			
			// Perform the network request, parse the response, and extract a list of news articles.
			return fetchNewsData(mUrl);
		}
	}
	
	/**
	 * Returns new URL object from the given string URL.
	 */
	private static URL createUrl(String stringUrl)
	{
		URL url = null;
		
		try
		{
			url = new URL(stringUrl);
		} catch (MalformedURLException e)
		{
			Log.e(LOG_TAG, "Error while building the URL ", e);
		}
		return url;
	}
	
	/**
	 * Make an HTTP request to the given URL and return a String as the response.
	 */
	private static String makeHttpRequest(URL url) throws IOException
	{
		String jsonResponse = "";
		
		if (url == null)
		{
			return jsonResponse;
		}
		
		HttpURLConnection urlConnection = null;
		InputStream inputStream = null;
		try
		{
			urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setReadTimeout(10000 /* milliseconds */);
			urlConnection.setConnectTimeout(15000 /* milliseconds */);
			urlConnection.setRequestMethod("GET");
			urlConnection.connect();
			
			// If the request was successful (response code 200),
			// then read the input stream and parse the response.
			if (urlConnection.getResponseCode() == 200)
			{
				inputStream = urlConnection.getInputStream();
				jsonResponse = readFromStream(inputStream);
			} else
			{
				Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
			}
		} catch (IOException e)
		{
			Log.e(LOG_TAG, "Error while retrieving the news JSON results. " + e.getMessage(), e);
		} finally
		{
			if (urlConnection != null)
			{
				urlConnection.disconnect();
			}
			if (inputStream != null)
			{
				inputStream.close();
			}
		}
		return jsonResponse;
	}
	
	/**
	 * Convert the {@link InputStream} into a String which contains the
	 * whole JSON response from the server.
	 */
	private static String readFromStream(InputStream inputStream) throws IOException
	{
		StringBuilder output = new StringBuilder();
		if (inputStream != null)
		{
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
			BufferedReader reader = new BufferedReader(inputStreamReader);
			String line = reader.readLine();
			while (line != null)
			{
				output.append(line);
				line = reader.readLine();
			}
		}
		return output.toString();
	}
	
	/*
	 * Return a list of {@link News} objects that has been built up from
	 * parsing the given JSON response.
	 */
	private static List<News> extractFeatureFromJson(String newsJSON)
	{
		if (TextUtils.isEmpty(newsJSON))
		{
			return null;
		}
		
		List<News> newsList = new ArrayList<>();
		
		try
		{
			JSONObject baseJsonResponse = new JSONObject(newsJSON);
			JSONArray newsArray = baseJsonResponse.getJSONObject("response").getJSONArray("results");
			
			for (int i = 0; i < newsArray.length(); i++)
			{
				JSONObject currentNews = newsArray.getJSONObject(i);
				
				String title = "";
				String date = "";
				String url = "";
				String sectionName = "";
				String sectionId = "";
				String authors = "";
				
				if (currentNews.has("webTitle"))
					title = currentNews.getString("webTitle");
				if (currentNews.has("webPublicationDate"))
					date = currentNews.getString("webPublicationDate");
				if (currentNews.has("webUrl"))
					url = currentNews.getString("webUrl");
				if (currentNews.has("sectionName"))
					sectionName = currentNews.getString("sectionName");
				if (currentNews.has("sectionId"))
					sectionId = currentNews.getString("sectionId");
				if (currentNews.has("tags"))
					authors = getAuthors(currentNews.getJSONArray("tags"));
				
				newsList.add(new News(title, date, url, sectionName, sectionId, authors));
			}
		} catch (JSONException e)
		{
			Log.e(LOG_TAG, "Error parsing the news JSON results", e);
		}
		
		return newsList;
	}
	
	public static List<News> fetchNewsData(String requestUrl)
	{
		URL url = createUrl(requestUrl);
		
		// Perform HTTP request to the URL and receive a JSON response back
		String jsonResponse = null;
		
		try
		{
			jsonResponse = makeHttpRequest(url);
		} catch (IOException e)
		{
			Log.e(LOG_TAG, "Error making the HTTP request.", e);
		}
		
		return extractFeatureFromJson(jsonResponse);
	}
	
	private static String getAuthors(JSONArray tagsJSONArray)
	{
		StringBuilder authors = new StringBuilder("");
		
		for (int j = 0; j < tagsJSONArray.length(); j++)
		{
			try
			{
				JSONObject tag = tagsJSONArray.getJSONObject(j);
				
				if (tag.has("firstName"))
				{
					// if more than one author add comma and space
					if (j > 0)
					{
						authors.append(", ");
					}
					authors.append(tag.getString("firstName"));
					authors.append(" ");
				}
				//if more than one author && without firstName add comma and space
				else if (j > 0 && tag.has("lastName"))
				{
					authors.append(", ");
				}
				
				if (tag.has("lastName"))
				{
					authors.append(tag.getString("lastName"));
				}
			} catch (JSONException e)
			{
				e.printStackTrace();
			}
		}
		Log.i(LOG_TAG, "Authors: " + authors.toString().trim());
		
		return authors.toString().trim();
	}
	
	private Boolean isInternetConnected()
	{
		showProgressSpinner(true);
		showMessage(false, 0);
		
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		boolean isConnected = false;
		
		if (cm != null)
		{
			NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
			isConnected = activeNetwork != null && activeNetwork.isConnected();
			
			if (!isConnected)
			{
				showTryAgainButton(true);
				showMessage(true, R.string.no_internet_connection);
			}
		}
		Log.i(LOG_TAG, "isInternetConnected: " + isConnected);
		return isConnected;
	}
	
	private void showTryAgainButton(Boolean show)
	{
		Button tryButton = findViewById(R.id.try_again_button);
		
		if (show)
		{
			tryButton.setVisibility(View.VISIBLE);
			tryButton.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					if (isInternetConnected())
					{
						reloadData();
					}
				}
			});
		} else
		{
			tryButton.setVisibility(View.GONE);
			showProgressSpinner(false);
		}
	}
	
	private void showProgressSpinner(Boolean show)
	{
		if (show)
			findViewById(R.id.loading_spinner_progress_bar).setVisibility(View.VISIBLE);
		else
			findViewById(R.id.loading_spinner_progress_bar).setVisibility(View.GONE);
	}
	
	private void showMessage(Boolean show, int messageResId)
	{
		if (show)
		{
			TextView messageTextView = findViewById(R.id.empty_message_textview);
			messageTextView.setText(messageResId);
			messageTextView.setVisibility(View.VISIBLE);
		} else
		{
			findViewById(R.id.empty_message_textview).setVisibility(View.GONE);
		}
	}
	
	private void reloadData()
	{
		loaderManager.restartLoader(NEWS_LOADER_ID, null, this);
	}
}