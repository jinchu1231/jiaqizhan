import { isNumberStr } from '@/components/Generator/utils'
export default {
  inject: ["getShowType"],
  data() {
    return {}
  },
  computed: {
    showType() {
      return this.getShowType()
    }
  },
  methods: {
    onDefaultValueInput(str) {
      if (Array.isArray(this.activeData.__config__.defaultValue)) {
        // 数组
        this.$set(
          this.activeData.__config__,
          'defaultValue',
          str.split(',').map(val => (isNumberStr(val) ? +val : val))
        )
      } else if (['true', 'false'].indexOf(str) > -1) {
        // 布尔
        this.$set(this.activeData.__config__, 'defaultValue', JSON.parse(str))
      } else {
        // 字符串和数字
        this.$set(
          this.activeData.__config__,
          'defaultValue',
          isNumberStr(str) ? +str : str
        )
      }
    },
    onInterfaceIdChange(val, row) {
      if (!val) {
        this.activeData.interfaceId = ''
        this.activeData.interfaceName = ''
        this.activeData.templateJson = []
        this.activeData.__config__.defaultValue = ''
        this.activeData.interfaceHasPage = 0
        this.getDataInterfaceInfo()
        return
      }
      this.activeData.interfaceId = val
      this.activeData.interfaceName = row.fullName
      this.activeData.templateJson = row.parameterJson ? JSON.parse(row.parameterJson) : []
      this.activeData.__config__.defaultValue = ''
      this.getDataInterfaceInfo()
    },
    setDefaultValue(val) {
      if (Array.isArray(val)) {
        return val.join(',')
      }
      // if (['string', 'number'].indexOf(typeof val) > -1) {
      //   return val
      // }
      if (typeof val === 'boolean') {
        return `${val}`
      }
      return val
    },
    addReg() {
      this.activeData.__config__.regList.push({
        pattern: '',
        message: ''
      })
    }
  }
}