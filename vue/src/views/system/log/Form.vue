<template>
  <div class="sysLog-main">
    <el-drawer title="详情" :visible.sync="visible" :direction="direction" :size="500"
      class="contacts-drawer JNPF-common-drawer">
      <!-- <el-divider></el-divider> -->
      <el-form ref="form" :model="dataForm" label-width="80px" class="drawer-form">
        <el-form-item :label="activeKey == 5 ? '请求时间' : '操作时间'">
          <p>{{creatorTime }}</p>
        </el-form-item>
        <el-form-item label="用户">
          <p>{{ dataForm.userName }}</p>
        </el-form-item>
        <el-form-item label="IP地址">
          <p>{{ dataForm.ipAddress }}</p>
        </el-form-item>
        <el-form-item label="地点">
          <p>{{ dataForm.ipAddressName }}</p>
        </el-form-item>
        <el-form-item label="浏览器">
          <p>{{ dataForm.browser }}</p>
        </el-form-item>
        <el-form-item label="操作系统">
          <p>{{ dataForm.platForm }}</p>
        </el-form-item>
        <el-form-item label="操作模块" v-if="activeKey == 3">
          <p>{{ dataForm.moduleName }}</p>
        </el-form-item>
        <el-form-item label="请求方式">
          <p>{{ dataForm.requestMethod }}</p>
        </el-form-item>
        <el-form-item label="耗时(毫秒)" v-if="activeKey != 4">
          <p>{{ dataForm.requestDuration }}</p>
        </el-form-item>
        <el-collapse v-model="activeName" ghost expandIconPosition="right">
          <el-collapse-item name="2" title="请求地址">
            <div class="jnpf-code-box" v-if="dataForm.requestUrl">{{ dataForm.requestUrl }}</div>
          </el-collapse-item>
          <template v-if="activeKey != 4">
            <el-collapse-item name="3" title="请求方法">
              <div class="jnpf-code-box" v-if="dataForm.requestTarget">{{ dataForm.requestTarget }}
              </div>
            </el-collapse-item>
            <el-collapse-item name="4" title="请求参数">
              <div class="jnpf-code-box" v-if="dataForm.requestParam">{{ dataForm.requestParam }}
              </div>
            </el-collapse-item>
            <el-collapse-item name="5" title="返回结果">
              <div class="jnpf-code-box" v-if="dataForm.jsons">{{ dataForm.jsons }}</div>
            </el-collapse-item>
          </template>
          <el-collapse-item name="1" title="异常描述" v-if="activeKey == 4">
            <div class="jnpf-code-box" v-if="dataForm.jsons">{{ dataForm.jsons }}</div>
          </el-collapse-item>
        </el-collapse>
      </el-form>
    </el-drawer>
  </div>
</template>
<script>
import { getInfo } from '@/api/system/log'
export default {
  data() {
    return {
      visible: false,
      data: '',
      title: '异常描述',
      direction: 'rtl',
      dataForm: {
        creatorTime: ''
      },
      activeKey: '',
      activeName: '',
      id: ''
    }
  },
  computed: {
    creatorTime() {
      return this.jnpf.toDate(this.dataForm.creatorTime, "yyyy-MM-dd HH:mm:ss")
    }
  },
  methods: {
    init(data, type) {
      this.data = data || ''
      this.id = this.data.id || ''
      this.activeKey = type || ''
      this.visible = true
      this.getInfo()
    },
    getInfo() {
      getInfo(this.id).then((res) => {
        this.dataForm = res.data || {}
      })
    },
    goBack() {
      this.$emit('close')
    },
  }
}
</script>