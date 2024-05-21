package jnpf.base.service;

import jnpf.base.entity.SysConfigEntity;
import jnpf.model.BaseSystemInfo;

import java.util.List;

/**
 * 系统配置
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2019年9月27日 上午9:18
 */
public interface SysconfigService extends SuperService<SysConfigEntity> {

    /**
     * 列表
     *
     * @param type
     * @return
     */
    List<SysConfigEntity> getList(String type);

    /**
     * 信息
     *
     * @return
     */
    BaseSystemInfo getWeChatInfo();

    /**
     * 根据key获取value
     * @param keyStr
     * @return
     */
    String getValueByKey(String keyStr);
    /**
     * 获取系统配置
     * @return
     */
    BaseSystemInfo getSysInfo();
    /**
     * 获取租户系统配置
     * @return
     */
    BaseSystemInfo getSysInfo(String tenantId);
    /**
     * 保存系统配置
     *
     * @param entitys 实体对象
     * @return
     */
    void save(List<SysConfigEntity> entitys);
    /**
     * 保存公众号配置
     *
     * @param entitys 实体对象
     * @return
     */
    boolean saveMp(List<SysConfigEntity> entitys);
    /**
     * 保存企业号配置
     *
     * @param entitys 实体对象
     */
    void saveQyh(List<SysConfigEntity> entitys);
}
