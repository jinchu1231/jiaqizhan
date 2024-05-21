package jnpf.base.util.form;

import jnpf.base.UserInfo;
import jnpf.base.entity.VisualdevEntity;
import jnpf.base.model.DownloadCodeForm;
import jnpf.config.ConfigValueUtil;
import jnpf.database.model.entity.DbLinkEntity;
import jnpf.database.util.DataSourceUtil;
import jnpf.model.visualJson.FormDataModel;
import lombok.Data;

/**
 *
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @author JNPF开发平台组
 * @date 2021/10/25
 */
@Data
public class FormGenModel {
    private ConfigValueUtil configValueUtil;
    /**
     * 文件夹名字
     */
    private String fileName;
    /**
     * 实体对象
     */
    private VisualdevEntity entity;
    /**
     * 下载对象
     */
    private DownloadCodeForm downloadCodeForm;
    /**
     * 表单对象
     */
    private FormDataModel model;
    /**
     * 模板文件
     */
    private String templatePath;
    /**
     * 主键
     */
    private String pKeyName;
    /**
     * 本地数据源
     */
    private DataSourceUtil dataSourceUtil;
    /**
     * 数据连接
     */
    private DbLinkEntity linkEntity;
    /**
     * 个人信息
     */
    private UserInfo userInfo;
    /**
     * 生成文件名字
     */
    private String className;
    /**
     * 数据库表
     */
    private String table;
    /**
     * 生成路径
     */
    private String serviceDirectory;
    /**
     * 模板路径
     */
    private String templateCodePath;

    private Boolean groupTable;

    private String type;
    /**
     * 是否主表
     */
    private boolean isMainTable;

}
