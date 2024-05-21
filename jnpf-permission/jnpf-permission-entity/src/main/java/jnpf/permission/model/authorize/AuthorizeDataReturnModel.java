package jnpf.permission.model.authorize;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthorizeDataReturnModel {
    private String id;
    private String fullName;
    private String icon;
    private String type;
    private Long sortCode=999L;
    private String category;
    private boolean disabled;
    private Long creatorTime;
    private List<AuthorizeDataReturnModel> children;
}
