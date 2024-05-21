<template>
  <div class="editor-slide-upload fullscreen">
    <el-upload :action="getAction" :show-file-list="false" :on-success="handleSuccess"
      :auto-upload='autoUpload' :on-error="handleUploadError" :headers="uploadHeaders"
      :before-upload="beforeUpload" :on-change="handleChange" ref="upload">
      <el-button size="small" :disabled='disabled' class="img-btn" type="text">图片上传</el-button>
    </el-upload>
  </div>
</template>

<script>

export default {
  name: 'EditorImage',
  props: {
    color: {
      type: String,
      default: '#1890ff'
    },
    disabled: {
      type: Boolean,
      default: false,
    },
    type: {
      type: String,
      default: 'annexpic'
    },
    autoUpload: {
      type: Boolean,
      default: true,
    },
  },
  data() {
    return {
      dialogVisible: false,
      listObj: {},
      fileList: [],
      uploading: false
    }
  },
  computed: {
    getAction() {
      return this.define.comUploadUrl + '/' + this.type
    },
    uploadHeaders() {
      return { Authorization: this.$store.getters.token }
    }
  },
  methods: {
    handleChange(file) {
      this.beforeUpload(file.raw)
    },
    handleUploadError(err, file, fileList) {
      // 处理上传失败的回调
      console.error("图片上传失败！", err);
    },
    beforeUpload(file) {
      let isAccept = new RegExp('image/*').test(file.type)
      if (!isAccept) {
        this.$message.error(`请上传图片`)
        return isAccept;
      }
      this.getBase64WithFile(file).then(({ result: thumbUrl }) => {
        this.$emit('successOK', thumbUrl)
      });
    },
    handleSuccess(res, file) {
      if (res.code == 200) {
        this.urlToBase64(this.define.APIURl + res.data.url).then(base64 => {
          this.$emit('successOK', base64)
        })
      } else {
        this.$message({ message: res.msg, type: 'error', duration: 1500 })
      }
    },
    urlToBase64(url, mineType) {
      return new Promise((resolve, reject) => {
        let canvas = document.createElement('CANVAS');
        const ctx = canvas.getContext('2d');
        const img = new Image();
        img.crossOrigin = '';
        img.onload = function () {
          if (!canvas || !ctx) {
            return reject();
          }
          canvas.height = img.height;
          canvas.width = img.width;
          ctx.drawImage(img, 0, 0);
          const dataURL = canvas.toDataURL(mineType || 'image/*');
          canvas = null;
          resolve(dataURL);
        };
        img.src = url;
      });
    },
    getBase64WithFile(file) {
      return new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.readAsDataURL(file);
        reader.onload = () => resolve({ result: reader.result, file });
        reader.onerror = error => reject(error);
      });
    }
  }
}
</script>

<style lang="scss" scoped>
.editor-slide-upload {
  margin-right: 10px;
  .img-btn {
    font-size: 14px;
  }
}
</style>
