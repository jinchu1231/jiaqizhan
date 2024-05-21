<template>
  <div class="login-container">
    <div class="login-version" v-if="sysConfig && sysConfig.sysVersion">
      <p class="login-version-text">{{sysConfig.sysVersion}}</p>
    </div>
    <div class="login-left">
      <a class="login-company-logo" target="_blank" href="https://www.jnpfsoft.com">
        <img class="login-company-logo-img" src="@/assets/images/login-company-logo.png" alt="">
      </a>
      <img class="login-banner" src="@/assets/images/login-banner.png" alt="">
      <div class="login-left-txt" v-if="showTxt">
        <p class="title1">赋能开发者，提速企业数字化，零代码实现80%的基础功能</p>
        <p class="title2">已帮助<span>20000+</span>家企业完成数字化转型升级</p>
        <a class="link" target="_blank" href="https://www.jnpfsoft.com">了解更多</a>
      </div>
    </div>
    <div class="login-content">
      <div class="login-form">
        <el-image class="login-logo" :src="define.comUrl+sysConfig.loginIcon"
          v-if="sysConfig && sysConfig.loginIcon">
          <template slot="error">
            <img class="login-logo" src="@/assets/images/login_logo.png" alt="">
          </template>
        </el-image>
        <img class="login-logo" src="@/assets/images/login_logo.png" alt="" v-else>
        <div v-show="!isSso && !ssoLoading">
          <div class="login-tab" :class="'active'+active">
            <a class="item" :class="{'active': active==1}"
              @click="active=1">{{$t('login.title')}}</a>
            <a class="item" :class="{'active': active==2}"
              @click="active=2">{{$t('login.scanTitle')}}</a>
          </div>
          <el-form v-show="active==1" ref="loginForm" :model="loginForm" :rules="loginRules"
            autocomplete="on" label-position="left">
            <el-form-item prop="account">
              <el-input ref="account" v-model="loginForm.account"
                :placeholder="$t('login.username')" name="account" type="text" tabindex="1"
                autocomplete="on" prefix-icon="el-icon-user" size="large" @change="getConfig">
              </el-input>
            </el-form-item>
            <el-tooltip v-model="capsTooltip" :content="$t('login.upper')" placement="right" manual>
              <el-form-item prop="password">
                <el-input ref="password" v-model="loginForm.password" show-password
                  :placeholder="$t('login.password')" name="password" tabindex="2" autocomplete="on"
                  @keyup.native="checkCapslock" @blur="capsTooltip = false"
                  prefix-icon="el-icon-lock" size="large"></el-input>
                <!-- @keyup.enter.native="handleLogin" -->
              </el-form-item>
            </el-tooltip>
            <el-form-item prop="code" v-if="needCode">
              <el-row type="flex" justify="space-between">
                <el-col class="sms-input">
                  <el-input v-model="loginForm.code" :placeholder="$t('login.codeTip')" name="code"
                    autocomplete="on" prefix-icon="el-icon-key" size="large">
                  </el-input>
                </el-col>
                <el-col class="sms-right code-right">
                  <el-tooltip :content="$t('login.changeCode')" placement="bottom">
                    <img id="imgcode" :alt="$t('login.changeCode')" :src="define.comUrl+imgUrl"
                      @click="changeImg">
                  </el-tooltip>
                </el-col>
              </el-row>
            </el-form-item>
            <el-button :loading="loading" type="primary" class="login-btn" size="large"
              @click.native.prevent="handleLogin">{{ $t('login.logIn') }}</el-button>
            <template v-if="socialsList.length">
              <el-divider content-position="center" class="divider__text">其他登录方式</el-divider>
              <div class="other-list">
                <div v-for="(item,i) in socialsList" :key="i">
                  <el-tooltip class="item" effect="dark" :content='item.name+"登录"' placement="top">
                    <div class="other-item" @click="otherLogin(item.enname)"><i
                        :class="item.icon" />
                    </div>
                  </el-tooltip>
                </div>
              </div>
            </template>
          </el-form>
          <div v-show="active==2" class="login-form-QRCode">
            <img class="qrcode-img" src="@/assets/images/login_qr.png">
            <p class="qrcode-tip">正在测试,稍后上线</p>
          </div>
        </div>
        <el-button type="primary" class="sso-login-btn" size="large" :loading="loading"
          @click.native.prevent="ssoLogin" v-show="isSso && !ssoLoading">登录</el-button>
      </div>
      <div class="copyright"> {{sysConfig.copyright}}</div>
    </div>
    <el-dialog title="登录" :close-on-click-modal="false" :visible.sync="visible"
      :close-on-press-escape="false" class="JNPF-dialog JNPF-dialog_center JNPF-dialog-sso"
      lock-scroll width="1000px" @open="onOpen">
      <iframe width="100%" height="100%" :src="ssoUrl" frameborder="0" v-if="show"></iframe>
    </el-dialog>
    <!-- <div class="login-foot">Copyright 引迈信息技术有限公司, All Rights Reserved. 沪ICP备17044791号-1
      助力企业和团队快速实现目标</div> -->
    <el-dialog :visible.sync="dialogVisible" width="600px"
      class="JNPF-dialog JNPF-dialog_center form-script-dialog other-login-dialog"
      :before-close="handleClose">
      <div class="other-main">
        <div class="other-title">
          <div class="other-icon"><i class="icon-ym icon-ym-user" /></div>
          <div class="other-text">请选择登录账号</div>
        </div>
        <div class="other-body">
          <el-row :gutter="20">
            <template v-for="(item,i) in tenantSocialList">
              <el-col :span="12" :key="i">
                <div @click="socailsLogin(item)">
                  <el-card shadow="hover" class="other-login-card">
                    <h2>{{item.socialName}}</h2>
                    <div class="other-login-des">租户名称：{{item.tenantName}}</div>
                    <div class="other-login-des">租户ID：{{item.tenantId}}</div>
                    <div class="other-login-des">账号ID：{{item.accountName}}</div>
                  </el-card>
                </div>
              </el-col>
            </template>
          </el-row>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getConfig } from '@/api/user'
import { getSocialsLoginList, getLoginConfig, getTicket, getTicketStatus, socialsLogin } from '@/api/permission/socialsUser'
import qs from 'qs'

export default {
  name: 'Login',
  data() {
    return {
      loginForm: {
        account: '',
        password: '',
        code: '',
        origin: 'password'
      },
      loginRules: {
        account: [
          { required: true, trigger: 'blur', message: this.$t('login.accountTip') }
        ],
        password: [
          { required: true, trigger: 'blur', message: this.$t('login.passwordTip') }
        ],
        code: [
          { required: true, trigger: 'blur', message: this.$t('login.codeTip') }
        ],
      },
      imgUrl: "",
      timestamp: '',
      capsTooltip: false,
      loading: false,
      showDialog: false,
      needCode: false,
      codeLength: 4,
      redirect: undefined,
      showTxt: false,
      otherQuery: {},
      active: 1,
      listenerLoad: false,
      visible: false,
      show: false,
      ssoLoading: true,
      isSso: false,
      preUrl: '',
      ssoUrl: '',
      ssoTicket: "",
      ticketParams: "",
      ssoTimer: null,
      socialsList: [],
      dialogVisible: false,
      tenantSocialList: [],
      redirectUrl: '',
    }
  },
  computed: {
    sysConfig() {
      return this.$store.state.settings.sysConfig
    },
    loginLoading() {
      return this.$store.state.user.loginLoading
    }
  },
  watch: {
    loginLoading(val) {
      if (!val) this.loading = false
    },
    visible(val) {
      if (!val) {
        this.loading = false
        this.show = false
        this.clearTimer()
      }
    },
    $route: {
      handler: function (route) {
        const query = route.query
        if (query) {
          this.redirect = query.redirect
          this.otherQuery = this.getOtherQuery(query)
        }
      },
      immediate: true
    }
  },
  created() {
    const _this = this
    document.onkeydown = function (e) {
      const { keyCode } = e
      if (keyCode === 13) {
        _this.handleLogin()
      }
    }
    if (this.needCode) this.changeImg()
    this.getLoginConfig()
  },
  mounted() {
    this.$store.commit('user/SET_LOGIN_LOADING', false)
    this.setShowTxt()
    window.onresize = () => {
      this.setShowTxt()
    }
  },
  destroyed() {
    document.onkeydown = function (e) {
      const { keyCode } = e
      if (keyCode === 13) { }
    }
  },
  methods: {
    setShowTxt() {
      if (document.documentElement.clientHeight <= 840) {
        this.showTxt = false
      } else {
        this.showTxt = true
      }
    },
    checkCapslock(e) {
      const { key } = e
      this.capsTooltip = key && key.length === 1 && key >= 'A' && key <= 'Z'
    },
    getConfig(val) {
      if (!val) return
      getConfig(this.loginForm.account).then(res => {
        this.needCode = !!res.data.enableVerificationCode
        if (this.needCode) {
          this.codeLength = res.data.verificationCodeNumber || 4
          this.changeImg()
        }
      })
    },
    handleLogin() {
      if (this.loading) return
      this.$refs.loginForm.validate(valid => {
        if (valid) {
          this.loading = true
          this.$store.commit('user/SET_LOGIN_LOADING', true)
          const query = {
            ...this.loginForm,
            timestamp: this.timestamp,
            jnpf_ticket: this.ssoTicket
          }
          this.$store.dispatch('user/login', query).then(res => {
            this.$router.push({
              path: this.redirect || '/home',
              query: this.otherQuery
            })
          }).catch(() => {
            if (this.needCode) {
              this.changeImg()
              this.loginForm.code = ''
            }
            this.$store.commit('user/SET_LOGIN_LOADING', false)
          })
        } else {
          return false
        }
      })
    },
    changeImg() {
      let timestamp = Math.random()
      this.timestamp = timestamp
      this.imgUrl = `/api/oauth/ImageCode/${this.codeLength || 4}/${timestamp}`
    },
    getOtherQuery(query) {
      return Object.keys(query).reduce((acc, cur) => {
        if (cur !== 'redirect') {
          acc[cur] = query[cur]
        }
        return acc
      }, {})
    },
    otherLoginList() {
      this.socialsList = []
      getSocialsLoginList().then(res => {
        this.socialsList = res.data.filter(item => { return item.isLatest == "true" });
      })
    },
    otherLogin(data) {
      getTicket().then(res => {
        this.ssoTicket = res.data
        if (this.winURL && !this.winURL.closed) {
          this.winURL.location.replace(this.redirectUrl)
          this.winURL.focus()
          return
        }
        this.socialsList.forEach(item => {
          if (data == item.enname) {
            let renderUrl = item.renderUrl.replace('JNPF_TICKET', this.ssoTicket)
            this.redirectUrl = renderUrl
          }
        })
        var iWidth = 750; //弹出窗口的宽度;
        var iHeight = 500;//弹出窗口的高度;
        var iLeft = (window.screen.width - iWidth) / 2;
        var iTop = (window.screen.height - iHeight) / 2;//获得窗口的垂直位置;
        this.winURL = window.open(this.redirectUrl, '_blank', 'height=' + iHeight + ',innerHeight=' + iHeight + ',width=' + iWidth + ',innerWidth=' + iWidth + ',top=' + iTop + ',left=' + iLeft + ',toolbar=no,menubar=no,scrollbars=auto,resizeable=no,location=no,status=no')
        this.ssoTimer = setInterval(() => {
          if (this.winURL.closed) { this.clearTimer() }
          this.getTicketStatus()
        }, 1000)
      })
    },
    getTicketStatus() {
      if (!this.ssoTicket) return
      getTicketStatus(this.ssoTicket).then(res => {
        if (res.data.status != 2) {
          this.winURL && this.winURL.close()
          if (res.data.status == 4) {//未绑定预留ticket
            clearInterval(this.ssoTimer)
            this.ssoTimer = null
          } else {
            this.clearTimer()
          }
          switch (res.data.status) {
            case 1://登陆成功
              let param = {
                theme: res.data.theme,
                token: res.data.value
              }
              this.$store.dispatch('user/setToken', param).then(res => {
                this.$router.push({
                  path: this.redirect || '/home',
                  query: this.otherQuery
                })
              })
              break;
            case 4://未绑定
              this.$message.error('第三方账号未绑定，5分钟内登录本系统账号密码自动绑定该账号！')
              this.visible = false
              this.ssoUrl = ''
              break;
            case 6://多租户绑定多个
              this.dialogVisible = true
              this.tenantSocialList = typeof res.data.value === 'string' ? JSON.parse(res.data.value) : res.data.value;
              break;
            default:
              this.$message.error(res.data.value || '账号异常！')
              this.visible = false
              this.ssoUrl = ''
              this.getLoginConfig()
              break
          }
        }
      })
    },
    clearTimer() {
      if (this.ssoTimer) {
        clearInterval(this.ssoTimer)
        this.ssoTimer = null
        this.ssoTicket = ''
      }
    },
    handleClose() {
      this.dialogVisible = false
    },
    socailsLogin(data) {
      socialsLogin(qs.stringify({ ...data, tenantLogin: true })).then(response => {
        if (response.code == 200) {
          this.$store.dispatch('user/setToken', response.data).then(res => {
            this.$router.push({
              path: this.redirect || '/home',
              query: this.otherQuery
            })
          })
        }
      })
    },
    ssoLogin() {
      if (this.loading) return
      this.loading = true
      getTicket().then(res => {
        this.ssoTicket = res.data
        this.ssoUrl = this.preUrl + '?' + this.ticketParams + '=' + this.ssoTicket
        this.visible = true
        this.ssoTimer = setInterval(() => {
          this.getTicketStatus()
        }, 1000)
      })
    },
    getLoginConfig() {
      getLoginConfig().then(res => {
        this.isSso = res.data.redirect
        this.preUrl = res.data.url
        this.ticketParams = res.data.ticketParams
        this.socialsList = res.data.socialsList || []
        localStorage.setItem('useSocials', this.socialsList.length)
        this.ssoLoading = false
      }).catch(() => {
        this.isSso = false
        this.ssoLoading = false
      })
    },
    onOpen() {
      this.show = true
    },
  }
}
</script>

<style lang="scss" scoped>
@import './index.scss';
</style>