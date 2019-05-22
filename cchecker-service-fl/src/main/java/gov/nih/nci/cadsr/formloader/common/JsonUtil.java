package gov.nih.nci.cadsr.formloader.common;

import com.google.gson.Gson;

public class JsonUtil {

	@SuppressWarnings("unchecked")
	public static  <T> T clone(T t) {
	   Gson gson = new Gson();
	   String json = gson.toJson(t);
	   return (T) gson.fromJson(json, t.getClass());
	}

}