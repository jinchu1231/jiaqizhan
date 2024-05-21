export default [{
  __config__: {
    label: '单行输入框',
    labelWidth: null,
    showLabel: true,
    changeTag: true,
    tag: 'el-input',
    tagIcon: 'input',
    defaultValue: undefined,
    required: true,
    layout: 'colFormItem',
    span: 24,
    jnpfKey: 'input',
    // 正则校验规则
    regList: [{
      pattern: '/^1(3|4|5|7|8|9)\\d{9}$/',
      message: '手机号格式错误'
    }]
  },
  // 组件的插槽属性
  __slot__: {
    prepend: '',
    append: ''
  },
  __vModel__: 'mobile',
  placeholder: '请输入手机号',
  style: { width: '100%' },
  clearable: true,
  prefixIcon: 'el-icon-mobile',
  suffixIcon: '',
  maxlength: 11,
  showWordLimit: true,
  readonly: false,
  disabled: false,
}]