package jnpf.model.document;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class DocumentShareForm {

   @Schema(description ="用户主键")
   private String  userId;
}
