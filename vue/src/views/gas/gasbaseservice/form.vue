


    <template>
            <transition name="el-zoom-in-center">
            <div class="JNPF-preview-main">
                <div class="JNPF-common-page-header">
                    <el-page-header @back="goBack"
                                    :content="!dataForm.id ? '新建':'编辑'"/>
                    <div class="options">
                            <el-dropdown class="dropdown" placement="bottom">
                                <el-button style="width:70px">
                                    更 多<i class="el-icon-arrow-down el-icon--right"></i>
                                </el-button>
                                <el-dropdown-menu slot="dropdown">
                                        <template v-if="dataForm.id">
                                            <el-dropdown-item @click.native="prev" :disabled='prevDis'>
                                                {{'上一条'}}
                                            </el-dropdown-item>
                                            <el-dropdown-item @click.native="next" :disabled='nextDis'>
                                                {{'下一条'}}
                                            </el-dropdown-item>
                                        </template>
                                    <el-dropdown-item type="primary" @click.native="dataFormSubmit(2)"
                                                      :loading="continueBtnLoading" :disabled='btnLoading'>
                                        {{!dataForm.id ?'确定并新增':'确定并继续'}}</el-dropdown-item>
                                </el-dropdown-menu>
                            </el-dropdown>
                        <el-button type="primary" @click="dataFormSubmit()" :loading="btnLoading" :disabled='continueBtnLoading'> 保 存</el-button>
                        <el-button @click="goBack"> 取 消</el-button>
                    </div>
                </div>
            <el-row :gutter="15" class=" main" :style="{margin: '0 auto',width: '100%'}">
    <el-form ref="formRef" :model="dataForm" :rules="dataRule" size="small" label-width="100px" label-position="right" >
    <template v-if="!loading">
        <!-- 具体表单 -->
        <el-col :span="12" >
        <jnpf-form-tip-item
 label="服务区名"              align="left"
 prop="name" >
        <JnpfInput   v-model="dataForm.name" @change="changeData('name',-1)" 
 placeholder="请输入"  clearable  :style='{"width":"100%"}' :maskConfig = "maskConfig.name">
    </JnpfInput>
    </jnpf-form-tip-item>
        </el-col>
        <el-col :span="12" >
        <jnpf-form-tip-item
 label="服务区编码"              align="left"
 prop="code" >
        <JnpfInput   v-model="dataForm.code" @change="changeData('code',-1)" 
 placeholder="系统自动生成"  readonly  :style='{"width":"100%"}'>
    </JnpfInput>
    </jnpf-form-tip-item>
        </el-col>
        <el-col :span="12" >
        <jnpf-form-tip-item
 label="管理单位"              align="left"
 prop="managementunit" >
        <JnpfDepSelect   v-model="dataForm.managementunit" @change="changeData('managementunit',-1)" 
 placeholder="请选择"  selectType="all"  :ableIds="ableAll.managementunitableIds"  clearable  :style='{"width":"100%"}'>
    </JnpfDepSelect>
    </jnpf-form-tip-item>
        </el-col>
        <el-col :span="12" >
        <jnpf-form-tip-item
 label="经营单位"              align="left"
 prop="businessunit" >
        <JnpfDepSelect   v-model="dataForm.businessunit" @change="changeData('businessunit',-1)" 
 placeholder="请选择"  selectType="all"  :ableIds="ableAll.businessunitableIds"  clearable  :style='{"width":"100%"}'>
    </JnpfDepSelect>
    </jnpf-form-tip-item>
        </el-col>
        <el-col :span="12" >
        <jnpf-form-tip-item
 label="所处地市"              align="left"
 prop="city" >
        <JnpfAreaSelect   v-model="dataForm.city" @change="changeData('city',-1)" 
 placeholder="请选择"  selectType="all"  clearable  :style='{"width":"100%"}' :level="2" >
    </JnpfAreaSelect>
    </jnpf-form-tip-item>
        </el-col>
        <el-col :span="12" >
        <jnpf-form-tip-item
 label="所处区县"              align="left"
 prop="county" >
        <JnpfAreaSelect   v-model="dataForm.county" @change="changeData('county',-1)" 
 placeholder="请选择"  selectType="all"  clearable  :style='{"width":"100%"}' :level="2" >
    </JnpfAreaSelect>
    </jnpf-form-tip-item>
        </el-col>
        <el-col :span="12" >
        <jnpf-form-tip-item
 label="行政区划"              align="left"
 prop="districtCode" >
        <JnpfInput   v-model="dataForm.districtCode" @change="changeData('districtCode',-1)" 
 placeholder="请输入"  clearable  :style='{"width":"100%"}' :maskConfig = "maskConfig.districtCode">
    </JnpfInput>
    </jnpf-form-tip-item>
        </el-col>
        <el-col :span="12" >
        <jnpf-form-tip-item
 label="服务区地址"              align="left"
 prop="address" >
        <JnpfLocation   v-model="dataForm.address" @change="changeData('address',-1)" 
 clearable  :adjustmentScope = "500" >
    </JnpfLocation>
    </jnpf-form-tip-item>
        </el-col>
        <el-col :span="12" >
        <jnpf-form-tip-item
 label="服务类型"              align="left"
 prop="type" >
        <JnpfSelect   v-model="dataForm.type" @change="changeData('type',-1)" 
 placeholder="请选择"  clearable  :style='{"width":"100%"}' :options="typeOptions" :props="typeProps" >
    </JnpfSelect>
    </jnpf-form-tip-item>
        </el-col>
        <el-col :span="12" >
        <jnpf-form-tip-item
 label="公路类型"              align="left"
 prop="highwayType" >
        <JnpfSelect   v-model="dataForm.highwayType" @change="changeData('highwayType',-1)" 
 placeholder="请选择"  clearable  :style='{"width":"100%"}' :options="highwayTypeOptions" :props="highwayTypeProps" >
    </JnpfSelect>
    </jnpf-form-tip-item>
        </el-col>
        <el-col :span="12" >
        <jnpf-form-tip-item
 label="路段名称"              align="left"
 prop="roadName" >
        <JnpfSelect   v-model="dataForm.roadName" @change="changeData('roadName',-1)" 
 placeholder="请选择"  clearable  :style='{"width":"100%"}' :options="roadNameOptions" :props="roadNameProps" >
    </JnpfSelect>
    </jnpf-form-tip-item>
        </el-col>
        <el-col :span="12" >
        <jnpf-form-tip-item
 label="路线名称"              align="left"
 prop="routeName" >
        <JnpfSelect   v-model="dataForm.routeName" @change="changeData('routeName',-1)" 
 placeholder="请选择"  clearable  :style='{"width":"100%"}' :options="routeNameOptions" :props="routeNameProps" >
    </JnpfSelect>
    </jnpf-form-tip-item>
        </el-col>
        <el-col :span="12" >
        <jnpf-form-tip-item
 label="路段编号"              align="left"
 prop="roadCode" >
        <JnpfInput   v-model="dataForm.roadCode" @change="changeData('roadCode',-1)" 
 placeholder="请输入"  clearable  :style='{"width":"100%"}' :maskConfig = "maskConfig.roadCode">
    </JnpfInput>
    </jnpf-form-tip-item>
        </el-col>
        <el-col :span="12" >
        <jnpf-form-tip-item
 label="路线编码"              align="left"
 prop="routeCode" >
        <JnpfInput   v-model="dataForm.routeCode" @change="changeData('routeCode',-1)" 
 placeholder="请输入"  clearable  :style='{"width":"100%"}' :maskConfig = "maskConfig.routeCode">
    </JnpfInput>
    </jnpf-form-tip-item>
        </el-col>
        <el-col :span="12" >
        <jnpf-form-tip-item
 label="服务位置"              align="left"
 prop="position" >
        <JnpfSelect   v-model="dataForm.position" @change="changeData('position',-1)" 
 placeholder="请选择"  clearable  :style='{"width":"100%"}' :options="positionOptions" :props="positionProps" >
    </JnpfSelect>
    </jnpf-form-tip-item>
        </el-col>
        <el-col :span="12" >
        <jnpf-form-tip-item
 label="桩号"              align="left"
 prop="pileNumber" >
        <JnpfInput   v-model="dataForm.pileNumber" @change="changeData('pileNumber',-1)" 
 placeholder="请输入"  clearable  :style='{"width":"100%"}' :maskConfig = "maskConfig.pileNumber">
    </JnpfInput>
    </jnpf-form-tip-item>
        </el-col>
        <el-col :span="12" >
        <jnpf-form-tip-item
 label="方向"              align="left"
 prop="direction" >
        <JnpfSelect   v-model="dataForm.direction" @change="changeData('direction',-1)" 
 placeholder="请选择"  clearable  :style='{"width":"100%"}' :options="directionOptions" :props="directionProps" >
    </JnpfSelect>
    </jnpf-form-tip-item>
        </el-col>
        <el-col :span="12" >
        <jnpf-form-tip-item
 label="行车方向"              align="left"
 prop="directiondriving" >
        <JnpfInput   v-model="dataForm.directiondriving" @change="changeData('directiondriving',-1)" 
 placeholder="请输入"  clearable  :style='{"width":"100%"}' :maskConfig = "maskConfig.directiondriving">
    </JnpfInput>
    </jnpf-form-tip-item>
        </el-col>
        <el-col :span="12" >
        <jnpf-form-tip-item
 label="是否入省口"              align="left"
 prop="entrance" >
        <JnpfSelect   v-model="dataForm.entrance" @change="changeData('entrance',-1)" 
 placeholder="请选择"  clearable  :style='{"width":"100%"}' :options="entranceOptions" :props="entranceProps" >
    </JnpfSelect>
    </jnpf-form-tip-item>
        </el-col>
        <el-col :span="12" >
        <jnpf-form-tip-item
 label="是否出省口"              align="left"
 prop="export" >
        <JnpfSelect   v-model="dataForm.export" @change="changeData('export',-1)" 
 placeholder="请选择"  clearable  :style='{"width":"100%"}' :options="exportOptions" :props="exportProps" >
    </JnpfSelect>
    </jnpf-form-tip-item>
        </el-col>
        <el-col :span="12" >
        <jnpf-form-tip-item
 label="相邻省份"              align="left"
 prop="adjacentprovinces" >
        <JnpfAreaSelect   v-model="dataForm.adjacentprovinces" @change="changeData('adjacentprovinces',-1)" 
 placeholder="请选择"  selectType="all"  clearable  :style='{"width":"100%"}' :level="2" >
    </JnpfAreaSelect>
    </jnpf-form-tip-item>
        </el-col>

        <el-col :span="12" >
        <jnpf-form-tip-item
 label="建成时间"              align="left"
 prop="completiontime" >
        <JnpfDatePicker   v-model="dataForm.completiontime" @change="changeData('completiontime',-1)" 
 :startTime="dateTime(false,1,1,'','')"  :endTime="dateTime(false,1,1,'','')"  placeholder="请选择"  clearable  :style='{"width":"100%"}' type="datetime"  format="yyyy-MM-dd HH:mm:ss" >
    </JnpfDatePicker>
    </jnpf-form-tip-item>
        </el-col>
        <el-col :span="12" >
        <jnpf-form-tip-item
 label="经度"              align="left"
 prop="longitude" >
        <JnpfInput   v-model="dataForm.longitude" @change="changeData('longitude',-1)" 
 placeholder="请输入"  clearable  :style='{"width":"100%"}' :maskConfig = "maskConfig.longitude">
    </JnpfInput>
    </jnpf-form-tip-item>
        </el-col>
        <el-col :span="12" >
        <jnpf-form-tip-item
 label="纬度"              align="left"
 prop="latitude" >
        <JnpfInput   v-model="dataForm.latitude" @change="changeData('latitude',-1)" 
 placeholder="请输入"  clearable  :style='{"width":"100%"}' :maskConfig = "maskConfig.latitude">
    </JnpfInput>
    </jnpf-form-tip-item>
        </el-col>
        <el-col :span="24" >
        <jnpf-form-tip-item
 label="备注"              align="left"
 prop="remarks" >
        <JnpfTextarea   v-model="dataForm.remarks" @change="changeData('remarks',-1)" 
 placeholder="请输入"  :style='{"width":"100%"}' true  type="textarea"  :autosize='{"minRows":4,"maxRows":4}'  :maskConfig = "maskConfig.remarks">
    </JnpfTextarea>
    </jnpf-form-tip-item>
        </el-col>
        <el-col :span="24" >
        <jnpf-form-tip-item
 label="图片"              align="left"
 prop="picture" >
        <JnpfUploadImg   v-model="dataForm.picture" @change="changeData('picture',-1)" 
 :fileSize="10" sizeUnit="MB"  :limit="9"  pathType="defaultPath"  :isAccount="0" >
    </JnpfUploadImg>
    </jnpf-form-tip-item>
        </el-col>
        <!-- 表单结束 -->
    </template>
    </el-form>
    <SelectDialog v-if="selectDialogVisible" :config="currTableConf" :formData="dataForm"
                  ref="selectDialog" @select="addForSelect" @close="closeForSelect"/>
    </el-row>
    </div>
    </transition>
    </template>


<script>
    import request from '@/utils/request'
    import {mapGetters} from "vuex";
    import { getDataInterfaceRes } from '@/api/systemData/dataInterface'
    import { getDictionaryDataSelector } from '@/api/systemData/dictionary'
    import { getDefaultCurrentValueUserId } from '@/api/permission/user'
    import { getDefaultCurrentValueDepartmentId } from '@/api/permission/organize'
    import { getDateDay, getLaterData, getBeforeData, getBeforeTime, getLaterTime } from '@/components/Generator/utils/index.js'
    import { thousandsFormat } from "@/components/Generator/utils/index"
    import SelectDialog from '@/components/SelectDialog'

    export default {
        components: { SelectDialog },
        props: [],
        data() {
        return {
            dataFormSubmitType: 0,
            continueBtnLoading: false,
            index: 0,
            prevDis: false,
            nextDis: false,
            allList: [],
            visible: false,
            loading: false,
            btnLoading: false,
            formRef: 'formRef',
            setting:{},
            eventType: '',
            userBoxVisible:false,
            selectDialogVisible: false,
            currTableConf:{},
            dataValueAll:{},
            addTableConf:{
            },
            //可选范围默认值
            ableAll:{
            },
            tableRows:{
        },
            Vmodel:"",
            currVmodel:"",
            dataForm: {
                                name : undefined,
                                code : undefined,
                                managementunit : undefined,
                                businessunit : undefined,
                                    city : [],
                                    county : [],
                                districtCode : undefined,
                                address : undefined,
                                    type : undefined,
                                    highwayType : undefined,
                                    roadName : undefined,
                                    routeName : undefined,
                                roadCode : undefined,
                                routeCode : undefined,
                                    position : undefined,
                                pileNumber : undefined,
                                    direction : undefined,
                                directiondriving : undefined,
                                    entrance : undefined,
                                    export : undefined,
                                    adjacentprovinces : [],
                                    completiontime : undefined,
                                longitude : undefined,
                                latitude : undefined,
                                remarks : undefined,
                                    picture : [],
            },
            tableRequiredData: {},
            dataRule:
            {
                            name: [
                                {
                                    required: true,
                                    message: '请输入',
                                    trigger: 'blur'
                                },
                        ],
                            managementunit: [
                                {
                                    required: true,
                                    message: '请选择',
                                    trigger: 'change'
                                },
                        ],
                            businessunit: [
                                {
                                    required: true,
                                    message: '请选择',
                                    trigger: 'change'
                                },
                        ],
                            city: [
                                {
                                    required: true,
                                    message: '请至少选择一个',
                                    trigger: 'change'
                                },
                        ],
                            county: [
                                {
                                    required: true,
                                    message: '请至少选择一个',
                                    trigger: 'change'
                                },
                        ],
                            districtCode: [
                                {
                                    required: true,
                                    message: '请输入',
                                    trigger: 'blur'
                                },
                        ],
                            type: [
                                {
                                    required: true,
                                    message: '请选择',
                                    trigger: 'change'
                                },
                        ],
                            highwayType: [
                                {
                                    required: true,
                                    message: '请选择',
                                    trigger: 'change'
                                },
                        ],
                            roadName: [
                                {
                                    required: true,
                                    message: '请选择',
                                    trigger: 'change'
                                },
                        ],
                            routeName: [
                                {
                                    required: true,
                                    message: '请选择',
                                    trigger: 'change'
                                },
                        ],
                            position: [
                                {
                                    required: true,
                                    message: '请选择',
                                    trigger: 'change'
                                },
                        ],
                            pileNumber: [
                                {
                                    required: true,
                                    message: '请输入',
                                    trigger: 'blur'
                                },
                        ],
                            direction: [
                                {
                                    required: true,
                                    message: '请选择',
                                    trigger: 'change'
                                },
                        ],
                            directiondriving: [
                                {
                                    required: true,
                                    message: '请输入',
                                    trigger: 'blur'
                                },
                        ],
                            entrance: [
                                {
                                    required: true,
                                    message: '请选择',
                                    trigger: 'change'
                                },
                        ],
                            export: [
                                {
                                    required: true,
                                    message: '请选择',
                                    trigger: 'change'
                                },
                        ],
                            completiontime: [
                                {
                                    required: true,
                                    message: '请选择',
                                    trigger: 'change'
                                },
                        ],
                            longitude: [
                                {
                                    required: true,
                                    message: '请输入',
                                    trigger: 'blur'
                                },
                        ],
                            latitude: [
                                {
                                    required: true,
                                    message: '请输入',
                                    trigger: 'blur'
                                },
                        ],
            },
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
            childIndex:-1,
            isEdit:false,
            interfaceRes: {
                        name:[] ,
                        code:[] ,
                        managementunit:[] ,
                        businessunit:[] ,
                        city:[] ,
                        county:[] ,
                        districtCode:[] ,
                        address:[] ,
                        type:[] ,
                        highwayType:[] ,
                        roadName:[] ,
                        routeName:[] ,
                        roadCode:[] ,
                        routeCode:[] ,
                        position:[] ,
                        pileNumber:[] ,
                        direction:[] ,
                        directiondriving:[] ,
                        entrance:[] ,
                        export:[] ,
                        adjacentprovinces:[] ,
                        completiontime:[] ,
                        longitude:[] ,
                        latitude:[] ,
                        remarks:[] ,
                        picture:[] ,
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
            //定位属性
            locationScope: {
                            address: [],
                },
            }
        },
        computed: {
            ...mapGetters(['userInfo']),


        },
        watch: {},
        created() {
            this.dataAll()
            this.initDefaultData()
            this.dataValueAll = JSON.parse(JSON.stringify(this.dataForm))
        },
        mounted() {},
        methods: {
            prev() {
                this.index--
                if (this.index === 0) {
                    this.prevDis = true
                }
                this.nextDis = false
                for (let index = 0; index < this.allList.length; index++) {
                    const element = this.allList[index];
                    if (this.index == index) {
                        this.getInfo(element.id)
                    }
                }
            },
            next() {
                this.index++
                if (this.index === this.allList.length - 1) {
                    this.nextDis = true
                }
                this.prevDis = false
                for (let index = 0; index < this.allList.length; index++) {
                    const element = this.allList[index];
                    if (this.index == index) {
                        this.getInfo(element.id)
                    }
                }
            },
            getInfo(id) {
                request({
                    url: '/api/gas/GasBaseService/'+ id,
                    method: 'get'
                }).then(res => {
                    this.dataInfo(res.data)
                });
            },
            goBack() {
                this.visible = false
                this.$emit('refreshDataList', true)
            },
            changeData(model, index) {
                this.isEdit = false
                this.childIndex = index
                let modelAll = model.split("-");
                let faceMode = "";
                for (let i = 0; i < modelAll.length; i++) {
                    faceMode += modelAll[i];
                }
                for (let key in this.interfaceRes) {
                    if (key != faceMode) {
                        let faceReList = this.interfaceRes[key]
                        for (let i = 0; i < faceReList.length; i++) {
                            if (faceReList[i].relationField == model) {
                                let options = 'get' + key + 'Options';
                                if(this[options]){
                                    this[options]()
                                }
                                this.changeData(key, index)
                            }
                        }
                    }
                }
            },
            changeDataFormData(type, data, model,index,defaultValue,edit) {
                if(!edit) {
                    if (type == 2) {
                        for (let i = 0; i < this.dataForm[data].length; i++) {
                            if (index == -1) {
                                this.dataForm[data][i][model] = defaultValue
                            } else if (index == i) {
                                this.dataForm[data][i][model] = defaultValue
                            }
                        }
                    } else {
                        this.dataForm[data] = defaultValue
                    }
                }
            },
            dataAll(){
                        this.gettypeOptions();
                        this.gethighwayTypeOptions();
                        this.getroadNameOptions();
                        this.getrouteNameOptions();
                        this.getpositionOptions();
                        this.getdirectionOptions();
                        this.getentranceOptions();
                        this.getexportOptions();
            },
                    gettypeOptions() {
                    getDictionaryDataSelector('562362879907471045').then(res => {
                        this.typeOptions = res.data.list
                    })
                },
                    gethighwayTypeOptions() {
                    getDictionaryDataSelector('562362704245825221').then(res => {
                        this.highwayTypeOptions = res.data.list
                    })
                },
                    getroadNameOptions() {
                    const edit = this.isEdit
                    const index = this.childIndex
                    let templateJsonList = JSON.parse(JSON.stringify(this.interfaceRes.roadName))
                    for (let i = 0; i < templateJsonList.length; i++) {
                        let json = templateJsonList[i];
                        if(json.relationField){
                            let relationFieldAll = json.relationField.split("-");
                            let val = json.defaultValue;
                            if(relationFieldAll.length>1 && index>-1){
                                val = this.dataForm[relationFieldAll[0]+'List']&&this.dataForm[relationFieldAll[0]+'List'].length?this.dataForm[relationFieldAll[0]+'List'][index][relationFieldAll[1]]:''
                            }else {
                                val = this.dataForm[relationFieldAll]
                            }
                            json.defaultValue = val
                        }
                    }
                    let template ={
                        paramList:templateJsonList
                    }
                    getDataInterfaceRes('562605106210545541',template).then(res => {
                        let data = res.data
                        this.roadNameOptions = data
                        this.changeDataFormData(1,'roadName','roadName',index,'',edit)
                    })
                },
                    getrouteNameOptions() {
                    const edit = this.isEdit
                    const index = this.childIndex
                    let templateJsonList = JSON.parse(JSON.stringify(this.interfaceRes.routeName))
                    for (let i = 0; i < templateJsonList.length; i++) {
                        let json = templateJsonList[i];
                        if(json.relationField){
                            let relationFieldAll = json.relationField.split("-");
                            let val = json.defaultValue;
                            if(relationFieldAll.length>1 && index>-1){
                                val = this.dataForm[relationFieldAll[0]+'List']&&this.dataForm[relationFieldAll[0]+'List'].length?this.dataForm[relationFieldAll[0]+'List'][index][relationFieldAll[1]]:''
                            }else {
                                val = this.dataForm[relationFieldAll]
                            }
                            json.defaultValue = val
                        }
                    }
                    let template ={
                        paramList:templateJsonList
                    }
                    getDataInterfaceRes('562605691408228229',template).then(res => {
                        let data = res.data
                        this.routeNameOptions = data
                        this.changeDataFormData(1,'routeName','routeName',index,'',edit)
                    })
                },
                    getpositionOptions() {
                    getDictionaryDataSelector('562364050713878213').then(res => {
                        this.positionOptions = res.data.list
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
                goBack() {
                this.$emit('refresh')
            },
            clearData(){
                this.dataForm = JSON.parse(JSON.stringify(this.dataValueAll))
            },
            init(id,isDetail,allList,leftTreeActiveInfo) {
                this.prevDis = false
                this.nextDis = false
                this.allList = allList || []
                if (allList.length) {
                    this.index = this.allList.findIndex(item => item.id === id)
                    if (this.index == 0) {
                        this.prevDis = true
                    }
                    if (this.index == this.allList.length - 1) {
                        this.nextDis = true
                    }
                } else {
                    this.prevDis = true
                    this.nextDis = true
                }
                this.dataForm.id = id || 0;
                this.visible = true;
                this.$nextTick(() => {
                    if(this.dataForm.id){
                        this.loading = true
                        request({
                            url: '/api/gas/GasBaseService/'+this.dataForm.id,
                            method: 'get'
                        }).then(res => {
                            this.dataInfo(res.data)
                            this.loading = false
                        });
                    }else{
                        this.clearData()
                        this.initDefaultData()
                        this.dataForm = { ...this.dataForm, ...leftTreeActiveInfo }
                    }
                });
                this.$store.commit('generator/UPDATE_RELATION_DATA', {})
            },
            //初始化默认数据
            initDefaultData() {
            this.dataForm.completiontime = new Date().getTime()

            },
            // 表单提交
            dataFormSubmit(type) {
                this.dataFormSubmitType = type ? type : 0
                this.$refs['formRef'].validate((valid) => {
                    if (valid) {
                        this.request()
                    }
                })
            },
            request() {
                let _data =this.dataList()
                if (this.dataFormSubmitType == 2) {
                    this.continueBtnLoading = true
                } else {
                    this.btnLoading = true
                }
                if (!this.dataForm.id) {
                    request({
                        url: '/api/gas/GasBaseService',
                        method: 'post',
                        data: _data
                    }).then((res) => {
                        this.$message({
                            message: res.msg,
                            type: 'success',
                            duration: 1000,
                            onClose: () => {
                                if (this.dataFormSubmitType == 2) {
                                    this.$nextTick(() => {
                                        this.clearData()
                                        this.initDefaultData()
                                    })
                                    this.continueBtnLoading = false
                                    return
                                }
                                this.visible = false
                                this.btnLoading = false
                                this.$emit('refresh', true)
                            }
                        })
                    }).catch(()=>{
                        this.btnLoading = false
                        this.continueBtnLoading = false
                    })
                }else{
                    request({
                        url: '/api/gas/GasBaseService/'+this.dataForm.id,
                        method: 'PUT',
                        data: _data
                    }).then((res) => {
                        this.$message({
                            message: res.msg,
                            type: 'success',
                            duration: 1000,
                            onClose: () => {
                                if (this.dataFormSubmitType == 2) return this.continueBtnLoading = false
                                this.visible = false
                                this.btnLoading = false
                                this.$emit('refresh', true)
                            }
                        })
                    }).catch(()=>{
                        this.btnLoading = false
                        this.continueBtnLoading = false
                    })
                }
            },
            openSelectDialog(key,value) {
                this.currTableConf=this.addTableConf[key + value]
                this.currVmodel=key
                this.selectDialogVisible = true
                this.$nextTick(() => {
                    this.$refs.selectDialog.init()
                })
            },
            addForSelect(data) {
                this.closeForSelect()
                for (let i = 0; i < data.length; i++) {
                    let t = data[i]
                    if(this['get'+this.currVmodel]){
                        this['get'+this.currVmodel](t,true)
                    }
                }
            },
            closeForSelect() {
                this.selectDialogVisible = false
            },
            dateTime(timeRule, timeType, timeTarget, timeValueData, dataValue) {
                let timeDataValue = null;
                let timeValue = Number(timeValueData)
                if (timeRule) {
                    if (timeType == 1) {
                        timeDataValue = timeValue
                    } else if (timeType == 2) {
                        timeDataValue = dataValue
                    } else if (timeType == 3) {
                        timeDataValue = new Date().getTime()
                    } else if (timeType == 4) {
                        let previousDate = '';
                        if (timeTarget == 1 || timeTarget == 2) {
                            previousDate = getDateDay(timeTarget, timeType, timeValue)
                            timeDataValue = new Date(previousDate).getTime()
                        } else if (timeTarget == 3) {
                            previousDate = getBeforeData(timeValue)
                            timeDataValue = new Date(previousDate).getTime()
                        } else {
                            timeDataValue = getBeforeTime(timeTarget, timeValue).getTime()
                        }
                    } else if (timeType == 5) {
                        let previousDate = '';
                        if (timeTarget == 1 || timeTarget == 2) {
                            previousDate = getDateDay(timeTarget, timeType, timeValue)
                            timeDataValue = new Date(previousDate).getTime()
                        } else if (timeTarget == 3) {
                            previousDate = getLaterData(timeValue)
                            timeDataValue = new Date(previousDate).getTime()
                        } else {
                            timeDataValue = getLaterTime(timeTarget, timeValue).getTime()
                        }
                    }
                }
                return timeDataValue;
            },
            time(timeRule, timeType, timeTarget, timeValue, formatType, dataValue) {
                let format = formatType == 'HH:mm' ? 'HH:mm:00' : formatType
                let timeDataValue = null
                if (timeRule) {
                    if (timeType == 1) {
                        timeDataValue = timeValue || '00:00:00'
                        if (timeDataValue.split(':').length == 3) {
                            timeDataValue = timeDataValue
                        } else {
                            timeDataValue = timeDataValue + ':00'
                        }
                    } else if (timeType == 2) {
                        timeDataValue = dataValue
                    } else if (timeType == 3) {
                        timeDataValue = this.jnpf.toDate(new Date(), format)
                    } else if (timeType == 4) {
                        let previousDate = '';
                        previousDate = getBeforeTime(timeTarget, timeValue)
                        timeDataValue = this.jnpf.toDate(previousDate, format)
                    } else if (timeType == 5) {
                        let previousDate = '';
                        previousDate = getLaterTime(timeTarget, timeValue)
                        timeDataValue = this.jnpf.toDate(previousDate, format)
                    }
                }
                return timeDataValue;
            },
            dataList(){
                var _data = this.dataForm;
                return _data;
            },
            dataInfo(dataAll){
                let _dataAll =dataAll
                this.dataForm = _dataAll
                this.isEdit = true
                this.dataAll()
                this.childIndex=-1
            },
        },
    }

</script>
