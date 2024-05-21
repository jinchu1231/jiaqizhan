<template>
  <div class="right-menu">
    <template v-if="device!=='mobile'">
      <template v-if="showSearch">
        <search id="header-search" class="right-menu-item" />
      </template>
      <!-- <screenfull id="screenfull" class="right-menu-item hover-effect" /> -->
      <el-tooltip :content="$t('navbar.chat')" placement="bottom">
        <i class="icon-ym icon-ym-header-IM right-menu-item hover-effect" @click="openUserList()"
          :class="{'twinkle':isTwinkle}"></i>
      </el-tooltip>
      <el-tooltip :content="$t('navbar.notice')" placement="bottom">
        <div class="right-menu-item hover-effect" @click="$refs.MessageList.init()">
          <el-badge :value="messageCount" :hidden="!messageCount" :max="99">
            <i class="icon-ym icon-ym-header-message"></i>
          </el-badge>
        </div>
      </el-tooltip>
      <el-tooltip :content="$t('navbar.systemChange')" placement="bottom">
        <i class="icon-ym icon-ym-systemToggle right-menu-item hover-effect" @click="changeApply()"
          v-if="userInfo.systemIds && userInfo.systemIds.length>1 && !jnpf.getJnpfAppId()"></i>
      </el-tooltip>
      <template v-if="showLanguage">
        <lang-select class="right-menu-item hover-effect" />
      </template>
    </template>
    <el-dropdown class="avatar-container right-menu-item hover-effect" trigger='click'>
      <div class="avatar-wrapper">
        <el-avatar :size="26" :src="define.comUrl + userInfo.headIcon" class="user-avatar"
          v-if="userInfo.headIcon" />
        <span>{{userInfo.userName}}</span>
        <i class="el-icon-arrow-down"></i>
      </div>
      <el-dropdown-menu slot="dropdown">
        <router-link to="/profile">
          <el-dropdown-item>
            <i class="icon-ym icon-ym-header-userInfo"></i>{{ $t('navbar.profile') }}
          </el-dropdown-item>
        </router-link>
        <a href="http://mail.qq.com/cgi-bin/qm_share?t=qm_mailme&email=dA0dGhkVHQcbEgA0BQVaFxsZ"
          target="_blank">
          <el-dropdown-item>
            <i class="icon-ym icon-ym-header-feedBack"></i>{{ $t('navbar.feedback') }}
          </el-dropdown-item>
        </a>
        <el-dropdown-item @click.native="visible = true">
          <i class="icon-ym icon-ym-header-about"></i>{{ $t('navbar.about') }}
        </el-dropdown-item>
        <el-dropdown-item @click.native="visible2 = true">
          <i class="icon-ym icon-ym-generator-card"></i>{{ $t('navbar.statement') }}
        </el-dropdown-item>
        <el-dropdown-item @click.native="lock">
          <i class="icon-ym icon-ym-header-lockScreen"></i>{{ $t('navbar.lockScreen') }}
        </el-dropdown-item>
        <el-dropdown-item divided @click.native="handleLogout">
          <i class="icon-ym icon-ym-header-loginOut"></i>{{ $t('navbar.logout') }}
        </el-dropdown-item>
      </el-dropdown-menu>
    </el-dropdown>
    <el-tooltip :content="$t('navbar.setting')" placement="bottom">
      <div class="right-menu-item hover-effect" @click="$refs.Settings.init()" v-if="showSettings">
        <i class="icon-ym icon-ym-header-pannel"></i>
      </div>
    </el-tooltip>
    <el-dialog title="关于平台" :close-on-click-modal="false" :visible.sync="visible" v-drag-dialog
      class="JNPF-dialog JNPF-dialog_center about-dialog" lock-scroll width="400px">
      <div class="about-dialog-main">
        <el-image :src="define.comUrl+sysConfig.logoIcon" class="about-logo"
          v-if="sysConfig && sysConfig.logoIcon">
          <template slot="error">
            <img src="@/assets/images/jnpf.png" class="about-logo" />
          </template>
        </el-image>
        <img src="@/assets/images/jnpf.png" class="about-logo" v-else />
        <div>
          <p class="title"><a href="https://www.jnpfsoft.com"
              target="_blank">{{sysConfig.sysName}}</a></p>
          <p>版本：{{sysConfig.sysVersion}}</p>
          <p>作者：引迈软件</p>
          <p>引迈信息技术有限公司出品</p>
        </div>
      </div>
      <p class="about-dialog-main-tip">解放90%重复性的代码编写工作，让你更加专注于业务和体验！</p>
    </el-dialog>
    <el-dialog title="官方声明" :close-on-click-modal="false" :visible.sync="visible2" v-drag-dialog
      class="JNPF-dialog JNPF-dialog_center about-dialog" lock-scroll width="400px">
      <div class="about-dialog-main">
        <div>
          JNPF是对外开放性体验产品，平台内所有数据为我司虚构的测试数据，是随机生成非敏感数据，所以严禁任何人在JNPF平台上修改数据内容、扭曲数据目的、传播或售卖测试数据，特此警告。
        </div>
      </div>
    </el-dialog>
    <MessageList ref="MessageList" @read='read' />
    <Settings ref="Settings" />
    <UserList ref="UserList" @changeTwinkle='changeTwinkle' />
    <ApplyList ref="ApplyList" />
  </div>
</template>
<script>
import { mapGetters, mapState } from 'vuex'
import Screenfull from '@/components/Screenfull'
import SizeSelect from '@/components/SizeSelect'
import LangSelect from '@/components/LangSelect'
import Search from '@/components/HeaderSearch'
import MessageList from './MessageList'
import Settings from './settings'
import UserList from './userList/UserList'
import ApplyList from './ApplyList'
import dragDialog from "@/directive/el-drag-dialog";
import ReconnectingWebSocket from 'reconnecting-websocket'
import { getToken } from '@/utils/auth'
export default {
  directives: { dragDialog },
  components: {
    Screenfull,
    SizeSelect,
    LangSelect,
    Search,
    MessageList,
    UserList,
    ApplyList,
    Settings,
  },
  computed: {
    ...mapState({
      showLanguage: state => state.settings.showLanguage,
      showSearch: state => state.settings.showSearch,
      showSettings: state => state.settings.showSettings,
    }),
    ...mapGetters(['device', 'userInfo']),
    sysConfig() {
      return this.$store.state.settings.sysConfig
    }
  },
  data() {
    return {
      visible: false,
      visible2: false,
      isTwinkle: false,
      messageCount: 0,
      notify: null,
      userList: []
    }
  },
  watch: {
    isTwinkle(val) {
      // if (val) {
      //   this.notify.setTitle(true)
      // } else {
      //   this.notify.setTitle()
      // }
    }
  },
  created() {
    this.initWebSocket()
    this.initNotify()
  },
  methods: {
    initWebSocket() {
      this.socket = this.$store.getters.socket || null
      if ('WebSocket' in window) {
        if (!this.socket) {
          const isDev = process.env.NODE_ENV === 'development'
          const token = this.$store.getters.token
          const url = isDev ? this.define.APIURl + '/api/message/websocket/' + token : window.location.origin + '/websocket/' + token
          const webSocketUrl = url.replace('https://', 'wss://').replace('http://', 'ws://')
          this.socket = new ReconnectingWebSocket(webSocketUrl)
          this.$store.commit('user/SET_SOCKET', this.socket)
        }
        //添加事件监听
        let socket = this.socket
        socket.onopen = () => {
          var onConnection = {
            "method": "OnConnection", "token": this.$store.getters.token, "mobileDevice": false, systemId: this.userInfo.systemId, isSeparate: !!this.jnpf.getJnpfAppId()
          }
          let msg = JSON.stringify(onConnection);
          socket.send(msg)
        }
        socket.onmessage = (event) => {
          let dataStr = event.data;
          const data = JSON.parse(dataStr)
          if (data.method == 'initMessage') {
            this.messageCount = data.unreadTotalCount || 0
            this.isTwinkle = !!data.unreadNums.length
          }
          //用户在线
          if (data.method == 'Online') {
          }
          //用户离线
          if (data.method == 'Offline') {
          }
          //消息推送（消息公告用的）
          if (data.method == 'messagePush') {
            this.messageCount += data.unreadNoticeCount
            if (this.$refs.MessageList.visible) this.$refs.MessageList.init()
          }
          //用户过期
          if (data.method == 'logout') {
            setTimeout(() => {
              if (data.token && data.token !== getToken()) return location.reload()
              if (this.socket) {
                this.socket.close()
                this.socket = null
                this.$store.commit('user/SET_SOCKET', this.socket)
              }
              this.$message({
                message: data.msg || '登录过期,请重新登录',
                type: 'error',
                duration: 1000,
                onClose: () => {
                  this.$store.dispatch('user/resetToken').then(() => {
                    location.reload()
                  })
                }
              })
            }, 1000);
          }
          //断开websocket连接
          if (data.method == 'closeSocket') {
            if (this.socket) {
              this.socket.close()
              this.socket = null
              this.$store.commit('user/SET_SOCKET', this.socket)
            }
          }
          //接收对方发送的消息
          if (data.method == 'receiveMessage') {
            //判断是否打开窗口
            if (this.$refs.UserList && this.$refs.UserList.$refs.JNPFIm && this.$refs.UserList.$refs.JNPFIm.visible) {
              if (this.$refs.UserList.$refs.JNPFIm.info.id === data.formUserId) {
                let messItem = {
                  userId: data.formUserId,
                  messageType: data.messageType,
                  message: data.formMessage,
                  dateTime: this.jnpf.toDate(data.dateTime)
                }
                this.$refs.UserList.$refs.JNPFIm.addItem(messItem)
                //更新已读
                let msgObj = {
                  method: "UpdateReadMessage",
                  formUserId: data.formUserId,
                  token: this.$store.getters.token
                }
                let msg = JSON.stringify(msgObj);
                socket.send(msg)
                this.$refs.UserList.updateReply(data)
              } else {
                this.$refs.UserList.updateReply(data, 1)
                this.isTwinkle = true
              }
            } else {
              this.$refs.UserList.updateReply(data, 1)
              this.isTwinkle = true
            }
          }
          //显示自己发送的消息
          if (data.method == 'sendMessage') {
            if (this.$refs.UserList.$refs.JNPFIm.info.id !== data.toUserId) return
            //添加到客户端
            let messItem = {
              userId: data.UserId,
              messageType: data.messageType,
              message: data.toMessage,
              dateTime: this.jnpf.toDate(data.dateTime)
            }
            this.$refs.UserList.updateLatestMessage(data)
            this.$refs.UserList.$refs.JNPFIm.addItem(messItem)
          }
          //消息列表
          if (data.method == 'messageList') {
            this.$refs.UserList.$refs.JNPFIm.getList(data)
          }
          //刷新页面
          if (data.method == 'refresh') {
            if (data.msg) {
              this.$message.error(data.msg)
              setTimeout(() => {
                location.reload()
              }, 2000);
            } else {
              location.reload()
            }
          }
        }
      }
    },
    toggleSideBar() {
      this.$store.dispatch('app/toggleSideBar')
    },
    handleLogout() {
      this.$confirm('您确定要退出应用程序吗?', '提示', {
        type: 'warning'
      }).then(() => {
        this.logout();
      }).catch(() => { });
    },
    async logout() {
      await this.$store.dispatch('user/logout')
      // this.$router.push(`/login?redirect=${this.$route.fullPath}`)
      this.$router.push(`/login`)
    },
    lock() {
      this.$store.dispatch('user/setLock')
      this.$nextTick(() => {
        this.$router.push('/lockScreen')
      })
    },
    read(isAll) {
      if (isAll) {
        this.messageCount = 0
      } else {
        this.messageCount -= 1
        this.messageCount = this.messageCount >= 0 ? this.messageCount : 0
      }
    },
    openUserList() {
      this.$refs.UserList.init()
    },
    changeApply() {
      this.$refs.ApplyList.init()
    },
    changeTwinkle(boo) {
      this.isTwinkle = boo
    },
    initNotify() {
      // this.notify = new Notify({
      //   message: '您有新消息',
      //   effect: 'flash', // flash | scroll
      //   interval: 1000
      // });
    },
  }
}
</script>

<style lang="scss" scoped>
.navbar {
  .right-menu {
    float: right;
    height: 100%;
    &:focus {
      outline: none;
    }

    .right-menu-item {
      display: inline-block;
      padding: 0 8px;
      height: 100%;
      font-size: 18px;
      color: #666666;
      vertical-align: text-bottom;
      line-height: 60px;

      &.hover-effect {
        cursor: pointer;
        transition: background 0.3s;

        &:hover {
          background: rgba(0, 0, 0, 0.025);
        }
      }
      >>> .el-badge {
        line-height: 1;
        .el-badge__content {
          top: -4px;
        }
      }
      >>> .icon-ym {
        font-size: 18px;
      }
    }

    .avatar-container {
      // margin-right: 30px;

      .avatar-wrapper {
        position: relative;

        .user-avatar {
          cursor: pointer;
          vertical-align: top;
          margin-top: 18px;
          margin-right: 2px;
        }

        .el-icon-caret-bottom {
          cursor: pointer;
          position: absolute;
          right: -20px;
          top: 24px;
          font-size: 18px;
        }
        span {
          font-size: 14px;
          line-height: 60px;
        }
      }
    }
  }
}
.about-dialog {
  &.JNPF-dialog.JNPF-dialog_center >>> .el-dialog .el-dialog__body {
    padding: 0 0 40px 0;
  }
  .about-dialog-main {
    display: flex;
    align-items: center;
    height: 150px;
    padding: 0 30px;
    .about-logo {
      display: inline-block;
      width: 95px;
      height: 95px;
      line-height: 95px;
      text-align: center;
      border-radius: 10px;
      color: #fff;
      margin-right: 30px;
    }
    line-height: 24px;
  }
  .about-dialog-main-tip {
    font-size: 12px;
    padding-left: 30px;
    margin: 0;
  }
  .title {
    font-size: 16px;
    font-weight: 600;
  }
}
/* 定义keyframe动画，命名为blink */
@keyframes blink {
  0% {
    opacity: 1;
  }
  50% {
    opacity: 1;
  }
  50.01% {
    opacity: 0;
  }
  100% {
    opacity: 0;
  }
}
.twinkle {
  animation: blink 0.5s linear infinite;
}
</style>
