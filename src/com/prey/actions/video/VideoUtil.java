package com.prey.actions.video;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
 

import android.content.Context;
import android.content.Intent;

 
import com.prey.PreyLogger;
import com.prey.actions.HttpDataService;
import com.prey.actions.camera.CameraAction;
import com.prey.activities.VideoActivity;
 
import com.prey.net.http.EntityFile;

public class VideoUtil {

	
	public static HttpDataService getVideo(Context ctx) {
		HttpDataService data = null;
		
		VideoActivity.activity = null;
		InputStream inFile = null;
        try {
                Intent intent = new Intent(ctx, VideoActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        
                ctx.startActivity(intent);
                
                int i = 0;
                while (VideoActivity.activity == null && i < 10) {
                        try {
                                Thread.sleep(1000);
                        } catch (InterruptedException e) {
                        }
                        PreyLogger.i("esperando antes take video[" + i + "]");
                        i++;
                }

                VideoActivity.activity.start();
                try {
                    Thread.sleep(30000);
                } catch (InterruptedException e) {
                }
                VideoActivity.activity.stop();
                
                File file = new File(VideoActivity.filePath);
                
                if (file != null&&file.length()>0) {
     				PreyLogger.i("dataVideo data length=" + file.length());
     				inFile = new FileInputStream(file);
     				 
     				EntityFile entityFile = new EntityFile();
     				entityFile.setFile(inFile);
     				entityFile.setMimeType("video/mp4");
     				entityFile.setName("video.mp4");
     				entityFile.setType("video");

     				data = new HttpDataService(CameraAction.DATA_ID);
     				data.setList(true);
     				data.addEntityFile(entityFile);
     			} 

        } catch(Exception e){
        		PreyLogger.e("Error, causa:"+e.getMessage(), e);
        } finally {
                if(VideoActivity.activity!=null){
                	VideoActivity.activity.finish();
                	VideoActivity.activity = null;
                }
                
        }
        
		return data;
	}
	
	public static void deleteFile(){
		File file = new File(VideoActivity.filePath);

		file.delete();
	}
	
	 
}
