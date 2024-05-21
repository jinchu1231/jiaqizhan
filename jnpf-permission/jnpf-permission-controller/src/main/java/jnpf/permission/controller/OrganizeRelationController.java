package jnpf.permission.controller;

import jnpf.base.controller.SuperController;
import jnpf.permission.entity.OrganizeRelationEntity;
import jnpf.permission.entity.PermissionGroupEntity;
import jnpf.permission.model.organize.OrganizeConditionModel;
import jnpf.permission.model.organize.OrganizeModel;
import jnpf.permission.model.organizerelation.AutoGetMajorOrgIdModel;
import jnpf.permission.service.OrganizeRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 组织关系控制器
 *
 * @author ：JNPF开发平台组
 * @version: V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date ：2022/4/7 10:27
 */
@RestController
@RequestMapping("/api/permission/OrganizeRelation")
public class OrganizeRelationController extends SuperController<OrganizeRelationService, OrganizeRelationEntity> {

    @Autowired
    private OrganizeRelationService organizeRelationService;

    
    @GetMapping("/existByRoleIdAndOrgId")
    public Boolean existByRoleIdAndOrgId(@RequestParam("roleId") String roleId, @RequestParam("orgId") String orgId) {
        return organizeRelationService.existByRoleIdAndOrgId(roleId, orgId);
    }

    
    @GetMapping("/checkBasePermission")
    public List<PermissionGroupEntity> checkBasePermission(@RequestParam("userId") String userId, @RequestParam("orgId") String orgId, @RequestParam(value = "systemId", required = false) String systemId) {
        return organizeRelationService.checkBasePermission(userId, orgId, systemId);
    }

    
    @PostMapping("/checkBasePermission")
    public String autoGetMajorOrganizeId(@RequestBody AutoGetMajorOrgIdModel autoGetMajorOrgIdModel) {
        return organizeRelationService.autoGetMajorOrganizeId(autoGetMajorOrgIdModel.getUserId(), autoGetMajorOrgIdModel.getOrgIds(), autoGetMajorOrgIdModel.getOrganizeId(), autoGetMajorOrgIdModel.getSystemId());
    }

    
    @GetMapping("/autoGetMajorPositionId")
    public String autoGetMajorPositionId(@RequestParam("userId")String userId, @RequestParam("organizeId") String organizeId, @RequestParam( value = "positionId", required = false) String positionId) {
        return organizeRelationService.autoGetMajorPositionId(userId, organizeId, positionId);
    }

    
    @PostMapping("/getRelationListByOrganizeId")
    public List<OrganizeRelationEntity> getRelationListByOrganizeId(@RequestBody List<String> ableIds,@RequestParam("type") String type) {
        return organizeRelationService.getRelationListByOrganizeId( ableIds, type);
    }

    
    @PostMapping("/getOrgIds")
    public List<String> getOrgIds(@RequestBody List<String> departIds) {
        return organizeRelationService.getOrgIds(departIds,null);
    }

    
    @PostMapping("/getOrgIdsList")
    public List<OrganizeModel> getOrgIdsList(@RequestBody OrganizeConditionModel organizeConditionModel) {
        return organizeRelationService.getOrgIdsList(organizeConditionModel);
    }
}
