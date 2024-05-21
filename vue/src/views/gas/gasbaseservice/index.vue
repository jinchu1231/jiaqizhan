






<template>
    <div class="JNPF-common-layout">
        <div class="JNPF-common-layout-center">
            <el-row class="JNPF-common-search-box" :gutter="16">
                <el-form @submit.native.prevent>
                            <el-col :span="6">
                                <el-form-item label="服务区名">
                                        <el-input v-model="query.name" placeholder="请输入" clearable>  </el-input>
                                </el-form-item>
                            </el-col>
                            <el-col :span="6">
                                <el-form-item label="管理单位">
                                        <JnpfDepSelect
 selectType="all"                                         v-model="query.managementunit" placeholder="请选择
                                        " clearable  multiple  />
                                </el-form-item>
                            </el-col>
                            <el-col :span="6">
                                <el-form-item label="经营单位">
                                        <JnpfDepSelect
 selectType="all"                                         v-model="query.businessunit" placeholder="请选择
                                        " clearable  multiple  />
                                </el-form-item>
                            </el-col>
                        <template v-if="showAll">
                                    <el-col :span="6">
                                        <el-form-item label="所处地市">
                                                <JnpfAreaSelect  v-model="query.city" placeholder="请选择" :level="2"
                                                clearable/>
                                        </el-form-item>
                                    </el-col>
                                    <el-col :span="6">
                                        <el-form-item label="公路类型">
                                                <JnpfSelect v-model="query.highwayType" placeholder="请选择" clearable
                                                            :options="highwayTypeOptions"
                                                            :props="highwayTypeProps"  multiple >
                                                </JnpfSelect>
                                        </el-form-item>
                                    </el-col>
                                    <el-col :span="6">
                                        <el-form-item label="服务类型">
                                                <JnpfSelect v-model="query.type" placeholder="请选择" clearable
                                                            :options="typeOptions"
                                                            :props="typeProps"  multiple >
                                                </JnpfSelect>
                                        </el-form-item>
                                    </el-col>
                                    <el-col :span="6">
                                        <el-form-item label="方向">
                                                <JnpfSelect v-model="query.direction" placeholder="请选择" clearable
                                                            :options="directionOptions"
                                                            :props="directionProps"  multiple >
                                                </JnpfSelect>
                                        </el-form-item>
                                    </el-col>
                                    <el-col :span="6">
                                        <el-form-item label="行车方向">
                                                <el-input v-model="query.directiondriving" placeholder="请输入" clearable>  </el-input>
                                        </el-form-item>
                                    </el-col>
                                    <el-col :span="6">
                                        <el-form-item label="是否入省口">
                                                <JnpfSelect v-model="query.entrance" placeholder="请选择" clearable
                                                            :options="entranceOptions"
                                                            :props="entranceProps"  multiple >
                                                </JnpfSelect>
                                        </el-form-item>
                                    </el-col>
                                    <el-col :span="6">
                                        <el-form-item label="是否出省口">
                                                <JnpfSelect v-model="query.export" placeholder="请选择" clearable
                                                            :options="exportOptions"
                                                            :props="exportProps"  multiple >
                                                </JnpfSelect>
                                        </el-form-item>
                                    </el-col>
                        </template>
                    <el-col :span="6">
                        <el-form-item>
                            <el-button type="primary" icon="el-icon-search" @click="search()">查询</el-button>
                            <el-button icon="el-icon-refresh-right" @click="reset()">重置</el-button>
                                <el-button type="text" icon="el-icon-arrow-down" @click="showAll=true" v-if="!showAll">
                                    展开
                                </el-button>
                                <el-button type="text" icon="el-icon-arrow-up" @click="showAll=false" v-else>
                                    收起
                                </el-button>
                        </el-form-item>
                    </el-col>
                </el-form>
            </el-row>
            <div class="JNPF-common-layout-main JNPF-flex-main">
                <div class="JNPF-common-head">
                    <div>
                                    <el-button type="primary" icon="icon-ym icon-ym-btn-add"  @click="addOrUpdateHandle()">新增
                                    </el-button>
                    </div>
                    <div class="JNPF-common-head-right">
                            <el-tooltip content="高级查询" placement="top" v-if="true">
                                <el-link icon="icon-ym icon-ym-filter JNPF-common-head-icon" :underline="false"
                                         @click="openSuperQuery()" />
                            </el-tooltip>
                            <el-tooltip effect="dark" :content="$t('common.refresh')" placement="top">
                                <el-link icon="icon-ym icon-ym-Refresh JNPF-common-head-icon" :underline="false"
                                         @click="initData()" />
                            </el-tooltip>
                    </div>
                </div>
                <JNPF-table v-loading="listLoading" :data="list" @sort-change='handleTableSort' :header-cell-class-name="handleHeaderClass"
                            :has-c="hasBatchBtn" @selection-change="handleSelectionChange"
 :span-method="arraySpanMethod"  

>
                                                <el-table-column
                                                            prop="name"
 label="服务区名"  align="center"
  show-overflow-tooltip >
    <template slot-scope="scope">
        <JnpfInput v-model="scope.row.name" detailed  showOverflow   />
    </template>
                                                </el-table-column>
                                                <el-table-column
                                                            prop="code"
 label="服务区编码"  align="center"
  show-overflow-tooltip >
                                                </el-table-column>
                                                <el-table-column
                                                            prop="managementunit"
 label="管理单位"  align="center"
  show-overflow-tooltip >
                                                </el-table-column>
                                                <el-table-column
                                                            prop="businessunit"
 label="经营单位"  align="center"
  show-overflow-tooltip >
                                                </el-table-column>
                                                <el-table-column
                                                            prop="city"
 label="所处地市"  align="center"
  show-overflow-tooltip >
                                                </el-table-column>
                                                <el-table-column
                                                            prop="county"
 label="所处区县"  align="center"
  show-overflow-tooltip >
                                                </el-table-column>
                                                <el-table-column
                                                            prop="districtCode"
 label="行政区划"  align="center"
  show-overflow-tooltip >
    <template slot-scope="scope">
        <JnpfInput v-model="scope.row.districtCode" detailed  showOverflow   />
    </template>
                                                </el-table-column>
                                                <el-table-column
                                                            prop="address"
 label="服务区地址"  align="center"
  show-overflow-tooltip >
                                                </el-table-column>
                                                <el-table-column
                                                            prop="type"
 label="服务类型"  align="center"
  show-overflow-tooltip >
                                                </el-table-column>
                                                <el-table-column
                                                            prop="highwayType"
 label="公路类型"  align="center"
  show-overflow-tooltip >
                                                </el-table-column>
                                                <el-table-column
                                                            prop="roadName"
 label="路段名称"  align="center"
  show-overflow-tooltip >
                                                </el-table-column>
                                                <el-table-column
                                                            prop="routeName"
 label="路线名称"  align="center"
  show-overflow-tooltip >
                                                </el-table-column>
                                                <el-table-column
                                                            prop="roadCode"
 label="路段编号"  align="center"
  show-overflow-tooltip >
    <template slot-scope="scope">
        <JnpfInput v-model="scope.row.roadCode" detailed  showOverflow   />
    </template>
                                                </el-table-column>
                                                <el-table-column
                                                            prop="routeCode"
 label="路线编码"  align="center"
  show-overflow-tooltip >
    <template slot-scope="scope">
        <JnpfInput v-model="scope.row.routeCode" detailed  showOverflow   />
    </template>
                                                </el-table-column>
                                                <el-table-column
                                                            prop="position"
 label="服务位置"  align="center"
  show-overflow-tooltip >
                                                </el-table-column>
                                                <el-table-column
                                                            prop="pileNumber"
 label="桩号"  align="center"
  show-overflow-tooltip >
    <template slot-scope="scope">
        <JnpfInput v-model="scope.row.pileNumber" detailed  showOverflow   />
    </template>
                                                </el-table-column>
                                                <el-table-column
                                                            prop="direction"
 label="方向"  align="center"
  show-overflow-tooltip >
                                                </el-table-column>
                                                <el-table-column
                                                            prop="directiondriving"
 label="行车方向"  align="center"
  show-overflow-tooltip >
    <template slot-scope="scope">
        <JnpfInput v-model="scope.row.directiondriving" detailed  showOverflow   />
    </template>
                                                </el-table-column>
                                                <el-table-column
                                                            prop="entrance"
 label="是否入省口"  align="center"
  show-overflow-tooltip >
                                                </el-table-column>
                                                <el-table-column
                                                            prop="export"
 label="是否出省口"  align="center"
  show-overflow-tooltip >
                                                </el-table-column>
                                                <el-table-column
                                                            prop="adjacentprovinces"
 label="相邻省份"  align="center"
  show-overflow-tooltip >
                                                </el-table-column>
                                                <el-table-column
                                                            prop="completiontime"
 label="建成时间"  align="center"
  show-overflow-tooltip >
                                                </el-table-column>
                                                <el-table-column
                                                            prop="longitude"
 label="经度"  align="center"
  show-overflow-tooltip >
    <template slot-scope="scope">
        <JnpfInput v-model="scope.row.longitude" detailed  showOverflow   />
    </template>
                                                </el-table-column>
                                                <el-table-column
                                                            prop="latitude"
 label="纬度"  align="center"
  show-overflow-tooltip >
    <template slot-scope="scope">
        <JnpfInput v-model="scope.row.latitude" detailed  showOverflow   />
    </template>
                                                </el-table-column>
                                                <el-table-column
                                                            prop="remarks"
 label="备注"  align="center"
  show-overflow-tooltip >
                                                </el-table-column>
                                                <el-table-column
                                                            prop="picture"
 label="图片"  align="center"
 >
    <template slot-scope="scope">
        <JnpfUploadImg v-model="scope.row.picture" detailed simple/>
    </template>
                                                </el-table-column>
                        <el-table-column label="操作"
 fixed="right" width="150" >
                            <template slot-scope="scope"  >
                                            <el-button type="text"
                                                       @click="addOrUpdateHandle(scope.row)" >编辑
                                            </el-button>
                                        <el-button type="text" class="JNPF-table-delBtn"  @click="handleDel(scope.row.id)">删除
                                        </el-button>
                                        <el-button type="text"  
                                                   @click="goDetail(scope.row.id)">详情
                                        </el-button>
                            </template>
                        </el-table-column>
                </JNPF-table>
                        <pagination :total="total" :page.sync="listQuery.currentPage" :limit.sync="listQuery.pageSize" @pagination="initData"/>
             </div>
        </div>
        <JNPF-Form v-if="formVisible" ref="JNPFForm" @refresh="refresh"/>
        <ExportBox v-if="exportBoxVisible" ref="ExportBox" @download="download"/>




        <ImportBox v-if="uploadBoxVisible" ref="UploadBox" @refresh="initData" />
        <Detail v-if="detailVisible" ref="Detail" @refresh="detailVisible=false"/>
        <ToFormDetail v-if="toFormDetailVisible" ref="toFormDetail" @close="toFormDetailVisible = false" />
            <SuperQuery v-if="superQueryVisible" ref="SuperQuery" :columnOptions="superQueryJson"
                        @superQuery="superQuery" />
    </div>
</template>

<script>
import request from '@/utils/request'
import {mapGetters} from "vuex";
import {getDictionaryDataSelector} from '@/api/systemData/dictionary'
import JNPFForm from './form'
import Detail from './Detail'
import ExportBox from '@/components/ExportBox'
import ToFormDetail from '@/views/basic/dynamicModel/list/detail'
import {getDataInterfaceRes} from '@/api/systemData/dataInterface'
import { getConfigData } from '@/api/onlineDev/visualDev'
import { getDefaultCurrentValueUserIdAsync } from '@/api/permission/user'
import { getDefaultCurrentValueDepartmentIdAsync } from '@/api/permission/organize'
import columnList from './columnList'
import { thousandsFormat } from "@/components/Generator/utils/index"
import SuperQuery from '@/components/SuperQuery'
import superQueryJson from './superQueryJson'
import { noGroupList } from '@/components/Generator/generator/comConfig'

    export default {
        components: {
                JNPFForm,
                Detail,
 ExportBox,ToFormDetail , SuperQuery
        },
        data() {
            return {

                keyword:'',
                expandsTree: true,
                refreshTree: true,
                toFormDetailVisible:false,
                hasBatchBtn:false,
                                expandObj:{},
                columnOptions: [],
                mergeList: [],
                exportList:[],
                    columnList,

                    showAll: false,
                    superQueryVisible: false,
                    superQueryJson,
                uploadBoxVisible: false,
                detailVisible: false,
                query: {
                        name:undefined,
                        managementunit:undefined,
                        businessunit:undefined,
                        city:undefined,
                        highwayType:undefined,
                        type:undefined,
                        direction:undefined,
                        directiondriving:undefined,
                        entrance:undefined,
                        export:undefined,
            },
                defListQuery: {
                    sort: 'desc',
                    sidx: '',
                },
                //排序默认值
                defaultSortConfig:  [],
            treeProps: {
                    children: 'children',
                    label: 'fullName',
                    value: 'id',
                    isLeaf: 'isLeaf'
            },
            list: [],
            listLoading: true,
            multipleSelection: [],
                    total: 0,
            queryData: {},
            listQuery: {
                    superQueryJson: '',
                    currentPage: 1,
                    pageSize: 20,
                    sort: "",
                    sidx: "",
            },
            //多列排序
            ordersList: [],
            formVisible: false,
            flowVisible: false,
            flowListVisible: false,
            flowList: [],
            exportBoxVisible: false,
                                                                                                                                                                typeOptions:[],
                        typeProps:{"label":"fullName","value":"id"  },
                            highwayTypeOptions:[],
                        highwayTypeProps:{"label":"fullName","value":"id"  },
                            roadNameOptions:[],
                        roadNameProps:{"label":"name","value":"code"  },
                            routeNameOptions:[],
                        routeNameProps:{"label":"name","value":"code"  },
                            positionOptions:[],
                        positionProps:{"label":"fullName","value":"id"  },
                            directionOptions:[],
                        directionProps:{"label":"fullName","value":"id"  },
                            entranceOptions:[],
                        entranceProps:{"label":"fullName","value":"id"  },
                            exportOptions:[],
                        exportProps:{"label":"fullName","value":"id"  },
            interfaceRes: {
                },
            //掩码配置
            maskConfig: {
                        name: {"prefixType":1,"useUnrealMask":false,"maskType":1,"unrealMaskLength":1,"prefixLimit":0,"suffixLimit":0,"filler":"*","prefixSpecifyChar":"","suffixType":1,"ignoreChar":"","suffixSpecifyChar":""} ,
                        districtCode: {"prefixType":1,"useUnrealMask":false,"maskType":1,"unrealMaskLength":1,"prefixLimit":0,"suffixLimit":0,"filler":"*","prefixSpecifyChar":"","suffixType":1,"ignoreChar":"","suffixSpecifyChar":""} ,
                        roadCode: {"prefixType":1,"useUnrealMask":false,"maskType":1,"unrealMaskLength":1,"prefixLimit":0,"suffixLimit":0,"filler":"*","prefixSpecifyChar":"","suffixType":1,"ignoreChar":"","suffixSpecifyChar":""} ,
                        routeCode: {"prefixType":1,"useUnrealMask":false,"maskType":1,"unrealMaskLength":1,"prefixLimit":0,"suffixLimit":0,"filler":"*","prefixSpecifyChar":"","suffixType":1,"ignoreChar":"","suffixSpecifyChar":""} ,
                        pileNumber: {"prefixType":1,"useUnrealMask":false,"maskType":1,"unrealMaskLength":1,"prefixLimit":0,"suffixLimit":0,"filler":"*","prefixSpecifyChar":"","suffixType":1,"ignoreChar":"","suffixSpecifyChar":""} ,
                        directiondriving: {"prefixType":1,"useUnrealMask":false,"maskType":1,"unrealMaskLength":1,"prefixLimit":0,"suffixLimit":0,"filler":"*","prefixSpecifyChar":"","suffixType":1,"ignoreChar":"","suffixSpecifyChar":""} ,
                        longitude: {"prefixType":1,"useUnrealMask":false,"maskType":1,"unrealMaskLength":1,"prefixLimit":0,"suffixLimit":0,"filler":"*","prefixSpecifyChar":"","suffixType":1,"ignoreChar":"","suffixSpecifyChar":""} ,
                        latitude: {"prefixType":1,"useUnrealMask":false,"maskType":1,"unrealMaskLength":1,"prefixLimit":0,"suffixLimit":0,"filler":"*","prefixSpecifyChar":"","suffixType":1,"ignoreChar":"","suffixSpecifyChar":""} ,
            },
            }
        },
        computed: {
            ...mapGetters(['userInfo']),
            menuId() {
                return this.$route.meta.modelId || ''
            }
        },
        created() {
                        this.getColumnList(),
                this.initSearchDataAndListData()
                    this.gethighwayTypeOptions();
                    this.gettypeOptions();
                    this.getdirectionOptions();
                    this.getentranceOptions();
                    this.getexportOptions();
            this.queryData = JSON.parse(JSON.stringify(this.query))
            this.getHasBatchBtn();
            //排序默认值
            this.setDefaultQuery(this.defaultSortConfig);
        },
        methods: {
            getHasBatchBtn(){
                let btnsList =[]
                this.hasBatchBtn = btnsList.some(o => ['batchRemove', 'batchPrint', 'download'].includes(o))
            },

            treeRefresh(){
                this.keyword=''
                this.treeActiveId = ''
                this.leftTreeActiveInfo ={}
                this.$refs.treeBox.setCurrentKey(null)
                this.getTreeView()
            },

            toDetail(defaultValue, modelId) {
                if (!defaultValue) return
                getConfigData(modelId).then(res => {
                    if (!res.data || !res.data.formData) return
                    let formData = JSON.parse(res.data.formData)
                    formData.popupType = 'general'
                    this.toFormDetailVisible = true
                    this.$nextTick(() => {
                        this.$refs.toFormDetail.init(formData, modelId, defaultValue)
                    })
                })
            },
            toggleTreeExpand(expands) {
                this.refreshTree = false
                this.expandsTree = expands
                this.$nextTick(() => {
                    this.refreshTree = true
                    this.$nextTick(() => {
                        this.$refs.treeBox.setCurrentKey(null)
                    })
                })
            },
            filterNode(value, data) {
                if (!value) return true;
                return data[this.treeProps.label].indexOf(value) !== -1;
            },
            loadNode(node, resolve) {
                const nodeData = node.data
                const config ={
                    treeInterfaceId:"",
                    treeTemplateJson:[]
                }
                if (config.treeInterfaceId) {
                    //这里是为了拿到参数中关联的字段的值，后端自行拿
                    if (config.treeTemplateJson && config.treeTemplateJson.length) {
                        for (let i = 0; i < config.treeTemplateJson.length; i++) {
                            const element = config.treeTemplateJson[i];
                            element.defaultValue = nodeData[element.relationField] || ''
                        }
                    }
                    //参数
                    let query = {
                        paramList: config.treeTemplateJson || [],
                    }
                    //接口
                    getDataInterfaceRes(config.treeInterfaceId, query).then(res => {
                        let data = res.data
                        if (Array.isArray(data)) {
                            resolve(data);
                        } else {
                            resolve([]);
                        }
                    })
                }
            },
                        getColumnList() {
                    // 没有开启权限
                    this.columnOptions = this.transformColumnList(this.columnList)
            },
            transformColumnList(columnList) {
                let list = []
                for (let i = 0; i < columnList.length; i++) {
                    const e = columnList[i];
                    if (!e.prop.includes('-')) {
                        list.push(e)
                    } else {
                        let prop = e.prop.split('-')[0]
                        let label = e.label.split('-')[0]
                        let vModel = e.prop.split('-')[1]
                        let newItem = {
                            align: "center",
                            jnpfKey: "table",
                            prop,
                            label,
                            children: []
                        }
                        e.vModel = vModel
                        if (!this.expandObj.hasOwnProperty(`${prop}Expand`)) this.$set(this.expandObj, `${prop}Expand`, false)
                        if (!list.some(o => o.prop === prop)) list.push(newItem)
                        for (let i = 0; i < list.length; i++) {
                            if (list[i].prop === prop) {
                                list[i].children.push(e)
                                break
                            }
                        }
                    }
                }
                this.getMergeList(list)
                this.getExportList(list)
                return list
            },
            arraySpanMethod({ column }) {
                for (let i = 0; i < this.mergeList.length; i++) {
                    if (column.property == this.mergeList[i].prop) {
                        return [this.mergeList[i].rowspan, this.mergeList[i].colspan]
                    }
                }
            },
            getMergeList(list) {
                let newList = JSON.parse(JSON.stringify(list))
                newList.forEach(item => {
                    if (item.children && item.children.length) {
                        let child = {
                            prop: item.prop + '-child-first'
                        }
                        item.children.unshift(child)
                    }
                })
                newList.forEach(item => {
                    if (item.children && item.children.length ) {
                        item.children.forEach((child, index) => {
                            if (index == 0) {
                                this.mergeList.push({
                                    prop: child.prop,
                                    rowspan: 1,
                                    colspan: item.children.length
                                })
                            } else {
                                this.mergeList.push({
                                    prop: child.prop,
                                    rowspan: 0,
                                    colspan: 0
                                })
                            }
                        })
                    } else {
                        this.mergeList.push({
                            prop: item.prop,
                            rowspan: 1,
                            colspan: 1
                        })
                    }
                })
            },
            getExportList(list) {
                let exportList = []
                for (let i = 0; i < list.length; i++) {
                    if (list[i].jnpfKey === 'table') {
                        for (let j = 0; j < list[i].children.length; j++) {
                            exportList.push(list[i].children[j])
                        }
                    } else {
                        exportList.push(list[i])
                    }
                }
                this.exportList = exportList.filter(o => !noGroupList.includes(o.__config__.jnpfKey))
            },
                    gethighwayTypeOptions() {
                        getDictionaryDataSelector('562362704245825221').then(res => {
                            this.highwayTypeOptions = res.data.list
                        })
                    },
                    gettypeOptions() {
                        getDictionaryDataSelector('562362879907471045').then(res => {
                            this.typeOptions = res.data.list
                        })
                    },
                    getdirectionOptions() {
                        getDictionaryDataSelector('562363166160330437').then(res => {
                            this.directionOptions = res.data.list
                        })
                    },
                    getentranceOptions() {
                        getDictionaryDataSelector('562363471975423685').then(res => {
                            this.entranceOptions = res.data.list
                        })
                    },
                    getexportOptions() {
                        getDictionaryDataSelector('562363361631674053').then(res => {
                            this.exportOptions = res.data.list
                        })
                    },
            goDetail(id){
                this.detailVisible = true
                this.$nextTick(() => {
                    this.$refs.Detail.init(id)
                })
            },
            sortChange({column, prop, order}) {
                this.listQuery.sort = order == 'ascending' ? 'asc' : 'desc'
                this.listQuery.sidx = !order ? '' : prop
                this.initData()
            },
            async initSearchDataAndListData() {
                await this.initSearchData()
                this.initData()
            },
            //初始化查询的默认数据
            async initSearchData() {
            },
            initData() {
                // this.queryData = JSON.parse(JSON.stringify(this.query))导致重置失效
                this.listLoading = true;
                let _query = {
                    ...this.listQuery,
                    ...this.query,
                    ...this.defListQuery,
                        keyword: this.keyword,
                        dataType: 0,
                    menuId:this.menuId,
                    moduleId:'562603695003410309',
                    type:1,
                };
                request({
                    url: `/api/gas/GasBaseService/getList`,
                    method: 'post',
                    data: _query
                }).then(res => {
                    var _list =[];
                    for(let i=0;i<res.data.list.length;i++){
                        let _data = res.data.list[i];
                        _list.push(_data)
                    }
                                            this.list = _list.map(o => ({
                            ...o,
                            ...this.expandObj,
                        }))
                            this.total = res.data.pagination.total
                    this.listLoading = false
                })
            },
            handleDel(id) {
                this.$confirm('此操作将永久删除该数据, 是否继续?', '提示', {
                    type: 'warning'
                }).then(() => {
                    request({
                        url: `/api/gas/GasBaseService/${id}`,
                        method: 'DELETE'
                    }).then(res => {
                        this.$message({
                            type: 'success',
                            message: res.msg,
                            onClose: () => {
                                this.initData()
                            }
                        });
                    })
                }).catch(() => {
                });
            },
            handelUpload(){
                this.uploadBoxVisible = true
                this.$nextTick(() => {
                    this.$refs.UploadBox.init("","gas/GasBaseService",0,this.flowList)
                })
            },
                openSuperQuery() {
                    this.superQueryVisible = true
                    this.$nextTick(() => {
                        this.$refs.SuperQuery.init()
                    })
                },
                superQuery(queryJson) {
                    this.listQuery.superQueryJson = queryJson
                    this.listQuery.currentPage = 1
                    this.initData()
                },
            addOrUpdateHandle(row, isDetail) {
                let id = row?row.id:""
                this.formVisible = true
                if(!this.treeActiveId) {
                    this.leftTreeActiveInfo={}
                }
                this.$nextTick(() => {
                    this.$refs.JNPFForm.init(id, isDetail,this.list,this.leftTreeActiveInfo)
                })
            },
            exportData() {
                this.exportBoxVisible = true
                this.$nextTick(() => {
                    this.$refs.ExportBox.init(this.exportList, this.multipleSelection)
                })
            },
            download(data) {
                let query = {...data, ...this.listQuery, ...this.query,menuId:this.menuId}
                request({
                    url: `/api/gas/GasBaseService/Actions/Export`,
                    method: 'post',
                    data: query
                }).then(res => {
                    if (!res.data.url) return
                    this.jnpf.downloadFile(res.data.url)
                    this.$refs.ExportBox.visible = false
                    this.exportBoxVisible = false
                })
            },
            search() {
                    this.listQuery.currentPage=1
                        this.listQuery.pageSize=20
                this.initData()
            },
            refresh(isrRefresh) {
                this.formVisible = false
                if (isrRefresh) this.reset()
            },
            reset() {
                this.query = JSON.parse(JSON.stringify(this.queryData))
                this.search()
            },
            colseFlow(isrRefresh) {
                this.flowVisible = false
                if (isrRefresh) this.reset()
            },

            //以下排序相关方法
            setDefaultQuery(defaultSortList) {
                const defaultSortConfig = (defaultSortList || []).map(o =>
                        (o.sort === 'desc' ? '-' : '') + o.field);
                this.defListQuery.sidx = defaultSortConfig.join(',')
            },
            handleHeaderClass({ column }) {
                column.order = column.multiOrder
            },
            handleTableSort({ column }) {
                if (column.sortable !== 'custom') return
                column.multiOrder = column.multiOrder === 'descending' ? 'ascending' : column.multiOrder ? '' : 'descending';
                this.handleOrderChange(column.property, column.multiOrder)
            },
            handleOrderChange(orderColumn, orderState) {
                let index = this.ordersList.findIndex(e => e.field === orderColumn);
                let sort = orderState === 'ascending' ? 'asc' : orderState === 'descending' ? 'desc' : '';
                if (index > -1) {
                    this.ordersList[index].sort = orderState;
                } else {
                    this.ordersList.push({ field: orderColumn, sort });
                }
                this.ordersList = this.ordersList.filter(e => e.sort);
                this.ordersList.length ? this.setDefaultQuery(this.ordersList) : this.setDefaultQuery(this.defaultSortConfig)
                this.initData()
            },
            //以上排序相关方法
        }
    }
</script>
