package com.zimdugo.push.domain;

public interface PushLockerNameReader {

    String findName(Long lockerId, PushLocale locale);
}
