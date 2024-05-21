package jnpf.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.xuyanwu.spring.file.storage.FileInfo;
import com.alibaba.druid.util.StringUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jnpf.base.ActionResult;
import jnpf.base.Page;
import jnpf.base.UserInfo;
import jnpf.base.controller.SuperController;
import jnpf.base.vo.DownloadVO;
import jnpf.base.vo.ListVO;
import jnpf.config.ConfigValueUtil;
import jnpf.constant.MsgCode;
import jnpf.entity.DocumentEntity;
import jnpf.entity.DocumentShareEntity;
import jnpf.exception.DataException;
import jnpf.model.MergeChunkDto;
import jnpf.model.document.DocumentCrForm;
import jnpf.model.document.DocumentFolderTreeModel;
import jnpf.model.document.DocumentFolderTreeVO;
import jnpf.model.document.DocumentInfoVO;
import jnpf.model.document.DocumentListVO;
import jnpf.model.document.DocumentShareForm;
import jnpf.model.document.DocumentStomeListVO;
import jnpf.model.document.DocumentSuserListVO;
import jnpf.model.document.DocumentUpForm;
import jnpf.model.document.DocumentUploader;
import jnpf.model.document.PageDocument;
import jnpf.permission.entity.UserEntity;
import jnpf.permission.service.UserService;
import jnpf.service.DocumentService;
import jnpf.util.DateUtil;
import jnpf.util.FileUploadUtils;
import jnpf.util.FileUtil;
import jnpf.util.JsonUtil;
import jnpf.util.OptimizeUtil;
import jnpf.util.RandomUtil;
import jnpf.util.StringUtil;
import jnpf.util.UpUtil;
import jnpf.util.UploaderUtil;
import jnpf.util.UserProvider;
import jnpf.util.XSSEscape;
import jnpf.util.treeutil.SumTree;
import jnpf.util.treeutil.TreeDotUtils;
import lombok.Cleanup;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文档管理
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2019年9月26日 上午9:18
 */
@Tag(name = "知识管理", description = "Document")
@RestController
@RequestMapping("/api/extend/Document")
public class DocumentController extends SuperController<DocumentService, DocumentEntity> {

    @Autowired
    private DocumentService documentService;
    @Autowired
    private ConfigValueUtil configValueUtil;
    @Autowired
    private UserService userService;
    @Autowired
    private UserProvider userProvider;

    /**
     * 列表
     *
     * @param id 主键
     * @return
     */
    @Operation(summary = "列表")
    @GetMapping("/{id}")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true),
    })
    @SaCheckPermission("extend.document")
    public ActionResult<DocumentInfoVO> info(@PathVariable("id") String id) throws DataException {
        DocumentEntity entity = documentService.getInfo(id);
        DocumentInfoVO vo = JsonUtil.getJsonToBeanEx(entity, DocumentInfoVO.class);
        //截取后缀
        String[] fullName = vo.getFullName().split("\\.");
        if (fullName.length > 1) {
            String fullNames = "";
            for (int i = 0; i < fullName.length - 1; i++) {
                if (i > 0) {
                    fullNames += "." + fullName[i];
                } else {
                    fullNames += fullName[i];
                }
            }
            vo.setFullName(fullNames);
        }
        return ActionResult.success(vo);
    }

    /**
     * 新建
     *
     * @param documentCrForm 新建模型
     * @return
     */
    @Operation(summary = "新建")
    @PostMapping
    @Parameters({
            @Parameter(name = "documentCrForm", description = "知识模型", required = true),
    })
    @SaCheckPermission("extend.document")
    public ActionResult create(@RequestBody @Valid DocumentCrForm documentCrForm) {
        DocumentEntity entity = JsonUtil.getJsonToBean(documentCrForm, DocumentEntity.class);
        if (documentService.isExistByFullName(documentCrForm.getFullName(), entity.getId(), documentCrForm.getParentId())) {
            return ActionResult.fail("文件夹名称不能重复");
        }
        entity.setEnabledMark(1);
        documentService.create(entity);
        return ActionResult.success("新建成功");
    }

    /**
     * 修改
     *
     * @param id             主键
     * @param documentUpForm 修改模型
     * @return
     */
    @Operation(summary = "修改")
    @PutMapping("/{id}")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true),
            @Parameter(name = "documentUpForm", description = "知识模型", required = true),
    })
    @SaCheckPermission("extend.document")
    public ActionResult update(@PathVariable("id") String id, @RequestBody @Valid DocumentUpForm documentUpForm) {
        DocumentEntity entity = JsonUtil.getJsonToBean(documentUpForm, DocumentEntity.class);
        if (documentService.isExistByFullName(documentUpForm.getFullName(), id, documentUpForm.getParentId())) {
            return ActionResult.fail("文件夹名称不能重复");
        }
        DocumentEntity info = documentService.getInfo(id);
        //获取后缀名
        String[] fullName = info.getFullName().split("\\.");
        if (fullName.length > 1) {
            entity.setFullName(entity.getFullName() + "." + fullName[fullName.length - 1]);
        }
        boolean flag = documentService.update(id, entity);
        if (flag == false) {
            return ActionResult.fail("更新失败，数据不存在");
        }
        return ActionResult.success("更新成功");
    }

    /**
     * 删除
     *
     * @param id 主键
     * @return
     */
    @Operation(summary = "删除")
    @DeleteMapping("/{id}")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true),
    })
    @SaCheckPermission("extend.document")
    public ActionResult delete(@PathVariable("id") String id) {
        DocumentEntity entity = documentService.getInfo(id);
        if (entity != null) {
            List<DocumentEntity> allList = documentService.getAllList(entity.getId());
            if (allList.size() > 0) {
                return ActionResult.fail("删除失败，该文件夹存在数据");
            }
            documentService.delete(entity);
            return ActionResult.success("删除成功");
        }
        return ActionResult.fail("删除失败，数据不存在");
    }

    /**
     * 列表
     *
     * @param id 主键
     * @return
     */
    @Operation(summary = "获取知识管理列表（文件夹树）")
    @GetMapping("/FolderTree/{id}")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true),
    })
    @SaCheckPermission("extend.document")
    public ActionResult<ListVO<DocumentFolderTreeVO>> folderTree(@PathVariable("id") String id) {
        List<DocumentEntity> data = documentService.getFolderList();
        if (!"0".equals(id)) {
            data.remove(documentService.getInfo(id));
        }
        List<DocumentFolderTreeModel> treeList = new ArrayList<>();
        DocumentFolderTreeModel model = new DocumentFolderTreeModel();
        model.setId("-1");
        model.setFullName("全部文档");
        model.setParentId("0");
        model.setIcon("0");
        treeList.add(model);
        for (DocumentEntity entity : data) {
            DocumentFolderTreeModel treeModel = new DocumentFolderTreeModel();
            treeModel.setId(entity.getId());
            treeModel.setFullName(entity.getFullName());
            treeModel.setParentId(entity.getParentId());
            treeModel.setIcon("fa fa-folder");
            treeList.add(treeModel);
        }
        List<SumTree<DocumentFolderTreeModel>> trees = TreeDotUtils.convertListToTreeDotFilter(treeList);
        List<DocumentFolderTreeVO> listVO = JsonUtil.getJsonToList(trees, DocumentFolderTreeVO.class);
        ListVO vo = new ListVO();
        vo.setList(listVO);
        return ActionResult.success(vo);
    }

    /**
     * 列表（全部文档）
     *
     * @param page 分页模型
     * @return
     */
    @Operation(summary = "获取知识管理列表（全部文档）")
    @GetMapping
    @SaCheckPermission("extend.document")
    public ActionResult<ListVO<DocumentListVO>> allList(PageDocument page) {
        List<DocumentEntity> data = documentService.getAllList(page.getParentId());
        if (!StringUtils.isEmpty(page.getKeyword())) {
            data = data.stream().filter(t -> t.getFullName().contains(page.getKeyword())).collect(Collectors.toList());
        }
        List<DocumentListVO> list = JsonUtil.getJsonToList(data, DocumentListVO.class);
        //读取允许文件预览类型
        String allowPreviewType = configValueUtil.getAllowPreviewFileType();
        String[] fileType = allowPreviewType.split(",");
        for (DocumentListVO documentListVO : list) {
            //文件预览类型检验
            String s = Arrays.asList(fileType).stream().filter(type -> type.equals(documentListVO.getFileExtension())).findFirst().orElse(null);
            documentListVO.setIsPreview(s);
        }

        ListVO vo = new ListVO();
        vo.setList(list);
        return ActionResult.success(vo);
    }

    /**
     * 列表（我的分享）
     *
     * @param page 分页模型
     * @return
     */
    @Operation(summary = "知识管理（我的共享列表）")
    @GetMapping("/Share")
    @SaCheckPermission("extend.document")
    public ActionResult shareOutList(Page page) {
        List<DocumentEntity> data = documentService.getShareOutList();
        if (!StringUtils.isEmpty(page.getKeyword())) {
            data = data.stream().filter(t -> t.getFullName().contains(page.getKeyword())).collect(Collectors.toList());
        }
        ListVO vo = new ListVO();
        vo.setList(data);
        return ActionResult.success(vo);
    }

    /**
     * 列表（共享给我）
     *
     * @param page 分页模型
     * @return
     */
    @Operation(summary = "获取知识管理列表（共享给我）")
    @GetMapping("/ShareTome")
    @SaCheckPermission("extend.document")
    public ActionResult<ListVO<DocumentStomeListVO>> shareTomeList(Page page) {
        List<DocumentEntity> list = documentService.getShareTomeList();
        if (!StringUtil.isEmpty(page.getKeyword())) {
            list = list.stream().filter(t -> t.getFullName().contains(page.getKeyword())).collect(Collectors.toList());
        }
        List<String> userId = list.stream().map(t -> t.getCreatorUserId()).collect(Collectors.toList());
        List<UserEntity> userName = userService.getUserName(userId);
        for (DocumentEntity entity : list) {
            UserEntity userEntity = userName.stream().filter(t -> t.getId().equals(entity.getCreatorUserId())).findFirst().orElse(null);
            entity.setCreatorUserId(userEntity != null ? userEntity.getRealName() + "/" + userEntity.getAccount() : "");
        }
        List<DocumentStomeListVO> vos = JsonUtil.getJsonToList(list, DocumentStomeListVO.class);
        ListVO vo = new ListVO();
        vo.setList(vos);
        return ActionResult.success(vo);
    }

    /**
     * 列表（回收站）
     *
     * @param page 分页模型
     * @return
     */
    @Operation(summary = "获取知识管理列表（回收站）")
    @GetMapping("/Trash")
    @SaCheckPermission("extend.document")
    public ActionResult<ListVO<DocumentStomeListVO>> trashList(Page page) {
        List<DocumentEntity> data = documentService.getTrashList();
        if (!StringUtils.isEmpty(page.getKeyword())) {
            data = data.stream().filter(t -> t.getFullName().contains(page.getKeyword())).collect(Collectors.toList());
        }
        ListVO vo = new ListVO();
        vo.setList(data);
        return ActionResult.success(vo);
    }

    /**
     * 列表（共享人员）
     *
     * @param documentId 文档主键
     * @return
     */
    @Operation(summary = "获取知识管理列表（共享人员）")
    @GetMapping("/ShareUser/{documentId}")
    @Parameters({
            @Parameter(name = "documentId", description = "文档主键", required = true),
    })
    @SaCheckPermission("extend.document")
    public ActionResult<ListVO<DocumentSuserListVO>> shareUserList(@PathVariable("documentId") String documentId) {
        List<DocumentShareEntity> data = documentService.getShareUserList(documentId);
        List<DocumentSuserListVO> list = JsonUtil.getJsonToList(data, DocumentSuserListVO.class);
        ListVO vo = new ListVO();
        vo.setList(list);
        return ActionResult.success(vo);
    }

    /**
     * 上传文件
     *
     * @param documentUploader 上传模型
     * @return
     */
    @Operation(summary = "知识管理上传文件")
    @PostMapping("/Uploader")
    @SaCheckPermission("extend.document")
    public ActionResult uploader(DocumentUploader documentUploader) throws DataException {
        String fileName = documentUploader.getFile().getOriginalFilename();
        List<DocumentEntity> data = documentService.getAllList(documentUploader.getParentId());
        String finalFileName = fileName;
        data = data.stream().filter(t -> finalFileName.equals(t.getFullName())).collect(Collectors.toList());
        if (data.size() > 0) {
            fileName = DateUtil.getNow("+8") + "-" + fileName;
        }
        String fileType = UpUtil.getFileType(documentUploader.getFile());
        String name = RandomUtil.uuId();
        String filePath = configValueUtil.getDocumentFilePath();
        //验证类型
        if (!OptimizeUtil.fileType(configValueUtil.getAllowUploadFileType(), fileType)) {
            return ActionResult.fail(MsgCode.FA017.get());
        }
        //上传
        FileInfo fileInfo = FileUploadUtils.uploadFile(documentUploader.getFile(), filePath, name + "." + fileType);
        DocumentEntity entity = new DocumentEntity();
        entity.setType(1);
        entity.setFullName(fileInfo.getFilename());
        entity.setParentId(documentUploader.getParentId());
        entity.setFileExtension(fileType);
        entity.setFilePath(name + "." + fileType);
        entity.setFileSize(String.valueOf(documentUploader.getFile().getSize()));
        documentService.create(entity);
        return ActionResult.success("上传成功");
    }

    /**
     * 分片组装
     *
     * @param mergeChunkDto 合并模型
     * @return
     */
    @Operation(summary = "分片组装")
    @PostMapping("/merge")
    @SaCheckPermission("extend.document")
    public ActionResult merge(MergeChunkDto mergeChunkDto) {
        String identifier = mergeChunkDto.getIdentifier();
        String path = FileUploadUtils.getLocalBasePath() + configValueUtil.getTemporaryFilePath();
        String filePath = XSSEscape.escapePath(path + identifier);
        String partFile = XSSEscape.escapePath(path + mergeChunkDto.getFileName());
        try {
            List<File> mergeFileList = FileUtil.getFile(new File(filePath));
            @Cleanup FileOutputStream destTempfos = new FileOutputStream(partFile, true);
            for (int i = 0; i < mergeFileList.size(); i++) {
                String chunkName = identifier.concat("-") + (i + 1);
                File files = new File(filePath, chunkName);
                if (files.exists()) {
                    FileUtils.copyFile(files, destTempfos);
                }
            }
            File partFiles = new File(partFile);
            if (partFiles.exists()) {
                MultipartFile multipartFile = FileUtil.createFileItem(partFiles);
                uploaderVO(multipartFile, mergeChunkDto.getParentId());
                FileUtil.deleteTmp(multipartFile);
            }
        } catch (Exception e) {
            System.out.println("合并分片失败:" + e);
        } finally {
            FileUtils.deleteQuietly(new File(filePath));
            FileUtils.deleteQuietly(new File(partFile));
        }
        return ActionResult.success(MsgCode.SU015.get());
    }

    /**
     * 合并文件
     *
     * @param guid
     * @return
     */
    /*@Operation(summary = "（未找到）知识管理合并文件")
    @PostMapping("/Merge/{guid}")
    public ActionResult Merge(@PathVariable("guid") String guid, String fileName, String folderId) {
        //临时文件
        String temp = configValueUtil.getTemporaryFilePath() + guid;
        File file = new File(jnpf.util.XSSEscape.escapePath(temp));
        //保存文件
        UserInfo userInfo = userProvider.get();
        String userId = userInfo.getUserId();
        String tenantId = userInfo.getTenantId();
        String time = DateTimeFormatter.ofPattern("yyyyMMdd").format(LocalDateTime.now());
        String path = configValueUtil.getDocumentFilePath() + "\\" + tenantId + "\\" + userId + "\\" + time;
        String fileType = "";
        String name = RandomUtil.uuId();
        File partFile = null;
        try {
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                if (files != null && files.length > 0) {
                    fileType = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
                    partFile = new File(jnpf.util.XSSEscape.escapePath(path));
                    if (!partFile.exists()) {
                        partFile.mkdirs();
                    }
                    partFile = new File(path + "\\" + name + "." + fileType);
                    for (int i = 0; i < files.length; i++) {
                        File s = new File(temp, i + ".part");
                        FileOutputStream destTempfos = new FileOutputStream(partFile, true);
                        FileUtils.copyFile(s, destTempfos);
                        destTempfos.close();
                    }
                    FileUtils.deleteDirectory(file);
                }
            }
            DocumentEntity entity = new DocumentEntity();
            entity.setFType(1);
            entity.setFParentId(folderId);
            entity.setFFullName(fileName);
            entity.setFFileExtension(fileType);
            entity.setFFilePath(tenantId + "\\" + userId + "\\" + time + "\\" + name + "." + fileType);
            entity.setFFileSize(String.valueOf(partFile.length()));
            entity.setFDeleteMark(0);
            documentService.create(entity);
            return ActionResult.success("合并成功");
        } catch (Exception e) {
            try {
                FileUtils.deleteDirectory(file);
            } catch (Exception e1) {
                System.out.println(e1.getMessage());
            }
            System.out.println(e.getMessage());
            return ActionResult.fail("上传失败");
        }
    }*/

    /**
     * 获取下载文件链接
     *
     * @param id 主键
     * @return
     */
    @Operation(summary = "获取下载文件链接")
    @PostMapping("/Download/{id}")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true),
    })
    @SaCheckPermission("extend.document")
    public ActionResult download(@PathVariable("id") String id) {
        UserInfo userInfo = userProvider.get();
        DocumentEntity entity = documentService.getInfo(id);
        if (entity != null) {
            String name = entity.getFilePath();
            String fileName = name + "#" + "document#" + entity.getFullName() + "." + entity.getFileExtension();
            DownloadVO vo = DownloadVO.builder().name(entity.getFullName()).url(UploaderUtil.uploaderFile(fileName)).build();
            return ActionResult.success(vo);
        }
        return ActionResult.fail(MsgCode.FA018.get());
    }

    /**
     * 回收站（彻底删除）
     *
     * @param id 主键
     * @return
     */
    @Operation(summary = "回收站（彻底删除）")
    @DeleteMapping("/Trash/{id}")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true),
    })
    @SaCheckPermission("extend.document")
    public ActionResult trashdelete(@PathVariable("id") String id) {
        documentService.trashdelete(id);
        return ActionResult.success("删除成功");
    }

    /**
     * 回收站（还原文件）
     *
     * @param id 主键
     * @return
     */
    @Operation(summary = "回收站（还原文件）")
    @PostMapping("/Trash/{id}/Actions/Recovery")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true),
    })
    @SaCheckPermission("extend.document")
    public ActionResult trashRecovery(@PathVariable("id") String id) {
        DocumentEntity entity = documentService.getInfo(id);
        if (entity == null) {
            return ActionResult.fail("操作失败，原文件不存在");
        }
        if (!"0".equals(entity.getParentId())) {
            DocumentEntity info = documentService.getInfo(entity.getParentId());
            if (info == null) {
                return ActionResult.fail("操作失败，原文件不存在");
            }
            if (info.getEnabledMark() != null && info.getEnabledMark() == 0) {
                return ActionResult.fail("找不到父级");
            }
        }
        boolean flag = documentService.trashRecovery(id);
        if (!flag) {
            return ActionResult.fail(MsgCode.FA010.get());
        }
        return ActionResult.success(MsgCode.SU010.get());
    }

    /**
     * 共享文件（创建）
     *
     * @param id                主键
     * @param documentShareForm 分享模型
     * @return
     */
    @Operation(summary = "分享文件/文件夹")
    @PostMapping("/{id}/Actions/Share")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true),
            @Parameter(name = "documentShareForm", description = "分享模型", required = true),
    })
    @SaCheckPermission("extend.document")
    public ActionResult shareCreate(@PathVariable("id") String id, @RequestBody DocumentShareForm documentShareForm) {
        String[] shareUserId = documentShareForm.getUserId().split(",");
        boolean flag = documentService.sharecreate(id, shareUserId);
        if (flag == false) {
            return ActionResult.fail("操作失败，原文件不存在");
        }
        return ActionResult.success("操作成功");
    }

    /**
     * 共享文件（取消）
     *
     * @param id 主键值
     * @return
     */
    @Operation(summary = "取消分享文件/文件夹")
    @DeleteMapping("/{id}/Actions/Share")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true),
    })
    @SaCheckPermission("extend.document")
    public ActionResult shareCancel(@PathVariable("id") String id) {
        boolean flag = documentService.shareCancel(id);
        if (flag == false) {
            return ActionResult.fail("操作失败，原文件不存在");
        }
        return ActionResult.success("操作成功");
    }

    /**
     * 文件/夹移动到
     *
     * @param id   主键值
     * @param toId 将要移动到Id
     * @return
     */
    @Operation(summary = "移动文件/文件夹")
    @PutMapping("/{id}/Actions/MoveTo/{toId}")
    @Parameters({
            @Parameter(name = "id", description = "主键", required = true),
            @Parameter(name = "toId", description = "将要移动到Id", required = true),
    })
    @SaCheckPermission("extend.document")
    public ActionResult moveTo(@PathVariable("id") String id, @PathVariable("toId") String toId) {
        if (id.equals(toId)) {
            return ActionResult.fail("不能移动到自己的文件夹");
        }
        boolean flag = documentService.moveTo(id, toId);
        if (flag == false) {
            return ActionResult.fail("更新失败，数据不存在");
        }
        return ActionResult.success("更新成功");
    }

    /**
     * 封装上传附件
     *
     * @param file
     * @param parentId
     * @return
     */
    private void uploaderVO(MultipartFile file, String parentId) {
        String fileType = UpUtil.getFileType(file);
        String filePath = configValueUtil.getDocumentFilePath();
        String name = RandomUtil.uuId();
        String fileName = file.getOriginalFilename();
        List<DocumentEntity> data = documentService.getAllList(parentId);
        String finalFileName = fileName;
        data = data.stream().filter(t -> finalFileName.equals(t.getFullName())).collect(Collectors.toList());
        if (data.size() > 0) {
            fileName = DateUtil.getNow("+8") + "-" + fileName;
        }
        //上传
        FileInfo fileInfo = FileUploadUtils.uploadFile(file, filePath, fileName + "." + fileType);
        DocumentEntity entity = new DocumentEntity();
        entity.setType(1);
        entity.setFullName(fileName);
        entity.setParentId(parentId);
        entity.setFileExtension(fileType);
        entity.setFilePath(fileInfo.getFilename());
        entity.setFileSize(String.valueOf(file.getSize()));
        entity.setEnabledMark(1);
        entity.setUploaderUrl(UploaderUtil.uploaderImg("/api/file/Image/document/", fileInfo.getFilename()));
        documentService.create(entity);
    }

}
