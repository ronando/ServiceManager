package com.crgt.service;

import java.lang.reflect.Proxy;

/**
 * Created by android on 19/5/28.
 */

public class DynamicProxyHelper {
    public static Object getInstance(Class<?> cls) {
        MethodProxy invocationHandler = new MethodProxy();
        Object newProxyInstance = Proxy.newProxyInstance(
                cls.getClassLoader(),
                new Class[]{cls},
                invocationHandler);
        return newProxyInstance;
    }
}
