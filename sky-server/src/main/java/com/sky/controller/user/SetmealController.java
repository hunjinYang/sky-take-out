package com.sky.controller.user;

import com.sky.constant.StatusConstant;
import com.sky.entity.Setmeal;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api(tags = "C端-套餐相关接口")
@Slf4j
@RequestMapping("/user/setmeal")
@RestController("userSetmealController")
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    /**
     * 用户端根据分类和状态查询套餐
     * @param categoryId
     * @return
     */
    @ApiOperation("查询套餐")
    @GetMapping("/list")
    public Result<List<Setmeal>> list(Long categoryId){
        log.info("根据分类查询套餐：{}",categoryId);
        Setmeal setmeal = new Setmeal();
        setmeal.setCategoryId(categoryId);
        setmeal.setStatus(StatusConstant.ENABLE);
        List<Setmeal> setmeals = setmealService.list(setmeal);
        return Result.success(setmeals);
    }

    /**
     * 根据套餐id查询包含的菜品列表
     * @param id
     * @return
     */
    @ApiOperation("查询套餐包含菜品")
    @GetMapping("/dish/{id}")
    public Result<List<DishItemVO>>  dishList(@PathVariable  Long id){
        log.info("根据套餐id查询菜品：{}",id);
        List<DishItemVO> dishItemVOS = setmealService.getDishItemById(id);
        return Result.success(dishItemVOS);
    }



}
