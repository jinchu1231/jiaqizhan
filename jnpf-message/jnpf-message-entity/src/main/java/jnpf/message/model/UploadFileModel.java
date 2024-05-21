package jnpf.message.model;
import lombok.Data;

import java.io.InputStream;
import java.io.Serializable;

/**
 * 文件上传模型
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2021-07-22
 */
@Data
public class UploadFileModel implements Serializable {
    protected String folderName;
    protected String objectName;
    private String filePath;
    private InputStream is;
    private byte[] barray;

    public UploadFileModel(String filePath, String folderName, String objectName) {
        this.filePath = filePath;
        this.folderName = folderName;
        this.objectName = objectName;
    }

    public UploadFileModel(String folderName, String objectName, String filePath, byte[] barray) {
        this.folderName = folderName;
        this.objectName = objectName;
        this.filePath = filePath;
        this.barray = barray;
    }

    public UploadFileModel(String folderName, String objectName, byte[] barray) {
        this.folderName = folderName;
        this.objectName = objectName;
        this.barray = barray;
    }

    public UploadFileModel(String folderName, String objectName, String filePath, InputStream is) {
        this.folderName = folderName;
        this.objectName = objectName;
        this.filePath = filePath;
        this.is = is;
    }

    public UploadFileModel() {
    }
}
