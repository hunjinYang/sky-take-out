package com.sky.controller.user;

import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrdersService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api(tags = "C端-用户订单")
@Slf4j
@RequestMapping("/user/order")
@RestController("userOrdersController")
public class OrdersController {

    @Autowired
    private OrdersService ordersService;

    /**
     * 用户下单
     * @param dto
     * @return
     */
    @ApiOperation("用户下单")
    @PostMapping("/submit")
    public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO dto){
        log.info("用户下单：{}",dto);
        OrderSubmitVO vo = ordersService.submit(dto);
        return Result.success(vo);
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    @PutMapping("/payment")
    @ApiOperation("订单支付")
    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        log.info("订单支付：{}", ordersPaymentDTO);
        OrderPaymentVO orderPaymentVO = ordersService.payment(ordersPaymentDTO);
        log.info("生成预支付交易单：{}", orderPaymentVO);
        return Result.success(orderPaymentVO);
    }

    /**
     * 历史订单查询
     * @param ordersPageQueryDTO
     * @return
     */
    @ApiOperation("历史订单查询")
    @GetMapping("/historyOrders")
    public Result<PageResult> historyOrders(OrdersPageQueryDTO ordersPageQueryDTO){
        log.info("历史订单查询：{}",ordersPageQueryDTO);
        PageResult p = ordersService.historyOrders(ordersPageQueryDTO);
        return Result.success(p);
    }

    /**
     * 查询订单详情
     * @param id
     * @return
     */
    @ApiOperation("查询订单详情")
    @GetMapping("/orderDetail/{id}")
    public Result<OrderVO> getOrderDetail(@PathVariable Long id){
        log.info("查询订单详情：{}",id);
        OrderVO orderVO = ordersService.getOrderDetail(id);
        return Result.success(orderVO);
    }

    /**
     * 取消订单
     * @param id
     * @return
     */
    @ApiOperation("取消订单")
    @PutMapping("/cancel/{id}")
    public Result<String> cancelOrders(@PathVariable Long id){
        log.info("取消订单：{}",id);
        ordersService.cancelOrders(id);
        return Result.success();
    }

    /**
     * 再来一单
     * @return
     */
    @ApiOperation("再来一单")
    @PostMapping("/repetition/{id}")
    public Result<String> repetitionOrders(@PathVariable Long id){
        log.info("再来一单：{}",id);
        ordersService.repetitionOrdersById(id);
        return Result.success();
    }

    /**
     * 催单
     * @param id
     * @return
     */
    @ApiOperation("催单")
    @GetMapping("/reminder/{id}")
    public Result<String> reminder(@PathVariable Long id){
        log.info("催单：{}",id);
        ordersService.reminderById(id);
        return Result.success();
    }


}
