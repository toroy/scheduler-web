package com.zhugeio.platform.scheduler.web.core.utils;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;

/**
 * guava本地缓存
 * 
 * @author 陈泰（周利江）
 * @Date 2018年10月9日上午11:48:17
 *
 */
public class GuavaCacheUtil {
	
	/**
	 * 单个缓存最大值
	 */
	private static int MAXIMUM_SIZE = 10_000;
	
	/**
	 * 缓存过期时间，单位秒
	 */
	private static int EXPIRE_TIME_SECODDS = 30 * 60;
	
	private static Cache<String, Optional<Object>> CACHE;
	
	static {
		CACHE = CacheBuilder.newBuilder().refreshAfterWrite(EXPIRE_TIME_SECODDS, TimeUnit.SECONDS)
				.maximumSize(MAXIMUM_SIZE).build(new CacheLoader<String, Optional<Object>>() {
					@Override
					public Optional<Object> load(String key) throws Exception {
						return null;
					}
				});
	}
	
	public static void put(String key, Object val) {
		CACHE.put(key, Optional.ofNullable(val));
	}

	
	public static void clear(Object key) {
		CACHE.invalidate(key);
	}
	
	public static Object get(String key, Callable<? extends Optional<Object>> valueLoader) {
		try {
			Optional<Object> opt = CACHE.get(key, valueLoader);
			return opt.isPresent() ? opt.get() : null;
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static <T> T get(String key) {
		try {
			Optional<Object> opt = CACHE.get(key, new Callable<Optional<Object>>() {

				@Override
				public Optional<Object> call() throws Exception {
					return Optional.ofNullable(null);
				}
			});
			if (opt.isPresent())
				return (T) opt.get();
			else
				return null;
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return null;
		
	}

	
	public static void main(String[] args) throws InterruptedException {
		GuavaCacheUtil.put("test","test");
		String val = GuavaCacheUtil.get("test");
		System.out.println("值:"+val);
		for (int i = 0; i< 200; i++) {
			Thread.sleep(1000);
			val = GuavaCacheUtil.get("test");
			if (val == null) {
				System.out.println("is null");
			}
			System.out.println("值:"+val);
		}
	}
	
}
