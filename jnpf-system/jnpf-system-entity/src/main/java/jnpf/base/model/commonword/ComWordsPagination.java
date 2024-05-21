package jnpf.base.model.commonword;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import jnpf.base.Pagination;
import jnpf.base.entity.CommonWordsEntity;
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
public class ComWordsPagination extends Pagination {

    @Schema(description = "状态")
    private Integer enabledMark;

    public Page<CommonWordsEntity> getPage(){
        return new Page<>(getCurrentPage(), getPageSize(), getTotal());
    }

}
