package jnpf.integrate.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jnpf.base.ActionResult;
import jnpf.base.controller.SuperController;
import jnpf.base.vo.DownloadVO;
import jnpf.base.vo.PageListVO;
import jnpf.base.vo.PaginationVO;
import jnpf.config.ConfigValueUtil;
import jnpf.constant.MsgCode;
import jnpf.emnus.ModuleTypeEnum;
import jnpf.exception.WorkFlowException;
import jnpf.integrate.entity.IntegrateEntity;
import jnpf.integrate.model.integrate.IntegrateCrForm;
import jnpf.integrate.model.integrate.IntegrateInfoVO;
import jnpf.integrate.model.integrate.IntegrateListVO;
import jnpf.integrate.model.integrate.IntegratePagination;
import jnpf.integrate.model.integrate.IntegrateUpForm;
import jnpf.integrate.service.IntegrateService;
import jnpf.permission.entity.UserEntity;
import jnpf.permission.service.UserService;
import jnpf.util.DataFileExport;
import jnpf.util.FileUtil;
import jnpf.util.JsonUtil;
import jnpf.util.RandomUtil;
import jnpf.util.UserProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Tag(name = "集成助手", description = "Integrate")
@RestController
@RequestMapping("/api/visualdev/Integrate")
public class IntegrateController extends SuperController<IntegrateService, IntegrateEntity> {

    @Autowired
    private UserService userService;
    @Autowired
    private UserProvider userProvider;
    @Autowired
    private IntegrateService integrateService;
    @Autowired
    private DataFileExport fileExport;
    @Autowired
    private ConfigValueUtil configValueUtil;

    /**
     * 列表
     *
     * @return
     */
    @Operation(summary = "列表")
    @GetMapping
    public ActionResult<PageListVO<IntegrateListVO>> list(IntegratePagination pagination) {
        List<IntegrateEntity> data = integrateService.getList(pagination);
        List<String> userId = data.stream().map(t -> t.getCreatorUserId()).collect(Collectors.toList());
        List<UserEntity> userEntities = userService.getUserName(userId);
        List<IntegrateListVO> resultList = new ArrayList<>();
        for (IntegrateEntity entity : data) {
            IntegrateListVO vo = JsonUtil.getJsonToBean(entity, IntegrateListVO.class);
            UserEntity creatorUser = userEntities.stream().filter(t -> t.getId().equals(entity.getCreatorUserId())).findFirst().orElse(null);
            vo.setCreatorUser(creatorUser != null ? creatorUser.getRealName() + "/" + creatorUser.getAccount() : "");
            resultList.add(vo);
        }
        PaginationVO paginationVO = JsonUtil.getJsonToBean(pagination, PaginationVO.class);
        return ActionResult.page(resultList, paginationVO);
    }

    /**
     * 信息
     *
     * @param id 主键值
     * @return
     */
    @Operation(summary = "获取信息")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true)
    })
    @GetMapping("/{id}")
    public ActionResult info(@PathVariable("id") String id) {
        IntegrateEntity entity = integrateService.getInfo(id);
        if (entity == null) {
            return ActionResult.fail("数据不存在");
        }
        IntegrateInfoVO vo = JsonUtil.getJsonToBean(entity, IntegrateInfoVO.class);
        return ActionResult.success(vo);
    }

    /**
     * 新建
     *
     * @param integrateCrForm 实体对象
     * @return
     */
    @Operation(summary = "添加")
    @Parameters({
            @Parameter(name = "integrateCrForm", description = "实体对象", required = true)
    })
    @PostMapping
    public ActionResult create(@RequestBody @Valid IntegrateCrForm integrateCrForm) {
        IntegrateEntity entity = JsonUtil.getJsonToBean(integrateCrForm, IntegrateEntity.class);
        if (integrateService.isExistByFullName(entity.getFullName(), entity.getId())) {
            return ActionResult.fail("名称不能重复");
        }
        if (integrateService.isExistByEnCode(entity.getEnCode(), entity.getId())) {
            return ActionResult.fail("编码不能重复");
        }
        String id = RandomUtil.uuId();
        entity.setId(id);
        integrateService.create(entity);
        return ActionResult.success("新建成功", id);
    }

    /**
     * 更新
     *
     * @param id 主键值
     * @return
     */
    @Operation(summary = "修改")
    @Parameters({
            @Parameter(name = "id", description = "主键值", required = true),
            @Parameter(name = "integrateUpForm", description = "实体对象", required = true)
    })
    @PutMapping("/{id}")
    public ActionResult update(@PathVariable("id") String id, @RequestBody IntegrateUpForm integrateUpForm) {
        IntegrateEntity positionEntity = integrateService.getInfo(id);
        if (positionEntity == null) {
            return ActionResult.fail(MsgCode.FA002.get());
        }
        IntegrateEntity entity = JsonUtil.getJsonToBean(integrateUpForm, IntegrateEntity.class);
        if (integrateService.isExistByFullName(entity.getFullName(), id)) {
            return ActionResult.fail("名称不能重复");
        }
        if (integrateService.isExistByEnCode(entity.getEnCode(), id)) {
            return ActionResult.fail("编码不能重复");
        }
        boolean flag = integrateService.update(id, entity,false);
        if (flag == false) {
            return ActionResult.fail("更新失败，数据不存在");
        }
        return ActionResult.success("更新成功", id);
    }

    /**
     * 删除
     *
     * @param id 主键值
     * @return
     */
    @Operation(summary = "删除")
    @Parameters({
            @Parameter(name = "id", description = "主键值", required = true)
    })
    @DeleteMapping("/{id}")
    public ActionResult delete(@PathVariable("id") String id) {
        IntegrateEntity entity = integrateService.getInfo(id);
        if (entity != null) {
            integrateService.delete(entity);
            return ActionResult.success("删除成功");
        }
        return ActionResult.fail("删除失败，数据不存在");
    }

    /**
     * 复制功能
     *
     * @param id 主键值
     * @return
     */
    @Operation(summary = "复制功能")
    @Parameters({
            @Parameter(name = "id", description = "主键"),
    })
    @PostMapping("/{id}/Actions/Copy")
    public ActionResult copyInfo(@PathVariable("id") String id) throws Exception {
        IntegrateEntity entity = integrateService.getInfo(id);
        entity.setEnabledMark(0);
        String copyNum = UUID.randomUUID().toString().substring(0, 5);
        entity.setFullName(entity.getFullName() + ".副本" + copyNum);
        entity.setLastModifyTime(null);
        entity.setLastModifyUserId(null);
        entity.setId(RandomUtil.uuId());
        entity.setEnCode(entity.getEnCode() + copyNum);
        entity.setCreatorTime(new Date());
        entity.setEnabledMark(0);
        entity.setCreatorUserId(userProvider.get().getUserId());
        integrateService.create(entity);
        return ActionResult.success(MsgCode.SU007.get());
    }

    /**
     * 更新功能状态
     *
     * @param id 主键值
     * @return
     */
    @Operation(summary = "更新功能状态")
    @Parameters({
            @Parameter(name = "id", description = "主键"),
    })
    @PutMapping("/{id}/Actions/State")
    public ActionResult update(@PathVariable("id") String id) {
        IntegrateEntity entity = integrateService.getInfo(id);
        if (entity != null) {
            if (entity.getEnabledMark() == null || "1".equals(String.valueOf(entity.getEnabledMark()))) {
                entity.setEnabledMark(0);
            } else {
                entity.setEnabledMark(1);
            }
            boolean flag = integrateService.update(entity.getId(), entity,true);
            if (flag == false) {
                return ActionResult.fail("更新失败，任务不存在");
            }
        }
        return ActionResult.success(MsgCode.SU004.get());
    }

    /**
     * 导出
     *
     * @param id 主键
     * @return
     */
    @Operation(summary = "导出")
    @PostMapping("/{id}/Actions/Export")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true),
    })
    public ActionResult<DownloadVO> exportData(@PathVariable("id") String id) {
        IntegrateEntity entity = integrateService.getInfo(id);
        DownloadVO downloadVO = fileExport.exportFile(entity, configValueUtil.getTemporaryFilePath(), entity.getFullName(), ModuleTypeEnum.BASE_INTEGRATE.getTableName());
        return ActionResult.success(downloadVO);
    }

    /**
     * 导入
     *
     * @param file 文件
     * @return
     */
    @Operation(summary = "导入")
    @PostMapping(value = "/Actions/Import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ActionResult ImportData(@RequestPart("file") MultipartFile file,@RequestParam("type") Integer type) throws WorkFlowException {
        //判断是否为.json结尾
        if (FileUtil.existsSuffix(file, ModuleTypeEnum.BASE_INTEGRATE.getTableName())) {
            return ActionResult.fail(MsgCode.IMP002.get());
        }
        try {
            String fileContent = FileUtil.getFileContent(file);
            IntegrateEntity entity = JsonUtil.getJsonToBean(fileContent, IntegrateEntity.class);
            return integrateService.ImportData(entity, type);
        } catch (Exception e) {
            throw new WorkFlowException("导入失败，数据有误");
        }
    }

}
