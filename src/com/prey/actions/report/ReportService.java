package com.prey.actions.report;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.actions.HttpDataService;
import com.prey.actions.observer.ActionResult;
import com.prey.net.PreyHttpResponse;
import com.prey.net.PreyWebServices;
import com.prey.net.http.EntityFile;
import com.prey.util.ClassUtil;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

public class ReportService extends IntentService {

	public ReportService() {
		super("reportService");
	}

	public ReportService(String name) {
		super(name);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		JSONArray jsonArray = new JSONArray();
		List<String> listExclude = new ArrayList<String>();
		PreyLogger.i("start report");
		List<HttpDataService> listData = new ArrayList<HttpDataService>();
		Context ctx = this;
 
		jsonArray = new JSONArray();
		if (!listExclude.contains("picture"))
			jsonArray.put(new String("picture"));
		if (!listExclude.contains("location"))
			jsonArray.put(new String("location"));
		if (!listExclude.contains("access_points_list"))
			jsonArray.put(new String("access_points_list"));

		try {
			List<ActionResult> lista = new ArrayList<ActionResult>();
			for (int i = 0; i < jsonArray.length(); i++) {
				String nameAction = jsonArray.getString(i);
				PreyLogger.d("nameAction:" + nameAction);
				String methodAction = "report";
				JSONObject parametersAction = null;
				listData = ClassUtil.execute(ctx, lista, nameAction, methodAction, parametersAction, listData);
			}
		} catch (Exception e) {
		}

		int parms = 0;
		for (int i = 0; listData != null && i < listData.size(); i++) {
			HttpDataService httpDataService = listData.get(i);
			parms = parms + httpDataService.getDataAsParameters().size();
			if (httpDataService.getEntityFiles() != null) {
				for (int j = 0; j < httpDataService.getEntityFiles().size(); j++) {
					EntityFile entity = httpDataService.getEntityFiles().get(j);
					if (entity != null && entity.getLength() > 0) {
						parms = parms + 1;
					}
				}
			}
		}
 
		if (PreyConfig.getPreyConfig(ctx).isConnectionExists()) {
			if (parms > 0) {
				PreyHttpResponse response = PreyWebServices.getInstance().sendPreyHttpReport(ctx, listData);
				if (response != null) {
					PreyConfig.getPreyConfig(ctx).setLastEvent("report_send");
					PreyLogger.d("response.getStatusLine():" + response.getStatusLine());
					if (409 == response.getStatusLine().getStatusCode()) {
						PreyConfig.getPreyConfig(ctx).setMissing(false);
						PreyConfig.getPreyConfig(ctx).setIntervalReport("");
						ReportScheduled.getInstance(ctx).reset();
					}
				} 
			}
		}
 
		stopSelf();

	}

}
