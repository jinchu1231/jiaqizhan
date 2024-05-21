<template>
  <div>
    <el-dialog :title="!dataForm.id ? '新建' : '编辑'" :close-on-click-modal="false"
      :close-on-press-escape="false" :visible.sync="visible" lock-scroll
      class="JNPF-dialog JNPF-dialog_center" width="630px">
      <el-form ref="dataForm" :model="dataForm" :rules="dataRule" label-width="100px"
        v-loading="formLoading" class="menuForm">
        <jnpf-form-tip-item label="字段名称" prop="enCode">
          <el-input v-model="dataForm.enCode" placeholder="请输入字段名称">
            <template slot="append">
              <el-button type="primary" @click="selectName">选择</el-button>
            </template>
          </el-input>
        </jnpf-form-tip-item>
        <jnpf-form-tip-item label="数据库表" prop="bindTable" v-if="dataForm.bindTable">
          <el-input v-model="dataForm.bindTable" placeholder="请输入数据库表" disabled>
          </el-input>
        </jnpf-form-tip-item>
        <jnpf-form-tip-item label="字段规则" prop="fieldRule">
          <el-select v-model="dataForm.fieldRule" placeholder="请选择字段规则" clearable
            @change="changeFieldRule">
            <el-option v-for="item in fieldRuleOptions" :key="item.value" :label="item.label"
              :value="item.value">
              <template v-if="item.value!=0">
                <span>{{item.label}}<el-tooltip :content="`与主表是一对${item.value==1?'一':'多'}的主从关系`"
                    placement="top-start">
                    <i class="el-icon-question tooltip-question" style="margin-left:5px"></i>
                  </el-tooltip></span>
              </template>
            </el-option>
          </el-select>
        </jnpf-form-tip-item>
        <jnpf-form-tip-item label="关联字段" prop="childTableKey"
          tip-label="输入表单设计内设计子表的控件字段名，例：tableField107" v-if="dataForm.fieldRule==2">
          <el-input v-model="dataForm.childTableKey" placeholder="请输入关联主表的子表控件名称" />
        </jnpf-form-tip-item>
        <jnpf-form-tip-item label="字段说明" prop="fullName">
          <el-input v-model="dataForm.fullName" placeholder="输入字段说明" />
        </jnpf-form-tip-item>
        <jnpf-form-tip-item label="字段类型" prop="type">
          <el-select v-model="dataForm.type" placeholder="请选择" clearable filterable
            @change="typeChange">
            <el-option v-for="item in typeOptions" :key="item.value" :label="item.label"
              :value="item.value">
            </el-option>
          </el-select>
        </jnpf-form-tip-item>
        <jnpf-form-tip-item label="条件内容" prop="conditionText">
          <el-select v-model="dataForm.conditionText" placeholder="请选择" clearable filterable
            @change="conditionTextChange">
            <template v-if="['int','bigint','double'].includes(dataForm.type)">
              <el-option v-for="item in numberOptions" :key="item.value" :label="item.label"
                :value="item.value">
              </el-option>
            </template>
            <template v-else-if="['datetime'].includes(dataForm.type)">
              <el-option v-for="item in dataTimeOptions" :key="item.value" :label="item.label"
                :value="item.value">
              </el-option>
            </template>
            <template v-else-if="['string','varchar'].includes(dataForm.type)">
              <el-option v-for="item in organizeOptions" :key="item.value" :label="item.label"
                :value="item.value">
              </el-option>
            </template>
            <template v-else-if="['text'].includes(dataForm.type)">
              <el-option v-for="item in textOptions" :key="item.value" :label="item.label"
                :value="item.value">
              </el-option>
            </template>
          </el-select>
        </jnpf-form-tip-item>
        <jnpf-form-tip-item label="" prop="format" v-if="dataForm.conditionText==='datePicker'">
          <el-select v-model="dataForm.format" placeholder="请选择" clearable filterable>
            <el-option v-for="item in formatOptions" :key="item.value" :label="item.label"
              :value="item.value">
            </el-option>
          </el-select>
        </jnpf-form-tip-item>
        <jnpf-form-tip-item label="条件符号" prop="conditionSymbol">
          <el-select v-model="conditionSymbol" multiple placeholder="请选择" filterable>
            <template
              v-if="['int','bigint','double','datetime'].includes(dataForm.type) && ['datePicker','inputNumber'].includes(dataForm.conditionText)">
              <el-option v-for="item in symbolOptionsDateNum" :key="item.value" :label="item.label"
                :value="item.value">
              </el-option>
            </template>
            <template
              v-else-if="['string','varchar'].includes(dataForm.type) &&  
              ['organizeSelect','depSelect','posSelect','roleSelect','groupSelect','userSelect','@userAraSubordinates','@organizationAndSuborganization','@branchManageOrganize'].includes(dataForm.conditionText)">
              <el-option v-for="item in symbolOptionsSelect" :key="item.value" :label="item.label"
                :value="item.value">
              </el-option>
            </template>
            <template v-else>
              <el-option v-for="item in symbolOptionsBase" :key="item.value" :label="item.label"
                :value="item.value">
              </el-option>
            </template>
          </el-select>
        </jnpf-form-tip-item>
        <jnpf-form-tip-item label="备注" prop="description">
          <el-input v-model="dataForm.description" type="textarea" :rows="3" />
        </jnpf-form-tip-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="visible = false">{{$t("common.cancelButton")}}</el-button>
        <el-button type="primary" :loading="btnLoading" @click="dataFormSubmit()">
          {{ $t("common.confirmButton") }}</el-button>
      </span>
    </el-dialog>
    <nameSelects :visible.sync="nameVisible" :value="dataForm.enCode" :moduleId="dataForm.moduleId"
      :title="dataForm.enCode" :dataType="dataType" :bindTable="dataForm.bindTable"
      :menuType="menuType" :treeData="treeData" ref="nameForm" @closeForm="closeForm" />
  </div>
</template>
<script>
import {
  createDataAuthorize,
  updateDataAuthorize,
  getDataAuthorizeInfo
} from "@/api/system/dataAuthorize";
import nameSelects from '../NameSelect.vue'
import { getVisualTables } from "@/api/system/authorize"
const textOptions = [{
  value: "input",
  label: "任意文本"
},
{
  value: "@userId",
  label: "当前用户"
},
{
  value: "@organizeId",
  label: "当前组织"
}]
const organizeOptions = [
  ...textOptions,
  {
    value: "organizeSelect",
    label: "组织选择"
  }, {
    value: "depSelect",
    label: "部门选择"
  }, {
    value: "posSelect",
    label: "岗位选择"
  }, {
    value: "roleSelect",
    label: "角色选择"
  }, {
    value: "groupSelect",
    label: "分组选择"
  }, {
    value: "userSelect",
    label: "用户选择"
  },
  {
    value: "@userAraSubordinates",
    label: "当前用户及下属"
  },
  {
    value: "@organizationAndSuborganization",
    label: "当前组织及子组织"
  },
  {
    value: "@branchManageOrganize",
    label: "当前分管组织"
  }
]
const numberOptions = [
  {
    value: "inputNumber",
    label: "数字输入"
  }
]
const dataTimeOptions = [
  {
    value: "datePicker",
    label: "日期选择"
  }
]
const formatOptions = [
  {
    label: 'yyyy',
    value: 'yyyy'
  },
  {
    label: 'yyyy-MM',
    value: 'yyyy-MM'
  },
  {
    label: 'yyyy-MM-dd',
    value: 'yyyy-MM-dd'
  },
  {
    label: 'yyyy-MM-dd HH:mm',
    value: 'yyyy-MM-dd HH:mm'
  },
  {
    label: 'yyyy-MM-dd HH:mm:ss',
    value: 'yyyy-MM-dd HH:mm:ss'
  }
]
export default {
  components: { nameSelects },

  data() {
    return {
      visible: false,
      formLoading: false,
      btnLoading: false,
      typeOptions: [
        {
          value: "string",
          label: "string"
        },
        {
          value: "int",
          label: "int"
        },
        {
          value: "double",
          label: "double"
        },
        {
          value: "varchar",
          label: "varchar"
        },
        {
          value: "datetime",
          label: "datetime"
        },
        {
          value: "text",
          label: "text"
        },
        {
          value: "bigint",
          label: "bigint"
        }
      ],
      fieldRuleOptions: [
        { value: 0, label: "主表规则" },
        { value: 1, label: "副表规则" },
        { value: 2, label: "子表规则" }
      ],
      treeData: [],
      textOptions,
      organizeOptions,
      dataTimeOptions,
      numberOptions,
      formatOptions,
      symbolOptionsBase: [
        {
          label: "等于",
          value: "=="
        },
        {
          label: "不等于",
          value: "<>"
        },
        {
          label: "包含",
          value: "like"
        },
        {
          label: "不包含",
          value: "notLike"
        },
        {
          label: "为空",
          value: "null"
        },
        {
          label: "不为空",
          value: "notNull"
        },
      ],
      symbolOptionsDateNum: [
        {
          label: "等于",
          value: "=="
        },
        {
          label: "不等于",
          value: "<>"
        },
        {
          label: "大于",
          value: ">"
        },
        {
          label: "小于",
          value: "<"
        },
        {
          label: "介于",
          value: "between"
        },
        {
          label: "大于等于",
          value: ">="
        },
        {
          label: "小于等于",
          value: "<="
        },
        {
          label: "为空",
          value: "null"
        },
        {
          label: "不为空",
          value: "notNull"
        },
      ],
      symbolOptionsSelect: [
        {
          label: "等于",
          value: "=="
        },
        {
          label: "不等于",
          value: "<>"
        },
        {
          label: "包含任意一个",
          value: "in"
        },
        {
          label: "不包含任意一个",
          value: "notIn"
        },
        {
          label: "为空",
          value: "null"
        },
        {
          label: "不为空",
          value: "notNull"
        },
      ],
      symbolOptions: [
        {
          label: "大于等于",
          value: ">="
        },
        {
          label: "大于",
          value: ">"
        },
        {
          label: "等于",
          value: "=="
        },
        {
          label: "小于等于",
          value: "<="
        },
        {
          label: "小于",
          value: "<"
        },
        {
          label: "不等于",
          value: "<>"
        },
        {
          label: "介于",
          value: "between"
        },
        {
          label: "包含任意一个",
          value: "in"
        },
        {
          label: "不包含任意一个",
          value: "notIn"
        },
        {
          label: "包含",
          value: "like"
        },
        {
          label: "不包含",
          value: "notLike"
        },
        {
          label: "为空",
          value: "null"
        },
        {
          label: "不为空",
          value: "notNull"
        },
      ],
      dataForm: {
        id: "",
        moduleId: "",
        fullName: "",
        enCode: "",
        type: "",
        conditionSymbol: "",
        conditionText: "",
        description: "",
        fieldRule: 0,
        childTableKey: '',
        format: 'yyyy-MM-dd HH:mm:ss'
      },
      conditionSymbol: [],
      menuType: 2,
      nameVisible: false,
      dataRule: {
        enCode: [
          { required: true, message: "字段名称不能为空", trigger: "blur" }
        ],
        fieldRule: [
          { required: true, message: "主表规则不能为空", trigger: "blur" }
        ],
        fullName: [
          { required: true, message: "字段说明不能为空", trigger: "blur" }
        ],
        childTableKey: [
          { required: true, message: "关联字段不能为空", trigger: "blur" }
        ],
        type: [{ required: true, message: "请选择字段类型", trigger: "blur" }],
        conditionSymbol: [
          { required: true, message: "请选择条件符号", trigger: "blur" }
        ],
        conditionText: [
          { required: true, message: "请选择条件内容", trigger: "blur" }
        ]
      },
      enCodeOptions: [],
      dataType: ""
    };
  },
  methods: {
    init(moduleId, id, menuType, dataType) {
      this.menuType = menuType;
      this.dataType = dataType
      this.dataForm.id = id || "";
      this.dataForm.moduleId = moduleId;
      this.conditionSymbol = [];
      this.visible = true;
      this.formLoading = true
      this.treeData = []
      getVisualTables(moduleId, dataType).then(res => {
        let data = []
        for (const key in res.data.linkTables) {
          data.push({
            tableName: res.data.linkTables[key],
            dbLink: res.data.linkId
          })
        }
        this.treeData = data
      })
      this.$nextTick(() => {
        this.$refs["dataForm"].resetFields();
        this.dataForm.bindTable = ''
        this.dataForm.conditionText = ''
        this.dataForm.type = ''
        this.dataForm.fieldRule = 0
        // 获取字段数据
        if (this.dataForm.id) {
          getDataAuthorizeInfo(this.dataForm.id).then(res => {
            this.dataForm = res.data;
            this.conditionSymbol = res.data.conditionSymbol
              ? res.data.conditionSymbol.split(",")
              : [];
          });
        }
        this.formLoading = false;
      });
    },
    typeChange() {
      this.dataForm.conditionText = ''
      this.conditionSymbol = ''
    },
    conditionTextChange() {
      this.conditionSymbol = ''
    },
    selectName() {
      this.nameVisible = true
      this.$nextTick(() => {
        this.$refs.nameForm.openDialog();
      });
    },
    changeFieldRule() {
      this.dataForm.childTableKey = ''
    },
    closeForm(val, value) {
      this.dataForm.enCode = val
      if (value.tableName) this.dataForm.bindTable = value.tableName
      if (value.fieldName) this.dataForm.fullName = value.fieldName
    },
    onEnCodeChange(e) {
      let objVal;
      this.dataForm.fullName = "";
      this.dataForm.type = "";
      if (!!e) {
        this.enCodeOptions.forEach(o => {
          if (o.field === e) {
            objVal = {
              fullName: o.fieldName,
              type: o.dataType
            };
            return objVal;
          }
        });
        this.dataForm.fullName = objVal["fullName"];
        this.dataForm.type = objVal["type"];
      }
    },
    dataFormSubmit() {
      this.dataForm.conditionSymbol = ''
      if (this.conditionSymbol) this.dataForm.conditionSymbol = this.conditionSymbol.join();
      this.$refs["dataForm"].validate(valid => {
        if (valid) {
          this.btnLoading = true;
          const formMethod = this.dataForm.id
            ? updateDataAuthorize
            : createDataAuthorize;
          formMethod(this.dataForm)
            .then(res => {
              this.$message({
                message: res.msg,
                type: "success",
                duration: 1500,
                onClose: () => {
                  this.visible = false;
                  this.btnLoading = false;
                  this.$emit("refreshDataList");
                }
              });
            })
            .catch(() => {
              this.btnLoading = false;
            });
        }
      });
    }
  }
};
</script>
