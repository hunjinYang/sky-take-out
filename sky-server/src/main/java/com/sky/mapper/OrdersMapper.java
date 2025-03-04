package com.sky.mapper;

import com.sky.dto.GoodsSalesDTO;
import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrdersMapper {
    /**
     * 添加订单
     * @param orders
     */
    void insert(Orders orders);

    /**
     * 根据订单号和用户id查询订单
     * @param orderNumber
     * @param userId
     */
    @Select("select * from orders where number = #{orderNumber} and user_id= #{userId}")
    Orders getByNumberAndUserId(String orderNumber, Long userId);

    /**
     * 修改订单信息
     * @param orders
     */
    void update(Orders orders);

    /**
     * 条件分页查询
     * @param dto
     * @return
     */
    List<Orders> pageQuery(OrdersPageQueryDTO dto);

    /**
     * 查询订单
     * @param id
     * @return
     */
    Orders getById(Long id);

    /**
     * 订单状态统计
     * @param status
     * @return
     */
    @Select("select count(id) from orders where status = #{status}")
    Integer statisticsStatus(Integer status);

    /**
     * 根据订单状态和下单时间查询超时订单
     * @return
     */
    @Select("select * from orders where status = #{status} and order_time < #{orderTime}")
    List<Orders> getByStatusAndOrderTimeLT(Integer status, LocalDateTime orderTime);


    /**
     * 统计营业额
     * @param map
     * @return
     */
    @Select("select sum(amount) from orders where status=#{status} and order_time between #{beginTime} and #{endTime}")
    Double sumByMap(Map map);

    /**
     * 统计订单数量
     * @param map
     * @return
     */
    Integer getByMap(Map map);

    /**
     * 统计销量前10
     * @param map
     * @return
     */
    //@MapKey("name")
    List<GoodsSalesDTO> sumTop10(Map map);
}
