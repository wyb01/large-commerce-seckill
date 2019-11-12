package com.itlaoqi.babytunseckill.scheduler;

import com.itlaoqi.babytunseckill.dao.PromotionSecKillDAO;
import com.itlaoqi.babytunseckill.entity.PromotionSecKill;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
public class SecKillTask {

    @Resource
    private PromotionSecKillDAO promotionSecKillDAO;

    @Resource
    private RedisTemplate redisTemplate; //RedisTempldate是Spring封装的Redis操作类，提供了一系列操作redis的模板方法

    /**
    * @Description: 定时器，监测活动是否开始
    * @Return void
    * @Author: wyb
    * @Date: 2019-11-12 17:35:10
    */
    @Scheduled(cron = "0/5 * * * * ?")
    public void startSecKill(){
        //查询当前时间位于"开始时间"和"结束时间且还未开始的活动
        List<PromotionSecKill> list  = promotionSecKillDAO.findUnstartSecKill();
        for(PromotionSecKill ps : list){
            System.out.println(ps.getPsId() + "秒杀活动已启动");
            //删掉以前重复的活动任务缓存
            redisTemplate.delete("seckill:count:" + ps.getPsId());
            //有几个库存商品，则初始化几个list对象
            for(int i = 0 ; i < ps.getPsCount() ; i++){
                redisTemplate.opsForList().rightPush("seckill:count:" + ps.getPsId() , ps.getGoodsId());
            }
            ps.setStatus(1); //活动启动
            promotionSecKillDAO.update(ps);
        }
    }

    /**
    * @Description: 定时器，监测活动是否结束
    * @Return void
    * @Author: wyb
    * @Date: 2019-11-12 17:35:45
    */
    @Scheduled(cron = "0/5 * * * * ?")
    public void endSecKill(){
        //进行中的且结束时间小于当前时间的活动
        List<PromotionSecKill> psList = promotionSecKillDAO.findExpireSecKill();
        for (PromotionSecKill ps : psList) {
            System.out.println(ps.getPsId() + "秒杀活动已结束");
            ps.setStatus(2);  //秒杀活动结束
            promotionSecKillDAO.update(ps);
            redisTemplate.delete("seckill:count:" + ps.getPsId());  //到达结束时间后，redis删除该活动
        }
    }
}
