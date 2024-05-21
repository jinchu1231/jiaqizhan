<template>
  <transition name="el-zoom-in-center">
    <div class="JNPF-preview-main">
      <div class="JNPF-common-page-header">
        <el-page-header @back="goBack" :content="'提取变量'" />
        <div class="options">
          <el-button @click="goBack()">{{$t('common.cancelButton')}}</el-button>
        </div>
      </div>
      <div class="main" v-if="!flowVisible">
        <el-row class="JNPF-common-search-box" :gutter="16">
          <el-form @submit.native.prevent>
            <el-col :span="6">
              <el-form-item label="关键词">
                <el-input v-model="keyword" placeholder="请输入关键词查询" clearable />
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item>
                <el-button type="primary" icon="el-icon-search" @click="search()">
                  {{$t('common.search')}}</el-button>
                <el-button icon="el-icon-refresh-right" @click="reset()">{{$t('common.reset')}}
                </el-button>
              </el-form-item>
            </el-col>
          </el-form>
        </el-row>
        <div class="JNPF-common-head">
          <topOpts @add="addOrUpdateHandle()">
            <upload-btn url="/api/system/DataInterfaceVariate/Actions/Import" accept=".ffa"
              @on-success="getVariateList" />
          </topOpts>
          <div class="JNPF-common-head-right">
            <el-tooltip effect="dark" :content="$t('common.refresh')" placement="top">
              <el-link icon="icon-ym icon-ym-Refresh JNPF-common-head-icon" :underline="false"
                @click="getVariateList()" />
            </el-tooltip>
          </div>
        </div>
        <JNPF-table v-loading="listLoading" :data="list">
          <el-table-column prop="fullName" label="变量名" show-overflow-tooltip min-width="200" />
          <el-table-column prop="value" label="变量值" width="200" />
          <el-table-column prop="creatorUser" label="创建人" width="120" />
          <el-table-column prop="creatorTime" label="创建时间" :formatter="jnpf.tableDateFormat"
            width="140" />
          <el-table-column prop="lastModifyTime" label="最后修改时间" :formatter="jnpf.tableDateFormat"
            width="140" />
          <el-table-column label="操作" fixed="right" width="150">
            <template slot-scope="scope">
              <tableOpts @edit="addOrUpdateHandle(scope.row)" @del="del(scope.row)">
                <el-dropdown>
                  <span class="el-dropdown-link">
                    <el-button type="text" size="mini">{{$t('common.moreBtn')}}<i
                        class="el-icon-arrow-down el-icon--right"></i>
                    </el-button>
                  </span>
                  <el-dropdown-menu slot="dropdown">
                    <el-dropdown-item @click.native="handleCopy(scope.row.id)">复制</el-dropdown-item>
                    <el-dropdown-item @click.native="handleExport(scope.row.id)">导出
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </el-dropdown>
              </tableOpts>
            </template>
          </el-table-column>
        </JNPF-table>
      </div>
      <Form v-if="formVisible" ref="Form" @refreshDataList="getVariateList" />
    </div>
  </transition>
</template>
<script>
import Process from '@/components/Process/Preview'
import Form from './Form'
import {
  getVariateList,
  deleteVariate,
  copyVariate,
  exportVariate
} from '@/api/systemData/dataInterface'
export default {
  components: { Process, Form },
  props: [],
  data() {
    return {
      flowVisible: false,
      list: [],
      formVisible: false,
      listLoading: false,
      keyword: '',
      listQuery: {
        keyword: '',
        id: ''
      },
      id: '',
    }
  },
  methods: {
    init(id) {
      this.id = id
      this.getVariateList()
    },
    getVariateList() {
      this.listQuery.id = this.id
      getVariateList(this.listQuery).then(res => {
        this.list = res.data.list || []
        this.listLoading = false
      })
    },
    addOrUpdateHandle(item) {
      this.formVisible = true
      this.$nextTick(() => {
        this.$refs.Form.init(item, this.id)
      })
    },
    del(item) {
      this.$confirm('此操作将永久删除该数据，是否继续？', '系统提示', {
        type: 'warning'
      }).then(() => {
        deleteVariate(item.id).then(res => {
          if (res.code == 200) {
            this.$message({
              type: 'success',
              message: res.msg
            });
            this.getVariateList()
          }
        })
      })
    },
    goBack() {
      if (this.flowVisible) {
        return this.flowVisible = false
      }
      this.$emit('close', true)
    },
    reset() {
      this.keyword = ""
      this.listQuery.keyword = ""
      this.getVariateList()
    },
    search() {
      this.listQuery.keyword = this.keyword
      this.getVariateList()
    },
    handleCopy(id) {
      this.$confirm('您确定要复制该功能模板, 是否继续?', '提示', {
        type: 'warning'
      }).then(() => {
        copyVariate(id).then(res => {
          this.getVariateList()
        });
      })
    },
    handleExport(id) {
      this.$confirm('您确定要导出该变量, 是否继续?', '提示', {
        type: 'warning'
      }).then(() => {
        exportVariate(id).then(res => {
          this.jnpf.downloadFile(res.data.url)
        });
      })
    }
  }
}
</script>
<style lang="scss" scoped>
.main {
  overflow: hidden;
  display: flex;
  flex-direction: column;
  padding: 0 0 10px;
  .JNPF-common-search-box {
    border-bottom: 1px solid #dcdfe6;
  }
  >>> .el-table {
    flex: 1;
    border-top: none;
  }
  >>> .tips-item {
    display: none;
  }
  >>> .flow-container {
    display: inline-block;
    background: #fff;
    width: 100%;
    -webkit-box-sizing: border-box;
    box-sizing: border-box;
    text-align: center;
    overflow: auto;
    margin-top: 30px;
  }
  >>> .tips {
    position: absolute;
    left: 9px;
    top: -1px;
    z-index: 199;
    text-align: left;
  }
  >>> .scale-slider {
    position: fixed;
    right: 10px;
    z-index: 199;
    margin-top: -20px;
  }
}
.flow-text {
  font-size: 18px;
}
</style>
