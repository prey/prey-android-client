/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.location;

import java.util.HashMap;

import android.content.Context;
import android.location.Location;

import com.prey.R;
import com.prey.actions.HttpDataService;
import com.prey.actions.PreyAction;
import com.prey.actions.observer.ActionJob;
import com.prey.actions.observer.ActionResult;
import com.prey.exceptions.PreyException;

public class LocationNotifierAction extends PreyAction {

    public static final String DATA_ID = "geo";
    private HttpDataService data;
    private String lat;
    private String lng;

    public LocationNotifierAction() {
        data = new HttpDataService(LocationNotifierAction.DATA_ID);
        data.setList(true);
    }

    protected void notifyLocationChange(Location newLocation) {
        this.lat = Double.toString(newLocation.getLatitude());
        this.lng = Double.toString(newLocation.getLongitude());
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("lat", this.lat);
        parameters.put("lng", this.lng);
        parameters.put("acc", Float.toString(newLocation.getAccuracy()));
        parameters.put("alt", Double.toString(newLocation.getAltitude()));
        data.getDataList().putAll(parameters);
        parameters = null;
    }

    @Override
    public String textToNotifyUserOnEachReport(Context ctx) {
        String prefix = ctx.getText(R.string.location_notification_prefix).toString();
        String latToShow = "";
        String lonToShow = "";
        try {
            latToShow = lat.substring(0, 6);
            lonToShow = lng.substring(0, 6);
        } catch (Exception e) {
        }
        return prefix.concat(" lat=".concat(latToShow).concat(" lon=").concat(lonToShow));
    }

    @Override
    public boolean shouldNotify() {
        return true;
    }

    @Override
    public void execute(ActionJob actionJob, Context ctx) throws PreyException {
        HttpDataService data = LocationUtil.dataLocation(ctx,null,false);
        ActionResult result = new ActionResult();
        result.setDataToSend(data);
        actionJob.finish(result);
    }

    @Override
    public boolean isSyncAction() {
        return true;
    }

    public int getPriority(){
        return GEO_PRIORITY;
    }

}