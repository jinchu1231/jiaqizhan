package jnpf.base.model.printdev.vo;


import io.swagger.v3.oas.annotations.media.Schema;
import jnpf.base.model.printdev.PrintDevTreeModel;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 打印模板数视图对象
 *
 * @author JNPF开发平台组 YY
 * @version V3.2.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月30日
 */
@Data
public class PrintDevVO {

    /**
     * 分类下模板数量
     */
    @Schema(description ="分类下模板数量")
    private Integer num;

    /**
     * 主键_id
     */
    @Schema(description ="主键_id")
    private String id;

    /**
     * 名称
     */
    @Schema(description ="名称")
    private String fullName;

    /**
     * 编码
     */
    @Schema(description ="编码")
    private String encode;

    /**
     * 分类
     */
    @Schema(description ="分类")
    private String category;

    /**
     * 类型
     */
    @Schema(description ="类型")
    private Integer type;

    /**
     * 描述
     */
    @Schema(description ="描述")
    private String description;

    /**
     * 排序码
     */
    @Schema(description ="排序码")
    private Integer sortCode;

    /**
     * 有效标志
     */
    @Schema(description ="有效标志")
    private Integer enabledMark;

    /**
     * 创建时间
     */
    @Schema(description ="创建时间")
    private LocalDateTime creatorTime;

    /**
     * 创建用户_id
     */
    @Schema(description ="创建用户_id")
    private String creatorUserId;

    /**
     * 修改时间
     */
    @Schema(description ="修改时间")
    private LocalDateTime lastModifyTime;

    /**
     * 修改用户_id
     */
    @Schema(description ="修改用户_id")
    private String lastModifyUserId;

    /**
     * 删除标志
     */
    @Schema(description ="删除标志")
    private Integer deleteMark;

    /**
     * 删除时间
     */
    @Schema(description ="删除时间")
    private LocalDateTime deleteTime;

    /**
     * 删除用户_id
     */
    @Schema(description ="删除用户_id")
    private String deleteUserId;

    /**
     * 连接数据 _id
     */
    @Schema(description ="连接数据 _id")
    private String dbLinkId;

    /**
     * sql语句
     */
    @Schema(description ="sql语句")
    private String sqlTemplate;

    /**
     * 左侧字段
     */
    @Schema(description ="左侧字段")
    private String leftFields;

    /**
     * 打印模板
     */
    @Schema(description ="打印模板")
    private String printTemplate;

    /**
     * 子节点
     */
    @Schema(description ="子节点")
    private List<PrintDevTreeModel> children;
}
