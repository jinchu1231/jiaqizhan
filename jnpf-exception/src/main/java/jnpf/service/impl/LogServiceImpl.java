package jnpf.service.impl;


import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jnpf.base.LogSortEnum;
import jnpf.base.UserInfo;
import jnpf.base.service.SuperServiceImpl;
import jnpf.config.ConfigValueUtil;
import jnpf.database.util.TenantDataSourceUtil;
import jnpf.entity.LogEntity;
import jnpf.mapper.LogMapper;
import jnpf.model.PaginationLogModel;
import jnpf.service.LogService;
import jnpf.util.IpUtil;
import jnpf.util.RandomUtil;
import jnpf.util.ServletUtil;
import jnpf.util.StringUtil;
import jnpf.util.UserProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 系统日志
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2019年9月27日 上午9:18
 */
@Service
public class LogServiceImpl extends SuperServiceImpl<LogMapper, LogEntity> implements LogService {

    @Autowired
    private UserProvider userProvider;
    @Autowired
    private ConfigValueUtil configValueUtil;

    @Override
    public List<LogEntity> getList(int category, PaginationLogModel paginationTime) {
        UserInfo userInfo = userProvider.get();
        QueryWrapper<LogEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(LogEntity::getType, category);
        //用户Id
        String userId = userInfo.getUserId();
        String userAccount = userInfo.getUserAccount();
        if (!StringUtil.isEmpty(userId) && !StringUtil.isEmpty(userAccount)) {
            if (!userInfo.getIsAdministrator()){
                queryWrapper.lambda().and(
                        t -> t.eq(LogEntity::getUserId, userId)
                                .or().eq(LogEntity::getUserId, userAccount)
                );
            }
        }
        //日期范围（近7天、近1月、近3月、自定义）
        if (!ObjectUtil.isEmpty(paginationTime.getStartTime()) && !ObjectUtil.isEmpty(paginationTime.getEndTime())) {
            queryWrapper.lambda().between(LogEntity::getCreatorTime, new Date(paginationTime.getStartTime()), new Date(paginationTime.getEndTime()));
        }
        //关键字（用户、IP地址、功能名称）
        String keyWord = paginationTime.getKeyword();
        if (!StringUtil.isEmpty(keyWord)) {
            if (category == 1) {
                queryWrapper.lambda().and(
                        t -> t.like(LogEntity::getUserName, keyWord)
                                .or().like(LogEntity::getIpAddress, keyWord)
                );
            } else if (category == 5 || category == 4){
                queryWrapper.lambda().and(
                        t -> t.like(LogEntity::getUserName, keyWord)
                                .or().like(LogEntity::getIpAddress, keyWord)
                                .or().like(LogEntity::getRequestUrl, keyWord)
                );
            } else if (category == 3){
                queryWrapper.lambda().and(
                        t -> t.like(LogEntity::getUserName, keyWord)
                                .or().like(LogEntity::getIpAddress, keyWord)
                                .or().like(LogEntity::getRequestUrl, keyWord)
                                .or().like(LogEntity::getModuleName, keyWord)
                );
            }
        }
        // 请求方式
        if (StringUtil.isNotEmpty(paginationTime.getRequestMethod())) {
            queryWrapper.lambda().eq(LogEntity::getRequestMethod, paginationTime.getRequestMethod());
        }
        // 类型
        if (paginationTime.getLoginType() != null) {
            queryWrapper.lambda().eq(LogEntity::getLoginType, paginationTime.getLoginType());
        }
        // 状态
        if (paginationTime.getLoginMark() != null) {
            queryWrapper.lambda().eq(LogEntity::getLoginMark, paginationTime.getLoginMark());
        }
        //排序
        queryWrapper.lambda().orderByDesc(LogEntity::getCreatorTime);
        Page<LogEntity> page = new Page<>(paginationTime.getCurrentPage(), paginationTime.getPageSize());
        IPage<LogEntity> userPage = this.page(page, queryWrapper);
        return paginationTime.setData(userPage.getRecords(), page.getTotal());
    }

    @Override
    public LogEntity getInfo(String id) {
        QueryWrapper<LogEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(LogEntity::getId, id);
        return this.getOne(queryWrapper);
    }

    @Override
    public boolean delete(String[] ids) {
        if (ids.length > 0) {
            QueryWrapper<LogEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().in(LogEntity::getId, ids);
            return this.remove(queryWrapper);
        }
        return false;
    }

    @Override
    public void writeLogAsync(String userId, String userName, String abstracts, long requestDuration) {
        writeLogAsync(userId, userName, abstracts, null, 1, null, requestDuration);
    }

    @Override
    public void writeLogAsync(String userId, String userName, String abstracts, UserInfo userInfo, int loginMark, Integer loginType, long requestDuration) {
        LogEntity entity = new LogEntity();
        if (configValueUtil.isMultiTenancy()) {
            try {
                TenantDataSourceUtil.switchTenant(userInfo.getTenantId());
            } catch (Exception e) {
                return;
            }
        }
        String ipAddr = IpUtil.getIpAddr();
        entity.setIpAddress(ipAddr);
        entity.setIpAddressName(IpUtil.getIpCity(ipAddr));
        // 请求设备
        UserAgent userAgent = UserAgentUtil.parse(ServletUtil.getUserAgent());
        if (userAgent != null) {
            entity.setPlatForm(userAgent.getPlatform().getName() + " " + userAgent.getOsVersion());
            entity.setBrowser(userAgent.getBrowser().getName() + " " + userAgent.getVersion());
        }
        if (loginType != null) {
            entity.setLoginType(1);
        } else {
            entity.setLoginType(0);
        }
        entity.setLoginMark(loginMark);
        entity.setRequestDuration(Integer.parseInt(String.valueOf(requestDuration)));
        entity.setId(RandomUtil.uuId());
        entity.setUserId(userId);
        entity.setUserName(userName);
        entity.setDescription(abstracts);
        entity.setRequestUrl(ServletUtil.getServletPath());
        entity.setRequestMethod(ServletUtil.getRequest().getMethod());
        entity.setType(LogSortEnum.Login.getCode());
        this.save(entity);
    }

    @Override
    public void writeLogAsync(LogEntity entity) {
        entity.setId(RandomUtil.uuId());
        this.save(entity);
    }

    @Override
    public void deleteHandleLog(String type, Integer userOnline) {
        QueryWrapper<LogEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(LogEntity::getType, Integer.valueOf(type));
        if (ObjectUtil.equals(userOnline, 1)) {
            queryWrapper.lambda().eq(LogEntity::getCreatorUserId, UserProvider.getLoginUserId());
        }
        this.remove(queryWrapper);
    }

    @Override
    public Set<String> queryList() {
        QueryWrapper<LogEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(LogEntity::getType, 3);
        return this.list(queryWrapper).size() > 0 ? this.list(queryWrapper).stream().map(t -> t.getModuleName()).collect(Collectors.toSet()) : new HashSet<>(16);
    }
}
