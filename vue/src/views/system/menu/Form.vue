<template>
  <el-dialog :title="!dataForm.id ? '新建' : '编辑'" :close-on-click-modal="false"
    :close-on-press-escape="false" :visible.sync="visible" lock-scroll
    class="JNPF-dialog JNPF-dialog_center" width="600px">
    <el-form ref="dataForm" v-loading="formLoading" :model="dataForm" :rules="dataRule"
      label-width="100px" class="menuForm">
      <jnpf-form-tip-item label="名称" prop="fullName">
        <el-input v-model="dataForm.fullName" placeholder="输入名称" />
      </jnpf-form-tip-item>
      <jnpf-form-tip-item label="编码" prop="enCode">
        <el-input v-model="dataForm.enCode" placeholder="输入编码" @change="getSelfUrl" />
      </jnpf-form-tip-item>
      <jnpf-form-tip-item label="图标" prop="icon">
        <el-row type="flex">
          <div style="flex:1;margin-right:10px">
            <el-input v-model="dataForm.icon" placeholder="请选择图标" readonly
              :suffix-icon="dataForm.icon">
              <el-button slot="append" @click="openIconBox">选择</el-button>
            </el-input>
          </div>
          <el-color-picker v-model="dataForm.propertyJson.iconBackgroundColor"
            :predefine="['#188ae2', '#35b8e0', '#26bf8c','#f9c851','#ff5b5b', '#5b69bc', '#ff8acc', '#3b3e47','#282828' ]" />
        </el-row>
      </jnpf-form-tip-item>
      <jnpf-form-tip-item label="访问地址" tip-label="仅支持PC端访问，APP端不支持。">
        <el-row type="flex">
          <div style="flex:1;margin-right:10px">
            <el-input v-model="selfUrl" placeholder="访问地址" readonly />
          </div>
          <el-button-group>
            <el-button @click="openWindow(selfUrl)">打开</el-button>
            <el-button type="primary" @click="handleCopy(selfUrl,$event)">复制</el-button>
          </el-button-group>
        </el-row>
      </jnpf-form-tip-item>
      <jnpf-form-tip-item label="导航图标">
        <div class="img-list">
          <single-img v-model="dataForm.navigationIcon" tip="文字图标" sub-tip="210 × 60 px" />
          <single-img v-model="dataForm.workLogoIcon" tip="Logo图标" sub-tip="60 × 60 px" />
        </div>
      </jnpf-form-tip-item>
      <jnpf-form-tip-item label="协同办公" tip-label="关闭后该应用下不显示协同办公模块内容" prop="workflowEnabled">
        <el-switch v-model="dataForm.workflowEnabled" :active-value="1" :inactive-value="0" />
      </jnpf-form-tip-item>
      <jnpf-form-tip-item label="排序" prop="sortCode">
        <el-input-number :min="0" :max="999999" v-model="dataForm.sortCode"
          controls-position="right" />
      </jnpf-form-tip-item>
      <jnpf-form-tip-item label="状态" prop="enabledMark">
        <el-switch v-model="dataForm.enabledMark" :active-value="1" :inactive-value="0" />
      </jnpf-form-tip-item>
      <jnpf-form-tip-item label="说明" prop="bodyText">
        <el-input v-model="dataForm.description" type="textarea" :rows="4" />
      </jnpf-form-tip-item>
    </el-form>
    <span slot="footer" class="dialog-footer">
      <el-button @click="visible = false">{{$t('common.cancelButton')}}</el-button>
      <el-button type="primary" :loading="btnLoading" @click="dataFormSubmit()">
        {{$t('common.confirmButton')}}</el-button>
    </span>
    <icon-box :visible.sync="iconBoxVisible" ref="iconBox" :current="dataForm.icon"
      @choiceIcon="choiceIcon" />
  </el-dialog>
</template>

<script>
import iconBox from "@/components/JNPF-iconBox";
import { create, update, info } from "@/api/system/system";
import singleImg from "@/components/Upload/SingleImg";
import clipboard from "@/utils/clipboard";

export default {
  components: { iconBox, singleImg },
  data() {
    return {
      visible: false,
      dataForm: {
        id: "",
        fullName: "",
        enCode: "",
        sortCode: 0,
        icon: "",
        navigationIcon: "",
        enabledMark: 1,
        workflowEnabled: 1,
        description: "",
        workLogoIcon: "",
        propertyJson: {
          iconBackgroundColor: ""
        }
      },
      dataRule: {
        fullName: [
          { required: true, message: "名称不能为空", trigger: "blur" }
        ],
        enCode: [{ required: true, message: "编码不能为空", trigger: "blur" }],
        icon: [{ required: true, message: "图标不能为空", trigger: "click" }]
      },
      formLoading: false,
      iconBoxVisible: false,
      btnLoading: false,
      selfUrl: ""
    };
  },
  methods: {
    init(id) {
      this.dataForm.id = id || "";
      this.selfUrl = "";
      this.visible = true;
      this.$nextTick(() => {
        this.$refs["dataForm"].resetFields();
        if (this.dataForm.id) {
          this.formLoading = true;
          info(this.dataForm.id)
            .then(res => {
              this.dataForm = res.data;
              const propertyJson = res.data.propertyJson
                ? JSON.parse(res.data.propertyJson)
                : null;
              this.dataForm.propertyJson = propertyJson || {
                iconBackgroundColor: ""
              };
              this.getSelfUrl(res.data.enCode);
              this.$nextTick(() => {
                this.formLoading = false;
              });
            })
            .catch(() => { });
        } else {
          this.dataForm.propertyJson.iconBackgroundColor = "";
          this.dataForm.description = "";
          this.dataForm.workLogoIcon = "";
          this.dataForm.navigationIcon = "";
        }
      });
    },
    openIconBox() {
      this.iconBoxVisible = true;
    },
    dataFormSubmit() {
      this.$refs["dataForm"].validate(valid => {
        if (valid) {
          const query = {
            ...this.dataForm,
            propertyJson: JSON.stringify(this.dataForm.propertyJson)
          };
          const formMethod = this.dataForm.id ? update : create;
          formMethod(query).then(res => {
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
          }).catch(() => {
            this.btnLoading = false;
          });
        }
      });
    },
    choiceIcon(value) {
      this.dataForm.icon = value;
    },
    getSelfUrl(enCode) {
      if (!enCode) return (this.selfUrl = "");
      this.selfUrl = `${window.location.origin}/JNPF_APP_${enCode}`;
    },
    handleCopy(text, event) {
      if (text) clipboard(text, event);
    },
    openWindow(val) {
      if (val) window.open(val);
    }
  }
};
</script>
<style lang="scss" scoped>
.img-list {
  display: flex;
  >>> .singleImg-container {
    margin-right: 20px;
    :last-child {
      margin-right: 0;
    }
  }
}
</style>