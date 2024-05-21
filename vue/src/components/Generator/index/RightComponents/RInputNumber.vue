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
    <el-form-item label="占位提示">
      <el-input v-model="activeData.placeholder" placeholder="请输入占位提示" />
    </el-form-item>
    <el-form-item label="默认值">
      <el-input-number v-model="activeData.__config__.defaultValue" placeholder="默认值"
        :min="activeData.min" :max="activeData.max" :step="activeData.step"
        :precision="activeData.precision" controls-position="right" />
    </el-form-item>
    <el-form-item label="最小值">
      <el-input-number v-model="activeData.min" placeholder="最小值" controls-position="right" />
    </el-form-item>
    <el-form-item label="最大值">
      <el-input-number v-model="activeData.max" placeholder="最大值" controls-position="right" />
    </el-form-item>
    <el-form-item label="精度">
      <el-input-number v-model="activeData.precision" :min="0" placeholder="精度" :max="15"
        controls-position="right" :precision="0" />
    </el-form-item>
    <el-form-item label="按钮">
      <el-radio-group v-model="controlsPosition" @change="controlsPositionChange">
        <el-radio-button label="">无</el-radio-button>
        <el-radio-button label="bothSides">左右</el-radio-button>
        <el-radio-button v-show="showType==='pc'" label="right">右侧</el-radio-button>
      </el-radio-group>
    </el-form-item>
    <template v-if="!activeData.controls">
      <el-form-item label="前缀">
        <el-input v-model="activeData.addonBefore" placeholder="请输入前缀" />
      </el-form-item>
      <el-form-item label="后缀">
        <el-input v-model="activeData.addonAfter" placeholder="请输入后缀" />
      </el-form-item>
      <el-form-item label="千位分隔">
        <el-switch v-model="activeData.thousands" />
      </el-form-item>
    </template>
    <template v-else>
      <el-form-item label="步长">
        <el-input-number v-model="activeData.step" placeholder="步数"
          :precision="activeData.precision" controls-position="right" />
      </el-form-item>
    </template>
    <el-form-item label="大写金额">
      <el-switch v-model="activeData.isAmountChinese" />
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
export default {
  props: ['activeData'],
  mixins: [comMixin],
  data() {
    return {}
  },
  computed: {
    controlsPosition: {
      get() {
        if (!this.activeData.controls) return ''
        return this.activeData.controlsPosition === '' ? 'bothSides' : 'right'
      },
      set(val) {
        this.activeData.controls = !!val;
        this.activeData.controlsPosition = val === 'right' ? 'right' : ''
      }
    },
  },
  methods: {
    controlsPositionChange() {
      this.activeData.thousands = false
      this.activeData.addonBefore = ''
      this.activeData.addonAfter = ''
    }
  }
}
</script>