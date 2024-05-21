<template>
  <el-dialog title="同步门户" :visible.sync="releaseDialogVisible" append-to-body
    class="JNPF-dialog JNPF-dialog_center jnpf-release-modal" lock-scroll width="600px">
    <el-form ref="releaseForm" :model="releaseQuery" :rules="releaseQueryRule"
      label-position="right" label-width="50px" class="dialog-form-main">
      <div class="release-title">生成主页门户</div>
      <div class="release-main">
        <div class="item" :class="{'active':releaseQuery.pcPortal===1}"
          @click="selectToggle('pcPortal')">
          <p class="item-title"><i class="item-icon icon-ym icon-ym-pc"></i>同步Web端门户</p>
          <div class="icon-checked">
            <i class="el-icon-check"></i>
          </div>
        </div>
        <div class="item" :class="{'active':releaseQuery.appPortal===1}"
          @click="selectToggle('appPortal')">
          <p class="item-title"><i class="item-icon icon-ym icon-ym-mobile"></i>同步APP端门户</p>
          <div class="icon-checked">
            <i class="el-icon-check"></i>
          </div>
        </div>
      </div>
      <div class="release-form-main">
        <template v-if="!currRow.pcPortalIsRelease && releaseQuery.pcPortal">
          <el-form-item label="应用" prop="pcPortalSystemId">
            <el-select v-model="pcPortalSystemId" multiple placeholder="选择应用"
              @change="portalChange($event,'pcPortalSystemId')">
              <el-option v-for="item in treeData" :key="item.id" :label="item.fullName"
                :value="item.id">
              </el-option>
            </el-select>
          </el-form-item>
        </template>
        <template v-if="!currRow.appPortalIsRelease">
          <el-form-item label="" prop="portal"
            v-if="(!releaseQuery.pcPortal||currRow.pcPortalIsRelease) && releaseQuery.appPortal">
          </el-form-item>
          <el-form-item label="应用" prop="appPortalSystemId" v-if="releaseQuery.appPortal">
            <el-select v-model="appPortalSystemId" multiple placeholder="选择应用"
              @change="portalChange($event,'appPortalSystemId')">
              <el-option v-for="item in treeData" :key="item.id" :label="item.fullName"
                :value="item.id">
              </el-option>
            </el-select>
          </el-form-item>
        </template>
      </div>
      <div class="release-title">生成应用菜单</div>
      <div class="release-main">
        <div class="item" :class="{'active':releaseQuery.pc===1}" @click="selectToggle('pc')">
          <p class="item-title"><i class="item-icon icon-ym icon-ym-pc"></i>同步Web端菜单</p>
          <div class="icon-checked">
            <i class="el-icon-check"></i>
          </div>
        </div>
        <div class="item" :class="{'active':releaseQuery.app===1}" @click="selectToggle('app')">
          <p class="item-title"><i class="item-icon icon-ym icon-ym-mobile"></i>同步APP端菜单</p>
          <div class="icon-checked">
            <i class="el-icon-check"></i>
          </div>
        </div>
      </div>
      <div class="release-form-main">
        <template v-if="!currRow.pcIsRelease">
          <el-form-item label="上级" prop="pcModuleParentId" v-if="releaseQuery.pc">
            <JnpfTreeSelect v-model="releaseQuery.pcModuleParentId" :options="treeDataSystem"
              placeholder="选择上级菜单" @change="treeSelectChange(arguments,'pcSystemId')" />
          </el-form-item>
        </template>
        <template v-if="!currRow.appIsRelease">
          <el-form-item label="" prop="menu"
            v-if="(!releaseQuery.pc||currRow.pcIsRelease) && releaseQuery.app">
          </el-form-item>
          <el-form-item label="上级" prop="appModuleParentId" v-if="releaseQuery.app">
            <JnpfTreeSelect v-model="releaseQuery.appModuleParentId" :options="appTreeDataSystem"
              placeholder="选择上级菜单" @change="treeSelectChange(arguments,'appSystemId')" />
          </el-form-item>
        </template>
      </div>
    </el-form>
    <span slot="footer" class="dialog-footer">
      <el-button @click="releaseDialogVisible = false">{{$t('common.cancelButton')}}</el-button>
      <el-button type="primary" :loading="releaseBtnLoading" @click="releaseModel">
        {{$t('common.confirmButton')}}</el-button>
    </span>
  </el-dialog>
</template>
<script>
import {
  Release
} from "@/api/system/portal";
import { mapGetters } from "vuex";
import { getSystem } from "@/api/system/system";
import { getMenuSelector } from '@/api/system/menu'
export default {
  props: [],
  name: 'releaseDialog',
  data() {
    return {
      releaseDialogVisible: false,
      releaseBtnLoading: false,
      currRow: {},
      releaseQuery: {
        pc: 1,
        app: 1,
        pcModuleParentId: '',
        appModuleParentId: '',
        pcPortal: 1,
        appPortal: 1,
        pcPortalSystemId: [],
        appPortalSystemId: [],
      },
      releaseQueryRule: {
        pcPortalSystemId: [
          { required: true, message: '应用不能为空', trigger: 'change' }
        ],
        appPortalSystemId: [
          { required: true, message: '应用不能为空', trigger: 'change' }
        ],
        pcModuleParentId: [
          { required: true, message: '上级菜单不能为空', trigger: 'change' }
        ],
        appModuleParentId: [
          { required: true, message: '上级菜单不能为空', trigger: 'change' }
        ],
      },
      treeData: [],
      appTreeData: [],
      pcSystemId: "",
      appSystemId: "",
      treeDataSystem: [],
      appTreeDataSystem: [],
      pcPortalSystemId: [],
      appPortalSystemId: []
    }
  },
  computed: {
    ...mapGetters(['userInfo']),
  },
  methods: {
    releaseModel() {
      this.$refs['releaseForm'].validate(res => {
        if (res) {
          this.$confirm('发布确定后会覆盖当前线上版本且进行门户同步，是否继续？', '提示', {
            type: 'warning'
          }).then(() => {
            setTimeout(() => {
              this.release()
            }, 200)
          })
        }
      }).catch()
    },
    portalChange(val, key) {
      this.releaseQuery[key] = val || []
    },
    openRelease(row) {
      this.releaseBtnLoading = false
      this.currRow = row
      this.releaseDialogVisible = true
      this.pcSystemId = ""
      this.appSystemId = ""
      this.pcPortalSystemId = []
      this.appPortalSystemId = []
      this.releaseQuery = {
        pc: !row.pcIsRelease && !row.appIsRelease && !row.pcPortalIsRelease && !row.appPortalIsRelease ? 1 : row.pcIsRelease,
        app: !row.pcIsRelease && !row.appIsRelease && !row.pcPortalIsRelease && !row.appPortalIsRelease ? 1 : row.appIsRelease,
        pcModuleParentId: '',
        appModuleParentId: '',
        pcPortal: !row.pcIsRelease && !row.appIsRelease && !row.pcPortalIsRelease && !row.appPortalIsRelease ? 1 : row.pcPortalIsRelease,
        appPortal: !row.pcIsRelease && !row.appIsRelease && !row.pcPortalIsRelease && !row.appPortalIsRelease ? 1 : row.appPortalIsRelease,
        pcPortalSystemId: [],
        appPortalSystemId: [],
      }
      this.$nextTick(() => {
        this.$refs['releaseForm'] && this.$refs['releaseForm'].resetFields()
      })
      getSystem({ enableMark: 1, selector: true }).then((res) => {
        this.treeData = res.data.list;
      }).catch(() => { });
      this.getMenuSelector()
      this.getAPPMenuSelector()
    },
    selectToggle(key) {
      this.releaseQuery[key] = this.releaseQuery[key] === 1 ? 0 : 1
      const type = key == 'pc' ? 'pcModuleParentId' : key == 'app' ? 'appModuleParentId' : key == 'pcPortal' ? 'pcPortalSystemId' : 'appPortalSystemId'
      this.$refs['releaseForm'].clearValidate(type)
    },
    treeSelectChange(data, type) {
      const systemId = data[1].systemId
      this[type] = systemId
    },
    // 发布菜单
    release() {
      if (!this.releaseQuery.pc && !this.releaseQuery.app && !this.releaseQuery.pcPortal && !this.releaseQuery.appPortal) return this.$message.error('请至少选择一个同步方式')
      this.releaseBtnLoading = true
      let query = {
        ...this.releaseQuery,
        pcSystemId: this.pcSystemId,
        appSystemId: this.appSystemId,
        pcPortalSystemId: this.releaseQuery.pcPortalSystemId && this.releaseQuery.pcPortalSystemId.join(','),
        appPortalSystemId: this.releaseQuery.appPortalSystemId && this.releaseQuery.appPortalSystemId.join(',')
      }
      Release(this.currRow.id, query).then(res => {
        this.$message({
          type: 'success',
          message: res.msg,
          duration: 1000,
          onClose: () => {
            this.releaseBtnLoading = false
            this.releaseDialogVisible = false
            this.$emit('release', true)
          }
        });

      }).catch(() => { this.releaseBtnLoading = false })
    },
    getMenuSelector() {
      getMenuSelector({ category: 'Web' }, 0).then(res => {
        this.treeDataSystem = res.data.list
      })
    },
    getAPPMenuSelector() {
      getMenuSelector({ category: 'App' }, 0).then(res => {
        this.appTreeDataSystem = res.data.list || []
        for (let index = 0; index < this.appTreeDataSystem.length; index++) {
          const item = this.appTreeDataSystem[index];
          if (item.type == 0) {
            item.disabled = true
          }
        }
      })
    },
  }
}
</script>
<style lang="scss" scoped>
.jnpf-release-modal {
  >>> .el-dialog {
    .el-dialog__body {
      height: 430px;
      padding: 12px 55px;
    }
  }
  .release-main {
    margin-top: 20px;
    display: flex;
    justify-content: space-between;
    .item {
      position: relative;
      width: 215px;
      height: 80px;
      cursor: pointer;
      border: 1px solid #dcdfe6;
      border-radius: 6px;
      text-align: center;
      padding-top: 0;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #606266;
      &.active {
        border-color: #1890ff;
        color: #1890ff;
        box-shadow: 0 0 6px rgba(6, 58, 108, 0.1);
        .item-icon {
          border-color: #1890ff;
        }
        .icon-checked {
          display: block;
        }
      }
      .item-icon {
        display: inline-block;
        width: 28px;
        height: 28px;
        border: 1px solid #606266;
        line-height: 40px;
        margin: 0 8px 0 0 !important;
        font-size: 16px;
        text-align: center;
        border-radius: 50%;
        line-height: 26px !important;
        border-width: 1px !important;
      }
      .item-title {
        font-size: 16px;
        font-weight: 400;
      }
      .icon-checked {
        display: none;
        width: 18px;
        height: 18px;
        border: 18px solid #1890ff;
        border-left: 18px solid transparent;
        border-top: 18px solid transparent;
        border-bottom-right-radius: 4px;
        position: absolute;
        right: 0px;
        bottom: 0px;

        i {
          font-size: 16px;
          position: absolute;
          top: 0;
          left: -2px;
          color: #fff;
        }
      }
    }
  }
  .release-form-main {
    margin-top: 20px;
    display: flex;
    justify-content: space-between;
    .el-form-item {
      width: 215px;
    }
  }
}
.release-title {
  font-size: 16px;
  padding-top: 10px;
  font-weight: bold;
}
</style>