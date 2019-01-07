package com.prey.actions.autoconnect;

import com.prey.PreyLogger;

import java.util.ArrayList;
import java.util.List;

public class AutoConnectBlacklist {


    private static AutoConnectBlacklist instance=null;
    private AutoConnectBlacklist(){
        blackList=new ArrayList<>();
    }
    private List<String> blackList;

    public static AutoConnectBlacklist getInstance(){
        if(instance==null){
            instance=new AutoConnectBlacklist();
        }
        return instance;
    }

    public boolean contains(String ssid){
        return blackList.contains(ssid);
    }
    public void add(String ssid){
        blackList.add(ssid);
    }

    public void print( ){

        for (int i=0;blackList!=null&&i<blackList.size();i++){
            PreyLogger.d("blackList["+i+"]"+blackList.get(i));
        }

    }

}
