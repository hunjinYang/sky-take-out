package com.sky.controller.user;


import com.sky.constant.StatusConstant;
import com.sky.entity.Dish;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@Api(tags = "C端-菜品相关接口")
@RequestMapping("/user/dish")
@RestController("userDishController")
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    @ApiOperation("根据分类id查询菜品")
    @GetMapping("/list")
    public Result<List<DishVO>> list(Long categoryId){
        log.info("根据分类id查询菜品：{}",categoryId);
        //构造Redis中的key
        String key = "dish_"+categoryId;
        //在查询MySQL数据库之前，先查询redis中是否有菜品数据
        List<DishVO> list = (List<DishVO>)redisTemplate.opsForValue().get(key);
        //redis中有菜品数据，直接返回
        if(list != null && list.size()>0){
            return Result.success(list);
        }
        //redis中没有菜品数据，查询MySQL数据库
        Dish dish = new Dish();
        dish.setCategoryId(categoryId);
        dish.setStatus(StatusConstant.ENABLE); //查询启售中的菜品

        //如果不存在，查询数据库，并将查询到的数据放入redis中
        list = dishService.listWithFlavor(dish);
        redisTemplate.opsForValue().set(key,list);
        return Result.success(list);
    }
}
