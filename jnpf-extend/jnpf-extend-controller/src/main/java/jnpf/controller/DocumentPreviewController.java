package jnpf.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jnpf.base.ActionResult;
import jnpf.base.Page;
import jnpf.config.ConfigValueUtil;
import jnpf.enums.FilePreviewTypeEnum;
import jnpf.exception.DataException;
import jnpf.model.FileListVO;
import jnpf.model.YozoFileParams;
import jnpf.model.YozoParams;
import jnpf.util.FileDownloadUtil;
import jnpf.util.FileUploadUtils;
import jnpf.util.NoDataSourceBind;
import jnpf.util.StringUtil;
import jnpf.util.XSSEscape;
import jnpf.utils.SplicingUrlUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 文档在线预览
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月26日 上午9:18
 */
@NoDataSourceBind()
@Tag(name = "文档在线预览", description = "DocumentPreview")
@RestController
@RequestMapping("/api/extend/DocumentPreview")
public class DocumentPreviewController {

    @Autowired
    private ConfigValueUtil configValueUtil;

    /**
     * 永中文件预览
     *
     * @param fileId 文件主键
     * @param params 永中模型
     * @param previewType 类型
     * @return
     */
    @Operation(summary = "文件预览")
    @GetMapping("/{fileId}/Preview")
    @Parameters({
            @Parameter(name = "fileId", description = "文件主键",required = true),
            @Parameter(name = "previewType", description = "类型"),
    })
    @SaCheckPermission("extend.documentPreview")
    public ActionResult filePreview(@PathVariable("fileId") String fileId, YozoFileParams params, @RequestParam("previewType") String previewType) {
        FileListVO fileListVO = FileUploadUtils.getFileDetail(configValueUtil.getDocumentPreviewPath(), fileId);
        if (fileListVO == null) {
            return ActionResult.fail("文件找不到!");
        }
        if (fileListVO.getFileName() != null) {
            String[] split = fileListVO.getFileName().split("/");
            if (split.length > 0) {
                fileListVO.setFileName(split[split.length - 1]);
            }
        }
        String url = YozoParams.JNPF_DOMAINS + "/api/extend/DocumentPreview/down/" + fileListVO.getFileName();
        String urlPath;
        if (previewType.equals(FilePreviewTypeEnum.YOZO_ONLINE_PREVIEW.getType())){
            params.setUrl(url);
            urlPath = SplicingUrlUtil.getPreviewUrl(params);
            return ActionResult.success("success", XSSEscape.escape(urlPath));
        }
        return ActionResult.success("success",url);
    }

    /**
     * 列表
     *
     * @param page 分页模型
     * @return
     */
    @Operation(summary = "获取文档列表")
    @GetMapping
    @SaCheckPermission("extend.documentPreview")
    public ActionResult<List<FileListVO>> list(Page page) {
        List<FileListVO> fileList = FileUploadUtils.getFileList(configValueUtil.getDocumentPreviewPath());
        fileList.stream().forEach(t -> {
            if (t.getFileName() != null) {
                String[] split = t.getFileName().split("/");
                if (split.length > 0) {
                    t.setFileName(split[split.length - 1]);
                }
            }
        });
        if (StringUtil.isNotEmpty(page.getKeyword())) {
            fileList = fileList.stream().filter(t -> t.getFileName().contains(page.getKeyword())).collect(Collectors.toList());
        }
        return ActionResult.success(fileList);
    }

    /**
     * 文件下载url
     *
     * @param fileName 名称
     */
    @NoDataSourceBind()
    @GetMapping("/down/{fileName}")
    @Parameters({
            @Parameter(name = "fileName", description = "名称",required = true),
    })
    @SaCheckPermission("extend.documentPreview")
    public void pointDown(@PathVariable("fileName") String fileName) throws DataException {
        boolean exists = FileUploadUtils.exists(configValueUtil.getDocumentPreviewPath(), fileName);
        if (!exists) {
            throw new DataException("下载失败");
        }
        byte[] bytes = FileUploadUtils.downloadFileByte(configValueUtil.getDocumentPreviewPath(), fileName, false);
        FileDownloadUtil.downloadFile(bytes, fileName, null);
    }

}
