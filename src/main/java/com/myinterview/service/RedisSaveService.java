package com.myinterview.service;

import java.sql.SQLException;

public interface RedisSaveService {

    //保存redis抢红包列表
    public void saveUserRedPacketByRedis(Long redPacketId, Double unitAmount) throws SQLException;
}
