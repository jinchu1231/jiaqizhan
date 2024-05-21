package jnpf.integrate.service;

import jnpf.base.service.SuperService;
import jnpf.integrate.entity.IntegrateTaskEntity;
import jnpf.integrate.model.integrate.IntegratePageModel;

import java.util.List;

public interface IntegrateTaskService extends SuperService<IntegrateTaskEntity> {

    List<IntegrateTaskEntity> getList(IntegratePageModel pagination);

    List<IntegrateTaskEntity> getList(List<String> id);

    IntegrateTaskEntity getInfo(String id);

    void create(IntegrateTaskEntity entity);

    Boolean update(String id, IntegrateTaskEntity entity);

    void delete(IntegrateTaskEntity entity) ;
}
