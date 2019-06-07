package com.crgt.service;

import android.content.Context;

/**
 * Created by android on 19/5/20.
 */

public class ServiceManager {

    public static void init(Context context, boolean debug) {
        ServiceManagerImpl.getInstance().init(context.getApplicationContext(), debug);
    }

    public static <T extends IService> T findService(Class<? extends T> serviceClass) {
        return ServiceManagerImpl.getInstance().findService(serviceClass);
    }
}
