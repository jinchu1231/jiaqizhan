package jnpf.permission.mapper;


import jnpf.base.mapper.SuperMapper;
import jnpf.base.model.base.SystemBaeModel;
import jnpf.base.model.button.ButtonModel;
import jnpf.base.model.column.ColumnModel;
import jnpf.base.model.form.ModuleFormModel;
import jnpf.base.model.module.ModuleModel;
import jnpf.base.model.resource.ResourceModel;
import jnpf.permission.entity.AuthorizeEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/12 15:27
 */
public interface AuthorizeMapper extends SuperMapper<AuthorizeEntity> {


    List<ModuleModel> findModule(@Param("objectId") String objectId, @Param("id") String id,
                                 @Param("moduleAuthorize") List<String> moduleAuthorize,
                                 @Param("moduleUrlAddressAuthorize") List<String> moduleUrlAddressAuthorize,
                                 @Param("mark") Integer mark);

    List<ButtonModel> findButton(@Param("objectId") String objectId);

    List<ColumnModel> findColumn(@Param("objectId") String objectId);

    List<ResourceModel> findResource(@Param("objectId") String objectId);

    List<ModuleFormModel> findForms(@Param("objectId") String objectId);

    List<SystemBaeModel> findSystem(@Param("objectId") String objectId, @Param("enCode") String enCode, @Param("moduleAuthorize") List<String> moduleAuthorize, @Param("mark") Integer mark);

    List<ButtonModel> findButtonAdmin(@Param("mark") Integer mark);

    List<ColumnModel> findColumnAdmin(@Param("mark") Integer mark);

    List<ResourceModel> findResourceAdmin(@Param("mark") Integer mark);

    List<ModuleFormModel> findFormsAdmin(@Param("mark") Integer mark);

    void saveBatch(@Param("values") String values);

    void savaAuth(AuthorizeEntity authorizeEntity);

}
