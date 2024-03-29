package com.itlaoqi.babytunseckill.service;

import com.itlaoqi.babytunseckill.dao.OrderDAO;
import com.itlaoqi.babytunseckill.entity.Order;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Date;
import java.util.Map;

/**
* @Description: 消费消息
* @Author: wyb
* @Date: 2019-11-12 17:27:23       
*/
@Component
public class OrderConsumer {

    @Resource
    private OrderDAO orderDAO;

    //rabbitmq监听器
    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = "queue-order") ,   //队列
                    exchange = @Exchange(value = "exchange-order" , type = "fanout")  //交换机
            )
    )

    //用于获取消息
    @RabbitHandler
    public void handleMessage(@Payload Map data , Channel channel ,
                              @Headers Map<String,Object> headers){

        System.out.println("=======获取到订单数据:" + data + "===========);");

        try {
            //对接支付宝、对接物流系统、日志登记。。。。
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Order order = new Order();
            order.setOrderNo(data.get("orderNo").toString());
            order.setOrderStatus(0);
            order.setUserid(data.get("userid").toString());
            order.setRecvName("xxx");
            order.setRecvMobile("1393310xxxx");
            order.setRecvAddress("xxxxxxxxxx");
            order.setAmout(19.8f);
            order.setPostage(0f);
            order.setCreateTime(new Date());
            orderDAO.insert(order);

            Long tag = (Long)headers.get(AmqpHeaders.DELIVERY_TAG);
            channel.basicAck(tag , false);  //消息确认

            System.out.println(data.get("orderNo") + "订单已创建");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
