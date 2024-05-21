package jnpf.base.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.util.ObjectUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jnpf.base.ActionResult;
import jnpf.base.model.cacheManage.CacheManageInfoVO;
import jnpf.base.model.cacheManage.CacheManageListVO;
import jnpf.base.model.cacheManage.PaginationCacheManage;
import jnpf.base.vo.ListVO;
import jnpf.util.DateUtil;
import jnpf.util.RedisUtil;
import jnpf.util.StringUtil;
import jnpf.util.UserProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 缓存管理
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2019年9月27日 上午9:18
 */
@Tag(name = "缓存管理", description = "CacheManage")
@RestController
@RequestMapping("/api/system/CacheManage")
public class CacheManageController {

    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private UserProvider userProvider;

    /**
     * 获取缓存列表
     *
     * @param page 关键词
     * @return
     */
    @Operation(summary = "获取缓存列表")
    @SaCheckPermission("system.cache")
    @GetMapping
    public ActionResult<ListVO<CacheManageListVO>> getList(PaginationCacheManage page) {
        String tenantId = userProvider.get().getTenantId();
        List<CacheManageListVO> list = new ArrayList<>();
        Set<String> data = redisUtil.getAllKeys();
        for (String key : data) {
            try {
                if (!StringUtil.isEmpty(tenantId) && key.contains(tenantId)) {
                    CacheManageListVO model = new CacheManageListVO();
                    model.setName(key);
                    model.setCacheSize(String.valueOf(redisUtil.getString(key)).getBytes().length);
                    model.setOverdueTime(new Date((DateUtil.getTime(new Date()) + redisUtil.getLiveTime(key)) * 1000).getTime());
                    list.add(model);
                } else if (StringUtil.isEmpty(tenantId)) {
                    CacheManageListVO model = new CacheManageListVO();
                    model.setName(key);
                    model.setCacheSize(String.valueOf(redisUtil.getString(key)).getBytes().length);
                    model.setOverdueTime(new Date((DateUtil.getTime(new Date()) + redisUtil.getLiveTime(key)) * 1000).getTime());
                    list.add(model);
                }
            }catch (Exception e){
            }
        }
        list = list.stream().sorted(Comparator.comparing(CacheManageListVO::getOverdueTime)).collect(Collectors.toList());
        if (StringUtil.isNotEmpty(page.getKeyword())) {
            list = list.stream().filter(t -> t.getName().contains(page.getKeyword())).collect(Collectors.toList());
        }
        if (ObjectUtil.isNotNull(page.getOverdueStartTime()) && ObjectUtil.isNotNull(page.getOverdueEndTime())) {
            list = list.stream().filter(t -> t.getOverdueTime() >= page.getOverdueStartTime() && t.getOverdueTime() <= page.getOverdueEndTime()).collect(Collectors.toList());
        }
        ListVO<CacheManageListVO> vo = new ListVO<>();
        vo.setList(list);
        return ActionResult.success(vo);
    }

    /**
     * 获取缓存信息
     *
     * @param name 主键值
     * @return
     */
    @Operation(summary = "获取缓存信息")
    @Parameter(name = "name", description = "主键值", required = true)
    @SaCheckPermission("system.cache")
    @GetMapping("/{name}")
    public ActionResult<CacheManageInfoVO> info(@PathVariable("name") String name) {
        String json = String.valueOf(redisUtil.getString(name));
        CacheManageInfoVO vo=new CacheManageInfoVO();
        vo.setName(name);
        vo.setValue(json);
        return ActionResult.success(vo);
    }

    /**
     * 清空所有缓存
     *
     * @return
     */
    @Operation(summary = "清空所有缓存")
    @SaCheckPermission("system.cache")
    @PostMapping("/Actions/ClearAll")
    public ActionResult clearAll() {
        String tenantId = userProvider.get().getTenantId();
        if ("".equals(tenantId)) {
            Set<String> keys = redisUtil.getAllKeys();
            for (String key : keys) {
                redisUtil.remove(key);
            }
        } else {
            Set<String> data = redisUtil.getAllKeys();
            String clientKey = UserProvider.getToken();
            System.out.println(clientKey);
            for (String key : data) {
                if (key.contains(tenantId)) {
                    redisUtil.remove(key);
                }
            }
        }
        return ActionResult.success("清理成功");
    }

    /**
     * 清空单个缓存
     *
     * @param name 主键值
     * @return
     */
    @Operation(summary = "清空单个缓存")
    @Parameter(name = "name", description = "主键值", required = true)
    @SaCheckPermission("system.cache")
    @DeleteMapping("/{name}")
    public ActionResult clear(@PathVariable("name") String name) {
        redisUtil.remove(name);
        return ActionResult.success("清空成功");
    }
}
