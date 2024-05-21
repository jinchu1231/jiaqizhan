package jnpf.base.model;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class ExportSelectedModel {
    private String tableField;
    private String field;
    private String label;
    private List<ExportSelectedModel> selectedModelList;
}
