
<template>
  <component :is="currentView" :config="config" :modelId="modelId" :isPreview="isPreview"
    :encryption='encryption' v-show="showPage" />
</template>

<script>
import { getConfigData } from '@/api/onlineDev/webDesign'
import Form from './form'
import List from './list'
import { message } from '@/utils/message'
export default {
  name: 'formShortLink',
  components: { Form, List },
  data() {
    return {
      currentView: '',
      showPage: false,
      isPreview: false,
      modelId: '',
      config: {},
      encryption: '',
      type: 'from'
    }
  },
  created() {
    const decryptedData = this.jnpf.aesEncryption.decrypt(this.$route.query.encryption)
    if (!decryptedData) return
    let dataAll = JSON.parse(decryptedData)
    this.modelId = dataAll.modelId ? dataAll.modelId : ''
    this.encryption = this.$route.query.encryption
    this.type = dataAll.type
    if (!this.modelId) return
    this.getConfigData()
  },
  methods: {
    getConfigData() {
      getConfigData(this.modelId, this.encryption).then(res => {
        if (res.code !== 200 || !res.data) {
          this.$store.dispatch('tagsView/delView', this.$route)
          this.$router.replace('/404')
          message({
            message: res.msg || '请求出错，请重试',
            type: 'error',
            duration: 1500,
          })
          return
        }
        this.config = res.data
        if (this.type == 'form') {
          this.currentView = 'Form'
        } else {
          this.currentView = 'List'
        }
        this.showPage = true
      }).catch(() => { })
    },
  }
}
</script>
<style lang="scss" scoped>
.table {
  width: 80%;
}
</style>

