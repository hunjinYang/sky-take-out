package com.sky.service;

import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;

import java.time.LocalDateTime;

public interface WorkSpaceService {

    /**
     * 今日运营数据
     * @return
     */
    BusinessDataVO businessData(LocalDateTime beginTime, LocalDateTime endTime);

    /**
     * 订单管理数据
     * @return
     */
    OrderOverViewVO overviewOrders();

    /**
     * 菜品总览
     * @return
     */
    DishOverViewVO overviewDishes();

    /**
     * 套餐总览
     * @return
     */
    SetmealOverViewVO overviewSetmeals();
}
