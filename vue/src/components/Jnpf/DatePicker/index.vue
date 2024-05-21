<template>
  <el-date-picker :type="datetype" v-model="innerValue" :placeholder="placeholder"
    value-format="timestamp" :picker-options='pickerOptions' :format="format" @change="change"
    :disabled="disabled" @blur="onblur($event)" :clearable="clearable" :readonly="readOnly"
    :key="key"></el-date-picker>
</template>
<script>
import dayjs from 'dayjs'
export default {
  name: 'JnpfDatePicker',
  components: {},
  props: {
    activeData: {
      default: undefined
    },
    value: {
      default: undefined
    },
    defaultCurrent: {
      type: Boolean,
      default: false
    },
    format: {
      type: String,
      default: 'yyyy-MM-dd'
    },
    startTime: {
      default: undefined
    },
    endTime: {
      default: undefined
    },
    disabled: {
      type: Boolean,
      default: false
    },
    clearable: {
      type: Boolean,
      default: false
    },
    readonly: {
      type: Boolean,
      default: false
    },
    placeholder: {
      type: String,
      default: '请选择'
    }
  },
  data() {
    return {
      innerValue: this.value,
      oldInnerValue: this.value,
      key: +new Date(),
    }
  },
  watch: {
    value: {
      handler(val) {
        this.innerValue = val
      },
      immediate: true
    },
    format() {
      this.key = +new Date()
    },
    innerValue: {
      handler(newVal, oldVal) {
        this.oldInnerValue = oldVal
      },
    }
  },
  computed: {
    datetype() {
      return this.format === 'yyyy' ? 'year' : this.format === 'yyyy-MM' ? 'month' : this.format === 'yyyy-MM-dd' ? 'date' : 'datetime'
    },
    readOnly() {
      if (this.readonly) return true
      if (this.startTime && this.endTime && (this.startTime > this.endTime)) return true
      return false
    },
    realStartTime() {
      const format = this.format === 'yyyy' ? 'yyyy-01-01 00:00:00' : this.format === 'yyyy-MM' ? 'yyyy-MM-01 00:00:00' : 'yyyy-MM-dd 00:00:00'
      if (!this.startTime) return null
      const startTime = this.jnpf.toDate(this.startTime, format)
      return new Date(startTime).getTime()
    },
    realEndTime() {
      if (!this.endTime) return null
      const endTime = this.jnpf.toDate(this.endTime, 'yyyy-MM-dd 23:59:59')
      return new Date(endTime).getTime()
    },
    pickerOptions() {
      const startDate = this.startTime ? this.jnpf.toDate(this.startTime, 'yyyy-MM-dd') : ''
      const startTime = this.startTime ? this.jnpf.toDate(this.startTime, 'HH:mm:ss') : '00:00:00'
      const endDate = this.endTime ? this.jnpf.toDate(this.endTime, 'yyyy-MM-dd') : ''
      const endTime = this.endTime ? this.jnpf.toDate(this.endTime, 'HH:mm:ss') : '23:59:59'
      const currentDate = this.jnpf.toDate(this.innerValue, 'yyyy-MM-dd')
      let start = currentDate == startDate ? startTime : '00:00:00'
      let end = currentDate == endDate ? endTime : '23:59:59'
      this.startTimeRange = start + '-' + end;
      if (this.format === 'yyyy-MM-dd HH:mm' || this.format === 'yyyy-MM-dd HH:mm:ss') {
        if (currentDate == startDate && this.jnpf.toDate(this.innerValue, 'HH:mm:ss') == '00:00:00') {
          const list = startTime.split(':')
          this.innerValue = dayjs(this.innerValue).add(list[0] || 0, 'hour').add(list[1] || 0, 'minute').add(list[2] || 0, 'second')
        } else if (currentDate != this.jnpf.toDate(this.oldInnerValue, 'yyyy-MM-dd')) {
          this.innerValue = new Date(this.jnpf.toDate(this.innerValue, 'yyyy-MM-dd 00:00:00')).getTime()
        }
      }
      return {
        selectableRange: this.startTimeRange,
        disabledDate: (time) => {
          const timeVal = time.getTime()
          if (!this.realStartTime && !this.realEndTime) return false
          if (this.realStartTime && this.realEndTime) return timeVal < this.realStartTime || timeVal > this.realEndTime
          if (this.realEndTime) return timeVal > this.realEndTime
          return timeVal < this.realStartTime
        }
      }
    }
  },
  methods: {
    change(val) {
      this.$emit('input', this.innerValue)
      this.$emit('change', this.innerValue)
    },
    onblur(event) {
      this.$emit('blur', event)
    }
  }
}
</script>
<style lang="scss">
.el-picker-panel__link-btn.el-button--text {
  display: none;
}
</style>