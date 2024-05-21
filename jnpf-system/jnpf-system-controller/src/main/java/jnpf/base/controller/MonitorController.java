package jnpf.base.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jnpf.base.ActionResult;
import jnpf.base.model.monitor.MonitorListVO;
import jnpf.base.util.MonitorUtil;
import jnpf.util.JsonUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * 系统监控
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2019年9月27日 上午9:18
 */
@Tag(name = "系统监控", description = "Monitor")
@RestController
@RequestMapping("/api/system/Monitor")
public class MonitorController {

    /**
     * 系统监控
     *
     * @return
     */
    @Operation(summary = "系统监控")
    @SaCheckPermission("system.monitor")
    @GetMapping
    public ActionResult<MonitorListVO> list() {
        MonitorUtil monitorUtil = new MonitorUtil();
        MonitorListVO vo = JsonUtil.getJsonToBean(monitorUtil, MonitorListVO.class);
        vo.setTime(System.currentTimeMillis());
        return ActionResult.success(vo);
    }
}
