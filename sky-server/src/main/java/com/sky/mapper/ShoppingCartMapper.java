package com.sky.mapper;

import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {
    /**
     * 根据dishId、setmealId、dishFlavor、userId查询
     * @param shoppingCart
     * @return
     */
    ShoppingCart listBy(ShoppingCart shoppingCart);

    /**
     * 添加购物车
     * @param shoppingCart
     */
    void insert(ShoppingCart shoppingCart);

    /**
     * 更新购物车
     * @param cart
     */
    void update(ShoppingCart cart);

    /**
     * 查看购物车
     * @param currentId
     * @return
     */
    List<ShoppingCart> listByUserId(Long currentId);

    /**
     * 清空购物车
     * @param currentId
     */
    void deleteByUserId(Long currentId);


    /**
     * 删除购物车一个商品
     * @param cart
     */
    void delete(ShoppingCart cart);

    /**
     * 批量添加购物车
     * @param shoppingCarts
     */
    void insertBatch(List<ShoppingCart> shoppingCarts);
}
