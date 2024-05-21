package jnpf.message.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jnpf.base.ActionResult;
import jnpf.base.service.SuperServiceImpl;
import jnpf.constant.MsgCode;
import jnpf.exception.DataException;
import jnpf.message.entity.MessageTemplateConfigEntity;
import jnpf.message.entity.SmsFieldEntity;
import jnpf.message.entity.TemplateParamEntity;
import jnpf.message.mapper.MessageTemplateConfigMapper;
import jnpf.message.model.messagetemplateconfig.MessageTemplateConfigForm;
import jnpf.message.model.messagetemplateconfig.MessageTemplateConfigPagination;
import jnpf.message.model.messagetemplateconfig.TemplateParamModel;
import jnpf.message.service.MessageTemplateConfigService;
import jnpf.message.service.SmsFieldService;
import jnpf.message.service.TemplateParamService;
import jnpf.util.StringUtil;
import jnpf.util.UserProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 消息模板（新）
 * 版本： V3.2.0
 * 版权： 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * 作者： JNPF开发平台组
 * 日期： 2022-08-18
 */
@Service
public class MessageTemplateConfigServiceImpl extends SuperServiceImpl<MessageTemplateConfigMapper, MessageTemplateConfigEntity> implements MessageTemplateConfigService {



    @Autowired
    private UserProvider userProvider;



    @Autowired
    private TemplateParamService templateParamService;

    @Autowired
    private SmsFieldService smsFieldService;


    @Override
    public List<MessageTemplateConfigEntity> getList(MessageTemplateConfigPagination MessageTemplateConfigPagination) {
        return getTypeList(MessageTemplateConfigPagination, MessageTemplateConfigPagination.getDataType());
    }

    @Override
    public List<MessageTemplateConfigEntity> getTypeList(MessageTemplateConfigPagination MessageTemplateConfigPagination, String dataType) {
        String userId = userProvider.get().getUserId();
        int total = 0;
        int messageTemplateNewNum = 0;
        QueryWrapper<MessageTemplateConfigEntity> messageTemplateNewQueryWrapper = new QueryWrapper<>();
        int templateParamNum = 0;
        QueryWrapper<TemplateParamEntity> templateParamQueryWrapper = new QueryWrapper<>();
        int smsFieldNum = 0;
        QueryWrapper<SmsFieldEntity> smsFieldQueryWrapper = new QueryWrapper<>();
        //关键字
        if (ObjectUtil.isNotEmpty(MessageTemplateConfigPagination.getKeyword())) {
            messageTemplateNewNum++;
            messageTemplateNewQueryWrapper.lambda().and(t -> t.like(MessageTemplateConfigEntity::getEnCode, MessageTemplateConfigPagination.getKeyword()).
                    or().like(MessageTemplateConfigEntity::getFullName, MessageTemplateConfigPagination.getKeyword()));
        }
        //模板类型
        if (ObjectUtil.isNotEmpty(MessageTemplateConfigPagination.getTemplateType())) {
            messageTemplateNewNum++;
            messageTemplateNewQueryWrapper.lambda().eq(MessageTemplateConfigEntity::getTemplateType, MessageTemplateConfigPagination.getTemplateType());
        }
        //消息类型
        if (ObjectUtil.isNotEmpty(MessageTemplateConfigPagination.getMessageType())) {
            messageTemplateNewNum++;
            messageTemplateNewQueryWrapper.lambda().eq(MessageTemplateConfigEntity::getMessageType, MessageTemplateConfigPagination.getMessageType());
        }
        //消息来源
        if (ObjectUtil.isNotEmpty(MessageTemplateConfigPagination.getMessageSource())) {
            messageTemplateNewNum++;
            messageTemplateNewQueryWrapper.lambda().eq(MessageTemplateConfigEntity::getMessageSource, MessageTemplateConfigPagination.getMessageSource());
        }
        //状态
        if (ObjectUtil.isNotEmpty(MessageTemplateConfigPagination.getEnabledMark())) {
            messageTemplateNewNum++;
            int enabledMark = Integer.parseInt(MessageTemplateConfigPagination.getEnabledMark());
            messageTemplateNewQueryWrapper.lambda().eq(MessageTemplateConfigEntity::getEnabledMark, enabledMark);
        }

        //排序
        if (StringUtil.isEmpty(MessageTemplateConfigPagination.getSidx())) {
            messageTemplateNewQueryWrapper.lambda().orderByAsc(MessageTemplateConfigEntity::getSortCode).orderByDesc(MessageTemplateConfigEntity::getCreatorTime).orderByDesc(MessageTemplateConfigEntity::getLastModifyTime);
        } else {
            try {
                String sidx = MessageTemplateConfigPagination.getSidx();
                MessageTemplateConfigEntity MessageTemplateConfigEntity = new MessageTemplateConfigEntity();
                Field declaredField = MessageTemplateConfigEntity.getClass().getDeclaredField(sidx);
                declaredField.setAccessible(true);
                String value = declaredField.getAnnotation(TableField.class).value();
                messageTemplateNewQueryWrapper = "asc".equals(MessageTemplateConfigPagination.getSort().toLowerCase()) ? messageTemplateNewQueryWrapper.orderByAsc(value) : messageTemplateNewQueryWrapper.orderByDesc(value);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        }
        if (!"1".equals(dataType)) {
            if (total > 0 || total == 0) {
                Page<MessageTemplateConfigEntity> page = new Page<>(MessageTemplateConfigPagination.getCurrentPage(), MessageTemplateConfigPagination.getPageSize());
                IPage<MessageTemplateConfigEntity> userIPage = this.page(page, messageTemplateNewQueryWrapper);
                return MessageTemplateConfigPagination.setData(userIPage.getRecords(), userIPage.getTotal());
            } else {
                List<MessageTemplateConfigEntity> list = new ArrayList();
                return MessageTemplateConfigPagination.setData(list, list.size());
            }
        } else {
            return this.list(messageTemplateNewQueryWrapper);
        }
    }


    @Override
    public MessageTemplateConfigEntity getInfo(String id) {
        QueryWrapper<MessageTemplateConfigEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(MessageTemplateConfigEntity::getId, id);
        return this.getOne(queryWrapper);
    }

    @Override
    public MessageTemplateConfigEntity getInfoByEnCode(String enCode,String messageType) {
        QueryWrapper<MessageTemplateConfigEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(MessageTemplateConfigEntity::getEnCode, enCode);
        queryWrapper.lambda().eq(MessageTemplateConfigEntity::getMessageType, messageType);
        return this.getOne(queryWrapper);
    }

    @Override
    public void create(MessageTemplateConfigEntity entity) {
        this.save(entity);
    }

    @Override
    public boolean update(String id, MessageTemplateConfigEntity entity) {
        entity.setId(id);
        return this.updateById(entity);
    }

    @Override
    public void delete(MessageTemplateConfigEntity entity) {
        if (entity != null) {
            this.removeById(entity.getId());
        }
    }

    //子表方法
    @Override
    public List<TemplateParamEntity> getTemplateParamList(String id, MessageTemplateConfigPagination MessageTemplateConfigPagination) {
        QueryWrapper<TemplateParamEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(TemplateParamEntity::getTemplateId, id);
        return templateParamService.list(templateParamService.getChild(MessageTemplateConfigPagination, queryWrapper));
    }

    @Override
    public List<TemplateParamEntity> getTemplateParamList(String id) {
        QueryWrapper<TemplateParamEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(TemplateParamEntity::getTemplateId, id);
        return templateParamService.list(queryWrapper);
    }

    @Override
    public List<SmsFieldEntity> getSmsFieldList(String id, MessageTemplateConfigPagination MessageTemplateConfigPagination) {
        QueryWrapper<SmsFieldEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(SmsFieldEntity::getTemplateId, id);
        return smsFieldService.list(smsFieldService.getChild(MessageTemplateConfigPagination, queryWrapper));
    }

    @Override
    public List<SmsFieldEntity> getSmsFieldList(String id) {
        QueryWrapper<SmsFieldEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(SmsFieldEntity::getTemplateId, id);
        return smsFieldService.list(queryWrapper);
    }


    //验证表单唯一字段
    @Override
    public boolean checkForm(MessageTemplateConfigForm form, int i,String id) {
        int total = 0;
        if (ObjectUtil.isNotEmpty(form.getEnCode())) {
            QueryWrapper<MessageTemplateConfigEntity> codeWrapper = new QueryWrapper<>();
            codeWrapper.lambda().eq(MessageTemplateConfigEntity::getEnCode, form.getEnCode());
            if(StringUtil.isNotBlank(id) && !"null".equals(id)) {
                codeWrapper.lambda().ne(MessageTemplateConfigEntity::getId, id);
            }
            if ((int) this.count(codeWrapper) > i) {
                total++;
            }
        }
        if (form.getTemplateParamList() != null) {
        }
        if (form.getSmsFieldList() != null) {
        }
        if (total > 0) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isExistByFullName(String fullName, String id) {
        QueryWrapper<MessageTemplateConfigEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(MessageTemplateConfigEntity::getFullName, fullName);
        if (!StringUtil.isEmpty(id)) {
            queryWrapper.lambda().ne(MessageTemplateConfigEntity::getId, id);
        }
        return this.count(queryWrapper) > 0 ? true : false;
    }

    @Override
    public boolean isExistByEnCode(String enCode, String id) {
        QueryWrapper<MessageTemplateConfigEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(MessageTemplateConfigEntity::getEnCode, enCode);
        if (!StringUtil.isEmpty(id)) {
            queryWrapper.lambda().ne(MessageTemplateConfigEntity::getId, id);
        }
        return this.count(queryWrapper) > 0 ? true : false;
    }

    @Override
    public ActionResult ImportData(MessageTemplateConfigEntity entity) throws DataException {
        if (entity != null) {
//            if (isExistByFullName(entity.getFullName(), null)) {
//                return ActionResult.fail(MsgCode.EXIST001.get());
//            }
            if (isExistByEnCode(entity.getEnCode(), entity.getId())) {
                return ActionResult.fail(MsgCode.EXIST002.get());
            }
            try {
                this.save(entity);
            } catch (Exception e) {
                throw new DataException(MsgCode.IMP003.get());
            }
            return ActionResult.success(MsgCode.IMP001.get());
        }
        return ActionResult.fail("导入数据格式不正确");
    }


    @Override
    public List<TemplateParamModel> getParamJson(String id) {
        MessageTemplateConfigEntity entity = getInfo(id);
        List<TemplateParamModel> paramModelList = new ArrayList<>();
        List<String> smsField = new ArrayList<>();
        List<String> paramList = new ArrayList<>();
        if (entity != null) {
            if ("3".equals(entity.getMessageType()) || "7".equals(entity.getMessageType())) {
                List<SmsFieldEntity> smsFieldList = smsFieldService.getDetailListByParentId(id);
                for (SmsFieldEntity entity1 : smsFieldList) {
                    if(!"@FlowLink".equals(entity1.getField())) {
                        smsField.add(entity1.getField());
                    }
                }
                List<TemplateParamEntity> paramFieldList = templateParamService.getDetailListByParentId(id);
                for (TemplateParamEntity entity1 : paramFieldList) {
                    if (smsField.contains(entity1.getField())) {
                        TemplateParamModel paramModel = new TemplateParamModel();
                        paramModel.setTemplateId(entity.getId());
                        paramModel.setTemplateCode(entity.getEnCode());
                        paramModel.setTemplateType(entity.getTemplateType());
                        paramModel.setField(entity1.getField());
                        paramModel.setFieldName(entity1.getFieldName());
                        paramModel.setId(entity1.getId());
                        paramModel.setTemplateName(entity.getFullName());
                        paramModelList.add(paramModel);
                    }
                }
            } else {
                String content = StringUtil.isNotEmpty(entity.getContent()) ? entity.getContent() : "";
                String title = StringUtil.isNotEmpty(entity.getTitle()) ? entity.getTitle() : "";
                Set<String> list = new HashSet<>();
                list.addAll(regexContent(content));
                list.addAll(regexContent(title));
                List<TemplateParamEntity> paramFieldList = templateParamService.getDetailListByParentId(id);
                for (TemplateParamEntity entity1 : paramFieldList) {
                    TemplateParamModel paramModel = new TemplateParamModel();
                    paramModel.setTemplateId(entity.getId());
                    paramModel.setTemplateCode(entity.getEnCode());
                    paramModel.setTemplateType(entity.getTemplateType());
                    paramModel.setField(entity1.getField());
                    paramModel.setFieldName(entity1.getFieldName());
                    paramModel.setId(entity1.getId());
                    paramModel.setTemplateName(entity.getFullName());
                    if(list.contains(entity1.getField())){
                        if (!"@FlowLink".equals(entity1.getField())) {
                            paramModelList.add(paramModel);
                        }
                    }
                }
            }
        }
        //将参数模板转为json格式数据
//        String data = JsonUtil.getObjectToString(paramModelList);
        return paramModelList;
    }

    //获取消息内容参数
    public List<String> regexContent(String content){
        List<String> list = new ArrayList<>();
        String pattern = "[{]([^}]+)[}]";
        Pattern patternList = Pattern.compile(pattern);
        Matcher m = patternList.matcher(content);
        while (m.find()) {
            String group = m.group().replaceAll("\\{", "").replaceAll("}", "");
            list.add(group);
        }
        return list;
    }



}
