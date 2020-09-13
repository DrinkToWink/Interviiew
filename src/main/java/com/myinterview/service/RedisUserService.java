package com.myinterview.service;

/**
 * @author: 一个小菜逼
 * @Date: 2020/3/31
 * redis的抢红包
 */
public interface RedisUserService {

    public Long grapRedPacketByRedis(Long redPacketId, Long userId);
}
