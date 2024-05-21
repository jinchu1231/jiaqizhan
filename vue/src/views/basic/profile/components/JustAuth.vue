<template>
  <div class="justAuth">
    <div class="JNPF-common-title mb-20">
      <h2 class="bold">第三方服务绑定</h2>
    </div>
    <el-col :span="22" :offset="1" class="mt-20">
      <template v-for="(item,i) in socialsList">
        <el-row :key="i">
          <el-col :span="1" class="col-img">
            <img v-if="item.enname" :src="item.logo" alt="" class="row-img" />
            <img v-if="!item.enname" src="" alt="" class="row-img" />
          </el-col>
          <el-col :span="21">
            <div class="tag-group-div">
              <span class="tag-group-span">{{item.name}}</span>
              <el-tag type="success" effect="dark" v-if="item.entity"> 已绑定 </el-tag>
              <el-tag type="warning" effect="dark" v-if="!item.entity"> 未绑定 </el-tag>
            </div>
            <div class="tag-group-con"> <span>{{item.describetion}}</span>
            </div>
          </el-col>
          <el-col :span="1" class="col-line-height">
            <el-button size="small" type="" v-if="item.entity"
              @click="deleteSocials(item.entity.userId,item.entity.id)">解绑</el-button>
            <el-button size="small" type="primary" v-if="!item.entity"
              @click="binding(item.enname)">绑定</el-button>
          </el-col>
        </el-row>
        <el-divider class="divider-margin" :key="i"></el-divider>

      </template>
    </el-col>
  </div>
</template>
<script>
import { getSocialsUserList, binding, deleteSocials } from '@/api/permission/socialsUser'
export default {
  data() {
    return {
      socialsList: [],
      dialogVisible: false,
      dialogSrt: '',
      listenerLoad: false,
    }
  },
  created() {
    this.init()
    this.bindListener()
  },
  beforeDestroy() {
    window.removeEventListener('message', () => { })
  },
  methods: {
    init() {
      this.loading = true
      getSocialsUserList().then(res => {
        this.socialsList = res.data;
        this.loading = false
      })
    },
    binding(data) {
      binding(data).then(res => {
        if (this.winURL && !this.winURL.closed) {
          this.winURL.location.replace(res.msg)
          this.winURL.focus()
          return
        }
        var iWidth = 750; //弹出窗口的宽度;
        var iHeight = 500;//弹出窗口的高度;
        var iLeft = (window.screen.width - iWidth) / 2;
        var iTop = (window.screen.height - iHeight) / 2;//获得窗口的垂直位置;
        this.winURL = window.open(res.msg, '_blank', 'height=' + iHeight + ',innerHeight=' + iHeight + ',width=' + iWidth + ',innerWidth=' + iWidth + ',top=' + iTop + ',left=' + iLeft + ',toolbar=no,menubar=no,scrollbars=auto,resizeable=no,location=no,status=no')
      })
    },
    bindListener() {
      if (!this.listenerLoad) {
        window.addEventListener('message', (e) => {
          var res = typeof e.data === 'string' ? JSON.parse(e.data) : e.data;
          if (res.code == "200") {
            this.$message({
              message: res.message,
              type: 'success',
              duration: 1000,
              onClose: () => {
                this.init()
              }
            })
          }
          if (res.code == "201") {
            this.$message({
              message: res.message,
              type: 'error',
              duration: 3000,
              onClose: () => {
                this.init()
              }
            })
          }
        })
      }
      this.listenerLoad = true
    },
    deleteSocials(userId, id) {
      this.$confirm('确定要解除该账号绑定?', '提示', {
        type: 'warning'
      }).then(() => {
        deleteSocials(userId, id).then(res => {
          this.$message({
            message: res.msg,
            type: 'success',
            duration: 1000,
            onClose: () => {
              this.init()
            }
          })
        })
        this.init()
      }).catch(() => { });
    },
    handleClose(done) {
      this.$confirm('确认关闭？')
        .then(_ => {
          done();
        })
        .catch(_ => { });
    },
    iframeLoad() {
      window.addEventListener("message", this.handleMessage);
    },
    handleMessage(e) {
      //e为传过来的信息
      console.log(e)
    }
  }
}

</script>
<style scoped>
.col-img {
  margin: 0px 10px;
}
.tag-group-div {
  height: 24px;
  line-height: 24px;
}
.row-img {
  height: 50px;
  width: 50px;
}
.tag-group-span {
  height: 24px;
  line-height: 24px;
  margin-right: 20px;
  font-weight: bolder;
  font-size: 16px;
}
.tag-group-con {
  font-size: 14px;
  margin: 5px 0;
}
.divider-margin {
  margin: 15px 0px;
}
.tag-group-button {
  height: 24px;
  line-height: 24px;
}

.col-line-height {
  line-height: 50px;
}
</style>