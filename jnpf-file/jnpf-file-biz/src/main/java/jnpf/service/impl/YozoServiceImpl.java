package jnpf.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jnpf.base.ActionResult;
import jnpf.base.service.SuperServiceImpl;
import jnpf.base.vo.PaginationVO;
import jnpf.constant.MsgCode;
import jnpf.entity.FileEntity;
import jnpf.mapper.FileMapper;
import jnpf.model.YozoFileParams;
import jnpf.service.YozoService;
import jnpf.utils.SplicingUrlUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

/**
 *
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date  2021/5/13
 */
@Service
public class YozoServiceImpl extends SuperServiceImpl<FileMapper,FileEntity> implements YozoService {

    @Autowired
    private FileMapper fileMapper;

    @Override
    public String getPreviewUrl(YozoFileParams params) {
        String previewUrl = SplicingUrlUtil.getPreviewUrl(params);
        return previewUrl;
    }

    @Override
    public ActionResult saveFileId(String fileVersionId, String fileId, String fileName) {
        FileEntity fileEntity =new FileEntity();
        fileEntity.setId(fileId);
        fileEntity.setFileName(fileName);
        fileEntity.setFileVersionId(fileVersionId);
        fileEntity.setType("create");
        this.save(fileEntity);

        return ActionResult.success("新建文档成功");
    }

    @Override
    public FileEntity selectByName(String fileNa) {
        QueryWrapper<FileEntity> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(FileEntity::getFileName,fileNa);
        return this.getOne(wrapper);
    }

    @Override
    public ActionResult saveFileIdByHttp(String fileVersionId, String fileId, String fileUrl) {
        String fileName = "";
        String url = "";
        String name = "";
        try {
            url = URLDecoder.decode(fileUrl, "UTF-8");
            if (url.contains("/")) {
                fileName = url.substring(url.lastIndexOf("/") + 1);
            } else {
                fileName = url.substring(url.lastIndexOf("\\") + 1);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        //同一url文件数
        QueryWrapper<FileEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("F_Url", url);
        Long total = fileMapper.selectCount(wrapper);
        if (total == 0) {
            name = fileName;
        } else {
            String t = total.toString();
            name = fileName + "(" + t + ")";
        }
        FileEntity fileEntity = new FileEntity();
        fileEntity.setType(url.contains("http") ? "http" : "local");
        fileEntity.setFileVersionId(fileVersionId);
        fileEntity.setId(fileId);
        fileEntity.setFileName(name);
        fileEntity.setUrl(url);
        fileMapper.insert(fileEntity);
        return ActionResult.success("新建文档成功");
    }

    @Override
    public ActionResult deleteFileByVersionId(String versionId) {
        QueryWrapper<FileEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("F_FileVersion", versionId);
        int i = fileMapper.delete(wrapper);
        if (i == 1) {
            return ActionResult.success(MsgCode.SU003.get());
        }
        return ActionResult.fail("删除失败");
    }

    @Override
    public FileEntity selectByVersionId(String fileVersionId) {
        QueryWrapper<FileEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("F_FileVersion", fileVersionId);
        FileEntity fileEntity = fileMapper.selectOne(wrapper);
        return fileEntity;
    }

    @Override
    public ActionResult deleteBatch(String[] versions) {
        for (String version : versions) {
            QueryWrapper<FileEntity> wrapper = new QueryWrapper<>();
            wrapper.eq("F_FileVersion", version);
            int i = fileMapper.delete(wrapper);
            if (i == 0) {
                return ActionResult.fail("删除文件:" + version + "失败");
            }
        }
        return ActionResult.success(MsgCode.SU003.get());

    }

    @Override
    public void editFileVersion(String oldFileId, String newFileId) {
        UpdateWrapper<FileEntity> wrapper = new UpdateWrapper<>();
        wrapper.eq("F_FileVersion", oldFileId);
        FileEntity fileEntity = new FileEntity();
        fileEntity.setFileVersionId(newFileId);
        fileEntity.setOldFileVersionId(oldFileId);
        fileMapper.update(fileEntity, wrapper);
    }

    @Override
    public List<FileEntity> getAllList(PaginationVO pageModel) {
        Page page = new Page(pageModel.getCurrentPage(), pageModel.getPageSize());
        IPage<FileEntity> iPage = fileMapper.selectPage(page, null);
        return iPage.getRecords();
    }

}
