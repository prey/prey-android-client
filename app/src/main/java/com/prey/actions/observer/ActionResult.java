package com.prey.actions.observer;

/**
 * Created by oso on 24-08-15.
 */
import com.prey.actions.HttpDataService;

public class ActionResult {

    private String result;
    private HttpDataService dataToSend;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public HttpDataService getDataToSend() {
        return dataToSend;
    }

    public void setDataToSend(HttpDataService dataToSend) {
        this.dataToSend = dataToSend;
    }

}

