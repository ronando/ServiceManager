package com.crgt.demo.serviceimpl;

import com.crgt.demo.service.ObjectEntity;
import com.crgt.demo.service.ObjectService;
import com.crgt.service.annotation.ServiceImpl;

/**
 * Created by lujie on 2019/6/7.
 * jesse.lu@crgecent.com
 */

@ServiceImpl(singleton = true)
public class ObjectServiceImpl implements ObjectService {

    private ObjectEntity objectEntity = new ObjectEntity();

    @Override
    public ObjectEntity getObject() {
        return objectEntity;
    }
}
