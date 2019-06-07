package com.crgt.demo.service;


import com.crgt.service.IService;

/**
 * Created by android on 19/5/20.
 */

public interface UserInfoService extends IService {
    String getUserId();
    String getUserToken();
}
