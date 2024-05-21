


package jnpf.message.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.alibaba.fastjson.JSONObject;
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
import jnpf.message.entity.AccountConfigEntity;
import jnpf.message.model.EmailModel;
import jnpf.message.model.accountconfig.AccountConfigForm;
import jnpf.message.model.accountconfig.AccountConfigInfoVO;
import jnpf.message.model.accountconfig.AccountConfigListVO;
import jnpf.message.model.accountconfig.AccountConfigPagination;
import jnpf.message.service.AccountConfigService;
import jnpf.message.service.SendConfigTemplateService;
import jnpf.message.util.EmailUtil;
import jnpf.permission.entity.UserEntity;
import jnpf.permission.service.UserService;
import jnpf.util.DateUtil;
import jnpf.util.JsonUtil;
import jnpf.util.RandomUtil;
import jnpf.util.StringUtil;
import jnpf.util.UserProvider;
import jnpf.util.third.DingTalkUtil;
import jnpf.util.third.QyWebChatUtil;
import jnpf.util.wxutil.HttpUtil;
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
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;


/**
 * 账号配置功能
 *
 * @版本： V3.2.0
 * @版权： 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @作者： JNPF开发平台组
 * @日期： 2022-08-18
 */
@Slf4j
@RestController
@Tag(name = "账号配置功能", description = "AccountConfig")
@RequestMapping("/api/message/AccountConfig")
public class AccountConfigController extends SuperController<AccountConfigService, AccountConfigEntity> {


    @Autowired
    private ConfigValueUtil configValueUtil;

    @Autowired
    private UserProvider userProvider;

    @Autowired
    private UserService userService;

    @Autowired
    private AccountConfigService accountConfigService;
    @Autowired
    private SendConfigTemplateService sendConfigTemplateService;
    @Autowired
    private DictionaryDataService dictionaryDataService;

    /**
     * 列表
     *
     * @param accountConfigPagination 账号配置分页模型
     * @return ignore
     */
    @Operation(summary = "列表")
    @SaCheckPermission("msgCenter.accountConfig")
    @GetMapping
    public ActionResult<PageListVO<AccountConfigListVO>> list(AccountConfigPagination accountConfigPagination) throws IOException {
        List<AccountConfigEntity> list = accountConfigService.getList(accountConfigPagination);
        List<DictionaryDataEntity> smsSendTypeList = dictionaryDataService.getListByTypeDataCode("smsSendType");
        List<DictionaryDataEntity> webHookList = dictionaryDataService.getListByTypeDataCode("msgWebHookSendType");
        //处理id字段转名称，若无需转或者为空可删除
        UserEntity userEntity;
        List<AccountConfigListVO> listVO = JsonUtil.getJsonToList(list, AccountConfigListVO.class);
        for (AccountConfigListVO accountConfigVO : listVO) {
            //渠道
            if (StringUtil.isNotEmpty(accountConfigVO.getChannel())) {
                smsSendTypeList.stream().filter(t -> accountConfigVO.getChannel().equals(t.getEnCode())).findFirst()
                        .ifPresent(dataTypeEntity -> accountConfigVO.setChannel(dataTypeEntity.getFullName()));
            }
            //webhook类型
            if (StringUtil.isNotEmpty(accountConfigVO.getWebhookType())) {
                webHookList.stream().filter(t -> accountConfigVO.getWebhookType().equals(t.getEnCode())).findFirst()
                        .ifPresent(dataTypeEntity -> accountConfigVO.setWebhookType(dataTypeEntity.getFullName()));
            }

            if (StringUtil.isNotEmpty(accountConfigVO.getCreatorUserId())) {
                userEntity = userService.getInfo(accountConfigVO.getCreatorUserId());
                if (userEntity != null) {
                    accountConfigVO.setCreatorUser(userEntity.getRealName() + "/" + userEntity.getAccount());
                }
            }

//            if(StringUtil.isNotBlank(accountConfigVO.getLastModifyUserId()) && !"null".equals(accountConfigVO.getLastModifyUserId())){
//                userEntity = userService.getInfo(accountConfigVO.getLastModifyUserId());
//                if(userEntity != null){
//                    accountConfigVO.setLastModifyUserId(userEntity.getRealName() + userEntity.getAccount());
//                }
//            }

        }

        PageListVO vo = new PageListVO();
        vo.setList(listVO);
        PaginationVO page = JsonUtil.getJsonToBean(accountConfigPagination, PaginationVO.class);
        vo.setPagination(page);
        return ActionResult.success(vo);
    }

    /**
     * 创建
     *
     * @param accountConfigForm 新建账号配置模型
     * @return ignore
     */
    @Operation(summary = "新建")
    @PostMapping
    @Parameters({
            @Parameter(name = "accountConfigForm", description = "新建账号配置模型")
    })
    @SaCheckPermission("msgCenter.accountConfig")
    @Transactional
    public ActionResult create(@RequestBody @Valid AccountConfigForm accountConfigForm) throws DataException {
        boolean b = accountConfigService.checkForm(accountConfigForm, 0, accountConfigForm.getType(), "");
        if (b) {
            return ActionResult.fail("编码不能重复");
        }
        boolean c = accountConfigService.checkGzhId(accountConfigForm.getAppKey(), 0, "7", "");
        if ("7".equals(accountConfigForm.getType())) {
            if (c) {
                return ActionResult.fail("微信公众号原始id不能重复");
            }
        }
        String mainId = RandomUtil.uuId();
        UserInfo userInfo = userProvider.get();
        AccountConfigEntity entity = JsonUtil.getJsonToBean(accountConfigForm, AccountConfigEntity.class);
        entity.setCreatorTime(DateUtil.getNowDate());
        entity.setCreatorUserId(userInfo.getUserId());
        entity.setId(mainId);
        accountConfigService.save(entity);
        return ActionResult.success("创建成功");
    }


    /**
     * 信息
     *
     * @param id 主键
     * @return ignore
     */
    @Operation(summary = "信息")
    @GetMapping("/{id}")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true)
    })
    @SaCheckPermission("msgCenter.accountConfig")
    public ActionResult<AccountConfigInfoVO> info(@PathVariable("id") String id) {
        AccountConfigEntity entity = accountConfigService.getInfo(id);
        AccountConfigInfoVO vo = JsonUtil.getJsonToBean(entity, AccountConfigInfoVO.class);
        return ActionResult.success(vo);
    }

    /**
     * 表单信息(详情页)
     *
     * @param id 主键
     * @return ignore
     */
    @Operation(summary = "表单信息(详情页)")
    @GetMapping("/detail/{id}")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true)
    })
    @SaCheckPermission("msgCenter.accountConfig")
    public ActionResult<AccountConfigInfoVO> detailInfo(@PathVariable("id") String id) {
        AccountConfigEntity entity = accountConfigService.getInfo(id);
        AccountConfigInfoVO vo = JsonUtil.getJsonToBean(entity, AccountConfigInfoVO.class);
        return ActionResult.success(vo);
    }


    /**
     * 更新
     *
     * @param id                主键
     * @param accountConfigForm 修改账号配置模型
     * @return ignore
     */
    @Operation(summary = "更新")
    @PutMapping("/{id}")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true),
            @Parameter(name = "accountConfigForm", description = "修改账号配置模型", required = true)
    })
    @SaCheckPermission("msgCenter.accountConfig")
    @Transactional
    public ActionResult update(@PathVariable("id") String id, @RequestBody @Valid AccountConfigForm accountConfigForm) throws DataException {

        boolean b = accountConfigService.checkForm(accountConfigForm, 0, accountConfigForm.getType(), accountConfigForm.getId());
        if (b) {
            return ActionResult.fail("编码不能重复");
        }
        //判断配置是否被引用
        if ("0".equals(String.valueOf(accountConfigForm.getEnabledMark()))) {
            if (sendConfigTemplateService.isUsedAccount(accountConfigForm.getId())) {
                return ActionResult.fail("此记录与“消息发送配置”关联引用，不允许被禁用");
            }
        }
        boolean c = accountConfigService.checkGzhId(accountConfigForm.getAppKey(), 0, "7", id);
        if ("7".equals(accountConfigForm.getType())) {
            if (c) {
                return ActionResult.fail("微信公众号原始id不能重复");
            }
        }
        //判断配置是否被引用
        if (Objects.equals(0, accountConfigForm.getEnabledMark())) {
            if (sendConfigTemplateService.isUsedAccount(accountConfigForm.getId())) {
                return ActionResult.fail("此记录与“消息发送配置”关联引用，不允许被禁用");
            }
        }
        UserInfo userInfo = userProvider.get();
        AccountConfigEntity entity = accountConfigService.getInfo(id);
        if (entity != null) {
            AccountConfigEntity subentity = JsonUtil.getJsonToBean(accountConfigForm, AccountConfigEntity.class);
            subentity.setCreatorTime(entity.getCreatorTime());
            subentity.setCreatorUserId(entity.getCreatorUserId());
            subentity.setLastModifyTime(DateUtil.getNowDate());
            subentity.setLastModifyUserId(userInfo.getUserId());
            boolean b1 = accountConfigService.updateById(subentity);
            if (!b1) {
                return ActionResult.fail("当前表单原数据已被调整，请重新进入该页面编辑并提交数据");
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
    @DeleteMapping("/{id}")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true)
    })
    @SaCheckPermission("msgCenter.accountConfig")
    @Transactional
    public ActionResult delete(@PathVariable("id") String id) {
        AccountConfigEntity entity = accountConfigService.getInfo(id);
        if (entity != null) {
            //判断是否与消息发送配置关联
            //判断配置是否被引用
            if (sendConfigTemplateService.isUsedAccount(entity.getId())) {
                return ActionResult.fail("此记录与“消息发送配置”关联引用，不允许被删除");
            }
            accountConfigService.delete(entity);

        }
        return ActionResult.success("删除成功");
    }

    /**
     * 开启或禁用
     *
     * @param id 主键
     * @return ignore
     */
    @Operation(summary = "开启或禁用")
    @PostMapping("/unable/{id}")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true)
    })
    @SaCheckPermission("msgCenter.accountConfig")
    @Transactional
    public ActionResult unable(@PathVariable("id") String id) {
        AccountConfigEntity entity = accountConfigService.getInfo(id);
        if (entity != null) {
            if ("1".equals(String.valueOf(entity.getEnabledMark()))) {
                entity.setEnabledMark(0);
                return ActionResult.success("禁用成功");
            } else {
                //判断是否被引用

                entity.setEnabledMark(1);
                return ActionResult.success("启用成功");
            }
        } else {
            return ActionResult.fail("操作失败，数据不存在");
        }
    }

    /**
     * 复制
     *
     * @param id 主键
     * @return ignore
     */
    @Operation(summary = "复制")
    @PostMapping("/{id}/Actions/Copy")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true)
    })
    @SaCheckPermission("msgCenter.accountConfig")
    @Transactional
    public ActionResult copy(@PathVariable("id") String id) {
        UserInfo userInfo = userProvider.get();
        AccountConfigEntity entity = accountConfigService.getInfo(id);
        if (entity != null) {
            entity.setEnabledMark(0);
            String copyNum = UUID.randomUUID().toString().substring(0, 5);
            entity.setFullName(entity.getFullName() + ".副本" + copyNum);
            entity.setEnCode(entity.getEnCode() + copyNum);
            entity.setCreatorTime(DateUtil.getNowDate());
            entity.setCreatorUserId(userInfo.getUserId());
            if ("7".equals(entity.getType())) {
                entity.setAppKey(entity.getAppKey() + "副本" + copyNum);
            }
            entity.setLastModifyTime(null);
            entity.setLastModifyUserId(null);
            entity.setId(RandomUtil.uuId());
            AccountConfigEntity copyEntity = JsonUtil.getJsonToBean(entity, AccountConfigEntity.class);
            if (copyEntity.getEnCode().length() > 50 || copyEntity.getFullName().length() > 50) {
                return ActionResult.fail("已到达该模板复制上限，请复制源模板");
            }
            accountConfigService.create(copyEntity);
            return ActionResult.success("复制数据成功");
        } else {
            return ActionResult.fail("复制失败，数据不存在");
        }
    }


    /**
     * 测试发送邮件
     *
     * @param accountConfigForm 账号测试模型
     * @return ignore
     */
    @Operation(summary = "测试发送邮箱")
    @PostMapping("/testSendMail")
    @Parameters({
            @Parameter(name = "accountConfigForm", description = "账号测试模型", required = true)
    })
    @SaCheckPermission("msgCenter.accountConfig")
    @Transactional
    public ActionResult testSendMail(@RequestBody @Valid AccountConfigForm accountConfigForm) {
        List<String> toMails = accountConfigForm.getTestSendEmail();
        // 获取邮箱配置
        Map<String, String> objModel = new HashMap<>();
        objModel.put("emailSmtpHost", accountConfigForm.getSmtpServer());
        objModel.put("emailSmtpPort", accountConfigForm.getSmtpPort().toString());
        objModel.put("emailSenderName", accountConfigForm.getAddressorName());
        objModel.put("emailAccount", accountConfigForm.getSmtpUser());
        objModel.put("emailPassword", accountConfigForm.getSmtpPassword());
        objModel.put("emailSsl", accountConfigForm.getSslLink() == 1 ? "true" : "false");


        EmailModel emailModel = JsonUtil.getJsonToBean(objModel, EmailModel.class);
        StringBuilder toUserMail = new StringBuilder();
        StringBuilder nullUserInfo = new StringBuilder();
        String userEmailAll = "";
        String userEmail = "";
        String userName = "";

        // 相关参数验证
        if (StringUtil.isEmpty(emailModel.getEmailSmtpHost())) {
            return ActionResult.fail("发送失败，失败原因：SMTP服务为空");
        } else if (StringUtil.isEmpty(emailModel.getEmailSmtpPort())) {
            return ActionResult.fail("发送失败，失败原因：SMTP端口为空");
        } else if (StringUtil.isEmpty(emailModel.getEmailAccount())) {
            return ActionResult.fail("发送失败，失败原因：发件人邮箱为空");
        } else if (StringUtil.isEmpty(emailModel.getEmailPassword())) {
            return ActionResult.fail("发送失败，失败原因：发件人密码为空");
        } else if (toMails == null || toMails.size() < 1) {
            return ActionResult.fail("发送失败，失败原因：接收人为空");
        } else {
            // 设置邮件标题
            emailModel.setEmailTitle(accountConfigForm.getTestEmailTitle());
            // 设置邮件内容
            String content = accountConfigForm.getTestEmailContent();
            emailModel.setEmailContent(content);

            // 获取收件人的邮箱地址、创建消息用户实体
            for (String userId : toMails) {
                UserEntity userEntity = userService.getInfo(userId);
                if (userEntity != null) {
                    userEmail = StringUtil.isEmpty(userEntity.getEmail()) ? "" : userEntity.getEmail();
                    userName = userEntity.getRealName();
                }
                if (StringUtil.isNotBlank(userEmail) && !"null".equals(userEmail)) {
                    //校验用户邮箱格式
                    if (!isEmail(userEmail)) {
                        return ActionResult.fail("发送失败。失败原因：" + userName + "的邮箱账号格式有误！");
                    }
                    toUserMail = toUserMail.append(",").append(userName).append("<").append(userEmail).append(">");
                } else {
                    return ActionResult.fail("发送失败。失败原因：" + userName + "的邮箱账号为空！");
                }
            }
            // 处理接收人员的邮箱信息串并验证
            userEmailAll = toUserMail.toString();
            if (StringUtil.isNotEmpty(userEmailAll)) {
                userEmailAll = userEmailAll.substring(1);
            }
            if (StringUtil.isEmpty(userEmailAll)) {
                return ActionResult.fail("发送失败。失败原因：接收人对应的邮箱全部为空");
            } else {
                // 设置接收人员
                emailModel.setEmailToUsers(userEmailAll);
                // 发送邮件
                JSONObject retJson = EmailUtil.sendMail(emailModel);
                if (!retJson.getBoolean("code")) {
                    return ActionResult.fail("发送失败。失败原因：" + retJson.get("error"));
                }
            }
        }
        return ActionResult.success("已发送");
    }

    /**
     * 测试企业微信配置的连接功能
     *
     * @param accountConfigForm 账号测试模型
     * @return ignore
     */
    @Operation(summary = "测试企业微信配置的连接功能")
    @Parameters({
            @Parameter(name = "accountConfigForm", description = "账号测试模型", required = true)
    })
    @SaCheckPermission("msgCenter.accountConfig")
    @PostMapping("/testQyWebChatConnect")
    public ActionResult testQyWebChatConnect(@RequestBody @Valid AccountConfigForm accountConfigForm) {
        JSONObject retMsg = new JSONObject();
        // 测试发送消息、组织同步的连接
        //企业微信企业id
        String corpId = accountConfigForm.getEnterpriseId();
        //企业微信应用secret
        String agentSecret = accountConfigForm.getAppSecret();
//        String corpSecret = testAccountConfigForm.getQyhCorpSecret();
        // 测试发送消息的连接
        retMsg = QyWebChatUtil.getAccessToken(corpId, agentSecret);
        if (HttpUtil.isWxError(retMsg)) {
            return ActionResult.fail("连接失败。失败原因：" + retMsg.getString("errmsg"));
        }
        return ActionResult.success("连接成功");
    }

    /**
     * 测试钉钉配置的连接功能
     *
     * @param accountConfigForm 账号测试模型
     * @return ignore
     */
    @Operation(summary = "测试钉钉配置的连接功能")
    @Parameters({
            @Parameter(name = "accountConfigForm", description = "账号测试模型", required = true)
    })
    @SaCheckPermission("msgCenter.accountConfig")
    @PostMapping("/testDingTalkConnect")
    public ActionResult testDingTalkConnect(@RequestBody @Valid AccountConfigForm accountConfigForm) {
        JSONObject retMsg = new JSONObject();
        // 测试钉钉配置的连接
        String appKey = accountConfigForm.getAppId();
        String appSecret = accountConfigForm.getAppSecret();
        ///
//        String agentId = dingTalkModel.getDingAgentId();
        // 测试钉钉的连接
        retMsg = DingTalkUtil.getAccessToken(appKey, appSecret);
        if (!retMsg.getBoolean("code")) {
            return ActionResult.fail("连接失败。失败原因：" + retMsg.getString("error"));
        }

        return ActionResult.success("连接成功");
    }

    public boolean isEmail(String email) {
        String EMAIL_REGEX = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
        Boolean b = email.matches(EMAIL_REGEX);
        return b;
    }
}
