<template>
  <el-date-picker :type="datetype" v-model="innerValue" placeholder="请选择" value-format="timestamp"
    :range-separator="separator" :picker-options='pickerOptions' style="width:100%"
    :start-placeholder="startPlaceholder" :end-placeholder="endPlaceholder"
    :format="format"></el-date-picker>
</template>
<script>
export default {
  name: 'JNPF-date-picker',
  components: {},
  props: {
    value: {
      default: undefined
    },
    format: {
      type: String,
      default: 'yyyy-MM-dd HH:mm:ss'
    },
    separator: {
      type: String,
      default: '-'
    },
    startTime: {
      default: undefined
    },
    endTime: {
      default: undefined
    },
    startPlaceholder: {
      type: String,
      default: '开始日期'
    },
    endPlaceholder: {
      type: String,
      default: '结束日期'
    }
  },
  data() {
    return {
      innerValue: this.value,
      pickerOptions: {
        disabledDate: (time) => {
          if (this.format === 'yyyy') return time.getMonth() + 1 != 1
        },
      }
    }
  },
  watch: {
    innerValue(val) {
      this.$emit('input', val)
      this.$emit('change', val)
    },
    value(val) {
      this.innerValue = val
    },
  },
  computed: {
    datetype() {
      let type = this.format === 'yyyy' ? 'month' : this.format === 'yyyy-MM' ? 'month' : this.format === 'yyyy-MM-dd' ? 'date' : 'datetime'
      return type + 'range'
    }
  }
}
</script>