package jnpf.base.model.commonword;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 类功能
 *
 * @author JNPF开发平台组 YanYu
 * @version v3.4.6
 * @copyrignt 引迈信息技术有限公司
 * @date 2023-01-07
 */
@Data
@Schema(description = "CommonWordsForm对象")
public class CommonWordsForm {

    @Schema(description = "常用语Id")
    private String id;
    @Schema(description = "常用语类型(0:系统,1:个人)")
    private Integer commonWordsType;
    @Schema(description = "常用语")
    private String commonWordsText;
    @Schema(description = "排序")
    private Long sortCode;
    @Schema(description = "有效标志")
    private Integer enabledMark;

}
