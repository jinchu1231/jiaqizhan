package jnpf.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jnpf.base.UserInfo;
import jnpf.base.entity.ModuleEntity;
import jnpf.base.model.module.ModuleModel;
import jnpf.base.service.ModuleService;
import jnpf.base.service.SuperServiceImpl;
import jnpf.engine.entity.FlowTemplateEntity;
import jnpf.engine.model.flowengine.FlowPagination;
import jnpf.engine.service.FlowTemplateService;
import jnpf.entity.AppDataEntity;
import jnpf.mapper.AppDataMapper;
import jnpf.model.AppDataListAllVO;
import jnpf.model.AppFlowListAllVO;
import jnpf.model.UserMenuModel;
import jnpf.permission.model.authorize.AuthorizeVO;
import jnpf.permission.service.AuthorizeService;
import jnpf.service.AppDataService;
import jnpf.util.JsonUtil;
import jnpf.util.RandomUtil;
import jnpf.util.StringUtil;
import jnpf.util.UserProvider;
import jnpf.util.treeutil.SumTree;
import jnpf.util.treeutil.newtreeutil.TreeDotUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * app常用数据
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021-08-08
 */
@Service
public class AppDataServiceImpl extends SuperServiceImpl<AppDataMapper, AppDataEntity> implements AppDataService {

    @Autowired
    private UserProvider userProvider;
    @Autowired
    private ModuleService moduleService;
    @Autowired
    private AuthorizeService authorizeService;
    @Autowired
    private FlowTemplateService flowTemplateService;

    @Override
    public List<AppDataEntity> getList(String type) {
        UserInfo userInfo = userProvider.get();
        QueryWrapper<AppDataEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(AppDataEntity::getObjectType, type).eq(AppDataEntity::getCreatorUserId, userInfo.getUserId());
        if ("2".equals(type)) {
            List<String> moduleEntities = menuList().stream().map(ModuleModel::getId).collect(Collectors.toList());
            if (moduleEntities.size() == 0) {
                return new ArrayList<>();
            }
            queryWrapper.lambda().in(AppDataEntity::getObjectId, moduleEntities);
        }
        List<AppDataEntity> list = this.list(queryWrapper);
        list = list.stream().filter(t -> StringUtil.isNotEmpty(t.getSystemId()) && t.getSystemId().equals(userInfo.getAppSystemId())).collect(Collectors.toList());
        List<String> idAll = list.stream().map(AppDataEntity::getObjectId).collect(Collectors.toList());
        List<FlowTemplateEntity> templateList = flowTemplateService.getTemplateList(idAll).stream().filter(t -> t.getEnabledMark() == 1).collect(Collectors.toList());
        for (int i = 0; i < list.size(); i++) {
            AppDataEntity appDataEntity = list.get(i);
            if ("2".equals(type)) {
                ModuleEntity info = moduleService.getInfo(appDataEntity.getObjectId());
                if (info == null || info.getEnabledMark() == 0) {
                    list.remove(i);
                    i--;
                }
            } else {
                FlowTemplateEntity templateEntity = templateList.stream().filter(t -> t.getId().equals(appDataEntity.getObjectId())).findFirst().orElse(null);
                if (templateEntity == null) {
                    list.remove(i);
                    i--;
                }
            }
        }
        return list;
    }

    @Override
    public List<AppDataEntity> getList() {
        QueryWrapper<AppDataEntity> queryWrapper = new QueryWrapper<>();
        return this.list(queryWrapper);
    }

    @Override
    public AppDataEntity getInfo(String objectId) {
        UserInfo userInfo = userProvider.get();
        QueryWrapper<AppDataEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(AppDataEntity::getObjectId, objectId).eq(AppDataEntity::getCreatorUserId, userInfo.getUserId());
        return this.getOne(queryWrapper);
    }

    @Override
    public boolean isExistByObjectId(String objectId, String systemId) {
        UserInfo userInfo = userProvider.get();
        QueryWrapper<AppDataEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(AppDataEntity::getObjectId, objectId)
                .eq(AppDataEntity::getCreatorUserId, userInfo.getUserId())
                .eq(AppDataEntity::getSystemId, systemId);
        return this.count(queryWrapper) > 0 ? true : false;
    }

    @Override
    public void create(AppDataEntity entity) {
        UserInfo userInfo = userProvider.get();
        entity.setId(RandomUtil.uuId());
        entity.setCreatorUserId(userInfo.getUserId());
        entity.setCreatorTime(new Date());
        entity.setEnabledMark(1);
        entity.setSystemId(userInfo.getAppSystemId());
        this.save(entity);
    }

    @Override
    public void delete(AppDataEntity entity) {
        this.removeById(entity.getId());
    }

    @Override
    public void delete(String objectId) {
        QueryWrapper<AppDataEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(AppDataEntity::getObjectId, objectId);
        this.remove(queryWrapper);
    }

    @Override
    public List<AppFlowListAllVO> getFlowList(FlowPagination pagination) {
        List<String> objectId = getList("1").stream().map(AppDataEntity::getObjectId).collect(Collectors.toList());;
        List<FlowTemplateEntity> pageList = flowTemplateService.getListAll(pagination,true);
        List<AppFlowListAllVO> result = new ArrayList<>();
        for (FlowTemplateEntity entity : pageList) {
            AppFlowListAllVO vo = JsonUtil.getJsonToBean(entity, AppFlowListAllVO.class);
            vo.setIsData(objectId.contains(vo.getId()));
            result.add(vo);
        }
        return pagination.setData(result, pagination.getTotal());
    }

    @Override
    public List<AppDataListAllVO> getDataList(String type) {
        List<AppDataEntity> dataList = getList(type);
        AuthorizeVO authorizeModel = authorizeService.getAuthorize(true, false);
        List<ModuleModel> buttonList = authorizeModel.getModuleList();
        List<ModuleModel> menuList = menuList();
        List<UserMenuModel> list = new LinkedList<>();
        for (ModuleModel module : menuList) {
            boolean count = buttonList.stream().filter(t -> t.getId().equals(module.getId())).count() > 0;
            UserMenuModel userMenuModel = JsonUtil.getJsonToBean(module, UserMenuModel.class);
            if (count) {
                boolean isData = dataList.stream().filter(t -> t.getObjectId().equals(module.getId())).count() > 0;
                userMenuModel.setIsData(isData);
                list.add(userMenuModel);
            }
        }
        List<SumTree<UserMenuModel>> menuAll = TreeDotUtils.convertListToTreeDot(list);
        List<AppDataListAllVO> menuListAll = JsonUtil.getJsonToList(menuAll, AppDataListAllVO.class);
        List<AppDataListAllVO> data = new LinkedList<>();
        for (AppDataListAllVO appMenu : menuListAll) {
            if ("-1".equals(appMenu.getParentId())) {
                data.add(appMenu);
            }
        }
        return data;
    }

    private List<ModuleModel> menuList() {
        String appSystemId = userProvider.get().getAppSystemId();
        AuthorizeVO authorizeModel = authorizeService.getAuthorizeByUser(false);
        List<ModuleModel> menuList = authorizeModel.getModuleList().stream().filter(t -> "App".equals(t.getCategory()) && t.getSystemId().equals(appSystemId)).collect(Collectors.toList());
        return menuList;
    }

}
