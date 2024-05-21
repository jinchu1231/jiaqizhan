import { deepClone } from '@/utils'

function vModel(dataObject, defaultValue) {
  dataObject.props.value = defaultValue
  dataObject.on.input = val => {
    this.$emit('input', val)
  }
}

function emitEvents(confClone) {
  ['on', 'nativeOn'].forEach(attr => {
    const eventKeyList = Object.keys(confClone[attr] || {})
    eventKeyList.forEach(key => {
      const val = confClone[attr][key]
      if (typeof key === 'string') {
        confClone[attr][key] = (...arg) => this.$emit(key, arg)
      }
    })
  })
}

function buildDataObject(confClone, dataObject, formData) {
  const jnpfKey = confClone.__config__.jnpfKey
  Object.keys(confClone).forEach(key => {
    const val = confClone[key]
    if (key === '__vModel__') {
      vModel.call(this, dataObject, confClone.__config__.defaultValue)
    } else if (key === 'props') {
      dataObject[key][key] = val
    } else if (dataObject[key] !== undefined) {
      if (dataObject[key] === null ||
        dataObject[key] instanceof RegExp || ['boolean', 'string', 'number', 'function'].includes(typeof dataObject[key])) {
        dataObject[key] = val
      } else if (Array.isArray(dataObject[key])) {
        dataObject[key] = [...dataObject[key], ...val]
      } else {
        dataObject[key] = { ...dataObject[key], ...val }
      }
    } else {
      dataObject.attrs[key] = val
    }
  })
  if (['calculate', 'table', 'barcode', 'qrcode', 'popupSelect', 'popupTableSelect', 'autoComplete'].includes(jnpfKey)) {
    dataObject.attrs['formData'] = formData
  }
  if (['table'].includes(jnpfKey)) {
    dataObject.attrs['relations'] = this.relations
    dataObject.attrs['vModel'] = confClone.__vModel__
  }
  if (['popupAttr', 'calculate', 'relationFormAttr'].includes(jnpfKey)) {
    dataObject.attrs['defaultValue'] = confClone.__config__.defaultValue
  }
  if (['relationForm', 'popupSelect'].includes(jnpfKey)) {
    dataObject.attrs['field'] = confClone.__config__.tableName ? confClone.__vModel__ + '_jnpfTable_' + confClone.__config__.tableName + (confClone.__config__.isSubTable ? '0' : "1") : confClone.__vModel__
  }
  // 清理属性
  clearAttrs(dataObject)
}

function clearAttrs(dataObject) {
  delete dataObject.attrs.__config__
  delete dataObject.attrs.__slot__
  delete dataObject.attrs.__methods__
}

function makeDataObject() {
  return {
    class: {},
    attrs: {},
    props: {},
    domProps: {},
    nativeOn: {},
    on: {},
    style: {},
    directives: [],
    scopedSlots: {},
    slot: null,
    key: null,
    ref: null,
    refInFor: true
  }
}

export default {
  props: {
    conf: {
      type: Object,
      required: true
    },
    formData: {
      type: Object,
      default: () => { }
    },
    relations: Object,
  },
  render(h) {
    const dataObject = makeDataObject()
    const confClone = deepClone(this.conf)
    const children = this.$slots.default || []

    // 将字符串类型的事件，发送为消息
    emitEvents.call(this, confClone)

    // 将json表单配置转化为vue render可以识别的 “数据对象（dataObject）”
    buildDataObject.call(this, confClone, dataObject, this.formData)

    return h(this.conf.__config__.tag, dataObject, children)
  }
}