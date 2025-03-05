package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Employee;
import com.sky.entity.Orders;
import com.sky.mapper.OrdersMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.OrdersService;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrdersMapper ordersMapper;
    @Autowired
    private UserMapper userMapper;

    /**
     * 提取的公共代码，根据begin和end得到时间列表
     * @param begin
     * @param end
     * @return
     */
    private static List<LocalDate> getLocalDates(LocalDate begin, LocalDate end) {
        //准备日期列表数据 dateList
        List<LocalDate> dateList = new ArrayList<LocalDate>();
        //循环插入日期数据
        while (!begin.isAfter(end)) {
            dateList.add(begin);
            begin = begin.plusDays(1);
        }
        return dateList;
    }


    /**
     * 营业额统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    public TurnoverReportVO turnoverStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = getLocalDates(begin, end);

        List<Double> turnoverList = new ArrayList<Double>();
        dateList.forEach(date ->{
            //准备营业额列表数据
            //查询orders表：条件：状态和日期
            Map map = new HashMap();
            map.put("status",Orders.COMPLETED);
            map.put("beginTime",LocalDateTime.of(date, LocalTime.MIN));
            map.put("endTime",LocalDateTime.of(date, LocalTime.MAX));
            Double turnover = ordersMapper.sumByMap(map);
            turnover = turnover == null ? 0.0 : turnover;
            turnoverList.add(turnover);
        });
        //构造TurnoverReportVO对象
        return TurnoverReportVO.builder()
                .dateList(StringUtils.join(dateList,","))
                .turnoverList(StringUtils.join(turnoverList,","))
                .build();
    }


    /**
     * 用户统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    public UserReportVO userStatistics(LocalDate begin, LocalDate end) {
        //准备日期列表数据 dateList
        List<LocalDate> dateList = getLocalDates(begin, end);
        //准备新用户数据
        List<Integer> newUserList = new ArrayList<>();
        //准备旧用户数据
        List<Integer> totalUserList = new ArrayList<>();

        dateList.forEach(date->{
            //构造newUserList
            Map map = new HashMap();
            map.put("beginTime",LocalDateTime.of(date, LocalTime.MIN));
            map.put("endTime",LocalDateTime.of(date, LocalTime.MAX));
            //select count(id) from user where create_time <= endTime and create_time >= beginTime
            Integer newUser = userMapper.countByMap(map);
            newUserList.add(newUser);

            //构造totalUserList
            map.put("beginTime",null);
            Integer totalUser = userMapper.countByMap(map);
            totalUserList.add(totalUser);
        });
        //构造UserReportVO
        return UserReportVO.builder()
                .dateList(StringUtils.join(dateList,","))
                .newUserList(StringUtils.join(newUserList,","))
                .totalUserList(StringUtils.join(totalUserList,","))
                .build();
    }

    /**
     * 订单统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    public OrderReportVO ordersStatistics(LocalDate begin, LocalDate end) {
        //准备日期列表数据 dateList
        List<LocalDate> dateList = getLocalDates(begin, end);

        //每日总订单
        List<Integer> orderCountList = new ArrayList<>();
        //每日有效订单数
        List<Integer> validOrderCountList = new ArrayList<>();
        //获取订单总数
        Integer totalOrderCount = 0;
        //有效订单总数
        Integer validOrderCount = 0;

        for (LocalDate date : dateList) {
            Map map = new HashMap();
            map.put("beginTime",LocalDateTime.of(date, LocalTime.MIN));
            map.put("endTime",LocalDateTime.of(date, LocalTime.MAX));
            //统计每日订单数量，条件：下单时间 >= 当天起始时间 and 下单时间 <= 当天结束时间
            Integer orderCount = ordersMapper.getByMap(map);
            orderCountList.add(orderCount);

            //统计每日有效订单数量，条件：下单时间 >= 当天起始时间 and 下单时间 <= 当天结束时间 and 状态=已完成
            map.put("status",Orders.COMPLETED);
            Integer validCount = ordersMapper.getByMap(map);
            validOrderCountList.add(validCount);

            //计算订单总数
            totalOrderCount += orderCount;
            //计算有效订单总数
            validOrderCount += validCount;
        }
        Double orderCompletionRate = 0.0;
        if(totalOrderCount != 0){
            orderCompletionRate = (validOrderCount+0.0) / totalOrderCount;
        }

        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList,","))
                .orderCountList(StringUtils.join(orderCountList,","))
                .validOrderCountList(StringUtils.join(validOrderCountList,","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    /**
     * 统计top10商品
     * @param begin
     * @param end
     * @return
     */
    @Override
    public SalesTop10ReportVO salesTop10(LocalDate begin, LocalDate end) {
        //构造nameList,商品名称列表
        List<String> nameList = new ArrayList<>();
        //构造numberList，商品销量（份数）
        List<Integer> numberList = new ArrayList<>();

        Map map = new HashMap();
        map.put("beginTime",LocalDateTime.of(begin,LocalTime.MIN));
        map.put("endTime",LocalDateTime.of(end,LocalTime.MAX));
        map.put("status",Orders.COMPLETED);    //订单状态：已完成
        List<GoodsSalesDTO> list = ordersMapper.sumTop10(map);

//        for (Map m : list) {
//            String name = (String)m.get("name");
//            nameList.add(name);
//
//            Object sumNum =   m.get("sumNum");
//            numberList.add(sumNum);
//        }

        for (GoodsSalesDTO dto : list) {
            nameList.add(dto.getName());
            numberList.add(dto.getNumber());
        }
        return SalesTop10ReportVO.builder()
                .nameList(StringUtils.join(nameList,","))
                .numberList(StringUtils.join(numberList,","))
                .build();
    }
}
