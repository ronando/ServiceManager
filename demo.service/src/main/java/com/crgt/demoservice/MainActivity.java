package com.crgt.demoservice;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.crgt.demo.service.ObjectService;
import com.crgt.demo.service.UserInfoService;
import com.crgt.demoservice.service.SimpleTypeService;
import com.crgt.service.ServiceManager;

/**
 * route demo activity
 */

public class MainActivity extends Activity {
    private static final String TAG = "ServiceManager";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //调用其他模块服务
        UserInfoService userInfoService = ServiceManager.findService(UserInfoService.class);
        String userId = userInfoService.getUserId();
        String userToken = userInfoService.getUserToken();
        Log.e(TAG, "userId: " + userId + " userToken: " + userToken);


        //调用其他模块服务, 返回复杂类型
        ObjectService objectService = ServiceManager.findService(ObjectService.class);
        Log.d(TAG, "objectService:" + objectService.getObject().id);

        //调用本模块服务, 返回简单类型
        SimpleTypeService simpleTypeService = ServiceManager.findService(SimpleTypeService.class);
        int unPublishCount = simpleTypeService.getInt();
        Log.e(TAG, "simpleTypeService: " + unPublishCount);



        objectService.getObject().id = 102;
        ObjectService objectService2 = ServiceManager.findService(ObjectService.class);
        Log.d(TAG, "objectService2:" + objectService2.getObject().id);

    }


    private void toastMsg(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
