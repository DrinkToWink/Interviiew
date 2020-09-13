package com.myinterview.dao;

import com.myinterview.entity.UserRedPacket;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

public interface UserRedPacketDao {

    //用户信息，返回插入的数据条数
    @Insert("insert into T_USER_RED_PACKET(red_packet_id,user_id,amount,grab_time,note)\n" +
            "        values (#{userRedPacket.redPacketId},#{userRedPacket.userId}," +
            "#{userRedPacket.amount},now(),#{userRedPacket.note})")
    public int grapRedPacket(@Param("userRedPacket") UserRedPacket userRedPacket);
}
