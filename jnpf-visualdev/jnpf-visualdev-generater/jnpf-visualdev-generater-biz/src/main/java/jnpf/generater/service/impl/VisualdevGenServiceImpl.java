package jnpf.generater.service.impl;

import com.alibaba.fastjson.JSON;
import com.google.common.base.CaseFormat;
import jnpf.base.UserInfo;
import jnpf.base.entity.VisualdevEntity;
import jnpf.base.mapper.VisualdevMapper;
import jnpf.base.model.ColumnDataModel;
import jnpf.base.model.DownloadCodeForm;
import jnpf.base.service.DbLinkService;
import jnpf.base.service.SuperServiceImpl;
import jnpf.base.service.VisualdevService;
import jnpf.base.util.VisualUtils;
import jnpf.base.util.app.AppGenModel;
import jnpf.base.util.app.AppGenUtil;
import jnpf.base.util.common.FormCommonUtil;
import jnpf.base.util.custom.VelocityEnum;
import jnpf.base.util.form.FormGenModel;
import jnpf.base.util.form.FormGenUtil;
import jnpf.base.util.fuctionFormVue3.common.GenerateParamModel;
import jnpf.config.ConfigValueUtil;
import jnpf.constant.MsgCode;
import jnpf.database.model.entity.DbLinkEntity;
import jnpf.database.util.DataSourceUtil;
import jnpf.generater.factory.CodeGenerateFactory;
import jnpf.generater.factory.CodeGenerateFactoryV3;
import jnpf.generater.model.GenBaseInfo;
import jnpf.generater.service.VisualdevGenService;
import jnpf.model.FileListVO;
import jnpf.model.visualJson.FieLdsModel;
import jnpf.model.visualJson.FormDataModel;
import jnpf.model.visualJson.TableModel;
import jnpf.util.DateUtil;
import jnpf.util.FileUploadUtils;
import jnpf.util.JsonUtil;
import jnpf.util.StringUtil;
import jnpf.util.UserProvider;
import org.apache.velocity.app.Velocity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 可视化开发功能表
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2021-04-02
 */
@Service
public class VisualdevGenServiceImpl extends SuperServiceImpl<VisualdevMapper, VisualdevEntity> implements VisualdevGenService {

    @Autowired
    private ConfigValueUtil configValueUtil;
    @Autowired
    private UserProvider userProvider;
    @Autowired
    private DataSourceUtil dataSourceUtil;
    @Autowired
    private DbLinkService dataSourceService;
    @Autowired
    private VisualdevService visualdevService;
    @Autowired
    private CodeGenerateFactory CodeGenerateFactory;
    @Autowired
    private CodeGenerateFactoryV3 GenerateFactoryV3;

    @Override
    public String codeGengerate(String id, DownloadCodeForm downloadCodeForm) throws Exception {
        UserInfo userInfo = userProvider.get();
        VisualdevEntity entity = visualdevService.getInfo(id);
        DbLinkEntity linkEntity = dataSourceService.getInfo(entity.getDbLinkId());
        if (entity.getDbLinkId() == null) {
            linkEntity = null;
        }
        if (entity != null) {
            if (!StringUtil.isEmpty(entity.getVisualTables())) {
                FormDataModel model = JsonUtil.getJsonToBean(entity.getFormData(), FormDataModel.class);
                model.setModule(downloadCodeForm.getModule());
                model.setClassName(downloadCodeForm.getClassName());
                model.setAreasName(downloadCodeForm.getModule());
                model.setServiceDirectory(configValueUtil.getServiceDirectoryPath());
                List<FieLdsModel> filterFeildList = JsonUtil.getJsonToList(model.getFields(), FieLdsModel.class);
                model.setFields(JSON.toJSONString(filterFeildList));
                String fileName = entity.getFullName().trim() + "_" + DateUtil.nowDateTime();
                //初始化模板
                Velocity.reset();
                VelocityEnum.init.initVelocity(FileUploadUtils.getLocalBasePath() + configValueUtil.getTemplateCodePath());

                List<TableModel> list = JsonUtil.getJsonToList(entity.getVisualTables(), TableModel.class);
                //获取主表
                String mainTable = list.stream().filter(t -> "1".equals(t.getTypeId())).findFirst().get().getTable();
                //获取主键
                String pKeyName = VisualUtils.getpKey(linkEntity, mainTable).toLowerCase().trim();
                pKeyName = pKeyName.startsWith("f_") ? pKeyName.substring(2) : pKeyName;
                //自定义包名
                String modulePackageName = StringUtil.isNotEmpty(downloadCodeForm.getModulePackageName()) ? downloadCodeForm.getModulePackageName() : GenBaseInfo.PACKAGE_NAME;
                downloadCodeForm.setModulePackageName(modulePackageName);
                //获取其他子表的主键
                Map<String, Object> childpKeyMap = new HashMap<>(16);
                for (TableModel tableModel : list) {
                    String childKey = VisualUtils.getpKey(linkEntity, tableModel.getTable());
                    if (childKey.length() > 2) {
                        if ("f_".equals(childKey.substring(0, 2).toLowerCase())) {
                            childKey = childKey.substring(2);
                        }
                    }
                    childpKeyMap.put(tableModel.getTable(), childKey);
                }
                //判断子表名称
                List<String> childTb = new ArrayList();
                if (!StringUtil.isEmpty(downloadCodeForm.getSubClassName())) {
                    childTb = Arrays.asList(downloadCodeForm.getSubClassName().split(","));
                }

                Set<String> set = new HashSet<>(childTb);
                boolean result = childTb.size() == set.size() ? true : false;
                if (!result) {
                    return "名称不能重复";
                }
                String templatesPath = null;

                //非本地模板需要下载-获取模板如下
                if (!FileUploadUtils.getDefaultPlatform().startsWith("local")) {
                    List<FileListVO> fileList = new ArrayList<>();
                    fileList.addAll(FileUploadUtils.getDefaultFileList(configValueUtil.getTemplateCodePath()));
                    for (FileListVO fileListVO : fileList) {
                        String eachFileName = fileListVO.getFileName();
                        int index = eachFileName.lastIndexOf("/");
                        //服务器路径
                        String floderName = eachFileName.substring(0, index + 1);
                        //本地路径
                        String filePath = FileUploadUtils.getLocalBasePath() + floderName + "/";
                        //文件名
                        String objectName = eachFileName.substring(index + 1);
                        FileUploadUtils.downLocal(floderName, filePath, objectName);
                    }
                }

                if (entity.getType() == 3) {
                    entity.setEnableFlow(1);
                    downloadCodeForm.setModule("form");
                    downloadCodeForm.setModulePackageName("jnpf.form");
                    templatesPath = "TemplateCode1";
                }

                if (entity.getType() == 4) {
                    switch (entity.getWebType()) {
                        case 1:
                            templatesPath = entity.getEnableFlow() == 1 ? "TemplateCode5" : "TemplateCode4";
                            break;
                        case 2:
                            templatesPath = entity.getEnableFlow() == 1 ? "TemplateCode3" : "TemplateCode2";
                            break;
                        case 3:
                            templatesPath = "TemplateCode3";
                        default:
                            break;
                    }
                }

                String column = StringUtil.isNotEmpty(entity.getColumnData()) ? entity.getColumnData() : "{}";
                ColumnDataModel columnDataModel = JsonUtil.getJsonToBean(column, ColumnDataModel.class);
                boolean groupTable = "3".equals(String.valueOf(columnDataModel.getType()));
                FormGenUtil genUtil = new FormGenUtil();
                FormGenModel formGenModel = new FormGenModel();
                formGenModel.setEntity(entity);
                String keyName = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, pKeyName);
                formGenModel.setPKeyName(keyName);
                formGenModel.setServiceDirectory(FileUploadUtils.getLocalBasePath() + configValueUtil.getServiceDirectoryPath());
                formGenModel.setTemplateCodePath(FileUploadUtils.getLocalBasePath() + configValueUtil.getTemplateCodePath());
                formGenModel.setDownloadCodeForm(downloadCodeForm);
                formGenModel.setUserInfo(userInfo);
                formGenModel.setTemplatePath(templatesPath);
                formGenModel.setFileName(fileName);
                formGenModel.setLinkEntity(linkEntity);
                formGenModel.setDataSourceUtil(dataSourceUtil);
                formGenModel.setGroupTable(groupTable);
                formGenModel.setType(String.valueOf(columnDataModel.getType()));
                formGenModel.setModel(model);
                formGenModel.setConfigValueUtil(configValueUtil);
                formGenModel.setTable(mainTable);
                genUtil.generate(formGenModel);
                genUtil.htmlTemplates(formGenModel);

                entity.setColumnData(entity.getAppColumnData());
                AppGenModel appGenModel = new AppGenModel();
                appGenModel.setEntity(entity);
                appGenModel.setPKeyName(keyName);
                appGenModel.setServiceDirectory(FileUploadUtils.getLocalBasePath() + configValueUtil.getServiceDirectoryPath());
                appGenModel.setTemplateCodePath(FileUploadUtils.getLocalBasePath() + configValueUtil.getTemplateCodePath());
                appGenModel.setDownloadCodeForm(downloadCodeForm);
                appGenModel.setUserInfo(userInfo);
                appGenModel.setTemplatePath(templatesPath);
                appGenModel.setFileName(fileName);
                appGenModel.setLinkEntity(linkEntity);
                appGenModel.setDataSourceUtil(dataSourceUtil);
                appGenModel.setGroupTable(groupTable);
                appGenModel.setType(String.valueOf(columnDataModel.getType()));
                appGenModel.setModel(model);
                AppGenUtil appGenUtil = new AppGenUtil();
                appGenUtil.htmlTemplates(appGenModel);

                return fileName;
            }
        }
        return null;
    }

    /**
     * vue3代码生成
     *
     * @param entity           可视化开发功能
     * @param downloadCodeForm 下载相关信息
     * @return
     * @throws Exception
     */
    @Override
    public String codeGengerateV3(VisualdevEntity entity, DownloadCodeForm downloadCodeForm) throws Exception {
        UserInfo userInfo = userProvider.get();
        DbLinkEntity linkEntity = null;
        if (entity != null) {
            // 是否存在关联数据库
            if (StringUtil.isNotEmpty(entity.getDbLinkId())) {
                linkEntity = dataSourceService.getInfo(entity.getDbLinkId());
            }
            // 是否存在关联表
            if (StringUtil.isNotEmpty(entity.getVisualTables())) {
                FormDataModel model = JsonUtil.getJsonToBean(entity.getFormData(), FormDataModel.class);
                model.setModule(downloadCodeForm.getModule());
                model.setClassName(downloadCodeForm.getClassName());
                model.setAreasName(downloadCodeForm.getModule());
                model.setServiceDirectory(configValueUtil.getServiceDirectoryPath());
                List<FieLdsModel> filterFeildList = JsonUtil.getJsonToList(model.getFields(), FieLdsModel.class);
                model.setFields(JSON.toJSONString(filterFeildList));
                String fileName = entity.getFullName() + "_" + DateUtil.nowDateTime();
                //初始化模板
                Velocity.reset();
                VelocityEnum.init.initVelocity(FormCommonUtil.getLocalBasePath() + configValueUtil.getTemplateCodePathVue3());

                List<TableModel> list = JsonUtil.getJsonToList(entity.getVisualTables(), TableModel.class);
                //获取主表
                String mainTable = list.stream().filter(t -> "1".equals(t.getTypeId())).findFirst().orElse(null).getTable();
                //获取主键
                String pKeyName = VisualUtils.getpKey(linkEntity, mainTable).toLowerCase().trim();
                pKeyName = pKeyName.startsWith("f_") ? pKeyName.replaceFirst("f_", "") : pKeyName;
                //自定义包名
                String modulePackageName = StringUtil.isNotEmpty(downloadCodeForm.getModulePackageName()) ? downloadCodeForm.getModulePackageName() :
                        GenBaseInfo.PACKAGE_NAME;
                downloadCodeForm.setModulePackageName(modulePackageName);
                //获取其他子表的主键
                Map<String, Object> childpKeyMap = new HashMap<>(16);
                for (TableModel tableModel : list) {
                    String childKey = VisualUtils.getpKey(linkEntity, tableModel.getTable());
                    if (childKey.length() > 2) {
                        if ("f_".equals(childKey.substring(0, 2).toLowerCase())) {
                            childKey = childKey.substring(2);
                        }
                    }
                    childpKeyMap.put(tableModel.getTable(), childKey);
                }
                //判断子表名称
                List<String> childTb = new ArrayList();
                if (!StringUtil.isEmpty(downloadCodeForm.getSubClassName())) {
                    childTb = Arrays.asList(downloadCodeForm.getSubClassName().split(","));
                }

                Set<String> set = new HashSet<>(childTb);
                boolean result = childTb.size() == set.size() ? true : false;
                if (!result) {
                    return MsgCode.EXIST001.get();
                }
                String templatesPath = null;
                //发起表单
                if (entity.getType() == 3) {
                    //工作流生成器
                    downloadCodeForm.setModule("form");
                    entity.setEnableFlow(1);
                    downloadCodeForm.setModulePackageName("jnpf.form");
                    templatesPath = "TemplateCode1";
                }
                //功能表单
                if (entity.getType() == 4) {
                    switch (entity.getWebType()) {
                        case 1:
                            templatesPath = entity.getEnableFlow() == 1 ? "TemplateCode5" : "TemplateCode4";
                            break;
                        case 2:
                            templatesPath = entity.getEnableFlow() == 1 ? "TemplateCode3" : "TemplateCode2";
                            break;
                        default:
                            break;
                    }
                }

                //非本地模板需要下载-获取模板如下
                if (!FileUploadUtils.getDefaultPlatform().startsWith("local")) {
                    List<FileListVO> fileList = new ArrayList<>();
                    fileList.addAll(FileUploadUtils.getDefaultFileList(configValueUtil.getTemplateCodePathVue3()));
                    for (FileListVO fileListVO : fileList) {
                        String eachFileName = fileListVO.getFileName();
                        int index = eachFileName.lastIndexOf("/");
                        //服务器路径
                        String floderName = eachFileName.substring(0, index + 1);
                        //本地路径
                        String filePath = FileUploadUtils.getLocalBasePath() + floderName + "/";
                        //文件名
                        String objectName = eachFileName.substring(index + 1);
                        FileUploadUtils.downLocal(floderName, filePath, objectName);
                    }
                }

                //执行代码生成器
                GenerateParamModel generateParamModel = new GenerateParamModel().builder()
                        .dataSourceUtil(dataSourceUtil)
                        .path(FormCommonUtil.getLocalBasePath() + configValueUtil.getTemplateCodePathVue3())
                        .fileName(fileName)
                        .templatesPath(templatesPath)
                        .downloadCodeForm(downloadCodeForm)
                        .entity(entity)
                        .userInfo(userInfo)
                        .configValueUtil(configValueUtil)
                        .linkEntity(linkEntity)
                        .pKeyName(CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, pKeyName))
                        .build();
                GenerateFactoryV3.runGenerator(templatesPath, generateParamModel);
                return fileName;
            }
        }
        return null;
    }
}
