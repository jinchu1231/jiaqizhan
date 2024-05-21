package jnpf.message.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jnpf.base.controller.SuperController;
import jnpf.message.entity.UserDeviceEntity;
import jnpf.message.service.UserDeviceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@Tag(name = "个推", description = "UserDevice")
@RequestMapping("/api/message/UserDevice")
public class UserDeviceController extends SuperController<UserDeviceService, UserDeviceEntity>  {
    @Autowired
    private UserDeviceService userDeviceService;


    
    @GetMapping("/getInfoByClientId/{id}")
    public UserDeviceEntity getInfoByClientId(@RequestParam("id") String id) {
        return userDeviceService.getInfoByClientId(id);
    }

    
    @PostMapping("/update/{id}")
    public Boolean update(@RequestParam("id") String id, @RequestBody UserDeviceEntity entity){
        return userDeviceService.update(id, entity);
    }

    
    @PostMapping("/create")
    public Boolean create(@RequestBody UserDeviceEntity userDeviceEntity){
        userDeviceService.create(userDeviceEntity);
        return true;
    }

    
    @PostMapping("/delete")
    public void delete(@RequestBody UserDeviceEntity userDeviceEntity) {
        userDeviceService.delete(userDeviceEntity);
    }

}
