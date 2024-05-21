<template>
  <el-divider v-bind="$attrs" v-on="$listeners">{{content}}</el-divider>
</template>

<script>
export default {
  name: 'JnpfDivider',
  props: {
    content: {
      type: String,
      default: ''
    },
  },
};
</script>