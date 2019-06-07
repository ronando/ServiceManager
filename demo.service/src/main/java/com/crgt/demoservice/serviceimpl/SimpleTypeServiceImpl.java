package com.crgt.demoservice.serviceimpl;

import android.util.Log;

import com.crgt.demoservice.service.SimpleTypeService;
import com.crgt.service.annotation.ServiceImpl;

/**
 * Created by android on 19/5/20.
 */

@ServiceImpl
public class SimpleTypeServiceImpl implements SimpleTypeService {
    @Override
    public int getInt() {
        return 10;
    }

    @Override
    public void get() {
        Log.e("TestService", "republish");
    }
}
