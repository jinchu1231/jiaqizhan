<template>
  <div class="app-container JNPF-flex-main systemConfig">
    <el-form ref="baseForm" :model="baseForm" :rules="rules" label-width="130px">
      <el-tabs v-model="activeName" type="border-card" class="JNPF-el_tabs">
        <el-tab-pane label="基本设置" name="first">
          <el-row :gutter="20">
            <el-col :span="24">
              <el-form-item label="系统图标">
                <div class="img-list">
                  <single-img v-model="baseForm.loginIcon" tip="登录图标" />
                  <single-img v-model="baseForm.logoIcon" tip="LOGO图标" />
                  <single-img v-model="baseForm.appIcon" tip="APP图标" />
                </div>
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="系统标题" prop="title">
                <el-input v-model="baseForm.title" clearable placeholder="系统标题" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="系统名称" prop="sysName">
                <el-input v-model="baseForm.sysName" clearable placeholder="系统名称" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="系统版本" prop="sysVersion">
                <el-input v-model="baseForm.sysVersion" maxlength="8" readonly placeholder="系统版本" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="公司名称" prop="companyName">
                <el-input v-model="baseForm.companyName" clearable placeholder="公司名称" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="版权信息" prop="copyright">
                <el-input v-model="baseForm.copyright" clearable placeholder="版权信息" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="公司简称" prop="companyCode">
                <el-input v-model="baseForm.companyCode" clearable placeholder="公司简称" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="公司地址" prop="companyAddress">
                <el-input v-model="baseForm.companyAddress" clearable placeholder="公司地址" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="公司法人" prop="companyContacts">
                <el-input v-model="baseForm.companyContacts" clearable placeholder="公司法人" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="公司电话" prop="companyTelePhone">
                <el-input v-model="baseForm.companyTelePhone" clearable placeholder="公司电话" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="公司邮箱" prop="companyEmail">
                <el-input v-model="baseForm.companyEmail" clearable placeholder="公司邮箱" />
              </el-form-item>
            </el-col>
            <el-col>
              <el-form-item label="系统描述" prop="sysDescription">
                <el-input v-model="baseForm.sysDescription" type="textarea"
                  :autosize="{ minRows: 5, maxRows: 10}" placeholder="系统描述" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" size="small" :loading="btnLoading" class="saveBtn"
                  @click="submitForm()">保 存</el-button>
              </el-form-item>
            </el-col>
          </el-row>
        </el-tab-pane>
        <el-tab-pane label="安全设置" name="second">
          <el-tabs tab-position="left" style="height:100%" v-model="secondTab" class="secondTab">
            <el-tab-pane label="登录策略">
              <el-row>
                <el-col :span="18">
                  <el-form-item label="登录方式">
                    <el-select v-model="baseForm.singleLogin" placeholder="请选择">
                      <el-option label="单一登录" :value="1" />
                      <el-option label="同时登录" :value="2" />
                    </el-select>
                  </el-form-item>
                  <el-form-item label="超时登出">
                    <el-input-number v-model="baseForm.tokenTimeout" :min="1" :precision="0"
                      :step="1" controls-position="right" /> 分钟
                  </el-form-item>
                  <el-form-item label="密码错误次数">
                    <el-input-number v-model="baseForm.passwordErrorsNumber" :min="0" :precision="0"
                      :step="1" controls-position="right" /> 次
                    <div class="tip">输入密码错误将用户锁定，设置3以下值表示不启动该功能</div>
                    <el-radio-group v-model="baseForm.lockType">
                      <el-radio :label="1">账号锁定</el-radio>
                      <el-radio :label="2">延时登录</el-radio>
                    </el-radio-group>
                    <div v-if="baseForm.lockType===2">
                      <span class="lockTime">延迟时间</span>
                      <el-input-number v-model="baseForm.lockTime" :min="1" :precision="0" :step="1"
                        controls-position="right" /> 分钟
                    </div>
                  </el-form-item>
                  <el-form-item label="登录验证码">
                    <el-switch v-model="baseForm.enableVerificationCode" :active-value="1"
                      :inactive-value="0" />
                  </el-form-item>
                  <el-form-item label="验证码位数" v-if="baseForm.enableVerificationCode">
                    <el-input-number v-model="baseForm.verificationCodeNumber" :min="3" :max="6"
                      :precision="0" :step="1" controls-position="right" /> 位
                  </el-form-item>
                  <el-form-item label="登录提示语">
                    <el-switch v-model="baseForm.lastLoginTimeSwitch" :active-value="1"
                      :inactive-value="0" />
                  </el-form-item>
                  <el-form-item v-if="baseForm.lastLoginTimeSwitch">
                    <el-card class="box-card" shadow="never" style="width: 300px;"
                      :body-style="{ padding: '0px 20px' }">
                      <div slot="header">
                        <span>上次登录信息提示</span>
                        <i style="float: right; padding: 11px 0" class="el-icon-close" />
                      </div>
                      <div class="item">
                        <p>时间：2018-10-17 12:40</p>
                        <p>地点：上海市</p>
                        <p>IP：255.255.0.0</p>
                      </div>
                    </el-card>
                  </el-form-item>
                  <el-form-item label="白名单验证">
                    <el-switch v-model="baseForm.whitelistSwitch" :active-value="1"
                      :inactive-value="0" />
                  </el-form-item>
                  <el-form-item label="白名单设置" v-if="baseForm.whitelistSwitch">
                    <el-input type="textarea" :autosize="{ minRows: 3, maxRows: 10}"
                      v-model="baseForm.whiteListIp" placeholder="允许访问IP" />
                    <div class="tip">多个IP设置，用英文符号隔开，如192.168.0.1,192.168.0.2</div>
                  </el-form-item>
                  <el-form-item>
                    <el-button type="primary" size="small" :loading="btnLoading" class="saveBtn"
                      @click="submitForm()">保 存</el-button>
                  </el-form-item>
                </el-col>
              </el-row>
            </el-tab-pane>
            <el-tab-pane label="密码策略">
              <el-row>
                <el-col :span="18">
                  <el-form-item label="密码定期更新">
                    <el-switch v-model="baseForm.passwordIsUpdatedRegularly" :active-value="1"
                      :inactive-value="0" />
                  </el-form-item>
                  <el-form-item label="更新周期" v-if="baseForm.passwordIsUpdatedRegularly">
                    <el-input-number v-model="baseForm.updateCycle" :precision="0" :step="1"
                      :min="1" controls-position="right" @blur="initConfig" /> 天
                  </el-form-item>
                  <el-form-item label="提前" v-if="baseForm.passwordIsUpdatedRegularly">
                    <el-input-number v-model="baseForm.updateInAdvance" :precision="0" :step="1"
                      :min="1" controls-position="right" @blur="initConfig" /> 天提醒更新
                  </el-form-item>
                  <el-form-item label="密码强度限制">
                    <el-switch v-model="baseForm.passwordStrengthLimit" :active-value="1"
                      :inactive-value="0" />
                  </el-form-item>
                  <el-form-item v-if="baseForm.passwordStrengthLimit">
                    <el-checkbox v-model="baseForm.passwordLengthMin" :true-label="1"
                      :false-label="0">最小长度</el-checkbox>
                    <el-input-number v-model="baseForm.passwordLengthMinNumber" :precision="0"
                      :min="1" :step="1" controls-position="right" @blur="initConfig" /><br />
                    <el-checkbox v-model="baseForm.containsNumbers" :true-label="1"
                      :false-label="0">包含数字</el-checkbox><br />
                    <el-checkbox v-model="baseForm.includeLowercaseLetters" :true-label="1"
                      :false-label="0">包含小写字母</el-checkbox><br />
                    <el-checkbox v-model="baseForm.includeUppercaseLetters" :true-label="1"
                      :false-label="0">包含大写字母</el-checkbox><br />
                    <el-checkbox v-model="baseForm.containsCharacters" :true-label="1"
                      :false-label="0">包含字符</el-checkbox>
                  </el-form-item>
                  <el-form-item label="禁用旧密码">
                    <el-switch v-model="baseForm.disableOldPassword" :active-value="1"
                      :inactive-value="0" />
                  </el-form-item>
                  <el-form-item label="禁用个数" v-if="baseForm.disableOldPassword">
                    <el-input-number v-model="baseForm.disableTheNumberOfOldPasswords" :min="1"
                      :precision="0" :step="1" controls-position="right" @blur="initConfig" /> 个
                  </el-form-item>
                  <el-form-item label="修改初始密码提醒">
                    <el-switch v-model="baseForm.mandatoryModificationOfInitialPassword"
                      :active-value="1" :inactive-value="0" />
                  </el-form-item>
                  <el-form-item>
                    <el-button type="primary" size="small" :loading="btnLoading" class="saveBtn"
                      @click="submitForm()">保 存</el-button>
                  </el-form-item>
                </el-col>
              </el-row>
            </el-tab-pane>
          </el-tabs>
        </el-tab-pane>
        <el-tab-pane label="同步设置" name="third">
          <el-tabs tab-position="left" style="height:100%" v-model="thirdTab" class="thirdTab">
            <el-tab-pane name="1" label="企业微信">
              <el-row :gutter="20">
                <el-col :span="12" :offset="6" :pull="6">
                  <el-form-item label="企业号Id" prop="qyhCorpId">
                    <el-input v-model="baseForm.qyhCorpId" clearable placeholder="请输入CorpId" />
                  </el-form-item>
                </el-col>
                <el-col :span="12" :offset="6" :pull="6">
                  <el-form-item label="应用凭证" prop="qyhAgentId">
                    <el-input v-model="baseForm.qyhAgentId" clearable placeholder="请输入AgentId" />
                  </el-form-item>
                </el-col>
                <el-col :span="12" :offset="6" :pull="6">
                  <el-form-item label="凭证密钥" prop="qyhAgentSecret">
                    <el-input v-model="baseForm.qyhAgentSecret" placeholder="请输入AppSecret">
                      <el-button slot="append" @click="checkQy(0)" :loading="testQyLoading">连接测试
                      </el-button>
                    </el-input>
                  </el-form-item>
                </el-col>
                <el-col :span="12" :offset="6" :pull="6">
                  <el-form-item label="同步密钥" prop="qyhCorpSecret">
                    <el-input v-model="baseForm.qyhCorpSecret" placeholder="请输入CorpSecret">
                      <el-button slot="append" @click="checkQy(1)" :loading="testSyncLoading">同步测试
                      </el-button>
                    </el-input>
                  </el-form-item>
                </el-col>
                <el-col>
                  <el-form-item label="触发事件">
                    <el-table :data="wxEvents" border>
                      <el-table-column prop="select" label="" width="50" align="center">
                        <template slot-scope="scope">
                          <el-checkbox v-model='scope.row.select' />
                        </template>
                      </el-table-column>
                      <el-table-column prop="title" label="触发事件" width="200" />
                      <el-table-column prop="desc" label="描述" />
                    </el-table>
                  </el-form-item>
                </el-col>
                <el-col>
                  <el-form-item label="同步反馈">
                    <el-table :data="wxResults" border>
                      <el-table-column prop="synType" label="同步类型" />
                      <el-table-column prop="recordTotal" label="总数" />
                      <el-table-column prop="synSuccessCount" label="同步成功数" />
                      <el-table-column prop="synFailCount" label="同步失败数" />
                      <el-table-column prop="unSynCount" label="未同步数" />
                      <el-table-column prop="synDate" label="同步时间" :formatter="jnpf.tableDateFormat"
                        width="140" />
                      <el-table-column label="操作" width="70">
                        <template slot-scope="scope">
                          <el-button size="mini" type="text" @click="syncWechat(scope.row)"
                            :loading="scope.row.loading">同步</el-button>
                        </template>
                      </el-table-column>
                    </el-table>
                  </el-form-item>
                </el-col>
                <el-col>
                  <el-form-item>
                    <el-button type="primary" size="small" :loading="btnLoading" class="saveBtn"
                      @click="submitForm()">保 存</el-button>
                  </el-form-item>
                </el-col>
              </el-row>
            </el-tab-pane>
            <el-tab-pane name="2" label="阿里钉钉">
              <el-row :gutter="20">
                <el-col :span="12" :offset="6" :pull="6">
                  <el-form-item label="企业号Id" prop="dingAgentId">
                    <el-input v-model="baseForm.dingAgentId" clearable placeholder="请输入AgentId" />
                  </el-form-item>
                </el-col>
                <el-col :span="12" :offset="6" :pull="6">
                  <el-form-item label="应用凭证" prop="dingSynAppKey">
                    <el-input v-model="baseForm.dingSynAppKey" clearable placeholder="请输入AppKey" />
                  </el-form-item>
                </el-col>
                <el-col :span="12" :offset="6" :pull="6">
                  <el-form-item label="凭证密钥" prop="dingSynAppSecret">
                    <el-input v-model="baseForm.dingSynAppSecret" placeholder="请输入AppSecret">
                      <el-button slot="append" @click="checkDing" :loading="testDingLoading">连接测试
                      </el-button>
                    </el-input>
                  </el-form-item>
                </el-col>
                <el-col>
                  <el-form-item label="触发事件">
                    <el-table :data="ddEvents" border>
                      <el-table-column prop="select" label="" width="50" align="center">
                        <template slot-scope="scope">
                          <el-checkbox v-model='scope.row.select' />
                        </template>
                      </el-table-column>
                      <el-table-column prop="title" label="触发事件" width="200" />
                      <el-table-column prop="desc" label="描述" />
                    </el-table>
                  </el-form-item>
                </el-col>
                <el-col>
                  <el-form-item label="同步反馈">
                    <el-table :data="ddResults" border>
                      <el-table-column prop="synType" label="同步类型" />
                      <el-table-column prop="recordTotal" label="总数" />
                      <el-table-column prop="synSuccessCount" label="同步成功数" />
                      <el-table-column prop="synFailCount" label="同步失败数" />
                      <el-table-column prop="unSynCount" label="未同步数" />
                      <el-table-column prop="synDate" label="同步时间" :formatter="jnpf.tableDateFormat"
                        width="140" />
                      <el-table-column label="操作" width="70">
                        <template slot-scope="scope">
                          <el-button size="mini" type="text" @click="syncDingVisible(scope.row)">同步
                          </el-button>
                        </template>
                      </el-table-column>
                    </el-table>
                  </el-form-item>
                </el-col>
                <el-col>
                  <el-form-item>
                    <el-button type="primary" size="small" :loading="btnLoading" class="saveBtn"
                      @click="submitForm()">保 存</el-button>
                  </el-form-item>
                </el-col>
              </el-row>
            </el-tab-pane>
          </el-tabs>
        </el-tab-pane>
        <el-tab-pane label="管理员设置" name="fourth">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="超级管理员">
                <JnpfUserSelect v-model="adminIds" placeholder="请选择超级管理员" multiple />
              </el-form-item>
            </el-col>
            <el-col>
              <el-form-item>
                <el-button type="primary" size="small" :loading="btnLoading" class="saveBtn"
                  @click="setAdminList()">保 存</el-button>
              </el-form-item>
            </el-col>
          </el-row>
        </el-tab-pane>
        <el-tab-pane label="流程设置" name="process">
          <el-row>
            <el-col :span="18">
              <jnpf-form-tip-item label-width="120px" label="链接时效性" prop="title"
                tipLabel="审批链接是否有效：审批链接发送时间 + 审批链接时效性">
                <el-input-number v-model="baseForm.linkTime" :min="1" :precision="0" :step="1"
                  controls-position="right" />
                <span class="lockTime unit">小时</span>
              </jnpf-form-tip-item>
              <jnpf-form-tip-item label-width="120px" label="链接点击失效" prop="title"
                tipLabel=" 禁用：不判断点击次数；启用：根据失效次数判断，点击超过次数链接失效。">
                <el-switch v-model="baseForm.isClick" :active-value="1" :inactive-value="0" />
              </jnpf-form-tip-item>
              <jnpf-form-tip-item label-width="120px" label="失效次数" v-if="baseForm.isClick">
                <el-input-number v-model="baseForm.unClickNum" :min="1" :precision="0" :step="1"
                  controls-position="right" />
                <span class="lockTime unit">次</span>
              </jnpf-form-tip-item>
              <el-form-item label-width="120px">
                <el-button type="primary" size="small" :loading="btnLoading" class="saveBtn"
                  @click="submitForm()">保 存</el-button>
              </el-form-item>
            </el-col>
          </el-row>
        </el-tab-pane>
      </el-tabs>
    </el-form>
    <el-dialog title="数据同步" :visible.sync="visible"
      class="JNPF-dialog JNPF-dialog_center sync-dialog" lock-scroll width="450px">
      <div class="add-main">
        <div class="add-item add-item-sys">
          <i class="add-icon icon-ym icon-ym-upload"></i>
          <div class="add-txt">
            <p class="add-title">{{name}}</p>
            <p class="add-desc">{{names}}</p>
          </div>
          <div class="add-button">
            <el-button type="primary" size="small" @click="name==='同步到企业微信'?syncQy(0):syncDing(0)"
              :loading="wechatLoading" :disabled="dingLoading">
              同步</el-button>
          </div>
        </div>
      </div>
      <div class="add-main">
        <div class="add-item add-item-sys">
          <i class="add-icons icon-ym icon-ym-download"></i>
          <div class="add-txt">
            <p class="add-title">同步到系统</p>
            <p class="add-desc">{{synchronization}}</p>
          </div>
          <div class="add-button">
            <el-button type="primary" size="small" @click="name==='同步到企业微信'?syncQy(1):syncDing(1)"
              :loading="dingLoading" :disabled="wechatLoading">
              同步</el-button>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  getSystemConfig,
  updateSystemConfig,
  testQy,
  testDing,
  getSynThirdTotal,
  synAllOrganizeSysToDing,
  synAllUserSysToDing,
  synAllOrganizeSysToQy,
  synAllUserSysToQy,
  getAdminList,
  setAdminList
} from '@/api/system/sysConfig'
import singleImg from '@/components/Upload/SingleImg'

export default {
  name: 'system-sysConfig',
  components: { singleImg },
  data() {
    return {
      activeName: 'first',
      listLoading: false,
      btnLoading: false,
      testQyLoading: false,
      testSyncLoading: false,
      testDingLoading: false,
      wechatLoading: false,
      dingLoading: false,
      thirdTab: '1',
      secondTab: '0',
      visible: false,
      synchronization: '',
      name: '',
      names: '',
      row: '',
      baseForm: {
        title: '',
        sysName: '',
        sysDescription: '',
        sysVersion: '',
        loginIcon: '',
        logoIcon: '',
        appIcon: '',
        copyright: '',
        companyName: '',
        companyCode: '',
        companyAddress: '',
        companyContacts: '',
        companyTelePhone: '',
        companyEmail: '',
        singleLogin: '1',
        tokenTimeout: '1',
        lastLoginTimeSwitch: 1,
        whitelistSwitch: 1,
        whiteListIp: '',
        qyhCorpId: '',
        qyhAgentId: '',
        qyhAgentSecret: '',
        qyhCorpSecret: '',
        qyhIsSynOrg: 0,
        qyhIsSynUser: 0,
        dingSynAppKey: '',
        dingSynAppSecret: '',
        dingAgentId: '',
        dingSynIsSynOrg: 0,
        dingSynIsSynUser: 0,
        passwordErrorsNumber: 0,
        lockType: 1,
        lockTime: 10,
        enableVerificationCode: 0,
        passwordIsUpdatedRegularly: 0,
        updateCycle: 0,
        updateInAdvance: 0,
        passwordStrengthLimit: 0,
        passwordLengthMin: false,
        passwordLengthMinNumber: 0,
        containsNumbers: false,
        includeLowercaseLetters: false,
        includeUppercaseLetters: false,
        containsCharacters: false,
        disableOldPassword: 0,
        disableTheNumberOfOldPasswords: 0,
        mandatoryModificationOfInitialPassword: 0,
        linkTime: 24,
        isClick: 0,
        unClickNum: 1,
      },
      rules: {},
      wxEvents: [{
        select: false,
        title: '启用同步组织',
        desc: '新增、删除、修改组织信息触发同步组织事件'
      }, {
        select: false,
        title: '启用同步用户',
        desc: '新增、删除、修改用户信息触发同步用户事件'
      }],
      wxResults: [{
        recordTotal: '',
        synDate: '',
        synFailCount: '',
        synSuccessCount: '',
        synType: '组织',
        unSynCount: '',
      }, {
        recordTotal: '',
        synDate: '',
        synFailCount: '',
        synSuccessCount: '',
        synType: '用户',
        unSynCount: '',
      }],
      ddEvents: [{
        select: false,
        title: '启用同步组织',
        desc: '新增、删除、修改组织信息触发同步组织事件'
      }, {
        select: false,
        title: '启用同步用户',
        desc: '新增、删除、修改用户信息触发同步用户事件'
      }],
      ddResults: [{
        recordTotal: '',
        synDate: '',
        synFailCount: '',
        synSuccessCount: '',
        synType: '组织',
        unSynCount: '',
      }, {
        recordTotal: '',
        synDate: '',
        synFailCount: '',
        synSuccessCount: '',
        synType: '用户',
        unSynCount: '',
      }],
      adminIds: []
    }
  },
  watch: {
    thirdTab: {
      handler(val) {
        this.getSynThirdTotal(val)
      },
      immediate: true
    },
    activeName(val) {
      if (val === 'fourth') {
        this.getAdminList()
      }
    }
  },
  created() {
    this.activeName = this.$route.query.type == 1 ? 'third' : 'first'
    this.initData()
  },
  methods: {
    initConfig() {
      if (!this.baseForm.disableTheNumberOfOldPasswords) this.baseForm.disableTheNumberOfOldPasswords = 1
      if (!this.baseForm.updateCycle) this.baseForm.updateCycle = 1
      if (!this.baseForm.updateInAdvance) this.baseForm.updateInAdvance = 1
      if (!this.baseForm.passwordLengthMinNumber) this.baseForm.passwordLengthMinNumber = 1
    },
    initData() {
      this.listLoading = true
      this.$nextTick(() => {
        getSystemConfig().then(res => {
          this.baseForm = res.data
          this.wxEvents[0].select = this.baseForm.qyhIsSynOrg ? true : false
          this.wxEvents[1].select = this.baseForm.qyhIsSynUser ? true : false
          this.ddEvents[0].select = this.baseForm.dingSynIsSynOrg ? true : false
          this.ddEvents[1].select = this.baseForm.dingSynIsSynUser ? true : false
          this.baseForm.smsCompany = this.baseForm.smsCompany ? this.baseForm.smsCompany : '1'
          this.baseForm.lockType = this.baseForm.lockType ? this.baseForm.lockType : 1
          this.baseForm.lockTime = this.baseForm.lockTime ? this.baseForm.lockTime : 10
          this.baseForm.isClick = this.baseForm.isClick ? this.baseForm.isClick : 0
          this.baseForm.linkTime = this.baseForm.linkTime ? this.baseForm.linkTime : 24
          this.baseForm.unClickNum = this.baseForm.unClickNum ? this.baseForm.unClickNum : 1
          this.listLoading = false
        }).catch(() => {
          this.listLoading = false
        })
      })
    },
    checkQy(type) {
      type === 1 ? this.testSyncLoading = true : this.testQyLoading = true
      const data = {
        qyhAgentId: this.baseForm.qyhAgentId,
        qyhAgentSecret: this.baseForm.qyhAgentSecret,
        qyhCorpId: this.baseForm.qyhCorpId,
        qyhCorpSecret: this.baseForm.qyhCorpSecret
      }
      testQy(data, type).then(res => {
        this.$message({
          message: res.msg,
          type: 'success',
          duration: 1500,
          onClose: () => {
            type === 1 ? this.testSyncLoading = false : this.testQyLoading = false
          }
        })
      }).catch(() => {
        type === 1 ? this.testSyncLoading = false : this.testQyLoading = false
      })
    },
    checkDing() {
      this.testDingLoading = true
      const data = {
        dingAgentId: this.baseForm.dingAgentId,
        dingSynAppKey: this.baseForm.dingSynAppKey,
        dingSynAppSecret: this.baseForm.dingSynAppSecret
      }
      testDing(data).then(res => {
        this.$message({
          message: res.msg,
          type: 'success',
          duration: 1500,
          onClose: () => {
            this.testDingLoading = false
          }
        })
      }).catch(() => {
        this.testDingLoading = false
      })
    },
    getSynThirdTotal(type) {
      getSynThirdTotal(type).then(res => {
        let list = res.data.map(o => ({ loading: false, ...o }))
        type == '1' ? this.wxResults = list : this.ddResults = list
      })
    },
    syncWechat(row) {
      this.name = '同步到企业微信'
      this.names = '把系统数据同步到企业微信'
      this.synchronization = '把企业微信数据同步到系统'
      this.row = row || ''
      this.visible = true

    },
    syncDingVisible(row) {
      this.name = '同步到阿里钉钉'
      this.names = '把系统数据同步到阿里钉钉'
      this.synchronization = '把阿里钉钉数据同步到系统'
      this.row = row || ''
      this.visible = true
    },
    syncQy(type) {
      this.$confirm('同步以后会丢失现有数据，是否继续？', '提示', {
        type: 'warning'
      }).then(() => {
        type == 0 ? this.wechatLoading = true : this.dingLoading = true
        const method = this.row.synType == '组织' ? synAllOrganizeSysToQy : synAllUserSysToQy
        method(type).then(res => {
          type == 0 ? this.wechatLoading = false : this.dingLoading = false
          this.visible = false
          if (res.msg === '正在进行同步,请稍等') {
            this.$message({
              message: res.msg,
              type: 'success',
              duration: 1500,
            })
            return
          }
          this.row.recordTotal = res.data.recordTotal
          this.row.synDate = res.data.synDate
          this.row.synFailCount = res.data.synFailCount
          this.row.synSuccessCount = res.data.synSuccessCount
          this.row.synType = res.data.synType
          this.row.unSynCount = res.data.unSynCount
          this.$message({
            message: '同步成功',
            type: 'success',
            duration: 1500,
          })
        }).catch(() => { type == 0 ? this.wechatLoading = false : this.dingLoading = false })
      })
    },
    syncDing(type) {
      this.$confirm('同步以后会丢失现有数据，是否继续？', '提示', {
        type: 'warning'
      }).then(() => {
        type == 0 ? this.wechatLoading = true : this.dingLoading = true
        const method = this.row.synType == '组织' ? synAllOrganizeSysToDing : synAllUserSysToDing
        method(type).then(res => {
          type == 0 ? this.wechatLoading = false : this.dingLoading = false
          this.visible = false
          if (res.msg === '正在进行同步,请稍等') {
            this.$message({
              message: res.msg,
              type: 'success',
              duration: 1500,
            })
            return
          }
          this.row.recordTotal = res.data.recordTotal
          this.row.synDate = res.data.synDate
          this.row.synFailCount = res.data.synFailCount
          this.row.synSuccessCount = res.data.synSuccessCount
          this.row.synType = res.data.synType
          this.row.unSynCount = res.data.unSynCount
          this.$message({
            message: '同步成功',
            type: 'success',
            duration: 1500,
          })
        }).catch(() => { type == 0 ? this.wechatLoading = false : this.dingLoading = false })
      })
    },
    submitSmsForm() {
      this.$refs['baseForm'].validate((valid) => {
        if (!valid) return
        this.submitForm()
      })
    },
    submitForm() {
      this.baseForm.qyhIsSynOrg = this.wxEvents[0].select ? 1 : 0
      this.baseForm.qyhIsSynUser = this.wxEvents[1].select ? 1 : 0
      this.baseForm.dingSynIsSynOrg = this.ddEvents[0].select ? 1 : 0
      this.baseForm.dingSynIsSynUser = this.ddEvents[1].select ? 1 : 0
      this.baseForm.passwordLengthMin = this.baseForm.passwordLengthMin ? 1 : 0
      this.baseForm.containsNumbers = this.baseForm.containsNumbers ? 1 : 0
      this.baseForm.includeLowercaseLetters = this.baseForm.includeLowercaseLetters ? 1 : 0
      this.baseForm.includeUppercaseLetters = this.baseForm.includeUppercaseLetters ? 1 : 0
      this.baseForm.containsCharacters = this.baseForm.containsCharacters ? 1 : 0
      this.$confirm('您确定要保存，是否继续？', '提示', {
        type: 'warning'
      }).then(() => {
        this.btnLoading = true
        updateSystemConfig(this.baseForm).then(res => {
          this.$message({
            message: res.msg,
            type: 'success',
            duration: 1500,
            onClose: () => {
              this.btnLoading = false
              const sysConfig = {
                sysName: this.baseForm.sysName,
                sysVersion: this.baseForm.sysVersion,
                loginIcon: this.baseForm.loginIcon,
                copyright: this.baseForm.copyright,
                companyName: this.baseForm.companyName,
                logoIcon: this.baseForm.logoIcon,
                appIcon: this.baseForm.appIcon,
                title: this.baseForm.title
              }
              this.$store.commit('settings/CHANGE_SETTING', { key: "sysConfig", value: sysConfig })
              this.initData()
            }
          })
        }).catch(() => {
          this.btnLoading = false
        })
      }).catch(() => { this.btnLoading = false })
    },
    getAdminList() {
      getAdminList().then(res => {
        if (!res.data) return
        this.adminIds = res.data.map(o => o.id)
      })
    },
    setAdminList() {
      this.btnLoading = true
      this.$confirm('您确定要保存，是否继续？', '提示', {
        type: 'warning'
      }).then(() => {
        setAdminList(this.adminIds).then(res => {
          this.$message({
            message: res.msg,
            type: 'success',
            duration: 1500,
            onClose: () => {
              this.btnLoading = false
            }
          })
        })
      }).catch(() => { this.btnLoading = false })
    }
  }
}
</script>
<style lang="scss" scoped>
.systemConfig {
  padding: 0;
  >>> .el-card__header {
    padding: 6px 20px;
  }
  >>> .el-tabs--border-card {
    box-shadow: none;
    height: calc(100vh - 120px);
  }
  >>> .el-tabs--border-card > .el-tabs__content {
    padding: 30px;
  }
  .saveBtn {
    width: 100px;
  }
  .el-table >>> th {
    line-height: 23px;
  }
  .thirdTab >>> .el-tabs__content {
    height: 100%;
    .el-tab-pane {
      height: auto;
    }
  }
  .tip {
    font-size: 14px;
    color: #a5a5a5;
  }
  .lockTime {
    line-height: 32px;
    padding-right: 12px;
    color: #606266;
    font-size: 14px;
  }
  .unit {
    padding-right: unset;
    padding-left: 12px;
  }
  .img-list {
    display: flex;
    >>> .singleImg-container {
      margin-right: 20px;
      :last-child {
        margin-right: 0;
      }
    }
  }
  .secondTab {
    >>> .el-tabs__content {
      height: 100%;
    }
  }
}
.add-main {
  display: flex;
  align-items: center;
  justify-content: space-between;
  .add-item {
    width: 450px;
    height: 100px;
    display: flex;
    align-items: center;
    cursor: pointer;
    &.add-item-sys {
      .add-icon {
        background: #75d8f791;
        color: #08c0f8;
      }
    }
    .add-icon {
      width: 56px;
      height: 56px;
      margin-right: 10px;
      background: #cefae2;
      border-radius: 50%;
      color: #0eac5c;
      flex-shrink: 0;
      font-size: 30px;
      line-height: 56px;
      text-align: center;
    }
    .add-button {
      margin-left: 80px;
    }
    .add-icons {
      width: 56px;
      height: 56px;
      margin-right: 10px;
      background: #cefae2;
      border-radius: 50%;
      color: #0eac5c;
      flex-shrink: 0;
      font-size: 30px;
      line-height: 56px;
      text-align: center;
    }
    .add-txt {
      height: 56px;
      P {
        line-height: 28px;
      }
      .add-title {
        font-size: 18px;
        font-weight: bold;
      }
      .add-desc {
        color: #8d8989;
        font-size: 12px;
        width: 150px;
      }
    }
  }
}
>>> .sync-dialog .el-dialog__body {
  padding: 20px 50px !important;
}
</style>
