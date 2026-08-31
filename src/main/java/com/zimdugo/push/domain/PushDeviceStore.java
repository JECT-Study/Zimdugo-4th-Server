package com.zimdugo.push.domain;

public interface PushDeviceStore {

    void save(String tokenHash);
}
