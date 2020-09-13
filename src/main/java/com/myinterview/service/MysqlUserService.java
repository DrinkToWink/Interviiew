package com.myinterview.service;

/**
 * 保存mysql抢红包信息
 */
public interface MysqlUserService {

    public int grapRedPacket(Long redPacketId, Long userId);

}
