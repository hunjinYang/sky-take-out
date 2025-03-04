package com.sky.task;

import com.sky.constant.MessageConstant;
import com.sky.entity.Orders;
import com.sky.mapper.OrdersMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 自定义定时任务类：定时处理订单状态
 */
@Slf4j
@Component
public class OrderTask {

    @Autowired
    private OrdersMapper ordersMapper;

    /**
     * 处理超时订单---处理超时15分钟未付款的订单
     */
    @Scheduled(cron = "0 * * * * ?")  //每分钟触发一次
    public void processTimeoutOrder(){
        log.info("定时处理超时订单：{}", LocalDateTime.now());
        //得到当前时间的前15分钟
        LocalDateTime time = LocalDateTime.now().plusMinutes(-15);
        //根据订单状态和下单时间查询超时订单
        //select * from orders where status = #{status} and order_time < #{orderTime}
        List<Orders> ordersList = ordersMapper.getByStatusAndOrderTimeLT(Orders.PENDING_PAYMENT, time);
        if(ordersList != null && ordersList.size()>0){
            for (Orders orders : ordersList) {
                orders.setStatus(Orders.CANCELLED);
                orders.setCancelReason(MessageConstant.ORDER_TIMEOUT_AUTOMATIC_CANCLE);
                orders.setCancelTime(LocalDateTime.now());
                ordersMapper.update(orders);
            }
        }
    }

    /**
     * 处理一直处于派送中状态的订单
     */
    @Scheduled(cron = "0 0 1 * * ?" )  //每天凌晨一点处理
    public void processDeliveryOrders(){
        log.info("定时处理处于派送中的订单：{}",LocalDateTime.now());
        LocalDateTime time = LocalDateTime.now().plusMinutes(-60);
        //处理处于派送中的订单，且时间为上一个工作日的
        List<Orders> ordersList = ordersMapper.getByStatusAndOrderTimeLT(Orders.DELIVERY_IN_PROGRESS, time);
        if(ordersList != null && ordersList.size()>0){
            for (Orders orders : ordersList) {
                orders.setStatus(Orders.COMPLETED);
                ordersMapper.update(orders);
            }
        }
    }

}
