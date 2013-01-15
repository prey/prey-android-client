package com.prey.util;

import java.lang.reflect.Method;
import java.util.List;

import org.json.JSONObject;

import android.content.Context;

import com.prey.PreyLogger;
import com.prey.actions.observer.ActionResult;
 
 

public class ClassUtil {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void execute(Context ctx,List<ActionResult> lista,String nameAction, String methodAction, JSONObject parametersAction) {

		PreyLogger.i("name:" + nameAction);
		PreyLogger.i("target:" + methodAction);
		PreyLogger.i("options:" + parametersAction);
		nameAction = StringUtil.classFormat(nameAction);
		
		
		try {
			Class actionClass = Class.forName("com.prey.json.actions." + nameAction);
			Object actionObject = actionClass.newInstance();
			Method method = actionClass.getMethod(methodAction, new Class[] {Context.class, List.class, JSONObject.class });
			Object[] params = new Object[] { ctx,lista,parametersAction };
			method.invoke(actionObject, params);

		} catch (Exception e) {
			PreyLogger.e("Error, causa:" + e.getMessage(), e);
		}
	}
}
