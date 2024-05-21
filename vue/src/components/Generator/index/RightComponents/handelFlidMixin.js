
import { getDataInterfaceInfo } from '@/api/systemData/dataInterface'
const useSelect = ['treeSelect', 'radio', 'checkbox', 'select', 'cascader']
export default {
  data() {
    return { fieldJsonList: [], hasPage: false }
  },
  methods: {
    getDataInterfaceInfo() {
      let url = this.activeData.interfaceId ? this.activeData.interfaceId : this.activeData.__config__.propsUrl
      getDataInterfaceInfo(url).then(res => {
        this.fieldJsonList = res.data && res.data.fieldJson && (JSON.parse(res.data.fieldJson) || []) || []
        this.hasPage = res.data && res.data.hasPage == 1 ? true : false
        this.activeData.interfaceHasPage = this.hasPage
        this.activeData.hasPage = this.hasPage
      })
    },
    querySearch(queryString, cb) {
      var restaurants = this.fieldJsonList || [];
      var results = queryString ? restaurants.filter(this.createStateFilter(queryString)) : restaurants;
      // 调用 callback 返回建议列表的数据
      cb(results);
    },
    createStateFilter(queryString) {
      return (state) => {
        return (state.defaultValue.toLowerCase().indexOf(queryString.toLowerCase()) === 0);
      };
    },
    handleSelect(item, field, index) {
      const jnpfKey = this.activeData.__config__.jnpfKey
      if (jnpfKey === 'autoComplete') this.activeData[field] = item.defaultValue
      if (jnpfKey === 'popupSelect' || jnpfKey === 'popupTableSelect') {
        if (isNaN(index)) {
          this.activeData[field] = item.defaultValue
        } else {
          this.activeData.columnOptions[index][field] = item.defaultValue
        }
      }
      if (useSelect.includes(jnpfKey)) this.activeData.props[field] = item.defaultValue
      this.activeData.__config__.renderKey = +new Date()
    }
  }
}