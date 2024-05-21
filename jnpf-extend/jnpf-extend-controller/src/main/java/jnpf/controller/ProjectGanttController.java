package jnpf.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jnpf.base.ActionResult;
import jnpf.base.Page;
import jnpf.base.controller.SuperController;
import jnpf.base.vo.ListVO;
import jnpf.entity.ProjectGanttEntity;
import jnpf.exception.DataException;
import jnpf.model.projectgantt.ProjectGanttCrForm;
import jnpf.model.projectgantt.ProjectGanttInfoVO;
import jnpf.model.projectgantt.ProjectGanttListVO;
import jnpf.model.projectgantt.ProjectGanttManagerIModel;
import jnpf.model.projectgantt.ProjectGanttTaskInfoVO;
import jnpf.model.projectgantt.ProjectGanttTaskTreeModel;
import jnpf.model.projectgantt.ProjectGanttTaskTreeVO;
import jnpf.model.projectgantt.ProjectGanttTreeModel;
import jnpf.model.projectgantt.ProjectGanttTsakCrForm;
import jnpf.model.projectgantt.ProjectGanttTsakUpForm;
import jnpf.model.projectgantt.ProjectGanttUpForm;
import jnpf.permission.entity.UserEntity;
import jnpf.permission.service.UserService;
import jnpf.service.ProjectGanttService;
import jnpf.util.JsonUtil;
import jnpf.util.UploaderUtil;
import jnpf.util.treeutil.ListToTreeUtil;
import jnpf.util.treeutil.SumTree;
import jnpf.util.treeutil.TreeDotUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 项目计划
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2019年9月26日 上午9:18
 */
@Tag(name = "项目计划", description = "ProjectGantt")
@RestController
@RequestMapping("/api/extend/ProjectGantt")
public class ProjectGanttController extends SuperController<ProjectGanttService, ProjectGanttEntity> {

    @Autowired
    private ProjectGanttService projectGanttService;
    @Autowired
    private UserService userService;


    /**
     * 项目列表
     *
     * @param page 分页模型
     * @return
     */
    @Operation(summary = "获取项目管理列表")
    @GetMapping
    @SaCheckPermission("extend.projectGantt")
    public ActionResult<ListVO<ProjectGanttListVO>> list(Page page) {
        List<ProjectGanttEntity> data = projectGanttService.getList(page);
        List<ProjectGanttListVO> list = JsonUtil.getJsonToList(data, ProjectGanttListVO.class);
        //获取用户给项目参与人员列表赋值
        List<String> userId = new ArrayList<>();
        list.forEach(t -> {
            String[] ids = t.getManagerIds().split(",");
            Collections.addAll(userId, ids);
        });
        List<UserEntity> userList = userService.getUserName(userId);
        for (ProjectGanttListVO vo : list) {
            List<String> managerList = new ArrayList<>();
            Collections.addAll(managerList, vo.getManagerIds().split(","));
            List<UserEntity> user = userList.stream().filter(t -> managerList.contains(t.getId())).collect(Collectors.toList());
            List<ProjectGanttManagerIModel> list1 = new ArrayList<>();
            user.forEach(t -> {
                ProjectGanttManagerIModel model1 = new ProjectGanttManagerIModel();
                model1.setAccount(t.getRealName() + "/" + t.getAccount());
                model1.setHeadIcon(UploaderUtil.uploaderImg(t.getHeadIcon()));
                list1.add(model1);
            });
            vo.setManagersInfo(list1);
        }
        ListVO listVO = new ListVO<>();
        listVO.setList(list);
        return ActionResult.success(listVO);
    }

    /**
     * 任务列表
     *
     * @param page 分页模型
     * @param projectId 主键
     * @return
     */
    @Operation(summary = "获取项目任务列表")
    @GetMapping("/{projectId}/Task")
    @Parameters({
            @Parameter(name = "projectId", description = "主键",required = true),
    })
    @SaCheckPermission("extend.projectGantt")
    public ActionResult<ListVO<ProjectGanttTaskTreeVO>> taskList(Page page, @PathVariable("projectId") String projectId) {
        List<ProjectGanttEntity> data = projectGanttService.getTaskList(projectId);
        List<ProjectGanttEntity> dataAll = data;
        if (!StringUtils.isEmpty(page.getKeyword())) {
            data = data.stream().filter(t -> String.valueOf(t.getFullName()).contains(page.getKeyword()) || String.valueOf(t.getEnCode()).contains(page.getKeyword())).collect(Collectors.toList());
        }
        List<ProjectGanttEntity> list = JsonUtil.getJsonToList(ListToTreeUtil.treeWhere(data, dataAll), ProjectGanttEntity.class);
        List<ProjectGanttTreeModel> treeList = JsonUtil.getJsonToList(list, ProjectGanttTreeModel.class);
        List<SumTree<ProjectGanttTreeModel>> trees = TreeDotUtils.convertListToTreeDot(treeList);
        List<ProjectGanttTaskTreeVO> listVO = JsonUtil.getJsonToList(trees, ProjectGanttTaskTreeVO.class);
        ListVO vo = new ListVO();
        vo.setList(listVO);
        return ActionResult.success(vo);
    }

    /**
     * 任务树形
     *
     * @param projectId 主键
     * @param id 主键
     * @return
     */
    @Operation(summary = "获取项目计划任务树形（新建任务）")
    @GetMapping("/{projectId}/Task/Selector/{id}")
    @Parameters({
            @Parameter(name = "projectId", description = "主键",required = true),
            @Parameter(name = "id", description = "主键",required = true),
    })
    @SaCheckPermission("extend.projectGantt")
    public ActionResult<ListVO<ProjectGanttTaskTreeVO>> taskTreeView(@PathVariable("projectId") String projectId, @PathVariable("id") String id) {
        List<ProjectGanttTaskTreeModel> treeList = new ArrayList<>();
        List<ProjectGanttEntity> data = projectGanttService.getTaskList(projectId);
        if (!"0".equals(id)) {
            //上级不能选择自己
            data.remove(projectGanttService.getInfo(id));
        }
        for (ProjectGanttEntity entity : data) {
            ProjectGanttTaskTreeModel treeModel = new ProjectGanttTaskTreeModel();
            treeModel.setId(entity.getId());
            treeModel.setFullName(entity.getFullName());
            treeModel.setParentId(entity.getParentId());
            treeList.add(treeModel);
        }
        List<SumTree<ProjectGanttTaskTreeModel>> trees = TreeDotUtils.convertListToTreeDotFilter(treeList);
        List<ProjectGanttTaskTreeVO> listVO = JsonUtil.getJsonToList(trees, ProjectGanttTaskTreeVO.class);
        ListVO vo = new ListVO();
        vo.setList(listVO);
        return ActionResult.success(vo);
    }

    /**
     * 信息
     *
     * @param id 主键
     * @return
     */
    @Operation(summary = "获取项目计划信息")
    @GetMapping("/{id}")
    @Parameters({
            @Parameter(name = "id", description = "主键",required = true),
    })
    @SaCheckPermission("extend.projectGantt")
    public ActionResult<ProjectGanttInfoVO> info(@PathVariable("id") String id) throws DataException {
        ProjectGanttEntity entity = projectGanttService.getInfo(id);
        ProjectGanttInfoVO vo = JsonUtil.getJsonToBeanEx(entity, ProjectGanttInfoVO.class);
        return ActionResult.success(vo);
    }

    /**
     * 信息
     *
     * @param id 主键
     * @return
     */
    @Operation(summary = "获取项目计划信息")
    @GetMapping("Task/{id}")
    @Parameters({
            @Parameter(name = "id", description = "主键",required = true),
    })
    @SaCheckPermission("extend.projectGantt")
    public ActionResult<ProjectGanttTaskInfoVO> taskInfo(@PathVariable("id") String id) throws DataException {
        ProjectGanttEntity entity = projectGanttService.getInfo(id);
        ProjectGanttTaskInfoVO vo = JsonUtil.getJsonToBeanEx(entity, ProjectGanttTaskInfoVO.class);
        return ActionResult.success(vo);
    }


    /**
     * 删除
     *
     * @param id 主键
     * @return
     */
    @Operation(summary = "删除项目计划/任务")
    @DeleteMapping("/{id}")
    @Parameters({
            @Parameter(name = "id", description = "主键",required = true),
    })
    @SaCheckPermission("extend.projectGantt")
    public ActionResult delete(@PathVariable("id") String id) {
        if (projectGanttService.allowDelete(id)) {
            ProjectGanttEntity entity = projectGanttService.getInfo(id);
            if (entity != null) {
                projectGanttService.delete(entity);
                return ActionResult.success("删除成功");
            }
            return ActionResult.fail("删除失败，此任务不存在");
        } else {
            return ActionResult.fail("此记录被关联引用,不允许被删除");
        }
    }

    /**
     * 创建
     *
     * @param projectGanttCrForm 项目模型
     * @return
     */
    @Operation(summary = "添加项目计划")
    @PostMapping
    @Parameters({
            @Parameter(name = "projectGanttCrForm", description = "项目模型",required = true),
    })
    @SaCheckPermission("extend.projectGantt")
    public ActionResult create(@RequestBody @Valid ProjectGanttCrForm projectGanttCrForm) {
        ProjectGanttEntity entity = JsonUtil.getJsonToBean(projectGanttCrForm, ProjectGanttEntity.class);
        entity.setType(1);
        entity.setParentId("0");
        if (projectGanttService.isExistByFullName(projectGanttCrForm.getFullName(), entity.getId())) {
            return ActionResult.fail("项目名称不能重复");
        }
        if (projectGanttService.isExistByEnCode(projectGanttCrForm.getEnCode(), entity.getId())) {
            return ActionResult.fail("项目编码不能重复");
        }
        projectGanttService.create(entity);
        return ActionResult.success("新建成功");
    }

    /**
     * 编辑
     *
     * @param id                 主键
     * @param projectGanttUpForm 项目模型
     * @return
     */
    @Operation(summary = "修改项目计划")
    @PutMapping("/{id}")
    @Parameters({
            @Parameter(name = "id", description = "主键",required = true),
            @Parameter(name = "projectGanttUpForm", description = "项目模型",required = true),
    })
    @SaCheckPermission("extend.projectGantt")
    public ActionResult update(@PathVariable("id") String id, @RequestBody @Valid ProjectGanttUpForm projectGanttUpForm) {
        ProjectGanttEntity entity = JsonUtil.getJsonToBean(projectGanttUpForm, ProjectGanttEntity.class);
        if (projectGanttService.isExistByFullName(projectGanttUpForm.getFullName(), id)) {
            return ActionResult.fail("项目名称不能重复");
        }
        if (projectGanttService.isExistByEnCode(projectGanttUpForm.getEnCode(), id)) {
            return ActionResult.fail("项目编码不能重复");
        }
        boolean flag = projectGanttService.update(id, entity);
        if (flag == false) {
            return ActionResult.fail("更新失败，数据不存在");
        }
        return ActionResult.success("更新成功");
    }


    /**
     * 创建
     *
     * @param projectGanttTsakCrForm 项目模型
     * @return
     */
    @Operation(summary = "添加项目任务")
    @PostMapping("/Task")
    @Parameters({
            @Parameter(name = "projectGanttTsakCrForm", description = "项目模型",required = true),
    })
    @SaCheckPermission("extend.projectGantt")
    public ActionResult createTask(@RequestBody @Valid ProjectGanttTsakCrForm projectGanttTsakCrForm) {
        ProjectGanttEntity entity = JsonUtil.getJsonToBean(projectGanttTsakCrForm, ProjectGanttEntity.class);
        entity.setType(2);
        if (projectGanttService.isExistByFullName(projectGanttTsakCrForm.getFullName(), entity.getId())) {
            return ActionResult.fail("任务名称不能重复");
        }
        projectGanttService.create(entity);
        return ActionResult.success("新建成功");
    }

    /**
     * 编辑
     *
     * @param id                     主键
     * @param projectGanttTsakCrForm 项目模型
     * @return
     */
    @Operation(summary = "修改项目任务")
    @PutMapping("/Task/{id}")
    @Parameters({
            @Parameter(name = "projectGanttTsakCrForm", description = "项目模型",required = true),
            @Parameter(name = "id", description = "主键",required = true),
    })
    @SaCheckPermission("extend.projectGantt")
    public ActionResult updateTask(@PathVariable("id") String id, @RequestBody @Valid ProjectGanttTsakUpForm projectGanttTsakCrForm) {
        ProjectGanttEntity entity = JsonUtil.getJsonToBean(projectGanttTsakCrForm, ProjectGanttEntity.class);
        if (projectGanttService.isExistByFullName(projectGanttTsakCrForm.getFullName(), id)) {
            return ActionResult.fail("任务名称不能重复");
        }
        boolean flag = projectGanttService.update(id, entity);
        if (flag == false) {
            return ActionResult.fail("更新失败，数据不存在");
        }
        return ActionResult.success("更新成功");
    }

}
