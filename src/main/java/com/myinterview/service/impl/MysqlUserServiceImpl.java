package com.myinterview.service.impl;

import com.myinterview.dao.RedPacketDao;
import com.myinterview.dao.UserRedPacketDao;
import com.myinterview.entity.RedPacket;
import com.myinterview.entity.UserRedPacket;
import com.myinterview.service.MysqlUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author: 一个小菜逼
 * @Date: 2020/3/31
 */
@Component
public class MysqlUserServiceImpl implements MysqlUserService {

    @Autowired
    private UserRedPacketDao userRedPacketDao;

    @Autowired
    private RedPacketDao redPacketDao;

    private final int FAILED = 0;


    /**
     *@param: redPacketId 前台传过来的红包id
     *@param userId 前台传入的用户id
     *@return:
     *@Transactional 放在方法上面表示该方法的事务交给spring进行管理，如果方法中对数据库执行的操作成功
     * 则由spring进行事务的提交，如果失败，则由spring进行回滚,springboot会默认开启注解配置
     * 使用注解配置的事务，只用@Transactional一个注解就行了
     * ioslation 事务的隔离级别，Isolation.READ_COMMITTED：读已经提交的数据
     * propagation 事务传播行为的设置，Propagation.REQUIRED：支持当前事务如果当前没有事务，则新建事务
     * 存在的问题：普通方式存在超发现象
     */

    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public int grapRedPacket(Long redPacketId, Long userId) {
        //获取红包信息
        RedPacket redPacket = redPacketDao.getRedPacket(redPacketId);
        //红包库存大于0
        if (redPacket.getStock() > 0) {
            //传入红包id进行红包数量减一
            redPacketDao.decreaseRedPacket(redPacketId);
            //创建用户对象，将红包id、用户id、红包数量封装到该用户对象中
            UserRedPacket userRedPacket = new UserRedPacket();
            userRedPacket.setRedPacketId(redPacketId);
            userRedPacket.setUserId(userId);
            userRedPacket.setAmount(redPacket.getAmount());
            //这个属性有啥用，没看出来
            userRedPacket.setNote("抢红包:" + redPacketId);
            //将用户对象存放到数据库中
            int result = userRedPacketDao.grapRedPacket(userRedPacket);
            //返回存放的数据条数
            return result;
        }
        //红包库存小于0，直接返回失败
        return FAILED;
    }


    /**
     * 乐观锁实现扣减红包，解决超发问题，没有进行优化的情况
     *
     */

//    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
//    public int grapRedPacket(Long redPacketId, Long userId) {
//        //获取红包信息
//        RedPacket redPacket = redPacketDao.getRedPacket(redPacketId);
//        //红包库存大于0
//        if (redPacket.getStock() > 0) {
//            //传入红包id进行红包数量减一
//            int updateResult =
//                    redPacketDao.decreaseRedPacketForVersion(redPacketId, redPacket.getVersion());
//            //如果扣减失败，则updateResult=0，结束方法
//            if (updateResult<=0){
//                return FAILED;
//            }
//            //创建用户对象，将红包id、用户id、红包数量封装到该用户对象中
//            UserRedPacket userRedPacket = new UserRedPacket();
//            userRedPacket.setRedPacketId(redPacketId);
//            userRedPacket.setUserId(userId);
//            userRedPacket.setAmount(redPacket.getAmount());
//            //这个属性有啥用，没看出来
//            userRedPacket.setNote("抢红包:" + redPacketId);
//            //将用户对象存放到数据库中
//            int result = userRedPacketDao.grapRedPacket(userRedPacket);
//            //返回存放的数据条数
//            return result;
//        }
//        //红包库存小于0，直接返回失败
//        return FAILED;
//    }


    /**
     * 乐观锁，时间重试机制
     */
//    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
//    public int grapRedPacket(Long redPacketId, Long userId) {
//        //获取红包信息
//        RedPacket redPacket = redPacketDao.getRedPacket(redPacketId);
//        long startTime = System.currentTimeMillis();
//        while (true) {
//            long endTime = System.currentTimeMillis();
//            if (endTime-startTime>100){
//                return FAILED;
//            }
//            //红包库存大于0
//            if (redPacket.getStock() > 0) {
//                //传入红包id进行红包数量减一
//                int updateResult =
//                        redPacketDao.decreaseRedPacketForVersion(redPacketId, redPacket.getVersion());
//                //如果扣减失败，则updateResult=0，结束方法
//                if (updateResult <= 0) {
//                    return FAILED;
//                }
//                //创建用户对象，将红包id、用户id、红包数量封装到该用户对象中
//                UserRedPacket userRedPacket = new UserRedPacket();
//                userRedPacket.setRedPacketId(redPacketId);
//                userRedPacket.setUserId(userId);
//                userRedPacket.setAmount(redPacket.getAmount());
//                //这个属性有啥用，没看出来
//                userRedPacket.setNote("抢红包:" + redPacketId);
//                //将用户对象存放到数据库中
//                int result = userRedPacketDao.grapRedPacket(userRedPacket);
//                //返回存放的数据条数
//                return result;
//            }
//            //红包库存小于0，直接返回失败
//            return FAILED;
//        }
//    }


    /**
     * 乐观锁，循环控制重试策略
     */

//    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
//    public int grapRedPacket(Long redPacketId, Long userId) {
//        for (int i = 0; i < 3; i++) {
//            //获取红包信息
//            RedPacket redPacket = redPacketDao.getRedPacket(redPacketId);
//            //红包库存大于0
//            if (redPacket.getStock() > 0) {
//                //传入红包id进行红包数量减一
//                int updateResult =
//                        redPacketDao.decreaseRedPacketForVersion(redPacketId, redPacket.getVersion());
//                //如果扣减失败，则updateResult=0，结束方法
//                if (updateResult <= 0) {
//                    return FAILED;
//                }
//                //创建用户对象，将红包id、用户id、红包数量封装到该用户对象中
//                UserRedPacket userRedPacket = new UserRedPacket();
//                userRedPacket.setRedPacketId(redPacketId);
//                userRedPacket.setUserId(userId);
//                userRedPacket.setAmount(redPacket.getAmount());
//                //这个属性有啥用，没看出来
//                userRedPacket.setNote("抢红包:" + redPacketId);
//                //将用户对象存放到数据库中
//                int result = userRedPacketDao.grapRedPacket(userRedPacket);
//                //返回存放的数据条数
//                return result;
//            }
//        }
//        //红包库存小于0，直接返回失败
//        return FAILED;
//    }


}
