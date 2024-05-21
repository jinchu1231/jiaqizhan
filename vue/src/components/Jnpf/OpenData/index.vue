<template>
  <div>
    <el-input :value="value" placeholder="系统自动生成" readonly v-if="!detailed" />
    <p v-else>{{value||''}}</p>
  </div>
</template>

<script>
import { mapGetters } from "vuex"
export default {
  name: 'JnpfOpenData',
  computed: {
    ...mapGetters(['userInfo'])
  },
  props: {
    value: {
      type: String,
      default: ''
    },
    /**
     * currUser - 当前用户
     * currTime - 当前时间
     * currOrganize - 所属组织
     * currPosition - 所属岗位
     */
    type: {
      type: String,
      default: 'currUser'
    },
    showLevel: {
      type: String,
      default: 'last'
    },
    detailed: {
      type: Boolean,
      default: false
    },
    placeholder: {
      type: String,
      default: '系统自动生成'
    },
  },
  data() {
    return {
      innerValue: ''
    }
  },
  watch: {
    showLevel() {
      this.setDefault()
    }
  },
  created() {
    this.setDefault()
  },
  methods: {
    setDefault() {
      if (this.type === 'currUser') {
        this.innerValue = this.userInfo.userName + '/' + this.userInfo.userAccount
        if (!this.userInfo.userName && !this.userInfo.userAccount) this.innerValue = ''
      }
      if (this.type === 'currTime') {
        this.innerValue = this.jnpf.toDate(new Date().getTime(), 'yyyy-MM-dd HH:mm:ss')
      }
      if (this.type === 'currOrganize') {
        this.innerValue = this.showLevel === 'last' ? this.userInfo.departmentName : this.userInfo.organizeName
      }
      if (this.type === 'currPosition') {
        this.innerValue = this.userInfo.positionName
      }
    }
  }
}
</script>