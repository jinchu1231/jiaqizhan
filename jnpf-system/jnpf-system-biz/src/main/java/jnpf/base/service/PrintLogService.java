package jnpf.base.service;

import jnpf.base.entity.PrintLogEntity;
import jnpf.base.model.printlog.PrintLogQuery;

import java.util.List;


public interface PrintLogService extends SuperService<PrintLogEntity> {

    List<PrintLogEntity> getListId(String printId, PrintLogQuery page);
}