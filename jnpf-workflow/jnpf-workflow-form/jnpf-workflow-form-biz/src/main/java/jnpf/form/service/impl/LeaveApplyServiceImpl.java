package jnpf.form.service.impl;

import com.baomidou.dynamic.datasource.annotation.DSTransactional;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jnpf.base.service.SuperServiceImpl;
import jnpf.form.entity.LeaveApplyEntity;
import jnpf.form.mapper.LeaveApplyMapper;
import jnpf.form.model.leaveapply.LeaveApplyForm;
import jnpf.form.service.LeaveApplyService;
import jnpf.util.JsonUtil;
import jnpf.util.ServiceAllUtil;
import jnpf.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 流程表单【请假申请】
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月29日 上午9:18
 */
@Service
public class LeaveApplyServiceImpl extends SuperServiceImpl<LeaveApplyMapper, LeaveApplyEntity> implements LeaveApplyService {

    @Autowired
    private ServiceAllUtil serviceAllUtil;

    @Override
    public LeaveApplyEntity getInfo(String id) {
        QueryWrapper<LeaveApplyEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(LeaveApplyEntity::getId, id);
        return getOne(queryWrapper);
    }

    @Override
    @DSTransactional
    public void save(String id, LeaveApplyEntity entity, LeaveApplyForm form) {
        //表单信息
        if (StringUtil.isEmpty(entity.getId())) {
            entity.setId(id);
            save(entity);
            serviceAllUtil.useBillNumber("WF_LeaveApplyNo");
        } else {
            entity.setId(id);
            updateById(entity);
        }
    }

    @Override
    @DSTransactional
    public void submit(String id, LeaveApplyEntity entity, LeaveApplyForm form) {
        //表单信息
        if (StringUtil.isEmpty(entity.getId())) {
            entity.setId(id);
            save(entity);
            serviceAllUtil.useBillNumber("WF_LeaveApplyNo");
        } else {
            entity.setId(id);
            updateById(entity);
        }
    }

    @Override
    public void data(String id, String data) {
        LeaveApplyForm leaveApplyForm = JsonUtil.getJsonToBean(data, LeaveApplyForm.class);
        LeaveApplyEntity entity = JsonUtil.getJsonToBean(leaveApplyForm, LeaveApplyEntity.class);
        entity.setId(id);
        saveOrUpdate(entity);
    }

}
