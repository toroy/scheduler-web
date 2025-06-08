package com.zhugeio.platform.scheduler.web.core.utils;

import java.io.IOException;
import java.util.Map;

import com.zhugeio.platform.scheduler.common.util.Assert;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Slf4j
public class OkHttp3Utils {

	private static OkHttpClient okHttpClient;
	
	static {
		okHttpClient = new OkHttpClient();
	}
	
	public static Response getClient(Request request) {
		Call call = okHttpClient.newCall(request);
		Response response = null;
		try {
			response = call.execute();
		} catch (IOException e) {
			log.error("request:{}",request, e);
		}
		return response;
	}
	
	public static Response post(String url, String data) {
		Assert.notBlank(url);
		Assert.notBlank(data);
		
		RequestBody body = RequestBody.create(MediaType.parse("application/json"), data);
		Request request = new Request.Builder().url(url).post(body).build();
		return OkHttp3Utils.getClient(request);
	}
	
	public static Response get(String url, Map<String, String> headerMap) {
		Assert.notBlank(url);
		
		Headers headers = Headers.of(headerMap);
		Request request = new Request.Builder().url(url).headers(headers).get().build();
		return OkHttp3Utils.getClient(request);
	}
	
}
