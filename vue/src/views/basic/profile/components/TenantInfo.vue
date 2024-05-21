<template>
  <div class="main">
    <el-form ref="dataForm" :model="dataForm" label-width="100px">
      <div class="JNPF-common-title mb-20">
        <h2 class="bold">租户信息</h2>
      </div>
      <el-row :gutter="20" class="custom-row">
        <el-col :sm="12" :xs="24">
          <el-form-item label="租户名称" prop="tenantName">
            <p>{{tenantData.tenantName}}</p>
          </el-form-item>
        </el-col>
        <el-col :sm="12" :xs="24">
          <el-form-item label="租户号" prop="tenantId">
            <p>{{tenantData.tenantId}}</p>
          </el-form-item>
        </el-col>
        <el-col :sm="12" :xs="24">
          <el-form-item label="有效期" prop="validTime">
            <p>{{tenantData.validTime}}</p>
          </el-form-item>
        </el-col>
        <el-col :sm="12" :xs="24">
          <el-form-item label="域名" prop="domain">
            <p>{{tenantData.domain}}</p>
          </el-form-item>
        </el-col>
      </el-row>
      <div class="JNPF-common-title mb-20">
        <h2 class="bold">单位信息</h2>
      </div>
      <el-row :gutter="20" class="custom-row">
        <el-col :sm="12" :xs="24">
          <el-form-item label="单位简称" prop="unitShortName">
            <p>{{tenantData.unitInfoJson.unitShortName}}</p>
          </el-form-item>
        </el-col>
        <el-col :sm="12" :xs="24">
          <el-form-item label="信用代码">
            <p>{{tenantData.unitInfoJson.unitCreditCode}}</p>
          </el-form-item>
        </el-col>
        <el-col :sm="12" :xs="24">
          <el-form-item label="单位性质">
            <p>{{unitNature}}</p>
          </el-form-item>
        </el-col>
        <el-col :sm="24" :xs="24">
          <el-form-item label="详细地址" prop="account">
            <p>{{tenantData.unitInfoJson.unitAddress}}</p>
          </el-form-item>
        </el-col>
        <el-col :sm="12" :xs="24">
          <el-form-item label="单位简介" prop="unitDescription">
            <p>{{tenantData.unitInfoJson.unitDescription}}</p>
          </el-form-item>
        </el-col>
      </el-row>
      <div class="JNPF-common-title mb-20">
        <h2 class="bold">联系人信息</h2>
      </div>
      <el-row :gutter="20" class="custom-row">
        <el-col :sm="12" :xs="24">
          <el-form-item label="联系人" prop="contacts">
            <p>{{tenantData.userInfoJson.contacts}}</p>
          </el-form-item>
        </el-col>
        <el-col :sm="12" :xs="24">
          <el-form-item label="联系邮箱" prop="contactEmail">
            <p>{{tenantData.userInfoJson.contactEmail}}</p>
          </el-form-item>
        </el-col>
        <el-col :sm="12" :xs="24">
          <el-form-item label="联系电话" prop="contactPhone">
            <p>{{tenantData.userInfoJson.contactPhone}}</p>
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
  </div>
</template>
<script>
export default {
  props: {
    tenantData: {
      type: Object,
      default: () => {
        return {}
      }
    }
  },
  data() {
    return {
      formLoading: false,
      dataForm: {},
      unitNatureList: [
        { fullName: '个体户', id: 0 },
        { fullName: '合伙企业', id: 1 },
        { fullName: '集体企业', id: 2 },
        { fullName: '私营企业', id: 3 },
        { fullName: '国有企业', id: 4 },
      ],
    }
  },
  computed: {
    unitNature() {
      let val = ''
      this.unitNatureList.forEach(o => {
        if (o.id === this.tenantData.unitInfoJson.unitNature) val = o.fullName
      })
      return val
    }
  },
  created() {
  },
  methods: {
  }
}
</script>
