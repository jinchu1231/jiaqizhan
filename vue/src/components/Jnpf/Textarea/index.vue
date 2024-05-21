<template>
  <el-input v-bind="$attrs" v-on="$listeners" type="textarea" v-model="innerValue"
    :autosize="autoSize" @change="onChange">
    <template #[item]="data" v-for="item in Object.keys($slots)">
      <slot :name="item" v-bind="data || {}"></slot>
    </template>
  </el-input>
</template>

<script>
export default {
  name: 'JnpfTextarea',
  props: {
    value: {
      type: [String, Number],
      default: ''
    },
    autoSize: {
      type: Object,
      default: () => { }
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
    },
    onChange(value) {
      this.$emit('input', value);
    }
  }
};
</script>