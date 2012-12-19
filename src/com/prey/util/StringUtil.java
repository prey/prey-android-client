package com.prey.util;

public class StringUtil {

	public static String firstCharUpper(String myString) {
		if (myString != null && myString.length() > 1) {
			char[] stringArray = myString.toCharArray();
			stringArray[0] = Character.toUpperCase(stringArray[0]);
			myString = new String(stringArray);
		}
		return myString;
	}

	
	public static boolean isTextBoolean(String texto){
		boolean out=false;
		if (texto!=null){
			texto=texto.toLowerCase().trim();
			if ("true".equals(texto)||"false".equals(texto)){
				return true;
			}
		}
		return out;		 
	}
	
	public static boolean isTextInteger(String texto){
		boolean out=false;
		if (texto!=null){
			texto=texto.toLowerCase().trim();
			try{
				Integer.parseInt(texto);
				return true;
			}catch(Exception e){
				return false;
			}
		}
		return out;		 
	}
}
