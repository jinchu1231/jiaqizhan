package jnpf.util;

import com.baomidou.dynamic.datasource.annotation.DS;
import jnpf.base.Page;
import jnpf.base.Pagination;
import jnpf.base.entity.DictionaryDataEntity;
import jnpf.base.service.BillRuleService;
import jnpf.base.service.DataInterfaceService;
import jnpf.base.service.DbLinkService;
import jnpf.base.service.DbTableService;
import jnpf.base.service.DictionaryDataService;
import jnpf.base.service.DictionaryTypeService;
import jnpf.database.model.dbfield.DbFieldModel;
import jnpf.database.model.dbfield.base.DbFieldModelBase;
import jnpf.database.model.dbtable.DbTableFieldModel;
import jnpf.database.model.entity.DbLinkEntity;
import jnpf.permission.entity.OrganizeEntity;
import jnpf.permission.entity.PositionEntity;
import jnpf.permission.entity.RoleEntity;
import jnpf.permission.entity.UserEntity;
import jnpf.permission.entity.UserRelationEntity;
import jnpf.permission.model.user.UserByRoleVO;
import jnpf.permission.service.OrganizeService;
import jnpf.permission.service.PositionService;
import jnpf.permission.service.RoleService;
import jnpf.permission.service.UserRelationService;
import jnpf.permission.service.UserService;
import jnpf.util.enums.DictionaryDataEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author ：JNPF开发平台组
 * @version: V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date ：2022/4/9 13:28
 */
@Component
@DS("")
public class ServiceBaseUtil {

    @Autowired
    private DbLinkService dblinkService;
    @Autowired
    private DbTableService dbTableService;
    @Autowired
    private DictionaryDataService dictionaryDataService;
    @Autowired
    private DictionaryTypeService dictionaryTypeService;
    @Autowired
    private UserRelationService userRelationService;
    @Autowired
    private UserService userService;
    @Autowired
    private RoleService roleService;
    @Autowired
    private OrganizeService organizeService;
    @Autowired
    private PositionService positionService;
    @Autowired
    private BillRuleService billRuleService;
    @Autowired
    private DataInterfaceService dataInterfaceService;

    //--------------------------------数据连接------------------------------
    public DbLinkEntity getDbLink(String dbLink) {
        DbLinkEntity link = StringUtil.isNotEmpty(dbLink) ? dblinkService.getInfo(dbLink) : null;
        return link;
    }

    public void createTable(List<DbTableFieldModel> dbTable) throws Exception {
        for (DbTableFieldModel dbTableFieldModel : dbTable) {
            dbTableService.createTable(dbTableFieldModel);
        }
    }

    public void addField(DbTableFieldModel dbTable) throws Exception {
        dbTableService.addField(dbTable);
    }

    public List<DbFieldModelBase> getDbTableModel(String linkId, String table) throws Exception{
        List<DbFieldModel> dbFieldModelList = dbTableService.getDbTableModel(linkId, table).getDbFieldModelList();
        List<DbFieldModelBase> list = JsonUtil.getJsonToList(dbFieldModelList,DbFieldModelBase.class);
        return list;
    }

    /**
     * 获取所有字段
     * @param linkId 链接名
     * @param table 表名
     * @return
     * @throws Exception
     */
    public List<DbFieldModel> getFieldList(String linkId, String table) throws Exception {
        return dbTableService.getFieldList(linkId, table);
    }

    //--------------------------------数据字典------------------------------
    public List<DictionaryDataEntity> getDiList() {
        List<DictionaryDataEntity> dictionList = dictionaryDataService.getList(dictionaryTypeService.getInfoByEnCode(DictionaryDataEnum.FLOWWOEK_ENGINE.getDictionaryTypeId()).getId());
        return dictionList;
    }

    public List<DictionaryDataEntity> getDictionName(List<String> id) {
        List<DictionaryDataEntity> dictionList = dictionaryDataService.getDictionName(id);
        return dictionList;
    }

    //--------------------------------用户关系表------------------------------
    public List<UserRelationEntity> getListByUserIdAll(List<String> id) {
        List<UserRelationEntity> list = userRelationService.getListByUserIdAll(id);
        return list;
    }

    public List<UserRelationEntity> getListByObjectIdAll(List<String> id) {
        List<UserRelationEntity> list = userRelationService.getListByObjectIdAll(id);
        return list;
    }

    public String getAdmin() {
        UserEntity admin = userService.getUserByAccount("admin");
        return admin.getId();
    }

    //--------------------------------用户------------------------------
    public List<UserEntity> getUserName(List<String> id) {
        List<UserEntity> list = getUserName(id, false);
        return list;
    }

    public List<UserEntity> getListByManagerId(String managerId) {
        List<UserEntity> list = StringUtil.isNotEmpty(managerId) ? userService.getListByManagerId(managerId, null) : new ArrayList<>();
        return list;
    }

    public List<UserEntity> getUserName(List<String> id, boolean enableMark) {
        List<UserEntity> list = userService.getUserName(id);
        if (enableMark) {
            list = list.stream().filter(t -> t.getEnabledMark() != 0).collect(Collectors.toList());
        }
        return list;
    }

    public List<UserEntity> getUserName(List<String> id, Pagination pagination) {
        List<UserEntity> list = userService.getUserName(id, pagination);
        return list;
    }

    public UserEntity getUserInfo(String id) {
        UserEntity entity = null;
        if (StringUtil.isNotEmpty(id)) {
            entity = id.equalsIgnoreCase("admin") ? userService.getUserByAccount(id) : userService.getInfo(id);
        }
        return entity;
    }

    public UserEntity getByRealName(String realName) {
        UserEntity entity = StringUtil.isNotEmpty(realName) ? userService.getByRealName(realName) : null;
        return entity;
    }

    public List<UserByRoleVO> getListByAuthorize(String organizeId) {
        List<UserByRoleVO> list = userService.getListByAuthorize(organizeId, new Page());
        return list;
    }

    //--------------------------------单据规则------------------------------
    public String getBillNumber(String enCode) {
        String billNo = "";
        try {
            billNo = billRuleService.getBillNumber(enCode, false);
        } catch (Exception e) {

        }
        return billNo;
    }

    public void useBillNumber(String enCode) {
        billRuleService.useBillNumber(enCode);
    }

    //--------------------------------角色------------------------------
    public List<RoleEntity> getListByIds(List<String> id) {
        List<RoleEntity> list = roleService.getListByIds(id, null, false);
        return list;
    }

    //--------------------------------组织------------------------------
    public List<OrganizeEntity> getOrganizeName(List<String> id) {
        List<OrganizeEntity> list = organizeService.getOrganizeName(id);
        return list;
    }

    public OrganizeEntity getOrganizeInfo(String id) {
        OrganizeEntity entity = StringUtil.isNotEmpty(id) ? organizeService.getInfo(id) : null;
        return entity;
    }

    public OrganizeEntity getOrganizeFullName(String fullName) {
        OrganizeEntity entity = organizeService.getByFullName(fullName);
        return entity;
    }

    public List<OrganizeEntity> getOrganizeId(String organizeId) {
        List<OrganizeEntity> organizeList = new ArrayList<>();
        organizeService.getOrganizeId(organizeId, organizeList);
        Collections.reverse(organizeList);
        return organizeList;
    }

    public List<OrganizeEntity> getDepartmentAll(String organizeId) {
        List<OrganizeEntity> departmentAll = organizeService.getDepartmentAll(organizeId);
        return departmentAll;
    }

    /**
     * 获取当前组织名称（all-显示组织名,else 显示部门名）
     *
     * @param obj
     * @param showLevel
     * @return
     */
    public String getCurrentOrganizeName(Object obj, String showLevel) {
        if(obj==null){
            return null;
        }
        String value=String.valueOf(obj);
        String orgName = "";
        if (value != null) {
            String orgId = "";
            try {
                List<String> jsonToList = JsonUtil.getJsonToList(value, String.class);
                orgId = jsonToList.get(jsonToList.size() - 1);
            } catch (Exception e) {
                orgId = value;
            }
            OrganizeEntity organizeEntity = this.getOrganizeInfo(orgId);
            if ("all".equals(showLevel)) {
                if (organizeEntity != null) {
                    List<OrganizeEntity> organizeList = this.getOrganizeId(organizeEntity.getId());
                    orgName = organizeList.stream().map(OrganizeEntity::getFullName).collect(Collectors.joining("/"));
                }
            } else {
                if (organizeEntity != null) {
                    orgName = organizeEntity.getFullName();
                } else {
                    orgName = " ";
                }
            }
        }
        return orgName;
    }

    //--------------------------------岗位------------------------------
    public List<PositionEntity> getPositionName(List<String> id) {
        List<PositionEntity> list = positionService.getPositionName(id, null);
        return list;
    }

    public PositionEntity getPositionFullName(String fullName) {
        PositionEntity entity = positionService.getByFullName(fullName);
        return entity;
    }

    public PositionEntity getPositionInfo(String id) {
        PositionEntity entity = StringUtil.isNotEmpty(id) ? positionService.getInfo(id) : null;
        return entity;
    }

    //--------------------------------远端------------------------------
    public void infoToId(String interId, Map<String, String> parameterMap) {
        dataInterfaceService.infoToId(interId, null, parameterMap);
    }

}
