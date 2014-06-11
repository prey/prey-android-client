package com.prey.actions.browser;

import org.json.JSONArray;

import android.content.Context;
import android.database.Cursor; 
import android.provider.Browser; 

import com.prey.PreyLogger; 

public class BrowserUtil {

	public JSONArray history(Context ctx){
		JSONArray array=new JSONArray();
		Cursor c = null;
		try{
			PreyBrowser objBrowser= new PreyBrowser();
	     	c = ctx.getContentResolver().query(Browser.BOOKMARKS_URI, null, null, null, null);
	        int totalCall = c.getCount();
            if (c.moveToFirst()) {
            	for (int i = 0; i < totalCall; i++) {
            		objBrowser = new PreyBrowser();
            		objBrowser.setBookmark(c.getString(c.getColumnIndex(Browser.BookmarkColumns.BOOKMARK)));
            		objBrowser.setCreated (c.getString(c.getColumnIndex(Browser.BookmarkColumns.CREATED)));
            		objBrowser.setDate (c.getString(c.getColumnIndex(Browser.BookmarkColumns.DATE)));
            		objBrowser.setFavicon (c.getString(c.getColumnIndex(Browser.BookmarkColumns.FAVICON)));
            		objBrowser.setTitle (c.getString(c.getColumnIndex(Browser.BookmarkColumns.TITLE)));
            		objBrowser.setUrl(c.getString(c.getColumnIndex(Browser.BookmarkColumns.URL)));
            		objBrowser.setVisits (c.getString(c.getColumnIndex(Browser.BookmarkColumns.VISITS)));
            	 
            		
            		 
            		 
            		PreyLogger.i(objBrowser.toString());
            		array.put(objBrowser.toJSONObject());
	                c.moveToNext();
	            }
	        }
		}catch(Exception e){
			 
		}finally{
			if (c!=null){
				 c.close();
			}
		}
 		return array;
	}
}
