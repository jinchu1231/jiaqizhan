package jnpf.base.mapper;

import jnpf.base.entity.FilterEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface FilterMapper extends  SuperMapper<FilterEntity>  {
}