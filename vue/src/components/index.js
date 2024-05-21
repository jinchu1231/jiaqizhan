
import topOperation from '@/components/JNPF-topOperation/index'
import tableOperation from '@/components/JNPF-tableOperation'
import Pagination from '@/components/Pagination'
import JNPFTable from '@/components/JNPF-table'
import jnpfFormTipItem from '@/components/jnpf-form-tip-item'
import UserBox from '@/components/JNPF-userBox'
import Screenfull from '@/components/Screenfull'
import ColumnSettings from '@/components/ColumnSettings'
import uploadBtn from '@/components/JNPF-uploadBtn'
import ImportBox from '@/components/JNPFImport'
// 代码生成器专供
import JnpfInput from '@/components/Jnpf/Input'
import JnpfTextarea from '@/components/Jnpf/Textarea'
import JnpfSwitch from '@/components/Jnpf/Switch'
import JnpfCascader from '@/components/Jnpf/Cascader'
import JnpfColorPicker from '@/components/Jnpf/ColorPicker'
import JnpfRate from '@/components/Jnpf/Rate'
import JnpfSlider from '@/components/Jnpf/Slider'
import JnpfAlert from '@/components/Jnpf/Alert'
import JnpfDivider from '@/components/Jnpf/Divider'
import JnpfLink from '@/components/Jnpf/Link'
import JnpfInputNumber from '@/components/Jnpf/InputNumber'
import JnpfRadio from '@/components/Jnpf/Radio'
import JnpfCheckbox from '@/components/Jnpf/Checkbox'
import JnpfSelect from '@/components/Jnpf/Select'
import JnpfTreeSelect from '@/components/Jnpf/TreeSelect'
import JnpfDatePicker from '@/components/Jnpf/DatePicker'
import JnpfTimePicker from '@/components/Jnpf/TimePicker'
import JnpfUploadFile from '@/components/Jnpf/Upload/UploadFile'
import JnpfUploadImg from '@/components/Jnpf/Upload/UploadImg'
import JnpfEditor from '@/components/JNPFEditor'
import JnpfButton from '@/components/Jnpf/Button'
import JnpfText from '@/components/Jnpf/Text'
import JnpfBarcode from '@/components/Jnpf/Barcode'
import JnpfQrcode from '@/components/Jnpf/Qrcode'
import JnpfOrganizeSelect from '@/components/Jnpf/OrganizeSelect'
import JnpfGroupSelect from '@/components/Jnpf/GroupSelect'
import JnpfDepSelect from '@/components/Jnpf/DepSelect'
import JnpfPosSelect from '@/components/Jnpf/PosSelect'
import JnpfUserSelect from '@/components/Jnpf/UserSelect'
import JnpfUsersSelect from '@/components/Jnpf/UsersSelect'
import JnpfRoleSelect from '@/components/Jnpf/RoleSelect'
import JnpfInputTable from '@/components/Jnpf/InputTable'
import JnpfPopupSelect from '@/components/Jnpf/PopupSelect'
import JnpfPopupTableSelect from '@/components/Jnpf/PopupTableSelect'
import JnpfPopupAttr from '@/components/Jnpf/PopupAttr'
import JnpfRelationForm from '@/components/Jnpf/RelationForm'
import JnpfRelationFormAttr from '@/components/Jnpf/RelationFormAttr'
import JnpfAreaSelect from '@/components/Jnpf/AreaSelect'
import JnpfGroupTitle from '@/components/Jnpf/GroupTitle'
import JnpfCalculate from '@/components/Jnpf/Calculate'
import JnpfAutoComplete from '@/components/Jnpf/AutoComplete'
import JnpfOpenData from '@/components/Jnpf/OpenData'

import NumRange from '@/components/Generator/components/NumRange'
import DicSelect from '@/components/Generator/components/DicSelect'
import BillRule from '@/components/Generator/components/BillRule'
import JnpfNumber from '@/components/Generator/components/JnpfNumber'
import JnpfDateRangePicker from '@/components/Generator/components/JnpfDateRangePicker'
import Location from '@/components/Generator/components/Location'
import Iframe from '@/components/Generator/components/Iframe'
import Sign from '@/components/Generator/components/Sign'
export default {
  install(Vue, options) {

    Vue.component('topOpts', topOperation)
    Vue.component('tableOpts', tableOperation)
    Vue.component('Pagination', Pagination)
    Vue.component('JNPFTable', JNPFTable)
    Vue.component('jnpfFormTipItem', jnpfFormTipItem)
    Vue.component('uploadBtn', uploadBtn)
    Vue.component('UserBox', UserBox)
    Vue.component('NumRange', NumRange)
    Vue.component('DicSelect', DicSelect)
    Vue.component('BillRule', BillRule)
    Vue.component('Screenfull', Screenfull)
    Vue.component('ColumnSettings', ColumnSettings)
    Vue.component('ImportBox', ImportBox)
    Vue.component('JnpfInputNumber', JnpfInputNumber)
    Vue.component('JnpfNumber', JnpfNumber)
    Vue.component('JnpfDateRangePicker', JnpfDateRangePicker)

    Vue.component('JnpfInput', JnpfInput)
    Vue.component('JnpfTextarea', JnpfTextarea)
    Vue.component('JnpfSwitch', JnpfSwitch)
    Vue.component('JnpfCascader', JnpfCascader)
    Vue.component('JnpfColorPicker', JnpfColorPicker)
    Vue.component('JnpfRate', JnpfRate)
    Vue.component('JnpfSlider', JnpfSlider)
    Vue.component('JnpfAlert', JnpfAlert)
    Vue.component('JnpfDivider', JnpfDivider)
    Vue.component('JnpfLink', JnpfLink)
    Vue.component('JnpfRadio', JnpfRadio)
    Vue.component('JnpfCheckbox', JnpfCheckbox)
    Vue.component('JnpfSelect', JnpfSelect)
    Vue.component('JnpfTreeSelect', JnpfTreeSelect)
    Vue.component('JnpfDatePicker', JnpfDatePicker)
    Vue.component('JnpfTimePicker', JnpfTimePicker)
    Vue.component('JnpfUploadFile', JnpfUploadFile)
    Vue.component('JnpfUploadImg', JnpfUploadImg)
    Vue.component('JnpfEditor', JnpfEditor)
    Vue.component('JnpfText', JnpfText)
    Vue.component('JnpfBarcode', JnpfBarcode)
    Vue.component('JnpfQrcode', JnpfQrcode)
    Vue.component('JnpfButton', JnpfButton)
    Vue.component('JnpfOrganizeSelect', JnpfOrganizeSelect)
    Vue.component('JnpfGroupSelect', JnpfGroupSelect)
    Vue.component('JnpfDepSelect', JnpfDepSelect)
    Vue.component('JnpfPosSelect', JnpfPosSelect)
    Vue.component('JnpfUserSelect', JnpfUserSelect)
    Vue.component('JnpfUsersSelect', JnpfUsersSelect)
    Vue.component('JnpfRoleSelect', JnpfRoleSelect)
    Vue.component('JnpfInputTable', JnpfInputTable)
    Vue.component('JnpfPopupSelect', JnpfPopupSelect)
    Vue.component('JnpfPopupTableSelect', JnpfPopupTableSelect)
    Vue.component('JnpfPopupAttr', JnpfPopupAttr)
    Vue.component('JnpfRelationForm', JnpfRelationForm)
    Vue.component('JnpfRelationFormAttr', JnpfRelationFormAttr)
    Vue.component('JnpfAutoComplete', JnpfAutoComplete)
    Vue.component('JnpfAreaSelect', JnpfAreaSelect)
    Vue.component('JnpfGroupTitle', JnpfGroupTitle)
    Vue.component('JnpfCalculate', JnpfCalculate)
    Vue.component('JnpfOpenData', JnpfOpenData)
    Vue.component('JnpfIframe', Iframe)
    Vue.component('JnpfSign', Sign)
    Vue.component('JnpfLocation', Location)

  }
}