package jnpf.generater.controller;


import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.xuyanwu.spring.file.storage.FileInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jnpf.base.ActionResult;
import jnpf.base.entity.DictionaryDataEntity;
import jnpf.base.entity.VisualdevEntity;
import jnpf.base.model.DownloadCodeForm;
import jnpf.base.model.read.ReadListVO;
import jnpf.base.service.DictionaryDataService;
import jnpf.base.service.VisualdevService;
import jnpf.base.util.ReadFile;
import jnpf.base.util.VisualUtil;
import jnpf.base.vo.DownloadVO;
import jnpf.base.vo.ListVO;
import jnpf.config.ConfigValueUtil;
import jnpf.constant.FileTypeConstant;
import jnpf.constant.MsgCode;
import jnpf.exception.DataException;
import jnpf.generater.service.VisualdevGenService;
import jnpf.util.DesUtil;
import jnpf.util.FileDownloadUtil;
import jnpf.util.FilePathUtil;
import jnpf.util.FileUploadUtils;
import jnpf.util.FileUtil;
import jnpf.util.NoDataSourceBind;
import jnpf.util.RandomUtil;
import jnpf.util.RedisUtil;
import jnpf.util.ServletUtil;
import jnpf.util.StringUtil;
import jnpf.util.TicketUtil;
import jnpf.util.UploaderUtil;
import jnpf.util.XSSEscape;
import jnpf.util.context.RequestContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 可视化开发功能表
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2021-04-02
 */
@Tag(name = "代码生成器", description = "VisualDevelopmentGen")
@RestController
@RequestMapping("/api/visualdev/Generater")
public class VisualdevGenController {

    @Autowired
    private ConfigValueUtil configValueUtil;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private VisualdevService visualdevService;
    @Autowired
    private VisualdevGenService visualdevGenService;
    @Autowired
    private DictionaryDataService dictionaryDataService;


    /**
     * 下载文件
     *
     * @return
     */
    @NoDataSourceBind()
    @Operation(summary = "下载文件")
    @GetMapping("/DownloadVisCode")
    public void downloadCode() throws DataException {
        HttpServletRequest request = ServletUtil.getRequest();
        String reqJson = request.getParameter("encryption");
        String name = request.getParameter("name");
        String fileNameAll = DesUtil.aesDecode(reqJson);
        if (!StringUtil.isEmpty(fileNameAll)) {
            String token = fileNameAll.split("#")[0];
            if (TicketUtil.parseTicket(token) != null) {
                TicketUtil.deleteTicket(token);
                String fileName = fileNameAll.split("#")[1];
                String path =  FilePathUtil.getFilePath(FileTypeConstant.CODETEMP);
                //下载到本地
                byte[] bytes = FileUploadUtils.downloadFileByte(path, fileName, false);
                FileDownloadUtil.downloadFile(bytes, fileName, name);
            }else {
                throw new DataException("下载链接已失效");
            }
        }else {
            throw new DataException("下载链接已失效");
        }
    }

    @Operation(summary = "获取命名空间")
    @GetMapping("/AreasName")
    @SaCheckPermission("generator.webForm")
    public ActionResult getAreasName() {
        String areasName = configValueUtil.getCodeAreasName();
        List<String> areasNameList = new ArrayList(Arrays.asList(areasName.split(",")));
        return ActionResult.success(areasNameList);
    }

    @Operation(summary = "下载代码")
    @Parameters({
            @Parameter(name = "id", description = "主键"),
    })
    @PostMapping("/{id}/Actions/DownloadCode")
    @SaCheckPermission("generator.webForm")
    @Transactional
    public ActionResult downloadCode(@PathVariable("id") String id, @RequestBody DownloadCodeForm downloadCodeForm) throws Exception {
        if(downloadCodeForm.getModule()!=null){
            DictionaryDataEntity info = dictionaryDataService.getInfo(downloadCodeForm.getModule());
            if(info!=null){
                downloadCodeForm.setModule(info.getEnCode());
            }
        }
        VisualdevEntity visualdevEntity = visualdevService.getInfo(id);
        String s = VisualUtil.checkPublishVisualModel(visualdevEntity,"下载");
        if (s!=null) {
            return ActionResult.fail(s);
        }
        DownloadVO vo;
        String fileName;
        if(RequestContext.isVue3()){
            downloadCodeForm.setVue3(true);
            fileName = visualdevGenService.codeGengerateV3(visualdevEntity, downloadCodeForm);
        }else{
            fileName = visualdevGenService.codeGengerate(id, downloadCodeForm);
        }
        //上传到minio
        String filePath = FileUploadUtils.getLocalBasePath() + configValueUtil.getServiceDirectoryPath() + fileName + ".zip";
        FileUtil.toZip(filePath, true, FileUploadUtils.getLocalBasePath() + configValueUtil.getServiceDirectoryPath() + fileName);
        // 删除源文件
        FileUtil.deleteFileAll(new File(FileUploadUtils.getLocalBasePath() + configValueUtil.getServiceDirectoryPath() + fileName));
        MultipartFile multipartFile = FileUtil.createFileItem(new File(XSSEscape.escapePath(filePath)));
        FileInfo fileInfo = FileUploadUtils.uploadFile(multipartFile, configValueUtil.getServiceDirectoryPath(), fileName + ".zip");
        vo = DownloadVO.builder().name(fileInfo.getFilename()).url(UploaderUtil.uploaderVisualFile(fileInfo.getFilename()) + "&name=" + fileName + ".zip").build();
        if (vo == null) {
            return ActionResult.fail(MsgCode.FA006.get());
        }
        return ActionResult.success(vo);
    }


    /**
     * 输出移动开发模板
     *
     * @return
     */
    @Operation(summary = "预览代码")
    @Parameters({
            @Parameter(name = "id", description = "主键"),
    })
    @PostMapping("/{id}/Actions/CodePreview")
    @SaCheckPermission("generator.webForm")
    public ActionResult codePreview(@PathVariable("id") String id, @RequestBody DownloadCodeForm downloadCodeForm) throws Exception {
        VisualdevEntity visualdevEntity = visualdevService.getInfo(id);
        String s = VisualUtil.checkPublishVisualModel(visualdevEntity,"预览");
        if (s!=null) {
            return ActionResult.fail(s);
        }
        String fileName;
        if(RequestContext.isVue3()){
            downloadCodeForm.setVue3(true);
            fileName = visualdevGenService.codeGengerateV3(visualdevEntity, downloadCodeForm);
        }else{
            fileName = visualdevGenService.codeGengerate(id, downloadCodeForm);
        }
        List<ReadListVO> dataList = ReadFile.priviewCode(FileUploadUtils.getLocalBasePath() + configValueUtil.getServiceDirectoryPath() + fileName);
        if (dataList == null && dataList.size() == 0) {
            return ActionResult.fail("预览失败，数据不存在");
        }
        ListVO datas = new ListVO<>();
        datas.setList(dataList);
        return ActionResult.success(datas);
    }

    /**
     * App预览(后台APP表单设计)
     *
     * @return
     */
    @Operation(summary = "App预览(后台APP表单设计)")
    @Parameters({
            @Parameter(name = "data", description = "数据"),
    })
    @PostMapping("/App/Preview")
    @SaCheckPermission("generator.webForm")
    public ActionResult appPreview(String data) {
        String id = RandomUtil.uuId();
        redisUtil.insert(id, data, 300);
        return ActionResult.success((Object) id);
    }

    /**
     * App预览(后台APP表单设计)
     *
     * @return
     */
    @Operation(summary = "App预览查看")
    @Parameters({
            @Parameter(name = "id", description = "主键"),
    })
    @GetMapping("/App/{id}/Preview")
    @SaCheckPermission("generator.webForm")
    public ActionResult preview(@PathVariable("id") String id) {
        if (redisUtil.exists(id)) {
            Object object = redisUtil.getString(id);
            return ActionResult.success(object);
        } else {
            return ActionResult.fail("已失效");
        }
    }

}


