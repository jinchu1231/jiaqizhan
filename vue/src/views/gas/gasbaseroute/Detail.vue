<template>
<el-dialog title="详情"
           :close-on-click-modal="false" append-to-body
           :visible.sync="visible" class="JNPF-dialog JNPF-dialog_center" lock-scroll
           width="600px">
<el-row :gutter="15" class="">
<el-form ref="formRef" :model="dataForm" size="small" label-width="100px" label-position="right" >
    <template v-if="!loading">
            <el-col :span="24"  >
                <jnpf-form-tip-item  label="路线名称"  
 prop="name"  >
                    <JnpfInput    v-model="dataForm.name"
 placeholder="请输入"  disabled
 detailed  clearable  :style='{"width":"100%"}' :maskConfig = "maskConfig.name">
                </JnpfInput>
                </jnpf-form-tip-item>
            </el-col>
            <el-col :span="24"  >
                <jnpf-form-tip-item  label="路线编码"  
 prop="code"  >
                        <p>{{dataForm.code}}</p>
                </jnpf-form-tip-item>
            </el-col>
    </template>
</el-form>
    </el-row>
    <span slot="footer" class="dialog-footer">
        <el-button @click="visible = false"> 取 消</el-button>
    </span>
    <Detail v-if="detailVisible" ref="Detail" @close="detailVisible = false" />
    </el-dialog>
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
                },
                //定位属性
                locationScope: {
                },

            dataForm: {

            },

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

            init(id) {
                this.dataForm.id = id || 0;
                this.visible = true;
                this.$nextTick(() => {
                    if(this.dataForm.id){
                        this.loading = true
                        request({
                            url: '/api/gas/GasBaseRoute/detail/'+this.dataForm.id,
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
