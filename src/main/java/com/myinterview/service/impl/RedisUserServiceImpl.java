package com.myinterview.service.impl;

import com.myinterview.service.RedisSaveService;
import com.myinterview.service.RedisUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

@Component
public class RedisUserServiceImpl implements RedisUserService {

    @Autowired
    private RedisSaveService redisSaveService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    //Lua脚本，这两个..总算弄明白了，相当于java中的+，就是字符串的拼接
    //tonumber函数可以把其他类型的数据转换为数字，并返回转换后的数字值，如果转换失败，则返回nil
    //lua中的nil相当于java中的null
    //这段lua脚本的作用：第一次调用，先从redis中取出hash类型的数据，key为redPacket stock
    //进行判断，如果stock<0,脚本结束，返回0；stock>0,将stock进行减1
    //将操作后的stock转换成字符串存入redis的hash数据类型，key为 redPacket stock
    //将list类型的数据存入redis，key为listkey 值为传递过来的第一个参数
    //判断stock是否为0，如果为0，则返回2（为啥？），否则返回1
    String script = "local listKey = 'red_packet_list_'..KEYS[1] \n"
            + "local redPacket = 'red_packet_'..KEYS[1] \n"
            + "local stock = tonumber(redis.call('hget', redPacket, 'stock')) \n"
            + "if stock <= 0 then return 0 end \n"
            + "stock = stock -1 \n"
            + "redis.call('hset', redPacket, 'stock', tostring(stock)) \n"
            + "redis.call('rpush', listKey, ARGV[1]) \n"
            + "if stock == 0 then return 2 end \n"
            + "return 1 \n";

    //在缓存Lua脚本后，使用该变量保存Redis返回的SHA1编码，让它去执行缓存的Lua脚本
    String shal = null;


    //使用redis配合Lua实现抢红包
    public Long grapRedPacketByRedis(Long redPacketId, Long userId) {
        //当前抢红包的用户和信息（userId和当前系统时间）userId-当前时间
        String args = userId + "-" + System.currentTimeMillis();
        Long result = null;
        //获取底层Redis操作对象
        Jedis jedis = (Jedis) redisTemplate.getConnectionFactory().getConnection().getNativeConnection();
        try {
            if (shal == null) {
                //将脚本加载到redis的服务器端，返回shal编码
                shal = jedis.scriptLoad(script);

                //shal=jedis.scriptLoad(myScript);
            }
            //根据sha1码执行脚本，将redPacketId和userId-当前时间，当做参数传入脚本，返回执行结果
            //程序运行脚本时报错
            Object res = jedis.evalsha(shal, 1, redPacketId + "", args);

            result = (Long) res;
            //System.out.println(result);
            //System.in.read();
            //返回2时表示最后一个红包，此时抢红包的信息会通过异步保存到数据库中
            if (result == 2) {
                //从redis中获取hash类型的数据，key为"red_packet_"+redPacketId hash的key为unit_amount
                String unitAmountStr = jedis.hget("red_packet_" + redPacketId, "unit_amount");
                //将从redis中获取的unitAmountStr变成double类型的数据
                Double unitAmount = Double.parseDouble(unitAmountStr);
                //输出当前线程的名字
                System.out.println("抢红包时的线程:" + Thread.currentThread().getName());
                //将红包id和红包金额放入redis（应该是这样）
                redisSaveService.saveUserRedPacketByRedis(redPacketId, unitAmount);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (jedis != null && jedis.isConnected()) {
                jedis.close();
            }
        }
        return result;
    }
}
