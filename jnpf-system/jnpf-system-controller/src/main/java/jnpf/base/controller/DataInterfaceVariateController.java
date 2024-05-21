package jnpf.base.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jnpf.base.ActionResult;
import jnpf.base.Page;
import jnpf.base.entity.DataInterfaceEntity;
import jnpf.base.entity.DataInterfaceVariateEntity;
import jnpf.base.model.datainterfacevariate.DataInterfaceVariateListVO;
import jnpf.base.model.datainterfacevariate.DataInterfaceVariateModel;
import jnpf.base.model.datainterfacevariate.DataInterfaceVariateSelectorVO;
import jnpf.base.model.datainterfacevariate.DataInterfaceVariateVO;
import jnpf.base.service.DataInterfaceService;
import jnpf.base.service.DataInterfaceVariateService;
import jnpf.base.vo.DownloadVO;
import jnpf.base.vo.ListVO;
import jnpf.config.ConfigValueUtil;
import jnpf.constant.MsgCode;
import jnpf.emnus.ModuleTypeEnum;
import jnpf.exception.DataException;
import jnpf.permission.entity.UserEntity;
import jnpf.permission.service.UserService;
import jnpf.util.DataFileExport;
import jnpf.util.FileUtil;
import jnpf.util.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 数据接口变量
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2021-03-15 10:29
 */
@Tag(name = "数据接口变量", description = "DataInterfaceVariate")
@RestController
@RequestMapping(value = "/api/system/DataInterfaceVariate")
public class DataInterfaceVariateController {

    @Autowired
    private DataInterfaceVariateService dataInterfaceVariateService;
    @Autowired
    private UserService userService;
    @Autowired
    private DataInterfaceService dataInterfaceService;
    @Autowired
    private DataFileExport fileExport;
    @Autowired
    private ConfigValueUtil configValueUtil;

    /**
     * 获取数据接口变量
     *
     * @param id 主键
     * @return
     */
    @Operation(summary = "获取数据接口变量")
    @SaCheckPermission("systemData.dataInterface")
    @Parameter(name = "id", description = "自然主键")
    @GetMapping("/{id}")
    public ActionResult<ListVO<DataInterfaceVariateListVO>> list(@PathVariable("id") String id, Page page) {
        List<DataInterfaceVariateListVO> list = new ArrayList<>();
        List<DataInterfaceVariateEntity> data = dataInterfaceVariateService.getList(id, page);
        data.forEach(t -> {
            DataInterfaceVariateListVO vo = new DataInterfaceVariateListVO();
            vo.setId(t.getId());
            vo.setInterfaceId(t.getInterfaceId());
            vo.setFullName(t.getFullName());
            vo.setValue(t.getValue());
            vo.setCreatorTime(t.getCreatorTime() != null ? t.getCreatorTime().getTime() : null);
            vo.setLastModifyTime(t.getLastModifyTime() != null ? t.getLastModifyTime().getTime() : null);
            UserEntity userEntity = userService.getInfo(t.getCreatorUserId());
            vo.setCreatorUser(userEntity != null ? userEntity.getRealName() + "/" + userEntity.getAccount() : null);
            list.add(vo);
        });
        ListVO<DataInterfaceVariateListVO> listVO = new ListVO<>();
        listVO.setList(list);
        return ActionResult.success(listVO);
    }

    /**
     * 下拉列表
     *
     * @return
     */
    @Operation(summary = "下拉列表")
    @SaCheckPermission("systemData.dataInterface")
    @GetMapping("/Selector")
    public ActionResult<List<DataInterfaceVariateSelectorVO>> selector() {
        List<DataInterfaceVariateEntity> data = dataInterfaceVariateService.getList(null, null);
        List<DataInterfaceEntity> list = dataInterfaceService.getList(data.stream().map(DataInterfaceVariateEntity::getInterfaceId).collect(Collectors.toList()));
        List<DataInterfaceVariateSelectorVO> jsonToList = JsonUtil.getJsonToList(list, DataInterfaceVariateSelectorVO.class);
        jsonToList.forEach(t -> {
            t.setParentId("-1");
            t.setType(0);
        });
        jsonToList.forEach(t -> {
            List<DataInterfaceVariateEntity> collect = data.stream().filter(variateEntity -> t.getId().equals(variateEntity.getInterfaceId())).collect(Collectors.toList());
            List<DataInterfaceVariateSelectorVO> selectorVOS = JsonUtil.getJsonToList(collect, DataInterfaceVariateSelectorVO.class);
            selectorVOS.forEach(selectorVO -> {
                selectorVO.setParentId(t.getId());
                selectorVO.setType(1);
            });
            t.setChildren(selectorVOS);
        });
        return ActionResult.success(jsonToList);
    }

    /**
     * 详情
     *
     * @param id 主键
     * @return
     */
    @Operation(summary = "详情")
    @SaCheckPermission("systemData.dataInterface")
    @Parameter(name = "id", description = "自然主键")
    @GetMapping("/{id}/Info")
    public ActionResult<DataInterfaceVariateVO> info(@PathVariable("id") String id) {
        DataInterfaceVariateEntity entity = dataInterfaceVariateService.getInfo(id);
        DataInterfaceVariateVO vo = JsonUtil.getJsonToBean(entity, DataInterfaceVariateVO.class);
        return ActionResult.success(vo);
    }

    /**
     * 导出
     *
     * @param id 自然主键
     * @return
     */
    @Operation(summary = "导出")
    @SaCheckPermission("systemData.dataInterface")
    @Parameter(name = "id", description = "自然主键")
    @GetMapping("/{id}/Actions/Export")
    public ActionResult<DownloadVO> export(@PathVariable("id") String id) {
        DataInterfaceVariateEntity entity = dataInterfaceVariateService.getInfo(id);
        //导出文件
        DownloadVO downloadVO = fileExport.exportFile(entity, configValueUtil.getTemporaryFilePath(), entity.getFullName(), ModuleTypeEnum.SYSTEM_DATAINTEFASE_VARIATE.getTableName());
        return ActionResult.success(downloadVO);
    }

    /**
     * 添加
     *
     * @param dataInterfaceVariateModel 模型
     * @return
     */
    @Operation(summary = "添加")
    @SaCheckPermission("systemData.dataInterface")
    @Parameter(name = "dataInterfaceVariateModel", description = "模型")
    @PostMapping
    public ActionResult<String> create(@RequestBody DataInterfaceVariateModel dataInterfaceVariateModel) {
        DataInterfaceVariateEntity entity = JsonUtil.getJsonToBean(dataInterfaceVariateModel, DataInterfaceVariateEntity.class);
        if (entity.getFullName().contains("@")) {
            return ActionResult.fail("变量名不能包含敏感字符");
        }
        if (dataInterfaceVariateService.isExistByFullName(entity)) {
            return ActionResult.fail("变量名已存在");
        }
        dataInterfaceVariateService.create(entity);
        return ActionResult.success(MsgCode.SU001.get());
    }

    /**
     * 修改
     *
     * @param id 自然主键
     * @param dataInterfaceVariateModel 模型
     * @return
     */
    @Operation(summary = "修改")
    @SaCheckPermission("systemData.dataInterface")
    @Parameters({
            @Parameter(name = "id", description = "自然主键"),
            @Parameter(name = "dataInterfaceVariateModel", description = "模型")
    })
    @PutMapping("/{id}")
    public ActionResult<String> update(@PathVariable("id") String id, @RequestBody DataInterfaceVariateModel dataInterfaceVariateModel) {
        DataInterfaceVariateEntity entity = JsonUtil.getJsonToBean(dataInterfaceVariateModel, DataInterfaceVariateEntity.class);
        if (entity.getFullName().contains("@")) {
            return ActionResult.fail("变量名不能包含敏感字符");
        }
        entity.setId(id);
        if (dataInterfaceVariateService.isExistByFullName(entity)) {
            return ActionResult.fail("变量名已存在");
        }
        dataInterfaceVariateService.update(entity);
        return ActionResult.success(MsgCode.SU004.get());
    }

    /**
     * 删除
     *
     * @param id 自然主键
     * @return
     */
    @Operation(summary = "删除")
    @SaCheckPermission("systemData.dataInterface")
    @Parameters({
            @Parameter(name = "id", description = "自然主键")
    })
    @DeleteMapping("/{id}")
    public ActionResult<String> delete(@PathVariable("id") String id) {
        DataInterfaceVariateEntity entity = dataInterfaceVariateService.getInfo(id);
        if (entity == null) {
            return ActionResult.fail(MsgCode.FA001.get());
        }
        dataInterfaceVariateService.delete(entity);
        return ActionResult.success(MsgCode.SU003.get());
    }

    /**
     * 导入
     *
     * @param multipartFile 文件
     * @return
     */
    @Operation(summary = "导入")
    @SaCheckPermission("systemData.dataInterface")
    @PostMapping(value = "/Actions/Import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ActionResult<String> delete(@RequestPart("file") MultipartFile multipartFile) {
        //判断是否为.json结尾
        if (FileUtil.existsSuffix(multipartFile, ModuleTypeEnum.SYSTEM_DATAINTEFASE_VARIATE.getTableName())) {
            return ActionResult.fail("导入文件格式错误");
        }
        //读取文件内容
        String fileContent = FileUtil.getFileContent(multipartFile);
        try {
            DataInterfaceVariateEntity entity = JsonUtil.getJsonToBean(fileContent, DataInterfaceVariateEntity.class);
            if (dataInterfaceVariateService.getInfo(entity.getId()) == null &&
                    !dataInterfaceVariateService.isExistByFullName(entity)) {
                dataInterfaceVariateService.create(entity);
                return ActionResult.success("导入成功");
            }
        } catch (Exception e) {
            throw new DataException("导入失败，数据有误");
        }
        return ActionResult.fail("数据已存在");
    }

    /**
     * 复制
     *
     * @param id 自然主键
     * @return
     */
    @Operation(summary = "复制")
    @SaCheckPermission("systemData.dataInterface")
    @Parameter(name = "id", description = "自然主键", required = true)
    @PostMapping("/{id}/Actions/Copy")
    public ActionResult<String> copy(@PathVariable("id") String id) {
        String copyNum = UUID.randomUUID().toString().substring(0, 5);
        DataInterfaceVariateEntity entity = dataInterfaceVariateService.getInfo(id);
        entity.setFullName(entity.getFullName() + ".副本" + copyNum);
        if(entity.getFullName().length() > 50) return ActionResult.fail(MsgCode.COPY001.get());
        entity.setLastModifyTime(null);
        entity.setLastModifyUserId(null);
        dataInterfaceVariateService.create(entity);
        return ActionResult.success(MsgCode.SU007.get());
    }

}
