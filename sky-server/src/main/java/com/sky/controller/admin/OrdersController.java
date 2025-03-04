package com.sky.controller.admin;

import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.mapper.OrdersMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrdersService;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.weaver.ast.Or;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api(tags = "订单相关接口")
@Slf4j
@RequestMapping("/admin/order")
@RestController("adminOrdersController")
public class OrdersController {

    @Autowired
    private OrdersService ordersService;

    /**
     * 订单搜索
     * @return
     */
    @ApiOperation("订单搜索")
    @GetMapping("/conditionSearch")
    public Result<PageResult> page(OrdersPageQueryDTO dto){
        log.info("订单搜索：{}",dto);
        PageResult p = ordersService.conditionSearch(dto);
        return Result.success(p);
    }

    /**
     * 查询订单详情
     * @return
     */
    @ApiOperation("查询订单详情")
    @GetMapping("/details/{id}")
    public Result<OrderVO> getDeatil(@PathVariable Long id){
        log.info("查询订单详情：{}",id);
        OrderVO orderDetail = ordersService.getOrderDetail(id);
        return Result.success(orderDetail);
    }

    /**
     * 订单状态统计
     * @return
     */
    @ApiOperation("订单状态统计")
    @GetMapping("/statistics")
    public Result<OrderStatisticsVO> statisticsStatus(){
        log.info("订单状态统计...");
        OrderStatisticsVO orderStatisticsVO = ordersService.statisticsStatus();
        return Result.success(orderStatisticsVO);
    }

    /**
     * 接单
     * @return
     */
    @ApiOperation("接单")
    @PutMapping("/confirm")
    public Result<String> confirmOrders(@RequestBody OrdersConfirmDTO dto){
        log.info("接单：{}",dto);
        ordersService.confirmOrders(dto);
        return Result.success();
    }

    /**
     * 拒单
     * @return
     */
    @ApiOperation("拒单")
    @PutMapping("/rejection")
    public Result<String> rejectionOrders(@RequestBody OrdersRejectionDTO ordersRejectionDTO){
        log.info("拒单：{}",ordersRejectionDTO);
        ordersService.rejectionOrders(ordersRejectionDTO);
        return Result.success();
    }

    /**
     * 取消订单
     * @return
     */
    @ApiOperation("取消订单")
    @PutMapping("/cancel")
    public Result<String> cancelOrders(@RequestBody OrdersCancelDTO ordersCancelDTO){
        log.info("取消订单：{}",ordersCancelDTO);
        ordersService.adminCancelOrders(ordersCancelDTO);
        return Result.success();
    }

    /**
     * 派送订单
     * @param id
     * @return
     */
    @ApiOperation("派送订单")
    @PutMapping("/delivery/{id}")
    public Result<String> deliveryOrders(@PathVariable Long id){
        log.info("派送订单：{}",id);
        ordersService.deliveryOrders(id);
        return Result.success();
    }

    /**
     * 完成订单
     * @return
     */
    @ApiOperation("完成订单")
    @PutMapping("/complete/{id}")
    public Result<String> completeOrders(@PathVariable Long id){
        log.info("完成订单：{}",id);
        ordersService.completeOrders(id);
        return Result.success();
    }

}
