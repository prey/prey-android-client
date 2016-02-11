/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.json.actions;

import android.content.Context;
import android.os.Environment;
import android.webkit.MimeTypeMap;

import com.prey.PreyLogger;
import com.prey.actions.observer.ActionResult;
import com.prey.json.UtilJson;
import com.prey.net.PreyWebServices;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.List;

public class Tree {


    public void get(Context ctx, List<ActionResult> list, JSONObject parameters) {
        PreyLogger.i("Tree");
        try{
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, UtilJson.makeMapParam("get", "tree", "started"));

            int depth = 0;
            try {
                depth=Integer.parseInt(parameters.getString("depth"));
            }catch(Exception e){
            }
            String path = parameters.getString("path");

            if ("sdcard".equals(path)){
                path="/";
            }

            String    pathBase = Environment.getExternalStorageDirectory().toString();

            File dir = new File(pathBase+path);

            PreyLogger.i("________");
            PreyLogger.i("________");

            JSONArray array = new JSONArray();
          // array=getFilesRecursiveJSON(pathBase, dir,depth);




            String tree=array.toString();
            String tree2=tree;
            int length=tree2.length();
            while(length>0) {
                if(length>100){
                    PreyLogger.i(tree2.substring(0,100));
                    tree2=tree2.substring(100);
                    length=tree2.length();
                }else{
                    PreyLogger.i(tree2);
                    length=0;
                }


            }


            JSONObject jsonTree = new JSONObject();

            jsonTree.put("tree", tree);


            PreyWebServices.getInstance().postJsonAutentication(ctx, jsonTree);




            //PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, UtilJson.makeMapParam("get", "tree", "stopped"));
        } catch (Exception e) {
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx, UtilJson.makeMapParam("get", "tree", "failed", e.getMessage()));
        }
        PreyLogger.i("________");
        PreyLogger.i("________");



    }





    private JSONArray getFilesRecursiveJSON(String pathBase, File folder,int depth) {
        depth=0;
        int sizze=0;
        try {
            sizze = folder.listFiles().length;
        }catch (Exception e){}
        // PreyLogger.i("folder:"+folder+" files:"+sizze);
        JSONArray array=new JSONArray();
        try {

            for (int i=0;folder!=null&&folder.listFiles()!=null&&i< sizze;i++) {
                File child=folder.listFiles()[i];

                String parent = child.getParent().replace(pathBase, "");
                JSONObject json = new JSONObject();
                int size=0;
                try {
                    size = child.listFiles().length;
                }catch(Exception e){}
                // PreyLogger.i("dir:"+child.isDirectory()+" files:"+size+" name:"+child.getName());
                if (child.isDirectory()&&size>0) {
                    json.put("name", child.getName());
                    json.put("path", parent+"/"+child.getName());
                    JSONArray listChildren =new JSONArray ();
                    if(depth>0) {
                        listChildren = getFilesRecursiveJSON(pathBase, child, depth - 1);
                        json.put("children", listChildren);
                    }


                    json.put("isFile", false);
                    array.put(json);

                }

                if (child.isFile()) {

                    String extension = MimeTypeMap.getFileExtensionFromUrl(child.getName());
                    MimeTypeMap mime = MimeTypeMap.getSingleton();
                    json.put("name", child.getName());
                    json.put("path", parent+"/"+child.getName());
                    json.put("mimetype", mime.getMimeTypeFromExtension(extension));
                    json.put("size", child.length());
                    json.put("isFile", true);
                    array.put(json);
                }



            }
        } catch (Exception e) {
            PreyLogger.e("Error:"+e.getMessage(),e);
        }
        return array;
    }
}

