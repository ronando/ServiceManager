package com.crgt.service;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by android on 19/5/20.
 */

class ServiceManagerImpl {

    private Context mContext;
    private static ServiceManagerImpl sServiceManager;
    private ServiceCollector mServiceCollector;
    private boolean mDebugMode;
    private static Toast sToast;
    private Map<String, IService> mCachedServiceImplInstances = new HashMap<>();

    public static ServiceManagerImpl getInstance() {
        if (sServiceManager == null) {
            sServiceManager = new ServiceManagerImpl();
        }
        return sServiceManager;
    }

    private ServiceManagerImpl() {

    }

    public void init(Context context, boolean debug) {
        mContext = context;
        mDebugMode = debug;
        ServiceLog.enable = debug;
        mServiceCollector = new ServiceCollector();
        mServiceCollector.printAllServices();
    }

    public <T extends IService> T findService(Class<? extends T> serviceClass) {
        String serviceName = serviceClass.getName();
        if (mCachedServiceImplInstances.get(serviceName) != null) {
            return (T) mCachedServiceImplInstances.get(serviceName);
        }
        String serviceImplClassName = mServiceCollector.findService(serviceName);
        if (TextUtils.isEmpty(serviceImplClassName)) {
            ServiceLog.e("Can't find implementation class for " + serviceName);
            debugToast("找不到 " + serviceClass.getSimpleName() + " 的实现类");
            Object implObject = DynamicProxyHelper.getInstance(serviceClass);
            return (T) implObject;
        } else {
            IService serviceInstance = reflectServiceInstance(serviceImplClassName);
            if (serviceInstance == null) {
                ServiceLog.e("Can't reflect implementation class " + serviceImplClassName);
                Object implObject = DynamicProxyHelper.getInstance(serviceClass);
                return (T) implObject;
            } else {
                if (isSingleton(serviceInstance.getClass())) {
                    mCachedServiceImplInstances.put(serviceName, serviceInstance);
                }
                return (T) serviceInstance;
            }
        }
    }

    private IService reflectServiceInstance(String serviceName) {
        IService serviceInstance = null;
        try {
            serviceInstance = (IService) Class.forName(serviceName).newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return serviceInstance;
    }

    private void debugToast(String text) {
        if (mDebugMode) {
            toast(text, Toast.LENGTH_SHORT);
        }
    }

    private void toast(String text, int duration) {
        if (sToast == null) {
            sToast = Toast.makeText(mContext, text, duration);
        } else {
            sToast.setDuration(duration);
            sToast.setText(text);
        }
        sToast.show();
    }

    public boolean isSingleton(Class<?> reflectClass) {
        return reflectClass.getAnnotation(com.crgt.service.annotation.ServiceImpl.class).singleton();
    }
}
