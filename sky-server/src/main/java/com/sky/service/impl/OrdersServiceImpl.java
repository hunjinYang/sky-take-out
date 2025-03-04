package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.OrderBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrdersService;
import com.sky.utils.HttpClientUtil;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import com.sky.websocket.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.weaver.ast.Or;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
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

    @Autowired
    private WebSocketServer webSocketServer;

    @Value("${sky.shop.address}")
    private String shopAddress;  //店铺地址
    @Value("${sky.shop.ak}")
    private String ak;  //百度地图应用服务ak
    @Value("${sky.shop.distance}")
    private Integer distance;  //配送距离上限

    //地理编码接口，获取地址的经纬度坐标
    private static final String GEO_CODING_URL = "https://api.map.baidu.com/geocoding/v3/";
    //路线规划接口，骑行路线规划，（可以视业务情况选择骑行、公交、驾车等路线，接口不一样，可以配置在yml中）
    private static final String DIRECTION_URL = "https://api.map.baidu.com/directionlite/v1/riding";

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

        //优化下单逻辑：需要检验配送范围（如果超过5公里，就抛出自定义异常）
        if (!checkOutOfRange(addressBook.getProvinceName()+addressBook.getCityName()+addressBook.getDistrictName()+addressBook.getDetail())) {
            throw new OrderBusinessException(MessageConstant.OUT_OF_RANGE);
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
     * 校验配送范围
     */
    private boolean checkOutOfRange(String address) {
        //1.获取店铺的地址坐标
        //https://api.map.baidu.com/geocoding/v3/?address=北京市海淀区上地十街10号&output=json&ak=您的ak
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("address", this.shopAddress);
        paramMap.put("output", "json");
        paramMap.put("ak", this.ak);
        String jsonStr = HttpClientUtil.doGet(GEO_CODING_URL, paramMap);
        JSONObject jsonObject = JSON.parseObject(jsonStr);
        if (!jsonObject.get("status").equals(0)) { //地址解析失败
            throw new OrderBusinessException(MessageConstant.ADDRESS_PARSE_FAILED);
        }
        JSONObject jsonObject1 = jsonObject.getJSONObject("result").getJSONObject("location");
        Object lng = jsonObject1.get("lng");   //经度
        Object lat = jsonObject1.get("lat");   //维度
        String origin = lat + "," + lng;
        log.info("店铺的坐标：{}", origin);

        //2.获取收货地址的坐标
        paramMap.put("address", address);
        jsonStr = HttpClientUtil.doGet(GEO_CODING_URL, paramMap);
        jsonObject = JSON.parseObject(jsonStr);
        if (!jsonObject.get("status").equals(0)) { //地址解析失败
            throw new OrderBusinessException(MessageConstant.ADDRESS_PARSE_FAILED);
        }
        jsonObject1 = jsonObject.getJSONObject("result").getJSONObject("location");
        lng = jsonObject1.get("lng");   //经度
        lat = jsonObject1.get("lat");   //维度
        String destination = lat + "," + lng;
        log.info("收货地址的坐标：{}", destination);

        //3.根据店铺和收货地址坐标，进行距离计算，如果超出设定的this.distance公里，就返回false
        //https://api.map.baidu.com/directionlite/v1/riding?origin=40.01116,116.339303&destination=39.936404,116.452562&ak=您的AK
        paramMap.put("origin", origin);
        paramMap.put("destination", destination);
        jsonStr = HttpClientUtil.doGet(DIRECTION_URL, paramMap);
        jsonObject = JSON.parseObject(jsonStr);
        if (!jsonObject.get("status").equals(0)) { //地址解析失败
            throw new OrderBusinessException(MessageConstant.ADDRESS_PARSE_FAILED);
        }
        JSONArray routes = jsonObject.getJSONObject("result").getJSONArray("routes");
        Integer distance = (Integer) routes.getJSONObject(0).get("distance");
        log.info("配送距离：{}米", distance);

        //Integer settingDistance = Integer.parseInt(this.distance);

        return distance <= this.distance;
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

        //通过websocket想客户端浏览器推送消息 type orderId content
        Map map = new HashMap();
        map.put("type",1);  //1表示来单提醒，2表示客户催单
        map.put("orderId",ordersDB.getId());
        map.put("content","订单号："+outTradeNo);
        String json = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(json);


    }

    /**
     * 订单分页查询
     * @param dto
     * @return
     */
    @Override
    public PageResult historyOrders(OrdersPageQueryDTO dto) {
        PageHelper.startPage(dto.getPage(),dto.getPageSize());
        dto.setUserId(BaseContext.getCurrentId());

        Page<Orders> p = (Page<Orders>)ordersMapper.pageQuery(dto);

        List<OrderVO> list = new ArrayList<>();

        if(p!= null && p.getTotal()>0){
            for(Orders orders:p){
                Long id = orders.getId();

                List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(id);
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders,orderVO);
                orderVO.setOrderDetailList(orderDetails);
                list.add(orderVO);
            }
        }
        return new PageResult(p.getTotal(),list);
    }

    /**
     * 查询订单
     * @param id
     * @return
     */
    @Override
    public OrderVO getOrderDetail(Long id) {
        Orders orders = ordersMapper.getById(id);

        List<OrderDetail> orderDetail = orderDetailMapper.getByOrderId(id);

        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders,orderVO);
        orderVO.setOrderDetailList(orderDetail);

        return orderVO;
    }

    /**
     * 取消订单
     * @param id
     */
    @Override
    public void cancelOrders(Long id) {
        //根据id查询订单
        Orders ordersTemp = ordersMapper.getById(id);
        // 校验订单是否存在
        if(ordersTemp == null){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        // 订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
        // 用户端取消订单，只能取消还未接单的订单，其他情况取消订单，需要联系商家在后台取消
        if(ordersTemp.getStatus()>2){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Orders orders = new Orders();
        orders.setId(id);

        // 需要判断订单支付状态，订单处于待接单状态下取消，需要进行退款
        if(ordersTemp.getPayStatus().equals(Orders.PAID)){
            //调用微信支付退款接口 --由于没有可用的商户id，下面的退款操作执行会失败，直接注释即可，假设退款成功了，直接修改状态
            // weChatPayUtil.refund(
            //         ordersDB.getNumber(), //商户订单号
            //         ordersDB.getNumber(), //商户退款单号
            //         new BigDecimal(0.01),//退款金额，单位 元
            //         new BigDecimal(0.01));//原订单金额

            //支付状态修改为 退款
            orders.setPayStatus(Orders.REFUND);
        }
        //更新订单状态、取消原因、取消时间
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason("用户取消");
        orders.setCancelTime(LocalDateTime.now());
        ordersMapper.update(orders);
    }

    /**
     * 再来一单
     * @param id
     */
    @Transactional
    public void repetitionOrdersById(Long id) {
        //查询当前用户id
        Long userId = BaseContext.getCurrentId();
        //根据订单查询当前订单详情
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(id);
        //将订单详情转换为购物车对象
        List<ShoppingCart> shoppingCarts = orderDetailList.stream().map(x ->{
            ShoppingCart shoppingCart = new ShoppingCart();

            //将原订单详情里面的菜品信息重新复制到购物车对象中
            BeanUtils.copyProperties(x,shoppingCart,"id");
            shoppingCart.setUserId(userId);
            shoppingCart.setCreateTime(LocalDateTime.now());
            return shoppingCart;
        }).collect(Collectors.toList());

        shoppingCartMapper.insertBatch(shoppingCarts);
    }

    /**
     * 催单
     * @param id
     */
    @Override
    public void reminderById(Long id) {
        Orders orders = ordersMapper.getById(id);
        if(orders == null){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        //通过websocket想客户端浏览器推送消息 type orderId content
        Map map = new HashMap();
        map.put("type",2);  //1表示来单提醒，2表示客户催单
        map.put("orderId",orders.getId());
        map.put("content","订单号："+orders.getNumber());
        String json = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(json);
    }

    /**
     * 订单搜索
     * @param dto
     * @return
     */
    @Override
    public PageResult conditionSearch(OrdersPageQueryDTO dto) {
        PageHelper.startPage(dto.getPage(),dto.getPageSize());
        Page<Orders> page = (Page<Orders>)ordersMapper.pageQuery(dto);

        //部分订单状态需要额外返回订单菜品信息
        List<OrderVO> orderVOList = new ArrayList<>();
        List<Orders> ordersList = page.getResult();
        if(!CollectionUtils.isEmpty(ordersList)){
            for(Orders orders:ordersList){
                //将共同字段复制到OrderVO
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders,orderVO);
                //根据订单id获取菜品信息字符串
                List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orders.getId());
                List<String> orderDishList = orderDetailList.stream().map(x -> {
                    String orderDish = x.getName() + "*" + x.getNumber() + ";";
                    return orderDish;
                }).collect(Collectors.toList());
                String orderDishes = String.join("",orderDishList);
                orderVO.setOrderDishes(orderDishes);
                orderVOList.add(orderVO);
            }
        }

        return new PageResult(page.getTotal(),orderVOList);
    }

    /**
     * 订单状态统计
     * @return
     */
    @Override
    public OrderStatisticsVO statisticsStatus() {
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        //待接单 TO_BE_CONFIRMED = 2
        orderStatisticsVO.setToBeConfirmed(ordersMapper.statisticsStatus(Orders.TO_BE_CONFIRMED));
        //已接单  CONFIRMED = 3
        orderStatisticsVO.setConfirmed(ordersMapper.statisticsStatus(Orders.CONFIRMED));
        //派送中 DELIVERY_IN_PROGRESS=4
        orderStatisticsVO.setDeliveryInProgress(ordersMapper.statisticsStatus(Orders.DELIVERY_IN_PROGRESS));
        return orderStatisticsVO;
    }

    /**
     * 接单
     * @param dto
     */
    @Override
    public void confirmOrders(OrdersConfirmDTO dto) {
        Orders orders = new Orders();
        orders.setId(dto.getId());
        //订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
        orders.setStatus(Orders.CONFIRMED);
        ordersMapper.update(orders);
    }

    /**
     * 拒单
     * @param ordersRejectionDTO
     */
    @Override
    public void rejectionOrders(OrdersRejectionDTO ordersRejectionDTO) {
        //根据id查询订单
        Orders ordersDB = ordersMapper.getById(ordersRejectionDTO.getId());

        //订单只有存在且状态为2才可以拒单
        if(ordersDB == null || !ordersDB.getStatus().equals(Orders.TO_BE_CONFIRMED)){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        //需要判断订单支付状态，订单处于待接单状态下取消，需要进行退款
        if(ordersDB.getPayStatus().equals(Orders.PAID)){
            //用户已支付，需要退款
            // String refund = weChatPayUtil.refund(
            //         ordersDB.getNumber(),
            //         ordersDB.getNumber(),
            //         new BigDecimal("0.01"),
            //         new BigDecimal("0.01"));
            // log.info("申请退款：{}", refund);
            log.info("申请退款,修改支付状态");
            ordersDB.setPayStatus(Orders.REFUND);
        }


        //订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
        ordersDB.setStatus(Orders.CANCELLED);
        ordersDB.setRejectionReason(ordersRejectionDTO.getRejectionReason());
        ordersDB.setCancelTime(LocalDateTime.now());
        ordersMapper.update(ordersDB);
    }

    /**
     * 管理端取消订单
     * @param ordersCancelDTO
     */
    @Override
    public void adminCancelOrders(OrdersCancelDTO ordersCancelDTO) {
        Orders ordersDB = ordersMapper.getById(ordersCancelDTO.getId());
        //没有该订单
        if(ordersDB == null){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        //只需判断支付状态，支付状态如果为已支付，则需要退款并修改支付状态
        if (ordersDB.getPayStatus().equals(Orders.PAID)) {
            //用户已支付，需要退款---商户id不存在无法回调，所以
            // String refund = weChatPayUtil.refund(
            //         ordersDB.getNumber(),
            //         ordersDB.getNumber(),
            //         new BigDecimal("0.01"),
            //         new BigDecimal("0.01"));
            // log.info("申请退款：{}", refund);
            // 支付状态修改为退款
            log.info("支付状态修改为退款");
            ordersDB.setPayStatus(Orders.REFUND);
        }
        //管理端取消订单需要退款，根据订单id更新订单状态，取消原因和取消时间
        ordersDB.setStatus(Orders.CANCELLED);
        ordersDB.setCancelReason(ordersCancelDTO.getCancelReason());
        ordersDB.setCancelTime(LocalDateTime.now());
        ordersMapper.update(ordersDB);
    }

    @Override
    public void deliveryOrders(Long id) {
        Orders ordersDB = ordersMapper.getById(id);
        if(ordersDB == null){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        if(!ordersDB.getStatus().equals(Orders.CONFIRMED)){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        //订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
        Orders orders = new Orders();
        orders.setId(ordersDB.getId());
        //更新订单状态，转为派送中
        orders.setStatus(Orders.DELIVERY_IN_PROGRESS);
        ordersMapper.update(orders);
    }

    /**
     * 完成订单
     * @param id
     */
    @Override
    public void completeOrders(Long id) {
        Orders ordersDB = ordersMapper.getById(id);
        if(ordersDB == null){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        if(!ordersDB.getStatus().equals(Orders.DELIVERY_IN_PROGRESS)){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Orders orders = new Orders();
        orders.setId(ordersDB.getId());
        //设置订单状态为完成
        orders.setStatus(Orders.COMPLETED);
        ordersMapper.update(orders);
    }


}
