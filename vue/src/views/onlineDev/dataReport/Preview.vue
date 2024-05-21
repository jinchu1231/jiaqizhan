<template>
  <transition name="el-zoom-in-center">
    <div class="JNPF-preview-main">
      <iframe width="100%" height="100%" :src="url" frameborder="0"></iframe>
    </div>
  </transition>
</template>

<script>
import { getToken } from '@/utils/auth'
import { getDataReportInfo } from '@/api/onlineDev/dataReport'
export default {
  data() {
    return {
      url: ''
    }
  },
  mounted() {
    window.addEventListener('message', this.handleMessage)
  },
  methods: {
    init(id) {
      this.id = id || ''
      this.$nextTick(() => {
        const token = getToken()
        let targetUrl = `${this.define.report}/preview.html?id=${id}&token=${token}&page=1`
        getDataReportInfo(id).then(res => {
          let item = {}
          if (res.data?.searchForm?.components && Array.isArray(res.data.searchForm.components)) {
            this.listQuery(res.data.searchForm.components, item)
            for (let key in item) {
              let item1 = '&' + key + '=' + item[key]
              targetUrl += item1
            }
          }
          this.url = targetUrl
        })
      })
    },
    listQuery(list, callback) {
      for (let i = 0; i < list.length; i++) {
        let item = list[i]
        let arrayList = []
        if (item.hasOwnProperty('cols') && Array.isArray(item.cols)) {
          arrayList = arrayList.concat(item.cols)
        }
        if (item.hasOwnProperty('children') && Array.isArray(item.children)) {
          arrayList = arrayList.concat(item.children)
        }
        if (item.bindParameter && item.defaultValue) {
          callback[item.bindParameter] = item.defaultValue
        }
        this.listQuery(arrayList, callback)
      }
    },
    async handleMessage(e) {
      const data = e.data
      if (data === 'closeDialog') {
        this.url = ''
        this.$emit('close')
      }
    }
  }
}
</script>
