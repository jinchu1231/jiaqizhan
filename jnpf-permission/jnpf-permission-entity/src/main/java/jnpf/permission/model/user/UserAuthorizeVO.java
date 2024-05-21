package jnpf.permission.model.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserAuthorizeVO {
    @Schema(description = "按钮")
    private List<UserAuthorizeModel> button;
    @Schema(description = "列表")
    private List<UserAuthorizeModel> column;
    @Schema(description = "菜单")
    private List<UserAuthorizeModel> module;
    @Schema(description = "数据权限")
    private List<UserAuthorizeModel> resource;
    @Schema(description = "表单")
    private List<UserAuthorizeModel> form;
    private List<UserAuthorizeModel> portal;
}
