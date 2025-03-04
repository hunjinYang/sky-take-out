package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@Api(tags = "菜品相关接口")
@Slf4j
@RequestMapping("/admin/dish")
@RestController("adminDishController")
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 新增菜品
     * @param dishDTO
     * @return
     */
    @ApiOperation("新增菜品")
    @PostMapping
    public Result<String> save(@RequestBody DishDTO dishDTO){
        log.info("新增菜品：{}",dishDTO);
        dishService.save(dishDTO);

        //缓存优化-清理缓存
        redisTemplate.delete("dish_"+dishDTO.getCategoryId());

        return Result.success();
    }

    /**
     * 分页查询
     * @param dishPageQueryDTO
     * @return
     */
    @ApiOperation("分页查询")
    @GetMapping("/page")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO){
        log.info("菜品分页查询：{}",dishPageQueryDTO);
        PageResult dishList = dishService.page(dishPageQueryDTO);
        return Result.success(dishList);
    }

    /**
     * 批量删除
     * @param ids
     * @return
     */
    @ApiOperation("删除菜品")
    @DeleteMapping
    public Result<String> delete(@RequestParam List<Long> ids){  //这里用集合接收必须使用@RequestParam
        log.info("批量删除菜品：{}",ids);
        dishService.delete(ids);

        //缓存优化-清理缓存-将菜品缓存全部删除
        Set keys = redisTemplate.keys("dish_*");
        redisTemplate.delete(keys);


        return Result.success();
    }

    /**
     * 根据id查询
     * @param id
     * @return
     */
    @ApiOperation("根据id查询")
    @GetMapping("/{id}")
    public Result<DishVO> getInfo(@PathVariable  Long id){
        log.info("根据id查询菜品：{}",id);
        DishVO dishVO = dishService.getInfo(id);
        return Result.success(dishVO);
    }


    /**
     * 修改菜品
     * @param dishDTO
     * @return
     */
    @ApiOperation("修改菜品")
    @PutMapping
    public Result<String> update(@RequestBody DishDTO dishDTO){
        log.info("修改菜品：{}",dishDTO);
        dishService.update(dishDTO);

        //缓存优化-清理缓存-将菜品缓存全部删除
        Set keys = redisTemplate.keys("dish_*");
        redisTemplate.delete(keys);

        return Result.success();
    }


    /**
     * 菜品起售停售
     * @param status
     * @return
     */
    @ApiOperation("菜品起售停售")
    @PostMapping("/status/{status}")
    public Result<String> setStatus(@PathVariable Integer status,Long id){
        log.info("菜品起售停售：{}",status);
        dishService.setStatus(id,status);

        //缓存优化-清理缓存-将菜品缓存全部删除
        Set keys = redisTemplate.keys("dish_*");
        redisTemplate.delete(keys);

        return Result.success();
    }

    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    @ApiOperation("分类id查询")
    @GetMapping("/list")
    public Result<List<Dish>>  list(Long categoryId){
        log.info("根据分类id查询菜品：{}");
        List<Dish> dishList = dishService.list(categoryId);
        return Result.success(dishList);
    }
}
