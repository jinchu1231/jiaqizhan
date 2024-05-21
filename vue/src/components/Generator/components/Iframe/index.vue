<template>
  <iframe :style="getStyle" :src="href" scrolling="yes" frameborder="0" />
</template>
<script>
export default {
  name: 'JnpfIframe',
  props: {
    href: {
      type: String,
      default: ''
    },
    height: {
      type: Number,
      default: 300
    },
    borderType: {
      type: String,
      default: 'solid'
    },
    borderColor: {
      type: String,
      default: '#E2E0E0'
    },
    borderWidth: {
      type: Number,
      default: 1
    },
  },
  computed: {
    getStyle() {
      let style = { width: '100%', height: (this.height || 300) + 'px', border: (this.borderWidth || 1) + 'px' + ' ' + this.borderType + ' ' + this.borderColor }
      return style
    }
  }
}
</script>

