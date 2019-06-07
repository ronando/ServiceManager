package com.crgt.demoservice.serviceimpl;

import com.crgt.demo.service.TestInterface;
import com.crgt.demo.service.TestInterface2;
import com.crgt.service.annotation.ServiceImpl;

/**
 * Created by android on 18/5/9.
 */

@ServiceImpl
public class TestInterfaceOneImpl implements TestInterface, TestInterface2 {

    @Override
    public float calculate() {
        return 1;
    }

    @Override
    public float calculate2() {
        return 2;
    }
}
