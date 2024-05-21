package jnpf.base.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jnpf.base.entity.CommonWordsEntity;
import jnpf.base.entity.SystemEntity;
import jnpf.base.mapper.CommonWordsMapper;
import jnpf.base.model.commonword.ComWordsPagination;
import jnpf.base.service.CommonWordsService;
import jnpf.base.service.SuperServiceImpl;
import jnpf.base.service.SystemService;
import jnpf.util.StringUtil;
import jnpf.util.UserProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


/**
 * 审批常用语 ServiceImpl
 *
 * @author JNPF开发平台组 YanYu
 * @version v3.4.6
 * @copyrignt 引迈信息技术有限公司
 * @date 2023-01-06
 */
@Service
public class CommonWordsServiceImpl extends SuperServiceImpl<CommonWordsMapper, CommonWordsEntity> implements CommonWordsService {

    @Autowired
    private UserProvider userProvider;

    @Autowired
    private SystemService systemService;

    @Override
    public List<CommonWordsEntity> getSysList(ComWordsPagination comWordsPagination, Boolean currentSysFlag) {
        QueryWrapper<SystemEntity> sysQuery = new QueryWrapper<>();
        // 匹配
        QueryWrapper<CommonWordsEntity> query = new QueryWrapper<>();
        query.lambda().eq(CommonWordsEntity::getCommonWordsType, 0);
//        if(currentSysFlag) query.lambda().like(CommonWordsEntity::getSystemIds, userProvider.get().getSystemId());
        if (StringUtil.isNotEmpty(comWordsPagination.getKeyword())) {
            sysQuery.lambda().like(SystemEntity::getFullName, comWordsPagination.getKeyword());
            List<String> ids =systemService.list(sysQuery).stream().map(SystemEntity::getId).collect(Collectors.toList());
            query.lambda().and(t ->{
                // 应用名称
                for (String id : ids) {
                    t.like(CommonWordsEntity::getSystemIds, id).or();
                }
                t.like(CommonWordsEntity::getCommonWordsText, comWordsPagination.getKeyword()); // 常用语
            });
        }
        if (comWordsPagination.getEnabledMark() != null) {
            query.lambda().eq(CommonWordsEntity::getEnabledMark, comWordsPagination.getEnabledMark());
        }
        // 排序
        query.lambda().orderByAsc(CommonWordsEntity::getSortCode).orderByDesc(CommonWordsEntity::getCreatorTime);
        return this.page(comWordsPagination.getPage(), query).getRecords();
    }

    @Override
    public List<CommonWordsEntity> getListModel(String type) {
        QueryWrapper<CommonWordsEntity> query = new QueryWrapper<>();
        query.lambda().eq(CommonWordsEntity::getEnabledMark, 1)
                .and(t ->
                        t.and(t2 -> t2.eq(CommonWordsEntity::getCreatorUserId, userProvider.get().getUserId()).or().eq(CommonWordsEntity::getCommonWordsType, 0))
                );
        // 排序
        query.lambda().orderByDesc(CommonWordsEntity::getCommonWordsType).orderByAsc(CommonWordsEntity::getSortCode).orderByDesc(CommonWordsEntity::getCreatorTime);
        return this.list(query);
    }

    @Override
    public Boolean existSystem(String systemId) {
        QueryWrapper<CommonWordsEntity> query = new QueryWrapper<>();
        query.lambda().like(CommonWordsEntity::getSystemIds, systemId);
        return count(query) > 0;
    }

}
