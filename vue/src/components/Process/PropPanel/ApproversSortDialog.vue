<template>
  <el-dialog title="设置依次审批顺序" :visible.sync="visible"
    class="JNPF-dialog JNPF-dialog_center formula-dialog" append-to-body width="400px">
    <div class="approvers-sort-body">
      <el-scrollbar class="list" v-loading="loading">
        <draggable :list='list' :animation="300" group="selectItem" handle=".option-drag"
          itemKey="id">
          <div class="column-item" v-for="item in list" :key="item.id">
            <div class="selected-item selected-item-user">
              <div class="selected-item-main">
                <el-avatar :size="36" :src="define.comUrl + item.headIcon"
                  class="selected-item-headIcon" />
                <div class="selected-item-text">
                  <p class="name">{{ item.fullName }}</p>
                  <p class="organize" :title="item.organize">{{ item.organize }}</p>
                </div>
                <div class="option-drag">
                  <i class="icon-ym icon-ym-darg" />
                </div>
              </div>
            </div>
          </div>
        </draggable>
      </el-scrollbar>
    </div>
    <span slot="footer" class="dialog-footer">
      <el-button @click="visible = false" size="small">{{$t('common.cancelButton')}}</el-button>
      <el-button type="primary" @click="handleSubmit()" size="small">{{$t('common.confirmButton')}}
      </el-button>
    </span>
  </el-dialog>
</template>
<script>
import { getSelectedList } from '@/api/permission/user';
import draggable from 'vuedraggable';
export default {
  name: 'ApproversSortDialog',
  components: { draggable },
  data() {
    return {
      visible: false,
      loading: false,
      list: [],
    }
  },
  methods: {
    init(data) {
      console.log(data)
      this.visible = true
      if (!data.ids || !data.ids.length) return;
      this.loading = true;
      this.list = [];
      getSelectedList(data.ids).then(res => {
        this.list = res.data.list || [];
        this.loading = false;
      });
    },
    handleSubmit() {
      const ids = this.list.map(o => o.id + '--user');
      this.$emit('confirm', ids);
      this.visible = false
    }
  }
}
</script>
<style lang="scss" scoped>
.approvers-sort-body {
  .list {
    height: 360px;
    font-size: 14px;
    border: 1px solid #d9d9d9;
    border-radius: 2px;
    .selected-item {
      width: 100%;
      padding: 0px 12px;
      display: flex;
      justify-content: space-between;
      align-items: center;

      &.selected-item-user {
        &:last-child {
          .selected-item-main {
            border-bottom-color: transparent;
          }
        }
        .selected-item-main {
          display: flex;
          align-items: center;
          height: 50px;
          width: 100%;
          box-sizing: border-box;
        }
        .selected-item-headIcon {
          flex-shrink: 0;
        }
        .selected-item-text {
          min-width: 0;
          flex: 1;
          margin-left: 10px;
          .name {
            height: 20px;
            line-height: 20px;
            font-size: 14px;
            margin-bottom: 2px;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }
          .organize {
            height: 17px;
            line-height: 17px;
            color: #999999;
            font-size: 12px;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }
        }
      }
      span {
        max-width: 90%;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }
      .option-drag {
        cursor: move;
        color: #606266;
        .icon-ym-darg {
          font-size: 20px;
          display: inline-block;
        }
      }
    }
  }
}
</style>
