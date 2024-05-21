package jnpf.message.util;

import jnpf.config.ConfigValueUtil;
import jnpf.constant.FileTypeConstant;
import jnpf.message.model.UploadFileModel;
import jnpf.util.FileUploadUtils;
import jnpf.util.StringUtil;
import jnpf.util.context.SpringContext;
import org.apache.commons.codec.binary.Base64;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/16 10:45
 */
public class Base64Util {

    private static ConfigValueUtil configValueUtil = SpringContext.getBean(ConfigValueUtil.class);

    /**
     * 把文件转化为base64.
     *
     * @param filePath 源文件路径
     */
    public static String fileToBase64(String filePath) {
        if (!StringUtil.isEmpty(filePath)) {
            try {
                byte[] bytes = Files.readAllBytes(Paths.get(filePath));
                return Base64.encodeBase64String(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * base64转化为文件.
     *
     * @param base64   base64
     * @param filePath 目标文件路径
     */
    public static void base64ToFile(String base64, String filePath, String fileName) {
        try {
            Files.write(Paths.get(filePath), Base64.decodeBase64(base64), StandardOpenOption.CREATE);
            UploadFileModel model = new UploadFileModel(filePath, FileTypeConstant.IM, fileName);
            FileUploadUtils.uploadFile(model.getBarray(), model.getFolderName(), model.getObjectName());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
