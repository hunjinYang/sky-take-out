package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    
    @Autowired
    private DishMapper dishMapper;
    
    @Autowired
    private SetmealMapper setmealMapper;

    /**
     * 添加购物车
     * @param shoppingCartDTO
     */
    @Override
    public void addCart(ShoppingCartDTO shoppingCartDTO) {
        //创建shoppingCart对象
        ShoppingCart shoppingCart = new ShoppingCart();
        //拷贝属性值
        BeanUtils.copyProperties(shoppingCartDTO,shoppingCart);

        shoppingCart.setUserId(BaseContext.getCurrentId());
        //补充缺失的属性值
        //判断该商品是否存在于购物车
        ShoppingCart cart = shoppingCartMapper.listBy(shoppingCart);
        if(cart == null){  //代表购物车没有该商品数据
            //判断是新增套餐还是菜品
            if(shoppingCartDTO.getDishId() != null){  //代表新增菜品
                //根据菜品id得到菜品信息
                Dish dish = dishMapper.getById(shoppingCartDTO.getDishId());
                shoppingCart.setName(dish.getName());
                shoppingCart.setImage(dish.getImage());
                shoppingCart.setAmount(dish.getPrice());
            }else{ //代表新增套餐
                //根据套餐id得到套餐信息
                Setmeal setmeal = setmealMapper.getById(shoppingCartDTO.getSetmealId());
                shoppingCart.setName(setmeal.getName());
                shoppingCart.setImage(setmeal.getImage());
                shoppingCart.setAmount(setmeal.getPrice());
            }
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());

            //将商品数据存入shopping_cart表中
            shoppingCartMapper.insert(shoppingCart);
        }else{  //购物有该商品
            //将原来的购物车商品数量加一，更新数据
            cart.setNumber(cart.getNumber()+1);
            shoppingCartMapper.update(cart);
        }
    }

    /**
     * 查看购物车
     * @return
     */
    @Override
    public List<ShoppingCart> list() {
        //只能查看自己名下的购物车
        return shoppingCartMapper.listByUserId(BaseContext.getCurrentId());
    }

    /**
     * 清空购物车
     */
    @Override
    public void delete() {
        //只能删除自己名下的购物车
        shoppingCartMapper.deleteByUserId(BaseContext.getCurrentId());
    }

    /**
     * 删除购物车中的一个商品
     * @param shoppingCartDTO
     */
    @Override
    public void subCart(ShoppingCartDTO shoppingCartDTO) {
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO,shoppingCart);
        shoppingCart.setUserId(BaseContext.getCurrentId());

        ShoppingCart cart = shoppingCartMapper.listBy(shoppingCart);

        if(cart.getNumber() == 1){
            shoppingCartMapper.delete(cart);
        }else{
            cart.setNumber(cart.getNumber()-1);
            shoppingCartMapper.update(cart);
        }
    }
}
