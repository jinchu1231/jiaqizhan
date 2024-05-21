<template>
  <el-dialog
    title="页面设置"
    :visible.sync="dialogFormVisible"
    append-to-body
    width="400px"
  >
    <el-form :model="form" :label-position="'top'">
      <el-form-item label="纸张设置" :label-width="formLabelWidth">
        <el-select v-model="form.type" placeholder="请选择" @change="change">
          <el-option
            v-for="item in options"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          >
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="" :label-width="formLabelWidth" v-if="form.type">
        <el-row :gutter="30">
          <el-col :span="1">宽</el-col>
          <el-col :span="9"
            ><el-input-number
              :disabled="form.type == '6' ? false : true"
              style="width: 60px"
              v-model="form.width"
              :min="1"
              :controls="false"
            ></el-input-number>
            <span style="margin-left:10px">mm</span>
          </el-col>
          <el-col :span="1">高</el-col>
          <el-col :span="9"
            ><el-input-number
              :disabled="form.type == '6' ? false : true"
              style="width: 60px"
              v-model="form.height"
              :min="1"
              :controls="false"
            ></el-input-number>
            <span style="margin-left:10px">mm</span>
          </el-col>
        </el-row>
      </el-form-item>

      <el-form-item label="方向" :label-width="formLabelWidth">
        <el-radio-group v-model="form.direction" @input="inputChange">
          <el-radio-button
            v-for="(item, index) in directions"
            :label="item.label"
            :key="index"
          ></el-radio-button>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="页边距" :label-width="formLabelWidth">
        <el-row :gutter="30">
          <el-col :span="1">上</el-col>
          <el-col :span="9"
            ><el-input-number
              style="width: 60px"
              v-model="form.mt"
              :min="1"
              :controls="false"
            ></el-input-number>
            <span style="margin-left:10px">mm</span>
          </el-col>
          <el-col :span="1">下</el-col>
          <el-col :span="9"
            ><el-input-number
              style="width: 60px"
              v-model="form.mb"
              :min="1"
              :controls="false"
            ></el-input-number>
            <span style="margin-left:10px">mm</span>
          </el-col>
        </el-row>
        <el-row class="mt10" :gutter="30">
          <el-col :span="1">左</el-col>
          <el-col :span="9"
            ><el-input-number
              style="width: 60px"
              v-model="form.ml"
              :min="1"
              :controls="false"
            ></el-input-number>
            <span style="margin-left:10px">mm</span>
          </el-col>
          <el-col :span="1">右</el-col>
          <el-col :span="9"
            ><el-input-number
              style="width: 60px"
              v-model="form.mr"
              :min="1"
              :controls="false"
            ></el-input-number>
            <span style="margin-left:10px">mm</span>
          </el-col>
        </el-row>
      </el-form-item>
    </el-form>
    <div slot="footer" class="dialog-footer">
      <el-button @click="dialogFormVisible = false">取 消</el-button>
      <el-button type="primary" @click="confirm">确 定</el-button>
    </div>
  </el-dialog>
</template>

<script>
export default {
  components: {},
  props: ["value"],
  watch: {
    value: {
      handler(val) {
        if(val){
          this.form = JSON.parse(JSON.stringify(val));
        }
        
      },
      immediate: true,
      deep: true
    }
  },
  data() {
    return {
      options: [
        {
          label: "A3",
          value: "1"
        },
        {
          label: "A4",
          value: "2"
        },
        {
          label: "A5",
          value: "3"
        },
        {
          label: "B3",
          value: "4"
        },
        {
          label: "B5",
          value: "5"
        },
        {
          label: "自定义",
          value: "6"
        }
      ],
      directions: [
        {
          label: "纵向"
        },
        {
          label: "横向"
        }
      ],
      dialogFormVisible: false,
      form: {
        mt: "10",
        mb: "10",
        ml: "10",
        mr: "10",
        direction: "纵向",
        width:210,
        height:297
      
      },
      formLabelWidth: "120px",
      sizeMap: {
        "1": [297, 420],
        "2": [210, 297],
        "3": [148, 210],
        "4": [353, 500],
        "5": [176, 250]
      }
    };
  },
  methods: {
    inputChange(e) {
      let temp = this.form.height;
      this.form.height = this.form.width;
      this.form.width = temp;
    },
    change(e) {
      let data = this.sizeMap[e];
      this.form.width = data[0];
      this.form.height = data[1];
    },
    confirm() {
      try {
        let width = this.form.width;
        let height = this.form.height;
        let mt = this.form.mt ? this.form.mt : 1;
        let mb = this.form.mb ? this.form.mb : 1;
        let ml = this.form.ml ? this.form.ml : 1;
        let mr = this.form.mr ? this.form.mr : 1;
        if (width <= Number(ml) + Number(mr)) {
          this.$message.warning("左右边距合计不能超过纸张宽度");
          return;
        }
        if (height <= Number(mt) + Number(mb)) {
          this.$message.warning("上下边距合计不能超过纸张高度");
          return;
        }
        this.$emit("change", this.form);
        this.dialogFormVisible = false;
      } catch (error) {
        this.dialogFormVisible = false;
      }
    }
  }
};
</script>

<style lang="scss" scoped>
.mt10 {
  margin-top: 10px;
}
</style>
