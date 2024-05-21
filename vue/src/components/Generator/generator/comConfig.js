// 动态options
const dyOptionsList = ['radio', 'checkbox', 'select', 'cascader', 'treeSelect']
// 不添加vModel
const noVModelList = ['divider', 'text', 'link', 'alert', 'groupTitle', 'relationFormAttr', 'popupAttr', 'button', 'barcode', 'qrcode', 'calculate', 'iframe']
// 不可以添加到子表组件
const noTableAllowList = ['divider', 'text', 'link', 'alert', 'groupTitle', 'button', 'barcode', 'qrcode', 'editor', 'radio', 'checkbox', 'createUser', 'createTime', 'modifyUser', 'modifyTime', 'currOrganize', 'currDept', 'currPosition', 'PsdInput', 'colorPicker', 'iframe']
// 不可以添加到列表展示
const noColumnShowList = ['PsdInput', 'colorPicker', 'divider', 'editor', 'text', 'relationFormAttr', 'popupAttr', 'groupTitle']
// 不可以添加到搜索
const noSearchList = [...noColumnShowList, 'switch', 'uploadImg', 'uploadFile', 'timeRange', 'dateRange', 'relationForm', 'popupSelect', 'popupTableSelect', 'sign']
// 搜索时控件为input
const useInputList = ['input', 'textarea', 'text', 'link', 'billRule', 'location']
// 搜索时控件为日期选择器
const useDateList = ['createTime', 'modifyTime']
// 搜索时控件为下拉选择器
const useSelectList = ['radio', 'checkbox', 'select']
// 系统控件
const systemComponentsList = ['createUser', 'createTime', 'modifyUser', 'modifyTime', 'currOrganize', 'currPosition', 'billRule']
// 不允许关联到联动里面的控件
const noAllowRelationList = ['table', 'uploadImg', 'uploadFile', 'modifyUser', 'modifyTime']
// 不允许分组、排序、导入导出
const noGroupList = ['sign', 'location', 'uploadImg', 'uploadFile'];
const calculateItem = {
  __config__: {
    jnpfKey: 'calculate',
    label: '计算公式',
    tipLabel: "",
    labelWidth: undefined,
    showLabel: true,
    required: false,
    tag: 'JnpfCalculate',
    tagIcon: 'icon-ym icon-ym-generator-count',
    tableAlign: 'left',
    tableFixed: 'none',
    className: [],
    defaultValue: null,
    layout: 'colFormItem',
    span: 24,
    dragDisabled: false,
    visibility: ["pc", "app"],
    tableName: '',
    noShow: false,
    regList: []
  },
  style: { width: "100%" },
  expression: [],
  thousands: false,
  isAmountChinese: false,
  precision: 2,
  isStorage: 0,
}
// 在线开发-功能设计/流程设计/移动设计独有组件
const onlinePeculiarList = [{
  __config__: {
    jnpfKey: 'qrcode',
    label: '二维码',
    tipLabel: "",
    labelWidth: undefined,
    showLabel: true,
    tag: 'JnpfQrcode',
    tagIcon: 'icon-ym icon-ym-generator-qrcode',
    tableAlign: 'left',
    tableFixed: 'none',
    className: [],
    defaultValue: '',
    layout: 'colFormItem',
    span: 24,
    dragDisabled: false,
    visibility: ["pc", "app"],
    tableName: '',
    regList: [],
  },
  colorDark: '#000',
  colorLight: '#fff',
  width: 100,
  dataType: 'static',
  staticText: '二维码',
  relationField: '',
},
{
  __config__: {
    jnpfKey: 'barcode',
    label: '条形码',
    tipLabel: "",
    labelWidth: undefined,
    showLabel: true,
    tag: 'JnpfBarcode',
    tagIcon: 'icon-ym icon-ym-generator-barcode',
    tableAlign: 'left',
    tableFixed: 'none',
    className: [],
    defaultValue: '',
    layout: 'colFormItem',
    span: 24,
    dragDisabled: false,
    visibility: ["pc", "app"],
    tableName: '',
    regList: [],
  },
  format: 'code128',
  lineColor: '#000',
  background: '#fff',
  width: 4,
  height: 40,
  dataType: 'static',
  staticText: '20220810',
  relationField: '',
},
]
export { dyOptionsList, noVModelList, noTableAllowList, noColumnShowList, noSearchList, calculateItem, onlinePeculiarList, useInputList, useDateList, useSelectList, systemComponentsList, noAllowRelationList, noGroupList }