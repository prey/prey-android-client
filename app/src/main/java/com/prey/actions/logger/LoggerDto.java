/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2023 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.logger;

public class LoggerDto {

    private int loggerId;
    private String txt;
    private String type;
    private String time;

    public int getLoggerId() {
        return loggerId;
    }

    public void setLoggerId(int loggerId) {
        this.loggerId = loggerId;
    }

    public String getTxt() {
        return txt;
    }

    public void setTxt(String txt) {
        this.txt = txt;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("loggerId:").append(loggerId).append(",");
        sb.append("txt:").append(txt).append(",");
        sb.append("type:").append(type);
        return sb.toString();
    }

}