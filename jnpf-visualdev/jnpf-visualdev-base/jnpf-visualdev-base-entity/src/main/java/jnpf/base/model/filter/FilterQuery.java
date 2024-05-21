package jnpf.base.model.filter;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.Date;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jnpf.base.entity.FilterEntity;
import javax.validation.constraints.NotNull;



@Data
@EqualsAndHashCode(callSuper = false)
public class FilterQuery extends Page<FilterEntity> {
    
}