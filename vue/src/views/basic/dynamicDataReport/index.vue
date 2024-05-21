<template>
  <div class="DynamicDataReport-container app-container">
    <iframe width="100%" height="100%" :src="url" frameborder="0"></iframe>
  </div>
</template>

<script>
import { getToken } from '@/utils/auth'
import { getDataReportInfo } from '@/api/onlineDev/dataReport'
export default {
  name: 'dynamicDataReport',
  data() {
    return {
      id: '',
      url: ''
    }
  },
  created() {
    this.id = this.$route.meta.relationId
    const token = getToken()
    if (!this.id) return
    this.$nextTick(() => {
      let targetUrl = `${this.define.report}/preview.html?id=${this.id}&token=${token}&page=1&from=menu`
      getDataReportInfo(this.id).then(res => {
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
  methods: {
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
  }
}
</script>
<style lang="scss" scoped>
.DynamicDataReport-container {
  padding: 0;
}
</style>
