package jnpf.base.mapper;


import jnpf.base.entity.DictionaryDataEntity;
import org.apache.ibatis.annotations.Param;

/**
 * 字典数据
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2019年9月27日 上午9:18
 */
public interface DictionaryDataMapper extends SuperMapper<DictionaryDataEntity> {

    DictionaryDataEntity getByTypeDataCode(@Param("typeCode") String typeCode, @Param("dataCode") String dataCode);

}
