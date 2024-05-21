<template>
  <div class="print-template-detail">
    <div class="print-template-detail__body">
      <div class="system-view-nav">
        <el-scrollbar class="left-scrollbar">
          <div class="JNPF-common-title">
            <h2>表单字段</h2>
          </div>
          <el-tree :data="newData" default-expand-all :props="defaultProps"
            @node-click="handleNodeClick"></el-tree>
          <div class="JNPF-common-title sys-title">
            <h2>系统字段</h2>
          </div>
          <el-tree :data="newSysData" default-expand-all :props="defaultProps"
            @node-click="handleNodeClick"></el-tree>
          <div class="JNPF-common-title sys-title">
            <h2>函数</h2>
          </div>
          <el-tree :data="parameter" default-expand-all :props="defaultProps"
            @node-click="handleNodeClick"></el-tree>
        </el-scrollbar>
      </div>
      <div class="system-view-content">
        <ts-designer-tinymce v-model="content" ref="createTinymce" :height="richHeight"
          :init="initConfig" class="rich-txt" />
      </div>
      <pageSize ref="pageSize" v-model="pageParam" @change="getEditConfig"></pageSize>
    </div>
    <el-dialog title="选择字典分类" :close-on-click-modal="false" :close-on-press-escape="false"
      :visible.sync="dialogVisible" lock-scroll class="JNPF-dialog JNPF-dialog_center"
      append-to-body width="600px">
      <el-form ref="dataForm" v-loading="formLoading" :model="dataForm" :rules="dataRule"
        label-width="100px" class="menuForm">
        <el-form-item label="数据字典" prop="dictionaryType">
          <JnpfTreeSelect :options="dicOptions" v-model="dataForm.dictionaryType"
            placeholder="请选择数据字典" lastLevel clearable @change="dictionaryTypeChange"
            @selectChange="selectChange">
          </JnpfTreeSelect>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">{{$t('common.cancelButton')}}</el-button>
        <el-button type="primary" :loading="btnLoading" @click="submit()">
          {{$t('common.confirmButton')}}</el-button>
      </span>
    </el-dialog>
  </div>
</template>
<script>
import TsDesignerTinymce from "./ts-designer-tinymce";
import { getDictionaryTypeSelector } from "@/api/systemData/dictionary"
import pageSize from "./pageSize";
export default {
  name: "tsPrintTemplater",
  components: { TsDesignerTinymce, pageSize },
  props: {
    value: String,
    type: Number,
    treeData: {
      type: Array,
      default: () => []
    },
    pageParam: {
      type: Object,
      default: {}
    }
  },
  data() {
    return {
      node: '',
      btnLoading: false,
      formLoading: false,
      dialogVisible: false,
      dicOptions: [],
      showEdit: false,
      tabs: [],
      initConfig: {},
      menuList: [],
      init: {},
      menuIndex: "",
      content: "",
      activeTab: "",
      richHeight: document.documentElement.clientHeight - 42,
      systemData: [
        { fullName: "打印人员", id: "systemPrinter" },
        { fullName: "打印时间", id: "systemPrintTime" },
        { fullName: "审批内容", id: "systemApprovalContent" },
        { fullName: "图片", id: "img" },
        { fullName: "二维码", id: "qrCode" },
        { fullName: "条形码", id: "barCode" },
      ],
      parameter: [
        { fullName: "千位分隔符(字段或数字,小数位数)", id: "thousands" },
        { fullName: "大写金额(字段或数字)", id: "isAmountChinese" },
        { fullName: "数据字典(字段,字典分类)", id: "jnpfDictionary" },
        { fullName: "日期选择(字段,日期格式)", id: "jnpfDate" }
      ],
      defaultProps: {
        children: "children",
        label: "fullName"
      },
      dataForm: {
        dictionaryType: ''
      },
      dataRule: {
        dictionaryType: [{ required: true, message: "数据字典不能为空", trigger: "click" }]
      },
    };
  },
  watch: {
    pageParam: {
      handler(val) {
        this.getEditConfig(val)
      },
      immediate: true,
      deep: true
    },
    value: {
      handler(val) {
        if (val != this.content) this.content = val
      },
      immediate: true,
      deep: true
    },
    content() {
      this.$emit("input", this.content);
    }
  },
  computed: {
    editor() {
      return this.$refs.createTinymce.editor;
    },
    newData() {
      return this.treeData;
    },
    newSysData() {
      let data = this.systemData;
      if (this.type == 2) {
        data = this.systemData.filter(o => o.id !== "systemApprovalContent");
      }
      return data;
    }
  },
  methods: {
    handleNodeClick(item, node) {
      if (item.children != null && item.children.length > 0) return;
      const tableParent = this.getCurrentParentByTag('table[data-wk-table-tag="table"]');
      if (!tableParent) {
        if (item.id == 'jnpfDictionary') return this.getDictionary(node)
        this.editor.insertContent(this.getSpanNode(item, node));
        this.content = this.editor.getContent({ format: "html" });
      }
    },
    getDictionary(node) {
      this.node = node;
      this.dialogVisible = true
      this.dataForm.dictionaryType = ''
      getDictionaryTypeSelector().then(res => {
        this.dicOptions = res.data.list.filter(o => o.children && o.children.length);
      });
    },
    submit() {
      this.$refs['dataForm'].validate((valid) => {
        if (valid) {
          this.editor.insertContent(this.getSpanNode({ id: 'jnpfDictionary' }, this.node));
          this.content = this.editor.getContent({ format: "html" });
          this.dialogVisible = false
        }
      })
    },
    getEditConfig(e) {
      if (!e) {
        e = {}
        e.mt = 10
        e.mb = 10
        e.ml = 10
        e.mr = 10
        e.width = 210
        e.height = 297
        e.direction = "纵向"
        e.type = '2'
        this.$emit("pageParamInit", e)
      }
      this.$emit("pageParamChange", e)
      let mt = e.mt
      let mb = e.mb
      let ml = e.ml
      let mr = e.mr
      let width = e.width ? e.width : 297
      let height = e.height ? e.height : 'calc(100% - 10px)'
      this.initConfig = {
        menubar: false,
        toolbar_sticky: true,
        statusbar: false,
        content_style: `html {
          background: #fff;
          padding: 20px 0;
          box-sizing: border-box;
          padding-bottom:20px;
        }
        body {
          font-family: simsun, serif, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, 'Open Sans', 'Helvetica Neue', sans-serif;
          width: ${width}mm;
          height: ${height}mm;
          padding: ${mt}mm ${mr}mm ${mb}mm ${ml}mm !important;
          margin: 0 auto !important;
          background: white;
          min-height: 100%;
          box-sizing: border-box;
          box-shadow: 0 2px 12px 0 rgb(0 0 0 / 10%);
          word-break:break-all;
        }
        .wk-tiny-color--customer {
          color: #005AF3;
        }
        .wk-tiny-color--contacts {
          color: #1CB24C;
        }
        .wk-tiny-color--business {
          color: #FF852F;
        }
        .wk-tiny-color--contract {
          color: #FF4A43;
        }
        .wk-tiny-color--receivables {
          color: #FFAE46;
        }
        .wk-tiny-color--product {
          color: #00D0D4;
        }
        .wk-tiny-color--common {
          color: #2362FB;
        }
        p { margin: 5px 0; line-height: 1.5;}`,
        table_advtab: false,
        table_cell_advtab: false,
        table_row_advtab: false,
        toolbar: {
          type: [String, Array],
          default: "code | lineheight | undo redo "
        },
        setup: editor => {
          let _this = this;
          editor.ui.registry.addButton("page", {
            text: `<i class="el-icon-s-order" style="font-size:18px"></i>`,
            tooltip: "纸张大小",
            onAction: function () {
              _this.$refs.pageSize.dialogFormVisible = true;
            }
          });
          editor.on("init", function () {
            editor.execCommand("mceFocus");
          });
          editor.on("keydown", e => {
            this.selectNodes = null;
            /* Backspace key and del key*/
            if (e.keyCode === 8 || e.keyCode == 46) {
              const currentNode = editor.selection.getNode();
              if (
                currentNode.getAttribute("data-wk-table-td-tag") === "value"
              ) {
                e.preventDefault();
                return false;
              } else if (currentNode.nodeName == "SPAN") {
                if (currentNode.hasAttribute("contenteditable")) {
                  e.preventDefault();
                  editor.dom.remove(currentNode);
                  return false;
                } else {
                  const span = this.getCurrentParentByTag(
                    "span[contenteditable]"
                  );
                  if (span) {
                    e.preventDefault();
                    editor.dom.remove(span);
                    return false;
                  }
                  if (currentNode.getAttribute("data-tag") === "thousands" || currentNode.getAttribute("data-tag") === "isAmountChinese") {
                    if (currentNode.innerHTML.length == 1) {
                      e.preventDefault();
                      editor.dom.remove(currentNode);
                      return false;
                    }
                  }
                }
              }
            } else if (e.keyCode == "65" && (e.metaKey || e.ctrlKey)) {
              // 全选
              this.selectNodes = editor.selection.getNode();
              this.setSpanEditAttr(this.selectNodes, true);
            } else {
              // table 里面不允许写内容
              const currentNode = editor.selection.getNode();
              if (
                currentNode.getAttribute("data-wk-table-td-tag") === "value"
              ) {
                // 只允许输入上下左右
                if (e.keyCode !== 37 && e.keyCode !== 38 && e.keyCode !== 39 && e.keyCode !== 40) {
                  e.preventDefault();
                  return false;
                }
              } else if (currentNode.nodeName == "SPAN") {
                if (currentNode.hasAttribute("contenteditable")) {
                  if (currentNode.getAttribute("contenteditable")) {
                    e.preventDefault();
                    currentNode.setAttribute("contenteditable", false);
                    return false;
                  }
                  return false;
                } else {
                  const span = this.getCurrentParentByTag(
                    "span[contenteditable]"
                  );
                  if (span) {
                    if (span.getAttribute("contenteditable")) {
                      e.preventDefault();
                      span.setAttribute("contenteditable", false);
                      return false;
                    }
                    return false;
                  }
                }
              }
              if (e.keyCode == 37 && e.keyCode == 38 && e.keyCode == 39 && e.keyCode == 40) this.cancelSpanEdit();
            }
          });
          editor.on("mousedown", () => {
            this.cancelSpanEdit();
          });
          editor.on("mouseup", e => {
            const selection = editor.selection.getSel();
            if (
              e.target.hasAttribute("contenteditable") &&
              selection.isCollapsed
            ) {
              this.cancelSpanEdit();
            } else {
              if (!selection.isCollapsed) {
                this.selectNodes = editor.selection.getNode();
                this.setSpanEditAttr(this.selectNodes, true);
              } else {
                this.cancelSpanEdit();
              }
            }
          });
        }
      };
    },
    cancelSpanEdit() {
      if (this.selectNodes) {
        setTimeout(() => {
          const selection = this.editor.selection.getSel();
          if (!selection.isCollapsed) {
            this.setSpanEditAttr(this.selectNodes, false);
            this.selectNodes = null;
          }
        }, 300);
      }
    },
    setSpanEditAttr(node, canEdit) {
      if (node && node.hasAttribute('contenteditable')) {
        if (node.getAttribute('contenteditable') != canEdit) node.setAttribute('contenteditable', canEdit)
      }
      if (node && node.children) {
        for (let index = 0; index < node.children.length; index++) {
          const element = node.children[index];
          if (element.children) {
            this.setSpanEditAttr(element, canEdit)
          } else if (element.hasAttribute('contenteditable')) {
            if (node.getAttribute('contenteditable') != canEdit) element.setAttribute('contenteditable', canEdit)
          }
        }
      }
    },
    getCurrentParentByTag(tag) {
      return this.editor.dom.getParent(this.editor.selection.getNode(), tag);
    },
    getSpanNode(item, node) {
      const parent = node.parent.data != null && node.parent.data.id != null ? node.parent.data.id : "null";
      if (item.id == "img" || item.id == "barCode" || item.id == "qrCode") return `&lt;${item.id} width='100' height='100'&gt;&lt;/${item.id}&gt;`;
      if (item.id == 'isAmountChinese') return `<span  data-tag='isAmountChinese'>大写金额(${parent})<span>`
      if (item.id == 'thousands') return `<span  data-tag='thousands'>千位分隔符(${parent},2)<span>`
      if (item.id == 'jnpfDictionary') return `<span  data-tag='jnpfDictionary'>数据字典(${parent},${this.dataForm.dictionaryType})<span>`
      if (item.id == 'jnpfDate') return `<span  data-tag='jnpfDate'>日期选择(${parent},"yyyy-MM-dd")<span>`
      return `<span data-tag="${parent}.${item.id}" class="wk-print-tag-wukong ${this.getSpanColorClass()}" contenteditable="false">{${item.id}}</span>`;
    },
    getSpanColorClass() {
      const color = ["customer", "contacts", "business", "contract", "receivables", "product"].includes(this.activeTab) ? this.activeTab : "common";
      return `wk-tiny-color--${color}`;
    }
  }
};
</script>
<style lang="scss" scoped>
.print-template-detail {
  height: 100%;
  display: flex;

  &__body {
    flex: 1;
    position: relative;
    display: flex;
    overflow: hidden;
  }
}
.system-view-nav {
  width: 300px;
  position: relative;
  background: #fff;
  margin-right: 10px;
  border-radius: 4px;
  overflow: hidden;
  .left-scrollbar {
    height: 100%;
    ::v-deep .el-scrollbar__wrap {
      overflow-x: hidden;
    }
  }
  .JNPF-common-title {
    padding: 0 10px;
    margin-bottom: 10px;
    height: 39px;
    &.sys-title {
      margin-top: 30px;
    }
    h2 {
      font-size: 14px;
      line-height: 39px;
      color: #043254;
      font-weight: bold;
    }
  }
}
.system-view-content {
  flex: 1;
  background: #fff;
  display: flex;
  flex-direction: column;
  overflow-x: auto;
  border-radius: 4px;
  ::v-deep .tox-tinymce {
    border: none;
  }
}
.xr-reminder {
  ::v-deep .reminder-body {
    align-items: stretch;
    width: 100%;
    height: 35px;
    line-height: 35px;
    border-radius: 0;
  }
}
.tox-sidebar-wrap {
  margin-bottom: 20px;
}
</style>
