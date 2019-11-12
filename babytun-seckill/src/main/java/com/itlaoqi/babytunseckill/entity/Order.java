package com.itlaoqi.babytunseckill.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
* @Description: 订单
* @Author: wyb
* @Date: 2019-11-12 17:12:47
*/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Order {

    private Long orderId;
    private String orderNo;
    private Integer orderStatus;
    private String userid;
    private String recvName;
    private String recvAddress;
    private String recvMobile;
    private Float postage;    //邮费
    private Float amout;
    private Date createTime;

}
