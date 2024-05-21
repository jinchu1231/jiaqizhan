package jnpf.integrate.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.dynamic.datasource.annotation.DSTransactional;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jnpf.base.ActionResult;
import jnpf.base.service.SuperServiceImpl;
import jnpf.constant.MsgCode;
import jnpf.exception.WorkFlowException;
import jnpf.integrate.entity.IntegrateEntity;
import jnpf.integrate.job.Integrate;
import jnpf.integrate.job.IntegrateJobUtil;
import jnpf.integrate.job.QuartzUtil;
import jnpf.integrate.mapper.IntegrateMapper;
import jnpf.integrate.model.childnode.IntegrateChildNode;
import jnpf.integrate.model.childnode.IntegrateProperties;
import jnpf.integrate.model.integrate.IntegratePagination;
import jnpf.integrate.model.nodeJson.IntegrateModel;
import jnpf.integrate.service.IntegrateQueueService;
import jnpf.integrate.service.IntegrateService;
import jnpf.util.JsonUtil;
import jnpf.util.RandomUtil;
import jnpf.util.RedisUtil;
import jnpf.util.StringUtil;
import jnpf.util.UserProvider;
import org.apache.commons.lang3.StringUtils;
import org.quartz.JobDataMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.UUID;

@Service
public class IntegrateServiceImpl extends SuperServiceImpl<IntegrateMapper, IntegrateEntity> implements IntegrateService {

    @Autowired
    private UserProvider userProvider;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private IntegrateQueueService integrateQueueService;

    @Override
    public List<IntegrateEntity> getList(IntegratePagination pagination) {
        return getList(pagination, true);
    }

    @Override
    public List<IntegrateEntity> getList(IntegratePagination pagination, boolean isPage) {
        QueryWrapper<IntegrateEntity> queryWrapper = new QueryWrapper<>();
        if (StringUtil.isNotEmpty(pagination.getKeyword())) {
            queryWrapper.lambda().and(
                    t -> t.like(IntegrateEntity::getFullName, pagination.getKeyword())
                            .or().like(IntegrateEntity::getEnCode, pagination.getKeyword())
            );
        }
        if (ObjectUtil.isNotEmpty(pagination.getType())) {
            queryWrapper.lambda().eq(IntegrateEntity::getType, pagination.getType());
        }
        if (ObjectUtil.isNotEmpty(pagination.getFormId())) {
            queryWrapper.lambda().eq(IntegrateEntity::getFormId, pagination.getFormId());
        }
        if (ObjectUtil.isNotEmpty(pagination.getTrigger())) {
            queryWrapper.lambda().eq(IntegrateEntity::getTriggerType, pagination.getTrigger());
        }
        if (ObjectUtil.isNotEmpty(pagination.getEnabledMark())) {
            queryWrapper.lambda().eq(IntegrateEntity::getEnabledMark, pagination.getEnabledMark());
        }
        //排序
        if (StringUtils.isEmpty(pagination.getSidx())) {
            queryWrapper.lambda().orderByDesc(IntegrateEntity::getCreatorTime);
        } else {
            queryWrapper = "asc".equals(pagination.getSort().toLowerCase()) ? queryWrapper.orderByAsc(pagination.getSidx()) : queryWrapper.orderByDesc(pagination.getSidx());
        }
        if (isPage) {
            Page<IntegrateEntity> page = new Page<>(pagination.getCurrentPage(), pagination.getPageSize());
            IPage<IntegrateEntity> userPage = page(page, queryWrapper);
            return pagination.setData(userPage.getRecords(), page.getTotal());
        }
        return this.list(queryWrapper);
    }

    @Override
    public IntegrateEntity getInfo(String id) {
        QueryWrapper<IntegrateEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(IntegrateEntity::getId, id);
        return this.getOne(queryWrapper);
    }

    @Override
    public Boolean isExistByFullName(String fullName, String id) {
        QueryWrapper<IntegrateEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(IntegrateEntity::getFullName, fullName);
        if (!StringUtil.isEmpty(id)) {
            queryWrapper.lambda().ne(IntegrateEntity::getId, id);
        }
        return this.count(queryWrapper) > 0;
    }

    @Override
    public Boolean isExistByEnCode(String encode, String id) {
        QueryWrapper<IntegrateEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(IntegrateEntity::getEnCode, encode);
        if (!StringUtil.isEmpty(id)) {
            queryWrapper.lambda().ne(IntegrateEntity::getId, id);
        }
        return this.count(queryWrapper) > 0;
    }

    @Override
    public void create(IntegrateEntity entity) {
        if (StringUtil.isEmpty(entity.getId())) {
            entity.setId(RandomUtil.uuId());
        }
        entity.setCreatorTime(new Date());
        entity.setCreatorUserId(userProvider.get().getUserId());
        formId(entity);
        this.save(entity);
    }

    @Override
    @DSTransactional
    public ActionResult ImportData(IntegrateEntity entity, Integer type) throws WorkFlowException {
        if (entity != null) {
            StringJoiner errList = new StringJoiner("、");
            String copyNum = UUID.randomUUID().toString().substring(0, 5);
            if (this.getInfo(entity.getId()) != null) {
                if (Objects.equals(type, 0)) {
                    errList.add("ID");
                } else {
                    entity.setId(RandomUtil.uuId());
                }
            }
            if (this.isExistByEnCode(entity.getEnCode(),null) ) {
                if (Objects.equals(type, 0)) {
                    errList.add("编码");
                } else {
                    entity.setEnCode(entity.getEnCode() + copyNum);
                }
            }
            if (this.isExistByFullName(entity.getFullName(), null) ) {
                if (Objects.equals(type, 0)) {
                    errList.add("名称");
                } else {
                    entity.setFullName(entity.getFullName() + ".副本" + copyNum);
                }
            }
            if (Objects.equals(type, 0) && errList.length() > 0) {
                return ActionResult.fail(errList + "重复");
            }
            try {
                this.setIgnoreLogicDelete().removeById(entity.getId());
                entity.setId(RandomUtil.uuId());
                entity.setCreatorTime(new Date());
                entity.setCreatorUserId(UserProvider.getLoginUserId());
                entity.setLastModifyTime(null);
                entity.setLastModifyUserId(null);
                entity.setEnabledMark(0);
                this.setIgnoreLogicDelete().saveOrUpdate(entity);
                this.clearIgnoreLogicDelete();
            } catch (Exception e) {
                throw new WorkFlowException(MsgCode.IMP003.get());
            }
            return ActionResult.success(MsgCode.IMP001.get());
        }
        return ActionResult.fail("导入数据格式不正确");
    }

    @Override
    public Boolean update(String id, IntegrateEntity entity, boolean state) {
        entity.setId(id);
        IntegrateEntity info = getInfo(id);
        if (Objects.equals(entity.getEnabledMark(), 0) || !Objects.equals(info.getTemplateJson(), entity.getTemplateJson())) {
            deleteJob(entity);
        }
        if (ObjectUtil.isEmpty(entity.getEnabledMark())) {
            entity.setEnabledMark(info.getEnabledMark());
        }
        if (ObjectUtil.isEmpty(entity.getType())) {
            entity.setType(info.getType());
        }
        if ((!Objects.equals(info.getTemplateJson(), entity.getTemplateJson()) || state) && Objects.equals(entity.getEnabledMark(), 1)) {
            formId(entity);
        }
        entity.setLastModifyTime(new Date());
        entity.setLastModifyUserId(userProvider.get().getUserId());
        return this.updateById(entity);
    }

    @Override
    public void delete(IntegrateEntity entity) {
        deleteJob(entity);
        this.removeById(entity.getId());
    }

    private void formId(IntegrateEntity entity) {
        Integer enabledMark = entity.getEnabledMark();
        String templateJson = entity.getTemplateJson();
        if (StringUtil.isNotEmpty(templateJson)) {
            IntegrateChildNode childNode = JsonUtil.getJsonToBean(templateJson, IntegrateChildNode.class);
            IntegrateProperties properties = childNode.getProperties();
            String formId = properties.getFormId();
            entity.setFormId(formId);
            entity.setTriggerType(properties.getTriggerEvent());
            if (Objects.equals(enabledMark, 1)) {
                if (Objects.equals(entity.getType(), 2)) {
                    IntegrateModel model = new IntegrateModel();
                    model.setId(entity.getId());
                    IntegrateJobUtil.removeModel(model, redisUtil);
                    IntegrateModel integrateModel = JsonUtil.getJsonToBean(properties, IntegrateModel.class);
                    integrateModel.setUserInfo(userProvider.get());
                    integrateModel.setId(entity.getId());
                    integrateModel.setState(0);
                    JobDataMap jobDataMap = new JobDataMap();
                    Date startTime = ObjectUtil.isNotEmpty(properties.getStartTime()) ? new Date(properties.getStartTime()) : new Date();
                    Date endTime = Objects.equals(properties.getEndTimeType(), 2) ? new Date(properties.getEndTime()) : null;
                    integrateModel.setEndTime(endTime != null ? endTime.getTime() : null);
                    integrateModel.setStartTime(startTime.getTime());
                    jobDataMap.putAll(JsonUtil.entityToMap(integrateModel));
                    redisUtil.removeHash(IntegrateJobUtil.IDGENERATOR_REDIS_KEY, entity.getId());
                    boolean isAdd = ObjectUtil.isNotEmpty(endTime) ? endTime.getTime() > System.currentTimeMillis() : true;
                    if (isAdd) {
                        QuartzUtil.addJob(entity.getId(), properties.getCron(), Integrate.class, jobDataMap, startTime, endTime);
                    }
                }
            }
        }
    }

    private void deleteJob(IntegrateEntity entity) {
        QuartzUtil.deleteJob(entity.getId());
        integrateQueueService.delete(entity.getId());
        IntegrateModel model = new IntegrateModel();
        model.setId(entity.getId());
        IntegrateJobUtil.removeModel(model, redisUtil);
    }

}
