<template>
  <el-rate v-bind="$attrs" v-on="$listeners" v-model="innerValue" :max="count" />
</template>

<script>
export default {
  name: 'JnpfRate',
  props: {
    value: {
      type: Number | String,
      default: null
    },
    count: {
      type: Number,
      default: 5
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