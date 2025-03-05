package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.WorkSpaceService;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Api(tags= "工作台相关接口")
@Slf4j
@RequestMapping("/admin/workspace")
@RestController
public class WorkSpaceController {

    @Autowired
    private WorkSpaceService workSpaceService;

    /**
     * 今日运营数据
     * @return
     */
    @ApiOperation("今日运营数据")
    @GetMapping("/businessData")
    public Result<BusinessDataVO> businessData(){
        log.info("今日运营数据");
        LocalDateTime beginTime = LocalDateTime.now().with(LocalTime.MIN);
        LocalDateTime endTime  = LocalDateTime.now().with(LocalTime.MAX);
        BusinessDataVO businessDataVO = workSpaceService.businessData(beginTime,endTime);
        return Result.success(businessDataVO);
    }

    /**
     * 订单管理数据
     * @return
     */
    @ApiOperation("订单管理数据")
    @GetMapping("/overviewOrders")
    public Result<OrderOverViewVO>  overviewOrders(){
        log.info("订单管理数据");
        OrderOverViewVO orderOverViewVO = workSpaceService.overviewOrders();
        return Result.success(orderOverViewVO);
    }

    /**
     * 菜品总览
     * @return
     */
    @ApiOperation("菜品总览")
    @GetMapping("/overviewDishes")
    public Result<DishOverViewVO> overviewDishes(){
        log.info("菜品总览");
        DishOverViewVO dishOverViewVO = workSpaceService.overviewDishes();
        return Result.success(dishOverViewVO);
    }

    /**
     * 套餐总览
     * @return
     */
    @ApiOperation("套餐总览")
    @GetMapping("/overviewSetmeals")
    public Result<SetmealOverViewVO> overviewSetmeals(){
        log.info("套餐总览");
        SetmealOverViewVO setmealOverViewVO = workSpaceService.overviewSetmeals();
        return Result.success(setmealOverViewVO);
    }
}
