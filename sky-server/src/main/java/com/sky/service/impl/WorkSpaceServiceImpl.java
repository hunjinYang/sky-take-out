package com.sky.service.impl;

import com.sky.constant.StatusConstant;
import com.sky.entity.Orders;
import com.sky.mapper.DishMapper;
import com.sky.mapper.OrdersMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.OrdersService;
import com.sky.service.WorkSpaceService;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;
import org.aspectj.weaver.ast.Or;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class WorkSpaceServiceImpl implements WorkSpaceService {

    @Autowired
    private OrdersMapper ordersMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;

    /**
     * 今日运营数据:
     *  营业额：当日已完成订单的总金额
     *  有效订单：当日已完成订单的数量
     *  订单完成率：有效订单数 / 总订单数
     *  平均客单价：营业额 / 有效订单数
     *  新增用户：当日新增用户的数量
     * @return
     */
    @Override
    public BusinessDataVO businessData() {
        LocalDateTime beginTime = LocalDateTime.now().with(LocalTime.MIN);
        LocalDateTime endTime  = LocalDateTime.now().with(LocalTime.MAX);
        Map map = new HashMap();
        map.put("status", Orders.COMPLETED);
        map.put("beginTime", beginTime);
        map.put("endTime",endTime);

        //营业额
        Double turnover = ordersMapper.sumByMap(map);
        turnover = turnover == null ? 0.0 : turnover;

        //有效订单
        Integer validOrderCount = ordersMapper.getByMap(map);


        map.put("status",null);
        Integer totalOrderCount = ordersMapper.getByMap(map);
        //订单完成率
        Double orderCompletionRate = 0.0;
        //平均客单率
        Double unitPrice = 0.0;
        if(totalOrderCount != 0 && validOrderCount != 0){
            //订单完成率
            orderCompletionRate = (validOrderCount+0.0) / totalOrderCount;
            //平均客单率
            unitPrice = (turnover+0.0)/validOrderCount;
        }

        //新增用户
        Integer newUsers = userMapper.countByMap(map);

        return BusinessDataVO.builder()
                .turnover(turnover)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .unitPrice(unitPrice)
                .newUsers(newUsers)
                .build();
    }

    /**
     * 订单管理数据
     * @return
     */
    @Override
    public OrderOverViewVO overviewOrders() {
        Map map = new HashMap();
        map.put("beginTime",LocalDateTime.now().with(LocalTime.MIN));
        map.put("status",null);
        //全部订单
        Integer allOrders = ordersMapper.getByMap(map);

        //待接单
        map.put("status",Orders.TO_BE_CONFIRMED);
        Integer waitingOrders =  ordersMapper.getByMap(map);
        //待派送
        map.put("status",Orders.CONFIRMED);
        Integer deliveredOrders = ordersMapper.getByMap(map);
        //已完成
        map.put("status", Orders.COMPLETED);
        Integer completedOrders = ordersMapper.getByMap(map);
        //已取消
        map.put("status",Orders.CANCELLED);
        Integer cancelledOrders = ordersMapper.getByMap(map);
        return OrderOverViewVO.builder()
                .allOrders(allOrders)
                .waitingOrders(waitingOrders)
                .deliveredOrders(deliveredOrders)
                .completedOrders(completedOrders)
                .cancelledOrders(cancelledOrders)
                .build();
    }

    /**
     * 菜品总览
     * @return
     */
    @Override
    public DishOverViewVO overviewDishes() {
        Map map = new HashMap();
        //启售的菜品
        map.put("status", StatusConstant.ENABLE);
        Integer sold = dishMapper.countByMap(map);
        //停售的菜品
        map.put("status", StatusConstant.DISABLE);
        Integer discontinued = dishMapper.countByMap(map);

        return DishOverViewVO.builder()
                .sold(sold)
                .discontinued(discontinued)
                .build();
    }

    /**
     * 套餐总览
     * @return
     */
    @Override
    public SetmealOverViewVO overviewSetmeals() {
        Map map = new HashMap();
        //售卖套餐数量
        map.put("status",StatusConstant.ENABLE);
        Integer sold = setmealMapper.countByMap(map);
        //停售套餐数量
        map.put("status",StatusConstant.DISABLE);
        Integer discontinued = setmealMapper.countByMap(map);
        return SetmealOverViewVO.builder()
                .sold(sold)
                .discontinued(discontinued)
                .build();
    }
}
