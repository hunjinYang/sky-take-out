package com.sky.controller.user;

import com.sky.entity.Category;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@Api(tags = "C端-分类相关接口")
@RequestMapping("/user/category")
@RestController("userCategoryController")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 查询分类
     * @param type
     * @return
     */
    @ApiOperation("分类查询")
    @GetMapping("/list")
    public Result<List<Category>> list(Integer type){
        log.info("查询菜品分类，类型为：{}",type);
        List<Category> categoryList = categoryService.listByType(type);
        return Result.success(categoryList);
    }
}
