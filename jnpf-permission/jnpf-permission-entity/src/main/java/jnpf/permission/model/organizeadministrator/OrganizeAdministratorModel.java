package jnpf.permission.model.organizeadministrator;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrganizeAdministratorModel  {
    private List<String> addList = new ArrayList<>();
    private List<String> editList = new ArrayList<>();
    private List<String> deleteList = new ArrayList<>();
    private List<String> selectList = new ArrayList<>();
}
