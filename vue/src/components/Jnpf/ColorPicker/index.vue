<template>
  <el-color-picker v-bind="$attrs" v-on="$listeners" v-model="innerValue" />
</template>

<script>
export default {
  name: 'JnpfColorPicker',
  props: {
    value: {
      type: String,
      default: ''
    },
  },
  data() {
    return {
      innerValue: ''
    };
  },
  watch: {
    value: {
      handler(val) {
        this.setValue(val);
      },
      immediate: true
    },
  },
  methods: {
    setValue(value) {
      this.innerValue = value;
    }
  }
};
</script>