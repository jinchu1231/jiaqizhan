<template>
  <div class="userInfo">
    <el-tabs class="JNPF-el_tabs">
      <el-tab-pane label="账户信息">
        <el-col :span="12">
          <el-form :model="form" label-width="100px">
            <el-form-item label="账户">
              <el-input v-model="form.account" readonly />
            </el-form-item>
            <el-form-item label="所属组织">
              <el-input v-model="form.organize" readonly />
            </el-form-item>
            <el-form-item label="直属主管">
              <el-input v-model="form.manager" readonly />
            </el-form-item>
            <el-form-item label="岗位">
              <el-input v-model="form.position" readonly />
            </el-form-item>
            <el-form-item label="职级">
              <el-input v-model="form.ranks" readonly />
            </el-form-item>
            <el-form-item label="角色">
              <el-input v-model="form.roleId" readonly />
            </el-form-item>
            <el-form-item label="注册时间">
              <el-input v-model="creatorTime" readonly />
            </el-form-item>
            <el-form-item label="上次登录">
              <el-input v-model="prevLogTime" readonly />
            </el-form-item>
            <el-form-item label="入职日期">
              <el-input v-model="entryDate" readonly />
            </el-form-item>
          </el-form>
        </el-col>
      </el-tab-pane>
      <el-tab-pane label="个人资料">
        <el-form ref="dataForm" :model="form2" :rules="form2Rule" label-width="100px">
          <el-row>
            <el-col :span="12">
              <el-form-item label="姓名" prop="realName">
                <el-input v-model="form2.realName" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="性别">
                <el-select v-model="form2.gender" placeholder="选择性别">
                  <el-option v-for="item in genderOptions" :key="item.enCode" :label="item.fullName"
                    :value="item.enCode" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="民族">
                <el-select v-model="form2.nation" placeholder="选择民族" filterable>
                  <el-option v-for="item in nationOptions" :key="item.id" :label="item.fullName"
                    :value="item.id" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="籍贯">
                <el-input v-model="form2.nativePlace" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="证件类型">
                <el-select v-model="form2.certificatesType" placeholder="请选择类型" filterable>
                  <el-option v-for="item in certificatesTypeOptions" :key="item.id"
                    :label="item.fullName" :value="item.id">
                  </el-option>
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="证件号码">
                <el-input v-model="form2.certificatesNumber" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="文化程度">
                <el-select v-model="form2.education" placeholder="请选择学历" filterable>
                  <el-option v-for="item in educationOptions" :key="item.id" :label="item.fullName"
                    :value="item.id">
                  </el-option>
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="出生年月">
                <el-date-picker v-model="form2.birthday" type="date" placeholder="选择日期"
                  value-format="timestamp" format="yyyy-MM-dd"></el-date-picker>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="办公电话">
                <el-input v-model="form2.telePhone" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="办公座机">
                <el-input v-model="form2.landline" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="手机号码">
                <el-input v-model="form2.mobilePhone" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="电子邮箱">
                <el-input v-model="form2.email" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="紧急联系">
                <el-input v-model="form2.urgentContacts" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="紧急电话">
                <el-input v-model="form2.urgentTelePhone" />
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="通讯地址">
                <el-input v-model="form2.postalAddress" />
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="自我介绍">
                <el-input v-model="form2.signature" type="textarea" :rows="3" />
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item>
                <el-button type="primary" @click="dataFormSubmit()">保 存</el-button>
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </el-tab-pane>
      <el-tab-pane label="个人签名">
        <el-row class="sign" :gutter="40">
          <div @click="addSign" style="cursor : pointer">
            <el-col :span="6" class="sign-item">
              <div class='add-sign'>
                <i class="add-icon el-icon-plus"></i>
              </div>
            </el-col>
          </div>
          <template v-for="(item,i) in signList">
            <el-col :span="6" class="sign-item" :key="i">
              <div :class="item.isDefault?'add-sign active':'add-sign'">
                <img :src="item.signImg" alt="" class="sign-img">
                <div class="icon-checked1" v-if="item.isDefault">
                  <i class=" el-icon-check"></i>
                </div>
                <div v-if="!item.isDefault" class="add-button">
                  <el-button @click="deleteSign(item.id)">删除</el-button>
                  <el-button type="primary" @click="updateDefault(item.id,item.signImg)">设为默认
                  </el-button>
                </div>
              </div>
            </el-col>
          </template>
        </el-row>
      </el-tab-pane>
    </el-tabs>
    <SignImgDialog v-if="signVisible" ref="SignImg" :lineWidth='3' :userInfo='userInfo'
      :isDefault='0' @close="closeDialog" />
  </div>
</template>
<script>
import { UpdateUser, getSign, deleteSign, updateDefault } from '@/api/permission/userSetting'
import { mapGetters } from "vuex";
import SignImgDialog from '@/components/SignImgDialog'
export default {
  components: { SignImgDialog },
  props: {
    user: {
      type: Object,
      default: () => {
        return {}
      }
    }
  },
  data() {
    return {
      form: {},
      form2: {
        realName: '',
        signature: '',
        gender: 1,
        nation: '',
        nativePlace: '',
        certificatesType: '',
        certificatesNumber: '',
        education: '',
        birthday: null,
        telePhone: '',
        landline: '',
        mobilePhone: '',
        email: '',
        urgentContacts: '',
        urgentTelePhone: '',
        postalAddress: '',

      },
      form2Rule: {
        realName: [
          { required: true, message: '姓名不能为空', trigger: 'blur' }
        ]
      },
      certificatesTypeOptions: [],
      educationOptions: [],
      genderOptions: [],
      nationOptions: [],
      signVisible: false,
      signImg: '',
      signList: []
    }
  },
  computed: {
    creatorTime() {
      return this.jnpf.toDate(this.form.creatorTime)
    },
    entryDate() {
      return this.jnpf.toDate(this.form.entryDate)
    },
    prevLogTime() {
      return this.jnpf.toDate(this.form.prevLogTime)
    },
    ...mapGetters(['userInfo'])
  },
  created() {
    this.getOptions()
    this.getInfo()
    this.getSign()
  },
  methods: {
    closeDialog() {
      this.signVisible = false
      this.getSign()
    },
    getSign() {
      getSign().then(res => {
        this.signList = res.data || []
      })
    },
    deleteSign(id) {
      deleteSign(id).then(res => {
        this.$message({
          message: res.msg,
          type: 'success',
          duration: 1500
        })
        this.getSign()
      })
    },
    updateDefault(id, signImg) {
      updateDefault(id).then(res => {
        this.$message({
          message: res.msg,
          type: 'success',
          duration: 1500
        })
        this.$store.commit('user/SET_USERINFO_SIGNIMG', signImg)
        this.getSign()
      }).catch(err => {
        this.getSign()
      })
    },
    addSign() {
      this.signVisible = true
      this.$nextTick(() => {
        this.$refs.SignImg.init()
      })
    },
    getOptions() {
      this.$store.dispatch('base/getDictionaryData', { sort: 'Education' }).then((res) => {
        this.educationOptions = JSON.parse(JSON.stringify(res))
        this.$store.dispatch('base/getDictionaryData', { sort: 'certificateType' }).then((res) => {
          this.certificatesTypeOptions = JSON.parse(JSON.stringify(res))
        })
        this.$store.dispatch('base/getDictionaryData', { sort: 'sex' }).then(res => {
          this.genderOptions = JSON.parse(JSON.stringify(res))
        })
        this.$store.dispatch('base/getDictionaryData', { sort: 'Nation' }).then(res => {
          this.nationOptions = JSON.parse(JSON.stringify(res))
        })
      })
    },
    getInfo() {
      this.form = this.user
      for (let key of Object.keys(this.form2)) {
        this.form2[key] = this.form[key]
      }
    },
    dataFormSubmit() {
      this.$refs['dataForm'].validate((valid) => {
        if (!valid) return
        UpdateUser(this.form2).then(res => {
          this.$message({
            message: res.msg,
            type: 'success',
            duration: 1500
          })
          this.$emit('updateInfo')
          this.$store.commit('user/SET_USERINFO_USERNAME', this.form2.realName)
        })
      })
    }
  }
}
</script>
<style lang="scss" scoped>
.userInfo {
  height: 100%;
  overflow: hidden;
  >>> .el-tabs__nav-scroll {
    padding-top: 0 !important;
  }
}
.sign {
  padding: 20px 50px 0px 50px;
}
.add-sign {
  position: relative;
  height: 160px;
  background-color: rgb(247, 247, 247);
  border-radius: 10px;
  display: flex;
  justify-content: center;
  align-items: center;
  &.active {
    border: 1px solid #1890ff;
    box-shadow: 0 0 6px rgba(6, 58, 108, 0.26);
    color: #1890ff;
    .btn,
    .icon-checked {
      display: block;
    }
  }
  .add-button {
    position: absolute;
    display: none;
  }
  .add-icon {
    font-size: 50px;
    color: rgb(157, 158, 159);
  }
  .sign-img {
    width: 100%;
    height: 100%;
  }
}
.add-sign:hover .add-button {
  display: flex;
  width: 100%;
  height: 100%;
  border-radius: 10px;
  background-color: rgba(157, 158, 159, 0.8);

  justify-content: center;
  align-items: center;
}
.icon-checked1 {
  display: block;
  width: 20px;
  height: 20px;
  border: 20px solid #1890ff;
  border-left: 20px solid transparent;
  border-top: 20px solid transparent;
  border-bottom-right-radius: 10px;
  position: absolute;
  transform: scale(0.8);
  right: -5px;
  bottom: -5px;
  i {
    position: absolute;
    top: -4px;
    left: -4px;
    font-size: 24px;
    color: #fff;
    transform: scale(0.8);
  }
}
.sign-item {
  margin-bottom: 20px;
}
</style>