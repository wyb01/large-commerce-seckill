package com.itlaoqi.babytunseckill.controller;

import com.itlaoqi.babytunseckill.entity.Order;
import com.itlaoqi.babytunseckill.service.PromotionSecKillService;
import com.itlaoqi.babytunseckill.service.exception.SecKillException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
* @Description: 秒杀控制器
* @Author: wyb
* @Date: 2019-11-12 16:06:52
*/
@Controller
public class SecKillController {

    @Resource
    PromotionSecKillService promotionSecKillService;
    
    /**
    * @Description: 
    * @param psid: 活动id
    * @param userid: 当前用户id
    * @Return java.util.Map
    * @Author: wyb
    * @Date: 2019-11-12 16:20:43       
    */
    @RequestMapping("/seckill")
    @ResponseBody
    public Map processSecKill(Long psid , String userid){
        Map result = new HashMap();
        try {
            promotionSecKillService.processSecKill(psid , userid , 1);  //当前用户是否可以抢到该商品
            String orderNo = promotionSecKillService.sendOrderToQueue(userid);
            Map data = new HashMap();
            data.put("orderNo", orderNo);
            result.put("code", "0");
            result.put("message", "success");
            result.put("data", data);
        } catch (SecKillException e) {
            result.put("code", "500");
            result.put("message", e.getMessage());
        }
        return result;
    }
    @GetMapping("/checkorder")
    public ModelAndView checkOrder(String orderNo){
        Order order =  promotionSecKillService.checkOrder(orderNo);
        ModelAndView mav = new ModelAndView();
        if(order != null){
            mav.addObject("order", order);
            mav.setViewName("/order");
        }else{
            mav.addObject("orderNo", orderNo);
            mav.setViewName("/waiting");
        }
        return mav;
    }
}
