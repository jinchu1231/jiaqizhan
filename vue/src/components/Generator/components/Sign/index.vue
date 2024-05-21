<template>
  <div>
    <div class="sign-main">
      <el-image :src="innerValue" alt="" v-if="innerValue" class="sign-img"
        :preview-src-list="srcList" />
      <div @click="addSign" class="sign-style" :class="{ 'disabled-btn': disabled }"
        v-if="!detailed">
        <i class="icon-ym icon-ym-signature add-sign"></i>
        <span class="sign-title" v-if="!innerValue">{{signTip}}</span>
      </div>
    </div>
    <SignImgDialog v-if="signVisible" ref="SignImg" :lineWidth='3' :isDefault='1' :type='1'
      @close="signDialog" />
  </div>
</template>
<script>
import SignImgDialog from '@/components/SignImgDialog'
export default {
  name: 'JnpfSign',
  components: { SignImgDialog },
  props: {
    value: { type: String, default: '' },
    signTip: { type: String, default: '手写签名' },
    disabled: { type: Boolean, default: false },
    detailed: { type: Boolean, default: false },
  },
  data() {
    return {
      signVisible: false,
      innerValue: '',
      srcList: []
    }
  },
  watch: {
    value: {
      handler(val) {
        this.innerValue = val || ''
        if (this.innerValue) this.srcList = [this.innerValue]
      },
      deep: true,
      immediate: true
    },
  },
  methods: {
    addSign() {
      if (this.disabled) return
      this.signVisible = true
      this.$nextTick(() => {
        this.$refs.SignImg.init()
      })
    },
    signDialog(val) {
      this.signVisible = false
      if (val) {
        this.srcList = [val]
        this.innerValue = val
        this.$emit('input', this.innerValue)
      }
    },
  }
}
</script>
<style lang="scss" scoped>
.sign-img {
  width: 80px;
  height: 40px;
  object-fit: contain;
  cursor: pointer;
}
.add-sign {
  font-size: 28px !important;
}
.sign-style {
  height: 40px;
  font-size: 14px;
  display: flex;
  align-items: center;
  cursor: pointer;
  &.disabled-btn {
    cursor: no-drop !important;
  }
  .sign-icon {
    font-size: 28px;
  }
  .sign-title {
    height: 30px;
    font-size: 14px;
    color: #2188ff;
    margin-top: 4px;
  }
}
</style>
