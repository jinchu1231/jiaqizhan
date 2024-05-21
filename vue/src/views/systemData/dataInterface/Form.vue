<template>
  <div class="JNPF-preview-main flow-form-main">
    <div class="JNPF-common-page-header">
      <el-page-header @back="goBack" content="" />
      <el-steps :active="active" finish-status="success" simple class="steps" :key="key">
        <el-step v-for="item in stepList" :key="item" :title="item"></el-step>
      </el-steps>
      <div class="options">
        <el-button :disabled="active <= 0" @click="handlePrev">{{$t('common.prev')}}</el-button>
        <el-button :disabled="active >= stepList.length-1"
          @click="handleNext">{{$t('common.next')}}</el-button>
        <el-button type="primary" :loading="btnLoading" :disabled="active < stepList.length-1"
          @click="handleSubmit()">{{$t('common.confirmButton')}}</el-button>
        <el-button @click="goBack">{{$t('common.cancelButton')}}</el-button>
      </div>
    </div>
    <!-- 基本信息 -->
    <el-form ref="dataForm" :model="dataForm" :rules="dataRule" v-loading="formLoading"
      label-width="100px" v-if="active === 0" :key="key">
      <el-row>
        <el-col :span="14" :offset="5" class="baseInfo mt-20">
          <el-form-item label="名称" prop="fullName">
            <el-input v-model="dataForm.fullName" placeholder="输入名称" maxlength="50" />
          </el-form-item>
          <el-form-item label="编码" prop="enCode">
            <el-input v-model="dataForm.enCode" placeholder="输入编码" maxlength="50" />
          </el-form-item>
          <el-form-item label="分类" prop="category">
            <JnpfTreeSelect v-model="dataForm.category" :options="selectData" placeholder="选择分类"
              clearable />
          </el-form-item>
          <el-form-item label="类型" prop="type">
            <el-radio-group v-model="dataForm.type" @input="onDataTypeChange">
              <el-radio :label="2">静态数据</el-radio>
              <el-radio :label="1">SQL操作</el-radio>
              <el-radio :label="3">API操作</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="动作" prop="action" v-if="dataForm.type===1">
            <el-radio-group v-model="dataForm.action" @change="onMethodChange($event,'sql')">
              <el-radio :label="3">查询</el-radio>
              <el-radio :label="1">增加</el-radio>
              <el-radio :label="2">修改</el-radio>
              <el-radio :label="4">删除</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="排序" prop="sortCode">
            <el-input-number :min="0" :max="999999" v-model="dataForm.sortCode"
              controls-position="right" />
          </el-form-item>
          <el-form-item label="状态" prop="enabledMark">
            <el-switch v-model="dataForm.enabledMark" :active-value="1" :inactive-value="0" />
          </el-form-item>
          <el-form-item label="分页"
            v-if="dataForm.type != 2 && (dataForm.type != 2 && dataForm.action == 3)">
            <el-switch v-model="dataForm.hasPage" :active-value="1" :inactive-value="0"
              :disabled="dataForm.isPostPosition ==1 || hasPageDisabled"
              @change="onHasPageChange" />
            <span class="page-explain" @click="handleShowPageExplain">分页使用说明</span>
          </el-form-item>
          <el-form-item prop="isPostPosition">
            <span slot="label">鉴权
              <el-tooltip content="开启后作为鉴权接口" placement="top">
                <a class="el-icon-question tooltip-question"></a>
              </el-tooltip>
            </span>
            <el-switch v-model="dataForm.isPostPosition" :active-value="1" :inactive-value="0"
              :disabled="!!dataForm.id||dataForm.hasPage==1" @change="onIsPostPositionChange" />
          </el-form-item>
          <el-form-item label="说明" prop="description">
            <el-input v-model="dataForm.description" type="textarea" :rows="3" />
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
    <!-- sql语句 -->
    <div class="config" v-if="getShowSqlBox()">
      <div class="tableData">
        <el-select v-model="dataConfigJson.sqlData.dbLinkId" placeholder="选择数据库" filterable
          @change="handleSelectTable" class="tableDataSelect">
          <el-option-group v-for="group in dbOptions" :key="group.fullName" :label="group.fullName">
            <el-option v-for="item in group.children" :key="item.id" :label="item.fullName"
              :value="item.id">
            </el-option>
          </el-option-group>
        </el-select>
        <div class="box">
          <div class="search-box">
            <el-input placeholder="请输入关键词查询" v-model="keyword" @keyup.enter.native="search"
              clearable class="search-input">
              <el-button slot="append" icon="el-icon-search" @click="search"></el-button>
            </el-input>
          </div>
          <div class="tree-box">
            <el-tree :data="treeData" node-key="index" v-loading="treeLoading" :props="defaultProps"
              lazy :load="loadNode" @node-click="handleNodeClick" :expand-on-click-node="false">
              <span class="custom-tree-node" slot-scope="{ data }">
                <span class="text" v-if="data.isLeaf">
                  {{ data.fieldName?data.field + '(' + data.fieldName + ')':data.field}}
                </span>
                <span class="text" v-else>
                  <i
                    :class="data.type == 1 ? 'icon-ym icon-ym-view' : 'icon-ym icon-ym-generator-tableGrid'" />
                  {{ data.tableName?data.table + '(' + data.tableName + ')':data.table}}
                </span>
              </span>
            </el-tree>
          </div>
        </div>
      </div>
      <div class="detail">
        <div class="middle-pane">
          <div class="right-pane-list">
            <div class="cap">
              <span slot="label">SQL语句
                <el-tooltip content="支持SQL语句&存储过程语句" placement="top">
                  <a class="el-icon-question tooltip-question"></a>
                </el-tooltip>
              </span>
              <div class="cap_sys">
                <el-dropdown>
                  <span class="el-dropdown-link" title="111">
                    系统变量<i class="el-icon-arrow-down el-icon--right"></i>
                  </span>
                  <el-dropdown-menu slot="dropdown">
                    <el-dropdown-item disabled>当前系统变量仅支持内部接口引用</el-dropdown-item>
                    <el-dropdown-item divided></el-dropdown-item>
                    <el-dropdown-item v-for="(item,index) in sysVariableList" :key="index">
                      <div @click="handleSysNodeClick(item.value)">
                        <span>{{ item.value }}</span>
                        <span class="tips">{{ item.tips }}</span>
                      </div>
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </el-dropdown>
              </div>
            </div>
            <div class="list">
              <div class="sql-box" v-if="active===1">
                <SQLEditor v-model="dataConfigJson.sqlData.sql" :options="sqlOptions"
                  ref="SQLEditorRef" />
              </div>
              <div class="sql-box" v-if="dataForm.hasPage && active===2 ">
                <SQLEditor v-model="dataCountJson.sqlData.sql" :options="sqlOptions"
                  ref="dataCountJson" />
              </div>
              <div class="sql-box" v-if="dataForm.hasPage && active===3">
                <SQLEditor v-model="dataEchoJson.sqlData.sql" :options="sqlOptions"
                  ref="dataEchoJson" />
              </div>
            </div>
          </div>
        </div>
      </div>
      <template v-if="dataForm.isPostPosition != 1">
        <div class="right-pane">
          <div class="right-pane-list">
            <div class="cap">
              <span>接口参数
                <el-tooltip content="接收方式:Body/json" placement="top">
                  <a class="el-icon-question tooltip-question"></a>
                </el-tooltip>
              </span>
            </div>
            <RightTable :data="parameterJson" ref="RightTable" tableType="1"
              @addOrUpdateHandle="addOrUpdateHandle($event,'1')"
              @removeParameter="removeParameter($event,'1')" @handleItemClick="handleItemClick" />
          </div>
          <div class="right-pane-list">
            <div class="cap">
              <span>字段列表
                <el-tooltip content="设置接口返回字段" placement="top">
                  <a class="el-icon-question tooltip-question"></a>
                </el-tooltip>
              </span>
            </div>
            <RightTable :data="fieldJson" ref="RightTable" tableType="2"
              @addOrUpdateHandle="addOrUpdateHandle($event,'2')"
              @removeParameter="removeParameter($event,'2')" />
          </div>
        </div>
      </template>
    </div>
    <!-- 静态数据数据处理 -->
    <div class="config" v-if="active === 1 && dataForm.type === 2">
      <div class="detail">
        <div class="middle-pane">
          <div class="right-pane-list">
            <div class="list">
              <el-form ref="dataForm" :model="dataConfigJson" :rules="dataRule" label-width="100px">
                <el-form-item label-width="0" prop="staticData">
                  <div class="sql-box">
                    <JSONEditor v-model="dataConfigJson.staticData" :options="jsonOptions"
                      ref="JSONEditorRef" />
                  </div>
                </el-form-item>
              </el-form>
            </div>
          </div>
        </div>
      </div>
      <div class="right-pane">
        <div class="right-pane-list">
          <div class="cap">
            <span>字段列表
              <el-tooltip content="设置接口返回字段" placement="top">
                <a class="el-icon-question tooltip-question"></a>
              </el-tooltip>
            </span>
          </div>
          <RightTable :data="fieldJson" ref="RightTable" tableType="2"
            @addOrUpdateHandle="addOrUpdateHandle($event,'2')"
            @removeParameter="removeParameter($event,'2')" />
        </div>
      </div>
    </div>
    <!-- api操作 -->
    <div class="config" v-if="getShowApiBox()">
      <div class="detail api_detail">
        <div class="middle-pane">
          <div class="right-pane-list">
            <div class="requestMethod">
              <el-form ref="dataForm" :model="apiData" :rules="dataRule">
                <el-form-item prop="url" label="">
                  <el-input placeholder="请输入接口地址" v-model="apiData.url">
                    <el-select v-model="apiData.method" slot="prepend" placeholder="请选择">
                      <el-option label="GET" :value="1"></el-option>
                      <el-option label="POST" :value="2"></el-option>
                    </el-select>
                  </el-input>
                </el-form-item>
              </el-form>
            </div>
            <p class="textBlock">请求参数</p>
            <div class="tabsBlock">
              <el-tabs v-model="activeKey" @tab-click="tabsHandleClick">
                <el-tab-pane label="Header" name="0"></el-tab-pane>
                <el-tab-pane label="Query" name="1"></el-tab-pane>
                <el-tab-pane label="Body" name="2"></el-tab-pane>
              </el-tabs>
            </div>
            <div class="radio-group-block" v-if="activeKey == 2">
              <el-radio-group v-model="apiData.bodyType" @change="onBtnTypeChange" :key="key2">
                <el-radio :label="0">none</el-radio>
                <el-radio :label="1">form-data</el-radio>
                <el-radio :label="2">x-www-form-urlencoded</el-radio>
                <el-radio :label="3">json</el-radio>
                <el-radio :label="4">xml</el-radio>
              </el-radio-group>
            </div>
            <template v-if="getShowTableBox()">
              <CommonTable v-if="getShowTableBox()" :data="getList" ref="CommonTable"
                @addHandle="addHandle" type="1" :sourceData="sourceData"
                :isPostPosition="dataForm.isPostPosition" :sourceOptions="sourceOptions"
                :parameterJson="parameterJson" @removeCommonTable="removeCommonTable"
                :pagingParameters="pagingParameters" style="width: 100%;" />
            </template>
            <p v-show="activeKey == 2 && apiData.bodyType == 0" class="body_txt">
              该请求没有Body主体</p>
            <div class="json-block"
              v-if="activeKey == 2 && (apiData.bodyType == 3 || apiData.bodyType == 4)">
              <div class="json-block-inner">
                <div class="inner-hd" v-if="!dataForm.isPostPosition">
                  <p>{{ jsonTxt }}</p>
                  <el-popover placement="bottom" width="260" trigger="hover">
                    <span slot="reference" class="el-dropdown-link cursor_pointer">
                      插入参数<i class="el-icon-arrow-down el-icon--right"></i>
                    </span>
                    <div>
                      <el-input placeholder="输入关键字" v-model="filterText"
                        suffix-icon="el-icon-search" clearable class="input-with-select"
                        style="margin-bottom: 10px;">
                      </el-input>
                      <div style="overflow-y: scroll;max-height: 300px;">
                        <el-tree :data="getTreeData" :props="props" :expand-on-click-node="true"
                          :default-expand-all="true" :filter-node-method="filterNode"
                          @node-click="bodyTreeNodeClick" ref="bodyTree">
                          <span class="custom-tree-node" slot-scope="{ node, data }">
                            <i :class="data.icon"></i>
                            <span class="text">{{node.label}}</span>
                          </span>
                        </el-tree>
                      </div>
                    </div>
                  </el-popover>
                </div>
                <JSONEditor v-if="apiData.bodyType == 3" v-model="apiData.bodyJson"
                  :options="jsonOptions" ref="JSONEditorRef" class="JSONEditor" />
                <JSONEditor v-if="apiData.bodyType == 4" v-model="apiData.bodyXml"
                  :options="jsonOptions" ref="JSONEditorRef" class="JSONEditor" :key="key2" />
              </div>
            </div>
          </div>
        </div>
      </div>
      <div class="right-pane" v-if="!dataForm.isPostPosition">
        <div class="right-pane-list">
          <div class="cap">
            <span>接口参数
              <el-tooltip content="接收方式:Body/json" placement="top">
                <a class="el-icon-question tooltip-question"></a>
              </el-tooltip>
            </span>
          </div>
          <RightTable :data="parameterJson" ref="RightTable" tableType="1"
            @addOrUpdateHandle="addOrUpdateHandle($event,'1')"
            @removeParameter="removeParameter($event,'1')" @handleItemClick="handleItemClick" />
        </div>
        <div class="right-pane-list">
          <div class="cap">
            <span>字段列表
              <el-tooltip content="设置接口返回字段" placement="top">
                <a class="el-icon-question tooltip-question"></a>
              </el-tooltip>
            </span>
          </div>
          <RightTable :data="fieldJson" ref="RightTable" tableType="2"
            @addOrUpdateHandle="addOrUpdateHandle($event,'2')"
            @removeParameter="removeParameter($event,'2')" />
        </div>
      </div>
    </div>
    <!-- api\sql数据处理 -->
    <div class="jsStaticData" v-if="getShowCodeBox()">
      <div class="json-box">
        <JNPFCodeEditor v-model="dataJsJson" :options="jsOptions" ref="CodeEditor"
          v-if="getIsLastStep()" />
        <JNPFCodeEditor v-model="dataExceptionJson" :options="jsOptions" ref="CodeEditor" />
      </div>
      <div class="jsTips">
        <p>1、支持JavaScript的脚本</p>
        <p>2、小程序不支持在线JS脚本</p>
      </div>
    </div>
    <FieldForm v-show="fieldFormVisible" ref="fieldForm" @addParameter="addParameter" />
    <page-explain v-if="pageExplainVisible" ref="pageExplain" />
  </div>
</template>
<script>
import { getDataInterfaceInfo, createDataInterface, updateDataInterface, getSelector } from '@/api/systemData/dataInterface'
import { getDataSourceListAll } from '@/api/systemData/dataSource'
import { DataModelListAll, DataModelFieldList } from '@/api/systemData/dataModel'
import SQLEditor from '@/components/JNPFEditor/monaco'
import JSONEditor from '@/components/JNPFEditor/monaco'
import FieldForm from './FieldForm'
import FormScript from './FormScript'
import PageExplain from './PageExplain'
import CommonTable from './components/CommonTable'
import { deepClone } from '@/utils'
import RightTable from './components/RightTable'
import JNPFCodeEditor from '@/components/JNPFEditor/monaco'
const defaultDataJsJson = '(data) => {\r\n    // 处理数据逻辑\r\n\r\n    // 返回所需的数据\r\n    return data\r\n}';
const defaultDataExceptionJson = '(data) => {\r\n    // 返回true表示接口验证成功！\r\n\r\n    // 返回flase表示接口验证失败！\r\n    return data\r\n}';
const defaultJson = {
  staticData: '',
  sqlData: {
    dbLinkId: '0',
    sql: '',
  },
  apiData: {
    method: 1,
    url: '',
    header: [],
    query: [],
    body: '',
    bodyType: 0,
    bodyJson: '',
    bodyXml: '',
  }
}

export default {
  components: {
    SQLEditor,
    JSONEditor,
    FieldForm,
    FormScript,
    PageExplain,
    JNPFCodeEditor,
    RightTable,
    CommonTable
  },
  data() {
    return {
      apiData: defaultJson.apiData,
      dataConfigJson: deepClone(defaultJson),
      dataCountJson: deepClone(defaultJson),
      dataEchoJson: deepClone(defaultJson),
      dataJsJson: defaultDataJsJson,
      dataExceptionJson: defaultDataExceptionJson,
      fieldJson: [],
      parameterJson: [],
      activeKey: '0',
      active: 0,
      treeLoading: false,
      formLoading: false,
      btnLoading: false,
      fieldFormVisible: false,
      pageExplainVisible: false,
      selectData: [],
      sqlOptions: { language: 'sql' },
      jsonOptions: { language: 'json' },
      jsOptions: { language: 'javascript' },
      props: {
        label: 'fullName',
        children: 'children',
        value: 'id'
      },
      commonTableData: [],
      defHasPage: 0,
      dataForm: {
        fullName: '',
        enCode: '',
        category: '',
        type: 2,
        hasPage: 0,
        sortCode: 0,
        enabledMark: 1,
        description: '',
        action: 3,
        isPostPosition: 0
      },
      sourceData: [],
      method: 1,
      apiRequestMethod: '6',
      dbOptions: [],
      treeData: [],
      defaultProps: {
        children: 'children',
        label: 'fullName',
        isLeaf: 'isLeaf'
      },
      dataRule: {
        fullName: [{ required: true, message: '名称不能为空', trigger: 'blur' }],
        enCode: [{ required: true, message: '编码不能为空', trigger: 'blur' }],
        category: [{ required: true, message: '请选择分类', trigger: 'blur' }],
        url: [{ required: true, message: '请填写接口路径', trigger: 'blur' }]
      },
      key: +new Date(),
      key2: +new Date(),
      sqlType: 0,
      keyword: "",
      filterText: '',
      pagingParameters: []
    }
  },
  watch: {
    filterText(val) {
      this.$nextTick(() => {
        this.$refs.bodyTree.filter(val)
      })
    },
    active: {
      handler(newVal, oldVal) {
        if (this.dataForm.type !== 3) return;
        if (oldVal == 1) this.dataConfigJson.apiData = JSON.parse(JSON.stringify(this.apiData));
        if (oldVal == 2 && this.dataForm.hasPage) this.dataEchoJson.apiData = JSON.parse(JSON.stringify(this.apiData));
        if (newVal === 1) {
          this.apiData = JSON.parse(JSON.stringify(this.dataConfigJson.apiData));
          if (this.apiData.bodyType != 3 && this.apiData.bodyType != 4) {
            this.$set(this.apiData, 'body', JSON.parse(this.apiData.body))
          } else {
            if (this.apiData.bodyType == 3) {
              this.$set(this.apiData, 'bodyJson', this.dataConfigJson.apiData.body)
            } else {
              this.$set(this.apiData, 'bodyXml', this.dataEchoJson.apiData.body)
            }
          }
        }
        if (newVal === 2 && this.dataForm.hasPage) this.apiData = JSON.parse(JSON.stringify(this.dataEchoJson.apiData));
      },
      deep: true
    }
  },
  computed: {
    getList() {
      if (this.activeKey === '0') this.list = this.apiData.header || [];
      if (this.activeKey === '1') this.list = this.apiData.query || [];
      if (this.activeKey === '2') this.list = this.apiData.body || [];
      return this.list;
    },
    hasPageDisabled() {
      if (this.dataForm.id && this.dataForm.isPostPosition == 1) return true
      if (this.dataForm.id && this.defHasPage == 1) return true
    },
    jsonTxt() {
      return this.apiData.bodyType == 3 ? 'JSON Body' : 'XML Body'
    },
    sourceOptions() {
      if (this.dataForm.hasPage && this.active == 1) return [{ label: '接口参数', value: 1 }, { label: '分页参数', value: 4 }, { label: '变量', value: 2 }, { label: '自定义', value: 3 }]
      if (this.dataForm.hasPage && this.active == 2) return [{ label: '接口参数', value: 1 }, { label: '回显参数', value: 5 }, { label: '变量', value: 2 }, { label: '自定义', value: 3 }]
      return [{ label: '接口参数', value: 1 }, { label: '变量', value: 2 }, { label: '自定义', value: 3 }]
    },
    stepList() {
      this.key = +new Date()
      this.pagingParameters = []
      this.active == 1 ? this.pagingParameters = [
        { value: 'currentPage', label: 'currentPage' },
        { value: 'pageSize', value: 'pageSize' },
        { value: 'keyword', value: 'keyword' },
      ] : this.pagingParameters = [
        { value: 'showKey', value: 'showKey' },
        { value: 'showValue', value: 'showValue' },
      ]
      let base = ['基本信息', '数据配置'];
      if (this.dataForm.type === 2) return base;
      if (this.dataForm.type === 1 && this.dataForm.hasPage === 1) base = [...base, '数量统计', '数据回显'];
      if (this.dataForm.type === 3 && this.dataForm.hasPage === 0) base = [...base, '异常验证'];
      if (this.dataForm.type === 3 && this.dataForm.hasPage === 1) base = [...base, '数据回显', '异常验证'];
      if (this.dataForm.type === 3 && this.dataForm.isPostPosition === 1) return base;
      return [...base, '数据处理'];
    },
    sysVariableList() {
      const list = [
        { value: '@lotSnowID', tips: '批量生成不同雪花ID' },
        { value: '@snowFlakeID', tips: "系统生成雪花ID" },
        { value: '@currentUser', tips: "当前用户" },
        { value: '@currentUsersAndSubordinates', tips: "当前用户及下属" },
        { value: '@organization', tips: "当前组织" },
        { value: '@currentOrganizationAndSuborganization', tips: "当前组织及子组织" },
        { value: '@chargeorganization', tips: "当前分管组织" }
      ]
      const dataConfigList = [
        { value: '@offsetSize', tips: "开始数据条数" },
        { value: '@pageSize', tips: "返回数据条数" },
      ]
      const dataEchoList = [
        { value: '@showKey', tips: "回显字段查询key" },
        { value: '@showValue', tips: "回显字段值" },
      ]
      const keyword = { value: '@keyword', tips: "关键词搜索" }
      if (!this.dataForm.hasPage) return list
      if (this.active === 2) return [...list, keyword]
      if (this.active === 3) return [...list, ...dataEchoList]
      return [...list, ...dataConfigList, keyword]
    },
    getTreeData() {
      const parameterJson = this.parameterJson.map(o => ({ ...o, fullName: o.field, parameter: 1, type: 1 }));
      let tree = [
        { id: 1, fullName: '接口参数', hasChildren: true, children: parameterJson },
        { id: 2, fullName: '变量', hasChildren: true, children: this.sourceData },
      ];
      tree = tree.filter(o => o.children && o.children.length);
      if (!this.dataForm.hasPage) return tree;
      const page = {
        id: 3,
        fullName: '分页参数',
        hasChildren: true,
        children: [
          { fullName: 'currentPage', id: 'currentPage', parameter: 1, type: 1 },
          { fullName: 'pageSize', id: 'pageSize', parameter: 1, type: 1 },
          { fullName: 'keyword', id: 'keyword', parameter: 1, type: 1 },
        ],
      };
      const echo = {
        id: 2,
        fullName: '回显参数',
        hasChildren: true,
        children: [
          { fullName: 'showKey', id: 'showKey', parameter: 1, type: 1 },
          { fullName: 'showValue', id: 'showValue', parameter: 1, type: 1 },
        ],
      };
      return [...tree, this.active == 1 ? page : echo];
    },
  },
  methods: {
    onIsPostPositionChange(e) {
      this.dataForm.isPostPosition = e;
      if (this.dataForm.isPostPosition && this.dataForm.hasPage) this.dataForm.hasPage = 0
    },
    onHasPageChange(e) {
      this.dataForm.hasPage = e;
      if (this.dataForm.isPostPosition && this.dataForm.hasPage) this.dataForm.isPostPosition = 0
    },
    getShowApiBox() {
      if (this.active == 1 && this.dataForm.type == 3) return true;
      if (this.active == 2 && this.dataForm.type == 3 && this.dataForm.hasPage) return true;
      return false;
    },
    getShowCodeBox() {
      if (this.getIsLastStep() && this.dataForm.type != 2) return true;
      if (this.dataForm.type === 3) {
        if (this.active === this.stepList.length - 1 && this.dataForm.isPostPosition == 1) return true;
        if (this.active === this.stepList.length - 2 && this.dataForm.isPostPosition !== 1) return true;
      }
      return false;
    },
    getIsLastStep() {
      return this.active === this.stepList.length - 1 && !(this.dataForm.type === 3 && this.dataForm.isPostPosition == 1) ? true : false;
    },
    filterNode(value, data) {
      if (!value) return true;
      return data.fullName.indexOf(value) !== -1;
    },
    init(id, category) {
      this.filterText = ''
      Object.assign(this.$data, this.$options.data())
      this.active = 0
      this.dataForm.id = id || ''
      this.formLoading = true
      this.$nextTick(() => {
        this.$refs['dataForm'].resetFields()
        // 获取分类
        this.$store.dispatch('base/getDictionaryData', { sort: 'DataInterfaceType' }).then((res) => {
          this.selectData = res
        })
        // 获取数据库
        getDataSourceListAll().then(res => {
          const list = res.data.list || []
          this.dbOptions = list.filter(o => o.children && o.children.length)
          if (this.dataForm.id) {
            this.formLoading = false
            this.getFormData()
          } else {
            this.dataForm.category = category
            this.formLoading = false
            this.getTableList()
          }
          this.getSelector()
        })
      })
    },
    getFormData() {
      getDataInterfaceInfo(this.dataForm.id).then(res => {
        this.dataForm = res.data;
        this.defHasPage = this.dataForm.hasPage
        if (!this.dataForm.action) this.dataForm.action = 3
        this.dataConfigJson = res.data.dataConfigJson ? JSON.parse(res.data.dataConfigJson) : JSON.parse(JSON.stringify(defaultJson));
        this.dataCountJson = res.data.dataCountJson ? JSON.parse(res.data.dataCountJson) : JSON.parse(JSON.stringify(defaultJson));
        this.dataEchoJson = res.data.dataEchoJson ? JSON.parse(res.data.dataEchoJson) : JSON.parse(JSON.stringify(defaultJson));
        this.parameterJson = res.data.parameterJson ? JSON.parse(res.data.parameterJson) : [];
        this.fieldJson = res.data.fieldJson ? JSON.parse(res.data.fieldJson) : [];
        this.dataJsJson = res.data.dataJsJson;
        this.dataExceptionJson = res.data.dataExceptionJson;
        this.getTableList();
        this.formLoading = false
      })
    },
    getTableList() {
      this.treeLoading = true
      const query = {
        linkId: this.dataConfigJson.sqlData.dbLinkId,
        keyword: this.keyword,
        pageSize: 1000000,
      };
      DataModelListAll(query).then(res => {
        this.treeLoading = false;
        this.treeData = res.data.list.map(o => ({
          ...o,
          fullName: o.tableName ? o.table + '(' + o.tableName + ')' : o.table,
          isLeaf: false,
          id: o.table,
          icon: o.type == 1 ? 'icon-ym icon-ym-view' : 'icon-ym icon-ym-generator-tableGrid',
        }));
      }).catch(() => {
        this.treeLoading = false;
        this.treeData = []
      })
    },
    handleApiData(apiData) {
      if (apiData.bodyXml) apiData.body = apiData.bodyXml;
      if (apiData.bodyJson) apiData.body = apiData.bodyJson;
      if (Array.isArray(apiData.body)) apiData.body = JSON.stringify(apiData.body)
    },
    handleSubmit() {
      if (!this.dataConfigJson.staticData && this.dataForm.type == 2) return this.$message.warning('请输入静态数据')
      this.btnLoading = true
      if (this.dataForm.type === 3) {
        this.handleApiData(this.dataConfigJson.apiData);
        if (this.dataForm.hasPage === 1) {
          this.handleApiData(this.dataEchoJson.apiData);
        }
      }
      const query = {
        ...this.dataForm,
        id: this.dataForm.id,
        dataConfigJson: JSON.stringify(this.dataConfigJson),
        dataCountJson: JSON.stringify(this.dataCountJson),
        dataEchoJson: JSON.stringify(this.dataEchoJson),
        dataJsJson: this.dataJsJson,
        dataExceptionJson: this.dataExceptionJson,
        fieldJson: JSON.stringify(this.fieldJson),
        parameterJson: JSON.stringify(this.parameterJson),
      };
      const formMethod = this.dataForm.id ? updateDataInterface : createDataInterface
      formMethod(query).then(res => {
        this.$message({
          message: res.msg,
          type: 'success',
          duration: 1500,
          onClose: () => {
            this.btnLoading = false
            this.$emit('close', true)
          }
        })
      }).catch(() => {
        this.btnLoading = false
      })
    },
    handlePrev() {
      this.active -= 1;
      this.activeKey = '0';
      if (this.dataForm.type == 1) this.setSqlData()
    },
    handleNext() {
      if (this.active == 0) {
        this.$refs['dataForm'].validate(valid => {
          if (valid) this.handleNextFun();
        })
      } else {
        if (this.dataForm.type === 1) {
          if (!this.dataConfigJson.sqlData.sql && this.active == 1) return this.$message.warning('请输入SQL语句')
          if (!this.dataCountJson.sqlData.sql && this.active == 2) return this.$message.warning('请输入SQL语句')
          if (!this.dataEchoJson.sqlData.sql && this.active == 3) return this.$message.warning('请输入SQL语句')
          this.handleNextFun();
        } else if (this.dataForm.type == 3) {
          if (this.active == 1 || (this.active == 2 && this.dataForm.hasPage)) {
            this.$refs['dataForm'].validate(valid => {
              if (valid) this.handleValidate()
            })
          } else {
            this.handleNextFun()
          }
        }
      }
    },
    handleNextFun() {
      this.active += 1;
      // SQL操作
      if (this.dataForm.type === 1 && this.getIsLastStep()) this.setDataJsJson();
      // API操作
      if (this.dataForm.type === 3) {
        if (this.active === 1 || (this.active === 2 && this.dataForm.hasPage)) this.activeKey = '0';
        if (this.getIsLastStep()) this.setDataJsJson();
        if (this.active === this.stepList.length - 2) this.setDataExceptionJson();
      }
      if (this.dataForm.type == 1) this.setSqlData()
    },
    handleValidate() {
      const headerList = this.apiData.header || [];
      const queryList = this.apiData.query || [];
      for (let i = 0; i < headerList.length; i++) {
        if (!headerList[i].field) return this.$message.warning('参数名称不能为空');
      }
      for (let i = 0; i < queryList.length; i++) {
        if (!queryList[i].field) return this.$message.warning('参数名称不能为空');
      }
      if ((this.apiData.bodyType === 1 || this.apiData.bodyType === 2) && Array.isArray(this.apiData.body) && this.apiData.body.length) {
        for (let i = 0; i < this.apiData.body.length; i++) {
          if (!this.apiData.body[i].field) return this.$message.warning('参数名称不能为空');
        }
      }
      this.handleNextFun();
    },
    setDataExceptionJson() {
      if (!this.dataExceptionJson) this.dataExceptionJson = defaultDataExceptionJson;
    },
    setDataJsJson() {
      if (!this.dataJsJson) this.dataJsJson = deepClone(defaultDataJsJson);
    },
    getShowTableBox() {
      if (this.activeKey == '0' || this.activeKey == '1') return true;
      if (this.activeKey == '2' && (this.apiData.bodyType == 1 || this.apiData.bodyType == 2)) {
        this.$set(this.apiData, 'body', '')
        return true;
      }
      return false;
    },
    /**左侧事件处理 start */
    handleSelectTable(val) {
      this.dataConfigJson.sqlData.dbLinkId = val
      this.dataEchoJson.sqlData.dbLinkId = val
      this.dataCountJson.sqlData.dbLinkId = val
      this.getTableList();
    },
    handleNodeClick(data) {
      const content = data.isLeaf ? data.field : data.table
      if (this.active == 1) this.$refs.SQLEditorRef && this.$refs.SQLEditorRef.insert(content)
      if (this.active == 2 || this.active == 3) this.$refs[this.active == 2 ? 'dataCountJson' : 'dataEchoJson'] && this.$refs[this.active == 2 ? 'dataCountJson' : 'dataEchoJson'].insert(content)
    },
    /**左侧事件处理 end */
    getSelector() {
      getSelector().then(res => {
        this.sourceData = res.data || []
      })
    },
    setSqlData() {
      this.$nextTick(() => {
        if (this.active == 1) this.$refs.SQLEditorRef && this.$refs.SQLEditorRef.changeEditor({ value: this.dataConfigJson.sqlData.sql, options: this.sqlOptions })
        if (this.dataForm.hasPage && (this.active === 2 || this.active === 3)) {
          const key = this.active === 2 ? 'dataCountJson' : 'dataEchoJson'
          const data = this[key].sqlData.sql || ''
          this.$refs[key] && this.$refs[key].changeEditor({ value: data, options: this.sqlOptions })
        }
      })
    },
    removeCommonTable(index) {
      this.$confirm('此操作删除该参数, 是否继续?', '提示', {
        type: 'warning'
      }).then(() => {
        let ref = this.activeKey == 0 ? 'header' : this.activeKey == 1 ? 'query' : 'body'
        this.apiData[ref].splice(index, 1)
      }).catch(() => { });
    },
    bodyTreeNodeClick(data) {
      if (data.hasChildren || data.type != 1) return
      const val = data.field || data.fullName;
      const insertVal = data.parameter == 1 ? '{' + val + '}' : '{@' + val + '}';
      this.$refs.JSONEditorRef.insert(insertVal, true)
    },
    handleItemClick(item) {
      if (!item.field) return
      if (this.dataForm.type == 1) {
        if (this.active == 1) this.$refs.SQLEditorRef.insert('{' + item.field + '}')
        if (this.active === 2 || this.active === 3) {
          const key = this.active === 2 ? 'dataCountJson' : 'dataEchoJson'
          this.$refs[key].insert('{' + item.field + '}')
        }
      }
      if (this.dataForm.type === 3) this.$refs.JSONEditorRef.insert('{' + item.field + '}')
    },
    onBtnTypeChange(val) {
      this.filterText = ""
      this.apiData.bodyType = val
      this.apiData.bodyJson = '';
      this.apiData.bodyXml = '';
      this.apiData.body = [];
      this.key2 = +new Date()
    },
    tabsHandleClick(e) {
      this.activeKey = e.index
      this.addTableName = this.activeKey == '1' ? '添加Query参数' : this.activeKey == '2' ? '添加Body参数' : '添加Header参数'
      let data = {
        addTableName: this.addTableName,
        activeKey: this.activeKey
      }
      this.$nextTick(() => {
        this.$refs.CommonTable && this.$refs.CommonTable.changTable(data)
      })
    },
    onDataTypeChange(val) {
      this.dataForm.hasPage = 0
      this.dataForm.action = 3
      this.dataForm.type = val
    },
    onMethodChange(val) {
      if (val != 3) this.dataForm.hasPage = 0
      this.dataForm.action = val
    },
    loadNode(node, resolve) {
      if (node.level === 0) {
        return resolve(this.treeData)
      }
      DataModelFieldList(this.dataConfigJson.sqlData.dbLinkId, node.data.table).then(res => {
        resolve(res.data.list.map(o => ({ ...o, isLeaf: true })))
      })
    },
    goBack() {
      this.$emit('close')
    },
    /**右侧表格操作 start */
    addOrUpdateHandle(item, type) {
      this.sqlType = type || 0
      const data = item ? JSON.parse(JSON.stringify(item)) : null
      const parameters = type == 1 ? this.parameterJson : this.fieldJson
      this.fieldFormVisible = true
      this.$nextTick(() => {
        this.$refs.fieldForm.init(data, parameters, type)
      })
    },
    addParameter(type, item, tableType) {
      let targetJson = tableType == 1 ? this.parameterJson : this.fieldJson;
      if (type === 'add') return targetJson.push(deepClone(item));
      let index = targetJson.findIndex(obj => obj.id === item.id);
      if (index !== -1) this.$set(targetJson, index, deepClone(item));
    },
    removeParameter(index, type) {
      this.$confirm('此操作删除该参数, 是否继续?', '提示', {
        type: 'warning'
      }).then(() => {
        this[type == 1 ? 'parameterJson' : 'fieldJson'].splice(index, 1)
      }).catch(() => { });
    },
    /**右侧表格操作 end */
    handleShowPageExplain() {
      this.pageExplainVisible = true
      this.$nextTick(() => {
        this.$refs.pageExplain.init()
      })
    },
    getShowSqlBox() {
      if (this.dataForm.type !== 1) return false
      if (this.active === 1) return true
      if (this.dataForm.hasPage && (this.active === 2 || this.active === 3)) return true
    },
    search() {
      this.getTableList();
    },
    addHandle() {
      this.list.push({ field: '', defaultValue: '', dataType: 'varchar', source: this.dataForm.isPostPosition ? 3 : 1 });
    },
    handleSysNodeClick(data) {
      this.$refs.SQLEditorRef && this.$refs.SQLEditorRef.insert(data)
    },
  }
}
</script>
<style lang="scss" scoped>
.tips {
  float: right;
  color: #8492a6;
  padding-left: 10px;
}
.jsTips {
  -ms-flex-negative: 0;
  flex-shrink: 0;
  padding: 8px 16px;
  background-color: #ecf8ff;
  border-radius: 4px;
  border-left: 5px solid #50bfff;
  font-size: 14px;
  line-height: 24px;
  color: #5e6d82;
}
.jsStaticData {
  flex: 1;
  display: flex;
  overflow: hidden;
  flex-direction: column;
  padding: 10px;
  .json-box {
    flex: 1;
  }
}
.monaco-container {
  height: 100%;
  width: 100%;
  overflow: hidden;
}
.steps {
  width: unset;
  overflow: auto;
  padding: 6px 20px;
  background: #fff;
  justify-items: flex-start;
  .el-step {
    width: 155px;
  }
}
.page-explain {
  cursor: pointer;
  float: right;
  color: #606266;
}
.flow-form-main {
  >>> .main {
    padding: 0 !important;
  }
  >>> .el-tabs__header {
    padding: 0;
    margin-bottom: 0;
    .el-tabs__item {
      line-height: 50px;
    }
  }
  .config {
    flex: 1;
    padding: 10px;
    display: flex;
    justify-content: space-between;
    overflow: hidden;
    .tableData {
      flex-shrink: 0;
      width: 350px;
      .tableDataSelect {
        width: 100%;
      }
      .box {
        margin-top: 8px;
        border-radius: 4px;
        height: calc(100% - 40px);
        border: 1px solid #dcdfe6;
        overflow: hidden;
        .search-box {
          padding: 10px;
        }
        .tree-box {
          height: calc(100% - 52px);
          overflow: auto;
          overflow-x: hidden;
        }
      }
    }
    .api_detail {
      margin-left: 0;
    }
    .detail {
      flex: 1;
      margin: -10px 10px 0 10px;
      overflow: hidden;
      display: flex;
      flex-direction: column;
      .top-box {
        display: flex;
        .main-box {
          flex: 1;
          margin-right: 18px;
        }
      }
      .sql-box {
        border-top: 1px solid #dcdfe6;
        height: calc(100vh - 258px);
        overflow: auto;
      }
    }
  }
  .parameterList {
    padding-left: 110px;
    margin-bottom: 18px;
    >>> .el-icon-edit-outline,
    >>> .el-icon-delete {
      font-size: 16px;
    }
  }
  .middle-pane {
    flex-shrink: 0;
    display: flex;
    flex-direction: column;
    height: 100%;
    overflow: hidden;
    margin-top: 10px;
    .right-pane-list {
      border: 1px solid #dcdfe6;
      border-radius: 4px;
      flex: 1;
      display: flex;
      flex-direction: column;
      margin-bottom: 10px;
      overflow: hidden;
      .requestMethod {
        width: 100%;
        padding: 18px 15px 0;
        border-bottom: 1px solid #dcdfe6;
        >>> .el-select .el-input {
          width: 90px;
        }
        >>> .el-form {
          padding-top: 0;
        }
      }
      .textBlock {
        text-align: left;
        font-size: 14px;
        padding: 18px 15px;
        border-bottom: 1px solid #dcdfe6;
      }
      .tabsBlock {
        >>> .el-tabs__header {
          padding: 0 15px !important;
        }
      }
      .radio-group-block {
        padding: 18px 15px 0;
      }
      .body_txt {
        margin-left: 50px;
        margin-top: 20px;
        color: #606266;
        font-size: 15px;
      }
      .json-block {
        padding: 0 15px 15px;
        width: 100%;
        height: 100%;
        margin-top: 18px;
        .json-block-inner {
          border: 1px solid #dcdfe6;
          width: 100%;
          height: 100%;
          border-radius: 4px;
          font-size: 14px;
          color: #606266;
          .inner-hd {
            padding: 10px;
            display: flex;
            flex-direction: row;
            align-items: center;
            justify-content: space-between;
            border-bottom: 1px solid #dcdfe6;
            .cursor_pointer {
              cursor: pointer;
            }
          }
          .JSONEditor {
            max-height: 450px;
          }
        }
      }
      .cap {
        height: 36px;
        line-height: 36px;
        display: flex;
        color: #606266;
        font-size: 14px;
        padding: 0 10px;
        flex-shrink: 0;
        justify-content: space-between;
        align-items: center;
      }
      .table-actions {
        flex-shrink: 0;
      }
      .list {
        flex: 1;
        display: flex;
        flex-direction: column;
        overflow: hidden;
      }
      >>> .el-icon-edit-outline,
      >>> .el-icon-delete {
        font-size: 16px;
      }
    }
    .right-pane-btn {
      flex-shrink: 0;
      .el-button {
        width: 100%;
      }
    }
  }
  .right-pane {
    width: 350px;
    flex-shrink: 0;
    display: flex;
    flex-direction: column;
    height: calc(100% + 9px);
    overflow: hidden;

    .right-pane-list {
      border: 1px solid #dcdfe6;
      border-radius: 4px;
      flex: 1;
      display: flex;
      flex-direction: column;
      margin-bottom: 10px;
      overflow: hidden;
      .cap {
        height: 38px;
        line-height: 38px;
        display: flex;
        color: #606266;
        font-size: 14px;
        padding: 0 10px;
        flex-shrink: 0;
        justify-content: space-between;
        align-items: center;
        .cap_sys {
          float: right;
          cursor: pointer;
        }
      }
      .table-actions {
        flex-shrink: 0;
      }
      .list {
        flex: 1;
        display: flex;
        flex-direction: column;
        overflow: hidden;
      }
      >>> .el-icon-edit-outline,
      >>> .el-icon-delete {
        font-size: 16px;
      }
    }
    .right-pane-btn {
      flex-shrink: 0;
      .el-button {
        width: 100%;
      }
    }
  }
}
</style>