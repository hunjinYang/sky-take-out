package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.*;
import com.sky.exception.OrderBusinessException;
import com.sky.mapper.*;
import com.sky.service.OrdersService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.PrimitiveIterator;

@Service
public class OrdersServiceImpl implements OrdersService {

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private AddressBookMapper addressBookMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private WeChatPayUtil weChatPayUtil;

    /**
     * 用户下单
     * @param dto
     * @return
     */
    @Transactional
    public OrderSubmitVO submit(OrdersSubmitDTO dto) {
        //构造订单数据，存入orders表中
        Orders orders = new Orders();
        BeanUtils.copyProperties(dto,orders);

        //查询地址表，获取收货人信息
        AddressBook addressBook = addressBookMapper.getById(dto.getAddressBookId());
        if(addressBook == null){
            throw new OrderBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }

        //查询用户表，获取用户信息
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);
        if(user == null){
            throw new OrderBusinessException(MessageConstant.USER_NOT_LOGIN);
        }

        //补充缺失的属性值
        orders.setNumber(System.currentTimeMillis()+""); //订单编号
        orders.setStatus(Orders.PENDING_PAYMENT); //订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
        orders.setUserId(BaseContext.getCurrentId());
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);  //支付状态 0未支付 1已支付 2退款
        orders.setPhone(addressBook.getPhone());
        orders.setAddress(addressBook.getDetail());
        orders.setConsignee(addressBook.getConsignee()); //收货人
        orders.setUserName(user.getName());  //下单人

        ordersMapper.insert(orders);


        //查询购物车列表数据
        List<ShoppingCart> shoppingCarts = shoppingCartMapper.listByUserId(userId);
        if(shoppingCarts == null || shoppingCarts.size() == 0){
            throw new OrderBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        //构造订单明细数据，存入order_detail表中
        List<OrderDetail> orderDetails = new ArrayList<>();
        //循环遍历购物车明细数据，构造订单明细
        shoppingCarts.forEach(cart ->{
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart,orderDetail,"id");
            //关联订单id
            orderDetail.setOrderId(orders.getId());
            orderDetails.add(orderDetail);
        });

        orderDetailMapper.insertBatch(orderDetails);
        //清空购物车
        shoppingCartMapper.deleteByUserId(userId);
        //构造OrderSubmitVO对象并返回
        return OrderSubmitVO.builder()
                .id(orders.getId())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .orderTime(orders.getOrderTime())
                .build();
    }


    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
//        Long userId = BaseContext.getCurrentId();
//        User user = userMapper.getById(userId);
//
//        //调用微信支付接口，生成预支付交易单
//        JSONObject jsonObject = weChatPayUtil.pay(
//                ordersPaymentDTO.getOrderNumber(), //商户订单号
//                new BigDecimal(0.01), //支付金额，单位 元
//                "苍穹外卖订单", //商品描述
//                user.getOpenid() //微信用户的openid
//        );
//
//        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
//            throw new OrderBusinessException("该订单已支付");
//        }
//
//        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
//        vo.setPackageStr(jsonObject.getString("package"));
//
//        return vo;

        //模拟支付成功---修改订单状态
        paySuccess(ordersPaymentDTO.getOrderNumber());
        //返回一个空对象就可以了
        return new OrderPaymentVO();
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();

        // 根据订单号查询当前用户的订单
        Orders ordersDB = ordersMapper.getByNumberAndUserId(outTradeNo, userId);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        ordersMapper.update(orders);
    }
}
