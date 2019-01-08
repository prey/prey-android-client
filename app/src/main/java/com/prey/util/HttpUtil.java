/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2019 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.util;

import android.os.StrictMode;

import com.prey.PreyLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLConnection;

import java.net.URL;

public class HttpUtil {

    public static String getContents(String url) {
        String contents ="";
        InputStream in=null;
        try {
            StrictMode.ThreadPolicy policy =null;
            policy=new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            URLConnection conn = new URL(url).openConnection();
            in = conn.getInputStream();
            contents = convertStreamToString(in);
        } catch (Exception e) {
            PreyLogger.e("getContents error:"+e.getMessage(),e);
        } finally {
            if(in!=null){
                try {
                    in.close();
                } catch (Exception e) {
                }
            }
        }
        return contents;
    }

    private static String convertStreamToString(InputStream is) {
        InputStreamReader inReader=null;
        BufferedReader reader =null;
        StringBuilder sb =null;
        try {
            inReader=new InputStreamReader(is, "UTF-8");
            reader = new BufferedReader(inReader);
            sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (Exception e) {
        } finally {
            try {
                if(reader!=null)
                    reader.close();
            } catch (IOException e) {
            }
            try {
                if(inReader!=null)
                    inReader.close();
            } catch (IOException e) {
            }
            try {
                if(is!=null)
                    is.close();
            } catch (IOException e) {
            }
        }
        return sb==null?"":sb.toString();
    }

}
