package jnpf.base.model.Template7;



import jnpf.model.visualJson.FieLdsModel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class Template7DataModel {
    private List<FieLdsModel> fields;
}
