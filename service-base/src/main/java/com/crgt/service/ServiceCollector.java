package com.crgt.service;

import java.util.ArrayList;
import java.util.List;

public class ServiceCollector {
    private List<ModuleServiceCollector> mCollector = new ArrayList<>();
    private ServiceMap mServiceMap = new ServiceMap();

    public ServiceCollector() {
        collectModuleCollectors();
        for (ModuleServiceCollector collector : mCollector) {
            collector.collect(mServiceMap);
        }
    }

    public String findService(String serviceName) {
        return mServiceMap.findService(serviceName);
    }

    public void printAllServices() {
        mServiceMap.printAllServices();
    }


    public void collectModuleCollectors() {
        // this method will be auto generated, don not to change it!
        // for example:
        // mCollector.add(new ModuleServiceCollector$$demoservice());
    }
}
