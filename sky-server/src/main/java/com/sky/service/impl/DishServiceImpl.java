package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

@Service
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    /**
     * 新增菜品
     * @param dishDTO
     */
    @Transactional  //开启事务（涉及到多张表的增删改需要开启事务）
    public void save(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        //插入菜品数据
        dishMapper.insert(dish);

        List<DishFlavor> flavors = dishDTO.getFlavors();
        flavors.forEach(flavor ->{
            flavor.setDishId(dish.getId());
        });
        //批量插入菜品口味数据
        dishFlavorMapper.insertBatch(flavors);

    }

    /**
     * 分页查询
     * @param dishPageQueryDTO
     * @return
     */
    @Override
    public PageResult page(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(),dishPageQueryDTO.getPageSize());

        List<DishVO> dishList = dishMapper.list(dishPageQueryDTO);

        Page<DishVO> p = (Page<DishVO>) dishList;
        return new PageResult(p.getTotal(),p.getResult());
    }

    /**
     * 批量删除
     * @param ids
     */
    @Transactional
    public void delete(List<Long> ids) {
        //1. 删除菜品之前，需要判断菜品是否启售，启售中不允许删除
        ids.forEach(id ->{
            Dish dish = dishMapper.getById(id);
            if(dish.getStatus() == StatusConstant.ENABLE){
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        });

        //2. 需要判断菜品是否被套餐关联，关联了也不允许删除
        Integer count = setmealDishMapper.countByDishId(ids);

        if(count > 0){
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }
        //删除菜品信息
        dishMapper.deleteByIds(ids);
        //删除口味信息
        dishFlavorMapper.deleteByIds(ids);
    }

    /**
     * 根据id查询
     * @param id
     * @return
     */
    @Override
    public DishVO getInfo(Long id) {
       Dish dish =  dishMapper.getById(id);
       List<DishFlavor> dishFlavors = dishFlavorMapper.getByDishId(id);

        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish,dishVO);
        dishVO.setFlavors(dishFlavors);

        return dishVO;
    }

    /**
     * 修改菜品
     * @param dishDTO
     */
    @Transactional
    public void update(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        dishMapper.updateById(dish);

        dishFlavorMapper.deleteByDishId(dish.getId());

        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(flavors != null && !flavors.isEmpty()){       //一定要进行判空
            //关联菜品id
            flavors.forEach(flavor ->{
                flavor.setDishId(dish.getId());
            });
            dishFlavorMapper.insertBatch(flavors);
        }

    }

    /**
     * 菜品起售停售
     * @param status
     */
    @Override
    public void setStatus(Long id,Integer status) {
        Dish dish = new Dish();
        dish.setId(id);
        dish.setStatus(status);
        dishMapper.updateById(dish);
    }

    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    @Override
    public List<Dish> list(Long categoryId) {
        return dishMapper.getByCategoryId(categoryId);
    }
}
