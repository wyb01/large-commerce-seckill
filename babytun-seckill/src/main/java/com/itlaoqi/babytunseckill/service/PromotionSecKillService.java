package com.itlaoqi.babytunseckill.service;

import com.itlaoqi.babytunseckill.dao.OrderDAO;
import com.itlaoqi.babytunseckill.dao.PromotionSecKillDAO;
import com.itlaoqi.babytunseckill.entity.Order;
import com.itlaoqi.babytunseckill.entity.PromotionSecKill;
import com.itlaoqi.babytunseckill.service.exception.SecKillException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
/*@@请加Q群：369531466,与几百名工程师共同学习,遇到难题可随时@老齐,多一点真诚，少一点套路@@*/
public class PromotionSecKillService {
    @Resource
    private PromotionSecKillDAO promotionSecKillDAO;
    @Resource
    private RedisTemplate redisTemplate;
    @Resource //RabbitMQ客户端
    private RabbitTemplate rabbitTemplate;

    @Resource
    private OrderDAO orderDAO;

    public void processSecKill(Long psId, String userid, Integer num) throws SecKillException {
        PromotionSecKill ps = promotionSecKillDAO.findById(psId); //参与秒删的活动
        if (ps == null) {
            //秒杀活动不存在
            throw new SecKillException("秒杀活动不存在");
        }
        if (ps.getStatus() == 0) {
            throw new SecKillException("秒杀活动还未开始");
        } else if (ps.getStatus() == 2) {
            throw new SecKillException("秒杀活动已经结束");
        }
        Integer goodsId = (Integer) redisTemplate.opsForList().leftPop("seckill:count:" + ps.getPsId()); //先在集合左边删除该商品数量1
        if (goodsId != null) {
            //判断当前用户是否已经抢购过
            boolean isExisted = redisTemplate.opsForSet().isMember("seckill:users:" + ps.getPsId(), userid);
            if (!isExisted) { //当前用户还没抢到过商品
                System.out.println("恭喜您，抢到商品啦。快去下单吧");
                redisTemplate.opsForSet().add("seckill:users:" + ps.getPsId(), userid); //当前用户抢到该商品，缓存入redis
            }else{
                redisTemplate.opsForList().rightPush("seckill:count:" + ps.getPsId(), ps.getGoodsId()); //参加过该活动，删除的商品重新添加回redis
                throw new SecKillException("抱歉，您已经参加过此活动，请勿重复抢购！");
            }
        } else {
            throw new SecKillException("抱歉，该商品已被抢光，下次再来吧！！");
        }
    }

    /**
    * @Description: 向MQ发送订单消息
    * @param userid:
    * @Return java.lang.String
    * @Author: wyb
    * @Date: 2019-11-12 17:03:59
    */
    public String sendOrderToQueue(String userid) {
        System.out.println("准备向队列发送信息");
        //订单基本信息
        Map data = new HashMap();
        data.put("userid", userid);  //当前用户id
        String orderNo = UUID.randomUUID().toString();
        data.put("orderNo", orderNo);
        //附加额外的订单信息
        rabbitTemplate.convertAndSend("exchange-order" , null , data);
        System.out.println("向队列发送信息成功！");
        return orderNo;
    }

    /**
    * @Description: 查询订单
    * @param orderNo:
    * @Return com.itlaoqi.babytunseckill.entity.Order
    * @Author: wyb
    * @Date: 2019-11-12 16:57:15
    */
    public Order checkOrder(String orderNo){
        Order order = orderDAO.findByOrderNo(orderNo);
        return order;
    }
}
