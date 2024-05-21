<template>
  <el-dialog :title="title" :close-on-click-modal="false"
    class="JNPF-dialog JNPF-dialog_center transfer-dialog" lock-scroll append-to-body
    v-bind="$attrs" width="800px" :modal-append-to-body="false" v-on="$listeners" @open="onOpen">
    <template v-if="type==='user'">
      <userTransfer v-model="selectedData" ref="userTransfer" multiple />
    </template>
    <template v-else-if="type==='users'">
      <orgUsersSelect v-model="selectedData" ref="usersTransfer" multiple :dynamicList="dynamicList"
        :dynamicParameter="dynamicParameter" v-if="inlet === 'flow'" />
      <usersTransfer v-model="selectedData" ref="usersTransfer" multiple v-else
        :dynamicList="dynamicList" :dynamicParameter="dynamicParameter" />
    </template>
    <roleTransfer v-model="selectedData" ref="roleTransfer" multiple v-else-if="type==='role'"
      :dynamicList="dynamicList" />
    <orgTransfer v-model="selectedData" :treeData="treeData" ref="orgTransfer" multiple
      v-else-if="type==='organize'" :dynamicList="dynamicList" />
    <positionTransfer v-model="selectedData" :treeData="treeData" ref="positionTransfer" multiple
      v-else-if="type==='position'" :dynamicList="dynamicList" />
    <JNPFTransfer :loading="loading" :treeData="treeData" v-model="selectedData" :type="type"
      ref="JNPFTransfer" multiple :dynamicList="dynamicList" v-else />
    <span slot="footer" class="dialog-footer">
      <el-button @click="closeTransfer">{{$t('common.cancelButton')}}</el-button>
      <el-button type="primary" @click="confirm">{{$t('common.confirmButton')}}</el-button>
    </span>
  </el-dialog>
</template>
<script>
import JNPFTransfer from '@/components/JNPF-transfer'
import userTransfer from '@/components/JNPF-userTransfer'
import usersTransfer from '@/components/JNPF-usersTransfer'
import roleTransfer from './roleTransfer'
import orgUsersSelect from '../OrgUsersSelect'
import orgTransfer from './orgTransfer'
import positionTransfer from './positionTransfer'
export default {
  name: 'org-transfer',
  components: { JNPFTransfer, usersTransfer, roleTransfer, orgTransfer, positionTransfer, orgUsersSelect, userTransfer },
  props: {
    value: {
      type: Array,
      default: () => []
    },
    dynamicList: {
      type: Array,
      default: () => []
    },
    type: {
      type: String,
      default: 'user'
    },
    inlet: {
      type: String,
      default: ''
    },
    title: {
      type: String,
      default: '组织机构'
    },
    dynamicParameter: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      loading: false,
      treeData: [],
      selectedData: []
    }
  },
  methods: {
    onOpen() {
      this.dataInit()
    },
    closeTransfer() {

      this.$emit('update:visible', false)
    },
    confirm() {
      this.$emit('confirm', this.selectedData)
      this.closeTransfer()
    },
    dataInit() {
      this.loading = true
      this.selectedData = []
      this.treeData = []
      this.$nextTick(async () => {
        if (this.type === 'user') {
          this.selectedData = JSON.parse(JSON.stringify(this.value))
          this.$nextTick(() => {
            this.$refs.userTransfer && this.$refs.userTransfer.init()
          })
        } else if (this.type === 'users') {
          this.selectedData = JSON.parse(JSON.stringify(this.value))
          this.$nextTick(() => {
            this.$refs.usersTransfer && this.$refs.usersTransfer.init()
          })
        } else if (this.type === 'role') {
          this.selectedData = JSON.parse(JSON.stringify(this.value))
          this.$nextTick(() => {
            this.$refs.roleTransfer && this.$refs.roleTransfer.init()
          })
        } else {
          let res = []
          if (this.type === 'department' || this.type === 'organize') {
            res = await this.$store.dispatch('generator/getDepTree')
          }
          if (this.type === 'group') {
            res = await this.$store.dispatch('generator/getGroupTree')
          }
          if (this.type === 'position') {
            res = await this.$store.dispatch('base/getPositionTree')
            this.$nextTick(() => {
              this.$refs.positionTransfer && this.$refs.positionTransfer.init(this.selectedData)
            })
          }
          this.$refs.JNPFTransfer && (this.$refs.JNPFTransfer.filterText = '')
          this.treeData = res
          this.selectedData = this.value
        }
        this.loading = false
      })
    }
  }
}
</script>