<template>
  <div class="upload-btn">
    <el-button ref="uploadBtn" :type="buttonType" :icon="showIcon?'el-icon-upload2':''"
      :loading="loading" @click="openModal">
      {{buttonText}}
    </el-button>
    <el-dialog :title="buttonText" class="JNPF-dialog JNPF-dialog_center" :closeOnClickModal='false'
      :visible.sync="visible" append-to-body width="600px">
      <el-form :colon="false" label-width="100px" :model="dataForm" :rules="rules" ref="dataForm">
        <el-form-item label="上级菜单" prop="parentId" v-if="type == 'menu'">
          <JnpfTreeSelect v-model="dataForm.parentId" :options="treeOptions" placeholder="选择上级菜单"
            :lastLevel="this.data.category == 'App'" />
        </el-form-item>
        <el-form-item label="识别重复" prop="type" v-if="!getIsHideType">
          <el-radio-group v-model="dataForm.type" class="form-item-content">
            <el-radio :label="0">跳过
              <el-tooltip content="当导入数据不存在，作为新数据导入；数据已存在，不做处理" placement="top">
                <i class="el-icon-question tooltip-question"></i>
              </el-tooltip>
            </el-radio>
            <el-radio :label="1">追加
              <el-tooltip content="当导入数据已存在，增加相同记录新数据，名称和编码自动增加随机码" placement="top">
                <i class="el-icon-question tooltip-question"></i>
              </el-tooltip>
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="上传文件" prop="fileList">
          <el-upload :file-list="dataForm.fileList" :accept="accept" :before-upload="beforeUpload"
            @remove="handleRemove" action='' class="upload-file-btn">
            <el-button icon="icon-ym icon-ym-btn-upload ">上传文件</el-button>
            <div class="upload-file__tip">{{ getTipText }}</div>
          </el-upload>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="visible=false">取消</el-button>
        <el-button type="primary" :loading="loading" @click="handleSubmit()">确定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { upload } from '@/api/common';
import { getMenuSelector } from '@/api/system/menu';
import { importData } from '@/api/onlineDev/dataReport'

const hideList = ['/system/DataModel', '/system/DataInterfaceVariate']

export default {
  name: 'JNPF-uploadBtn',
  data() {
    return {
      loading: false,
      visible: false,
      dataForm: {
        type: 0,
        fileList: [],
        parentId: '',
      },
      rules: {
        parentId: [{ required: true, message: '上级菜单不能为空', trigger: 'change' }],
        type: [{ required: true, message: '请选择识别重复', trigger: 'change', type: 'number' }],
        fileList: [{ required: true, message: '请上传文件', trigger: 'change' }],
      },
      treeOptions: []
    }
  },
  props: {
    url: {
      type: String,
      default: ""
    },
    buttonText: {
      type: String,
      default: '导入'
    },
    buttonType: {
      type: String,
      default: 'text'
    },
    data: {
      type: Object,
      default: () => { }
    },
    showIcon: {
      type: Boolean,
      default: true
    },
    accept: {
      type: String,
      default: '*'
    },
    type: { type: String, default: '' },
    importType: { type: String, default: '' },
  },
  computed: {
    getTipText() {
      return `一次只能导入一个文件，文件后缀名必须是${this.accept}。`
    },
    getIsHideType() {

      return hideList.some(o => this.url.indexOf(o) > -1)
    }
  },
  methods: {
    openModal() {
      this.visible = true
      this.loading = false
      if (this.type == 'menu') this.getMenuSelectorList();
      this.$nextTick(() => {
        this.$refs.dataForm && this.$refs.dataForm.resetFields()
      })
    },
    getMenuSelectorList() {
      getMenuSelector({ category: this.data.category }, '', this.data.systemId).then(res => {
        this.treeOptions = [
          {
            fullName: '顶级节点',
            hasChildren: true,
            id: '-1',
            children: res.data.list,
          },
        ];
      });
    },
    handleSubmit() {
      this.loading = true
      this.$refs['dataForm'].validate((valid) => {
        if (!valid) return this.loading = false;
        if (!this.url && !this.importType) return this.loading = false;
        const formData = new FormData();
        formData.append('file', this.dataForm.fileList[0]);
        formData.append('type', this.dataForm.type);
        for (let [key, value] of Object.entries(this.data || {})) {
          formData.append(key, value);
        }
        if (this.type == 'menu') formData.append('parentId', this.dataForm.parentId);
        const formMethod = this.importType === 'dataReport' ? importData : upload
        formMethod(this.url, formData).then(res => {
          this.$message.success(res.msg);
          this.uploadSuccess()
          this.loading = false
          this.visible = false;
        }).catch(() => { this.loading = false })
      })
    },
    uploadSuccess() {
      this.$emit('on-success');
    },
    handleRemove() {
      this.dataForm.fileList = [];
    },
    beforeUpload(file) {
      const isAccept = this.checkAccept(file);
      if (!isAccept) {
        this.$message.error(`请选择${this.accept}类型的文件`);
        return isAccept;
      }
      this.dataForm.fileList = [file];
      return false;
    },
    checkAccept(file) {
      if (!this.accept || this.accept === '*') return true;
      const extension = file.name.substring(file.name.lastIndexOf("."));
      if (this.accept.indexOf(extension) > -1) return true;
      return false;
    }
  }
};
</script>
<style lang="scss" scoped>
.upload-btn {
  display: inline-block;
  margin: 0 10px;
}
.upload-file-btn {
  >>> .el-upload {
    text-align: left;
  }
}
.upload-file__tip {
  line-height: 1.2;
  color: #00000073;
  margin-top: 5px;
  font-size: 12px;
  word-break: break-all;
}
</style>