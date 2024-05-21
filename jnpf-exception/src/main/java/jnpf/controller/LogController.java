package jnpf.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jnpf.base.ActionResult;
import jnpf.base.controller.SuperController;
import jnpf.base.vo.PageListVO;
import jnpf.base.vo.PaginationVO;
import jnpf.config.ConfigValueUtil;
import jnpf.constant.MsgCode;
import jnpf.entity.LogEntity;
import jnpf.model.ErrorLogVO;
import jnpf.model.HandleLogVO;
import jnpf.model.LogDelForm;
import jnpf.model.LogInfoVO;
import jnpf.model.LoginLogVO;
import jnpf.model.PaginationLogModel;
import jnpf.model.RequestLogVO;
import jnpf.model.WriteLogModel;
import jnpf.service.LogService;
import jnpf.util.JsonUtil;
import jnpf.util.NoDataSourceBind;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.map.HashedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 系统日志
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2019年9月27日 上午9:18
 */
@Slf4j
@Tag(name = "系统日志", description = "Log")
@RestController
@RequestMapping("/api/system/Log")
public class LogController extends SuperController<LogService, LogEntity> {

    @Autowired
    private LogService logService;
    @Autowired
    private ConfigValueUtil configValueUtil;

    /**
     * 获取系统日志信息
     *
     * @param pagination 主键值分类 1：登录日志，2.访问日志，3.操作日志，4.异常日志，5.请求日志
     * @return
     */
    @Operation(summary = "获取系统日志列表")
    @Parameters({
            @Parameter(name = "category", description = "分类", required = true)
    })
    @SaCheckPermission("system.log")
    @GetMapping
    public ActionResult getInfoList(PaginationLogModel pagination) {
        List<LogEntity> list = logService.getList(pagination.getCategory(), pagination);
        PaginationVO paginationVO = JsonUtil.getJsonToBean(pagination, PaginationVO.class);
        switch (pagination.getCategory()) {
            case 1:
                List<LoginLogVO> loginLogVOList = JsonUtil.getJsonToList(list, LoginLogVO.class);
                for (int i = 0; i < loginLogVOList.size(); i++) {
                    loginLogVOList.get(i).setAbstracts(list.get(i).getDescription());
                }
                return ActionResult.page(loginLogVOList, paginationVO);
            case 3:
                List<HandleLogVO> handleLogVOList = JsonUtil.getJsonToList(list, HandleLogVO.class);
                return ActionResult.page(handleLogVOList, paginationVO);
            case 4:
                List<ErrorLogVO> errorLogVOList = JsonUtil.getJsonToList(list, ErrorLogVO.class);
                return ActionResult.page(errorLogVOList, paginationVO);
            case 5:
                List<RequestLogVO> requestLogVOList = JsonUtil.getJsonToList(list, RequestLogVO.class);
                return ActionResult.page(requestLogVOList, paginationVO);
            default:
                return ActionResult.fail("获取失败");
        }
    }

    /**
     * 获取系统日志信息
     *
     * @param id 主键值
     * @return
     */
    @Operation(summary = "获取系统日志信息")
    @Parameters({
            @Parameter(name = "category", description = "分类", required = true)
    })
    @SaCheckPermission("system.log")
    @GetMapping("/{id}")
    public ActionResult<LogInfoVO> getInfoList(@PathVariable("id") String id) {
        LogEntity entity = logService.getInfo(id);
        if (entity == null) {
            return ActionResult.fail(MsgCode.FA001.get());
        }
        LogInfoVO vo = JsonUtil.getJsonToBean(entity, LogInfoVO.class);
        return ActionResult.success(vo);
    }

    /**
     * 批量删除系统日志
     *
     * @param logDelForm 批量删除日志模型
     * @return
     */
    @Operation(summary = "批量删除系统日志")
    @Parameters({
            @Parameter(name = "logDelForm", description = "批量删除日志模型", required = true)
    })
    @SaCheckPermission("system.log")
    @DeleteMapping
    public ActionResult delete(@RequestBody LogDelForm logDelForm) {
        boolean flag = logService.delete(logDelForm.getIds());
        if (!flag) {
            return ActionResult.fail(MsgCode.FA003.get());
        }
        return ActionResult.success(MsgCode.SU003.get());
    }

    /**
     * 一键清空操作日志
     *
     * @param type 分类
     * @return
     */
    @Operation(summary = "一键清空操作日志")
    @Parameters({
            @Parameter(name = "type", description = "分类", required = true)
    })
    @SaCheckPermission("system.log")
    @DeleteMapping("/{type}")
    public ActionResult deleteHandelLog(@PathVariable("type") String type) {
        logService.deleteHandleLog(type, null);
        return ActionResult.success(MsgCode.SU005.get());
    }

    /**
     * 一键清空登陆日志
     *
     * @return
     */
    @Operation(summary = "一键清空登陆日志")
    @SaCheckPermission("system.log")
    @DeleteMapping("/deleteLoginLog")
    public ActionResult deleteLoginLog() {
        logService.deleteHandleLog("1", 1);
        return ActionResult.success(MsgCode.SU005.get());
    }

    /**
     * 获取菜单名
     *
     * @return
     */
    @Operation(summary = "获取菜单名")
    @SaCheckPermission("system.log")
    @GetMapping("/ModuleName")
    public ActionResult<List<Map<String, String>>> moduleName() {
        List<Map<String, String>> list = new ArrayList<> (16);
        Set<String> set = logService.queryList();
        for (String moduleName : set) {
            Map<String, String> map = new HashedMap<>(1);
            map.put("moduleName", moduleName);
            list.add(map);
        }
        return ActionResult.success(list);
    }


    /**
     * 写入日志
     *
     * @param writeLogModel
     */

    @NoDataSourceBind
    @PostMapping("/writeLogAsync")
    public void writeLogAsync(@RequestBody WriteLogModel writeLogModel) {
        try {
            logService.writeLogAsync(writeLogModel.getUserId(), writeLogModel.getUserName(), writeLogModel.getAbstracts(), writeLogModel.getUserInfo(), writeLogModel.getLoginMark(), writeLogModel.getLoginType(), writeLogModel.getRequestDuration());
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    /**
     * 写入请求日志
     */

    @PostMapping("/writeLogRequest")
    public void writeLogRequest(@RequestBody LogEntity logEntity) {
        try {
            logService.save(logEntity);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }


    @PostMapping("/getList")
    public PageListVO<LogEntity> getList(@RequestBody PaginationLogModel pagination) {
        List<LogEntity> data = logService.getList(pagination.getCategory(), pagination);
        PageListVO pageListVO = new PageListVO();
        pageListVO.setList(data);
        pageListVO.setPagination(JsonUtil.getJsonToBean(pagination, PaginationVO.class));
        return pageListVO;
    }

}
