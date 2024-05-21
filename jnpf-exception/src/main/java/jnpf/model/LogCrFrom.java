package jnpf.model;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class LogCrFrom {

    @Schema(description ="id")
    private String id;

    @Schema(description ="用户id")
    private String userId;

    @Schema(description ="用户名")
    private String userName;

    @Schema(description ="分类")
    private Integer category;

    @Schema(description ="IP地址")
    private String iPAddress;

    @Schema(description ="IP地址名称")
    private String iPAddressName;

    @Schema(description ="请求地址")
    private String requestURL;

    @Schema(description ="请求方法")
    private String requestMethod;

    @Schema(description ="平台")
    private String platForm;

    @Schema(description ="菜单id")
    private String moduleId;

}
