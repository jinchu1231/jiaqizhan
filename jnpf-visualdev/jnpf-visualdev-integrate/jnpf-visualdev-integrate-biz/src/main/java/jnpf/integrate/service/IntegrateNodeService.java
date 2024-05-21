package jnpf.integrate.service;

import jnpf.base.service.SuperService;
import jnpf.integrate.entity.IntegrateNodeEntity;

import java.util.List;

public interface IntegrateNodeService extends SuperService<IntegrateNodeEntity> {

    List<IntegrateNodeEntity> getList(List<String> id,String nodeCode);

    List<IntegrateNodeEntity> getList(List<String> id,String nodeCode, Integer isRetry);

    IntegrateNodeEntity getInfo(String id);

    void create(IntegrateNodeEntity entity);

    void update(String id,String nodeCode);

    Boolean update(String id, IntegrateNodeEntity entity);

    void delete(IntegrateNodeEntity entity);

    void delete(String id);
}
