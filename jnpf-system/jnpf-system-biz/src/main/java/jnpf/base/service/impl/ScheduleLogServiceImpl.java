package jnpf.base.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jnpf.base.UserInfo;
import jnpf.base.entity.ScheduleLogEntity;
import jnpf.base.mapper.ScheduleLogMapper;
import jnpf.base.service.ScheduleLogService;
import jnpf.base.service.SuperServiceImpl;
import jnpf.util.RandomUtil;
import jnpf.util.UserProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 日程
 *
 * @author JNPF开发平台组
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 */
@Service
public class ScheduleLogServiceImpl extends SuperServiceImpl<ScheduleLogMapper, ScheduleLogEntity> implements ScheduleLogService {


    @Autowired
    private UserProvider userProvider;

    @Override
    public List<ScheduleLogEntity> getListAll(List<String> scheduleIdList) {
        List<ScheduleLogEntity> list = new ArrayList<>();
        QueryWrapper<ScheduleLogEntity> queryWrapper = new QueryWrapper<>();
        if(scheduleIdList.size()>0){
            queryWrapper.lambda().in(ScheduleLogEntity::getScheduleId,scheduleIdList);
            queryWrapper.lambda().orderByDesc(ScheduleLogEntity::getCreatorTime);
            list = this.list(queryWrapper);
        }
        return list;
    }

    @Override
    public ScheduleLogEntity getInfo(String id) {
        QueryWrapper<ScheduleLogEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ScheduleLogEntity::getId,id);
        return this.getOne(queryWrapper);
    }

    @Override
    public void create(ScheduleLogEntity entity) {
        UserInfo userInfo = userProvider.get();
        entity.setId(RandomUtil.uuId());
        entity.setCreatorTime(new Date());
        entity.setCreatorUserId(userInfo.getUserId());
        this.save(entity);
    }

    @Override
    public void delete(List<String> scheduleIdList, String operationType) {
        List<ScheduleLogEntity> listAll = getListAll(scheduleIdList);
        for (ScheduleLogEntity scheduleLogEntity : listAll) {
            scheduleLogEntity.setOperationType(operationType);
            create(scheduleLogEntity);
        }
    }

    @Override
    public boolean update(String id, ScheduleLogEntity entity) {
        entity.setId(id);
        boolean flag = this.updateById(entity);
        return flag;
    }


}
