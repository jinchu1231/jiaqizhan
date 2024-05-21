<template>
  <el-switch v-bind="$attrs" v-on="$listeners" v-model="innerValue" />
</template>

<script>
export default {
  name: 'JnpfSwitch',
  props: {
    value: {
      type: [Number, Boolean],
      default: 0
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
      this.$emit('input', this.innerValue ? this.innerValue : 0)
    },
    onChange(value) {
      this.$emit('input', value);
    }
  }
};
</script>