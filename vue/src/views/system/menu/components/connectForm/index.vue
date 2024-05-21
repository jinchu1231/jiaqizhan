<template>
  <el-dialog title="数据连接" :close-on-click-modal="false" :close-on-press-escape="false"
    :visible.sync="visible" lock-scroll class="JNPF-dialog JNPF-dialog_center" width="630px">
    <el-form ref="dataForm" :model="dataForm" :rules="dataRule" label-width="80px"
      v-loading="formLoading" class="menuForm">
      <el-form-item label="数据连接" prop="dbLinkId">
        <el-select v-model="dataForm.dbLinkId" placeholder="请选择数据库" @change="onDbChange" clearable
          filterable>
          <el-option-group v-for="group in dbOptions" :key="group.fullName" :label="group.fullName">
            <el-option v-for="item in group.children" :key="item.id" :label="item.fullName"
              :value="item.id" />
          </el-option-group>
        </el-select>
      </el-form-item>
      <el-form-item label="数据选择" prop="dataSelect" class="el-select">
        <dataTable v-model="dataForm.dataSelect" placeholder="请选择数据表" multiple clearable
          :dbLinkId="dataForm.dbLinkId" :value='dataForm.dataSelect' @change="onDataChange" />
      </el-form-item>
    </el-form>
    <span slot="footer" class="dialog-footer">
      <el-button @click="visible = false">{{$t("common.cancelButton")}}</el-button>
      <el-button type="primary" :loading="btnLoading" @click="dataFormSubmit()">
        {{ $t("common.confirmButton") }}</el-button>
    </span>
  </el-dialog>
</template>

<script>
import { saveLinkData } from "@/api/system/authorize";
import dataTable from './DataTable.vue'
export default {
  components: { dataTable },
  data() {
    return {
      inputHovering: false,
      visible: false,
      formLoading: false,
      btnLoading: false,
      dataForm: {
        id: "",
        dbLinkId: "0",
        dataSelect: ""
      },
      moduleId: "",
      dataRule: {
        dbLinkId: [
          { required: true, message: "数据库不能为空", trigger: "change" }
        ],
        dataSelect: [
          { required: true, message: "数据表不能为空", trigger: "change" }
        ]
      },
      dbOptions: [],
      type: 0,
      dataType: ""
    };
  },
  methods: {
    init(dataList, moduleId, dbOptions, dataType, type) {
      this.dataType = dataType
      this.moduleId = moduleId
      this.type = type || 1
      this.visible = true;
      this.dbOptions = dbOptions;
      this.$nextTick(() => {
        this.$refs['dataForm'].resetFields()
        if (dataList) {
          this.dataForm.id = dataList.id || ''
          this.dataForm.dbLinkId = dataList.linkId || '0'
          this.dataForm.dataSelect = dataList.linkTables ? dataList.linkTables.split(",") : []
        }
      })
    },
    onDataChange(val) {
      if (val && val.length) this.$refs['dataForm'].clearValidate()
    },
    dataFormSubmit() {
      this.$refs["dataForm"].validate(valid => {
        if (valid) {
          let query = {
            id: this.dataForm.id,
            moduleId: this.moduleId,
            linkId: this.dataForm.dbLinkId,
            linkTables: this.dataForm.dataSelect.toString(),
            dataType: this.dataType
          }
          saveLinkData(query).then(res => {
            this.$emit("refreshDataList", this.dataForm.dataSelect.toString());
            this.visible = false;
          }).catch(() => { });
        }
      });
    },
    onDbChange() {
      this.dataForm.dataSelect = ''
    },
  }
};
</script>
