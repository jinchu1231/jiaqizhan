<template>
  <div class="password">
    <div class="JNPF-common-title mb-20">
      <h2 class="bold">修改密码</h2>
    </div>
    <el-row>
      <el-col :span="12">
        <el-form ref="dataForm" :model="dataForm" :rules="dataRule" label-width="100px">
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
          <el-form-item>
            <el-button type="primary" @click="dataFormSubmit()">保 存</el-button>
          </el-form-item>
        </el-form>
      </el-col>
    </el-row>

  </div>
</template>

<script>
import md5 from 'js-md5';
import { UpdatePassword } from '@/api/permission/userSetting'
import { getSystemConfig } from '@/api/system/sysConfig'
export default {
  data() {
    var validatePass = (rule, value, callback) => {
      // const passwordreg = /(?=.*\d)(?=.*[a-zA-Z])(?=.*[^a-zA-Z0-9]).{6,16}/
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
    };

    //   if (value === '') {
    //     callback(new Error('新密码不能为空'));
    //   } else if (!passwordreg.test(value)) {
    //     callback(new Error('密码必须由数字、字母、特殊字符组合,请输入6-16位'))
    //   } else {
    //     if (this.dataForm.password2 !== '') {
    //       this.$refs.dataForm.validateField('password2');
    //     }
    //     callback();
    //   }
    // };
    var validatePass2 = (rule, value, callback) => {
      if (value !== this.dataForm.password) {
        callback(new Error('两次密码输入不一致'));
      } else {
        callback();
      }
    };
    return {
      listLoading: false,
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
      imgUrl: "",
      timestamp: '',
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
    dataFormSubmit() {
      this.$refs['dataForm'].validate((valid) => {
        if (valid) {
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
                this.$store.dispatch('user/resetToken').then(() => {
                  this.$router.push(`/login`)
                })
              }
            })
          }).catch(() => {
            this.changeImg()
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
<style lang="scss" scoped>
.password {
  >>> .el-input-group__append {
    padding: 0;
    height: 30px;
  }
  #imgcode {
    width: 100px;
    height: 32px;
    overflow: hidden;
    object-fit: cover;
    cursor: pointer;
    margin: 0;
    padding: 0;
  }
}
</style>
