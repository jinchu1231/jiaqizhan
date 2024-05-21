<template>
<transition name="el-zoom-in-center">
<div class="JNPF-preview-main">
    <Detail v-if="detailVisible" ref="Detail" @close="detailVisible = false" />
    <div class="JNPF-common-page-header">
        <el-page-header @back="goBack"
                        content="详情"/>
        <div class="options">
            <el-button @click="goBack">取 消</el-button>
        </div>
    </div>
<el-row :gutter="15" class=" main" :style="{margin: '0 auto',width: '100%'}">
<el-form ref="formRef" :model="dataForm" size="small" label-width="100px" label-position="right" >
    <template v-if="!loading">
            <el-col :span="12"  >
                <jnpf-form-tip-item  label="服务区名"  
 prop="name"  >
                    <JnpfInput    v-model="dataForm.name"
 placeholder="请输入"  disabled
 detailed  clearable  :style='{"width":"100%"}' :maskConfig = "maskConfig.name">
                </JnpfInput>
                </jnpf-form-tip-item>
            </el-col>
            <el-col :span="12"  >
                <jnpf-form-tip-item  label="服务区编码"  
 prop="code"  >
                        <p>{{dataForm.code}}</p>
                </jnpf-form-tip-item>
            </el-col>
            <el-col :span="12"  >
                <jnpf-form-tip-item  label="管理单位"  
 prop="managementunit"  >
                        <p>{{dataForm.managementunit}}</p>
                </jnpf-form-tip-item>
            </el-col>
            <el-col :span="12"  >
                <jnpf-form-tip-item  label="经营单位"  
 prop="businessunit"  >
                        <p>{{dataForm.businessunit}}</p>
                </jnpf-form-tip-item>
            </el-col>
            <el-col :span="12"  >
                <jnpf-form-tip-item  label="所处地市"  
 prop="city"  >
                        <p>{{dataForm.city}}</p>
                </jnpf-form-tip-item>
            </el-col>
            <el-col :span="12"  >
                <jnpf-form-tip-item  label="所处区县"  
 prop="county"  >
                        <p>{{dataForm.county}}</p>
                </jnpf-form-tip-item>
            </el-col>
            <el-col :span="12"  >
                <jnpf-form-tip-item  label="行政区划"  
 prop="districtCode"  >
                    <JnpfInput    v-model="dataForm.districtCode"
 placeholder="请输入"  disabled
 detailed  clearable  :style='{"width":"100%"}' :maskConfig = "maskConfig.districtCode">
                </JnpfInput>
                </jnpf-form-tip-item>
            </el-col>
            <el-col :span="12"  >
                <jnpf-form-tip-item  label="服务区地址"  
 prop="address"  >
                    <JnpfLocation    v-model="dataForm.address"
 disabled
 detailed  clearable  :adjustmentScope = "500" >
                </JnpfLocation>
                </jnpf-form-tip-item>
            </el-col>
            <el-col :span="12"  >
                <jnpf-form-tip-item  label="服务类型"  
 prop="type"  >
                        <p>{{dataForm.type}}</p>
                </jnpf-form-tip-item>
            </el-col>
            <el-col :span="12"  >
                <jnpf-form-tip-item  label="公路类型"  
 prop="highwayType"  >
                        <p>{{dataForm.highwayType}}</p>
                </jnpf-form-tip-item>
            </el-col>
            <el-col :span="12"  >
                <jnpf-form-tip-item  label="路段名称"  
 prop="roadName"  >
                        <p>{{dataForm.roadName}}</p>
                </jnpf-form-tip-item>
            </el-col>
            <el-col :span="12"  >
                <jnpf-form-tip-item  label="路线名称"  
 prop="routeName"  >
                        <p>{{dataForm.routeName}}</p>
                </jnpf-form-tip-item>
            </el-col>
            <el-col :span="12"  >
                <jnpf-form-tip-item  label="路段编号"  
 prop="roadCode"  >
                    <JnpfInput    v-model="dataForm.roadCode"
 placeholder="请输入"  disabled
 detailed  clearable  :style='{"width":"100%"}' :maskConfig = "maskConfig.roadCode">
                </JnpfInput>
                </jnpf-form-tip-item>
            </el-col>
            <el-col :span="12"  >
                <jnpf-form-tip-item  label="路线编码"  
 prop="routeCode"  >
                    <JnpfInput    v-model="dataForm.routeCode"
 placeholder="请输入"  disabled
 detailed  clearable  :style='{"width":"100%"}' :maskConfig = "maskConfig.routeCode">
                </JnpfInput>
                </jnpf-form-tip-item>
            </el-col>
            <el-col :span="12"  >
                <jnpf-form-tip-item  label="服务位置"  
 prop="position"  >
                        <p>{{dataForm.position}}</p>
                </jnpf-form-tip-item>
            </el-col>
            <el-col :span="12"  >
                <jnpf-form-tip-item  label="桩号"  
 prop="pileNumber"  >
                    <JnpfInput    v-model="dataForm.pileNumber"
 placeholder="请输入"  disabled
 detailed  clearable  :style='{"width":"100%"}' :maskConfig = "maskConfig.pileNumber">
                </JnpfInput>
                </jnpf-form-tip-item>
            </el-col>
            <el-col :span="12"  >
                <jnpf-form-tip-item  label="方向"  
 prop="direction"  >
                        <p>{{dataForm.direction}}</p>
                </jnpf-form-tip-item>
            </el-col>
            <el-col :span="12"  >
                <jnpf-form-tip-item  label="行车方向"  
 prop="directiondriving"  >
                    <JnpfInput    v-model="dataForm.directiondriving"
 placeholder="请输入"  disabled
 detailed  clearable  :style='{"width":"100%"}' :maskConfig = "maskConfig.directiondriving">
                </JnpfInput>
                </jnpf-form-tip-item>
            </el-col>
            <el-col :span="12"  >
                <jnpf-form-tip-item  label="是否入省口"  
 prop="entrance"  >
                        <p>{{dataForm.entrance}}</p>
                </jnpf-form-tip-item>
            </el-col>
            <el-col :span="12"  >
                <jnpf-form-tip-item  label="是否出省口"  
 prop="export"  >
                        <p>{{dataForm.export}}</p>
                </jnpf-form-tip-item>
            </el-col>
            <el-col :span="12"  >
                <jnpf-form-tip-item  label="相邻省份"  
 prop="adjacentprovinces"  >
                        <p>{{dataForm.adjacentprovinces}}</p>
                </jnpf-form-tip-item>
            </el-col>
            <el-col :span="12"  >
                <jnpf-form-tip-item  label="建成时间"  
 prop="completiontime"  >
                        <p>{{dataForm.completiontime}}</p>
                </jnpf-form-tip-item>
            </el-col>
            <el-col :span="12"  >
                <jnpf-form-tip-item  label="经度"  
 prop="longitude"  >
                    <JnpfInput    v-model="dataForm.longitude"
 placeholder="请输入"  disabled
 detailed  clearable  :style='{"width":"100%"}' :maskConfig = "maskConfig.longitude">
                </JnpfInput>
                </jnpf-form-tip-item>
            </el-col>
            <el-col :span="12"  >
                <jnpf-form-tip-item  label="纬度"  
 prop="latitude"  >
                    <JnpfInput    v-model="dataForm.latitude"
 placeholder="请输入"  disabled
 detailed  clearable  :style='{"width":"100%"}' :maskConfig = "maskConfig.latitude">
                </JnpfInput>
                </jnpf-form-tip-item>
            </el-col>
            <el-col :span="24"  >
                <jnpf-form-tip-item  label="备注"  
 prop="remarks"  >
                        <p>{{dataForm.remarks}}</p>
                </jnpf-form-tip-item>
            </el-col>
            <el-col :span="24"  >
                <jnpf-form-tip-item  label="图片"  
 prop="picture"  >
                    <JnpfUploadImg    v-model="dataForm.picture"
 disabled
 detailed  :fileSize="10"  sizeUnit="MB"  :limit="9"  pathType="defaultPath"  :isAccount="0" >
                </JnpfUploadImg>
                </jnpf-form-tip-item>
            </el-col>
    </template>
</el-form>
    </el-row>
    </div>
</transition>
</template>
<script>
    import request from '@/utils/request'

    import { getConfigData } from '@/api/onlineDev/visualDev'
    import jnpf from '@/utils/jnpf'
    import Detail from '@/views/basic/dynamicModel/list/detail'
    import { thousandsFormat } from "@/components/Generator/utils/index"
    export default {
        components: { Detail},
        props: [],
        data() {
            return {
                visible: false,
                detailVisible: false,
                loading: false,

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

            dataForm: {

            },
                        typeProps:{"label":"fullName","value":"id"  },
                        highwayTypeProps:{"label":"fullName","value":"id"  },
                        roadNameProps:{"label":"name","value":"code"  },
                        routeNameProps:{"label":"name","value":"code"  },
                        positionProps:{"label":"fullName","value":"id"  },
                        directionProps:{"label":"fullName","value":"id"  },
                        entranceProps:{"label":"fullName","value":"id"  },
                        exportProps:{"label":"fullName","value":"id"  },

        }
        },
        computed: {},
        watch: {},
        created() {

        },
        mounted() {},
        methods: {
            toDetail(defaultValue, modelId) {
                if (!defaultValue) return
                getConfigData(modelId).then(res => {
                    if (!res.data || !res.data.formData) return
                    let formData = JSON.parse(res.data.formData)
                    formData.popupType = 'general'
                    this.detailVisible = true
                    this.$nextTick(() => {
                        this.$refs.Detail.init(formData, modelId, defaultValue)
                    })
                })
            },
            dataInfo(dataAll){
                let _dataAll =dataAll
                this.dataForm = _dataAll
            },

                goBack() {
                    this.$emit('refresh')
                },
            init(id) {
                this.dataForm.id = id || 0;
                this.visible = true;
                this.$nextTick(() => {
                    if(this.dataForm.id){
                        this.loading = true
                        request({
                            url: '/api/gas/GasBaseService/detail/'+this.dataForm.id,
                            method: 'get'
                        }).then(res => {
                            this.dataInfo(res.data)
                            this.loading = false
                        })
                    }

                })
            },
        },
    }

</script>
