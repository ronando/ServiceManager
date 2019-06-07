package com.crgt.service;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by android on 19/5/27.
 *
 * @author android
 */

public class ServiceMap {
    private HashMap<String, String> map = new HashMap<>();

    public void put(String serviceClassName, String serviceImplClassName) {
        if (map.get(serviceClassName) != null) {
            throw new RuntimeException("Can't have two service impl for service : " + serviceClassName);
        }
        map.put(serviceClassName, serviceImplClassName);
    }

    public String findService(String serviceName) {
        return map.get(serviceName);
    }

    public HashMap<String, String> getMap() {
        return map;
    }

    public void printAllServices() {
        for (String key : map.keySet()) {
            System.out.println("service -> (" + key + ", " + map.get(key) + ")");
        }
    }
}
