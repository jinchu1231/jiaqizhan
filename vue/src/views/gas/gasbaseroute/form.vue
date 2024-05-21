


    <template>
        <el-dialog :title="!dataForm.id ? '新建' :'编辑'"
                   :close-on-click-modal="false" append-to-body
                   :visible.sync="visible" class="JNPF-dialog JNPF-dialog_center" lock-scroll
                   width="600px">
        <el-row :gutter="15" class="">
    <el-form ref="formRef" :model="dataForm" :rules="dataRule" size="small" label-width="100px" label-position="right" >
    <template v-if="!loading">
        <!-- 具体表单 -->
        <el-col :span="24" >
        <jnpf-form-tip-item
 label="路线名称"              align="left"
 prop="name" >
        <JnpfInput   v-model="dataForm.name" @change="changeData('name',-1)" 
 placeholder="请输入"  clearable  :style='{"width":"100%"}' :maskConfig = "maskConfig.name">
    </JnpfInput>
    </jnpf-form-tip-item>
        </el-col>
        <el-col :span="24" >
        <jnpf-form-tip-item
 label="路线编码"              align="left"
 prop="code" >
        <JnpfInput   v-model="dataForm.code" @change="changeData('code',-1)" 
 placeholder="系统自动生成"  readonly  :style='{"width":"100%"}'>
    </JnpfInput>
    </jnpf-form-tip-item>
        </el-col>
        <!-- 表单结束 -->
    </template>
    </el-form>
    <SelectDialog v-if="selectDialogVisible" :config="currTableConf" :formData="dataForm"
                  ref="selectDialog" @select="addForSelect" @close="closeForSelect"/>
    </el-row>
    <span slot="footer" class="dialog-footer">
                    <div class="upAndDown-button" v-if="dataForm.id">
                <el-button @click="prev" :disabled='prevDis'>
                  {{'上一条'}}
                </el-button>
                <el-button @click="next" :disabled='nextDis'>
                  {{'下一条'}}
                </el-button>
              </div>
                <el-button type="primary" @click="dataFormSubmit(2)" :loading="continueBtnLoading">
            {{!dataForm.id ?'确定并新增':'确定并继续'}}</el-button>
              <el-button @click="visible = false"> 取 消</el-button>
              <el-button type="primary" @click="dataFormSubmit()" :loading="btnLoading"> 确 定</el-button>
        </span>
    </el-dialog>
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
            },
            childIndex:-1,
            isEdit:false,
            interfaceRes: {
                        name:[] ,
                        code:[] ,
        },
            //掩码配置
            maskConfig: {
                            name: {"prefixType":1,"useUnrealMask":false,"maskType":1,"unrealMaskLength":1,"prefixLimit":0,"suffixLimit":0,"filler":"*","prefixSpecifyChar":"","suffixType":1,"ignoreChar":"","suffixSpecifyChar":""} ,
                },
            //定位属性
            locationScope: {
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
                    url: '/api/gas/GasBaseRoute/'+ id,
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
                            url: '/api/gas/GasBaseRoute/'+this.dataForm.id,
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
                        url: '/api/gas/GasBaseRoute',
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
                        url: '/api/gas/GasBaseRoute/'+this.dataForm.id,
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
