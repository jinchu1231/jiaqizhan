import PortalLayout from '@/components/VisualPortal/Layout'
import { getAuthPortal, UpdateCustomPortal } from '@/api/onlineDev/portal'
import { getDataInterfaceRes } from '@/api/systemData/dataInterface'
import { mapGetters } from 'vuex'

export default {
  components: { PortalLayout },
  data() {
    return {
      portalId: '',
      layout: [],
      type: null,
      linkType: null,
      currentView: null,
      url: '',
      ajaxing: true,
      loading: false,
      noData: false,
      refreshData: {},
      timerList: [],
      formData: {},
      enabledLock: 1,
      systemId: ''
    }
  },
  computed: {
    ...mapGetters(['userInfo'])
  },
  destroyed() {
    this.clearAutoRefresh()
  },
  methods: {
    getData() {
      this.loading = true
      this.layout = []
      this.noData = false
      if (!this.portalId) {
        this.loading = false
        this.ajaxing = false
        this.noData = true
        return
      }
      getAuthPortal(this.portalId, { platform: 'Web', systemId: this.systemId }).then(res => {
        if (res.code != 200) {
          this.$message({
            message: res.msg,
            type: 'error',
            duration: 1500,
            onClose: () => {
              this.loading = false
              this.ajaxing = false
              this.noData = true
              return
            }
          })
        }
        if (res.data) {
          this.type = res.data.type || 0
          this.linkType = res.data.linkType || 0
          this.url = res.data.customUrl || ''
          this.enabledLock = res.data.enabledLock || 0
          if (res.data.type === 1) {
            if (res.data.customUrl && res.data.customUrl !== 1) {
              this.currentView = (resolve) => require([`@/views/${res.data.customUrl}`], resolve)
            }
          } else {
            if (res.data.formData) {
              this.formData = JSON.parse(res.data.formData)
              this.layout = this.filterList(JSON.parse(JSON.stringify(this.formData.layout)) || [])
              this.refreshData = this.formData.refresh || {}
            }
          }
        }
        this.ajaxing = false
        setTimeout(() => {
          this.initAutoRefresh()
          this.loading = false
        }, 500);
      }).catch(() => {
        this.loading = false
        this.ajaxing = false
        this.noData = true
      })
    },
    filterList(layout) {
      const loop = list => {
        for (let i = 0; i < list.length; i++) {
          const item = list[i];
          if (!(Array.isArray(item.visibility) && item.visibility.includes('pc')) && item.jnpfKey) {
            list.splice(i, 1)
            i--
          }
          if (item.children && item.children.length) loop(item.children)
        }
      }
      loop(layout)
      return layout
    },
    initAutoRefresh() {
      if (!this.layout.length) return
      this.timerList = []
      const loop = (list, type = 1) => {
        list.forEach(ele => {
          if ((ele.refresh && ele.refresh.autoRefresh && ele.refresh.autoRefreshTime) || type == 2) {
            var timer = setInterval(() => {
              ele.renderKey = +new Date()
              this.autoRefresh(ele)
            }, type == 2 ? this.refreshData.autoRefreshTime * 1000 * 60 : ele.refresh.autoRefreshTime * 1000 * 60);
            this.timerList.push(timer)
          }
          if (ele.children && ele.children.length) loop(ele.children, type)
        });
      }
      if (this.refreshData.autoRefresh) {
        loop(this.layout, 2)
      } else {
        loop(this.layout)
      }
    },
    autoRefresh(item) {
      const chartList = ['barChart', 'lineChart', 'pieChart', 'radarChart', 'mapChart']
      if (item.dataType === 'dynamic' && chartList.includes(item.jnpfKey)) {
        if (!item.propsApi) return
        getDataInterfaceRes(item.propsApi).then(res => {
          const realData = res.data
          if (Array.isArray(realData)) {
            item.option.defaultValue = realData
          } else {
            item.option.defaultValue = []
          }
        })
      }
    },
    clearAutoRefresh() {
      if (this.timerList.length) {
        this.timerList.forEach((ele) => {
          if (ele) clearInterval(ele)
        })
      }
    },
    layoutUpdatedEvent() {
      this.formData.layout = this.layout
      const query = { formData: JSON.stringify(this.formData), systemId: this.systemId }
      UpdateCustomPortal(this.portalId, query).then(res => { })
    }
  }
}