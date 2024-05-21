// 表单属性【右面板】
export const formConf = {
  formRef: "formRef",
  formModel: "dataForm",
  size: "small",
  labelPosition: "right",
  labelWidth: 100,
  labelSuffix: '', // 标题后缀
  formRules: "rules",
  popupType: "general",
  generalWidth: "600px",
  fullScreenWidth: "100%",
  drawerWidth: "600px",
  gutter: 15,
  disabled: false,
  span: 24,
  formBtns: false,
  hasCancelBtn: true,
  cancelButtonText: "取 消",
  hasConfirmBtn: true,
  confirmButtonText: "确 定",
  hasPrintBtn: false,
  hasConfirmAndAddBtn: true,
  confirmAndAddText: '确定并继续操作',
  printButtonText: "打 印",
  primaryKeyPolicy: 1,
  concurrencyLock: false,
  logicalDelete: false,
  printId: "",
  formStyle: "",
  classNames: [],
  className: [],
  classJson: "",
  funcs: {
    onLoad:
      "({ formData, setFormData, setShowOrHide, setRequired, setDisabled, onlineUtils }) => {\n    // 在此编写代码\n    \n}",
    beforeSubmit:
      "({ formData, setFormData, setShowOrHide, setRequired, setDisabled, onlineUtils }) => {\n    return new Promise((resolve, reject) => {\n        // 在此编写代码\n        \n        // 继续执行\n        resolve()\n    })\n}",
    afterSubmit:
      "({ formData, setFormData, setShowOrHide, setRequired, setDisabled, onlineUtils }) => {\n    // 在此编写代码\n    \n}"
  },
  idGlobal: 100
};

// 基础控件 【左面板】
export const inputComponents = [
  {
    __config__: {
      jnpfKey: "input",
      label: "单行输入",
      tipLabel: "",
      labelWidth: undefined,
      showLabel: true,
      tag: "JnpfInput",
      tagIcon: "icon-ym icon-ym-generator-input",
      tableAlign: 'left',
      tableFixed: 'none',
      className: [],
      defaultValue: undefined,
      required: false,
      layout: "colFormItem",
      span: 24,
      dragDisabled: false,
      visibility: ["pc", "app"],
      tableName: "",
      noShow: false,
      unique: false,
      regList: [],
      trigger: "blur"
    },
    on: {
      change:
        "({ data, rowIndex, formData, setFormData, setShowOrHide, setRequired, setDisabled, onlineUtils }) => {\n    // 在此编写代码\n    \n}",
      blur:
        "({ data, rowIndex, formData, setFormData, setShowOrHide, setRequired, setDisabled, onlineUtils }) => {\n    // 在此编写代码\n    \n}"
    },
    placeholder: "请输入",
    style: { width: "100%" },
    useScan: false,
    useMask: false,
    maskConfig: {
      filler: '*',
      maskType: 1,
      prefixType: 1,
      prefixLimit: 0,
      prefixSpecifyChar: '',
      suffixType: 1,
      suffixLimit: 0,
      suffixSpecifyChar: '',
      ignoreChar: '',
      useUnrealMask: false,
      unrealMaskLength: 1,
    },
    clearable: true,
    addonBefore: '',
    addonAfter: '',
    prefixIcon: '',
    suffixIcon: '',
    maxlength: null,
    showWordLimit: false,
    showPassword: false,
    readonly: false,
    disabled: false
  },
  {
    __config__: {
      jnpfKey: "textarea",
      label: "多行输入",
      tipLabel: "",
      labelWidth: undefined,
      showLabel: true,
      tag: "JnpfTextarea",
      tagIcon: "icon-ym icon-ym-generator-textarea",
      tableAlign: 'left',
      tableFixed: 'none',
      className: [],
      defaultValue: undefined,
      required: false,
      layout: "colFormItem",
      span: 24,
      dragDisabled: false,
      visibility: ["pc", "app"],
      tableName: "",
      noShow: false,
      regList: [],
      trigger: "blur"
    },
    on: {
      change:
        "({ data, rowIndex, formData, setFormData, setShowOrHide, setRequired, setDisabled, onlineUtils }) => {\n    // 在此编写代码\n    \n}",
      blur:
        "({ data, rowIndex, formData, setFormData, setShowOrHide, setRequired, setDisabled, onlineUtils }) => {\n    // 在此编写代码\n    \n}"
    },
    type: "textarea",
    placeholder: "请输入",
    autoSize: {
      minRows: 4,
      maxRows: 4
    },
    style: { width: "100%" },
    useScan: false,
    useMask: false,
    maskConfig: {
      filler: '*',
      maskType: 1,
      prefixType: 1,
      prefixLimit: 0,
      prefixSpecifyChar: '',
      suffixType: 1,
      suffixLimit: 0,
      suffixSpecifyChar: '',
      ignoreChar: '',
      useUnrealMask: false,
      unrealMaskLength: 1,
    },
    maxlength: null,
    showWordLimit: true,
    readonly: false,
    disabled: false,
    readonly: false,
  },
  {
    __config__: {
      jnpfKey: "inputNumber",
      label: "数字输入",
      tipLabel: "",
      showLabel: true,
      labelWidth: undefined,
      tag: "JnpfInputNumber",
      tagIcon: "icon-ym icon-ym-generator-number",
      tableAlign: 'left',
      tableFixed: 'none',
      className: [],
      defaultValue: undefined,
      required: false,
      layout: "colFormItem",
      span: 24,
      dragDisabled: false,
      visibility: ["pc", "app"],
      tableName: "",
      noShow: false,
      regList: [],
      trigger: ["blur", "change"]
    },
    on: {
      change:
        "({ data, rowIndex, formData, setFormData, setShowOrHide, setRequired, setDisabled, onlineUtils }) => {\n    // 在此编写代码\n    \n}",
      blur:
        "({ data, rowIndex, formData, setFormData, setShowOrHide, setRequired, setDisabled, onlineUtils }) => {\n    // 在此编写代码\n    \n}"
    },
    style: { width: null },
    placeholder: "数字文本",
    min: undefined,
    max: undefined,
    step: 1,
    "step-strictly": false,
    precision: undefined,
    controls: false,
    controlsPosition: "",
    addonBefore: null,
    addonAfter: null,
    thousands: false,
    isAmountChinese: false,
    disabled: false
  },
  {
    __config__: {
      jnpfKey: "switch",
      label: "开关",
      tipLabel: "",
      labelWidth: undefined,
      showLabel: true,
      tag: "JnpfSwitch",
      tagIcon: "icon-ym icon-ym-generator-switch",
      tableAlign: 'left',
      tableFixed: 'none',
      className: [],
      defaultValue: false,
      required: false,
      layout: "colFormItem",
      span: 24,
      dragDisabled: false,
      visibility: ["pc", "app"],
      tableName: "",
      noShow: false,
      regList: [],
      trigger: "change"
    },
    on: {
      change:
        "({ data, rowIndex, formData, setFormData, setShowOrHide, setRequired, setDisabled, onlineUtils }) => {\n    // 在此编写代码\n    \n}"
    },
    disabled: false,
    activeTxt: "开",
    inactiveTxt: "关",
    activeColor: null,
    inactiveColor: null,
    activeValue: 1,
    inactiveValue: 0
  },
  {
    __config__: {
      jnpfKey: "radio",
      label: "单选框组",
      tipLabel: "",
      labelWidth: undefined,
      showLabel: true,
      tag: "JnpfRadio",
      tagIcon: "icon-ym icon-ym-generator-radio",
      tableAlign: 'left',
      tableFixed: 'none',
      className: [],
      defaultValue: undefined,
      required: false,
      layout: "colFormItem",
      span: 24,
      dragDisabled: false,
      visibility: ["pc", "app"],
      tableName: "",
      noShow: false,
      regList: [],
      trigger: "change",
      dataType: "static",
      dictionaryType: "",
      propsUrl: "",
      propsName: "",
      templateJson: [],
    },
    on: {
      change:
        "({ data, rowIndex, formData, setFormData, setShowOrHide, setRequired, setDisabled, onlineUtils }) => {\n    // 在此编写代码\n    \n}"
    },
    style: {},
    size: "small",
    disabled: false,
    props: {
      label: "fullName",
      value: "id"
    },
    options: [
      {
        fullName: "选项一",
        id: "1"
      },
      {
        fullName: "选项二",
        id: "2"
      }
    ],
    direction: 'horizontal',
    optionType: "default",
    border: false,
  },
  {
    __config__: {
      jnpfKey: "checkbox",
      label: "多选框组",
      tipLabel: "",
      labelWidth: undefined,
      showLabel: true,
      tag: "JnpfCheckbox",
      tagIcon: "icon-ym icon-ym-generator-checkbox",
      tableAlign: 'left',
      tableFixed: 'none',
      className: [],
      defaultValue: [],
      required: false,
      layout: "colFormItem",
      span: 24,
      dragDisabled: false,
      visibility: ["pc", "app"],
      tableName: "",
      noShow: false,
      regList: [],
      trigger: "change",
      dataType: "static",
      dictionaryType: "",
      propsUrl: "",
      propsName: "",
      templateJson: [],
    },
    on: {
      change:
        "({ data, rowIndex, formData, setFormData, setShowOrHide, setRequired, setDisabled, onlineUtils }) => {\n    // 在此编写代码\n    \n}"
    },
    style: {},
    size: "small",
    optionType: "default",
    border: false,
    props: {
      label: "fullName",
      value: "id"
    },
    options: [
      {
        fullName: "选项一",
        id: "1"
      },
      {
        fullName: "选项二",
        id: "2"
      }
    ],
    direction: 'horizontal',
    disabled: false
  },
  {
    __config__: {
      jnpfKey: "select",
      label: "下拉选择",
      tipLabel: "",
      labelWidth: undefined,
      showLabel: true,
      tag: "JnpfSelect",
      tagIcon: "icon-ym icon-ym-generator-select",
      tableAlign: 'left',
      tableFixed: 'none',
      className: [],
      defaultValue: "",
      required: false,
      layout: "colFormItem",
      span: 24,
      dragDisabled: false,
      visibility: ["pc", "app"],
      tableName: "",
      noShow: false,
      regList: [],
      trigger: "change",
      dataType: "static",
      dictionaryType: "",
      propsUrl: "",
      propsName: "",
      templateJson: [],
    },
    props: {
      label: "fullName",
      value: "id"
    },
    options: [
      {
        fullName: "选项一",
        id: "1"
      },
      {
        fullName: "选项二",
        id: "2"
      }
    ],
    on: {
      change:
        "({ data, rowIndex, formData, setFormData, setShowOrHide, setRequired, setDisabled, onlineUtils }) => {\n    // 在此编写代码\n    \n}"
    },
    placeholder: "请选择",
    style: { width: "100%" },
    clearable: true,
    disabled: false,
    filterable: false,
    multiple: false
  },
  {
    __config__: {
      jnpfKey: "cascader",
      label: "级联选择",
      tipLabel: "",
      labelWidth: undefined,
      showLabel: true,
      tag: "JnpfCascader",
      tagIcon: "icon-ym icon-ym-generator-cascader",
      tableAlign: 'left',
      tableFixed: 'none',
      className: [],
      defaultValue: [],
      required: false,
      layout: "colFormItem",
      span: 24,
      dragDisabled: false,
      visibility: ["pc", "app"],
      tableName: "",
      noShow: false,
      regList: [],
      trigger: "change",
      dataType: "static",
      propsUrl: "",
      propsName: "",
      templateJson: [],
      dictionaryType: ""
    },
    options: [
      {
        id: "1",
        fullName: "选项1",
        children: [
          {
            id: "2",
            fullName: "选项1-1"
          }
        ]
      }
    ],
    on: {
      change:
        "({ data, rowIndex, formData, setFormData, setShowOrHide, setRequired, setDisabled, onlineUtils }) => {\n    // 在此编写代码\n    \n}",
      blur:
        "({ data, rowIndex, formData, setFormData, setShowOrHide, setRequired, setDisabled, onlineUtils }) => {\n    // 在此编写代码\n    \n}"
    },
    placeholder: "请选择",
    style: { width: "100%" },
    props: {
      value: "id",
      label: "fullName",
      children: "children"
    },
    multiple: false,
    showAllLevels: true,
    disabled: false,
    clearable: true,
    filterable: false,
    separator: "/"
  },
  {
    __config__: {
      jnpfKey: "datePicker",
      label: "日期选择",
      tipLabel: "",
      labelWidth: undefined,
      showLabel: true,
      tag: "JnpfDatePicker",
      tagIcon: "icon-ym icon-ym-generator-date",
      tableAlign: 'left',
      tableFixed: 'none',
      className: [],
      noShow: false,
      defaultValue: null,
      defaultCurrent: false,
      required: false,
      layout: "colFormItem",
      span: 24,
      dragDisabled: false,
      startTimeRule: false,
      startTimeType: 1,
      startTimeTarget: 1,
      startTimeValue: "",
      endTimeRule: false,
      endTimeType: 1,
      endTimeTarget: 1,
      startRelationField: "",
      endRelationField: "",
      endTimeValue: "",
      visibility: ["pc", "app"],
      tableName: "",
      regList: [],
      trigger: "change"
    },
    on: {
      change:
        "({ data, rowIndex, formData, setFormData, setShowOrHide, setRequired, setDisabled, onlineUtils }) => {\n    // 在此编写代码\n    \n}",
      blur:
        "({ data, rowIndex, formData, setFormData, setShowOrHide, setRequired, setDisabled, onlineUtils }) => {\n    // 在此编写代码\n    \n}"
    },
    placeholder: "请选择",
    type: "date",
    style: { width: "100%" },
    disabled: false,
    clearable: true,
    format: "yyyy-MM-dd",
    startTime: null,
    endTime: null,
    readonly: false,
  },
  {
    __config__: {
      jnpfKey: "timePicker",
      label: "时间选择",
      tipLabel: "",
      labelWidth: undefined,
      showLabel: true,
      tag: "JnpfTimePicker",
      tagIcon: "icon-ym icon-ym-generator-time",
      tableAlign: 'left',
      tableFixed: 'none',
      className: [],
      noShow: false,
      defaultValue: null,
      required: false,
      defaultCurrent: false,
      layout: "colFormItem",
      span: 24,
      dragDisabled: false,
      startTimeRule: false,
      startTimeType: 1,
      startTimeTarget: 1,
      startTimeValue: "",
      endTimeRule: false,
      endTimeType: 1,
      endTimeTarget: 1,
      endTimeValue: "",
      startRelationField: "",
      endRelationField: "",
      visibility: ["pc", "app"],
      tableName: "",
      regList: [],
      trigger: "change"
    },
    on: {
      change:
        "({ data, rowIndex, formData, setFormData, setShowOrHide, setRequired, setDisabled, onlineUtils }) => {\n    // 在此编写代码\n    \n}",
      blur:
        "({ data, rowIndex, formData, setFormData, setShowOrHide, setRequired, setDisabled, onlineUtils }) => {\n    // 在此编写代码\n    \n}"
    },
    placeholder: "请选择",
    style: { width: "100%" },
    disabled: false,
    clearable: true,
    startTime: null,
    endTime: null,
    readonly: false,
    format: "HH:mm:ss",
  },
  {
    __config__: {
      jnpfKey: "uploadFile",
      label: "文件上传",
      tipLabel: "",
      labelWidth: undefined,
      showLabel: true,
      tag: "JnpfUploadFile",
      tagIcon: "icon-ym icon-ym-generator-upload",
      tableAlign: 'left',
      tableFixed: 'none',
      className: [],
      defaultValue: [],
      required: false,
      layout: "colFormItem",
      span: 24,
      dragDisabled: false,
      visibility: ["pc", "app"],
      tableName: "",
      noShow: false,
      regList: [],
      trigger: "change"
    },
    on: {
      change:
        "({ data, rowIndex, formData, setFormData, setShowOrHide, setRequired, setDisabled, onlineUtils }) => {\n    // 在此编写代码\n    \n}"
    },
    disabled: false,
    accept: "",
    fileSize: 10,
    sizeUnit: "MB",
    buttonText: "点击上传",
    showTip: false,
    pathType: "defaultPath",
    isAccount: 0,
    folder: "",
    tipText: '',
    limit: 9
  },
  {
    __config__: {
      jnpfKey: "uploadImg",
      label: "图片上传",
      tipLabel: "",
      labelWidth: undefined,
      showLabel: true,
      tag: "JnpfUploadImg",
      tagIcon: "icon-ym icon-ym-generator-upload",
      tableAlign: 'left',
      tableFixed: 'none',
      className: [],
      defaultValue: [],
      required: false,
      layout: "colFormItem",
      span: 24,
      dragDisabled: false,
      visibility: ["pc", "app"],
      tableName: "",
      noShow: false,
      regList: [],
      trigger: "change"
    },
    on: {
      change:
        "({ data, rowIndex, formData, setFormData, setShowOrHide, setRequired, setDisabled, onlineUtils }) => {\n    // 在此编写代码\n    \n}"
    },
    disabled: false,
    accept: "",
    showTip: false,
    fileSize: 10,
    sizeUnit: "MB",
    pathType: "defaultPath",
    isAccount: 0,
    folder: "",
    tipText: '',
    limit: 9
  },
  {
    __config__: {
      jnpfKey: "colorPicker",
      label: "颜色选择",
      tipLabel: "",
      labelWidth: undefined,
      showLabel: true,
      tag: "JnpfColorPicker",
      tagIcon: "icon-ym icon-ym-generator-color",
      tableAlign: 'left',
      tableFixed: 'none',
      className: [],
      defaultValue: null,
      required: false,
      layout: "colFormItem",
      span: 24,
      dragDisabled: false,
      visibility: ["pc", "app"],
      tableName: "",
      noShow: false,
      regList: [],
      trigger: "change"
    },
    showAlpha: false,
    colorFormat: "",
    disabled: false,
    size: "small"
  },
  {
    __config__: {
      jnpfKey: "rate",
      label: "评分",
      tipLabel: "",
      labelWidth: undefined,
      showLabel: true,
      tag: "JnpfRate",
      tagIcon: "icon-ym icon-ym-generator-rate",
      tableAlign: 'left',
      tableFixed: 'none',
      className: [],
      defaultValue: 0,
      required: false,
      layout: "colFormItem",
      span: 24,
      dragDisabled: false,
      visibility: ["pc", "app"],
      tableName: "",
      noShow: false,
      regList: [],
      trigger: "change"
    },
    on: {
      change:
        "({ data, rowIndex, formData, setFormData, setShowOrHide, setRequired, setDisabled, onlineUtils }) => {\n    // 在此编写代码\n    \n}"
    },
    style: {},
    count: 5,
    allowHalf: false,
    showText: false,
    showScore: false,
    disabled: false
  },
  {
    __config__: {
      jnpfKey: "slider",
      label: "滑块",
      tipLabel: "",
      labelWidth: undefined,
      showLabel: true,
      tag: "JnpfSlider",
      tagIcon: "icon-ym icon-ym-generator-slider",
      tableAlign: 'left',
      tableFixed: 'none',
      className: [],
      defaultValue: undefined,
      required: false,
      layout: "colFormItem",
      span: 24,
      dragDisabled: false,
      visibility: ["pc", "app"],
      tableName: "",
      noShow: false,
      regList: [],
      trigger: "change"
    },
    on: {
      change:
        "({ data, rowIndex, formData, setFormData, setShowOrHide, setRequired, setDisabled, onlineUtils }) => {\n    // 在此编写代码\n    \n}"
    },
    style: { width: "100%" },
    disabled: false,
    min: 0,
    max: 100,
    step: 1,
    showStops: false,
    range: false
  },
  {
    __config__: {
      jnpfKey: "editor",
      label: "富文本",
      tipLabel: "",
      labelWidth: undefined,
      showLabel: true,
      tag: "JnpfEditor",
      tagIcon: "icon-ym icon-ym-generator-rich-text",
      tableAlign: 'left',
      tableFixed: 'none',
      className: [],
      defaultValue: null,
      required: false,
      layout: "colFormItem",
      span: 24,
      dragDisabled: false,
      visibility: ["pc", "app"],
      tableName: "",
      noShow: false,
      regList: [],
      trigger: "blur"
    },
    style: { width: "100%" },
    placeholder: "请输入",
    disabled: false
  },
  {
    __config__: {
      jnpfKey: "link",
      label: "链接",
      labelWidth: undefined,
      showLabel: false,
      tag: "JnpfLink",
      tagIcon: "icon-ym icon-ym-generator-link",
      tableAlign: 'left',
      tableFixed: 'none',
      className: [],
      required: false,
      layout: "colFormItem",
      span: 24,
      dragDisabled: false,
      visibility: ["pc", "app"]
    },
    on: {
      click:
        "({ data, rowIndex, formData, setFormData, setShowOrHide, setRequired, setDisabled, onlineUtils }) => {\n    // 在此编写代码\n    \n}"
    },
    content: '文本链接',
    href: "",
    target: "_self",
    textStyle: {
      "text-align": "left",
    }
  },
  {
    __config__: {
      jnpfKey: "button",
      label: "按钮",
      tipLabel: "",
      labelWidth: undefined,
      showLabel: false,
      tag: "JnpfButton",
      tagIcon: "icon-ym icon-ym-generator-button",
      tableAlign: 'left',
      tableFixed: 'none',
      className: [],
      defaultValue: null,
      required: false,
      layout: "colFormItem",
      span: 24,
      dragDisabled: false,
      visibility: ["pc", "app"],
      regList: [],
      trigger: "click"
    },
    on: {
      click:
        "({ data, rowIndex, formData, setFormData, setShowOrHide, setRequired, setDisabled, onlineUtils }) => {\n    // 在此编写代码\n    \n}"
    },
    align: "left",
    buttonText: "按钮",
    type: "",
    disabled: false
  },
  {
    __config__: {
      jnpfKey: "text",
      label: "文本",
      tipLabel: "",
      labelWidth: undefined,
      showLabel: false,
      tag: "JnpfText",
      tagIcon: "icon-ym icon-ym-generator-textarea",
      tableAlign: 'left',
      tableFixed: 'none',
      className: [],
      defaultValue: undefined,
      required: false,
      layout: "colFormItem",
      span: 24,
      dragDisabled: false,
      visibility: ["pc", "app"]
    },
    textStyle: {
      color: "#000000",
      "text-align": "left",
      "font-weight": "normal",
      "font-style": "normal",
      "text-decoration": "none",
      "line-height": 32,
      "font-size": 12
    },
    content: '这是一段文字',
  },
  {
    __config__: {
      jnpfKey: "alert",
      label: "提示",
      tipLabel: "",
      labelWidth: undefined,
      showLabel: false,
      tag: "JnpfAlert",
      tagIcon: "icon-ym icon-ym-generator-alert",
      tableAlign: 'left',
      tableFixed: 'none',
      className: [],
      required: false,
      layout: "colFormItem",
      span: 24,
      dragDisabled: false,
      visibility: ["pc", "app"]
    },
    title: '这是一个提示',
    type: "success",
    showIcon: false,
    closable: true,
    description: '',
    closeText: '',
  },
];

// 高级控件 【左面板】
export const selectComponents = [
  {
    __config__: {
      jnpfKey: "organizeSelect",
      label: "组织选择",
      tipLabel: "",
      labelWidth: undefined,
      showLabel: true,
      tag: "JnpfOrganizeSelect",
      tagIcon: "icon-ym icon-ym-generator-company",
      tableAlign: 'left',
      tableFixed: 'none',
      className: [],
      defaultValue: [],
      defaultCurrent: false,
      required: false,
      layout: "colFormItem",
      span: 24,
      dragDisabled: false,
      visibility: ["pc", "app"],
      tableName: "",
      noShow: false,
      regList: [],
      trigger: "change"
    },
    on: {
      change:
        "({ data, rowIndex, formData, setFormData, setShowOrHide, setRequired, setDisabled, onlineUtils }) => {\n    // 在此编写代码\n    \n}"
    },
    style: { width: "100%" },
    placeholder: "请选择",
    multiple: false,
    clearable: true,
    filterable: false,
    disabled: false,
    ableIds: [],
    selectType: 'all',
  },
  {
    __config__: {
      jnpfKey: "depSelect",
      label: "部门选择",
      tipLabel: "",
      labelWidth: undefined,
      showLabel: true,
      tag: "JnpfDepSelect",
      tagIcon: "icon-ym icon-ym-generator-department",
      tableAlign: 'left',
      tableFixed: 'none',
      className: [],
      defaultValue: null,
      defaultCurrent: false,
      required: false,
      layout: "colFormItem",
      span: 24,
      dragDisabled: false,
      visibility: ["pc", "app"],
      tableName: "",
      noShow: false,
      regList: [],
      trigger: "change"
    },
    on: {
      change:
        "({ data, rowIndex, formData, setFormData, setShowOrHide, setRequired, setDisabled, onlineUtils }) => {\n    // 在此编写代码\n    \n}"
    },
    style: { width: "100%" },
    placeholder: "请选择",
    selectType: 'all',
    ableIds: [],
    multiple: false,
    clearable: true,
    filterable: false,
    disabled: false
  },
  {
    __config__: {
      jnpfKey: "posSelect",
      label: "岗位选择",
      tipLabel: "",
      labelWidth: undefined,
      showLabel: true,
      tag: "JnpfPosSelect",
      tagIcon: "icon-ym icon-ym-generator-jobs",
      tableAlign: 'left',
      tableFixed: 'none',
      className: [],
      defaultValue: null,
      defaultCurrent: false,
      required: false,
      layout: "colFormItem",
      span: 24,
      dragDisabled: false,
      visibility: ["pc", "app"],
      tableName: "",
      noShow: false,
      regList: [],
      trigger: "change"
    },
    on: {
      change:
        "({ data, rowIndex, formData, setFormData, setShowOrHide, setRequired, setDisabled, onlineUtils }) => {\n    // 在此编写代码\n    \n}"
    },
    style: { width: "100%" },
    placeholder: "请选择",
    selectType: 'all',
    ableIds: [],
    multiple: false,
    clearable: true,
    filterable: false,
    disabled: false
  },
  {
    __config__: {
      jnpfKey: "userSelect",
      label: "用户选择",
      tipLabel: "",
      labelWidth: undefined,
      showLabel: true,
      tag: "JnpfUserSelect",
      tagIcon: "icon-ym icon-ym-generator-user",
      tableAlign: 'left',
      tableFixed: 'none',
      className: [],
      defaultValue: null,
      defaultCurrent: false,
      required: false,
      layout: "colFormItem",
      span: 24,
      dragDisabled: false,
      visibility: ["pc", "app"],
      tableName: "",
      noShow: false,
      regList: [],
      trigger: "change",
      relationField: '',
    },
    on: {
      change:
        "({ data, rowIndex, formData, setFormData, setShowOrHide, setRequired, setDisabled, onlineUtils }) => {\n    // 在此编写代码\n    \n}"
    },
    style: { width: "100%" },
    placeholder: "请选择",
    ableIds: [],
    selectType: 'all',
    relationField: '',
    multiple: false,
    clearable: true,
    disabled: false
  },
  {
    __config__: {
      jnpfKey: "roleSelect",
      label: "角色选择",
      tipLabel: "",
      labelWidth: undefined,
      defaultCurrent: false,
      showLabel: true,
      tag: "JnpfRoleSelect",
      tagIcon: "icon-ym icon-ym-generator-role",
      tableAlign: 'left',
      tableFixed: 'none',
      className: [],
      defaultValue: null,
      required: false,
      layout: "colFormItem",
      span: 24,
      dragDisabled: false,
      visibility: ["pc", "app"],
      tableName: "",
      noShow: false,
      regList: [],
      trigger: "change"
    },
    on: {
      change:
        "({ data, rowIndex, formData, setFormData, setShowOrHide, setRequired, setDisabled, onlineUtils }) => {\n    // 在此编写代码\n    \n}"
    },
    style: { width: "100%" },
    placeholder: "请选择",
    multiple: false,
    clearable: true,
    disabled: false,
    ableIds: [],
    selectType: 'all',
  },
  {
    __config__: {
      jnpfKey: "groupSelect",
      label: "分组选择",
      tipLabel: "",
      labelWidth: undefined,
      showLabel: true,
      tag: "JnpfGroupSelect",
      tagIcon: "icon-ym icon-ym-generator-group1",
      tableAlign: 'left',
      tableFixed: 'none',
      className: [],
      defaultValue: '',
      defaultCurrent: false,
      required: false,
      layout: "colFormItem",
      span: 24,
      dragDisabled: false,
      visibility: ["pc", "app"],
      tableName: "",
      noShow: false,
      regList: [],
      trigger: "change"
    },
    on: {
      change:
        "({ data, rowIndex, formData, setFormData, setShowOrHide, setRequired, setDisabled, onlineUtils }) => {\n    // 在此编写代码\n    \n}"
    },
    style: { width: "100%" },
    placeholder: "请选择",
    multiple: false,
    clearable: true,
    filterable: false,
    disabled: false,
    ableIds: [],
    selectType: 'all',
  },
  {
    __config__: {
      jnpfKey: "usersSelect",
      label: "用户组件",
      tipLabel: "",
      labelWidth: undefined,
      showLabel: true,
      tag: "JnpfUsersSelect",
      tagIcon: "icon-ym icon-ym-generator-founder",
      tableAlign: 'left',
      tableFixed: 'none',
      className: [],
      defaultValue: null,
      defaultCurrent: false,
      required: false,
      layout: "colFormItem",
      span: 24,
      dragDisabled: false,
      visibility: ["pc", "app"],
      tableName: "",
      noShow: false,
      regList: [],
      trigger: "change"
    },
    on: {
      change:
        "({ data, rowIndex, formData, setFormData, setShowOrHide, setRequired, setDisabled, onlineUtils }) => {\n    // 在此编写代码\n    \n}"
    },
    placeholder: "请选择",
    selectType: 'all',
    ableIds: [],
    multiple: false,
    clearable: true,
    disabled: false
  },
  {
    __config__: {
      jnpfKey: "table",
      label: "设计子表",
      tipLabel: "",
      showLabel: false,
      tagIcon: "icon-ym icon-ym-generator-table",
      tableAlign: 'left',
      tableFixed: 'none',
      className: [],
      tag: "JnpfInputTable",
      defaultValue: [],
      layout: "rowFormItem",
      dragDisabled: false,
      visibility: ["pc", "app"],
      showTitle: true,
      type: "table",
      rowType: "table",
      children: [],
      tableName: "",
      complexHeaderList: [],
      fixed: 'none',
      align: "left"
    },
    showAddBtn: true,
    showDeleteBtn: true,
    disabled: false,
    actionText: "添加",
    showSummary: false,
    addType: 0,
    summaryField: [],
    thousands: false,
    thousandsField: [],
    tableConf: {},
    defaultValue: [],
    columnBtnsList: [
      { value: 'copy', label: '复制', show: true, btnType: 'primary', btnIcon: 'icon-ym icon-ym-btn-edit' },
      { value: 'remove', label: '删除', show: true, btnType: 'danger', btnIcon: 'icon-ym icon-ym-btn-clearn', showConfirm: true },
    ],
    footerBtnsList: [
      { value: 'add', label: '添加', show: true, btnType: 'primary', btnIcon: 'icon-ym icon-ym-btn-add' },
      { value: 'batchRemove', label: '批量删除', show: true, btnType: 'danger', btnIcon: 'icon-ym icon-ym-btn-clearn', showConfirm: true },
    ],
  },
  {
    __config__: {
      jnpfKey: "treeSelect",
      label: "下拉树形",
      tipLabel: "",
      labelWidth: undefined,
      showLabel: true,
      tag: "JnpfTreeSelect",
      tagIcon: "icon-ym icon-ym-generator-tree",
      tableAlign: 'left',
      tableFixed: 'none',
      className: [],
      defaultValue: null,
      required: false,
      layout: "colFormItem",
      span: 24,
      dragDisabled: false,
      visibility: ["pc", "app"],
      tableName: "",
      noShow: false,
      regList: [],
      trigger: "change",
      dataType: "static",
      dictionaryType: "",
      propsUrl: "",
      propsName: "",
      templateJson: [],
    },
    on: {
      change:
        "({ data, rowIndex, formData, setFormData, setShowOrHide, setRequired, setDisabled, onlineUtils }) => {\n    // 在此编写代码\n    \n}"
    },
    style: { width: "100%" },
    placeholder: "请选择",
    props: {
      value: "id",
      label: "fullName",
      children: "children"
    },
    options: [
      {
        id: "1",
        fullName: "选项1",
        children: [
          {
            id: "2",
            fullName: "选项1-1"
          }
        ]
      }
    ],
    multiple: false,
    clearable: true,
    filterable: false,
    disabled: false
  },
  {
    __config__: {
      jnpfKey: "popupTableSelect",
      label: "下拉表格",
      tipLabel: "",
      labelWidth: undefined,
      showLabel: true,
      required: false,
      tag: "JnpfPopupTableSelect",
      tagIcon: "icon-ym icon-ym-generator-popupTableSelect",
      tableAlign: 'left',
      tableFixed: 'none',
      className: [],
      defaultValue: "",
      noShow: false,
      layout: "colFormItem",
      span: 24,
      dragDisabled: false,
      visibility: ["pc", "app"],
      tableName: "",
      regList: [],
      trigger: "change"
    },
    on: {
      change:
        "({ data, rowIndex, formData, setFormData, setShowOrHide, setRequired, setDisabled, onlineUtils }) => {\n    // 在此编写代码\n    \n}"
    },
    style: { width: "100%" },
    placeholder: "请选择",
    interfaceId: "",
    interfaceName: "",
    templateJson: [],
    hasPage: false,
    pageSize: 20,
    columnOptions: [],
    propsValue: "id",
    relationField: "fullName",
    popupType: "popover",
    popupTitle: "选择数据",
    popupWidth: "800px",
    disabled: false,
    clearable: true,
    multiple: false,
    filterable: false,
    interfaceHasPage: 0
  },
  {
    __config__: {
      jnpfKey: "autoComplete",
      label: "下拉补全",
      tipLabel: "",
      labelWidth: undefined,
      showLabel: true,
      tag: "JnpfAutoComplete",
      tagIcon: "icon-ym icon-ym-generator-autoComplete",
      tableAlign: 'left',
      tableFixed: 'none',
      className: [],
      defaultValue: undefined,
      required: false,
      layout: "colFormItem",
      span: 24,
      dragDisabled: false,
      visibility: ["pc", "app"],
      tableName: "",
      noShow: false,
      unique: false,
      regList: [],
      trigger: "blur"
    },
    __slot__: {
      prepend: "",
      append: ""
    },
    on: {
      change:
        "({ data, rowIndex, formData, setFormData, setShowOrHide, setRequired, setDisabled, onlineUtils }) => {\n    // 在此编写代码\n    \n}",
      blur:
        "({ data, rowIndex, formData, setFormData, setShowOrHide, setRequired, setDisabled, onlineUtils }) => {\n    // 在此编写代码\n    \n}"
    },
    placeholder: "请输入",
    style: { width: "100%" },
    total: 10,
    relationField: "fullName",
    interfaceId: "",
    interfaceName: "",
    templateJson: [],
    clearable: true,
    maxlength: null,
    readonly: false,
    disabled: false
  },
  {
    __config__: {
      jnpfKey: "areaSelect",
      label: "省市区域",
      tipLabel: "",
      labelWidth: undefined,
      showLabel: true,
      tag: "JnpfAreaSelect",
      tagIcon: "icon-ym icon-ym-generator-Provinces",
      tableAlign: 'left',
      tableFixed: 'none',
      className: [],
      defaultValue: [],
      required: false,
      layout: "colFormItem",
      span: 24,
      noShow: false,
      dragDisabled: false,
      visibility: ["pc", "app"],
      tableName: "",
      regList: [],
      trigger: "change"
    },
    on: {
      change:
        "({ data, rowIndex, formData, setFormData, setShowOrHide, setRequired, setDisabled, onlineUtils }) => {\n    // 在此编写代码\n    \n}"
    },
    style: { width: "100%" },
    placeholder: "请选择",
    disabled: false,
    ableAddressIds: [],
    selectType: 'all',
    clearable: true,
    filterable: false,
    multiple: false,
    level: 2
  },
  {
    __config__: {
      jnpfKey: "relationForm",
      label: "关联表单",
      tipLabel: "",
      labelWidth: undefined,
      showLabel: true,
      tag: "JnpfRelationForm",
      tagIcon: "icon-ym icon-ym-generator-menu",
      tableAlign: 'left',
      tableFixed: 'none',
      className: [],
      defaultValue: "",
      required: false,
      layout: "colFormItem",
      span: 24,
      dragDisabled: false,
      noShow: false,
      visibility: ["pc", "app"],
      tableName: "",
      regList: [],
      trigger: "change",
      transferList: []
    },
    on: {
      change:
        "({ data, rowIndex, formData, setFormData, setShowOrHide, setRequired, setDisabled, onlineUtils }) => {\n    // 在此编写代码\n    \n}"
    },
    style: { width: "100%" },
    placeholder: "请选择",
    modelId: "",
    relationField: "",
    hasPage: false,
    pageSize: 20,
    columnOptions: [],
    clearable: true,
    popupType: "dialog",
    popupTitle: "选择数据",
    popupWidth: "800px",
    filterable: false,
    disabled: false
  },
  {
    __config__: {
      jnpfKey: "relationFormAttr",
      label: "关联表单属性",
      tipLabel: "",
      labelWidth: undefined,
      showLabel: true,
      tag: "JnpfRelationFormAttr",
      tagIcon: "icon-ym icon-ym-generator-nature",
      tableAlign: 'left',
      tableFixed: 'none',
      className: [],
      noShow: false,
      defaultValue: "",
      required: false,
      layout: "colFormItem",
      span: 24,
      dragDisabled: false,
      visibility: ["pc", "app"]
    },
    style: { width: "100%" },
    showField: "",
    relationField: "",
    isStorage: 0,
  },
  {
    __config__: {
      jnpfKey: "popupSelect",
      label: "弹窗选择",
      tipLabel: "",
      labelWidth: undefined,
      showLabel: true,
      required: false,
      tag: "JnpfPopupSelect",
      tagIcon: "icon-ym icon-ym-generator-popup",
      tableAlign: 'left',
      tableFixed: 'none',
      className: [],
      defaultValue: "",
      layout: "colFormItem",
      span: 24,
      dragDisabled: false,
      visibility: ["pc", "app"],
      tableName: "",
      noShow: false,
      regList: [],
      trigger: "change",
      transferList: []
    },
    on: {
      change:
        "({ data, rowIndex, formData, setFormData, setShowOrHide, setRequired, setDisabled, onlineUtils }) => {\n    // 在此编写代码\n    \n}"
    },
    style: { width: "100%" },
    placeholder: "请选择",
    interfaceId: "",
    interfaceName: "",
    templateJson: [],
    hasPage: false,
    pageSize: 20,
    columnOptions: [],
    propsValue: "id",
    relationField: "fullName",
    popupType: "dialog",
    popupTitle: "选择数据",
    popupWidth: "800px",
    disabled: false,
    clearable: true,
    interfaceHasPage: 0
  },
  {
    __config__: {
      jnpfKey: "popupAttr",
      label: "弹窗选择属性",
      tipLabel: "",
      labelWidth: undefined,
      showLabel: true,
      tag: "JnpfPopupAttr",
      tagIcon: "icon-ym icon-ym-generator-popup-attr",
      tableAlign: 'left',
      tableFixed: 'none',
      className: [],
      noShow: false,
      defaultValue: "",
      required: false,
      layout: "colFormItem",
      span: 24,
      dragDisabled: false,
      visibility: ["pc", "app"]
    },
    style: { width: "100%" },
    showField: "",
    relationField: "",
    isStorage: 0,
  },
  {
    __config__: {
      jnpfKey: "billRule",
      label: "单据组件",
      tipLabel: "",
      labelWidth: undefined,
      showLabel: true,
      tag: "JnpfInput",
      tagIcon: "icon-ym icon-ym-generator-documents",
      tableAlign: 'left',
      tableFixed: 'none',
      className: [],
      defaultValue: null,
      layout: "colFormItem",
      required: false,
      span: 24,
      dragDisabled: false,
      visibility: ["pc", "app"],
      tableName: "",
      noShow: false,
      trigger: "change",
      rule: "",
      ruleName: "",
    },
    style: { width: "100%" },
    readonly: true,
    placeholder: "系统自动生成"
  },
  {
    __config__: {
      jnpfKey: 'sign',
      label: '手写签名',
      tipLabel: '',
      labelWidth: undefined,
      showLabel: true,
      tag: 'JnpfSign',
      tagIcon: 'icon-ym icon-ym-signature',
      tableAlign: 'left',
      tableFixed: 'none',
      className: [],
      defaultValue: '',
      defaultCurrent: false,
      required: false,
      layout: 'colFormItem',
      span: 24,
      dragDisabled: false,
      visibility: ['pc', 'app'],
      tableName: '',
      noShow: false,
      regList: [],
      trigger: 'change',
    },
    on: {
      change: '({ data, rowIndex, formData, setFormData, setShowOrHide, setRequired, setDisabled, onlineUtils }) => {\n    // 在此编写代码\n    \n}',
    },
    clearable: true,
    disabled: false,
  },
  {
    __config__: {
      jnpfKey: 'location',
      label: '定位',
      tipLabel: '',
      labelWidth: undefined,
      showLabel: true,
      tag: 'JnpfLocation',
      tagIcon: 'icon-ym icon-ym-portal-mapChart',
      tableAlign: 'left',
      tableFixed: 'none',
      className: [],
      defaultValue: undefined,
      required: false,
      layout: 'colFormItem',
      span: 24,
      dragDisabled: false,
      visibility: ['pc', 'app'],
      tableName: '',
      noShow: false,
      regList: [],
      trigger: 'change',
    },
    on: {
      change: '({ data, rowIndex, formData, setFormData, setShowOrHide, setRequired, setDisabled, onlineUtils }) => {\n    // 在此编写代码\n    \n}',
    },
    autoLocation: false,
    adjustmentScope: 500,
    enableLocationScope: false,
    enableDesktopLocation: false,
    locationScope: undefined,
    clearable: true,
    disabled: false,
  },
  {
    __config__: {
      jnpfKey: 'iframe',
      label: 'Iframe',
      labelWidth: undefined,
      showLabel: true,
      tag: 'JnpfIframe',
      tagIcon: 'icon-ym icon-ym-generator-iframe',
      tableAlign: 'left',
      tableFixed: 'none',
      className: [],
      defaultValue: null,
      required: false,
      layout: 'colFormItem',
      span: 24,
      dragDisabled: false,
      visibility: ['pc'],
      noShow: false,
    },
    href: '',
    height: 300,
    borderType: 'solid',
    borderColor: '#E2E0E0',
    borderWidth: 1
  },
];

// 系统控件 【左面板】
export const systemComponents = [
  {
    __config__: {
      jnpfKey: "createUser",
      label: "创建人员",
      tipLabel: "",
      labelWidth: undefined,
      showLabel: true,
      tag: "JnpfOpenData",
      tagIcon: "icon-ym icon-ym-generator-founder",
      tableAlign: 'left',
      tableFixed: 'none',
      className: [],
      defaultValue: "",
      required: false,
      layout: "colFormItem",
      span: 24,
      dragDisabled: false,
      visibility: ["pc", "app"],
      tableName: "",
      noShow: false
    },
    style: { width: "100%" },
    type: 'currUser',
    readonly: true,
    placeholder: ""
  },
  {
    __config__: {
      jnpfKey: "createTime",
      label: "创建时间",
      tipLabel: "",
      labelWidth: undefined,
      showLabel: true,
      tag: "JnpfOpenData",
      tagIcon: "icon-ym icon-ym-generator-createtime",
      tableAlign: 'left',
      tableFixed: 'none',
      className: [],
      defaultValue: "",
      layout: "colFormItem",
      required: false,
      span: 24,
      dragDisabled: false,
      visibility: ["pc", "app"],
      tableName: "",
      noShow: false
    },
    style: { width: "100%" },
    type: 'currTime',
    readonly: true,
    placeholder: ""
  },
  {
    __config__: {
      jnpfKey: "modifyUser",
      label: "修改人员",
      tipLabel: "",
      labelWidth: undefined,
      showLabel: true,
      tag: "JnpfInput",
      tagIcon: "icon-ym icon-ym-generator-modifier",
      tableAlign: 'left',
      tableFixed: 'none',
      className: [],
      defaultValue: "",
      required: false,
      layout: "colFormItem",
      span: 24,
      dragDisabled: false,
      visibility: ["pc", "app"],
      tableName: "",
      noShow: false
    },
    style: { width: "100%" },
    readonly: true,
    placeholder: "系统自动生成"
  },
  {
    __config__: {
      jnpfKey: "modifyTime",
      label: "修改时间",
      tipLabel: "",
      labelWidth: undefined,
      showLabel: true,
      tag: "JnpfInput",
      tagIcon: "icon-ym icon-ym-generator-modifytime",
      tableAlign: 'left',
      tableFixed: 'none',
      className: [],
      defaultValue: "",
      required: false,
      layout: "colFormItem",
      span: 24,
      dragDisabled: false,
      visibility: ["pc", "app"],
      tableName: "",
      noShow: false
    },
    style: { width: "100%" },
    readonly: true,
    placeholder: "系统自动生成"
  },
  {
    __config__: {
      jnpfKey: "currOrganize",
      label: "所属组织",
      tipLabel: "",
      labelWidth: undefined,
      showLabel: true,
      tag: "JnpfOpenData",
      tagIcon: "icon-ym icon-ym-generator-company",
      tableAlign: 'left',
      tableFixed: 'none',
      className: [],
      defaultValue: "",
      required: false,
      layout: "colFormItem",
      span: 24,
      dragDisabled: false,
      visibility: ["pc", "app"],
      tableName: "",
      noShow: false
    },
    style: { width: "100%" },
    type: 'currOrganize',
    readonly: true,
    showLevel: "last",
    placeholder: ""
  },
  {
    __config__: {
      jnpfKey: "currPosition",
      label: "所属岗位",
      tipLabel: "",
      labelWidth: undefined,
      showLabel: true,
      tag: "JnpfOpenData",
      tagIcon: "icon-ym icon-ym-generator-station",
      tableAlign: 'left',
      tableFixed: 'none',
      className: [],
      defaultValue: "",
      required: false,
      layout: "colFormItem",
      span: 24,
      dragDisabled: false,
      visibility: ["pc", "app"],
      tableName: "",
      noShow: false
    },
    style: { width: "100%" },
    type: 'currPosition',
    readonly: true,
    placeholder: ""
  }
];

// 布局控件 【左面板】
export const layoutComponents = [
  {
    __config__: {
      jnpfKey: "groupTitle",
      label: "分组标题",
      labelWidth: undefined,
      showLabel: false,
      tag: "JnpfGroupTitle",
      tagIcon: "icon-ym icon-ym-generator-group",
      tableAlign: 'left',
      tableFixed: 'none',
      className: [],
      defaultValue: null,
      required: false,
      layout: "colFormItem",
      span: 24,
      dragDisabled: false,
      visibility: ["pc", "app"]
    },
    content: "分组标题",
    helpMessage: "",
    contentPosition: "left"
  },
  {
    __config__: {
      jnpfKey: "divider",
      label: "分割线",
      tipLabel: "",
      labelWidth: undefined,
      showLabel: false,
      tag: "JnpfDivider",
      tagIcon: "icon-ym icon-ym-generator-divider",
      tableAlign: 'left',
      tableFixed: 'none',
      className: [],
      defaultValue: null,
      required: false,
      layout: "colFormItem",
      span: 24,
      dragDisabled: false,
      visibility: ["pc", "app"]
    },
    content: '我是分割线',
    contentPosition: "center"
  },
  {
    __config__: {
      jnpfKey: "collapse",
      label: "折叠面板",
      tipLabel: "",
      showLabel: false,
      tag: "el-collapse",
      tagIcon: "icon-ym icon-ym-generator-fold",
      tableAlign: 'left',
      tableFixed: 'none',
      className: [],
      layout: "rowFormItem",
      span: "24",
      dragDisabled: false,
      visibility: ["pc", "app"],
      children: [
        {
          title: "面板1",
          name: "1",
          __config__: {
            jnpfKey: "collapseItem",
            children: [],
          }
        },
        {
          title: "面板2",
          name: "2",
          __config__: {
            jnpfKey: "collapseItem",
            children: [],
          }
        }
      ],
      active: ["1"]
    },
    on: {
      change:
        "({ data, rowIndex, formData, setFormData, setShowOrHide, setRequired, setDisabled, onlineUtils }) => {\n    // 在此编写代码\n    \n}"
    },
    accordion: false
  },
  {
    __config__: {
      jnpfKey: "tab",
      label: "标签面板",
      tipLabel: "",
      showLabel: false,
      tag: "el-tab",
      tagIcon: "icon-ym icon-ym-generator-label",
      tableAlign: 'left',
      tableFixed: 'none',
      className: [],
      layout: "rowFormItem",
      span: "24",
      dragDisabled: false,
      visibility: ["pc", "app"],
      children: [
        {
          title: "Tab 1",
          name: '1',
          __config__: {
            jnpfKey: "tabItem",
            children: [],
          }
        },
        {
          title: "Tab 2",
          name: '2',
          __config__: {
            jnpfKey: "tabItem",
            children: [],
          }
        }
      ],
      active: "1"
    },
    on: {
      "tabClick":
        "({ data, rowIndex, formData, setFormData, setShowOrHide, setRequired, setDisabled, onlineUtils }) => {\n    // 在此编写代码\n    \n}"
    },
    type: "",
    tabPosition: "top"
  },
  {
    __config__: {
      jnpfKey: "row",
      label: "栅格容器",
      tipLabel: "",
      tagIcon: "icon-ym icon-ym-generator-layout",
      tableAlign: 'left',
      tableFixed: 'none',
      className: [],
      layout: "rowFormItem",
      span: "24",
      dragDisabled: false,
      visibility: ["pc", "app"],
      layoutTree: true,
      rowType: "layout"
    },
    type: "default",
    justify: "start",
    align: "top"
  },
  {
    __config__: {
      jnpfKey: "card",
      label: "卡片容器",
      tipLabel: "",
      showLabel: false,
      tag: "el-card",
      tagIcon: "icon-ym icon-ym-generator-card",
      tableAlign: 'left',
      tableFixed: 'none',
      className: [],
      defaultValue: [],
      layout: "rowFormItem",
      span: "24",
      dragDisabled: false,
      visibility: ["pc", "app"],
      children: []
    },
    header: "卡片容器",
    shadow: "always"
  },
  {
    __config__: {
      jnpfKey: "tableGrid",
      label: "表格容器",
      showLabel: false,
      tagIcon: "icon-ym icon-ym-generator-tableGrid",
      tableAlign: 'left',
      tableFixed: 'none',
      className: [],
      defaultValue: [],
      layout: "rowFormItem",
      span: "24",
      dragDisabled: false,
      visibility: ["pc", "app"],
      borderType: "solid",
      borderColor: '#E2E0E0',
      borderWidth: 1,
      rowType: "tableGrid",
      children: [
        {
          __config__: {
            jnpfKey: "tableGridTr",
            children: [
              {
                __config__: {
                  jnpfKey: "tableGridTd",
                  merged: false,
                  rowType: "tableGrid",
                  colspan: 1,
                  rowspan: 1,
                  children: [],
                  backgroundColor: '',
                }
              }
            ]
          },
        }
      ]
    },
  },
];
