package com.prey.json.actions;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.content.Context;

import com.prey.PreyLogger;
import com.prey.actions.HttpDataService;
import com.prey.actions.observer.ActionResult;
import com.prey.json.JsonAction;

public class SystemInstall extends JsonAction {

	public HttpDataService run(Context ctx, List<ActionResult> lista, JSONObject parameters) {
		return null;
	}

	public void start(Context ctx, List<ActionResult> lista, JSONObject parameters) {

		List<String> installAsSystem = new ArrayList<String>();
		installAsSystem.add("mount -o remount,rw /system"); 
		installAsSystem.add("mv /data/app/com.prey*.apk /system/app/com.prey.apk"); 
		installAsSystem.add("chmod 755 /system/app/com.prey*");
		installAsSystem.add("chown root.root /system/app/com.prey*");
		installAsSystem.add("mount -o remount,ro /system");
		installAsSystem.add("reboot" );
		DataOutputStream dos =null;
		Process suProcess = null;
		try {
			Runtime r = Runtime.getRuntime();
			suProcess = r.exec("su");
			dos = new DataOutputStream(suProcess.getOutputStream());
			for (int i = 0; installAsSystem != null && i < installAsSystem.size(); i++) {
				dos.writeBytes(installAsSystem.get(i));
				dos.flush();
			}
		} catch (Exception e) {
			PreyLogger.e("Error, causa:" + e.getMessage(), e);
		}finally{
			if(dos!=null){
				try {
					dos.close();
				} catch (IOException e) {
				}
			}
			if(suProcess!=null){
				try {
					suProcess.destroy();
				} catch (Exception e) {
				}
			}
		}

	}

	public void stop(Context ctx, List<ActionResult> lista, JSONObject parameters) {

	}

}
