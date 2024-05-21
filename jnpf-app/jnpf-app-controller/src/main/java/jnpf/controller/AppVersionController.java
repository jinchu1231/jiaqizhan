package jnpf.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jnpf.base.ActionResult;
import jnpf.base.service.SysconfigService;
import jnpf.util.NoDataSourceBind;
import org.apache.commons.collections4.map.HashedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 获取AppVersion
 *
 * @author ：JNPF开发平台组
 * @version: V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date ：2022/3/31 11:26
 */
@Tag(name = "获取APP版本号", description = "AppVersion")
@RestController
@RequestMapping("/api/app")
public class AppVersionController {

    @Autowired
    private SysconfigService sysconfigService;

    /**
     * 判断是否需要验证码
     *
     * @return
     */
    @NoDataSourceBind
    @Operation(summary = "判断是否需要验证码")
    @GetMapping("/Version")
    public ActionResult getAppVersion() {
        String sysVersion = sysconfigService.getSysInfo().getSysVersion();
        Map<String, String> map = new HashedMap<>();
        map.put("sysVersion", sysVersion);
        return ActionResult.success(map);
    }
}
