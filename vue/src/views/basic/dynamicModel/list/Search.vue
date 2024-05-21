<template>
  <el-row class="JNPF-common-search-box" :gutter="16" v-if="searchList.length">
    <el-form @submit.native.prevent>
      <el-col :span="6" v-for="(item, index) in searchList" :key="index">
        <el-form-item :label="item.label" v-show="index<3 || (index>=3 && showAll)">
          <template v-if="useInputList.includes(item.__config__.jnpfKey)">
            <el-input v-model="item.value"
              :placeholder="item.__config__.placeholder?item.__config__.placeholder:'请输入'+item.label"
              clearable class="item" :key="key">
            </el-input>
          </template>
          <template v-else-if="useDateList.includes(item.__config__.jnpfKey)">
            <el-date-picker v-model="item.value" value-format="timestamp"
              format="yyyy-MM-dd HH:mm:ss" start-placeholder="开始日期" end-placeholder="结束日期"
              class="item" type="datetimerange" clearable />
          </template>
          <template v-else-if="useSelectList.includes(item.__config__.jnpfKey)">
            <el-select v-model="item.value" :placeholder="'请选择'+item.label" clearable filterable
              class="item" :multiple="item.searchMultiple">
              <el-option :label="oItem[item.props.label]" v-for="(oItem, i) in item.options"
                :value="oItem[item.props.value]" :key="i"></el-option>
            </el-select>
          </template>
          <template v-else>
            <template
              v-if="item.__config__.jnpfKey==='inputNumber' || item.__config__.jnpfKey==='slider'">
              <JnpfInputNumber v-model="item.value" placeholder="请输入" clearable
                v-if="item.__config__.isFromParam" controls controls-position='right' />
              <num-range v-model="item.value" :precision="item.precision" :key="key"
                v-else></num-range>
            </template>
            <template v-if="item.__config__.jnpfKey==='rate'">
              <JnpfInputNumber v-model="item.value" placeholder="请输入" clearable
                v-if="item.__config__.isFromParam" controls controls-position='right' />
              <num-range v-model="item.value" :precision="item.allowHalf ? 1 : 0" :key="key"
                v-else></num-range>
            </template>
            <template v-if="item.__config__.jnpfKey==='calculate'">
              <num-range v-model="item.value" :precision="item.precision||0"></num-range>
            </template>
            <template v-if="item.__config__.jnpfKey==='cascader'">
              <JnpfCascader v-model="item.value" :options="item.options" clearable
                :show-all-levels="item.showAllLevels" :props="item.props" filterable
                :placeholder="'请选择'+item.label" class="item"></JnpfCascader>
            </template>
            <template
              v-if="item.__config__.jnpfKey==='timePicker'|| item.__config__.jnpfKey==='timeRange'">
              <el-time-picker v-model="item.value" start-placeholder="开始时间" end-placeholder="结束时间"
                clearable :value-format="item.format" :format="item.format" is-range class="item"
                :key="key" />
            </template>
            <template v-if="item.__config__.jnpfKey==='datePicker'">
              <JnpfDatePicker style="width:100%" v-model="item.value" placeholder="请选择参数值"
                format="yyyy-MM-dd HH:mm:ss" clearable v-if="item.__config__.isFromParam" />
              <JnpfDateRangePicker v-model="item.value" :type="item.type" clearable
                :format="item.format" startPlaceholder="开始日期" endPlaceholder="结束日期" class="item"
                v-else>
              </JnpfDateRangePicker>
            </template>
            <template v-if="item.__config__.jnpfKey==='dateRange'">
              <el-date-picker v-model="item.value" :type="item.type" clearable
                value-format="timestamp" :format="item.format" start-placeholder="开始日期"
                end-placeholder="结束日期" class="item">
              </el-date-picker>
            </template>
            <template v-if="item.__config__.jnpfKey==='areaSelect'">
              <JnpfAreaSelect v-model="item.value" :placeholder="'请选择'+item.label"
                :level="item.level" class="item" clearable />
            </template>
            <template v-if="item.__config__.jnpfKey==='treeSelect'">
              <JnpfTreeSelect v-model="item.value" :placeholder="'请选择'+item.label"
                :options="item.options" :props="item.props" class="item" clearable />
            </template>
            <template
              v-if="item.__config__.jnpfKey==='createUser'||item.__config__.jnpfKey==='modifyUser'">
              <JnpfUserSelect v-model="item.value" :placeholder="'请选择'+item.label" clearable
                class="item" :ableIds="item.ableIds" :selectType="item.selectType" />
            </template>
            <template
              v-if="item.__config__.jnpfKey==='currOrganize'||item.__config__.jnpfKey==='organizeSelect'">
              <JnpfOrganizeSelect v-model="item.value" :placeholder="'请选择'+item.label" clearable
                class="item" :multiple="item.searchMultiple" :ableIds="item.ableIds"
                :selectType="item.selectType" />
            </template>
            <template v-if="item.__config__.jnpfKey==='currDept'">
              <JnpfDepSelect v-model="item.value" :placeholder="'请选择'+item.label" clearable
                class="item" :ableIds="item.ableIds" :selectType="item.selectType" />
            </template>
            <template
              v-if="item.__config__.jnpfKey==='currPosition' || item.__config__.jnpfKey==='posSelect'">
              <JnpfPosSelect v-model="item.value" :placeholder="'请选择'+item.label" clearable
                class="item" :multiple="item.searchMultiple" :ableIds="item.ableIds"
                :selectType="item.selectType" />
            </template>
            <template v-if="item.__config__.jnpfKey==='groupSelect'">
              <JnpfGroupSelect v-model="item.value" :placeholder="'请选择'+item.label" clearable
                class="item" :multiple="item.searchMultiple" :selectType="item.selectType"
                :ableIds="item.ableIds" />
            </template>
            <template v-if="item.__config__.jnpfKey==='roleSelect'">
              <JnpfRoleSelect v-model="item.value" :placeholder="'请选择'+item.label" clearable
                class="item" :multiple="item.searchMultiple" :selectType="item.selectType"
                :ableIds="item.ableIds" />
            </template>
            <template v-if="item.__config__.jnpfKey==='userSelect'">
              <JnpfUserSelect v-model="item.value" :placeholder="'请选择'+item.label" clearable
                class="item"
                :selectType="(item.selectType!='all' && item.selectType!='custom')?'all':item.selectType"
                :ableIds="item.ableIds" :multiple="item.searchMultiple" :key="key" />
            </template>
            <template v-if="item.__config__.jnpfKey==='usersSelect'">
              <JnpfUsersSelect v-model="item.value" :placeholder="'请选择'+item.label" clearable
                class="item" :selectType="item.selectType" :ableIds="item.ableIds"
                :multiple="item.searchMultiple" :key="key" />
            </template>
            <template v-if="item.__config__.jnpfKey==='depSelect'">
              <JnpfDepSelect v-model="item.value" :placeholder="'请选择'+item.label" clearable
                class="item" :selectType="item.selectType" :ableIds="item.ableIds"
                :multiple="item.searchMultiple" />
            </template>
            <template v-if="item.__config__.jnpfKey==='autoComplete'">
              <JnpfAutoComplete v-model="item.value" :placeholder="'请输入'+item.label"
                :total="item.total" clearable class="item" :relationField="item.relationField"
                :templateJson="item.templateJson" :interfaceId="item.interfaceId" />
            </template>
          </template>
        </el-form-item>
      </el-col>
      <el-col :span="6">
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="search()">{{$t('common.search')}}
          </el-button>
          <el-button icon="el-icon-refresh-right" @click="reset()">{{$t('common.reset')}}
          </el-button>
          <template v-if="searchList.length>3">
            <el-button type="text" icon="el-icon-arrow-down" @click="showAll=true" v-if="!showAll">
              展开
            </el-button>
            <el-button type="text" icon="el-icon-arrow-up" @click="showAll=false" v-else>
              收起</el-button>
          </template>
        </el-form-item>
      </el-col>
    </el-form>
  </el-row>
</template>
<script>
import { deepClone } from '@/utils'
import { dyOptionsList, useInputList, useDateList, useSelectList } from '@/components/Generator/generator/comConfig'
import { getDictionaryDataSelector } from '@/api/systemData/dictionary'
import { getDataInterfaceRes } from '@/api/systemData/dataInterface'
export default {
  props: ['list', 'initDataJson'],
  data() {
    return {
      showAll: false,
      searchList: [],
      useInputList,
      useDateList,
      useSelectList,
      defSearchList: [],
      key: +new Date()
    }
  },
  watch: {
    list: {
      handler: function (val) {
        this.searchList = deepClone(val)
        this.defSearchList = deepClone(val)
        this.buildOptions(this.searchList)
      },
      deep: true,
      immediate: true
    },
    initDataJson: {
      handler: function (val) {
        if (val != '') {
          let initData = JSON.parse(val);
          if (Object.keys(initData).length > 0) {
            for (let key in initData) {
              for (let i = 0; i < this.searchList.length; i++) {
                let e = this.searchList[i]
                //if ((Array.isArray(e.value) && e.value.length) || e.value || e.value === 0 || e.value === false) e.defaultValue = e.value;
                if (e.id === key) {
                  if (e.searchMultiple) {
                    if (e.jnpfKey == "organizeSelect") {
                      e.value = Array.isArray(initData[key]) && Array.isArray(initData[key][0]) ? initData[key] : [initData[key]]
                    } else {
                      e.value = Array.isArray(initData[key]) ? initData[key] : [initData[key]]
                    }
                  } else {
                    e.value = initData[key]
                  }
                  break;
                }
              }
            }
          }
        }
      }
    }
  },
  methods: {
    buildOptions(componentList) {
      componentList.forEach(cur => {
        const config = cur.__config__
        if (dyOptionsList.indexOf(config.jnpfKey) > -1) {
          if (config.dataType === 'dictionary' && config.dictionaryType) {
            cur.options = []
            getDictionaryDataSelector(config.dictionaryType).then(res => {
              cur.options = res.data.list
            })
          }
          if (config.dataType === 'dynamic' && config.propsUrl) {
            cur.options = []
            let query = { paramList: config.templateJson || [] }
            getDataInterfaceRes(config.propsUrl, query).then(res => {
              cur.options = Array.isArray(res.data) ? res.data : []
            })
          }
        }
      })
    },
    search() {
      let obj = {}
      for (let i = 0; i < this.searchList.length; i++) {
        const e = this.searchList[i]
        if (e.value) {
          if (Array.isArray(e.value)) {
            if (e.value.length) obj[e.prop] = e.value
          } else {
            obj[e.prop] = e.value
          }
        }
      }
      let queryJson = JSON.stringify(obj) === "{}" ? '' : JSON.stringify(obj)
      this.$emit('search', queryJson)
    },
    reset() {
      this.treeReset()
      this.$emit('reset')
    },
    treeReset() {
      for (let i = 0; i < this.defSearchList.length; i++) {
        if (this.searchList[i].value || this.searchList[i].value == undefined) {
          this.$set(this.searchList[i], 'value', this.defSearchList[i].value)
          if (['timePicker', 'timeRange'].includes(this.searchList[i].__config__.jnpfKey)) {
            this.$set(this.searchList[i], 'value', null)
          } else if (this.useInputList.includes(this.searchList[i].__config__.jnpfKey)) {
            this.$set(this.searchList[i], 'value', '')
          }
        }
      }
      this.key = +new Date()
    }
  }
}
</script>
