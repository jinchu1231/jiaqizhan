package jnpf.base.mapper;

import jnpf.base.entity.PrintLogEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface PrintLogMapper extends SuperMapper<PrintLogEntity> {

    List<String> getListId(@Param("printId") String printId, @Param("keyword") String keyword);

}