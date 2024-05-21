<template>
  <el-dialog :title="$t(`user.resetPassword`)" :close-on-click-modal="false"
    :close-on-press-escape="false" :visible.sync="visible" lock-scroll
    class="JNPF-dialog JNPF-dialog_center" width="600px">
    <el-form ref="dataForm" v-loading="formLoading" :model="dataForm" :rules="dataRule"
      label-width="100px">
      <el-form-item label="旧密码" prop="oldPassword">
        <el-input v-model="dataForm.oldPassword" placeholder="旧密码" show-password />
      </el-form-item>
      <el-form-item label="新密码" prop="password">
        <el-input v-model="dataForm.password" placeholder="新密码" show-password />
      </el-form-item>
      <el-form-item label="重复密码" prop="password2">
        <el-input v-model="dataForm.password2" placeholder="重复密码" show-password />
      </el-form-item>
      <el-form-item label="验证码" prop="code">
        <el-col :span="17">
          <el-input v-model="dataForm.code" placeholder="验证码">
          </el-input>
        </el-col>
        <el-col :span="6" :offset="1" style="height:32px">
          <img id="imgcode" alt="点击切换验证码" title="点击切换验证码" :src="define.comUrl+imgUrl"
               @click="changeImg">
        </el-col>
      </el-form-item>
    </el-form>
    <span slot="footer" class="dialog-footer">
      <el-button @click="visible = false">{{$t('common.cancelButton')}}</el-button>
      <el-button type="primary" :loading="btnLoading" @click="dataFormSubmit()">
        {{$t('common.confirmButton')}}</el-button>
    </span>
  </el-dialog>
</template>

<script>
import { UpdatePassword } from '@/api/permission/userSetting'
import md5 from 'js-md5'
import { getSystemConfig } from '@/api/system/sysConfig'

export default {
  data() {
    const validatePass = (rule, value, callback) => {
      //是否包含数字
      const containsNumbers = /[0-9]+/
      //是否包含小写字符
      const includeLowercaseLetters = /[a-z]+/
      //是否包含大写字符
      const includeUppercaseLetters = /[A-Z]+/
      //是否包含字符
      const containsCharacters = /\W/
      const containsCharacters2 = /_/

      if (value === '') {
        callback(new Error('新密码不能为空'));
      } else if(this.baseForm.passwordStrengthLimit == 1){
        if(this.baseForm.passwordLengthMin){
          if(value.length<this.baseForm.passwordLengthMinNumber){
            callback(new Error('新密码长度不能小于'+this.baseForm.passwordLengthMinNumber+'位'));
          }
        }
        if(this.baseForm.containsNumbers){
          if (!containsNumbers.test(value)) {
            callback(new Error('新密码必须包含数字'));
          }
        }
        if(this.baseForm.includeLowercaseLetters){
          if (!includeLowercaseLetters.test(value)) {
            callback(new Error('新密码必须包含小写字母'));
          }
        }
        if(this.baseForm.includeUppercaseLetters){
          if (!includeUppercaseLetters.test(value)) {
            callback(new Error('新密码必须包含大写字母'));
          }
        }
        if(this.baseForm.containsCharacters){
          if (!containsCharacters.test(value) && !containsCharacters2.test(value)) {
            callback(new Error('新密码必须包含字符'));
          }
        }
        if (this.dataForm.password2 !== '') {
          this.$refs.dataForm.validateField('password2');
        }
        callback();
      }else{
        if (this.dataForm.password2 !== '') {
          this.$refs.dataForm.validateField('password2');
        }
        callback();
      }
    }
    var validatePass2 = (rule, value, callback) => {
      if (value !== this.dataForm.password) {
        callback(new Error('两次密码输入不一致'));
      } else {
        callback();
      }
    };
    return {
      visible: false,
      formLoading: false,
      btnLoading: false,
      imgUrl: "",
      timestamp: '',
      dataForm: {
        oldPassword: '',
        password: '',
        password2: '',
        code: '',
      },
      baseForm:{
        passwordStrengthLimit:0,
        passwordLengthMin:false,
        passwordLengthMinNumber:0,
        containsNumbers:false,
        includeLowercaseLetters:false,
        includeUppercaseLetters:false,
        containsCharacters:false,
      },
      dataRule: {
        oldPassword: [
          { required: true, message: '旧密码不能为空', trigger: 'blur' }
        ],
        password: [
          { required: true, validator: validatePass, trigger: 'blur' }
        ],
        password2: [
          { required: true, message: '重复密码不能为空', trigger: 'blur' },
          { validator: validatePass2, trigger: 'blur' }
        ],
        code: [
          { required: true, message: '验证码不能为空', trigger: 'blur' }
        ],
      }
    }
  },
  created() {
    this.changeImg(),
    this.initData()
  },
  methods: {
    initData() {
      this.listLoading = true
      this.$nextTick(() => {
        getSystemConfig().then(res => {
          this.baseForm = res.data
          this.baseForm.passwordLengthMin = this.baseForm.passwordLengthMin ? true : false
          this.baseForm.containsNumbers = this.baseForm.containsNumbers ? true : false
          this.baseForm.includeLowercaseLetters = this.baseForm.includeLowercaseLetters ? true : false
          this.baseForm.includeUppercaseLetters = this.baseForm.includeUppercaseLetters ? true : false
          this.baseForm.containsCharacters = this.baseForm.containsCharacters ? true : false
          this.listLoading = false
        }).catch(() => {
          this.listLoading = false
        })
      })
    },
    init(id, account) {
      this.visible = true
      this.formLoading = true
      this.$nextTick(() => {
        this.$refs['dataForm'].resetFields()
        this.dataForm.id = id
        this.dataForm.account = account
        this.formLoading = false
      })
    },
    dataFormSubmit() {
      this.$refs['dataForm'].validate((valid) => {
        if (valid) {
          this.btnLoading = true
          let query = {
            oldPassword: md5(this.dataForm.oldPassword),
            password: md5(this.dataForm.password),
            code: this.dataForm.code,
            timestamp: this.timestamp
          }
          UpdatePassword(query).then(res => {
            this.$message({
              message: res.msg,
              type: 'success',
              duration: 1500,
              onClose: () => {
                this.visible = false
                this.btnLoading = false
                this.$emit('refreshDataList')
              }
            })
          }).catch(() => {
            this.btnLoading = false
          })
        }
      })
    },
    changeImg() {
      let timestamp = Math.random()
      this.timestamp = timestamp
      this.imgUrl = `/api/file/ImageCode/${timestamp}`
    }
  }
}
</script>
