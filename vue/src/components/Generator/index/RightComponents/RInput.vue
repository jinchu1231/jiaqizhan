<template>
  <el-row>
    <el-form-item label="控件标题">
      <el-input v-model="activeData.__config__.label" placeholder="请输入控件标题" />
    </el-form-item>
    <el-form-item label="显示标题">
      <el-switch v-model="activeData.__config__.showLabel" />
    </el-form-item>
    <el-form-item label="标题提示">
      <el-input type="textarea" :rows="2" v-model="activeData.__config__.tipLabel"
        placeholder="请输入标题提示" />
    </el-form-item>
    <el-form-item label="占位提示">
      <el-input v-model="activeData.placeholder" placeholder="请输入占位提示" />
    </el-form-item>
    <el-form-item label="默认值">
      <el-input v-model="activeData.__config__.defaultValue" placeholder="请输入默认值" />
    </el-form-item>
    <el-form-item label="前缀">
      <el-input v-model="activeData.addonBefore" placeholder="请输入前缀" />
    </el-form-item>
    <el-form-item label="后缀">
      <el-input v-model="activeData.addonAfter" placeholder="请输入后缀" />
    </el-form-item>
    <el-form-item label="前图标" v-show="showType==='pc'">
      <el-input v-model="activeData.prefixIcon" placeholder="请输入前图标名称">
        <el-button slot="append" @click="openIconsDialog('prefixIcon')">
          选择
        </el-button>
      </el-input>
    </el-form-item>
    <el-form-item label="后图标" v-show="showType==='pc'">
      <el-input v-model="activeData.suffixIcon" placeholder="请输入后图标名称">
        <el-button slot="append" @click="openIconsDialog('suffixIcon')">
          选择
        </el-button>
      </el-input>
    </el-form-item>
    <el-form-item label="最多输入">
      <el-input v-model="activeData.maxlength" placeholder="请输入字符长度" type="number">
        <template slot="append">个字符</template>
      </el-input>
    </el-form-item>
    <el-form-item label="掩码显示">
      <el-switch v-model="activeData.useMask" />
    </el-form-item>
    <el-form-item label="掩码规则" v-if="activeData.useMask">
      <el-button block @click="editMaskConfig" style="width: 100%;">掩码设置</el-button>
    </el-form-item>
    <jnpf-form-tip-item label="扫码输入" tip-label="支持常规二维码和条形码；桌面端和H5不支持扫码">
      <el-switch v-model="activeData.useScan" @change="onUseScanChange" />
    </jnpf-form-tip-item>
    <!-- <el-form-item label="显示标签">
      <el-switch v-model="activeData.__config__.showLabel" />
    </el-form-item> -->
    <!-- <el-form-item label="输入统计">
      <el-switch v-model="activeData.showWordLimit" />
    </el-form-item> -->
    <el-form-item label="能否清空">
      <el-switch v-model="activeData.clearable" />
    </el-form-item>
    <el-form-item label="是否密码">
      <el-switch v-model="activeData.showPassword" :disabled='activeData.useScan' />
    </el-form-item>
    <el-form-item label="是否只读" v-show="showType==='pc'">
      <el-switch v-model="activeData.readonly" />
    </el-form-item>
    <el-form-item label="是否禁用">
      <el-switch v-model="activeData.disabled" />
    </el-form-item>
    <el-form-item label="是否隐藏">
      <el-switch v-model="activeData.__config__.noShow" />
    </el-form-item>
    <el-divider>校验规则</el-divider>
    <el-form-item label="是否必填">
      <el-switch v-model="activeData.__config__.required" />
    </el-form-item>
    <el-form-item>
      <span slot="label">是否唯一
        <el-tooltip content="输入值唯一性校验" placement="top">
          <a class="el-icon-question tooltip-question"></a>
        </el-tooltip>
      </span>
      <el-switch v-model="activeData.__config__.unique" />
    </el-form-item>
    <div v-for="(item, index) in activeData.__config__.regList" :key="index" class="reg-item">
      <span class="close-btn" @click="activeData.__config__.regList.splice(index, 1)">
        <i class="el-icon-close" />
      </span>
      <el-form-item label="表达式">
        <el-input v-model="item.pattern" placeholder="请输入正则" @change="onPatternChange" />
      </el-form-item>
      <el-form-item label="错误提示" style="margin-bottom:0">
        <el-input v-model="item.message" placeholder="请输入错误提示" />
      </el-form-item>
    </div>
    <div class="mt-10">
      <el-dropdown>
        <el-button type="primary">添加常用校验<i class="el-icon-arrow-down el-icon--right"></i>
        </el-button>
        <el-dropdown-menu slot="dropdown">
          <el-dropdown-item @click.native="addHandle(item)" v-for="(item,i) in ruleList" :key="i">
            {{item.label}}</el-dropdown-item>
        </el-dropdown-menu>
      </el-dropdown>
      <el-button type="primary" @click="addReg" style="margin-left:10px">
        自定义规则
      </el-button>
    </div>
    <iconBox :visible.sync="iconsVisible" :current="activeData[currentIconModel]"
      @choiceIcon="setIcon" />
    <MaskConfigDialog ref="maskConfigRef" @confirm="updateMaskConfig" />
  </el-row>
</template>
<script>
import iconBox from '@/components/JNPF-iconBox'
import comMixin from './mixin';
import MaskConfigDialog from '@/components/Generator/components/MaskConfigDialog';
import { defaultMaskOptions } from '@/components/Generator/utils/useTextMask';
export default {
  props: ['activeData'],
  mixins: [comMixin],
  components: { iconBox, MaskConfigDialog },
  data() {
    return {
      iconsVisible: false,
      currentIconModel: null,
      ruleList: [
        {
          pattern: '/^\\d+$/',
          message: '请输入正确的数字',
          label: '数字'
        },
        {
          pattern: '/^([1-9][\\d]*|0)(\\.[\\d]+)?$/',
          message: '请输入正确的金额',
          label: '金额'
        },
        {
          pattern: '/^0\\d{2,3}-?\\d{7,8}$/',
          message: '请输入正确的电话号码',
          label: '电话'
        },
        {
          pattern: '/^1[3456789]\\d{9}$/',
          message: '请输入正确的手机号码',
          label: '手机'
        },
        {
          pattern: '/^1[3456789]\\d{9}$|^0\\d{2,3}-?\\d{7,8}$/',
          message: '请输入正确的联系方式',
          label: '电话/手机'
        },
        {
          pattern: '/^[a-z0-9]+([._\\-]*[a-z0-9])*@([a-z0-9]+[-a-z0-9]*[a-z0-9]+.){1,63}[a-z0-9]+$/',
          message: '请输入正确的邮箱',
          label: '邮箱'
        },
        {
          pattern: '/^[1-9]\\d{5}(18|19|([23]\\d))\\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$/',
          message: '请输入正确的身份证号码',
          label: '身份证'
        },
      ]
    }
  },
  created() { },
  methods: {
    openIconsDialog(model) {
      this.iconsVisible = true
      this.currentIconModel = model
    },
    setIcon(val) {
      this.activeData[this.currentIconModel] = val
    },
    addHandle(row) {
      this.activeData.__config__.regList.push({
        pattern: row.pattern,
        message: row.message
      })
    },
    onPatternChange(val) {
      try {
        const isRegExp = Object.prototype.toString.call(eval(val)) === '[object RegExp]'
        if (!isRegExp) this.$message.error('请输入正确的正则表达式')
      } catch {
        this.$message.error('请输入正确的正则表达式')
      }
    },
    editMaskConfig() {
      if (!this.activeData.maskConfig) this.activeData.maskConfig = JSON.parse(JSON.stringify(defaultMaskOptions))
      this.$nextTick(() => {
        this.$refs.maskConfigRef.openModal(this.activeData.maskConfig)
      })
    },
    updateMaskConfig(data) {
      this.activeData.maskConfig = data;
    },
    onUseScanChange(val) {
      if (val) this.activeData.showPassword = false;
    }
  }
}
</script>