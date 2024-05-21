<template>
  <el-input v-bind="$attrs" v-on="$listeners" v-model="innerValue" @change="onChange"
    v-if="!detailed">
    <template #[item]="data" v-for="item in Object.keys($slots)">
      <slot :name="item" v-bind="data || {}"></slot>
    </template>
    <template #prepend v-if="addonBefore">
      {{addonBefore}}
    </template>
    <template #append v-if="addonAfter">
      {{addonAfter}}
    </template>
  </el-input>
  <p class="detail-text" :class="{ ellipsis: showOverflow }"
    :title="showOverflow ? maskedValue : ''" v-else>
    <span v-if="$attrs.addonBefore">{{ $attrs.addonBefore }}</span>
    {{ maskedValue }}
    <span v-if="$attrs.addonAfter">{{ $attrs.addonAfter }}</span>
  </p>
</template>

<script>
import { useTextMask } from '@/components/Generator/utils/useTextMask';
export default {
  name: 'JnpfInput',
  props: {
    value: {
      type: [String, Number],
      default: ''
    },
    addonBefore: {
      type: String,
      default: ''
    },
    addonAfter: {
      type: String,
      default: ''
    },
    detailed: {
      type: Boolean,
      default: false
    },
    useMask: {
      type: Boolean,
      default: false
    },
    maskConfig: {
      type: Object,
      default: () => { }
    },
    showOverflow: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      innerValue: '',
      maskedValue: ''
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
      if (!this.useMask) return (this.maskedValue = value);
      const { getMaskedText } = useTextMask(this.maskConfig);
      this.maskedValue = getMaskedText(value);
    },
    onChange(value) {
      this.$emit('input', value);
    }
  }
};
</script>
<style lang="scss" scoped>
.el-table {
  .detail-text {
    word-break: break-all;
    white-space: wrap;
    line-height: 22px !important;
    &.ellipsis {
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }
}
</style>