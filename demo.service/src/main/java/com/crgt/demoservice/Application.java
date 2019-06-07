package com.crgt.demoservice;

import com.crgt.service.ServiceManager;

/**
 * Created by android on 19/5/27.
 */

public class Application extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ServiceManager.init(this, true);
    }
}
