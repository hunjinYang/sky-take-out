package com.sky.controller.user;

import com.sky.entity.AddressBook;
import com.sky.result.Result;
import com.sky.service.AddressBookService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "C端-地址簿相关接口")
@Slf4j
@RequestMapping("/user/addressBook")
@RestController
public class AddressBookController {

    @Autowired
    private AddressBookService addressBookService;

    /**
     * 新增地址
     * @param addressBook
     * @return
     */
    @ApiOperation("新增地址")
    @PostMapping
    public Result<String> save(@RequestBody AddressBook addressBook){
        log.info("新增地址：{}",addressBook);
        addressBookService.save(addressBook);
        return Result.success();
    }

    /**
     * 查询当前登录用户的所有地址信息
     * @return
     */
    @ApiOperation("查询地址")
    @GetMapping("/list")
    public Result<List<AddressBook>> list(){
        log.info("查询当前登录用户的所有地址信息...");
        List<AddressBook> addressBookList = addressBookService.list();
        return Result.success(addressBookList);
    }

    /**
     * 根据id删除地址
     * @param id
     * @return
     */
    @ApiOperation("删除地址")
    @DeleteMapping
    public Result<String> delete(Long id){
        log.info("根据id删除地址：{}",id);
        addressBookService.delete(id);
        return Result.success();
    }

    /**
     * 根据id查询地址
     * @param id
     * @return
     */
    @ApiOperation("根据id查询地址")
    @GetMapping("/{id}")
    public Result<AddressBook> getInfo(@PathVariable Long id){
        log.info("根据id查询地址：{}",id);
        AddressBook addressBook = addressBookService.getInfo(id);
        return Result.success(addressBook);
    }

    /**
     * 修改地址
     * @param addressBook
     * @return
     */
    @ApiOperation("修改地址")
    @PutMapping
    public Result<String> update(@RequestBody AddressBook addressBook){
        log.info("修改地址：{}",addressBook);
        addressBookService.update(addressBook);
        return Result.success();
    }

    /**
     * 设置默认地址
     * @param addressBook
     * @return
     */
    @ApiOperation("设置默认地址")
    @PutMapping("/default")
    public Result<String> setDefault(@RequestBody AddressBook addressBook){
        log.info("设置默认地址：{}",addressBook.getId());
        addressBookService.setDefault(addressBook);
        return Result.success();
    }

    /**
     * 查询默认地址
     * @return
     */
    @ApiOperation("查询默认地址")
    @GetMapping("/default")
    public Result<AddressBook> getDefault(){
        log.info("查询默认地址...");
        AddressBook addressBook = addressBookService.getDefault();
        return Result.success(addressBook);
    }




}
