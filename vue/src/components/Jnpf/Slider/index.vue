<template>
  <el-slider v-bind="$attrs" v-on="$listeners" v-model="innerValue" />
</template>

<script>
export default {
  name: 'JnpfSlide',
  props: {
    value: {
      type: Number | String,
      default: null
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
      this.innerValue = Number(value);
    }
  }
};
</script>