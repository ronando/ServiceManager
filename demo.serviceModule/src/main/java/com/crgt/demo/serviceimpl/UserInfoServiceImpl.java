package com.crgt.demo.serviceimpl;

import com.crgt.demo.service.UserInfoService;
import com.crgt.service.annotation.ServiceImpl;

/**
 * Created by android on 19/5/20.
 */

@ServiceImpl
public class UserInfoServiceImpl implements UserInfoService {


    @Override
    public String getUserId() {
        return "123456";
    }

    @Override
    public String getUserToken() {
        return "user token";
    }

}
