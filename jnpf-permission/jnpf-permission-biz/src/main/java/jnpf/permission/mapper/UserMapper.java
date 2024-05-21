package jnpf.permission.mapper;

import jnpf.base.mapper.SuperMapper;
import jnpf.permission.entity.UserEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * 用户信息
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月26日 上午9:18
 */
public interface UserMapper extends SuperMapper<UserEntity> {
    /**
     * 获取用户id
     * @return
     */
    List<String> getListId();

    /**
     * 通过组织id获取用户信息
     *
     * @param orgIdList
     * @param gender
     * @return
     */
    List<String> query(@Param("orgIdList") List<String> orgIdList, @Param("account") String account, @Param("dbSchema") String dbSchema, @Param("enabledMark") Integer enabledMark, @Param("gender") String gender);

    /**
     * 通过组织id获取用户信息
     *
     * @param orgIdList
     * @param gender
     * @return
     */
    Long count(@Param("orgIdList") List<String> orgIdList, @Param("account") String account, @Param("dbSchema") String dbSchema, @Param("enabledMark") Integer enabledMark, @Param("gender") String gender);
}
