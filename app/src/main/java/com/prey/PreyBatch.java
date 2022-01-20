package com.prey;

import android.content.Context;
import android.content.res.Resources;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PreyBatch {

    private static PreyBatch _instance = null;
    private Properties properties;

    private PreyBatch(Context ctx) {
        try {
            PreyLogger.d("Loading config batch properties from file...");
            properties = new Properties();
            InputStream is = ctx.getResources().openRawResource(R.raw.batch);
            properties.load(is);
            is.close();
            PreyLogger.d("Batch Config: " + properties);
        } catch (Resources.NotFoundException e) {
            PreyLogger.e("Batch Config file wasn't found", e);
        } catch (IOException e) {
            PreyLogger.e("Couldn't read config file", e);
        }
    }

    public static PreyBatch getInstance(Context ctx) {
        if (_instance == null)
            _instance = new PreyBatch(ctx);
        return _instance;
    }

    /**
     * Method get api key
     * @return
     */
    public String getApiKeyBatch() {
        return properties.getProperty("api-key-batch");
    }

    /**
     * Method get email
     * @return
     */
    public String getEmailBatch() {
        return properties.getProperty("email-batch");
    }

    /**
     * Method if it asks for the name
     * @return
     */
    public boolean isAskForNameBatch() {
        return Boolean.parseBoolean(properties.getProperty("ask-for-name-batch"));
    }

}
