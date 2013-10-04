package com.prey.actions.picture;

import java.util.ArrayList;

import com.prey.actions.HttpDataService;
import com.prey.net.PreyWebServices;

import android.content.Context;

public class PictureThread extends Thread {
	private Context ctx;

	public PictureThread(Context ctx) {
		this.ctx = ctx;
	}

	public void run() {
		HttpDataService data= PictureUtil.getPicture(ctx);
		ArrayList<HttpDataService> dataToBeSent = new ArrayList<HttpDataService>();
		dataToBeSent.add(data);
		PreyWebServices.getInstance().sendPreyHttpData(ctx, dataToBeSent);
	}

}
