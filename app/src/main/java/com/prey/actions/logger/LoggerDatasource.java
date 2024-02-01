/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2023 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.logger;

import android.content.Context;

import com.prey.PreyLogger;

import java.util.List;

public class LoggerDatasource {

    private LoggerOpenHelper dbHelper;

    public LoggerDatasource(Context context) {
        dbHelper = new LoggerOpenHelper(context);
    }

    public void createLogger(LoggerDto dto) {
        try {
            dbHelper.insertLogger(dto);
        } catch (Exception e) {
            try {
                dbHelper.updateLogger(dto);
            } catch (Exception e1) {
                PreyLogger.e(String.format("error db update:%s", e1.getMessage()), e1);
            }
        }
    }

    public void deleteLogger(int id) {
        dbHelper.deleteLogger(id);
    }

    public List<LoggerDto> getAllLogger() {
        return dbHelper.getAllLogger();
    }

    public LoggerDto getLogger(int id) {
        return dbHelper.getLogger(id);
    }

    public void deleteAllLogger() {
        dbHelper.deleteAllLogger();
    }

    public void deleteMinorsLogger(int id) {
        dbHelper.deleteMinorsLogger(id);
    }
}
