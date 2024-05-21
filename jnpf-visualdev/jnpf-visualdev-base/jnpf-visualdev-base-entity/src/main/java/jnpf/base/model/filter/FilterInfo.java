

package jnpf.base.model.filter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class FilterInfo {
    private String id;
    private String moduleId;
    private String config;
    private String deleteMark;
    
}