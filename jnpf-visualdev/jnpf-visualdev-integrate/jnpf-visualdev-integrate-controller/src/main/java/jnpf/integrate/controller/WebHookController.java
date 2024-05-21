package jnpf.integrate.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jnpf.base.ActionResult;
import jnpf.config.ConfigValueUtil;
import jnpf.constant.MsgCode;
import jnpf.database.util.TenantDataSourceUtil;
import jnpf.exception.WorkFlowException;
import jnpf.integrate.entity.IntegrateEntity;
import jnpf.integrate.model.integrate.WebHookInfoVo;
import jnpf.integrate.service.IntegrateService;
import jnpf.integrate.util.IntegrateUtil;
import jnpf.util.NoDataSourceBind;
import jnpf.util.RedisUtil;
import jnpf.util.ServletUtil;
import jnpf.util.StringUtil;
import jnpf.util.UserProvider;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Tag(name = "webhook触发", description = "WebHook")
@RestController
@RequestMapping("/api/visualdev/Hooks")
public class WebHookController {

    @Autowired
    private IntegrateService integrateService;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private ConfigValueUtil configValueUtil;
    @Autowired
    private IntegrateUtil integrateUtil;
    @Autowired
    private UserProvider userProvider;

    private static final String WEBHOOK_RED_KEY = "webhookencode";

    private static long DEFAULT_CACHE_TIME = 60 * 5;

    @Operation(summary = "数据接收接口")
    @Parameters({
            @Parameter(name = "id", description = "base64转码id", required = true),
            @Parameter(name = "tenantId", description = "租户id", required = false)
    })
    @PostMapping("/{id}")
    @NoDataSourceBind
    public ActionResult webhookTrigger(@PathVariable("id") String id,
                                       @RequestParam(value = "tenantId", required = false) String tenantId,
                                       @RequestBody Map<String, Object> body) throws WorkFlowException {
        String idReal = new String(Base64.decodeBase64(id.getBytes(StandardCharsets.UTF_8)));
        if (configValueUtil.isMultiTenancy()) {
            // 判断是不是从外面直接请求
            if (StringUtil.isNotEmpty(tenantId)) {
                //切换成租户库
                try {
                    TenantDataSourceUtil.switchTenant(tenantId);
                } catch (Exception e) {
                    return ActionResult.fail(MsgCode.LOG105.get());
                }
            }
        }
        integrateUtil.integrate(idReal, tenantId, body);
        return ActionResult.success();
    }

    @Operation(summary = "获取webhookUrl")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true)
    })
    @GetMapping("/getUrl")
    public ActionResult getWebhookUrl(@RequestParam("id") String id) {
        String enCodeBase64 = new String(Base64.encodeBase64(id.getBytes(StandardCharsets.UTF_8)));
        String randomStr = UUID.randomUUID().toString().substring(0, 5);
        WebHookInfoVo vo = new WebHookInfoVo();
        vo.setEnCodeStr(enCodeBase64);
        vo.setRandomStr(randomStr);
        vo.setWebhookUrl("/api/visualdev/Hooks/" + enCodeBase64);
        vo.setRequestUrl("/api/visualdev/Hooks/" + enCodeBase64 + "/params/" + randomStr);
        return ActionResult.success(vo);
    }

    @Operation(summary = "通过get接口获取参数")
    @Parameters({
            @Parameter(name = "id", description = "base64转码id", required = true),
            @Parameter(name = "randomStr", description = "获取webhookUrl提供的随机字符", required = true)
    })
    @GetMapping("/{id}/params/{randomStr}")
    @NoDataSourceBind
    public ActionResult getWebhookParams(@PathVariable("id") String id,
                                         @PathVariable("randomStr") String randomStr) throws WorkFlowException {
        insertRedis(id, randomStr, new HashMap<>());
        return ActionResult.success();
    }

    @Operation(summary = "通过post接口获取参数")
    @Parameters({
            @Parameter(name = "id", description = "base64转码id", required = true),
            @Parameter(name = "randomStr", description = "获取webhookUrl提供的随机字符", required = true)
    })
    @PostMapping("/{id}/params/{randomStr}")
    @NoDataSourceBind
    public ActionResult postWebhookParams(@PathVariable("id") String id,
                                          @PathVariable("randomStr") String randomStr,
                                          @RequestBody Map<String, Object> obj) throws WorkFlowException {
        insertRedis(id, randomStr, new HashMap<>(obj));
        return ActionResult.success();
    }

    /**
     * 助手id查询信息，写入缓存
     *
     * @param id
     * @param randomStr
     * @param resultMap
     * @throws WorkFlowException
     */
    private void insertRedis(String id, String randomStr, Map<String, Object> resultMap) throws WorkFlowException {
        String idReal = new String(Base64.decodeBase64(id.getBytes(StandardCharsets.UTF_8)));
        String key1 = WEBHOOK_RED_KEY + "_" + idReal + "_" + randomStr;
        if (!redisUtil.exists(key1)) {
            throw new WorkFlowException("路径错误");
        }
        String tenantId = redisUtil.getString(key1).toString();

        if (configValueUtil.isMultiTenancy()) {
            // 判断是不是从外面直接请求
            if (StringUtil.isNotEmpty(tenantId)) {
                //切换成租户库
                try {
                    TenantDataSourceUtil.switchTenant(tenantId);
                } catch (Exception e) {
                    throw new WorkFlowException(MsgCode.LOG105.get());
                }
            }
        }
        IntegrateEntity entity = integrateService.getInfo(idReal);
        if (Objects.equals(entity.getEnabledMark(), 0)) {
            throw new WorkFlowException("集成助手被禁用");
        }
        Map<String, Object> parameterMap = new HashMap<>(ServletUtil.getRequest().getParameterMap());
        for (String key : parameterMap.keySet()) {
            String[] parameterValues = ServletUtil.getRequest().getParameterValues(key);
            if (parameterValues.length == 1) {
                parameterMap.put(key, parameterValues[0]);
            } else {
                parameterMap.put(key, parameterValues);
            }
        }
        resultMap.putAll(parameterMap);
        if (resultMap.keySet().size() > 0) {
            redisUtil.insert(WEBHOOK_RED_KEY + "_" + randomStr, resultMap, DEFAULT_CACHE_TIME);
            redisUtil.remove(key1);
        }
    }

    @Operation(summary = "请求参数添加触发接口")
    @Parameters({
            @Parameter(name = "id", description = "base64转码id", required = true),
            @Parameter(name = "randomStr", description = "获取webhookUrl提供的随机字符", required = true)
    })
    @GetMapping("/{id}/start/{randomStr}")
    public ActionResult start(@PathVariable("id") String id,
                              @PathVariable("randomStr") String randomStr) {
        redisUtil.remove(WEBHOOK_RED_KEY + "_" + randomStr);
        redisUtil.insert(WEBHOOK_RED_KEY + "_" + id + "_" + randomStr, userProvider.get().getTenantId(), DEFAULT_CACHE_TIME);
        return ActionResult.success();
    }

    @Operation(summary = "获取缓存的接口参数")
    @Parameters({
            @Parameter(name = "randomStr", description = "获取webhookUrl提供的随机字符", required = true)
    })
    @GetMapping("/getParams/{randomStr}")
    public ActionResult getRedisParams(@PathVariable("randomStr") String randomStr) {
        Map<String, Object> mapRedis = new HashMap<>();
        String key = WEBHOOK_RED_KEY + "_" + randomStr;
        if (redisUtil.exists(key)) {
            mapRedis = redisUtil.getMap(key);
        }
        List<Map<String, Object>> list = new ArrayList<>();
        for (String redisKey : mapRedis.keySet()) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", redisKey);
            map.put("fullName", mapRedis.get(redisKey));
            list.add(map);
        }
        return ActionResult.success(list);
    }
}
