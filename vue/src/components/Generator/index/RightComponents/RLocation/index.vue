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
    <jnpf-form-tip-item label="获取定位" tip-label="桌面端不支持自动获取定位">
      <el-switch v-model="activeData.autoLocation" />
    </jnpf-form-tip-item>
    <jnpf-form-tip-item label="允许微调" tip-label="是否允许用户在当前定位的范围可以微调距离">
      <el-switch v-model="activeData.enableLocationScope" />
    </jnpf-form-tip-item>
    <jnpf-form-tip-item label="微调范围" tip-label="设置用户可以微调的范围" v-if="activeData.enableLocationScope">
      <el-input type='number' v-model="activeData.adjustmentScope" :min="1" @change="handleNumber">
        <template slot="append">米</template>
      </el-input>
    </jnpf-form-tip-item>
    <jnpf-form-tip-item label="定位区域" tip-label="设置用户可以提交定位的范围">
      <el-switch v-model="activeData.enableDesktopLocation" />
    </jnpf-form-tip-item>
    <template v-if="activeData.enableDesktopLocation">
      <div class="location-scope-item" v-for="(item, index) in activeData.locationScope"
        :key="index">
        <span class="name">{{ item.name }}</span>
        <div class="edit-btn" @click="addOrUpdateHandle(item)">
          <i class="icon-ym icon-ym-btn-edit" />
        </div>
        <div class="close-btn" @click="activeData.locationScope.splice(index, 1)">
          <i class="icon-ym icon-ym-btn-clearn" />
        </div>
      </div>
      <el-button type="text" icon="icon-ym icon-ym-btn-add" @click="addOrUpdateHandle()"
        class="!px-0 mb-10px">新增定位中心</el-button>
    </template>
    <el-form-item label="能否清空">
      <el-switch v-model="activeData.clearable" />
    </el-form-item>
    <el-form-item label="是否禁用">
      <el-switch v-model="activeData.disabled" />
    </el-form-item>
    <el-form-item label="是否必填">
      <el-switch v-model="activeData.__config__.required" />
    </el-form-item>
    <el-form-item label="是否隐藏">
      <el-switch v-model="activeData.__config__.noShow" />
    </el-form-item>
    <MapModal v-if="mapVisible" ref="registerModal" @confirm="handleConfirm" />
  </el-row>
</template>
<script>
import MapModal from './MapModal';
export default {
  props: ['activeData'],
  components: { MapModal },
  data() {
    return {
      mapVisible: false,
      props: {
        label: "fullName",
        value: "id"
      }
    }
  },
  methods: {
    addOrUpdateHandle(item) {
      this.mapVisible = true
      this.$nextTick(() => {
        this.$refs.registerModal.init({ item })
      })
    },
    handleConfirm(item) {
      this.mapVisible = false
      if (item.id) {
        for (let i = 0; i < this.activeData.locationScope.length; i++) {
          let o = this.activeData.locationScope[i];
          if (item.id === o.id) o = item;
        }
      } else {
        item.id = this.jnpf.idGenerator()
        this.activeData.locationScope = this.activeData.locationScope || []
        this.activeData.locationScope.push(item);
      }
    },
    handleNumber(val) {
      this.$nextTick(() => {
        this.$set(this.activeData, 'adjustmentScope', val && val > 0 ? Number(val) : 1)
      })
    }
  }
}
</script>
<style lang="scss" scoped>
.location-scope-item {
  display: flex;
  align-items: center;
  margin-bottom: 5px;
  .name {
    min-width: 0;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    flex: 1;
  }
  .edit-btn,
  .close-btn {
    cursor: pointer;
    .icon-ym {
      font-size: 18px;
    }
  }
  .edit-btn {
    color: #1890ff;
    margin: 0 5px 0 10px;
  }
  .close-btn {
    color: #ed6f6f;
  }
}
</style>
