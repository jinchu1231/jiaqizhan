
<template>
  <el-dialog title="掩码设置" :close-on-click-modal="false" :visible.sync="visible"
    class="JNPF-dialog JNPF-dialog_center mask-config-dialog" lock-scroll width="600px"
    append-to-body>
    <el-form :model="formData" :rules="rules" ref="formRef" class="mt-20" label-width="120px"
      @submit.native.prevent>
      <jnpf-form-tip-item label="填充符号" prop="filler">
        <jnpf-select v-model="formData.filler" placeholder="请选择填充符号" :options="fillerOptions"
          :props='props' :clearable='false' />
      </jnpf-form-tip-item>
      <jnpf-form-tip-item label="掩码规则" prop="maskType">
        <el-select v-model="formData.maskType" placeholder="请选择掩码规则" popper-class="select-down"
          @change="onMaskTypeChange($event)">
          <el-option v-for="item in maskTypeOptions" :key="item.id" :label="item.fullName"
            :value="item.id">
          </el-option>
        </el-select>
      </jnpf-form-tip-item>
      <template v-if="formData.maskType == 0">
        <jnpf-form-tip-item label="开头显示" name="prefixType">
          <jnpf-select v-model="formData.prefixType" placeholder="请选择开头显示"
            :options="prefixTypeOptions" :props='props' :clearable='false' />
        </jnpf-form-tip-item>
        <jnpf-form-tip-item label="字数" name="prefixLimit" v-if="formData.prefixType === 2">
          <el-input-number v-model="formData.prefixLimit" placeholder="请输入" :min="0" :precision="0"
            @change="onNumberChange($event, 'prefixLimit')" controls-position="right" />
        </jnpf-form-tip-item>
        <jnpf-form-tip-item label="字符" name="prefixSpecifyChar"
          v-if="formData.prefixType === 3 || formData.prefixType === 4">
          <el-input v-model="formData.prefixSpecifyChar" placeholder="请输入字符" />
        </jnpf-form-tip-item>
        <jnpf-form-tip-item label="结尾显示" name="suffixType">
          <jnpf-select v-model="formData.suffixType" placeholder="请选择结尾显示"
            :options="suffixTypeOptions" :props='props' :clearable='false' />
        </jnpf-form-tip-item>
        <jnpf-form-tip-item label="字数" name="suffixLimit" v-if="formData.suffixType === 2">
          <el-input-number v-model="formData.suffixLimit" placeholder="请输入" :min="0" :precision="0"
            @change="onNumberChange($event, 'suffixLimit')" controls-position="right" />
        </jnpf-form-tip-item>
        <jnpf-form-tip-item label="字符" name="suffixSpecifyChar"
          v-if="formData.suffixType === 3 || formData.suffixType === 4">
          <el-input v-model="formData.suffixSpecifyChar" placeholder="请输入字符" />
        </jnpf-form-tip-item>
        <jnpf-form-tip-item label="显示字符" name="ignoreChar">
          <el-input v-model="formData.ignoreChar" placeholder="多个字符之间用英文,号隔开，如：a,b,c" clearable />
        </jnpf-form-tip-item>
        <jnpf-form-tip-item name="useUnrealMask" label="虚拟掩码"
          tip-label="禁用：按照真实字数显示掩码；</br>启用：按照虚拟位数显示掩码，不暴露真实长度">
          <el-switch v-model="formData.useUnrealMask" />
        </jnpf-form-tip-item>
        <jnpf-form-tip-item label="掩码长度" name="unrealMaskLength" v-if="formData.useUnrealMask">
          <el-input-number v-model="formData.unrealMaskLength" placeholder="请输入" :min="1"
            controls-position="right" :precision="0" addonAfter="个字"
            @change="onNumberChange($event, 'unrealMaskLength')" />
        </jnpf-form-tip-item>
      </template>
      <el-divider></el-divider>
      <jnpf-form-tip-item label="效果测试" name="testText">
        <el-input v-model="testText" placeholder="请输入" clearable :disabled="isMask">
          <template slot="append">
            <p class="cursor-pointer" @click="handleMask()" v-show="!isMask">掩盖</p>
            <p class="cursor-pointer" @click="handleRevert()" v-show="isMask">解码</p>
          </template>
        </el-input>
      </jnpf-form-tip-item>
    </el-form>

    <span slot="footer" class="dialog-footer">
      <el-button @click="visible = false">{{$t('common.cancelButton')}}</el-button>
      <el-button type="primary" @click="handleSubmit">{{$t('common.confirmButton')}}</el-button>
    </span>
  </el-dialog>
</template>

<script>
import { useTextMask, defaultMaskOptions } from '@/components/Generator/utils/useTextMask';
const fillerOptions = [
  { id: '*', fullName: '*' },
  { id: '●', fullName: '●' },
  { id: '-', fullName: '-' },
  { id: '.', fullName: '.' },
  { id: '#', fullName: '#' },
]
const maskTypeOptions = [
  { id: 1, fullName: '全掩盖' },
  { id: 2, fullName: '姓名-显示前1个字，后1个字' },
  { id: 3, fullName: '手机号-显示前3位，后4位' },
  { id: 4, fullName: '邮箱-显示前3个字，@和之后的字' },
  { id: 5, fullName: '身份证-显示前6位，后3位，虚拟为4位' },
  { id: 6, fullName: 'IP地址-显示第1段IP' },
  { id: 7, fullName: '车牌号-显示前1个字，后2位' },
  { id: 8, fullName: '银行卡号-显示前6位，后4位' },
  { id: 0, fullName: '自定义规则' },
];
const prefixTypeOptions = [
  { id: 1, fullName: '不显示' },
  { id: 2, fullName: '指定字数' },
  { id: 3, fullName: '指定字符之前的字' },
  { id: 4, fullName: '指定字符和之前的字' },
];
const suffixTypeOptions = [
  { id: 1, fullName: '不显示' },
  { id: 2, fullName: '指定字数' },
  { id: 3, fullName: '指定字符之后的字' },
  { id: 4, fullName: '指定字符和之后的字' },
];
export default {
  props: ['type'],
  components: {},
  data() {
    return {
      visible: false,
      fillerOptions,
      maskTypeOptions,
      prefixTypeOptions,
      suffixTypeOptions,
      formData: defaultMaskOptions,
      rules: {
        fullName: [{ required: true, message: '请输入选项名' }],
        id: [{ required: true, message: '请输入选项值' }],
      },
      testText: '',
      originTestText: '',
      isMask: false,
      props: {
        label: "fullName",
        value: "id"
      }
    }
  },
  methods: {
    openModal(data) {
      this.visible = true;
      this.isMask = false;
      this.formData = JSON.parse(JSON.stringify(data));
      this.testText = ''
      this.$nextTick(() => {
        this.$refs.formRef.clearValidate();
      })
    },
    handleCancel() {
      this.visible = false;
    },
    async handleSubmit() {
      try {
        this.visible = false;
        this.$emit('confirm', this.formData);
      } catch (_) { }
    },
    handleMask() {
      if (!this.testText) return;
      this.originTestText = this.testText;
      this.isMask = true;
      const { getMaskedText } = useTextMask(this.formData);
      this.testText = getMaskedText(this.testText);
    },
    handleRevert() {
      this.testText = this.originTestText;
      this.originTestText = '';
      this.isMask = false;
    },
    onNumberChange(val, key) {
      if (val) return;
      if (key === 'unrealMaskLength') return this.$nextTick(() => { (this.formData[key] = 1) })
      this.$nextTick(() => { (this.formData[key] = 0) })
    },
    onMaskTypeChange(val) {
      this.testText = ''
      this.originTestText = ''
      this.isMask = false
      this.formData = {
        ...defaultMaskOptions,
        maskType: val
      }
    }
  }
}
</script>
<style lang="scss" >
.mask-config-dialog {
  >>> .el-dialog__body {
    padding: 20px 50px 0 !important;
  }
  .cursor-pointer {
    cursor: pointer;
  }
}
.select-down .el-select-dropdown__wrap {
  height: 320px !important;
  min-height: 320px !important;
}
</style>
