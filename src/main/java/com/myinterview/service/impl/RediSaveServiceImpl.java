package com.myinterview.service.impl;

import com.alibaba.druid.pool.DruidDataSource;
import com.myinterview.entity.UserRedPacket;
import com.myinterview.service.RedisSaveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@Component
public class RediSaveServiceImpl implements RedisSaveService {

    //每次取出1000条记录，避免一次取出消耗太多内存
    private static final int TIME_SIZE = 1000;

    private static final String PREFIX = "red_packet_list_";

    //要加上泛型，否则不能操作redis，不知道为啥
    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    //数据源
    @Autowired
    private DruidDataSource druidDataSource;

    /**
     *@param: 红包id:redPacketId  红包金额:unitAmount
     *@return:
     * 开启新的线程 自动创建一个线程执行 采用jdbc的批量处理，每1000条保存一次
     */
    @Async
    public void saveUserRedPacketByRedis(Long redPacketId, Double unitAmount) throws SQLException {
        System.out.println("开始保存数据");
        //获取当前系统时间
        Long start = System.currentTimeMillis();
        //一个红包被多少个用户同时抢，返回用户数量
        System.out.println("插入数据时的线程："+Thread.currentThread().getName());
        BoundListOperations ops = redisTemplate.boundListOps("red_packet_list_"+redPacketId);

        //用户数量，转化为long
        Long size = ops.size();
        //times代表插入的批次,用户是否为1000的整数倍，如果是则取结果，如果不是这取结果加1;
        Long times = size%TIME_SIZE==0?size/TIME_SIZE:size/TIME_SIZE+1;
        int count =0;
        //初始化ArrayList数组空间为1000（可以存1000个UserRedPacket类型的数据，不够的话会自动扩容）
        List<UserRedPacket> userRedPacketList = new ArrayList<UserRedPacket>(TIME_SIZE);
        for(int i=0;i<times;i++){
            List userIdList = null;
            if(i==0){
                //为0的话，第一次取出0-1000的用户数量
                userIdList = ops.range(i*TIME_SIZE,(i+1)*TIME_SIZE);
            }else{
                //从上一次取出的数量加1开始，在往后取出1000个用户
                userIdList = ops.range(i*TIME_SIZE+1,(i+1)*TIME_SIZE);
            }
            //清空集合，并不会释放集合初始化分配的1000个空间，只会把1000个空间中的引用清空掉
            //系统GC时，则会把1000引用所指向的对象清理掉
            userRedPacketList.clear();
            //遍历用户
            for(int j=0;j<userIdList.size();j++){
                //获取数据，转换成字符串
                String args = userIdList.get(j).toString();
                //将字符串以“-”分割开，保存在字符串数组arr中
                String[] arr = args.split("-");
                //获取用户id，前面拼接字符串的时候，就是按照这种方式来进行拼接的
                String userIdStr = arr[0];
                //获取抢红包的时间
                String timeStr = arr[1];
                //将用户id的字符串，转换为long类型
                Long userId = Long.parseLong(userIdStr);
                //将时间转换成long类型
                Long time = Long.parseLong(timeStr);
                //创建用户对象,将redis数据库中查询出来的信息，保存在UserRedPacket对象中
                UserRedPacket userRedPacket = new UserRedPacket();
                userRedPacket.setRedPacketId(redPacketId);
                userRedPacket.setUserId(userId);
                userRedPacket.setAmount(unitAmount);
                //创建一个TimeStamp类型的时间对象，每种时间对象，都对应不同的数据库的时间类型
                userRedPacket.setGrabTime(new Timestamp(time));
                userRedPacket.setNote("抢红包:"+redPacketId);
                //将UserRedPacket对象保存在userRedPacket集合中
                userRedPacketList.add(userRedPacket);
            }
            //每次插入1000个数据
            count+=executeBatch(userRedPacketList);
        }
        //把被抢红包对应的用户数据清空掉
        redisTemplate.delete("red_packet_list_"+redPacketId);
        //记录当前时间
        Long end = System.currentTimeMillis();
        //输出将数据插入到数据库中所用的时间
        System.out.println("耗时："+(end-start)+",插入数量:"+count);
    }

    //批处理：这里使用Statement而不是PreparedStatement
    //https://www.cnblogs.com/tommy-huang/p/4540407.html
    //https://blog.csdn.net/zhangw1236/article/details/54583192
    // 特点：批处理是指一次性执行多条SQL语句，并且在执行过程中，如果某条语句出现错误，则仅停止该错误语句的执行，而批处理中其他所有语句则继续执行。
    //Statement:
    //优点：可以向数据库发送多条不同的ＳＱＬ语句。
    //缺点：
    //SQL语句没有预编译。
    //PreparedStatement:
    //优点：可以通过占位符预编译，简化了重复属性多条格式相同的语句。
    //缺点：执行批处理的时候只能执行同一格式类型的语句，不能混合其他语句同时执行
    private int executeBatch(List<UserRedPacket> userRedPacketList) throws SQLException {
        Connection conn = null;
        Statement stmt = null;
        int[] count = null;
        try {
            //不会自动提交，而需要使用conn.commit()方法，手动提交事务
            conn = druidDataSource.getConnection();
            //设置非自动提交
            conn.setAutoCommit(false);
            stmt = conn.createStatement();
            for (UserRedPacket userRedPacket : userRedPacketList) {
                String sql1 = "update T_RED_PACKET set stock = stock-1 where id=" + userRedPacket.getRedPacketId();
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String sql2 = "insert into T_USER_RED_PACKET(red_packet_id, user_id, " + "amount, grab_time, note)"
                        + " values (" + userRedPacket.getRedPacketId() + ", " + userRedPacket.getUserId() + ", "
                        + userRedPacket.getAmount() + "," + "'" + df.format(userRedPacket.getGrabTime()) + "'," + "'"
                        + userRedPacket.getNote() + "')";
                //将两个sql语句都放入执行对象stmt中，同时执行这两条sql
                //可能会出现一条执行成功，一条执行失败的情况
                stmt.addBatch(sql1);
                stmt.addBatch(sql2);
            }
            //执行批处理命令 如果出现问题 executeBatch()就会抛出异常回滚，返回批处理数据的数量
            //返回的是一个数组，数组里面存放的每条数据处理的状态（是否成功等等...一系列的状态）
            count = stmt.executeBatch();
            //stmt.clearBatch() 清空addBatch，这里不需要
            for(int i=0;i<count.length;i++){
                //如果执行某一条sq时失败，则抛出异常，因为事务还为提交，所以数据并不会更新到数据库中
                if(count[i]==0){
                    //这里会遇到这种情况，更新库存，但是不是目标红包id,需要更新的是id：1，结果我们跟新了id：5
                    //如，即使id5的红包不存在，也不会报错，只会返回0而已，这时候数据库还是更新了，只不过是跟新了用户信息而已
                    throw new SQLException();
                }
            }
            //提交事务 如果代码正常得commit之后才会操作数据库
            conn.commit();
            conn.setAutoCommit(true);//在把自动提交打开
        }catch (SQLException ex){
            //上面如果出现异常，就进行回滚
            conn.rollback();
            //抛出异常，提示用户
            ex.printStackTrace();
            throw new RuntimeException("抢红包批量执行错误");
        }finally {
           try {
               {
                   //判断条件，关闭资源
                   if(stmt!=null&&!stmt.isClosed()){
                       stmt.close();
                   }
                   if(conn!=null&&!conn.isClosed()){
                       conn.close();
                   }
               }
           }catch (Exception ex){
               //资源关闭出现异常，打印信息进行提示，并没有做处理，只是简单的打印了一下
               ex.printStackTrace();
           }
        }
        //返回执行sql的数据的数量/2，除以2，
        //添加了两条sql，批处理执行1次，数据库的sql执行2000次，除以2，表示每条sql执行的次数
        return count.length/2;
    }
}
