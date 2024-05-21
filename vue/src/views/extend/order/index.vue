<template>
  <div class="JNPF-common-layout">
    <div class="JNPF-common-layout-center">
      <el-row class="JNPF-common-search-box" :gutter="16">
        <el-form @submit.native.prevent>
          <el-col :span="6">
            <el-form-item label="关键词">
              <el-input v-model="keyword" placeholder="请输入关键词查询" clearable
                @keyup.enter.native="search()" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="订单日期">
              <el-date-picker v-model="pickerVal" type="datetimerange" start-placeholder="开始时间"
                end-placeholder="结束时间" value-format="timestamp" clearable :editable="false"
                :default-time="['00:00:00', '23:59:59']">
              </el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item>
              <el-button type="primary" icon="el-icon-search" @click="search()">
                {{$t('common.search')}}</el-button>
              <el-button icon="el-icon-refresh-right" @click="refresh()">{{$t('common.reset')}}
              </el-button>
            </el-form-item>
          </el-col>
        </el-form>
      </el-row>
      <div class="JNPF-common-layout-main JNPF-flex-main">
        <div class="JNPF-common-head">
          <div>
            <topOpts @add="addHandle()" addText="新建"></topOpts>
          </div>
          <div class="JNPF-common-head-right">
            <el-tooltip effect="dark" :content="$t('common.refresh')" placement="top">
              <el-link icon="icon-ym icon-ym-Refresh JNPF-common-head-icon" :underline="false"
                @click="initData()" />
            </el-tooltip>
          </div>
        </div>
        <JNPF-table v-loading="listLoading" :data="list" @expand-change="expandChange"
          :hasNO="false" @sort-change="sortChange">
          <el-table-column type="expand" width="40">
            <template slot-scope="props">
              <el-tabs v-model="props.row.activeName">
                <el-tab-pane label="订单商品">
                  <el-table :data="props.row.childTable" stripe size='mini' show-summary>
                    <el-table-column prop="goodsName" label="商品名称" />
                    <el-table-column prop="specifications" label="规格型号" width="80" />
                    <el-table-column prop="unit" label="单位" width="80" />
                    <el-table-column prop="qty" label="数量" width="80" />
                    <el-table-column prop="price" label="单价" width="80" />
                    <el-table-column prop="amount" label="金额" width="80" />
                    <el-table-column prop="discount" label="折扣%" width="80" />
                    <el-table-column prop="cess" label="税率%" width="80" />
                    <el-table-column prop="actualPrice" label="实际单价" width="80" />
                    <el-table-column prop="actualAmount" label="实际金额" width="80" />
                  </el-table>
                </el-tab-pane>
                <el-tab-pane label="收款计划">
                  <el-table :data="props.row.childTable1" stripe size='mini' show-summary>
                    <el-table-column prop="receivableDate" label="收款日期"
                      :formatter="jnpf.tableDateFormat" width="140" />
                    <el-table-column prop="receivableRate" label="收款比率%" width="100" />
                    <el-table-column prop="receivableMoney" label="收款金额" width="100" />
                    <el-table-column prop="receivableMode" label="收款方式" width="100" />
                    <el-table-column prop="abstract" label="收款摘要" />
                  </el-table>
                </el-tab-pane>
              </el-tabs>
            </template>
          </el-table-column>
          <el-table-column type="index" width="50" label="序号" align="center" />
          <el-table-column prop="orderCode" label="订单编码" width="150" sortable="custom" />
          <el-table-column prop="orderDate" label="订单日期" width="140" sortable="custom"
            :formatter="jnpf.tableDateFormat" />
          <el-table-column prop="customerName" label="客户名称" width="220" sortable="custom" />
          <el-table-column prop="salesmanName" label="业务人员" width="120" sortable="custom" />
          <el-table-column prop="receivableMoney" label="付款金额" width="100" sortable="custom" />
          <el-table-column prop="creatorUser" label="制单人员" width="120" />
          <el-table-column prop="description" label="订单备注" show-overflow-tooltip />
          <el-table-column prop="currentState" label="状态" width="100" sortable="custom">
            <template slot-scope="scope">
              <el-tag v-if="scope.row.currentState==1">等待审核</el-tag>
              <el-tag type="success" v-else-if="scope.row.currentState==2">审核通过</el-tag>
              <el-tag type="danger" v-else-if="scope.row.currentState==3">审核退回</el-tag>
              <el-tag type="info"
                v-else-if="scope.row.currentState==4 ||scope.row.currentState==7">流程撤回</el-tag>
              <el-tag type="info" v-else-if="scope.row.currentState==5">审核终止</el-tag>
              <el-tag type="warning" v-else>等待提交</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template slot-scope="scope">
              <el-button size="mini" type="text" @click="updateHandle(scope.row)"
                :disabled="[1,2,4,5].indexOf(scope.row.currentState)>-1" v-has="'btn_edit'">编辑
              </el-button>
              <el-button size="mini" type="text" class="JNPF-table-delBtn"
                @click="handleDel(scope.$index,scope.row.id)"
                :disabled="[1,2,3,5].indexOf(scope.row.currentState)>-1" v-has="'btn_remove'">删除
              </el-button>
              <el-button size="mini" type="text" :disabled="!scope.row.currentState"
                @click="toApprovalDetail(scope.row)" v-has="'btn_flowDetail'">详情</el-button>
            </template>
          </el-table-column>
        </JNPF-table>
        <pagination :total="total" :page.sync="listQuery.currentPage"
          :limit.sync="listQuery.pageSize" @pagination="initData" />
      </div>
    </div>
    <FlowBox v-show="formVisible" @close="closeForm" ref="Form" />
    <Detail v-show="detailVisible" ref="detail" @close="detailVisible=false" />
    <SelectFlow ref="selectFlow" v-if="flowListVisible" @selectFlow='selectFlow' />
  </div>
</template>

<script>
import { OrderList, Delete, OrderEntryList, OrderReceivableList } from '@/api/extend/order'
import { getFlowIdByCode, getFlowList } from '@/api/workFlow/FlowEngine'
import Detail from './Detail'
import FlowBox from '@/views/workFlow/components/FlowBox'
import SelectFlow from '@/components/SelectFlowDialog'
export default {
  name: 'extend-order',
  components: { Detail, FlowBox, SelectFlow },
  data() {
    return {
      keyword: '',
      list: [],
      total: 0,
      listLoading: true,
      listQuery: {
        currentPage: 1,
        pageSize: 20,
        sort: 'desc',
        sidx: ''
      },
      formVisible: false,
      detailVisible: false,
      flowTemplateId: '',
      pickerVal: [],
      startTime: '',
      endTime: '',
      flowListVisible: false,
      flowList: []
    }
  },
  created() {
    this.initData()
    this.getFlowIdByCode()
  },
  methods: {
    search() {
      if (this.pickerVal && this.pickerVal.length) {
        this.startTime = this.pickerVal[0]
        this.endTime = this.pickerVal[1]
      } else {
        this.startTime = ''
        this.endTime = ''
      }
      this.listQuery = {
        currentPage: 1,
        pageSize: 20,
        sort: 'desc',
        sidx: ''
      }
      this.initData()
    },
    initData() {
      this.listLoading = true
      let query = {
        ...this.listQuery,
        keyword: this.keyword,
        startTime: this.startTime,
        endTime: this.endTime
      }
      OrderList(query).then(res => {
        this.list = res.data.list
        this.total = res.data.pagination.total
        for (let i = 0; i < this.list.length; i++) {
          this.$set(this.list[i], 'isExpanded', false)
          this.$set(this.list[i], 'activeName', '0')
          this.$set(this.list[i], 'childTable', [])
          this.$set(this.list[i], 'childTable1', [])
        }
        this.listLoading = false
      })
    },
    getFlowIdByCode() {
      getFlowIdByCode('crmOrder').then(res => {
        this.flowTemplateId = res.data
        this.getFlowList()
      })
    },
    getFlowList() {
      getFlowList(this.flowTemplateId, '1').then(res => {
        this.flowList = res.data
      })
    },
    expandChange(rows) {
      rows.isExpanded = !rows.isExpanded
      if (!rows.isExpanded) return
      if (rows.childTable.length || rows.childTable1.length) return
      OrderEntryList(rows.id).then(res => {
        rows.childTable = res.data.list
      })
      OrderReceivableList(rows.id).then(res => {
        rows.childTable1 = res.data.list
      })
    },
    sortChange({ column, prop, order }) {
      this.listQuery.sort = order == 'ascending' ? 'asc' : 'desc'
      this.listQuery.sidx = !order ? '' : prop
      this.initData()
    },
    handleDel(index, id) {
      this.$confirm(this.$t('common.delTip'), this.$t('common.tipTitle'), {
        type: 'warning'
      }).then(() => {
        this.asyncDel(index, id);
      }).catch(() => { });
    },
    asyncDel(index, id) {
      Delete(id).then(res => {
        this.list.splice(index, 1);
        this.$message({
          type: 'success',
          message: res.msg
        });
      })
    },
    addHandle() {
      if (!this.flowList.length) {
        this.$message({
          type: 'error',
          message: '流程不存在'
        });
      } else if (this.flowList.length === 1) {
        this.selectFlow(this.flowList[0])
      } else {
        this.flowListVisible = true
        this.$nextTick(() => {
          this.$refs.selectFlow.init(this.flowList)
        })
      }
    },
    updateHandle(row) {
      let data = {
        id: row.id,
        flowId: row.flowId,
        opType: '-1'
      }
      this.formVisible = true
      this.$nextTick(() => {
        this.$refs.Form.init(data)
      })
    },
    toApprovalDetail(row) {
      let data = {
        id: row.id,
        flowId: row.flowId,
        opType: 0,
      }
      this.formVisible = true
      this.$nextTick(() => {
        this.$refs.Form.init(data)
      })
    },
    toDetail(id) {
      this.detailVisible = true
      this.$nextTick(() => {
        this.$refs.detail.init(id)
      })
    },
    closeForm(isRefresh) {
      this.formVisible = false
      if (isRefresh) this.refresh()
    },
    refresh() {
      this.pickerVal = ''
      this.startTime = ''
      this.endTime = ''
      this.keyword = ''
      this.listQuery = {
        currentPage: 1,
        pageSize: 20,
        sort: 'desc',
        sidx: ''
      }
      this.initData()
    },
    selectFlow(item) {
      let data = {
        id: '',
        flowId: item.id,
        opType: '-1'
      }
      this.flowListVisible = false
      this.formVisible = true
      this.$nextTick(() => {
        this.$refs.Form.init(data)
      })
    }
  }
}
</script>