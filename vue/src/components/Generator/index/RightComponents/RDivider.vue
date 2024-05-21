<template>
  <el-row>
    <el-form-item label="展示文本">
      <el-input v-model="activeData.content" placeholder="显示在分割线上的文字" />
    </el-form-item>
    <el-form-item label="文本位置">
      <el-radio-group v-model="activeData.contentPosition">
        <el-radio-button label="left">左边</el-radio-button>
        <el-radio-button label="center">中间</el-radio-button>
        <el-radio-button label="right">右边</el-radio-button>
      </el-radio-group>
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
  created() { },
}
</script>