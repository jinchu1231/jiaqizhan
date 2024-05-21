package jnpf.message.service.impl;


import cn.hutool.core.util.ObjectUtil;
import com.baomidou.dynamic.datasource.annotation.DSTransactional;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jnpf.base.service.SuperServiceImpl;
import jnpf.message.entity.MessageMonitorEntity;
import jnpf.message.mapper.MessageMonitorMapper;
import jnpf.message.model.messagemonitor.MessageMonitorForm;
import jnpf.message.model.messagemonitor.MessageMonitorPagination;
import jnpf.message.service.MessageMonitorService;
import jnpf.permission.entity.UserEntity;
import jnpf.permission.service.UserService;
import jnpf.util.DateUtil;
import jnpf.util.JsonUtil;
import jnpf.util.StringUtil;
import jnpf.util.UserProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 消息监控
 * 版本： V3.2.0
 * 版权： 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * 作者： JNPF开发平台组
 * 日期： 2022-08-22
 */
@Service
public class MessageMonitorServiceImpl extends SuperServiceImpl<MessageMonitorMapper, MessageMonitorEntity> implements MessageMonitorService {


    @Autowired
    private UserProvider userProvider;

    @Autowired
    private UserService userService;



    @Override
    public List<MessageMonitorEntity> getList(MessageMonitorPagination messageMonitorPagination) {
        return getTypeList(messageMonitorPagination, messageMonitorPagination.getDataType());
    }

    @Override
    public List<MessageMonitorEntity> getTypeList(MessageMonitorPagination messageMonitorPagination, String dataType) {
        String userId = userProvider.get().getUserId();
        int total = 0;
        int messageMonitorNum = 0;
        QueryWrapper<MessageMonitorEntity> messageMonitorQueryWrapper = new QueryWrapper<>();
        //关键字
        if (ObjectUtil.isNotEmpty(messageMonitorPagination.getKeyword())) {
            messageMonitorNum++;
            messageMonitorQueryWrapper.lambda().and(t -> t.like(MessageMonitorEntity::getTitle, messageMonitorPagination.getKeyword()));
        }
        //消息类型
        if (ObjectUtil.isNotEmpty(messageMonitorPagination.getMessageType())) {
            messageMonitorNum++;
            messageMonitorQueryWrapper.lambda().eq(MessageMonitorEntity::getMessageType, messageMonitorPagination.getMessageType());
        }
        //发送时间
        if (ObjectUtil.isNotEmpty(messageMonitorPagination.getStartTime()) && ObjectUtil.isNotEmpty(messageMonitorPagination.getEndTime())) {
            messageMonitorNum++;
            Long fir = Long.valueOf(String.valueOf(messageMonitorPagination.getStartTime()));
            Long sec = Long.valueOf(String.valueOf(messageMonitorPagination.getEndTime()));

            messageMonitorQueryWrapper.lambda().ge(MessageMonitorEntity::getSendTime, new Date(fir))
                    .le(MessageMonitorEntity::getSendTime, DateUtil.stringToDate(DateUtil.daFormatYmd(sec) + " 23:59:59"));

        }
        //消息来源
        if (ObjectUtil.isNotEmpty(messageMonitorPagination.getMessageSource())) {
            messageMonitorNum++;
            messageMonitorQueryWrapper.lambda().eq(MessageMonitorEntity::getMessageSource, messageMonitorPagination.getMessageSource());
        }

        //排序
        if (StringUtil.isEmpty(messageMonitorPagination.getSidx())) {
            messageMonitorQueryWrapper.lambda().orderByDesc(MessageMonitorEntity::getSendTime);
        } else {
            try {
                String sidx = messageMonitorPagination.getSidx();
                MessageMonitorEntity messageMonitorEntity = new MessageMonitorEntity();
                Field declaredField = messageMonitorEntity.getClass().getDeclaredField(sidx);
                declaredField.setAccessible(true);
                String value = declaredField.getAnnotation(TableField.class).value();
                messageMonitorQueryWrapper = "asc".equals(messageMonitorPagination.getSort().toLowerCase()) ? messageMonitorQueryWrapper.orderByAsc(value) : messageMonitorQueryWrapper.orderByDesc(value);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        }
        if (!"1".equals(dataType)) {
            if (total > 0 || total == 0) {
                Page<MessageMonitorEntity> page = new Page<>(messageMonitorPagination.getCurrentPage(), messageMonitorPagination.getPageSize());
                IPage<MessageMonitorEntity> userIPage = this.page(page, messageMonitorQueryWrapper);
                return messageMonitorPagination.setData(userIPage.getRecords(), userIPage.getTotal());
            } else {
                List<MessageMonitorEntity> list = new ArrayList();
                return messageMonitorPagination.setData(list, list.size());
            }
        } else {
            return this.list(messageMonitorQueryWrapper);
        }
    }


    @Override
    public MessageMonitorEntity getInfo(String id) {
        QueryWrapper<MessageMonitorEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(MessageMonitorEntity::getId, id);
        return this.getOne(queryWrapper);
    }

    @Override
    public void create(MessageMonitorEntity entity) {
        this.save(entity);
    }

    @Override
    public boolean update(String id, MessageMonitorEntity entity) {
        entity.setId(id);
        return this.updateById(entity);
    }

    @Override
    public void delete(MessageMonitorEntity entity) {
        if (entity != null) {
            this.removeById(entity.getId());
        }
    }
    //子表方法

    //列表子表数据方法


    //验证表单唯一字段
    @Override
    public boolean checkForm(MessageMonitorForm form, int i) {
        int total = 0;
        if (total > i) {
            return true;
        }
        return false;
    }
    @Override
    public void emptyMonitor(){
        QueryWrapper<MessageMonitorEntity> queryWrapper = new QueryWrapper<>();
        this.remove(queryWrapper);
    }

    @Override
    @DSTransactional
    public boolean delete(String[] ids) {
        if (ids.length > 0) {
            QueryWrapper<MessageMonitorEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().in(MessageMonitorEntity::getId, ids);
            return this.remove(queryWrapper);
        }
        return false;
    }
    /**
     * 用户id转名称(多选)
     *
     * @param ids
     * @return
     */
    @Override
    public String userSelectValues(String ids) {
        if (StringUtil.isEmpty(ids)) {
            return ids;
        }
        if (ids.contains("[")){
            List<String> nameList = new ArrayList<>();
            List<String> jsonToList = JsonUtil.getJsonToList(ids, String.class);
            for (String userId : jsonToList){
                UserEntity info = userService.getInfo(userId);
                nameList.add(Objects.nonNull(info) ? info.getRealName()+ "/" + info.getAccount() : userId);
            }
            return String.join(";", nameList);
        }else {
            List<String> userInfoList = new ArrayList<>();
            String[] idList = ids.split(",");
            if (idList.length > 0) {
                for (String id : idList) {
                    UserEntity userEntity = userService.getInfo(id);
                    if (ObjectUtil.isNotEmpty(userEntity)) {
                        String info = userEntity.getRealName() + "/" + userEntity.getAccount();
                        userInfoList.add(info);
                    }
                }
            }
            return String.join("-", userInfoList);
        }
    }


}