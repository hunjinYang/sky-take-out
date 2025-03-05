package com.sky.service;

import com.sky.dto.*;
import com.sky.result.PageResult;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;

import java.util.Map;

public interface OrdersService {

    /**
     * 用户下单
     * @param dto
     * @return
     */
    OrderSubmitVO submit(OrdersSubmitDTO dto);

    /**
     * 订单支付
     * @param ordersPaymentDTO
     * @return
     */
    OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception;

    /**
     * 支付成功，修改订单状态
     * @param outTradeNo
     */
    void paySuccess(String outTradeNo);

    /**
     * 历史订单查询
     * @param ordersPageQueryDTO
     * @return
     */
    PageResult historyOrders(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 查询订单详情
     * @return
     */
    OrderVO getOrderDetail(Long id);

    /**
     * 取消订单
     * @param id
     */
    void cancelOrders(Long id);

    /**
     * 再来一单
     * @param id
     */
    void repetitionOrdersById(Long id);

    /**
     * 催单
     * @param id
     */
    void reminderById(Long id);

    /**
     * 订单搜索
     * @param dto
     * @return
     */
    PageResult conditionSearch(OrdersPageQueryDTO dto);

    /**
     * 订单状态统计
     * @return
     */
    OrderStatisticsVO statisticsStatus();

    /**
     * 接单
     * @param dto
     */
    void confirmOrders(OrdersConfirmDTO dto);

    /**
     * 拒单
     * @param ordersRejectionDTO
     */
    void rejectionOrders(OrdersRejectionDTO ordersRejectionDTO);

    /**
     * 管理端取消订单
     * @param ordersCancelDTO
     */
    void adminCancelOrders(OrdersCancelDTO ordersCancelDTO);

    /**
     * 派送订单
     * @param id
     */
    void deliveryOrders(Long id);

    /**
     * 完成订单
     * @param id
     */
    void completeOrders(Long id);

}
