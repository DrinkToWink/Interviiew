package com.myinterview.controller;

import com.myinterview.service.MysqlUserService;
import com.myinterview.service.RedisUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/userRedPacket")
public class UserRedPacketController {

    @Autowired
    private MysqlUserService mysqlUserService;

    @Autowired
    private RedisUserService redisUserService;

    @RequestMapping("/grapRedPacket")
    //@ResponseBody//转换为json返回给前端请求
    public Map<String,Object> grapRedPacket(Long redPacketId,Long userId){
        Map<String,Object> retMap = new HashMap<String, Object>();
        //使用redis数据库
        //Long result = redisUserService.grapRedPacketByRedis(redPacketId,userId);

        //使用mysql数据库,这种方式会存在超发的情况，想解决这种问题
        //就要在sql语句中使用乐观锁，或者是悲观锁
        int result = mysqlUserService.grapRedPacket(redPacketId,userId);

        //大于0，表示抢红包成功
        boolean flag =result>0;
        retMap.put("success",flag);
        retMap.put("message",flag?"抢红包成功":"抢红包失败");
        return retMap;
    }

}
