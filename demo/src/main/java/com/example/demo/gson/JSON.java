package com.example.demo.gson;

import java.lang.reflect.Type;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

/**
 * Gson包装工具类
 * 
 * @author Aris
 *
 */
public final class JSON {

	/**
	 * @description:
	 * @param {type}
	 * @return:
	 */
	private JSON() {

	}

	static final GsonBuilder builder;

	static final Gson gson;

	static {
		builder = new GsonBuilder();
		builder.registerTypeAdapter(Double.class, new JsonSerializer<Double>() {
			@Override
			public JsonElement serialize(Double src, Type typeOfSrc, JsonSerializationContext context) {
				if (src == src.longValue()) {
					return new JsonPrimitive(src.longValue());
				} else {
					return new JsonPrimitive(src);
				}
			}
		});
		gson = builder.create();
	}

	/**
	 * 对象转JSON字符串
	 * 
	 * @param object
	 * @return
	 */
	public static String toJSONString(Object object) {
		return gson.toJson(object);
	}

	/**
	 * 对象转JSON字符串
	 * 
	 * @param object
	 * @param typeOfSrc
	 * @return
	 */
	public static String toJSONString(Object object, Type typeOfSrc) {
		return gson.toJson(object, typeOfSrc);
	}

	/**
	 * JSON对象转JSON字符串，包含JsonObject、JsonArray
	 * 
	 * @param jsonElement
	 * @return
	 */
	public static String toJSONString(JsonElement jsonElement) {
		return gson.toJson(jsonElement);
	}

	/**
	 * json字符串转对象
	 * 
	 * @param <T>
	 * @param json
	 * @param classOfT
	 * @return
	 */
	public static <T> T parse(String json, Class<T> classOfT) {
		return gson.fromJson(json, classOfT);
	}

	/**
	 * json 字符串转对象
	 * 
	 * @param <T>
	 * @param json
	 * @param typeOfT
	 * @return
	 */
	public static <T> T parse(String json, Type typeOfT) {
		return gson.fromJson(json, typeOfT);
	}

	/**
	 * json 字符串转对象
	 * 
	 * @param <T>
	 * @param json
	 * @param typeToken
	 * @return
	 */
	public static <T> T parse(String json, TypeToken<T> typeToken) {
		return gson.fromJson(json, typeToken.getType());
	}

	/**
	 * json 字符串转换为JsonObject
	 * 
	 * @param json
	 * @return
	 */
	public static JsonObject parseObject(String json) {
		return gson.fromJson(json, JsonObject.class);
	}

	/**
	 * json 字符串转换为JsonArray
	 * 
	 * @param json
	 * @return
	 */
	public static JsonArray parseArray(String json) {
		return gson.fromJson(json, JsonArray.class);
	}

	/**
	 * 对象转换为json对象
	 * 
	 * @param object
	 * @return
	 */
	public static JsonElement toJsonElement(Object object) {
		return gson.toJsonTree(object);
	}

	/**
	 * 对象转换为json对象
	 * 
	 * @param object
	 * @return
	 */
	public static JsonElement toJsonElement(Object object, Type typeOfT) {
		return gson.toJsonTree(object, typeOfT);
	}
}
