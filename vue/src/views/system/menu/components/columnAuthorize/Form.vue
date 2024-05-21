<template>
  <div>
    <el-dialog :title="!dataForm.id ? '新建' : '编辑'" :close-on-click-modal="false"
      :close-on-press-escape="false" :visible.sync="visible" lock-scroll
      class="JNPF-dialog JNPF-dialog_center" width="600px">
      <el-form ref="dataForm" :model="dataForm" :rules="dataRule" label-width="100px"
        v-loading="formLoading" class="menuForm">
        <!--      <el-form-item label="绑定表格" prop="bindTable">-->
        <!--        <el-input v-model="dataForm.bindTable" placeholder="输入绑定表格" />-->
        <!--      </el-form-item>-->
        <!--      <el-form-item label="表格描述" prop="bindTableName">-->
        <!--        <el-input v-model="dataForm.bindTableName" placeholder="绑定表格描述" />-->
        <!--      </el-form-item>-->
        <jnpf-form-tip-item label="字段名称" prop="enCode">
          <el-input v-model="dataForm.enCode" placeholder="请输入字段名称">
            <template slot="append">
              <el-button type="primary" @click="selectName">选择</el-button>
            </template>
          </el-input>
        </jnpf-form-tip-item>
        <jnpf-form-tip-item label="数据库表" prop="bindTable" v-if="dataForm.bindTable">
          <el-input v-model="dataForm.bindTable" placeholder="请输入数据库表" disabled>
          </el-input>
        </jnpf-form-tip-item>
        <jnpf-form-tip-item label="字段规则" prop="fieldRule">
          <el-select v-model="dataForm.fieldRule" placeholder="请选择字段规则" clearable
            @change="changeFieldRule">
            <el-option v-for="item in fieldRuleOptions" :key="item.value" :label="item.label"
              :value="item.value">
              <template v-if="item.value!=0">
                <span>{{item.label}}<el-tooltip :content="`与主表是一对${item.value==1?'一':'多'}的主从关系`"
                    placement="top-start">
                    <i class="el-icon-question tooltip-question" style="margin-left:5px"></i>
                  </el-tooltip></span>
              </template>
            </el-option>
          </el-select>
        </jnpf-form-tip-item>
        <jnpf-form-tip-item label="关联字段" prop="childTableKey"
          tip-label="输入表单设计内设计子表的控件字段名，例：tableField107" v-if="dataForm.fieldRule==2">
          <el-input v-model="dataForm.childTableKey" placeholder="请输入关联主表的子表控件名称" />
        </jnpf-form-tip-item>
        <jnpf-form-tip-item label="字段说明" prop="fullName">
          <el-input v-model="dataForm.fullName" placeholder="输入字段说明" />
        </jnpf-form-tip-item>
        <jnpf-form-tip-item label="排序" prop="sortCode">
          <el-input-number :min="0" :max="999999" v-model="dataForm.sortCode"
            controls-position="right" />
        </jnpf-form-tip-item>
        <jnpf-form-tip-item label="状态" prop="enabledMark">
          <el-switch v-model="dataForm.enabledMark" :active-value="1" :inactive-value="0" />
        </jnpf-form-tip-item>
        <jnpf-form-tip-item label="备注" prop="description">
          <el-input v-model="dataForm.description" type="textarea" :rows="3" />
        </jnpf-form-tip-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="visible = false">{{
        $t("common.cancelButton")
      }}</el-button>
        <el-button type="primary" :loading="btnLoading" @click="dataFormSubmit()">
          {{ $t("common.confirmButton") }}</el-button>
      </span>
    </el-dialog>
    <nameSelects :visible.sync="nameVisible" :value="dataForm.enCode" :moduleId="dataForm.moduleId"
      :title="dataForm.enCode" :dataType="dataType" :bindTable="dataForm.bindTable"
      :menuType="menuType" :treeData="treeData" ref="nameForm" @closeForm="closeForm" />
  </div>
</template>

<script>
import {
  createColumn,
  updateColumn,
  getColumnInfo
} from "@/api/system/columnAuthorize";
import nameSelects from '../NameSelect.vue'
import { getVisualTables } from "@/api/system/authorize"
export default {
  components: { nameSelects },
  data() {
    return {
      nameVisible: false,
      visible: false,
      formLoading: false,
      btnLoading: false,
      dataForm: {
        id: "",
        moduleId: "",
        bindTable: "",
        bindTableName: "",
        fullName: "",
        enCode: "",
        sortCode: 0,
        enabledMark: 1,
        description: "",
        fieldRule: 0,
        childTableKey: ''
      },
      treeData: [],
      menuType: '',
      dataType: "",
      enCodeOptions: [],
      fieldRuleOptions: [
        { value: 0, label: "主表规则" },
        { value: 1, label: "副表规则" },
        { value: 2, label: "子表规则" }
      ],
      dataRule: {
        enCode: [
          { required: true, message: "字段名称不能为空", trigger: "change" }
        ],
        childTableKey: [
          { required: true, message: "关联字段不能为空", trigger: "blur" }
        ],
        fullName: [
          { required: true, message: "字段说明不能为空", trigger: "blur" }
        ],
        fieldRule: [
          { required: true, message: "字段规则不能为空", trigger: "change" }
        ],
      }
    };
  },
  methods: {
    init(moduleId, id, menuType, dataType) {
      this.menuType = menuType;
      this.dataType = dataType
      this.dataForm.id = id || "";
      this.dataForm.moduleId = moduleId;
      this.visible = true;
      this.formLoading = true;
      this.treeData = []
      getVisualTables(moduleId, dataType).then(res => {
        let data = []
        for (const key in res.data.linkTables) {
          data.push({
            tableName: res.data.linkTables[key],
            dbLink: res.data.linkId
          })
        }
        this.treeData = data
      })
      this.$nextTick(() => {
        this.$refs["dataForm"].resetFields()
        this.dataForm.bindTable = ''
        this.dataForm.fieldRule = 0
        // 获取字段数据
        if (this.dataForm.id) {
          getColumnInfo(this.dataForm.id).then(res => {
            this.dataForm = res.data
          })
        }
        this.formLoading = false
      });
    },
    selectName() {
      this.nameVisible = true
      this.$nextTick(() => {
        this.$refs.nameForm.openDialog();
      });
    },
    changeFieldRule() {
      this.dataForm.childTableKey = ''
    },
    closeForm(val, value) {
      this.dataForm.enCode = val
      if (value.tableName) this.dataForm.bindTable = value.tableName
      if (value.fieldName) this.dataForm.fullName = value.fieldName
    },
    onEnCodeChange(e) {
      let objVal;
      this.dataForm.fullName = "";
      this.dataForm.type = "";
      if (!!e) {
        this.enCodeOptions.forEach(o => {
          if (o.field === e) {
            objVal = {
              fullName: o.fieldName,
              type: o.dataType
            };
            return objVal;
          }
        });
        this.dataForm.fullName = objVal["fullName"];
        this.dataForm.type = objVal["type"];
      }
    },
    dataFormSubmit() {
      this.$refs["dataForm"].validate(valid => {
        if (valid) {
          this.btnLoading = true;
          const formMethod = this.dataForm.id ? updateColumn : createColumn;
          formMethod(this.dataForm)
            .then(res => {
              this.$message({
                message: res.msg,
                type: "success",
                duration: 1500,
                onClose: () => {
                  this.visible = false;
                  this.btnLoading = false;
                  this.$emit("refreshDataList");
                }
              });
            })
            .catch(() => {
              this.btnLoading = false;
            });
        }
      });
    }
  }
};
</script>
<style lang="scss" scoped></style>
