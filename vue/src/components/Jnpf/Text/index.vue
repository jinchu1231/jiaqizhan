<template>
  <p class="JnpfText" :style="_style">{{content}}</p>
</template>
<script>
export default {
  name: 'JnpfText',
  props: {
    textStyle: {
      type: Object,
      default: () => ({
        // 'font-size': ' 12px',
        // "color": '#00000',
        // 'text-align': 'center',
        // 'line-height': '32px',
        // 'font-weight': 'normal',
        // 'font-style': 'normal',
        // 'text-decoration': 'none',
      })
    },
    content: { type: String, default: '' },
  },
  computed: {
    _style() {
      return { ...this.textStyle, 'line-height': this.textStyle['line-height'] + 'px', 'font-size': this.textStyle['font-size'] + 'px' }
    }
  }
}
</script>
<style lang="scss">
.JnpfText {
  padding: 3px 0;
  margin: 0;
}
</style>