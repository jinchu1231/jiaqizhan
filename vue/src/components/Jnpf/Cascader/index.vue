<template>
  <el-cascader v-bind="$attrs" v-on="$listeners" v-model="innerValue" :props="{multiple,...props}"
    @change="onChange" @blur="onBlur">
    <template #[item]="data" v-for="item in Object.keys($slots)">
      <slot :name="item" v-bind="data || {}"></slot>
    </template>
  </el-cascader>
</template>

<script>
export default {
  name: 'JnpfCascader',
  props: {
    value: {
      type: Array,
      default: () => []
    },
    props: {
      type: Object,
      default: () => ({
        value: "id",
        label: "fullName",
        children: "children"
      })
    },
    multiple: {
      type: Boolean,
      default: false
    }
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
  created() {
    this.$attrs.options = this.clearAllChildren(this.$attrs.options)
  },
  methods: {
    clearAllChildren(items) {
      for (let i = 0; i < items.length; i++) {
        const item = items[i]
        if (item && item.children) {
          if (item.children.length === 0) {
            delete item.children
          } else {
            this.clearAllChildren(item.children)
          }
        }
      }
      return items
    },
    setValue(value) {
      this.innerValue = value;
    },
    onChange(value) {
      this.$emit('input', value);
    },
    onBlur(value) {
      this.$emit('blur', value);
    }
  }
};
</script>