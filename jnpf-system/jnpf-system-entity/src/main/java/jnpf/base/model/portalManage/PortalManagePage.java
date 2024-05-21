package jnpf.base.model.portalManage;

import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jnpf.base.Pagination;
import jnpf.base.vo.PaginationVO;
import lombok.Data;

/**
 * 类功能
 *
 * @author JNPF开发平台组 YanYu
 * @version v3.4.8
 * @copyrignt 引迈信息技术有限公司
 * @date 2023-04-17
 */
@Data
public class PortalManagePage extends Pagination {

    @Schema(description = "平台")
    private String platform;

    @Schema(description = "分类（字典）")
    private String category;

    @Schema(description = "系统ID")
    private String systemId;

    @Schema(description = "是否禁用")
    private Integer enabledMark;

    @Schema(description = "是否禁用")
    private Integer state;

    public <T> PageDTO<T> getPageDto(){
        return new PageDTO<T>(getCurrentPage(), getPageSize());
    }

    public PaginationVO getPaginationVO(){
        PaginationVO paginationVO = new PaginationVO();
        paginationVO.setTotal(Long.valueOf(getTotal()).intValue());
        paginationVO.setCurrentPage(getCurrentPage());
        paginationVO.setPageSize(getPageSize());
        return paginationVO;
    }

}
