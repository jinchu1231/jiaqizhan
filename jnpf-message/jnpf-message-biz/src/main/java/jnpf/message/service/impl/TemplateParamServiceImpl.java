package jnpf.message.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jnpf.base.service.SuperServiceImpl;
import jnpf.message.entity.TemplateParamEntity;
import jnpf.message.mapper.TemplateParamMapper;
import jnpf.message.model.messagetemplateconfig.MessageTemplateConfigPagination;
import jnpf.message.service.TemplateParamService;
import jnpf.util.UserProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 消息模板（新）
 * 版本： V3.2.0
 * 版权： 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * 作者： JNPF开发平台组
 * 日期： 2022-08-18
 */
@Service
public class TemplateParamServiceImpl extends SuperServiceImpl<TemplateParamMapper, TemplateParamEntity> implements TemplateParamService {


    @Autowired
    private UserProvider userProvider;


    @Override
    public QueryWrapper<TemplateParamEntity> getChild(MessageTemplateConfigPagination pagination, QueryWrapper<TemplateParamEntity> templateParamQueryWrapper) {
//        boolean pcPermission = false;
//        boolean appPermission = false;
//        boolean isPc = ServletUtil.getHeader("jnpf-origin").equals("pc");
//        if (isPc) {
//        }

        return templateParamQueryWrapper;
    }

    @Override
    public TemplateParamEntity getInfo(String id) {
        QueryWrapper<TemplateParamEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(TemplateParamEntity::getId, id);
        return this.getOne(queryWrapper);
    }

    @Override
    public List<TemplateParamEntity> getDetailListByParentId(String id) {
        QueryWrapper<TemplateParamEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(TemplateParamEntity::getTemplateId, id);
        return this.list(queryWrapper);
    }

    @Override
    public List<TemplateParamEntity> getParamList(String id,List<String> params) {
        QueryWrapper<TemplateParamEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(TemplateParamEntity::getTemplateId, id);
        queryWrapper.lambda().in(TemplateParamEntity::getField,params);
        return this.list(queryWrapper);
    }

}