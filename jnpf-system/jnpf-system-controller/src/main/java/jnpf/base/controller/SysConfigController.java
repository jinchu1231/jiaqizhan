package jnpf.base.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.alibaba.fastjson.JSONObject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jnpf.base.ActionResult;
import jnpf.base.entity.EmailConfigEntity;
import jnpf.base.entity.SysConfigEntity;
import jnpf.base.model.synthirdinfo.DingTalkModel;
import jnpf.base.model.synthirdinfo.QyWebChatModel;
import jnpf.base.model.systemconfig.EmailTestForm;
import jnpf.base.model.systemconfig.SysConfigModel;
import jnpf.base.service.CheckLoginService;
import jnpf.base.service.SysconfigService;
import jnpf.config.ConfigValueUtil;
import jnpf.constant.MsgCode;
import jnpf.database.util.TenantDataSourceUtil;
import jnpf.model.BaseSystemInfo;
import jnpf.permission.model.user.UserAdminVO;
import jnpf.permission.model.user.UserUpAdminForm;
import jnpf.permission.service.UserService;
import jnpf.util.JsonUtil;
import jnpf.util.NoDataSourceBind;
import jnpf.util.RandomUtil;
import jnpf.util.third.DingTalkUtil;
import jnpf.util.third.QyWebChatUtil;
import jnpf.util.wxutil.HttpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统配置
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2019年9月27日 上午9:18
 */
@Tag(name = "系统配置",description = "SysConfig")
@RestController
@RequestMapping("/api/system/SysConfig")
public class SysConfigController extends SuperController<SysconfigService, SysConfigEntity> {

    @Autowired
    private SysconfigService sysconfigService;
    @Autowired
    private CheckLoginService checkLoginService;
    @Autowired
    private ConfigValueUtil configValueUtil;
    @Autowired
    private UserService userService;

    /**
     * 列表
     *
     * @return
     */
    @Operation(summary = "列表")
    @GetMapping
    public ActionResult<SysConfigModel> list() {
        List<SysConfigEntity> list = sysconfigService.getList("SysConfig");
        HashMap<String, String> map = new HashMap<>();
        for (SysConfigEntity sys : list) {
            map.put(sys.getFkey(), sys.getValue());
        }
        SysConfigModel sysConfigModel= JsonUtil.getJsonToBean(map,SysConfigModel.class);
        return ActionResult.success(sysConfigModel);
    }

    /**
     * 保存设置
     *
     * @param sysConfigModel 系统模型
     * @return
     */
    @Operation(summary = "更新系统配置")
    @Parameter(name = "sysConfigModel", description = "系统模型", required = true)
    @SaCheckPermission("system.sysConfig")
    @PutMapping
    public ActionResult save(@RequestBody SysConfigModel sysConfigModel) {
        List<SysConfigEntity> entitys = new ArrayList<>();
        Map<String, Object> map = JsonUtil.entityToMap(sysConfigModel);
        map.put("isLog","1");
        map.put("sysTheme","1");
        map.put("pageSize","30");
        map.put("lastLoginTime",1);
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            SysConfigEntity entity = new SysConfigEntity();
            entity.setId(RandomUtil.uuId());
            entity.setFkey(entry.getKey());
            entity.setValue(String.valueOf(entry.getValue()));
            entitys.add(entity);
        }
        sysconfigService.save(entitys);
        return ActionResult.success("操作成功");
    }

    /**
     * 获取BaseSystemInfo
     * @return
     */
    
    @NoDataSourceBind
    @GetMapping("/getInfo")
    public BaseSystemInfo getSysInfo(@RequestParam("tenantId") String tenantId) {
        //判断是否为多租户
        if (configValueUtil.isMultiTenancy()) {
            TenantDataSourceUtil.switchTenant(tenantId);
        }
        BaseSystemInfo sysInfo = sysconfigService.getSysInfo();
        return sysInfo;
    }

    
    @GetMapping("/getSysConfigInfo")
    public BaseSystemInfo getSysConfigInfo() {
        return sysconfigService.getSysInfo();
    }

    
    @GetMapping("/getValueByKey")
    public String getValueByKey(@RequestParam("keyStr") String keyStr) {
        return sysconfigService.getValueByKey(keyStr);
    }

    
    @NoDataSourceBind
    @GetMapping("/getSysConfigInfoByType")
    public List<SysConfigEntity> getSysConfigInfoByType(@RequestParam("type") String type, @RequestParam("tenantId") String tenantId) {
        //判断是否为多租户
        if (configValueUtil.isMultiTenancy()) {
            TenantDataSourceUtil.switchTenant(tenantId);
        }
        return sysconfigService.getList(type);
    }

    /**
     * 邮箱账户密码验证
     *
     * @param emailTestForm 邮箱测试模型
     * @return
     */
    @Operation(summary = "邮箱连接测试")
    @Parameter(name = "emailTestForm", description = "邮箱测试模型", required = true)
    @SaCheckPermission("system.sysConfig")
    @PostMapping("/Email/Test")
    public ActionResult checkLogin(@RequestBody EmailTestForm emailTestForm) {
        EmailConfigEntity entity = JsonUtil.getJsonToBean(emailTestForm, EmailConfigEntity.class);
        entity.setEmailSsl(Integer.valueOf(emailTestForm.getSsl()));
        String result = checkLoginService.checkLogin(entity);
        if ("true".equals(result)) {
            return ActionResult.success("验证成功");
        } else {
            return ActionResult.fail(result);
        }
    }


    //=====================================测试企业微信、钉钉的连接=====================================

    /**
     * 测试企业微信配置的连接功能
     * @param qyWebChatModel    企业微信模型
     * @param type              0-发送消息,1-同步组织
     * @return
     */
    @Operation(summary = "测试企业微信配置的连接")
    @Parameters({
            @Parameter(name = "type", description = "0-发送消息,1-同步组织", required = true),
            @Parameter(name = "qyWebChatModel", description = "企业微信模型", required = true)
    })
    @SaCheckPermission("system.sysConfig")
    @PostMapping("{type}/testQyWebChatConnect")
    public ActionResult testQyWebChatConnect(@PathVariable("type") String type, @RequestBody @Valid QyWebChatModel qyWebChatModel){
        JSONObject retMsg = new JSONObject();
        // 测试发送消息、组织同步的连接
        String corpId = qyWebChatModel.getQyhCorpId();
        String agentSecret = qyWebChatModel.getQyhAgentSecret();
        String corpSecret = qyWebChatModel.getQyhCorpSecret();
        // 测试发送消息的连接
        if ("0".equals(type)){
            retMsg = QyWebChatUtil.getAccessToken(corpId,agentSecret);
            if (HttpUtil.isWxError(retMsg)) {
                return ActionResult.fail("测试发送消息的连接失败："+retMsg.getString("errmsg"));
            }
            return ActionResult.success("测试发送消息连接成功");
        }else if ("1".equals(type)){
            retMsg = QyWebChatUtil.getAccessToken(corpId,corpSecret);
            if (HttpUtil.isWxError(retMsg)) {
                return ActionResult.fail("测试组织同步的连接失败："+retMsg.getString("errmsg"));
            }
            return ActionResult.success("测试组织同步连接成功");
        }
        return ActionResult.fail("测试连接类型错误");
    }

    /**
     * 测试钉钉配置的连接功能
     *
     * @param dingTalkModel 钉钉模型
     * @return
     */
    @Operation(summary = "测试钉钉配置的连接")
    @Parameters({
            @Parameter(name = "dingTalkModel", description = "钉钉模型", required = true)
    })
    @SaCheckPermission("system.sysConfig")
    @PostMapping("/testDingTalkConnect")
    public ActionResult testDingTalkConnect(@RequestBody @Valid DingTalkModel dingTalkModel) {
        JSONObject retMsg = new JSONObject();
        // 测试钉钉配置的连接
        String appKey = dingTalkModel.getDingSynAppKey();
        String appSecret = dingTalkModel.getDingSynAppSecret();
        String agentId = dingTalkModel.getDingAgentId();
        // 测试钉钉的连接
        retMsg = DingTalkUtil.getAccessToken(appKey,appSecret);
        if (!retMsg.getBoolean("code")) {
            return ActionResult.fail("测试钉钉连接失败："+retMsg.getString("error"));
        }

        return ActionResult.success("测试钉钉连接成功");
    }

    /**
     * 获取管理员集合
     *
     * @return
     */
    @Operation(summary = "获取管理员集合")
    @SaCheckPermission("system.sysConfig")
    @GetMapping("/getAdminList")
    public ActionResult<List<UserAdminVO>> getAdminList(){
        List<UserAdminVO> admins = JsonUtil.getJsonToList(userService.getAdminList(), UserAdminVO.class);
        return ActionResult.success(admins);
    }

    /**
     * 获取管理员集合
     *
     * @param userUpAdminForm 超级管理员设置表单参数
     * @return
     */
    @Operation(summary = "获取管理员集合")
    @Parameters({
            @Parameter(name = "userUpAdminForm", description = "超级管理员设置表单参数", required = true)
    })
    @SaCheckPermission("system.sysConfig")
    @PutMapping("/setAdminList")
    public ActionResult<String> setAdminList(@RequestBody UserUpAdminForm userUpAdminForm){
        userService.setAdminListByIds(userUpAdminForm.getAdminIds());
        return ActionResult.success(MsgCode.SU004.get());
    }

}
