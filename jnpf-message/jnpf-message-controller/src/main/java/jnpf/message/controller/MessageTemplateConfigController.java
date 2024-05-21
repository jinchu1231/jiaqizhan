


package jnpf.message.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jnpf.base.ActionResult;
import jnpf.base.UserInfo;
import jnpf.base.controller.SuperController;
import jnpf.base.entity.DictionaryDataEntity;
import jnpf.base.service.DictionaryDataService;
import jnpf.base.vo.PageListVO;
import jnpf.base.vo.PaginationVO;
import jnpf.config.ConfigValueUtil;
import jnpf.exception.DataException;
import jnpf.message.entity.MessageTemplateConfigEntity;
import jnpf.message.entity.SendMessageConfigEntity;
import jnpf.message.entity.SmsFieldEntity;
import jnpf.message.entity.TemplateParamEntity;
import jnpf.message.model.messagetemplateconfig.MessageTemplateConfigForm;
import jnpf.message.model.messagetemplateconfig.MessageTemplateConfigInfoVO;
import jnpf.message.model.messagetemplateconfig.MessageTemplateConfigListVO;
import jnpf.message.model.messagetemplateconfig.MessageTemplateConfigPagination;
import jnpf.message.service.MessageTemplateConfigService;
import jnpf.message.service.SendConfigTemplateService;
import jnpf.message.service.SendMessageConfigService;
import jnpf.message.service.SmsFieldService;
import jnpf.message.service.TemplateParamService;
import jnpf.permission.entity.UserEntity;
import jnpf.permission.service.UserService;
import jnpf.util.DateUtil;
import jnpf.util.JsonUtil;
import jnpf.util.RandomUtil;
import jnpf.util.StringUtil;
import jnpf.util.UserProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 消息模板（新）
 *
 * @版本： V3.2.0
 * @版权： 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @作者： JNPF开发平台组
 * @日期： 2022-08-18
 */
@Slf4j
@RestController
@Tag(name = "消息模板（新）", description = "MessageTemplateConfig")
@RequestMapping("/api/message/MessageTemplateConfig")
public class MessageTemplateConfigController extends SuperController<MessageTemplateConfigService, MessageTemplateConfigEntity> {



    @Autowired
    private ConfigValueUtil configValueUtil;
    @Autowired
    private UserProvider userProvider;
    @Autowired
    private UserService userService;
    @Autowired
    private MessageTemplateConfigService messageTemplateConfigService;
    @Autowired
    private TemplateParamService templateParamService;
    @Autowired
    private SmsFieldService smsFieldService;
    @Autowired
    private SendConfigTemplateService sendConfigTemplateService;
    @Autowired
    private DictionaryDataService dictionaryDataService;
    @Autowired
    private SendMessageConfigService sendMessageConfigService;


    /**
     * 列表
     *
     * @param messageTemplateConfigPagination 消息模板分页模型
     * @return ignore
     */
    @Operation(summary = "列表")
    @SaCheckPermission(value = {"msgCenter.msgTemplate", "msgCenter.accountConfig"}, mode = SaMode.OR)
    @GetMapping
    public ActionResult<PageListVO<MessageTemplateConfigListVO>> list(MessageTemplateConfigPagination messageTemplateConfigPagination) throws IOException {
        List<MessageTemplateConfigEntity> list = messageTemplateConfigService.getList(messageTemplateConfigPagination);
        List<DictionaryDataEntity> msgSendTypeList = dictionaryDataService.getListByTypeDataCode("msgSendType");
        List<DictionaryDataEntity> msgSourceTypeList = dictionaryDataService.getListByTypeDataCode("msgSourceType");
        //处理id字段转名称，若无需转或者为空可删除
        UserEntity userEntity = new UserEntity();
        List<MessageTemplateConfigListVO> listVO = JsonUtil.getJsonToList(list, MessageTemplateConfigListVO.class);
        for (MessageTemplateConfigListVO messageTemplateNewVO : listVO) {
            //消息类型
            if(StringUtil.isNotEmpty(messageTemplateNewVO.getMessageType())){
                msgSendTypeList.stream().filter(t -> messageTemplateNewVO.getMessageType().equals(t.getEnCode())).findFirst()
                        .ifPresent(dataTypeEntity -> messageTemplateNewVO.setMessageType(dataTypeEntity.getFullName()));
            }
            //创建人员
            if(StringUtil.isNotBlank(messageTemplateNewVO.getCreatorUserId()) && !"null".equals(messageTemplateNewVO.getCreatorUserId())){
                userEntity = userService.getInfo(messageTemplateNewVO.getCreatorUserId());
                if(userEntity != null){
                    messageTemplateNewVO.setCreatorUser(userEntity.getRealName() +"/"+ userEntity.getAccount());
                }
            }
            //消息来源
            if(StringUtil.isNotEmpty(messageTemplateNewVO.getMessageSource())) {
                msgSourceTypeList.stream().filter(t -> messageTemplateNewVO.getMessageSource().equals(t.getEnCode())).findFirst()
                        .ifPresent(dataTypeEntity -> messageTemplateNewVO.setMessageSource(dataTypeEntity.getFullName()));
            }
        }

        PageListVO vo = new PageListVO();
        vo.setList(listVO);
        PaginationVO page = JsonUtil.getJsonToBean(messageTemplateConfigPagination, PaginationVO.class);
        vo.setPagination(page);
        return ActionResult.success(vo);

    }

    /**
     * 创建
     *
     * @param messageTemplateConfigForm 消息模板页模型
     * @return ignore
     */
    @Operation(summary = "创建")
    @Parameters({
            @Parameter(name = "messageTemplateConfigForm", description = "消息模板页模型", required = true)
    })
    @SaCheckPermission("msgCenter.msgTemplate")
    @PostMapping
    @Transactional
    public ActionResult create(@RequestBody @Valid MessageTemplateConfigForm messageTemplateConfigForm) throws DataException {
        boolean b = messageTemplateConfigService.checkForm(messageTemplateConfigForm, 0,"");
        if (b) {
            return ActionResult.fail("编码不能重复");
        }
        if(!"1".equals(messageTemplateConfigForm.getTemplateType())){
            if(messageTemplateConfigForm.getEnCode().contains("MBXT")){
                return ActionResult.fail("自定义模板编码不能使用系统模板编码规则");
            }
        }
        if (messageTemplateConfigForm.getSmsFieldList() != null && "7".equals(messageTemplateConfigForm.getMessageType())) {
            List<SmsFieldEntity> SmsFieldList = JsonUtil.getJsonToList(messageTemplateConfigForm.getSmsFieldList(), SmsFieldEntity.class);
            List<SmsFieldEntity> list = SmsFieldList.stream().filter(t->StringUtil.isNotEmpty(String.valueOf(t.getIsTitle())) &&!"null".equals(String.valueOf(t.getIsTitle())) && t.getIsTitle()==1).collect(Collectors.toList());
            if (list != null) {
                if(list.size() > 1) {
                    return ActionResult.fail("创建失败，存在多个标题参数");
                }
            }else {
                return ActionResult.fail("创建失败，不存在标题参数");
            }
        }
        String mainId = RandomUtil.uuId();
        UserInfo userInfo = userProvider.get();
        MessageTemplateConfigEntity entity = JsonUtil.getJsonToBean(messageTemplateConfigForm, MessageTemplateConfigEntity.class);
        entity.setCreatorTime(DateUtil.getNowDate());
        entity.setCreatorUserId(userInfo.getUserId());
        entity.setId(mainId);
        if("1".equals(entity.getMessageType()) && "2".equals(entity.getMessageSource())){
            entity.setContent(null);
        }
        messageTemplateConfigService.save(entity);
        if (messageTemplateConfigForm.getTemplateParamList() != null) {
            List<TemplateParamEntity> TemplateParamList = JsonUtil.getJsonToList(messageTemplateConfigForm.getTemplateParamList(), TemplateParamEntity.class);
            for (TemplateParamEntity entitys : TemplateParamList) {
                entitys.setId(RandomUtil.uuId());
                entitys.setTemplateId(entity.getId());
                templateParamService.save(entitys);
            }
        }
        if (messageTemplateConfigForm.getSmsFieldList() != null) {
            List<SmsFieldEntity> SmsFieldList = JsonUtil.getJsonToList(messageTemplateConfigForm.getSmsFieldList(), SmsFieldEntity.class);
            for (SmsFieldEntity entitys : SmsFieldList) {
                entitys.setId(RandomUtil.uuId());
                entitys.setTemplateId(entity.getId());
                smsFieldService.save(entitys);
            }
        }

        return ActionResult.success("创建成功");
    }


    /**
     * 信息
     *
     * @param id 主键
     * @return ignore
     */
    @Operation(summary = "信息")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true)
    })
    @SaCheckPermission(value = {"msgCenter.msgTemplate", "msgCenter.accountConfig"}, mode = SaMode.OR)
    @GetMapping("/{id}")
    public ActionResult<MessageTemplateConfigInfoVO> info(@PathVariable("id") String id) {
        MessageTemplateConfigEntity entity = messageTemplateConfigService.getInfo(id);
        MessageTemplateConfigInfoVO vo = JsonUtil.getJsonToBean(entity, MessageTemplateConfigInfoVO.class);
        //子表
        List<TemplateParamEntity> BaseTemplateParamList = messageTemplateConfigService.getTemplateParamList(id);
//        for (TemplateParamEntity basetemplateparamEntity : BaseTemplateParamList) {
//        }
        vo.setTemplateParamList(BaseTemplateParamList);
        List<SmsFieldEntity> BaseSmsFieldList = messageTemplateConfigService.getSmsFieldList(id);
//        for (SmsFieldEntity basesmsfieldEntity : BaseSmsFieldList) {
//        }
        vo.setSmsFieldList(BaseSmsFieldList);
        //副表
        return ActionResult.success(vo);
    }

    /**
     * 表单信息(详情页)
     *
     * @param id 主键
     * @return ignore
     */
    @Operation(summary = "表单信息(详情页)")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true)
    })
    @SaCheckPermission("msgCenter.msgTemplate")
    @GetMapping("/detail/{id}")
    public ActionResult<MessageTemplateConfigInfoVO> detailInfo(@PathVariable("id") String id) {
        MessageTemplateConfigEntity entity = messageTemplateConfigService.getInfo(id);
        UserEntity userEntity = new UserEntity();
        MessageTemplateConfigInfoVO vo = JsonUtil.getJsonToBean(entity, MessageTemplateConfigInfoVO.class);

        //子表数据转换
        List<TemplateParamEntity> BaseTemplateParamList = messageTemplateConfigService.getTemplateParamList(id);
//        for (TemplateParamEntity basetemplateparamEntity : BaseTemplateParamList) {
//        }
        vo.setTemplateParamList(BaseTemplateParamList);
        List<SmsFieldEntity> BaseSmsFieldList = messageTemplateConfigService.getSmsFieldList(id);
//        for (SmsFieldEntity basesmsfieldEntity : BaseSmsFieldList) {
//        }
        vo.setSmsFieldList(BaseSmsFieldList);

        return ActionResult.success(vo);
    }


    /**
     * 更新
     *
     * @param id 主键
     * @param messageTemplateConfigForm 消息模板页模型
     * @return
     */
    @Operation(summary = "更新")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true),
            @Parameter(name = "messageTemplateConfigForm", description = "消息模板页模型", required = true)
    })
    @SaCheckPermission("msgCenter.msgTemplate")
    @PutMapping("/{id}")
    @Transactional
    public ActionResult update(@PathVariable("id") String id, @RequestBody @Valid MessageTemplateConfigForm messageTemplateConfigForm) throws DataException {

        boolean b = messageTemplateConfigService.checkForm(messageTemplateConfigForm, 0, messageTemplateConfigForm.getId());
        if (b) {
            return ActionResult.fail("编码不能重复");
        }
        if(!"1".equals(messageTemplateConfigForm.getTemplateType())){
            if(messageTemplateConfigForm.getEnCode().contains("MBXT")){
                return ActionResult.fail("自定义模板编码不能使用系统模板编码规则");
            }
        }
        //判断配置是否被引用
        if("0".equals(String.valueOf(messageTemplateConfigForm.getEnabledMark()))){
            if(sendConfigTemplateService.isUsedTemplate(messageTemplateConfigForm.getId())) {
                return ActionResult.fail("此记录与“消息发送配置”关联引用，不允许被禁用");
            }
        }
        if (messageTemplateConfigForm.getSmsFieldList() != null && "7".equals(messageTemplateConfigForm.getMessageType())) {
            List<SmsFieldEntity> SmsFieldList = JsonUtil.getJsonToList(messageTemplateConfigForm.getSmsFieldList(), SmsFieldEntity.class);
            List<SmsFieldEntity> list = SmsFieldList.stream().filter(t->StringUtil.isNotEmpty(String.valueOf(t.getIsTitle())) &&!"null".equals(String.valueOf(t.getIsTitle())) && t.getIsTitle()==1).collect(Collectors.toList());
            if (list != null) {
                if(list.size() > 1) {
                    return ActionResult.fail("更新失败，存在多个标题参数");
                }
            }else {
                return ActionResult.fail("更新失败，不存在标题参数");
            }
        }
        UserInfo userInfo = userProvider.get();
        MessageTemplateConfigEntity entity = messageTemplateConfigService.getInfo(id);
        if (entity != null) {
            MessageTemplateConfigEntity subentity = JsonUtil.getJsonToBean(messageTemplateConfigForm, MessageTemplateConfigEntity.class);
            subentity.setCreatorTime(entity.getCreatorTime());
            subentity.setCreatorUserId(entity.getCreatorUserId());
            subentity.setLastModifyTime(DateUtil.getNowDate());
            subentity.setLastModifyUserId(userInfo.getUserId());
            if("1".equals(subentity.getMessageType()) && "2".equals(subentity.getMessageSource())){
                subentity.setContent(null);
            }
            boolean b1 = messageTemplateConfigService.updateById(subentity);
            if (!b1) {
                return ActionResult.fail("当前表单原数据已被调整，请重新进入该页面编辑并提交数据");
            }

            //明细表数据更新
            List<TemplateParamEntity> addParamList = new ArrayList<>();
            List<TemplateParamEntity> updParamList = new ArrayList<>();
            List<TemplateParamEntity> delParamList = new ArrayList<>();
            if (messageTemplateConfigForm.getTemplateParamList() != null) {
                List<TemplateParamEntity> templateParamList = JsonUtil.getJsonToList(messageTemplateConfigForm.getTemplateParamList(), TemplateParamEntity.class);
                for (TemplateParamEntity entitys : templateParamList) {
                    if (StringUtil.isNotBlank(entitys.getId()) && !"null".equals(entitys.getId())) {
                        TemplateParamEntity paramEntity = templateParamService.getInfo(entitys.getId());
                        if (paramEntity != null) {
                            paramEntity.setId(entitys.getId());
                            paramEntity.setTemplateId(entitys.getTemplateId());
                            paramEntity.setField(entitys.getField());
                            paramEntity.setFieldName(entitys.getFieldName());
                            paramEntity.setCreatorUserId(entity.getCreatorUserId());
                            paramEntity.setCreatorTime(entitys.getCreatorTime());
                            paramEntity.setLastModifyUserId(userInfo.getUserId());
                            paramEntity.setLastModifyTime(DateUtil.getNowDate());
                            updParamList.add(paramEntity);
                        }
                    } else {
                        entitys.setId(RandomUtil.uuId());
                        entitys.setTemplateId(entity.getId());
                        entitys.setCreatorUserId(userInfo.getUserId());
                        entitys.setCreatorTime(DateUtil.getNowDate());
                        addParamList.add(entitys);
                    }
                }

                //删除参数记录
                List<TemplateParamEntity> paramEntityList = templateParamService.getDetailListByParentId(entity.getId());
                if (paramEntityList != null) {
                    for (TemplateParamEntity paramEntity : paramEntityList) {
                        TemplateParamEntity paramEntity1 = templateParamList.stream().filter(t -> t.getId().equals(paramEntity.getId())).findFirst().orElse(null);
                        if (paramEntity1 == null) {
                            delParamList.add(paramEntity);
                        }
                    }
                }
                if (addParamList != null && addParamList.size() > 0) {
                    templateParamService.saveBatch(addParamList);
                }
                if (updParamList != null && updParamList.size() > 0) {
                    templateParamService.updateBatchById(updParamList);
                }
                if (delParamList != null && delParamList.size() > 0) {
                    templateParamService.removeByIds(delParamList.stream().map(TemplateParamEntity::getId).collect(Collectors.toList()));
                }
            }

            //短信参数明细表数据更新
            List<SmsFieldEntity> addSmsList = new ArrayList<>();
            List<SmsFieldEntity> updSmsList = new ArrayList<>();
            List<SmsFieldEntity> delSmsList = new ArrayList<>();
            if (messageTemplateConfigForm.getSmsFieldList() != null) {
                List<SmsFieldEntity> smsFieldList = JsonUtil.getJsonToList(messageTemplateConfigForm.getSmsFieldList(), SmsFieldEntity.class);
                for (SmsFieldEntity entitys : smsFieldList) {
                    if (StringUtil.isNotBlank(entitys.getId()) && !"null".equals(entitys.getId())) {
                        SmsFieldEntity smsFieldEntity = smsFieldService.getInfo(entitys.getId());
                        if (smsFieldEntity != null) {
                            smsFieldEntity.setId(entitys.getId());
                            smsFieldEntity.setTemplateId(entity.getId());
                            smsFieldEntity.setFieldId(entitys.getFieldId());
                            smsFieldEntity.setField(entitys.getField());
                            smsFieldEntity.setSmsField(entitys.getSmsField());
                            smsFieldEntity.setCreatorTime(entitys.getCreatorTime());
                            smsFieldEntity.setCreatorUserId(entitys.getCreatorUserId());
                            smsFieldEntity.setLastModifyTime(DateUtil.getNowDate());
                            smsFieldEntity.setLastModifyUserId(userInfo.getUserId());
                            smsFieldEntity.setIsTitle(entitys.getIsTitle());
                            updSmsList.add(smsFieldEntity);
                        }
                    } else {
                        entitys.setId(RandomUtil.uuId());
                        entitys.setTemplateId(entity.getId());
                        entitys.setCreatorTime(DateUtil.getNowDate());
                        entitys.setCreatorUserId(userInfo.getUserId());
                        addSmsList.add(entitys);
                    }
                }
                //删除短信参数明细表
                List<SmsFieldEntity> smsFieldEntityList = smsFieldService.getDetailListByParentId(entity.getId());
                if (smsFieldEntityList != null && smsFieldEntityList.size() > 0) {
                    for (SmsFieldEntity smsFieldEntity : smsFieldEntityList) {
                        SmsFieldEntity smsFieldEntity1 = smsFieldList.stream().filter(t -> t.getId().equals(smsFieldEntity.getId())).findFirst().orElse(null);
                        if (smsFieldEntity1 == null) {
                            delSmsList.add(smsFieldEntity);
                        }
                    }
                }
                if (addSmsList != null && addSmsList.size() > 0) {
                    smsFieldService.saveBatch(addSmsList);
                }
                if (updSmsList != null && updSmsList.size() > 0) {
                    smsFieldService.updateBatchById(updSmsList);
                }
                if (delSmsList != null && delSmsList.size() > 0) {
                    smsFieldService.removeByIds(delSmsList.stream().map(SmsFieldEntity::getId).collect(Collectors.toList()));
                }
            }
            return ActionResult.success("更新成功");
        } else {
            return ActionResult.fail("更新失败，数据不存在");
        }
    }


    /**
     * 删除
     *
     * @param id 主键
     * @return ignore
     */
    @Operation(summary = "删除")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true)
    })
    @SaCheckPermission("msgCenter.msgTemplate")
    @DeleteMapping("/{id}")
    @Transactional
    public ActionResult delete(@PathVariable("id") String id) {
        MessageTemplateConfigEntity entity = messageTemplateConfigService.getInfo(id);
        if (entity != null) {
            if(sendConfigTemplateService.isUsedTemplate(entity.getId())) {
                return ActionResult.fail("此记录与“消息发送配置”关联引用，不允许被删除");
            }
            messageTemplateConfigService.delete(entity);
            QueryWrapper<TemplateParamEntity> queryWrapperTemplateParam = new QueryWrapper<>();
            queryWrapperTemplateParam.lambda().eq(TemplateParamEntity::getTemplateId, entity.getId());
            templateParamService.remove(queryWrapperTemplateParam);
            QueryWrapper<SmsFieldEntity> queryWrapperSmsField = new QueryWrapper<>();
            queryWrapperSmsField.lambda().eq(SmsFieldEntity::getTemplateId, entity.getId());
            smsFieldService.remove(queryWrapperSmsField);

        }
        return ActionResult.success("删除成功");
    }

    /**
     * 开启或禁用
     *
     * @param id
     * @return
     */
    @Operation(summary = "开启或禁用")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true)
    })
    @SaCheckPermission("msgCenter.msgTemplate")
    @PostMapping("/unable/{id}")
    @Transactional
    public ActionResult unable(@PathVariable("id") String id) {
        MessageTemplateConfigEntity entity = messageTemplateConfigService.getInfo(id);
        if (entity != null) {
            if("1".equals(String.valueOf(entity.getEnabledMark()))){
                entity.setEnabledMark(0);
                return ActionResult.success("禁用成功");
            }else {
                //判断是否被引用

                entity.setEnabledMark(1);
                return ActionResult.success("启用成功");
            }
        }else {
            return ActionResult.fail("操作失败，数据不存在");
        }
    }

    /**
     * 复制
     *
     * @param id
     * @return
     */
    @Operation(summary = "复制")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true)
    })
    @SaCheckPermission("msgCenter.msgTemplate")
    @PostMapping("/{id}/Actions/Copy")
    @Transactional
    public ActionResult copy(@PathVariable("id") String id) {
        UserInfo userInfo = userProvider.get();
        MessageTemplateConfigEntity entity = messageTemplateConfigService.getInfo(id);
        if (entity != null) {
            entity.setEnabledMark(0);
            String copyNum = UUID.randomUUID().toString().substring(0, 5);
            entity.setFullName(entity.getFullName()+".副本"+copyNum);
            entity.setEnCode(entity.getEnCode()+copyNum);
            entity.setCreatorTime(DateUtil.getNowDate());
            entity.setCreatorUserId(userInfo.getUserId());
            entity.setLastModifyTime(null);
            entity.setLastModifyUserId(null);
            entity.setTemplateType("0");
            entity.setId(RandomUtil.uuId());
            MessageTemplateConfigEntity copyEntity = JsonUtil.getJsonToBean(entity, MessageTemplateConfigEntity.class);
            if(copyEntity.getEnCode().length()>50 || copyEntity.getFullName().length()>50){
                return ActionResult.fail("已到达该模板复制上限，请复制源模板");
            }
            messageTemplateConfigService.create(copyEntity);
            List<TemplateParamEntity> copyParamList = new ArrayList<>();
            List<TemplateParamEntity> baseParamList = templateParamService.getDetailListByParentId(id);
            if(baseParamList != null && baseParamList.size()>0){
                for(TemplateParamEntity entitys : baseParamList){
                    entitys.setId(RandomUtil.uuId());
                    entitys.setTemplateId(copyEntity.getId());
                    entitys.setCreatorTime(DateUtil.getNowDate());
                    entitys.setCreatorUserId(userInfo.getUserId());
                    entitys.setLastModifyTime(null);
                    entitys.setLastModifyUserId(null);
                    copyParamList.add(entitys);
                }
            }
            if(copyParamList != null && copyParamList.size()>0) {
                templateParamService.saveBatch(copyParamList);
            }
            List<SmsFieldEntity> copySmsList = new ArrayList<>();
            List<SmsFieldEntity> baseSmsFieldList = smsFieldService.getDetailListByParentId(id);
            if(baseSmsFieldList != null && baseSmsFieldList.size()>0){
                for(SmsFieldEntity entitys : baseSmsFieldList){
                    entitys.setId(RandomUtil.uuId());
                    entitys.setTemplateId(copyEntity.getId());
                    entitys.setCreatorTime(DateUtil.getNowDate());
                    entitys.setCreatorUserId(userInfo.getUserId());
                    entitys.setLastModifyTime(null);
                    entitys.setLastModifyUserId(null);
                    copySmsList.add(entitys);
                }
            }
            if(copySmsList != null && copySmsList.size()>0){
                smsFieldService.saveBatch(copySmsList);
            }
            return ActionResult.success("复制数据成功");
        }else {
            return ActionResult.fail("复制失败，数据不存在");
        }
    }

    
    @Operation(summary = "根据encode获取消息配置")
    @GetMapping("/getInfoByEnCode")
    public MessageTemplateConfigEntity getInfoByEnCode(@RequestParam("enCode") String enCode){
        MessageTemplateConfigEntity entity = messageTemplateConfigService.getInfoByEnCode(enCode,"1");
        return entity;
    }

    
    @GetMapping("/getSendMessageConfig/{id}")
    public SendMessageConfigEntity getSendMessageConfig(@RequestParam("id") String id) {
        SendMessageConfigEntity info = sendMessageConfigService.getInfo(id);
        return info;
    }


}
