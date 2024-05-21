package jnpf.permission.service;

import jnpf.base.service.SuperService;
import jnpf.permission.entity.UserOldPasswordEntity;

import java.util.List;

/**
 * 用户信息
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月26日 上午9:18
 */
public interface UserOldPasswordService extends SuperService<UserOldPasswordEntity> {

    /**
     * 列表
     *
     * @return
     */
    List<UserOldPasswordEntity>  getList(String userId);

    /**
     * 创建
     *
     * @param entity 实体对象
     */
    Boolean create(UserOldPasswordEntity entity);

}
