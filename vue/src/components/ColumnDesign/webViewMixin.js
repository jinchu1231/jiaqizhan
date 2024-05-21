import draggable from 'vuedraggable'
import BatchEditing from '../Generator/index/RightComponents/BatchEditing'
import { getDictionaryTypeSelector, getDictionaryDataSelector } from "@/api/systemData/dictionary"
import { getDataInterfaceRes } from '@/api/systemData/dataInterface'
import dicIndex from '@/views/systemData/dictionary/index.vue';
import InterfaceDialog from '@/components/Process/PropPanel/InterfaceDialog'
const useSelect = ['treeSelect', 'radio', 'checkbox', 'select', 'cascader']
export default {
  components: { draggable, BatchEditing, dicIndex, InterfaceDialog },
  data() {
    return {
      dicOptions: [],
      dictionaryId: '',
      fieldJsonList: [], 
      hasPage: false
    }
  },
  methods: {
    addSelectItem() {
      const id = this.jnpf.idGenerator()
      this.activeData.options.push({
        id: id,
        fullName: '选项' + id,
      })
    },
    updateSelectItem() {
      this.updateVisible = true
      this.$nextTick(() => {
        this.$refs.batchEditing.init(this.activeData.options)
      })
    },
    dataTypeChange(val) {
      this.activeData.options = [];
      this.activeData.props.value = 'id';
      this.activeData.props.label = 'fullName';
      if (Reflect.has(this.activeData.props, 'children')) this.activeData.props.children = 'children';
      this.activeData.__config__.dictionaryType = '';
      this.activeData.__config__.propsUrl = '';
      this.activeData.__config__.propsName = '';
      this.activeData.__config__.templateJson = [];
    },
    handleSure(arr) {
      this.activeData.options = arr || []
    },
    // static end 
    selectChange() {
      this.$emit('changeSelect')
      this.dictionaryTypeChange(this.dictionaryId)
    },
    defaultValueChange() {
      this.selectChange()
    },
    dictionaryTypeChange(val) {
      this.dictionaryId = val
      this.activeData.__config__.defaultValue = this.defaultValue
      if (!val) {
        this.activeData.options = []
        return
      }
      getDictionaryDataSelector(val).then(res => {
        this.activeData.options = res.data.list
      })
    },
    goDictionary() {
      this.dicVisible = true
      this.$nextTick(() => {
        this.$refs.dicIndex.initData()
      })
    },
    getDicOptions() {
      getDictionaryTypeSelector().then(res => {
        this.dicOptions = res.data.list;
      });
    },
    // dictionary end 
    propsUrlChange(val, row) {
      let jnpfKey = this.activeData.__config__.jnpfKey
      if (jnpfKey === 'cascader') {
        this.activeData.__config__.defaultValue = []
      } else if (jnpfKey === 'treeSelect') {
        this.activeData.__config__.defaultValue = this.activeData.multiple ? [] : ''
      } else {
        this.activeData.__config__.defaultValue = this.defaultValue
      }
      if (!val) {
        this.activeData.__config__.propsUrl = ''
        this.activeData.__config__.propsName = ''
        this.activeData.__config__.templateJson = []
        this.activeData.options = []
        return
      }
      let list = row.parameterJson ? JSON.parse(row.parameterJson) : []
      this.activeData.__config__.propsUrl = val
      this.activeData.__config__.propsName = row.fullName
      this.activeData.__config__.templateJson = list.map(o => ({ ...o, relationField: '' }))
      let query = {
        paramList: this.activeData.__config__.templateJson || [],
      }
      getDataInterfaceRes(val, query).then(res => {
        let data = res.data
        if (Array.isArray(data)) {
          this.activeData.options = data
        } else {
          this.activeData.options = []
        }
      }).catch(() => {
        this.activeData.__config__.propsUrl = ''
        this.activeData.__config__.propsName = ''
        this.activeData.__config__.templateJson = []
        this.activeData.options = []
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
    // dynamic end
  }
}