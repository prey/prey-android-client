package com.prey.util;

import java.lang.reflect.Field;

public class RUtil {

	public static Integer idStringXml(String id) {
		return idString(id, "string");
	}

	public static Integer idColorXml(String id) {
		return idString(id, "color");
	}

	public static Integer idDrawableXml(String id) {
		return idString(id, "drawable");
	}

	public static Integer idRawXml(String id) {
		return idString(id, "raw");
	}

	@SuppressWarnings("rawtypes")
	public static Integer idString(String id, String tipo) {
		try {
			Class actionClass = Class.forName("com.prey.R$" + tipo);
			Field myField = actionClass.getDeclaredField(id);
			return (Integer) myField.get(null);
		} catch (Exception e) {
			return null;
		}
	}
}
