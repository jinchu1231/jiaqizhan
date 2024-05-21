<template>
  <transition name="el-zoom-in-center">
    <div class="JNPF-preview-main" v-loading="loading">
      <div class="header-wrap">
        <el-page-header @back="goBack" :content="title + '的打印日志'">
        </el-page-header>
      </div>
      <el-divider style="margin: 0px;"></el-divider>
      <div class="main-panel">
        <div class="JNPF-common-layout-center">
          <el-row class="JNPF-common-search-box" :gutter="16">
            <el-form :model="query">
              <el-col :span="6">
                <el-form-item label="关键词">
                  <el-input v-model="query.keyword" placeholder="请输入关键词"
                    @keyup.enter.native="initData()" clearable>
                  </el-input>
                </el-form-item>
              </el-col>
              <el-col :span="6">
                <el-form-item label="打印时间">
                  <el-date-picker v-model="printTimeRange" type="datetimerange" align="right"
                    unlink-panels start-placeholder="开始时间" end-placeholder="结束时间"
                    :picker-options="pickerOptions" :default-time="['00:00:00', '23:59:59']">
                  </el-date-picker>
                </el-form-item>
              </el-col>
              <el-col :span="6">
                <el-form-item>
                  <el-button type="primary" icon="el-icon-search" @click="initData">查询</el-button>
                  <el-button icon="el-icon-refresh-right" @click="reset()">重置</el-button>
                </el-form-item>
              </el-col>
            </el-form>
          </el-row>
          <div class="JNPF-common-layout-main JNPF-flex-main">
            <JNPF-table v-loading="listLoading" :data="list">
              <el-table-column prop="printTitle" label="名称" align="left" />
              <el-table-column prop="printMan" label="打印人" align="left" />
              <el-table-column prop="printTime" label="打印时间" :formatter="jnpf.tableDateFormat"
                align="left" width="140" />
              <el-table-column prop="printNum" label="打印条数" align="left" />
            </JNPF-table>
            <pagination :total="pagination.total" :page.sync="pagination.currentPage"
              :limit.sync="pagination.pageSize" @pagination="initData" />
          </div>
        </div>
      </div>
    </div>
  </transition>
</template>

<script>
import request from "@/utils/request";
export default {
  components: {},
  props: {},
  data() {
    return {
      pickerOptions: {
        shortcuts: [
          {
            text: "最近一周",
            onClick(picker) {
              const end = new Date();
              const start = new Date();
              start.setTime(start.getTime() - 3600 * 1000 * 24 * 7);
              picker.$emit("pick", [start, end]);
            }
          },
          {
            text: "最近一个月",
            onClick(picker) {
              const end = new Date();
              const start = new Date();
              start.setTime(start.getTime() - 3600 * 1000 * 24 * 30);
              picker.$emit("pick", [start, end]);
            }
          },
          {
            text: "最近三个月",
            onClick(picker) {
              const end = new Date();
              const start = new Date();
              start.setTime(start.getTime() - 3600 * 1000 * 24 * 90);
              picker.$emit("pick", [start, end]);
            }
          }
        ]
      },
      list: [],
      pagination: {
        total: 0,
        currentPage: 1,
        pageSize: 20
      },
      loading: false,
      listLoading: true,
      title: "",
      printTimeRange: [],
      query: {
        keyword: "",
        printId: "",
        startTime: undefined,
        endTime: undefined,
        currentPage: 0,
        pageSize: 20
      }
    };
  },
  methods: {
    show(row) {
      this.title = row.fullName;
      this.rowId = row.id;
      this.initData();
      this.loading = false;
    },
    initData() {
      this.listLoading = true;
      if (this.printTimeRange && this.printTimeRange.length > 0) {
        this.query.startTime = +this.printTimeRange[0];
        this.query.endTime = +this.printTimeRange[1];
      } else {
        this.query.startTime = ''
        this.query.endTime = ''
      }
      let url = `/api/system/printLog/${this.rowId}`;
      request({
        url,
        method: "get",
        data: this.query
      }).then(res => {
        this.list = res.data.list;
        this.pagination = res.data.pagination;
        this.listLoading = false;
      });
    },
    reset() {
      this.query.keyword = "";
      this.printTimeRange = [];
      this.query.startTime = undefined;
      this.query.endTime = undefined;
      this.initData();
    },
    goBack() {
      this.$emit("goBack");
    }
  },
  computed: {},
  created() { },
  mounted() { }
};
</script>

<style lang="scss" scoped>
.JNPF-preview-main {
  background-color: transparent;
  overflow: hidden;
}

.JNPF-preview-main::-webkit-scrollbar {
  display: none;
}

.main-panel {
  min-height: 570px;
  height: calc(100% - 10px);
  background-color: #fff;
  overflow: scroll;
}

.main-panel::-webkit-scrollbar {
  display: none;
}

.header-wrap {
  width: 100%;
  padding: 20px 20px;
  font-size: 20px;
  background-color: #fff;
}
>>> .el-divider--horizontal {
  margin: 0px;
}
</style>
