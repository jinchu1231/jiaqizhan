<template>
  <div :class="{fullscreen:fullscreen}" class="tinymce-container">
    <editor ref="mceEditor" :id="tinymceId" v-model="dataValue" v-bind="$attrs" :init="showInit"
      v-if="showEdit" :plugins="showPlugins" :toolbar="showToolbar" class="tinymce-textarea"
      :style="{ width: '100%', height: (height || 300) + 'px' }"
      tinymce-script-src="/cdn/tinymce/tinymce.min.js" v-on="$listeners" />
    <div class="editor-custom-btn-container">
      <editorImage class="editor-upload-btn" color="#1890ff" :disabled='$attrs.disabled'
        :autoUpload='false' @successOK="successOK" />
    </div>
  </div>
</template>
<script>
import Editor from '@tinymce/tinymce-vue'
import { toolbar, plugins } from './tinymce';
import editorImage from './components/EditorImage'
let unique = 0

export default {
  name: 'JnpfEditor',
  components: {
    Editor, editorImage
  },
  inheritAttrs: false,
  props: {
    value: String,
    init: Object,
    height: {
      type: [String, Number],
      default: 400
    },
    plugins: [String, Array],
    toolbar: [String, Array],
    menubar: [String, Array],
    showImageUpload: {
      type: Boolean,
      default: true,
    },
  },
  data() {
    return {
      pageVisible: false,
      tinymceId: this.uuid(),
      dataValue: '',
      languageTypeList: {
        'en': 'en',
        'zh': 'zh_CN',
        'es': 'es_MX',
        'ja': 'ja'
      },
      showInit: {},
      showEdit: false,
      fullscreen: false
    }
  },
  computed: {
    editor() {
      return window.tinymce.get(this.tinymceId)
    },
    showToolbar() {
      return this.toolbar != undefined ? this.toolbar : toolbar
    },
    showPlugins() {
      return this.plugins != undefined ? this.plugins : plugins
    }
  },
  watch: {
    value: {
      handler(val) {
        this.dataValue = val || ''
      },
      immediate: true,
      deep: true
    },
    dataValue() {
      this.$emit('input', this.dataValue)
    },
    init: {
      handler() {
        this.showEdit = false
        this.uuid()
        this.initTinymce()
        this.$nextTick(() => {
          this.showEdit = true
        })
      },
      immediate: true,
      deep: true
    }
  },
  deactivated() {
    this.destroyTinymce()
  },
  destroyed() {
    this.destroyTinymce()
  },
  methods: {
    initTinymce() {
      let _this = this
      const initDefault = {
        skin: 'wukong',
        resize: false,
        branding: false,
        language: this.languageTypeList['zh'],
        menubar: 'file edit insert view format table',
        body_class: 'panel-body',
        image_advtab: true,
        end_container_on_empty_block: true,
        powerpaste_word_import: 'clean',
        fontsize_formats: '12px 14px 16px 18px 24px 36px 48px 56px 72px',
        font_formats: 'Andale Mono=andale mono,times;Arial=arial,helvetica,sans-serif;Arial Black=arial black,avant garde;Book Antiqua=book antiqua,palatino;Comic Sans MS=comic sans ms,sans-serif;Courier New=courier new,courier;Georgia=georgia,palatino;Helvetica=helvetica;Impact=impact,chicago;Symbol=symbol;Tahoma=tahoma,arial,helvetica,sans-serif;Terminal=terminal,monaco;Times New Roman=times new roman,times;Trebuchet MS=trebuchet ms,geneva;Verdana=verdana,geneva;Webdings=webdings;Wingdings=wingdings,zapf dingbats;微软雅黑=Microsoft YaHei,Helvetica Neue,PingFang SC,sans-serif;苹果苹方=PingFang SC,Microsoft YaHei,sans-serif;宋体=simsun,serif',
        code_dialog_height: 450,
        code_dialog_width: 1000,
        advlist_bullet_styles: 'square',
        advlist_number_styles: 'default',
        imagetools_cors_hosts: ['www.tinymce.com', 'codepen.io'],
        default_link_target: '_blank',
        link_title: false,
        object_resizing: true,
        paste_data_images: true, // 允许粘贴图像
        quickbars_insert_toolbar: false,
        nonbreaking_force_tab: true,
        convert_urls: false,
        toolbar_mode: 'sliding',
        toolbar_persist: false,
        ...this.init,
        setup(editor) {
          editor.on('FullscreenStateChanged', (e) => {
            _this.fullscreen = e.state
          })
        },
      }

      /**
       * statusbar 隐藏底部状态栏
       */

      if (this.height != undefined) {
        initDefault.height = this.height
      }
      this.showInit = initDefault

    },
    uuid() {
      const time = Date.now()
      const random = Math.floor(Math.random() * 1000000000)

      unique++

      return 'wukong_' + random + unique + String(time)
    },
    successOK(url) {
      window.tinymce.get(this.tinymceId).insertContent(`<img  src="${url}" >`)
    },
    destroyTinymce() {
      const tinymce = window.tinymce.get(this.tinymceId)
      if (this.fullscreen) {
        tinymce.execCommand('mceFullScreen')
      }

      if (tinymce) {
        tinymce.destroy()
      }
    },
  }
}
</script>

<style lang="scss">
.tinymce-container {
  position: relative;
  line-height: normal;
}
.tinymce-container >>> .mce-fullscreen {
  z-index: 10000;
}
.tinymce-textarea {
  visibility: hidden;
  z-index: -1;
}
.tox-tinymce-aux {
  z-index: 10001 !important;
}
.tox .tox-toolbar,
.tox .tox-toolbar__overflow,
.tox .tox-toolbar__primary {
  background-color: #fff !important;
}

.editor-custom-btn-container {
  position: absolute;
  right: 4px;
  top: 4px;
  z-index: 10000;
}

.fullscreen .editor-custom-btn-container {
  z-index: 10000 !important;
  position: fixed;
}

.editor-upload-btn {
  display: inline-block;
}
</style>
