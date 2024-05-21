package jnpf.base.model.dblink;

import jnpf.base.Pagination;
import lombok.Data;

@Data
public class PaginationDbLink extends Pagination {

    private String dbType;

}
