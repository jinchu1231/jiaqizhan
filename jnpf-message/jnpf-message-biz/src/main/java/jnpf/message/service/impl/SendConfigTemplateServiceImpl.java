package jnpf.message.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jnpf.base.service.SuperServiceImpl;
import jnpf.message.entity.SendConfigTemplateEntity;
import jnpf.message.mapper.SendConfigTemplateMapper;
import jnpf.message.model.sendmessageconfig.SendMessageConfigPagination;
import jnpf.message.service.SendConfigTemplateService;
import jnpf.util.UserProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 消息发送配置
 * 版本： V3.2.0
 * 版权： 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * 作者： JNPF开发平台组
 * 日期： 2022-08-19
 */
@Service
public class SendConfigTemplateServiceImpl extends SuperServiceImpl<SendConfigTemplateMapper, SendConfigTemplateEntity> implements SendConfigTemplateService {


    @Autowired
    private UserProvider userProvider;


    @Override
    public QueryWrapper<SendConfigTemplateEntity> getChild(SendMessageConfigPagination pagination, QueryWrapper<SendConfigTemplateEntity> sendConfigTemplateQueryWrapper) {
//        boolean pcPermission = false;
//        boolean appPermission = false;
//        boolean isPc = ServletUtil.getHeader("jnpf-origin").equals("pc");
//        if (isPc) {
//        }

        return sendConfigTemplateQueryWrapper;
    }

    @Override
    public SendConfigTemplateEntity getInfo(String id) {
        QueryWrapper<SendConfigTemplateEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(SendConfigTemplateEntity::getId, id);
        return this.getOne(queryWrapper);
    }

    @Override
    public List<SendConfigTemplateEntity> getDetailListByParentId(String id) {
        QueryWrapper<SendConfigTemplateEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(SendConfigTemplateEntity::getSendConfigId, id);
        return this.list(queryWrapper);
    }

    @Override
    public List<SendConfigTemplateEntity> getConfigTemplateListByConfigId(String id){
        QueryWrapper<SendConfigTemplateEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(SendConfigTemplateEntity::getSendConfigId, id);
        queryWrapper.lambda().eq(SendConfigTemplateEntity::getEnabledMark,1);
        return this.list(queryWrapper);
    }

    @Override
    public boolean isUsedAccount(String accountId){
        QueryWrapper<SendConfigTemplateEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(SendConfigTemplateEntity::getAccountConfigId,accountId);
        if(this.list(queryWrapper) != null && this.list(queryWrapper).size()>0){
            return true;
        }else {
            return false;
        }
    }

    @Override
    public boolean isUsedTemplate(String templateId){
        QueryWrapper<SendConfigTemplateEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(SendConfigTemplateEntity::getTemplateId,templateId);
        if(this.list(queryWrapper) != null && this.list(queryWrapper).size()>0){
            return true;
        }else {
            return false;
        }
    }
}