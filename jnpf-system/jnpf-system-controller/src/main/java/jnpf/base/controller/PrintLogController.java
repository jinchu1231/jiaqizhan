package jnpf.base.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jnpf.base.ActionResult;
import jnpf.base.UserInfo;
import jnpf.base.entity.PrintLogEntity;
import jnpf.base.model.printdev.vo.PrintLogVO;
import jnpf.base.model.printlog.PrintLogInfo;
import jnpf.base.model.printlog.PrintLogQuery;
import jnpf.base.service.PrintLogService;
import jnpf.base.vo.PaginationVO;
import jnpf.permission.entity.UserEntity;
import jnpf.permission.service.UserService;
import jnpf.util.JsonUtil;
import jnpf.util.RandomUtil;
import jnpf.util.UserProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Tag(name = "打印模板日志", description = "PrintLogController")
@RestController
@RequestMapping("/api/system/printLog")
public class PrintLogController {
    @Autowired
    private PrintLogService printLogService;

    @Autowired
    private UserProvider userProvider;

    @Autowired
    private UserService userService;

    /**
     * 获取列表
     *
     * @param page 分页模型
     * @return
     */
    @Operation(summary = "获取列表")
    @Parameters({
            @Parameter(name = "id", description = "打印模板ID", required = true)
    })
    @SaCheckPermission("system.printDev")
    @GetMapping("/{id}")
    public ActionResult<?> list(@PathVariable("id") String printId, PrintLogQuery page) {
        List<PrintLogEntity> records  = printLogService.getListId(printId, page);
        List<PrintLogVO> list = new ArrayList<>(records.size());
        PaginationVO paginationVO = JsonUtil.getJsonToBean(page, PaginationVO.class);
        // 转化名称
        List<String> collect = records.stream().map(PrintLogEntity::getCreatorUserId).filter(Objects::nonNull).collect(Collectors.toList());
        if (collect.size() > 0) {
            List<UserEntity> userEntityList = userService.getUserName(collect);
            Map<String, UserEntity> map = userEntityList.stream().collect(Collectors.toMap(UserEntity::getId, Function.identity()));
            for (PrintLogEntity record : records) {
                PrintLogVO vo = JsonUtil.getJsonToBean(record, PrintLogVO.class);
                UserEntity userEntity = map.get(record.getCreatorUserId());
                if (userEntity != null) {
                    vo.setPrintMan(userEntity.getRealName() + "/" + userEntity.getAccount());
                }
                vo.setPrintTime(ObjectUtil.isNotNull(record.getCreatorTime()) ? record.getCreatorTime().getTime() : null);
                list.add(vo);
            }
        }

        return ActionResult.page(list, paginationVO);
    }

    /**
     * 保存信息
     *
     * @param info 实体对象
     * @return
     */
    @Operation(summary = "保存信息")
    @Parameters({
            @Parameter(name = "info", description = "实体对象", required = true)
    })
    @SaCheckPermission("system.printDev")
    @PostMapping("save")
    public ActionResult<?> save(@RequestBody @Validated PrintLogInfo info) {
        PrintLogEntity printLogEntity = BeanUtil.copyProperties(info, PrintLogEntity.class);
        UserInfo userInfo = userProvider.get();

        printLogEntity.setId(RandomUtil.uuId());
        printLogEntity.setCreatorTime(new Date());
        printLogEntity.setCreatorUserId(userInfo.getUserId());
        printLogService.save(printLogEntity);
        return ActionResult.success("保存成功");
    }


}
