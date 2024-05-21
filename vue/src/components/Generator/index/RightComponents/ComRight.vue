<template>
  <el-row>
    <el-form-item label="控件标题">
      <el-input v-model="activeData.__config__.label" placeholder="请输入控件标题" />
    </el-form-item>
    <el-form-item label="显示标题">
      <el-switch v-model="activeData.__config__.showLabel" />
    </el-form-item>
    <el-form-item label="标题提示">
      <el-input type="textarea" :rows="2" v-model="activeData.__config__.tipLabel"
        placeholder="请输入标题提示" />
    </el-form-item>
    <el-form-item label="占位提示" v-if="activeData.__config__.jnpfKey !=='editor'">
      <el-input v-model="activeData.placeholder" placeholder="请输入占位提示" />
    </el-form-item>
    <el-form-item label="可选范围"
      v-if="['depSelect','posSelect','usersSelect','organizeSelect','roleSelect','groupSelect'].includes(activeData.__config__.jnpfKey)">
      <el-select v-model="activeData.selectType" placeholder="请选择" @change="onTypeChange"
        filterable>
        <el-option v-for="item in selectTypeOptions" :key="item.value" :label="item.label"
          :value="item.value" />
      </el-select>
      <template v-if="activeData.selectType === 'custom'">
        <template v-if="activeData.__config__.jnpfKey==='depSelect'">
          <org-Select v-model="activeData.ableIds" title="添加部门" type="department" class="mt-10" />
        </template>
        <template v-if="activeData.__config__.jnpfKey==='organizeSelect'">
          <org-Select v-model="activeData.ableIds" title="添加组织" type="organize" class="mt-10" />
        </template>
        <template v-if="activeData.__config__.jnpfKey==='roleSelect'">
          <org-Select v-model="activeData.ableIds" title="添加角色" type="role" class="mt-10" />
        </template>
        <template v-if="activeData.__config__.jnpfKey==='groupSelect'">
          <org-Select v-model="activeData.ableIds" title="添加分组" type="group" class="mt-10" />
        </template>
        <template v-if="activeData.__config__.jnpfKey==='posSelect'">
          <org-Select v-model="activeData.ableIds" title="添加岗位" type="position" class="mt-10" />
        </template>
        <template v-if="activeData.__config__.jnpfKey==='usersSelect'">
          <org-Select v-model="activeData.ableIds" title="添加用户" type="users" class="mt-10"
            :dynamicParameter="true" />
        </template>
      </template>
    </el-form-item>
    <el-form-item label="可选范围" v-if="activeData.__config__.jnpfKey==='userSelect'">
      <el-select v-model="activeData.selectType" placeholder="请选择" @change="onTypeChange"
        filterable>
        <el-option v-for="item in userSelectTypeOptions" :key="item.value" :label="item.label"
          :value="item.value" />
      </el-select>
      <template v-if="activeData.selectType==='custom'">
        <org-Select v-model="activeData.ableIds" title="添加用户" type="users" class="mt-10"
          :dynamicParameter="true" />
      </template>
      <template v-if="['dep','pos','role','group'].includes(activeData.selectType)">
        <el-radio-group v-model="activeData.relationField" class="relation-radio">
          <el-radio :label="item.realVModel" v-for="(item,i) in formFieldsOptions" :key="i">
            {{item.realLabel}}</el-radio>
        </el-radio-group>
      </template>
    </el-form-item>
    <el-form-item label="默认值">
      <JnpfOrganizeSelect v-model="activeData.__config__.defaultValue" placeholder="选择默认值" clearable
        v-if="activeData.__config__.jnpfKey==='organizeSelect'" :multiple="activeData.multiple"
        :key="key" :selectType="activeData.selectType"
        :disabled="activeData.__config__.defaultCurrent" :ableIds="activeData.ableIds" />
      <JnpfDepSelect v-model="activeData.__config__.defaultValue" placeholder="选择默认值" clearable
        v-if="activeData.__config__.jnpfKey==='depSelect'" :multiple="activeData.multiple"
        :key="key" :selectType="activeData.selectType" :ableIds="activeData.ableIds"
        :disabled="activeData.__config__.defaultCurrent" />
      <JnpfPosSelect v-model="activeData.__config__.defaultValue" placeholder="选择默认值" clearable
        v-if="activeData.__config__.jnpfKey==='posSelect'" :multiple="activeData.multiple"
        :key="key" :selectType="activeData.selectType" :ableIds="activeData.ableIds"
        :disabled="activeData.__config__.defaultCurrent" />
      <JnpfUserSelect v-model="activeData.__config__.defaultValue" placeholder="选择默认值" clearable
        v-if="activeData.__config__.jnpfKey==='userSelect'" :multiple="activeData.multiple"
        :key="key" :selectType="activeData.selectType" :ableIds="activeData.ableIds"
        :disabled="activeData.__config__.defaultCurrent" />
      <JnpfUsersSelect v-model="activeData.__config__.defaultValue" placeholder="选择默认值" clearable
        v-if="activeData.__config__.jnpfKey==='usersSelect'" :multiple="activeData.multiple"
        :key="key" :selectType="activeData.selectType" :ableIds="activeData.ableIds"
        :disabled="activeData.__config__.defaultCurrent" />
      <JnpfGroupSelect v-model="activeData.__config__.defaultValue" placeholder="选择默认值" clearable
        v-if="activeData.__config__.jnpfKey==='groupSelect'" :multiple="activeData.multiple"
        :ableIds="activeData.ableIds" :selectType="activeData.selectType"
        :disabled="activeData.__config__.defaultCurrent" />
      <JnpfRoleSelect v-model="activeData.__config__.defaultValue" placeholder="选择默认值" clearable
        v-if="activeData.__config__.jnpfKey==='roleSelect'" :multiple="activeData.multiple"
        :selectType="activeData.selectType" :ableIds="activeData.ableIds" :key="key"
        :disabled="activeData.__config__.defaultCurrent" />
      <el-input :value="setDefaultValue(activeData.__config__.defaultValue)" placeholder="请输入默认值"
        @input="onDefaultValueInput" v-if="activeData.__config__.jnpfKey==='editor'" />
    </el-form-item>
    <el-form-item v-if="activeData.__config__.jnpfKey!=='editor'">
      <el-checkbox label=" true" v-model="activeData.__config__.defaultCurrent"
        :disabled="activeData.selectType === 'custom'" :key="key"
        @change="defaultCurrentChange(activeData.__config__.defaultCurrent, activeData.__config__.jnpfKey)"
        class="currentCheckbox">
        <span>
          默认为当前登录{{getLabel(activeData.__config__.jnpfKey)}}
        </span>
      </el-checkbox>
    </el-form-item>
    <el-form-item label="能否清空" v-if="activeData.__config__.jnpfKey!=='editor'">
      <el-switch v-model="activeData.clearable" />
    </el-form-item>
    <el-form-item label="能否多选" v-if="activeData.__config__.jnpfKey!=='editor'">
      <el-switch v-model="activeData.multiple" @change="multipleChange" />
    </el-form-item>
    <el-form-item label="是否禁用">
      <el-switch v-model="activeData.disabled" />
    </el-form-item>
    <el-form-item label="是否隐藏">
      <el-switch v-model="activeData.__config__.noShow" />
    </el-form-item>
    <el-form-item label="是否必填">
      <el-switch v-model="activeData.__config__.required" />
    </el-form-item>
  </el-row>
</template>
<script>
import comMixin from './mixin';
import OrgSelect from '@/components/Process/OrgSelect'
import { getDrawingList } from '@/components/Generator/utils/db'
const selectTypeOptions = [{
  value: 'all',
  label: '全部'
}, {
  value: 'custom',
  label: '自定义'
}]
const userSelectTypeOptions = [
  ...selectTypeOptions,
  {
    value: 'dep',
    label: '部门组件联动'
  },
  {
    value: 'pos',
    label: '岗位组件联动'
  },
  {
    value: 'role',
    label: '角色组件联动'
  },
  {
    value: 'group',
    label: '分组组件联动'
  },
]
export default {
  props: ['activeData'],
  mixins: [comMixin],
  components: {
    OrgSelect
  },
  data() {
    return {
      selectTypeOptions,
      userSelectTypeOptions,
      relationOptions: [],
      key: +new Date()
    }
  },
  computed: {
    formFieldsOptions() {
      if (this.activeData.selectType === 'all' || this.activeData.selectType === 'custom') return []
      let list = []
      const loop = (data, parent) => {
        if (!data) return
        if (data.__config__ && this.isIncludesTable(data) && data.__config__.children && Array.isArray(data.__config__.children)) {
          loop(data.__config__.children, data)
        }
        if (Array.isArray(data)) data.forEach(d => loop(d, parent))
        if (data.__vModel__ && data.__config__.jnpfKey === `${this.activeData.selectType}Select` && data.__vModel__ !== this.activeData.__vModel__) {
          const isTableChild = parent && parent.__config__ && parent.__config__.jnpfKey === 'table'
          list.push({
            realVModel: isTableChild ? parent.__vModel__ + '-' + data.__vModel__ : data.__vModel__,
            realLabel: isTableChild ? parent.__config__.label + '-' + data.__config__.label : data.__config__.label,
            ...data
          })
        }
      }
      loop(getDrawingList())
      return list
    }
  },
  created() { },
  methods: {
    onTypeChange() {
      this.$set(this.activeData.__config__, 'defaultValue', this.activeData.multiple ? [] : '')
      if (this.activeData.selectType === 'custom') this.$set(this.activeData.__config__, 'defaultCurrent', false)
      this.activeData.ableIds = []
      if (this.activeData.__config__.jnpfKey === 'userSelect') {
        this.activeData.relationField = ''
        if (this.activeData.selectType != 'all' && this.activeData.selectType != 'custom') this.$set(this.activeData.__config__, 'defaultValue', null)
      }
      this.activeData.__config__.renderKey = +new Date()
      this.key = +new Date()
    },
    getLabel(key) {
      if (key === 'organizeSelect') return '组织';
      if (key === 'depSelect') return '部门';
      if (key === 'posSelect') return '岗位';
      if (key === 'roleSelect') return '角色';
      if (key === 'groupSelect') return '分组';
      return '用户';
    },
    isIncludesTable(data) {
      if ((!data.__config__.layout || data.__config__.layout === 'rowFormItem') && data.__config__.jnpfKey !== 'table') return true
      if (this.activeData.__config__.isSubTable) return this.activeData.__config__.parentVModel === data.__vModel__
      return data.__config__.jnpfKey !== 'table'
    },
    multipleChange(val) {
      this.$set(this.activeData.__config__, 'defaultValue', val ? [] : '')
      this.activeData.__config__.renderKey = +new Date()
      this.key = +new Date()
    },
    defaultCurrentChange(val, jnpfKey) {
      if (val) {
        if (jnpfKey === 'organizeSelect') {
          this.$set(this.activeData.__config__, 'defaultValue', [])
        } else {
          this.$set(this.activeData.__config__, 'defaultValue', null)
        }
      }
      this.key = +new Date()
    }
  }
}
</script>
<style lang="scss" scoped>
.currentCheckbox {
  float: left;
  margin-top: -15px;
}
.relation-radio {
  .el-radio {
    margin: 10px 0 0;
    display: block;
  }
}
</style>
