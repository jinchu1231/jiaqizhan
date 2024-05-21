package jnpf.permission.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jnpf.base.ActionResult;
import jnpf.base.Pagination;
import jnpf.base.controller.SuperController;
import jnpf.base.entity.ModuleEntity;
import jnpf.base.entity.SystemEntity;
import jnpf.base.service.ModuleService;
import jnpf.base.service.SystemService;
import jnpf.base.vo.PageListVO;
import jnpf.base.vo.PaginationVO;
import jnpf.config.ConfigValueUtil;
import jnpf.constant.JnpfConst;
import jnpf.constant.MsgCode;
import jnpf.constant.PermissionConst;
import jnpf.database.util.TenantDataSourceUtil;
import jnpf.permission.entity.OrganizeAdministratorEntity;
import jnpf.permission.entity.OrganizeEntity;
import jnpf.permission.entity.UserEntity;
import jnpf.permission.model.organizeadministrator.ModuleSelectorModel;
import jnpf.permission.model.organizeadministrator.ModuleSelectorVO;
import jnpf.permission.model.organizeadministrator.OrganizeAdminIsTratorCrForm;
import jnpf.permission.model.organizeadministrator.OrganizeAdministratorCrModel;
import jnpf.permission.model.organizeadministrator.OrganizeAdministratorListVo;
import jnpf.permission.model.organizeadministrator.OrganizeAdministratorModel;
import jnpf.permission.model.organizeadministrator.OrganizeAdministratorSelectedVO;
import jnpf.permission.model.organizeadministrator.OrganizeAdministratorSelectorModel;
import jnpf.permission.model.organizeadministrator.OrganizeAdministratorSelectorVO;
import jnpf.permission.model.organizeadministrator.SystemSelectorVO;
import jnpf.permission.service.OrganizeAdministratorService;
import jnpf.permission.service.OrganizeService;
import jnpf.permission.service.UserService;
import jnpf.util.JsonUtil;
import jnpf.util.NoDataSourceBind;
import jnpf.util.StringUtil;
import jnpf.util.UserProvider;
import jnpf.util.treeutil.SumTree;
import jnpf.util.treeutil.newtreeutil.TreeDotUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * 机构分级管理员
 *
 * @author JNPF
 * @版本： V3.1.0
 * @版权： 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @作者： JNPF开发平台组
 * @日期： 2021-08-30 10:30:04
 */
@Tag(name = "机构分级管理员", description = "organizeAdminIsTrator")
@Slf4j
@RestController
@RequestMapping("/api/permission/organizeAdminIsTrator")
public class OrganizeAdministratorController extends SuperController<OrganizeAdministratorService, OrganizeAdministratorEntity> {

	@Autowired
	private OrganizeAdministratorService organizeAdminIsTratorService;
	@Autowired
	private OrganizeService organizeService;
	@Autowired
	private UserProvider userProvider;
	@Autowired
	private SystemService systemService;
	@Autowired
	private ModuleService moduleService;
	@Autowired
	private UserService userService;
	@Autowired
	private ConfigValueUtil configValueUtil;

	/**
	 * 获取分级管理员列表
	 *
	 * @param pagination 分页模型
	 * @return
	 */
	@Operation(summary = "获取分级管理员列表")
	@SaCheckPermission(value = {"permission.grade"})
	@GetMapping
	public ActionResult<PageListVO<OrganizeAdministratorListVo>> list(Pagination pagination) {
		List<OrganizeAdministratorListVo> list = organizeAdminIsTratorService.getList(pagination);
		PaginationVO paginationVO = JsonUtil.getJsonToBean(pagination, PaginationVO.class);
		return ActionResult.page(list, paginationVO);
	}

	/**
	 * 保存分级管理员
	 *
	 * @param organizeAdminIsTratorCrForm 新建模型
	 * @return
	 */
	@Operation(summary = "保存分级管理员")
	@Parameters({
			@Parameter(name = "organizeAdminIsTratorCrForm", description = "新建模型", required = true)
	})
	@SaCheckPermission(value = {"permission.grade"})
	@PostMapping
	public ActionResult save(@RequestBody @Valid OrganizeAdminIsTratorCrForm organizeAdminIsTratorCrForm) {
		String userId = organizeAdminIsTratorCrForm.getUserId();
		if (UserProvider.getLoginUserId().equals(userId)) {
			return ActionResult.fail("无法设置当前用户操作权限");
		}
		List<OrganizeAdministratorCrModel> list = new ArrayList<>(16);
		// 递归得到所有的数组
		getOrganizeAdminIsTratorModel(list, organizeAdminIsTratorCrForm.getOrgAdminModel());
		List<OrganizeAdministratorEntity> jsonToList = JsonUtil.getJsonToList(list, OrganizeAdministratorEntity.class);
		// 处理应用
		List<String> systemIds = organizeAdminIsTratorCrForm.getSystemIds();
		systemIds.forEach(t -> {
			OrganizeAdministratorEntity entity = new OrganizeAdministratorEntity();
			entity.setOrganizeType(PermissionConst.SYSTEM);
			entity.setOrganizeId(t);
			entity.setThisLayerSelect(1);
			jsonToList.add(entity);
		});
		// 处理菜单
		List<String> moduleIds = organizeAdminIsTratorCrForm.getModuleIds();
		moduleIds.forEach(t -> {
			OrganizeAdministratorEntity entity = new OrganizeAdministratorEntity();
			entity.setOrganizeType(PermissionConst.MODULE);
			entity.setOrganizeId(t);
			entity.setThisLayerSelect(1);
			jsonToList.add(entity);
		});
		organizeAdminIsTratorService.createList(jsonToList, userId);
		return ActionResult.success("保存成功");
	}

	/**
	 * 获取
	 *
	 * @param list
	 * @param jsonToList
	 */
	private void getOrganizeAdminIsTratorModel(List<OrganizeAdministratorCrModel> list, List<OrganizeAdministratorCrModel> jsonToList) {
		if (jsonToList != null) {
			jsonToList.forEach(t -> {
				OrganizeAdministratorCrModel vo = JsonUtil.getJsonToBean(t, OrganizeAdministratorCrModel.class);
				vo.setChildren(null);
				if (vo.getThisLayerSelect() != null && (vo.getThisLayerSelect() == 2 || vo.getThisLayerSelect() == 1)) {
					vo.setThisLayerSelect(1);
					if (vo.getThisLayerAdd() != null && (vo.getThisLayerAdd() == 2 || vo.getThisLayerAdd() == 1)) {
						vo.setThisLayerAdd(1);
					}
					if (vo.getThisLayerEdit() != null && (vo.getThisLayerEdit() == 2 || vo.getThisLayerEdit() == 1)) {
						vo.setThisLayerEdit(1);
					}
					if (vo.getThisLayerDelete() != null && (vo.getThisLayerDelete() == 2 || vo.getThisLayerDelete() == 1)) {
						vo.setThisLayerDelete(1);
					}
				} else if (vo.getThisLayerSelect() == null || vo.getThisLayerSelect() == 0 || vo.getThisLayerSelect() == 3) {
					vo.setThisLayerSelect(0);
					vo.setThisLayerAdd(0);
					vo.setThisLayerEdit(0);
					vo.setThisLayerDelete(0);
				}
				if (vo.getSubLayerSelect() != null && (vo.getSubLayerSelect() == 2 || vo.getSubLayerSelect() == 1)) {
					vo.setSubLayerSelect(1);
					if (vo.getSubLayerAdd() != null && (vo.getSubLayerAdd() == 2 || vo.getSubLayerAdd() == 1)) {
						vo.setSubLayerAdd(1);
					}
					if (vo.getSubLayerEdit() != null && (vo.getSubLayerEdit() == 2 || vo.getSubLayerEdit() == 1)) {
						vo.setSubLayerEdit(1);
					}
					if (vo.getSubLayerDelete() != null && (vo.getSubLayerDelete() == 2 || vo.getSubLayerDelete() == 1)) {
						vo.setSubLayerDelete(1);
					}
				} else if (vo.getSubLayerSelect() == null || vo.getSubLayerSelect() == 0 || vo.getSubLayerSelect() == 3) {
					vo.setSubLayerSelect(0);
					vo.setSubLayerAdd(0);
					vo.setSubLayerEdit(0);
					vo.setSubLayerDelete(0);
				}
				list.add(vo);
				getOrganizeAdminIsTratorModel(list, t.getChildren());
			});
		}
	}

	/**
	 * 删除二级管理员
	 *
	 * @param id 主键值
	 * @return
	 */
	@Operation(summary = "删除二级管理员")
	@Parameters({
			@Parameter(name = "id", description = "用户id", required = true)
	})
	@SaCheckPermission(value = {"permission.grade"})
	@DeleteMapping("/{id}")
	public ActionResult delete(@PathVariable("id") String id) {
		organizeAdminIsTratorService.deleteByUserId(id);
		return ActionResult.success(MsgCode.SU003.get());
	}

	/**
	 * 获取组织下拉框列表
	 *
	 * @param userId 用户id
	 * @return
	 */
	@Operation(summary = "获取组织下拉框列表")
	@SaCheckPermission(value = {"permission.grade"})
	@GetMapping("/Selector")
	public ActionResult<OrganizeAdministratorSelectedVO> getSelector(String userId) {
		OrganizeAdministratorSelectedVO vo = new OrganizeAdministratorSelectedVO();
		if (StringUtil.isEmpty(userId)) {
			return ActionResult.success(vo);
		}
		if (StringUtil.isNotEmpty(userId)) {
			if (userId.equals(UserProvider.getLoginUserId())) {
				return ActionResult.fail("无法设置当前用户为分级管理员");
			}
			UserEntity userEntity = userService.getInfo(userId);
			if (userEntity != null && Objects.equals(userEntity.getIsAdministrator(), 1)) {
				return ActionResult.fail("无法设置超管为分级管理员");
			}
		}
		List<SystemSelectorVO> systemPermissionList = new ArrayList<>();
		List<ModuleSelectorVO> moduleVOPermissionList = new ArrayList<>();
		List<ModuleSelectorModel> modulePermissionList = new ArrayList<>();
		List<String> moduleSelectedList = new ArrayList<>();
		List<String> systemSelectedList = new ArrayList<>();
		// 存储组织集合
		Map<String, OrganizeEntity> orgMaps = organizeService.getOrgMaps(null, false, null);
		// 获取所有组织
		List<OrganizeAdministratorSelectorModel> selectorModels = JsonUtil.getJsonToList(orgMaps.values(), OrganizeAdministratorSelectorModel.class);
		// 获取应用列表
		List<SystemEntity> list = systemService.getList(null, false, true, true, false, new ArrayList<>());
		List<SystemSelectorVO> systemSelectorVOList = JsonUtil.getJsonToList(list, SystemSelectorVO.class);
		systemSelectorVOList.forEach(t -> t.setIsPermission(0));
		// 菜单
		SystemEntity mainSystem = systemService.getInfoByEnCode(JnpfConst.MAIN_SYSTEM_CODE);
		List<ModuleEntity> mainModule = moduleService.getMainModule(null, null, false);
		List<ModuleEntity> allModule = new ArrayList<>(mainModule);
		List<ModuleSelectorModel> moduleSelectorVOList = JsonUtil.getJsonToList(mainModule, ModuleSelectorModel.class);
		List<ModuleSelectorModel> allModuleSelectorVOList = JsonUtil.getJsonToList(allModule, ModuleSelectorModel.class);

		boolean isAdministrator = userProvider.get().getIsAdministrator();
		if (isAdministrator) {
			selectorModels.forEach(t -> {
				// 设置组织id
				t.setOrganizeId(t.getId());
				t.setThisLayerAdd(0);
				t.setThisLayerEdit(0);
				t.setThisLayerDelete(0);
				t.setThisLayerSelect(0);
				t.setSubLayerAdd(0);
				t.setSubLayerEdit(0);
				t.setSubLayerDelete(0);
				t.setSubLayerSelect(0);
			});
			moduleSelectorVOList.forEach(t -> t.setIsPermission(0));
		} else {
			List<OrganizeAdministratorEntity> organizeAdministratorEntity = organizeAdminIsTratorService.getOrganizeAdministratorEntity(UserProvider.getLoginUserId(), jnpf.constant.PermissionConst.ORGANIZE, false);
			// 当前用户分级管理权限
			Map<String, OrganizeAdministratorEntity> thisOrganizeMap = organizeAdministratorEntity.stream().collect(Collectors.toMap(OrganizeAdministratorEntity::getOrganizeId, Function.identity()));
			List<OrganizeAdministratorEntity> organizeAdministratorEntitys = new ArrayList<>(organizeAdministratorEntity);
			// 处理子组织权限
			organizeAdministratorEntitys.forEach(t -> {
				if (t.getSubLayerSelect() != null && t.getSubLayerSelect() == 1) {
					// 得到组织id
					List<String> underOrganizations = orgMaps.values().stream().filter(tt -> tt.getOrganizeIdTree().contains(t.getOrganizeId())).map(OrganizeEntity::getId).collect(Collectors.toList());
//                            organizeService.getUnderOrganizations(t.getOrganizeId(), false);
					underOrganizations.forEach(uo -> {
						OrganizeAdministratorEntity organizeAdministratorEntity1 = thisOrganizeMap.get(uo);
						if (organizeAdministratorEntity1 != null) {
							organizeAdministratorEntity1.setThisLayerSelect(1);
							organizeAdministratorEntity1.setSubLayerSelect(1);
							if (t.getSubLayerAdd() != null && t.getSubLayerAdd() == 1) {
								organizeAdministratorEntity1.setThisLayerAdd(1);
								organizeAdministratorEntity1.setSubLayerAdd(1);
							}
							if (t.getSubLayerEdit() != null && t.getSubLayerEdit() == 1) {
								organizeAdministratorEntity1.setThisLayerEdit(1);
								organizeAdministratorEntity1.setSubLayerEdit(1);
							}
							if (t.getSubLayerDelete() != null && t.getSubLayerDelete() == 1) {
								organizeAdministratorEntity1.setThisLayerDelete(1);
								organizeAdministratorEntity1.setSubLayerDelete(1);
							}
						}
					});
				}
			});

			List<OrganizeAdministratorSelectorModel> selectorModelss = new ArrayList<>(16);
			selectorModels.forEach(t -> {
				// 设置组织id
				t.setOrganizeId(t.getId());
				OrganizeAdministratorEntity administratorEntity = thisOrganizeMap.get(t.getOrganizeId());
				if (administratorEntity != null) {
					boolean flag = false;
					if (administratorEntity.getThisLayerSelect() != null && administratorEntity.getThisLayerSelect() == 1) {
						t.setThisLayerSelect(0);
						flag = true;
						if (administratorEntity.getThisLayerAdd() == 1) {
							t.setThisLayerAdd(0);
						}
						if (administratorEntity.getThisLayerEdit() == 1) {
							t.setThisLayerEdit(0);
						}
						if (administratorEntity.getThisLayerDelete() == 1) {
							t.setThisLayerDelete(0);
						}
					}
					if (administratorEntity.getSubLayerSelect()!= null && administratorEntity.getSubLayerSelect() == 1) {
						t.setSubLayerSelect(0);
						flag = true;
						if (administratorEntity.getSubLayerAdd()!= null && administratorEntity.getSubLayerAdd() == 1) {
							t.setSubLayerAdd(0);
						}
						if (administratorEntity.getSubLayerEdit()!= null && administratorEntity.getSubLayerEdit() == 1) {
							t.setSubLayerEdit(0);
						}
						if (administratorEntity.getSubLayerDelete()!= null && administratorEntity.getSubLayerDelete() == 1) {
							t.setSubLayerDelete(0);
						}
					}
					if (flag) {
						selectorModelss.add(t);
					}
				}
			});
			selectorModels = selectorModelss;

			List<OrganizeAdministratorEntity> moduleOrganizeAdministratorEntity = organizeAdminIsTratorService.getOrganizeAdministratorEntity(UserProvider.getLoginUserId(), jnpf.constant.PermissionConst.MODULE, false);
			List<String> moduleIds = moduleOrganizeAdministratorEntity.stream().map(OrganizeAdministratorEntity::getOrganizeId).collect(Collectors.toList());
			List<ModuleEntity> moduleByIds = moduleService.getModuleByIds(moduleIds);
			moduleSelectorVOList = JsonUtil.getJsonToList(moduleByIds, ModuleSelectorModel.class);

//			List<OrganizeAdministratorEntity> systemOrganizeAdministratorEntity = organizeAdminIsTratorService.getOrganizeAdministratorEntity(UserProvider.getLoginUserId(), true, PermissionConst.SYSTEM);

		}
		Map<String, OrganizeAdministratorSelectorModel> modelMap = selectorModels.stream().collect(Collectors.toMap(OrganizeAdministratorSelectorModel::getOrganizeId, Function.identity()));
		// 判断是否为空
		if (StringUtil.isNotEmpty(userId)) {
			List<OrganizeAdministratorEntity> organizeAdministratorEntity = organizeAdminIsTratorService.getOrganizeAdministratorEntity(userId, jnpf.constant.PermissionConst.ORGANIZE, false);
			// 处理子组织字段
			Map<String, OrganizeAdministratorEntity> hisOrganizeMap = organizeAdministratorEntity.stream().collect(Collectors.toMap(OrganizeAdministratorEntity::getOrganizeId, Function.identity()));
			List<OrganizeAdministratorSelectorModel> finalSelectorModels3 = selectorModels;
			hisOrganizeMap.values().forEach(t -> {
				if (!isAdministrator) {
					if (t.getSubLayerSelect() != null && t.getSubLayerSelect() == 1) {
						// 得到组织id
						List<String> underOrganizations = orgMaps.values().stream().filter(tt -> tt.getOrganizeIdTree().contains(t.getOrganizeId())).map(OrganizeEntity::getId).collect(Collectors.toList());
//                                organizeService.getUnderOrganizations(t.getOrganizeId(), false);
						// 将同样的组织id的数据先处理好
						List<OrganizeAdministratorSelectorModel> collect1 = modelMap.values().stream().filter(fsm -> underOrganizations.contains(fsm.getOrganizeId())).collect(Collectors.toList());
						collect1.forEach(cl -> {
							OrganizeAdministratorSelectorModel organizeAdministratorSelectorModel = modelMap.get(cl.getOrganizeId());
							if (organizeAdministratorSelectorModel != null) {
								if (organizeAdministratorSelectorModel.getThisLayerSelect() != null && organizeAdministratorSelectorModel.getThisLayerSelect() == 0) {
									organizeAdministratorSelectorModel.setThisLayerSelect(0);
								} else {
									organizeAdministratorSelectorModel.setThisLayerSelect(3);
								}
								if (organizeAdministratorSelectorModel.getSubLayerSelect() != null && organizeAdministratorSelectorModel.getSubLayerSelect() == 0) {
									organizeAdministratorSelectorModel.setSubLayerSelect(0);
								} else {
									organizeAdministratorSelectorModel.setSubLayerSelect(3);
								}
							}
						});
						// 当前模型包含组织id
						List<String> collect = underOrganizations.stream().filter(uo -> !modelMap.values().stream().map(OrganizeAdministratorSelectorModel::getOrganizeId).collect(Collectors.toList()).contains(uo)).collect(Collectors.toList());
						collect.forEach(cl -> {
							OrganizeAdministratorEntity organizeAdministratorEntity1 = hisOrganizeMap.get(cl);
							if (organizeAdministratorEntity1 != null) {
								OrganizeEntity info = orgMaps.get(organizeAdministratorEntity1.getOrganizeId());
								OrganizeAdministratorSelectorModel organizeAdministratorSelectorModel = JsonUtil.getJsonToBean(info, OrganizeAdministratorSelectorModel.class);
								organizeAdministratorSelectorModel.setOrganizeId(organizeAdministratorEntity1.getOrganizeId());
								organizeAdministratorSelectorModel.setThisLayerSelect(3);
								organizeAdministratorSelectorModel.setSubLayerSelect(3);
								finalSelectorModels3.add(organizeAdministratorSelectorModel);
							}
						});
					}
				}
			});
			Map<String, OrganizeAdministratorSelectorModel> selectorModelMap = selectorModels.stream().collect(Collectors.toMap(OrganizeAdministratorSelectorModel::getOrganizeId, Function.identity()));
			List<OrganizeAdministratorSelectorModel> finalSelectorModels1 = new ArrayList<>(selectorModels);
			hisOrganizeMap.values().forEach(t -> {
				// 我没有他有时，需要判断此组织是否跟我所管理的组织相同，不同则需要放进去
				OrganizeAdministratorSelectorModel organizeAdministratorSelectorModel = selectorModelMap.get(t.getOrganizeId());
				if (organizeAdministratorSelectorModel == null) {
					boolean flag = false;
					if (t.getThisLayerSelect() != null) {
						if (t.getThisLayerSelect() == 1) {
							t.setThisLayerSelect(2);
							flag = true;
							if (flag && t.getThisLayerAdd() != null && t.getThisLayerAdd() == 1) {
								t.setThisLayerAdd(2);
							} else if (t.getThisLayerAdd() != null) {
								t.setThisLayerAdd(null);
							}
							if (flag && t.getThisLayerEdit() != null && t.getThisLayerEdit() == 1) {
								t.setThisLayerEdit(2);
							} else if (t.getThisLayerEdit() != null) {
								t.setThisLayerEdit(null);
							}
							if (flag && t.getThisLayerDelete() != null && t.getThisLayerDelete() == 1) {
								t.setThisLayerDelete(2);
							} else if (t.getThisLayerDelete() != null) {
								t.setThisLayerDelete(null);
							}
						} else {
							t.setThisLayerSelect(null);
							t.setThisLayerAdd(null);
							t.setThisLayerEdit(null);
							t.setThisLayerDelete(null);
						}
					}
					boolean flag1 = false;
					if (t.getSubLayerSelect() != null) {
						if (t.getSubLayerSelect() == 1) {
							t.setSubLayerSelect(2);
							flag1 = true;
							if (flag1 && t.getSubLayerAdd() != null && t.getSubLayerAdd() == 1) {
								t.setSubLayerAdd(2);
							} else if (t.getSubLayerAdd() != null) {
								t.setSubLayerAdd(null);
							}
							if (flag1 && t.getSubLayerEdit() != null && t.getSubLayerEdit() == 1) {
								t.setSubLayerEdit(2);
							} else if (t.getSubLayerEdit() != null) {
								t.setSubLayerEdit(null);
							}
							if (flag1 && t.getSubLayerDelete() != null && t.getSubLayerDelete() == 1) {
								t.setSubLayerDelete(2);
							} else if (t.getSubLayerDelete() != null) {
								t.setSubLayerDelete(null);
							}
						} else {
							t.setSubLayerSelect(null);
							t.setSubLayerAdd(null);
							t.setSubLayerEdit(null);
							t.setSubLayerDelete(null);
						}
					}
					if (flag || flag1) {
						OrganizeAdministratorSelectorModel jsonToBean = JsonUtil.getJsonToBean(t, OrganizeAdministratorSelectorModel.class);
						OrganizeEntity info = orgMaps.get(t.getOrganizeId());
						if (info != null) {
							jsonToBean.setParentId(info.getParentId());
							jsonToBean.setId(info.getId());
							jsonToBean.setCategory(info.getCategory());
							jsonToBean.setIcon(jnpf.constant.PermissionConst.COMPANY.equals(info.getCategory()) ? "icon-ym icon-ym-tree-organization3" : "icon-ym icon-ym-tree-department1");
							jsonToBean.setOrganizeId(t.getOrganizeId());
							jsonToBean.setOrganizeIdTree(info.getOrganizeIdTree());
							jsonToBean.setFullName(info.getFullName());
							finalSelectorModels1.add(jsonToBean);
						}
					}
				}
			});
			hisOrganizeMap.values().forEach(t -> {
				finalSelectorModels1.forEach(fs -> {
					if (t.getOrganizeId().equals(fs.getOrganizeId())) {
						// 本层添加
						if (fs.getThisLayerAdd() != null) {
							if (fs.getThisLayerAdd() == 0) {
								if (t.getThisLayerAdd() != null) {
									if (t.getThisLayerAdd() == 1) {
										fs.setThisLayerAdd(1);
									} else {
										fs.setThisLayerAdd(0);
									}
								}
							} else if (fs.getThisLayerAdd() == 3) {
								if (t.getThisLayerAdd() != null) {
									if (t.getThisLayerAdd() == 1) {
										fs.setThisLayerAdd(1);
									} else {
										fs.setThisLayerAdd(3);
									}
								}
							} else {
								if (t.getThisLayerAdd() != null) {
									if (t.getThisLayerAdd() == 1) {
										if (isAdministrator) {
											fs.setThisLayerAdd(1);
										} else {
											fs.setThisLayerAdd(2);
										}
									}
								}
							}
						} else {
							if (t.getThisLayerAdd() != null) {
								if (t.getThisLayerAdd() == 1) {
									if (isAdministrator) {
										fs.setThisLayerAdd(1);
									} else {
										fs.setThisLayerAdd(2);
									}
								}
							}
						}
						// 本层编辑
						if (fs.getThisLayerEdit() != null) {
							if (fs.getThisLayerEdit() == 0) {
								if (t.getThisLayerEdit() != null) {
									if (t.getThisLayerEdit() == 1) {
										fs.setThisLayerEdit(1);
									} else {
										fs.setThisLayerEdit(0);
									}
								}
							} else if (fs.getThisLayerEdit() == 3) {
								if (t.getThisLayerEdit() != null) {
									if (t.getThisLayerEdit() == 1) {
										fs.setThisLayerEdit(1);
									} else {
										fs.setThisLayerEdit(3);
									}
								}
							} else {
								if (t.getThisLayerEdit() != null) {
									if (t.getThisLayerEdit() == 1) {
										if (isAdministrator) {
											fs.setThisLayerEdit(1);
										} else {
											fs.setThisLayerEdit(2);
										}
									}
								}
							}
						} else {
							if (t.getThisLayerEdit() != null) {
								if (t.getThisLayerEdit() == 1) {
									if (isAdministrator) {
										fs.setThisLayerEdit(1);
									} else {
										fs.setThisLayerEdit(2);
									}
								}
							}
						}
						// 本层删除
						if (fs.getThisLayerDelete() != null) {
							if (fs.getThisLayerDelete() == 0) {
								if (t.getThisLayerDelete() != null) {
									if (t.getThisLayerDelete() == 1) {
										fs.setThisLayerDelete(1);
									} else {
										fs.setThisLayerDelete(0);
									}
								}
							} else if (fs.getThisLayerDelete() == 3) {
								if (t.getThisLayerDelete() != null) {
									if (t.getThisLayerDelete() == 1) {
										fs.setThisLayerDelete(1);
									} else {
										fs.setThisLayerDelete(3);
									}
								}
							} else {
								if (t.getThisLayerDelete() != null) {
									if (t.getThisLayerDelete() == 1) {
										if (isAdministrator) {
											fs.setThisLayerDelete(1);
										} else {
											fs.setThisLayerDelete(2);
										}
									}
								}
							}
						} else {
							if (t.getThisLayerDelete() != null) {
								if (t.getThisLayerDelete() == 1) {
									if (isAdministrator) {
										fs.setThisLayerDelete(1);
									} else {
										fs.setThisLayerDelete(2);
									}
								}
							}
						}
						// 本层查看
						if (fs.getThisLayerSelect() != null) {
							if (fs.getThisLayerSelect() == 0) {
								if (t.getThisLayerSelect() != null) {
									if (t.getThisLayerSelect() == 1) {
										fs.setThisLayerSelect(1);
									} else {
										fs.setThisLayerSelect(0);
									}
								}
							} else if (fs.getThisLayerSelect() == 3) {
								if (t.getThisLayerSelect() != null) {
									if (t.getThisLayerSelect() == 1) {
										fs.setThisLayerSelect(1);
									} else {
										fs.setThisLayerSelect(3);
									}
								}
							} else {
								if (t.getThisLayerSelect() != null) {
									if (t.getThisLayerSelect() == 1) {
										if (isAdministrator) {
											fs.setThisLayerSelect(1);
										} else {
											fs.setThisLayerSelect(2);
										}
									}
								}
							}
						} else {
							if (t.getThisLayerSelect() != null) {
								if (t.getThisLayerSelect() == 1) {
									if (isAdministrator) {
										fs.setThisLayerSelect(1);
									} else {
										fs.setThisLayerSelect(2);
									}
								}
							}
						}
						// 子层添加
						if (fs.getSubLayerAdd() != null) {
							if (fs.getSubLayerAdd() == 0) {
								if (t.getSubLayerAdd() != null) {
									if (t.getSubLayerAdd() == 1) {
										fs.setSubLayerAdd(1);
									} else {
										fs.setSubLayerAdd(0);
									}
								}
							} else if (fs.getSubLayerAdd() == 3) {
								if (t.getSubLayerAdd() != null) {
									if (t.getSubLayerAdd() == 1) {
										fs.setSubLayerAdd(1);
									} else {
										fs.setSubLayerAdd(3);
									}
								}
							} else {
								if (t.getSubLayerAdd() != null) {
									if (t.getSubLayerAdd() == 1) {
										if (isAdministrator) {
											fs.setSubLayerAdd(1);
										} else {
											fs.setSubLayerAdd(2);
										}
									}
								}
							}
						} else {
							if (t.getSubLayerAdd() != null) {
								if (t.getSubLayerAdd() == 1) {
									if (isAdministrator) {
										fs.setSubLayerAdd(1);
									} else {
										fs.setSubLayerAdd(2);
									}
								}
							}
						}

						if (fs.getSubLayerEdit() != null) {
							if (fs.getSubLayerEdit() == 0) {
								if (t.getSubLayerEdit() != null) {
									if (t.getSubLayerEdit() == 1) {
										fs.setSubLayerEdit(1);
									} else {
										fs.setSubLayerEdit(0);
									}
								}
							} else if (fs.getSubLayerEdit() == 3) {
								if (t.getSubLayerEdit() != null) {
									if (t.getSubLayerEdit() == 1) {
										fs.setSubLayerEdit(1);
									} else {
										fs.setSubLayerEdit(3);
									}
								}
							} else {
								if (t.getSubLayerEdit() != null) {
									if (t.getSubLayerEdit() == 1) {
										if (isAdministrator) {
											fs.setSubLayerEdit(1);
										} else {
											fs.setSubLayerEdit(2);
										}
									}
								}
							}
						} else {
							if (t.getSubLayerEdit() != null) {
								if (t.getSubLayerEdit() == 1) {
									if (isAdministrator) {
										fs.setSubLayerEdit(1);
									} else {
										fs.setSubLayerEdit(2);
									}
								}
							}
						}

						if (fs.getSubLayerDelete() != null) {
							if (fs.getSubLayerDelete() == 0) {
								if (t.getSubLayerDelete() != null) {
									if (t.getSubLayerDelete() == 1) {
										fs.setSubLayerDelete(1);
									} else {
										fs.setSubLayerDelete(0);
									}
								}
							} else if (fs.getSubLayerDelete() == 3) {
								if (t.getSubLayerDelete() != null) {
									if (t.getSubLayerDelete() == 1) {
										fs.setSubLayerDelete(1);
									} else {
										fs.setSubLayerDelete(3);
									}
								}
							} else {
								if (t.getSubLayerDelete() != null) {
									if (t.getSubLayerDelete() == 1) {
										if (isAdministrator) {
											fs.setSubLayerDelete(1);
										} else {
											fs.setSubLayerDelete(2);
										}
									}
								}
							}
						} else {
							if (t.getSubLayerDelete() != null) {
								if (t.getSubLayerDelete() == 1) {
									if (isAdministrator) {
										fs.setSubLayerDelete(1);
									} else {
										fs.setSubLayerDelete(2);
									}
								}
							}
						}

						if (fs.getSubLayerSelect() != null) {
							if (fs.getSubLayerSelect() == 0) {
								if (t.getSubLayerSelect() != null) {
									if (t.getSubLayerSelect() == 1) {
										fs.setSubLayerSelect(1);
									} else {
										fs.setSubLayerSelect(0);
									}
								}
							} else if (fs.getSubLayerSelect() == 3) {
								if (t.getSubLayerSelect() != null) {
									if (t.getSubLayerSelect() == 1) {
										fs.setSubLayerSelect(1);
									} else {
										fs.setSubLayerSelect(3);
									}
								}
							} else {
								if (t.getSubLayerSelect() != null) {
									if (t.getSubLayerSelect() == 1) {
										if (isAdministrator) {
											fs.setSubLayerSelect(1);
										} else {
											fs.setSubLayerSelect(2);
										}
									}
								}
							}
						} else {
							if (t.getSubLayerSelect() != null) {
								if (t.getSubLayerSelect() == 1) {
									if (isAdministrator) {
										fs.setSubLayerSelect(1);
									} else {
										fs.setSubLayerSelect(2);
									}
								}
							}
						}


					}
				});
			});
			selectorModels = finalSelectorModels1;

			// 系统
			List<OrganizeAdministratorEntity> systemOrganizeAdministratorEntity = organizeAdminIsTratorService.getOrganizeAdministratorEntity(userId, jnpf.constant.PermissionConst.SYSTEM, true);
			List<String> systemPermissionIds = systemOrganizeAdministratorEntity.stream().map(OrganizeAdministratorEntity::getOrganizeId).collect(Collectors.toList());
			List<String> collect = systemSelectorVOList.stream().map(SystemSelectorVO::getId).collect(Collectors.toList());
			List<String> systemList = new ArrayList<>();
			systemList.addAll(systemPermissionIds);
			systemList.addAll(collect);
			// 交集  1
			List<String> collect1 = systemPermissionIds.stream().filter(collect::contains).collect(Collectors.toList());
			systemList = systemList.stream().distinct().collect(Collectors.toList());
			// 去掉交集后
			systemList.removeAll(collect1);
			// 我有他没有 0
			List<String> collect2 = collect.stream().filter(systemList::contains).collect(Collectors.toList());
			// 他有我没有 2
			List<String> collect3 = systemPermissionIds.stream().filter(systemList::contains).collect(Collectors.toList());
			List<SystemEntity> listByIds1 = systemService.getListByIds(collect1, null);
			List<SystemSelectorVO> systemSelectorVOList1 = JsonUtil.getJsonToList(listByIds1, SystemSelectorVO.class);
			systemSelectorVOList1.forEach(t -> t.setIsPermission(1));
			List<SystemEntity> listByIds2 = systemService.getListByIds(collect2, null);
			List<SystemSelectorVO> systemSelectorVOList2 = JsonUtil.getJsonToList(listByIds2, SystemSelectorVO.class);
			systemSelectorVOList2.forEach(t -> t.setIsPermission(0));
			List<SystemEntity> listByIds3 = systemService.getListByIds(collect3, null);
			List<SystemSelectorVO> systemSelectorVOList3 = JsonUtil.getJsonToList(listByIds3, SystemSelectorVO.class);
			systemSelectorVOList3.forEach(t -> {
				t.setIsPermission(2);
				t.setDisabled(true);
			});
			systemSelectedList.addAll(systemSelectorVOList1.stream().map(SystemSelectorVO::getId).collect(Collectors.toList()));
			systemSelectedList.addAll(systemSelectorVOList3.stream().map(SystemSelectorVO::getId).collect(Collectors.toList()));

			systemPermissionList.addAll(systemSelectorVOList1);
			systemPermissionList.addAll(systemSelectorVOList2);
			systemPermissionList.addAll(systemSelectorVOList3);
			systemPermissionList = systemPermissionList.stream().sorted(Comparator.comparing(SystemSelectorVO::getSortCode).thenComparing(Comparator.comparing(SystemSelectorVO::getCreatorTime).reversed())).collect(Collectors.toList());


			// 菜单
			List<OrganizeAdministratorEntity> moduleOrganizeAdministratorEntity = organizeAdminIsTratorService.getOrganizeAdministratorEntity(userId, PermissionConst.MODULE, false);
			List<String> modulePermissionIds = moduleOrganizeAdministratorEntity.stream().map(OrganizeAdministratorEntity::getOrganizeId).collect(Collectors.toList());
			List<String> moduleCollect = moduleSelectorVOList.stream().map(ModuleSelectorModel::getId).collect(Collectors.toList());
			List<String> moduleList = new ArrayList<>();
			moduleList.addAll(modulePermissionIds);
			moduleList.addAll(moduleCollect);
			// 交集  1
			List<String> moduleCollect1 = modulePermissionIds.stream().filter(moduleCollect::contains).collect(Collectors.toList());
			moduleList = moduleList.stream().distinct().collect(Collectors.toList());
			// 去掉交集后
			moduleList.removeAll(moduleCollect1);
			// 我有他没有 0
			List<String> moduleCollect2 = moduleCollect.stream().filter(moduleList::contains).collect(Collectors.toList());
			// 他有我没有 2
			List<String> moduleCollect3 = modulePermissionIds.stream().filter(moduleList::contains).collect(Collectors.toList());
			List<ModuleEntity> moduleListByIds1 = moduleService.getModuleByIds(moduleCollect1);
			List<ModuleSelectorModel> moduleSelectorVOList1 = JsonUtil.getJsonToList(moduleListByIds1, ModuleSelectorModel.class);
			moduleSelectorVOList1.forEach(t -> t.setIsPermission(1));
			List<ModuleEntity> moduleListByIds2 = moduleService.getModuleByIds(moduleCollect2);
			List<ModuleSelectorModel> moduleSelectorVOList2 = JsonUtil.getJsonToList(moduleListByIds2, ModuleSelectorModel.class);
			moduleSelectorVOList2.forEach(t -> t.setIsPermission(0));
			List<ModuleEntity> moduleListByIds3 = moduleService.getModuleByIds(moduleCollect3);
			List<ModuleSelectorModel> moduleSelectorVOList3 = JsonUtil.getJsonToList(moduleListByIds3, ModuleSelectorModel.class);
			moduleSelectorVOList3.forEach(t -> {
				t.setIsPermission(2);
				t.setDisabled(true);
			});
			modulePermissionList.addAll(moduleSelectorVOList1);
			modulePermissionList.addAll(moduleSelectorVOList2);
			modulePermissionList.addAll(moduleSelectorVOList3);
			Set<ModuleSelectorModel> tempModule = new HashSet<>(modulePermissionList);
			// 处理上级
			List<ModuleSelectorModel> finalModulePermissionList = modulePermissionList;
			modulePermissionList.forEach(t -> {
				// 判断上级是否存在，不存在的话取出放入总权限列表
				parentIdInList(finalModulePermissionList, tempModule, allModuleSelectorVOList, t, t);
			});
			modulePermissionList = new ArrayList<>(tempModule);
			modulePermissionList.forEach(t -> {
				if ("-1".equals(t.getParentId())) {
					t.setParentId(t.getSystemId());
				}
			});
			// 加入主系统
			ModuleSelectorModel jsonToBean = JsonUtil.getJsonToBean(mainSystem, ModuleSelectorModel.class);
			if (moduleSelectorVOList1.stream().anyMatch(t -> t.getId().equals(jsonToBean.getId()))) {
				jsonToBean.setIsPermission(1);
			} else if (moduleSelectorVOList2.stream().anyMatch(t -> t.getId().equals(jsonToBean.getId()))) {
				jsonToBean.setIsPermission(0);
			} else if (moduleSelectorVOList3.stream().anyMatch(t -> t.getId().equals(jsonToBean.getId()))) {
				jsonToBean.setIsPermission(2);
				jsonToBean.setDisabled(true);
			}
			moduleSelectedList.addAll(moduleSelectorVOList1.stream().map(ModuleSelectorModel::getId).collect(Collectors.toList()));
			moduleSelectedList.addAll(moduleSelectorVOList3.stream().map(ModuleSelectorModel::getId).collect(Collectors.toList()));

			jsonToBean.setType(0);
			jsonToBean.setParentId("-1");
			modulePermissionList.add(jsonToBean);
			modulePermissionList = modulePermissionList.stream().sorted(Comparator.comparing(ModuleSelectorModel::getSortCode).thenComparing(Comparator.comparing(ModuleSelectorModel::getCreatorTime).reversed())).collect(Collectors.toList());
			List<SumTree<ModuleSelectorModel>> menuList = TreeDotUtils.convertListToTreeDotFilter(modulePermissionList);
			moduleVOPermissionList = JsonUtil.getJsonToList(menuList, ModuleSelectorVO.class);
		}

		// 判断断层有没有上下级关系
		Map<String, OrganizeAdministratorSelectorModel> selectorModelMap = selectorModels.stream().collect(Collectors.toMap(OrganizeAdministratorSelectorModel::getId, Function.identity()));

		Map<String, String> orgIdNameMaps = organizeService.getInfoList();
		selectorModels.forEach(t -> {
			if (StringUtil.isNotEmpty(t.getOrganizeIdTree())) {
				List<String> list1 = new ArrayList<>();
				String[] split = t.getOrganizeIdTree().split(",");
				list1 = Arrays.asList(split);
				Collections.reverse(list1);
				for (String orgId : list1) {
					OrganizeAdministratorSelectorModel organizeEntity1 = selectorModelMap.get(orgId);
					if (organizeEntity1 != null && !organizeEntity1.getId().equals(t.getId())) {
						t.setParentId(organizeEntity1.getId());
						String[] split1 = t.getOrganizeIdTree().split(organizeEntity1.getId());
						if (split1.length > 1) {
							t.setFullName(organizeService.getFullNameByOrgIdTree(orgIdNameMaps, split1[1], "/"));
						}
						break;
					}
				}
			}
		});
		selectorModels.forEach(t -> t.setIcon(StringUtil.isNotEmpty(t.getCategory()) ? "company".equals(t.getCategory()) ? "icon-ym icon-ym-tree-organization3" : "icon-ym icon-ym-tree-department1" : ""));
		List<SumTree<OrganizeAdministratorSelectorModel>> trees = TreeDotUtils.convertListToTreeDot(selectorModels);
		List<OrganizeAdministratorSelectorVO> listVO = JsonUtil.getJsonToList(trees, OrganizeAdministratorSelectorVO.class);
		listVO.forEach(t -> {
			t.setFullName(organizeService.getFullNameByOrgIdTree(orgIdNameMaps, t.getOrganizeIdTree(), "/"));
		});
		vo.setOrgAdminList(listVO);
		vo.setSystemPermissionList(systemPermissionList);
		vo.setModulePermissionList(moduleVOPermissionList);
		vo.setSystemIds(systemSelectedList);
		vo.setModuleIds(moduleSelectedList);
		return ActionResult.success(vo);
	}

	private void parentIdInList(List<ModuleSelectorModel> modulePermissionList, Set<ModuleSelectorModel> tempModule,
								List<ModuleSelectorModel> allModuleSelectorVOList, ModuleSelectorModel moduleSelectorModel,
								ModuleSelectorModel moduleSelectorModels) {
		ModuleSelectorModel finalModuleSelectorModel = moduleSelectorModel;
		moduleSelectorModel = modulePermissionList.stream().filter(t -> finalModuleSelectorModel.getParentId().equals(t.getId())).findFirst().orElse(null);
		// !null return  ==1 return
		if (moduleSelectorModel != null && moduleSelectorModel.getType() == 1) {
			tempModule.add(moduleSelectorModel);
			moduleSelectorModels.setParentId(moduleSelectorModel.getId());
			return;
		}
		moduleSelectorModel = allModuleSelectorVOList.stream().filter(t -> finalModuleSelectorModel.getParentId().equals(t.getId())).findFirst().orElse(null);
		if (moduleSelectorModel == null) {
			return;
		}
		parentIdInList(modulePermissionList, tempModule, allModuleSelectorVOList, moduleSelectorModel, moduleSelectorModels);
	}

	
	@GetMapping("/getListByUserId")
	public List<OrganizeAdministratorEntity> getListByUserId(@RequestParam(value = "userId", required = false) String userId,
															 @RequestParam(value = "type", required = false) String type) {
		return organizeAdminIsTratorService.getOrganizeAdministratorEntity(userId, type, false);
	}

	
	@GetMapping("/getOrganizeList")
	public List<String> getOrganizeUserList(@RequestParam(value = "type", required = false) String type) {
		return organizeAdminIsTratorService.getOrganizeUserList(type);
	}

	
	@GetMapping("/getOrganizeAdministratorList")
	public OrganizeAdministratorModel getOrganizeAdministratorList() {
		OrganizeAdministratorModel model = organizeAdminIsTratorService.getOrganizeAdministratorList();
		return model;
	}

	
	@PostMapping("/saveOrganizeAdminTrator")
	public boolean saveOrganizeAdminTrator(@RequestBody OrganizeAdministratorEntity entity) {
		organizeAdminIsTratorService.create(entity);
		return true;
	}

	
	@NoDataSourceBind
	@PostMapping("/getInfoByUserId")
	public List<OrganizeAdministratorEntity> getInfoByUserId(@RequestParam(value = "userId", required = false) String userId,
															 @RequestParam(value = "tenantId", required = false) String tenantId) {
		//判断是否为多租户
		if (configValueUtil.isMultiTenancy()) {
			TenantDataSourceUtil.switchTenant(tenantId);
		}
		return organizeAdminIsTratorService.getInfoByUserId(userId, tenantId);
	}
}
