package com.myinterview.dao;

import com.myinterview.entity.RedPacket;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface RedPacketDao {

    /**
     *@param: 红包id
     *@return: 将通过红包id查询出的红包信息封装到红包对象RedPacket中,这是普通的sql语句
     * 会存在超发的问题
     */
    @Select("select id,user_id as userId,amount,send_date as sendDate,total,unit_amount as unitAmount,stock,version,note\n" +
            "        from T_RED_PACKET where id=#{id}")
    public RedPacket getRedPacket(@Param("id") Long id);


    /**
     * sql语句中加了一个for update表示使用悲观锁中的排他锁，这是数据库本身提供的功能
     * 超发问题解决，但是这种会造成性能不高的问题
     */
//    @Select("select id,user_id as userId,amount,send_date as sendDate,total,unit_amount as unitAmount,stock,version,note\n" +
//            "        from T_RED_PACKET where id=#{id} for update")
//    public RedPacket getRedPacket(@Param("id") Long id);


    /**
     *@param: 红包id
     *@return: 返回减少的个数（修改的数据条数）
     * 扣减抢红包数
     */
    @Update("update T_RED_PACKET set stock = stock-1 where id=#{id}")
    public int decreaseRedPacket(@Param("id") Long id);

    /**
     *@param:
     *@return:
     * 使用乐观锁来扣减红包
     */
    @Update("update T_RED_PACKET set stock = stock-1,version = version+1 where id=#{id} and version=#{version}")
    public int decreaseRedPacketForVersion(@Param("id") Long id, @Param("version") Integer version);

}
