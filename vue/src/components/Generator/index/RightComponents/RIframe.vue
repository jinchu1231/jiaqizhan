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
    <jnpf-form-tip-item label="链接地址" tip-label="地址以http://或https://开头">
      <el-input v-model="activeData.href" placeholder="请输入链接地址" />
    </jnpf-form-tip-item>
    <jnpf-form-tip-item label="高度">
      <el-input-number v-model="activeData.height" placeholder="请输入高度" :min="0"
        controls-position="right" />
    </jnpf-form-tip-item>
    <el-form-item label="边框">
      <el-radio-group v-model="activeData.borderType">
        <el-radio-button label="none">无</el-radio-button>
        <el-radio-button label="solid">实线</el-radio-button>
        <el-radio-button label="dashed">虚线</el-radio-button>
        <el-radio-button label="dotted">点状</el-radio-button>
      </el-radio-group>
    </el-form-item>
    <template v-if="activeData.borderType!='none'">
      <el-form-item label="边框颜色">
        <el-color-picker v-model="activeData.borderColor"></el-color-picker>
      </el-form-item>
      <el-form-item label="边框宽度">
        <el-input-number v-model="activeData.borderWidth" :min="1" :max="10" :precision="0"
          controls-position="right" @change="onBorderWidthChange" />
      </el-form-item>
    </template>
  </el-row>
</template>
<script>

export default {
  props: ['activeData'],
  data() {
    return {}
  },
  methods: {
    onBorderWidthChange(val) {
      if (!val) this.$nextTick(() => this.$set(this.activeData, 'borderWidth', 1))
    }
  }
}
</script>