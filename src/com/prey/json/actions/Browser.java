package com.prey.json.actions;

import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.database.Cursor;
 

import com.prey.PreyLogger;
import com.prey.actions.HttpDataService;
import com.prey.actions.browser.BrowserUtil;
import com.prey.actions.browser.PreyBrowser;
import com.prey.actions.observer.ActionResult;
import com.prey.net.PreyWebServices;

public class Browser {

	public  void history(Context ctx, List<ActionResult> list, JSONObject parameters) {
		BrowserUtil browser=new BrowserUtil();
		JSONArray array=browser.history(ctx);
		if(array!=null)
			PreyLogger.i("array length:"+array.length());
		else
			PreyLogger.i("array empty");
	}
	
	
	
	public HttpDataService start(Context ctx, List<ActionResult> lista, JSONObject parameters) {
		HttpDataService data = new HttpDataService("browser");
		HashMap<String, String> parametersMap =history(ctx);
		
		try {
			 
			 
			PreyWebServices.getInstance().sendBrowser(ctx, parametersMap);
		} catch (Exception ex) {
			PreyLogger.e("Error:" + ex.getMessage(), ex);

		}
		data.setList(true);
		data.addDataListAll(parametersMap);
		return data;
	}
	
	
	public HashMap<String, String> history(Context ctx) {
	
		HashMap<String, String> parametersMap = new HashMap<String, String>();
	
	
		
		Cursor c = null;
		try{
			PreyBrowser objBrowser= new PreyBrowser();
	     	c = ctx.getContentResolver().query(android.provider.Browser.BOOKMARKS_URI, null, null, null, null);
	        int totalCall = c.getCount();
            if (c.moveToFirst()) {
            	for (int i = 0; i < totalCall; i++) {
            		objBrowser = new PreyBrowser();
            		
            		
             		String bookmark=c.getString(c.getColumnIndex(android.provider.Browser.BookmarkColumns.BOOKMARK));
            		String created=c.getString(c.getColumnIndex(android.provider.Browser.BookmarkColumns.CREATED));
            		String date=c.getString(c.getColumnIndex(android.provider.Browser.BookmarkColumns.DATE));
            		String favicon=c.getString(c.getColumnIndex(android.provider.Browser.BookmarkColumns.FAVICON));
            		String title=c.getString(c.getColumnIndex(android.provider.Browser.BookmarkColumns.TITLE));
            		String url=c.getString(c.getColumnIndex(android.provider.Browser.BookmarkColumns.URL));
            		String visits=c.getString(c.getColumnIndex(android.provider.Browser.BookmarkColumns.VISITS));
            		
            		parametersMap.put("contacts_backup[" + i + "][bookmark]", bookmark);
            		parametersMap.put("contacts_backup[" + i + "][created]", created);
            		parametersMap.put("contacts_backup[" + i + "][date]", date);
            		parametersMap.put("contacts_backup[" + i + "][favicon]", favicon);
            		parametersMap.put("contacts_backup[" + i + "][title]", title);
            		parametersMap.put("contacts_backup[" + i + "][url]", url);
            		parametersMap.put("contacts_backup[" + i + "][visits]", visits);
            		 
            		
            		
            		
           
            	 
            		
            		 
            		 
            		PreyLogger.i(objBrowser.toString());
            	 
	                c.moveToNext();
	            }
	        }
		}catch(Exception e){
			 
		}finally{
			if (c!=null){
				 c.close();
			}
		}
		return parametersMap;
	}
}
