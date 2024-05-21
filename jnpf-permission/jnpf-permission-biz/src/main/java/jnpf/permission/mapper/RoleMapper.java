package jnpf.permission.mapper;

import jnpf.base.mapper.SuperMapper;
import jnpf.permission.entity.RoleEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 系统角色
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月26日 上午9:18
 */
public interface RoleMapper extends SuperMapper<RoleEntity> {

    /**
     * 通过组织id获取用户信息
     *
     * @param orgIdList
     * @return
     */
    List<String> query(@Param("orgIdList") List<String> orgIdList, @Param("keyword") String keyword, @Param("globalMark") Integer globalMark, @Param("enabledMark") Integer enabledMark);

    /**
     * 通过组织id获取用户信息
     *
     * @param
     * @param orgIdList
     * @return
     */
    Long count(@Param("orgIdList") List<String> orgIdList, @Param("keyword") String keyword, @Param("globalMark") Integer globalMark, @Param("enabledMark") Integer enabledMark);

}
