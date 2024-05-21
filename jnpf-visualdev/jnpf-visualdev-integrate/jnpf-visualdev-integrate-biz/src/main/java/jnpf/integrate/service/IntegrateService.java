package jnpf.integrate.service;

import jnpf.base.ActionResult;
import jnpf.base.service.SuperService;
import jnpf.exception.WorkFlowException;
import jnpf.integrate.entity.IntegrateEntity;
import jnpf.integrate.model.integrate.IntegratePagination;

import java.util.List;

public interface IntegrateService extends SuperService<IntegrateEntity> {

    List<IntegrateEntity> getList(IntegratePagination pagination);

    List<IntegrateEntity> getList(IntegratePagination pagination, boolean isPage);

    IntegrateEntity getInfo(String id);

    Boolean isExistByFullName(String fullName, String id);

    Boolean isExistByEnCode(String encode, String id);

    void create(IntegrateEntity entity);

    ActionResult ImportData(IntegrateEntity entity, Integer type) throws WorkFlowException;

    Boolean update(String id, IntegrateEntity entity,boolean state);

    void delete(IntegrateEntity entity);

}
