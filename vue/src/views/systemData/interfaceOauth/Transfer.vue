
<template>
  <el-dialog title="授权用户" :close-on-click-modal="false" :visible.sync="visible"
    class="JNPF-dialog JNPF-dialog_center transfer-dialog" lock-scroll append-to-body width="800px"
    :modal-append-to-body="false">
    <userTransfer v-model="selectedData" ref="userTransfer" multiple />
    <span slot="footer" class="dialog-footer">
      <el-button @click="visible=false">{{$t('common.cancelButton')}}</el-button>
      <el-button type="primary" @click="confirm">{{$t('common.confirmButton')}}</el-button>
    </span>
  </el-dialog>
</template>

<script>
import userTransfer from '@/components/JNPF-userTransfer'
import { SaveUserList, getInfo } from '@/api/systemData/interfaceOauth.js'
export default {
  name: 'transfer',
  components: { userTransfer },
  props: {
    id: {
      type: String,
      default: ''
    },
    systemId: {
      type: String,
      default: ''
    },
  },
  data() {
    return {
      selectedData: [],
      treeData: [],
      loading: false,
      visible: false,
      interfaceIdentId: ''
    }
  },
  methods: {
    init(id, list) {
      this.interfaceIdentId = id || ''
      this.selectedData = list || []
      this.visible = true
      getInfo(id).then(res => {
        this.selectedData = res.data.userList ? res.data.userList.map(o => o.userId) || [] : []
        this.dataInit()
      })
    },
    confirm() {
      let query = {
        interfaceIdentId: this.interfaceIdentId,
        userIds: this.selectedData
      }
      SaveUserList(query).then(res => {
        this.$message({
          type: 'success',
          message: res.msg,
          duration: 1000,
          onClose: () => {
            this.visible = false
            this.$emit('close')
          }
        });
      })
    },
    dataInit() {
      this.$nextTick(() => {
        this.$refs.userTransfer && this.$refs.userTransfer.init()
      })
    },
  }
}
</script>