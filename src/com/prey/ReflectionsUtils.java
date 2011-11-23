/*******************************************************************************
 * Created by Carlos Yaconi.
 * Copyright 2011 Fork Ltd. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
/**
 * 
 */
package com.prey;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

/**
 * @author Administrador
 * 
 */
public class ReflectionsUtils {

	private static final String ATTRIBUTE_CLASS = "className";

	/**
	 * 
	 */
	public ReflectionsUtils() {
		// TODO Auto-generated constructor stub
	}

	public static Class getJSONObjectClass(JSONObject object) {
		Class clazz = null;
		try {
			String className = (String) object.get(ATTRIBUTE_CLASS);
			clazz = Class.forName(className);

		} catch (JSONException e) {
			Log.e("Reflections Utils", "Ocurrio un error al obtener la clase del JSONObject");
		} catch (ClassNotFoundException e) {
			Log.e("Reflections Utils", "La clase que se intenta crear no Existe!");
		}

		return clazz;
	}

	public static Object JSONObjectToObject(JSONObject object) {
		Class clazz = ReflectionsUtils.getJSONObjectClass(object);
		try {
			return clazz.newInstance();
		} catch (IllegalAccessException e) {
			Log.e("Reflections Utils", "[Seguridad] Ocurrio un error al instanciar la clase " + clazz);
		} catch (InstantiationException e) {
			Log.e("Reflections Utils", "[Desconocido] Ocurrio un error al instanciar la clase " + clazz);
		}

		return null;
	}

	public static Object populateObject(JSONObject object) throws Exception {

		Object newObject = ReflectionsUtils.JSONObjectToObject(object);
		Field[] fields = newObject.getClass().getDeclaredFields();

		for (int i = 0; i < fields.length; i++) {
			Field f = fields[i];
			String setterName = "set" + f.getName().substring(0, 1).toUpperCase() + f.getName().substring(1);
			if (object.has(f.getName()))
				ReflectionsUtils.setProperty(setterName, newObject, object.get(f.getName()));
		}

		return newObject;
	}

	public static List<Object> populateListObject(JSONArray json) throws Exception {
		List<Object> result = new ArrayList<Object>();
		for (int i = 0; i < json.length(); i++) {
			result.add(ReflectionsUtils.populateObject(((JSONObject) json.get(i))));

		}
		return result;
	}

	public static void setProperty(String nameProperty, Object target, Object value) throws Exception {
		Class[] parameterTypes = { value.getClass() };
		Method method = target.getClass().getMethod(nameProperty, parameterTypes);
		if (method != null) {
			Object[] paraneters = { value };
			method.invoke(target, paraneters);
		}

	}

}
