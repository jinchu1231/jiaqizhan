
<template>
  <div class="JNPF-common-layout">
    <div class="JNPF-common-layout-left" v-if="columnData.type === 2">
      <div class="JNPF-common-title" v-if="columnData.treeTitle">
        <h2>{{columnData.treeTitle}}</h2>
        <el-dropdown v-if="columnData.treeSynType==0">
          <el-link icon="icon-ym icon-ym-mpMenu" :underline="false" />
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item @click.native="treeRefresh()">刷新数据</el-dropdown-item>
            <el-dropdown-item @click.native="toggleTreeExpand(true)">展开全部</el-dropdown-item>
            <el-dropdown-item @click.native="toggleTreeExpand(false)">折叠全部</el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </div>
      <div class="JNPF-common-tree-search-box"
        v-if="columnData.hasTreeQuery&&columnData.treeSynType==0">
        <el-input placeholder="输入关键字" v-model="keyword" suffix-icon="el-icon-search" clearable />
      </div>
      <el-tree :data="treeData" :props="treeProps" v-loading='treeLoading'
        :default-expand-all="columnData.treeSynType==0?expandsTree:false" highlight-current
        ref="treeBox" :expand-on-click-node="false" @node-click="handleNodeClick"
        class="JNPF-common-el-tree" :node-key="treeProps.value" :filter-node-method="filterNode"
        :lazy="columnData.treeSynType==1&columnData.treeDataSource==='api'?true:false"
        :load="loadNode" v-if="refreshTree">
        <span class="custom-tree-node" slot-scope="{ node, data }">
          <i :class="data.icon"></i>
          <span class="text">{{node.label}}</span>
        </span>
      </el-tree>
    </div>
    <div class="JNPF-common-layout-center">
      <Search ref="Search" :list="columnData.searchList" @reset="reset" @search="searchData"
        :initDataJson="listQuery.queryJson" />
      <div class="JNPF-common-layout-main JNPF-flex-main">
        <div class="JNPF-common-head">
          <div v-if="isPreview || !columnData.useBtnPermission">
            <span v-for="(item, i) in columnData.btnsList" :key="i" style="margin:0 4px;">
              <el-button :type="i==0?'primary':'text'" :icon="item.icon"
                @click="headBtnsHandel(item.value)">
                {{item.label}}</el-button>
            </span>
          </div>
          <div v-else>
            <span v-for="(item, i) in columnData.btnsList" :key="i" style="margin:0 4px;">
              <el-button :type="i==0?'primary':'text'" :icon="item.icon" v-has="'btn_'+item.value"
                @click="headBtnsHandel(item.value)">
                {{item.label}}</el-button>
            </span>
          </div>
          <div class="JNPF-common-head-right">
            <el-tooltip content="高级查询" placement="top" v-if="columnData.hasSuperQuery">
              <el-link icon="icon-ym icon-ym-filter JNPF-common-head-icon" :underline="false"
                @click="openSuperQuery()" />
            </el-tooltip>
            <template v-if="columnData.type==5&&columnData.treeLazyType == 0">
              <el-tooltip effect="dark" content="展开" placement="top">
                <el-link v-show="!expandsTable" type="text"
                  icon="icon-ym icon-ym-btn-expand JNPF-common-head-icon" :underline="false"
                  @click="toggleExpandList()" />
              </el-tooltip>
              <el-tooltip effect="dark" content="折叠" placement="top">
                <el-link v-show="expandsTable" type="text"
                  icon="icon-ym icon-ym-btn-collapse JNPF-common-head-icon" :underline="false"
                  @click="toggleExpandList()" />
              </el-tooltip>
            </template>
            <el-tooltip effect="dark" :content="$t('common.refresh')" placement="top">
              <el-link icon="icon-ym icon-ym-Refresh JNPF-common-head-icon" :underline="false"
                @click="initData()" />
            </el-tooltip>
          </div>
        </div>
        <JNPF-table v-loading="listLoading" :data="list" row-key="id"
          :default-expand-all="columnData.childTableStyle!==2&&columnData.treeLazyType!=1?expandsTable:false"
          :lazy="columnData.type==5&&columnData.treeLazyType==1"
          :tree-props="{children: 'children', hasChildren: columnData.treeLazyType==1?'hasChildren':''}"
          :load="treeLoad" @sort-change="handleTableSort" :cell-style="cellStyle"
          :header-cell-class-name="handleHeaderClass" :row-style="rowStyle" :has-c="hasBatchBtn"
          @selection-change="handleSelectionChange" v-if="refreshTable" custom-column
          :span-method="arraySpanMethod" ref="tableRef"
          :hasNO="!(columnData.childTableStyle==2&&childColumnList.length&&columnData.type != 3)&&columnData.type != 4"
          :hasNOFixed="columnList.some(o=>o.fixed == 'left')"
          :show-summary='columnData.showSummary && !(columnData.type==3 ||columnData.type==5)'
          :summary-method="getTableSummaries">
          <template v-if="columnData.type === 4">
            <el-table-column width="50" align="center" label="序号"
              :fixed="columnList.some(o=>o.fixed == 'left')">
              <template slot-scope="scope">
                <div class="row-action" v-if="config.enableFlow==0">
                  <span class="index">{{ scope.$index + 1 }}</span>
                  <i class="ym-custom ym-custom-arrow-expand"
                    @click="handleRowForm(scope.$index)"></i>
                </div>
                <div v-else>{{ scope.$index + 1 }}</div>
              </template>
            </el-table-column>
            <template v-for="(item, i) in columnList">
              <el-table-column :prop="item.prop" :label="item.label" :align="item.align"
                :fixed="item.fixed!='none'?item.fixed:false" :width="item.width" :key="i"
                :show-overflow-tooltip='getTooltip(item.jnpfKey)'
                :sortable="item.sortable?'custom':item.sortable" v-if="item.jnpfKey !=='table'">
                <template v-if="item.jnpfKey=='complexHeader'&&item.children&&item.children.length">
                  <template v-for="child in item.children">
                    <el-table-column :prop="child.prop" :label="child.label" :key="child.prop"
                      :width="child.width" :align="child.align"
                      :show-overflow-tooltip='getTooltip(child.jnpfKey)'
                      :sortable="child.sortable?'custom':child.sortable">
                      <template slot-scope="scope">
                        <template v-if="scope.row.rowEdit">
                          <template v-if="child.jnpfKey==='inputNumber'">
                            <JnpfInputNumber v-model="scope.row[child.prop]"
                              :placeholder="child.placeholder" :min="child.min" :max="child.max"
                              :step="child.step" :precision="child.precision"
                              :addonBefore="child.addonBefore" :addonAfter="child.addonAfter"
                              :controls-position="child['controls-position']"
                              :disabled="child.disabled" style="width:100%"
                              :thousands="child.thousands" />
                          </template>
                          <div v-else-if="child.jnpfKey==='switch'" style="padding-top: 5px;">
                            <el-switch v-model="scope.row[child.prop]"
                              :active-value="child['active-value']"
                              :inactive-value="child['inactive-value']"
                              :disabled="child.disabled" />
                          </div>
                          <template v-else-if="child.jnpfKey==='timePicker'">
                            <JnpfTimePicker v-model="scope.row[child.prop]" style="width:100%"
                              :placeholder="child.placeholder" :clearable="child.clearable"
                              :valueFormat="child.format" :format="child.format"
                              :readonly="child.readonly" :disabled="child.disabled"
                              :startTime='child.startTime' :endTime="child.endTime">
                            </JnpfTimePicker>
                          </template>
                          <template v-else-if="child.jnpfKey==='datePicker'">
                            <JnpfDatePicker v-model="scope.row[child.prop]"
                              :clearable="child.clearable" :placeholder="child.placeholder"
                              :format="child.format" style="width:100%" :readonly="child.readonly"
                              :disabled="child.disabled" :startTime='child.startTime'
                              :endTime="child.endTime">
                            </JnpfDatePicker>
                          </template>
                          <template v-else-if="['organizeSelect'].includes(child.jnpfKey)">
                            <JnpfOrganizeSelect v-model="scope.row[child.prop]"
                              :placeholder="child.placeholder" :multiple="child.multiple"
                              :clearable="child.clearable" :disabled="child.disabled"
                              :selectType="child.selectType" :ableIds="child.ableIds" />
                          </template>
                          <template v-else-if="['depSelect'].includes(child.jnpfKey)">
                            <JnpfDepSelect v-model="scope.row[child.prop]"
                              :placeholder="child.placeholder" :selectType="child.selectType"
                              :ableDepIds="child.ableDepIds" :multiple="child.multiple"
                              :clearable="child.clearable" :disabled="child.disabled"
                              :ableIds="child.ableIds" />
                          </template>
                          <template v-else-if="['userSelect'].includes(child.jnpfKey)">
                            <JnpfUserSelect v-model="scope.row[child.prop]"
                              :placeholder="child.placeholder" :selectType="child.selectType"
                              :ableIds="child.ableIds" :multiple="child.multiple"
                              :clearable="child.clearable" :disabled="child.disabled" />
                          </template>
                          <template v-else-if="['usersSelect'].includes(child.jnpfKey)">
                            <JnpfUsersSelect v-model="scope.row[child.prop]"
                              :placeholder="child.placeholder" :selectType="child.selectType"
                              :ableIds="child.ableIds" :multiple="child.multiple"
                              :clearable="child.clearable" :disabled="child.disabled" />
                          </template>
                          <template v-else-if="['posSelect'].includes(child.jnpfKey)">
                            <JnpfPosSelect v-model="scope.row[child.prop]"
                              :placeholder="child.placeholder" :selectType="child.selectType"
                              :ableIds="child.ableIds" :multiple="child.multiple"
                              :clearable="child.clearable" :disabled="child.disabled" />
                          </template>
                          <template v-else-if="child.jnpfKey==='groupSelect'">
                            <JnpfGroupSelect v-model="scope.row[child.prop]"
                              :ableIds="child.ableIds" :placeholder="child.placeholder"
                              :multiple="child.multiple" :selectType="child.selectType"
                              :clearable="child.clearable" :disabled="child.disabled" />
                          </template>
                          <template v-else-if="child.jnpfKey==='roleSelect'">
                            <JnpfRoleSelect v-model="scope.row[child.prop]" :ableIds="child.ableIds"
                              :placeholder="child.placeholder" :multiple="child.multiple"
                              :selectType="child.selectType" :clearable="child.clearable"
                              :disabled="child.disabled" />
                          </template>
                          <template v-else-if="child.jnpfKey==='areaSelect'">
                            <JnpfAreaSelect v-model="scope.row[child.prop]" :level="child.level"
                              :placeholder="child.placeholder" :multiple="child.multiple"
                              :clearable="child.clearable" :disabled="child.disabled" />
                          </template>
                          <template
                            v-else-if="['select','radio','checkbox'].includes(child.jnpfKey)">
                            <el-select v-model="scope.row[child.prop]"
                              :placeholder="child.placeholder" :filterable="child.filterable"
                              :multiple="child.multiple||child.jnpfKey==='checkbox'"
                              :clearable="child.clearable" :disabled="child.disabled">
                              <el-option :label="ochild[child.props.label]"
                                v-for="(ochild, i) in child.options"
                                :value="ochild[child.props.value]" :key="i"></el-option>
                            </el-select>
                          </template>
                          <template v-else-if="child.jnpfKey==='cascader'">
                            <el-cascader v-model="scope.row[child.prop]" :options="child.options"
                              :clearable="child.clearable" :show-all-levels="child.showAllLevels"
                              :props="child.props" :filterable="child.filterable"
                              :separator="child.separator" :placeholder="child.placeholder"
                              :disabled="child.disabled" style="width:100%">
                            </el-cascader>
                          </template>
                          <template v-else-if="child.jnpfKey==='treeSelect'">
                            <JnpfTreeSelect v-model="scope.row[child.prop]" :options="child.options"
                              :props="child.props" :placeholder="child.placeholder"
                              :multiple="child.multiple" :clearable="child.clearable"
                              :disabled="child.disabled" />
                          </template>
                          <template v-else-if="child.jnpfKey==='relationForm'">
                            <JnpfRelationForm v-model="scope.row[child.prop]"
                              :placeholder="child.placeholder" :modelId="child.modelId"
                              :columnOptions="child.columnOptions"
                              :relationField="child.relationField" :hasPage="child.hasPage"
                              :pageSize="child.pageSize" :clearable="child.clearable"
                              :disabled="child.disabled" />
                          </template>
                          <template v-else-if="child.jnpfKey==='popupSelect'">
                            <JnpfPopupSelect v-model="scope.row[child.prop]"
                              :placeholder="child.placeholder" :interfaceId="child.interfaceId"
                              :columnOptions="child.columnOptions" :propsValue="child.propsValue"
                              :relationField="child.relationField" :hasPage="child.hasPage"
                              :pageSize="child.pageSize" :popupType="child.popupType"
                              :templateJson="item.templateJson" :popupTitle="child.popupTitle"
                              :popupWidth="child.popupWidth" :clearable="child.clearable"
                              :disabled="child.disabled" />
                          </template>
                          <template v-else-if="child.jnpfKey==='autoComplete'">
                            <JnpfAutoComplete v-model="scope.row[child.prop]"
                              :placeholder="child.placeholder" :interfaceId="child.interfaceId"
                              :relationField="child.relationField" :total="child.total"
                              :clearable="child.clearable" :templateJson="child.templateJson"
                              :disabled="child.disabled" />
                          </template>
                          <template v-else-if="child.jnpfKey==='popupTableSelect'">
                            <JnpfPopupTableSelect v-model="scope.row[child.prop]"
                              :placeholder="child.placeholder" :interfaceId="child.interfaceId"
                              :multiple="child.multiple" :columnOptions="child.columnOptions"
                              :propsValue="child.propsValue" :relationField="child.relationField"
                              :hasPage="child.hasPage" :pageSize="child.pageSize"
                              :popupType="child.popupType" :popupTitle="child.popupTitle"
                              :popupWidth="child.popupWidth" :filterable="child.filterable"
                              :disabled="child.disabled" clearable
                              :templateJson="item.templateJson" />
                          </template>
                          <template v-else-if="child.jnpfKey==='sign'">
                            <JnpfSign v-model="scope.row[child.prop]" :disabled="child.disabled" />
                          </template>
                          <template v-else-if="child.jnpfKey==='location'">
                            <JnpfLocation v-model="scope.row[child.prop]" :disabled="child.disabled"
                              :autoLocation='child.autoLocation'
                              :adjustmentScope='child.adjustmentScope'
                              :enableDesktopLocation='child.enableDesktopLocation'
                              :locationScope='child.locationScope'
                              :enableLocationScope='child.enableLocationScope'
                              :clearable='child.clearable' />
                          </template>
                          <template v-else-if="child.jnpfKey==='rate'">
                            <JnpfRate v-model="scope.row[child.prop]" :disabled="child.disabled"
                              :count="child.count" />
                          </template>
                          <template v-else-if="child.jnpfKey==='slider'">
                            <JnpfSlider v-model="scope.row[child.prop]"
                              :disabled="child.disabled" />
                          </template>
                          <template v-else-if="child.jnpfKey==='uploadFile'">
                            <JnpfUploadFile v-model="scope.row[child.prop]"
                              :disabled="child.disabled" simple
                              v-if="scope.row[child.prop] && scope.row[child.prop].length" />
                          </template>
                          <template v-else-if="child.jnpfKey==='uploadImg'">
                            <JnpfUploadImg v-model="scope.row[child.prop]" disabled
                              v-if="scope.row[child.prop] && scope.row[child.prop].length" simple />
                          </template>
                          <template v-else-if="['input','textarea'].includes(child.jnpfKey)">
                            <el-input v-model="scope.row[child.prop]"
                              :placeholder="child.placeholder" :readonly="child.readonly"
                              :prefix-icon="child['prefix-icon']"
                              :suffix-icon="child['suffix-icon']" :clearable="child.clearable"
                              :show-word-limit="child['show-word-limit']"
                              :maxlength="child.maxlength" :show-password="child['show-password']"
                              :disabled="child.disabled">
                              <template slot="prepend"
                                v-if="child.__slot__ && child.__slot__.prepend">{{child.__slot__.prepend}}</template>
                              <template slot="append"
                                v-if="child.__slot__ && child.__slot__.append">{{child.__slot__.append}}</template>
                            </el-input>
                          </template>
                          <template v-else-if="systemComponentsList.includes(child.jnpfKey)">
                            {{scope.row[child.prop+'_name']||scope.row[child.prop]}}
                          </template>
                          <template v-else>
                            {{scope.row[child.prop]}}
                          </template>
                        </template>
                        <template v-else>
                          <template v-if="['relationForm'].includes(child.jnpfKey)">
                            <el-link :underline="false"
                              @click.native="toDetail(child.modelId,scope.row[`${child.prop}`])"
                              type="primary">
                              {{scope.row[child.prop+'_name']||scope.row[child.prop]}}</el-link>
                          </template>
                          <template v-else-if="['inputNumber','calculate'].includes(child.jnpfKey)">
                            <template v-if="scope.row[child.prop+'_name']">
                              <JnpfNumber v-model="scope.row[child.prop+'_name']"
                                :thousands="child.thousands" :precision="child.precision">
                              </JnpfNumber>
                            </template>
                            <template v-else>
                              <JnpfNumber v-model="scope.row[child.prop]"
                                :thousands="child.thousands" :precision="child.precision">
                              </JnpfNumber>
                            </template>
                          </template>
                          <template v-else-if="['input'].includes(child.jnpfKey)">
                            <template v-if="scope.row[child.prop+'_name']">
                              <jnpf-input v-model="scope.row[child.prop+'_name']"
                                :addonBefore="child.addonBefore" :addonAfter="child.addonAfter"
                                :showOverflow='getTooltip(child.jnpfKey)' :useMask="child.useMask"
                                :maskConfig="child.maskConfig" detailed />
                            </template>
                            <template v-else>
                              <jnpf-input v-model="scope.row[child.prop]"
                                :addonBefore="child.addonBefore" :addonAfter="child.addonAfter"
                                :showOverflow='getTooltip(child.jnpfKey)' :useMask="child.useMask"
                                :maskConfig="child.maskConfig" detailed />
                            </template>
                          </template>
                          <template v-else-if="['sign'].includes(child.jnpfKey)">
                            <template v-if="scope.row[child.prop+'_name']">
                              <JnpfSign v-model="scope.row[child.prop+'_name']" detailed />
                            </template>
                            <template v-else>
                              <JnpfSign v-model="scope.row[child.prop]" detailed />
                            </template>
                          </template>
                          <template
                            v-else-if="child.jnpfKey=='location'&&!!columnData.showOverflow">
                            <template v-if="scope.row[child.prop+'_name']">
                              <el-tooltip effect="dark" :content="scope.row[child.prop+'_name']"
                                placement="top">
                                <div class="showOverflow">{{scope.row[child.prop+'_name']}}</div>
                              </el-tooltip>
                            </template>
                            <template v-else>
                              <el-tooltip effect="dark" :content="scope.row[child.prop]"
                                placement="top">
                                <div class="showOverflow">{{scope.row[child.prop]}}</div>
                              </el-tooltip>
                            </template>
                          </template>
                          <template v-else-if="child.jnpfKey === 'rate'">
                            <template v-if="scope.row[child.prop+'_name']">
                              <JnpfRate v-model="scope.row[child.prop+'_name']" disabled
                                :count="child.count" />
                            </template>
                            <template v-else>
                              <JnpfRate v-model="scope.row[child.prop]" disabled
                                :count="child.count" />
                            </template>
                          </template>
                          <template v-else-if="child.jnpfKey === 'slider'">
                            <template v-if="scope.row[child.prop+'_name']">
                              <JnpfSlider v-model="scope.row[child.prop+'_name']" disabled />
                            </template>
                            <template v-else>
                              <JnpfSlider v-model="scope.row[child.prop]" disabled />
                            </template>
                          </template>
                          <template v-else-if="child.jnpfKey === 'uploadFile'">
                            <template v-if="scope.row[child.prop+'_name']">
                              <JnpfUploadFile v-model="scope.row[child.prop+'_name']" disabled
                                simple
                                v-if="scope.row[child.prop+'_name'] && scope.row[child.prop+'_name'].length" />
                            </template>
                            <template v-else>
                              <JnpfUploadFile v-model="scope.row[child.prop]" disabled simple
                                v-if="scope.row[child.prop] && scope.row[child.prop].length" />
                            </template>
                          </template>
                          <template v-else-if="child.jnpfKey === 'uploadImg'">
                            <template v-if="scope.row[child.prop+'_name']">
                              <JnpfUploadImg v-model="scope.row[child.prop+'_name']" disabled simple
                                v-if="scope.row[child.prop+'_name'] && scope.row[child.prop+'_name'].length" />
                            </template>
                            <template v-else>
                              <JnpfUploadImg v-model="scope.row[child.prop]" disabled simple
                                v-if="scope.row[child.prop] && scope.row[child.prop].length" />
                            </template>
                          </template>
                          <template v-else>
                            {{scope.row[child.prop+'_name']||scope.row[child.prop]}}
                          </template>
                        </template>
                      </template>
                    </el-table-column>
                  </template>
                </template>
                <template slot-scope="scope">
                  <template v-if="scope.row.rowEdit">
                    <template v-if="item.jnpfKey==='inputNumber'">
                      <JnpfInputNumber v-model="scope.row[item.prop]"
                        :placeholder="item.placeholder" :min="item.min" :max="item.max"
                        :step="item.step" :precision="item.precision"
                        :addonBefore="item.addonBefore" :addonAfter="item.addonAfter"
                        :controls-position="item['controls-position']" :disabled="item.disabled"
                        style="width:100%" :thousands="item.thousands" />
                    </template>
                    <div v-else-if="item.jnpfKey==='switch'" style="padding-top: 5px;">
                      <el-switch v-model="scope.row[item.prop]" :activeValue="item.activeValue"
                        :inactiveValue="item.inactiveValue" :disabled="item.disabled" />
                    </div>
                    <template v-else-if="item.jnpfKey==='timePicker'">
                      <JnpfTimePicker v-model="scope.row[item.prop]" style="width:100%"
                        :placeholder="item.placeholder" :clearable="item.clearable"
                        :valueFormat="item.format" :format="item.format" :readonly="item.readonly"
                        :disabled="item.disabled" :startTime='item.startTime'
                        :endTime="item.endTime">
                      </JnpfTimePicker>
                    </template>
                    <template v-else-if="item.jnpfKey==='datePicker'">
                      <JnpfDatePicker v-model="scope.row[item.prop]" :clearable="item.clearable"
                        :placeholder="item.placeholder" :format="item.format" style="width:100%"
                        :readonly="item.readonly" :disabled="item.disabled"
                        :startTime='item.startTime' :endTime="item.endTime">
                      </JnpfDatePicker>
                    </template>
                    <template v-else-if="['organizeSelect'].includes(item.jnpfKey)">
                      <JnpfOrganizeSelect v-model="scope.row[item.prop]"
                        :placeholder="item.placeholder" :multiple="item.multiple"
                        :clearable="item.clearable" :disabled="item.disabled" />
                    </template>
                    <template v-else-if="['depSelect'].includes(item.jnpfKey)">
                      <JnpfDepSelect v-model="scope.row[item.prop]" :placeholder="item.placeholder"
                        :selectType="item.selectType" :ableDepIds="item.ableDepIds"
                        :multiple="item.multiple" :clearable="item.clearable"
                        :disabled="item.disabled" />
                    </template>
                    <template v-else-if="['userSelect'].includes(item.jnpfKey)">
                      <JnpfUserSelect v-model="scope.row[item.prop]" :placeholder="item.placeholder"
                        :selectType="item.selectType" :ableDepIds="item.ableDepIds"
                        :ablePosIds="item.ablePosIds" :ableUserIds="item.ableUserIds"
                        :ableRoleIds="item.ableRoleIds" :ableGroupIds="item.ableGroupIds"
                        :multiple="item.multiple" :clearable="item.clearable"
                        :disabled="item.disabled" />
                    </template>
                    <template v-else-if="['usersSelect'].includes(item.jnpfKey)">
                      <JnpfUsersSelect v-model="scope.row[item.prop]"
                        :placeholder="item.placeholder" :selectType="item.selectType"
                        :ableIds="item.ableIds" :multiple="item.multiple"
                        :clearable="item.clearable" :disabled="item.disabled" />
                    </template>
                    <template v-else-if="['posSelect'].includes(item.jnpfKey)">
                      <JnpfPosSelect v-model="scope.row[item.prop]" :placeholder="item.placeholder"
                        :selectType="item.selectType" :ableDepIds="item.ableDepIds"
                        :ablePosIds="item.ablePosIds" :multiple="item.multiple"
                        :clearable="item.clearable" :disabled="item.disabled" />
                    </template>
                    <template v-else-if="item.jnpfKey==='groupSelect'">
                      <JnpfGroupSelect v-model="scope.row[item.prop]"
                        :placeholder="item.placeholder" :multiple="item.multiple"
                        :clearable="item.clearable" :disabled="item.disabled" />
                    </template>
                    <template v-else-if="item.jnpfKey==='roleSelect'">
                      <JnpfRoleSelect v-model="scope.row[item.prop]" :placeholder="item.placeholder"
                        :multiple="item.multiple" :clearable="item.clearable"
                        :disabled="item.disabled" />
                    </template>
                    <template v-else-if="item.jnpfKey==='areaSelect'">
                      <JnpfAreaSelect v-model="scope.row[item.prop]" :level="item.level"
                        :placeholder="item.placeholder" :multiple="item.multiple"
                        :clearable="item.clearable" :disabled="item.disabled" />
                    </template>
                    <template v-else-if="['select','radio','checkbox'].includes(item.jnpfKey)">
                      <el-select v-model="scope.row[item.prop]" :placeholder="item.placeholder"
                        :filterable="item.filterable"
                        :multiple="item.multiple||item.jnpfKey==='checkbox'"
                        :clearable="item.clearable" :disabled="item.disabled">
                        <el-option :label="oItem[item.props.label]"
                          v-for="(oItem, i) in item.options" :value="oItem[item.props.value]"
                          :key="i"></el-option>
                      </el-select>
                    </template>
                    <template v-else-if="item.jnpfKey==='cascader'">
                      <JnpfCascader v-model="scope.row[item.prop]" :options="item.options"
                        :clearable="item.clearable" :show-all-levels="item.showAllLevels"
                        :props="item.props" :filterable="item.filterable"
                        :separator="item.separator" :placeholder="item.placeholder"
                        :disabled="item.disabled" style="width:100%" :multiple="item.multiple">
                      </JnpfCascader>
                    </template>
                    <template v-else-if="item.jnpfKey==='treeSelect'">
                      <JnpfTreeSelect v-model="scope.row[item.prop]" :options="item.options"
                        :props="item.props" :placeholder="item.placeholder"
                        :multiple="item.multiple" :clearable="item.clearable"
                        :disabled="item.disabled" />
                    </template>
                    <template v-else-if="item.jnpfKey==='relationForm'">
                      <JnpfRelationForm v-model="scope.row[item.prop]"
                        :placeholder="item.placeholder" :modelId="item.modelId"
                        :columnOptions="item.columnOptions" :relationField="item.relationField"
                        :hasPage="item.hasPage" :pageSize="item.pageSize"
                        :clearable="item.clearable" :disabled="item.disabled" />
                    </template>
                    <template v-else-if="item.jnpfKey==='popupSelect'">
                      <JnpfPopupSelect v-model="scope.row[item.prop]"
                        :placeholder="item.placeholder" :interfaceId="item.interfaceId"
                        :columnOptions="item.columnOptions" :propsValue="item.propsValue"
                        :relationField="item.relationField" :hasPage="item.hasPage"
                        :pageSize="item.pageSize" :popupType="item.popupType"
                        :templateJson="item.templateJson" :popupTitle="item.popupTitle"
                        :popupWidth="item.popupWidth" :clearable="item.clearable"
                        :disabled="item.disabled" />
                    </template>
                    <template v-else-if="item.jnpfKey==='autoComplete'">
                      <JnpfAutoComplete v-model="scope.row[item.prop]"
                        :placeholder="item.placeholder" :interfaceId="item.interfaceId"
                        :relationField="item.relationField" :total="item.total"
                        :clearable="item.clearable" :templateJson="item.templateJson"
                        :disabled="item.disabled" />
                    </template>
                    <template v-else-if="item.jnpfKey==='popupTableSelect'">
                      <JnpfPopupTableSelect v-model="scope.row[item.prop]"
                        :placeholder="item.placeholder" :interfaceId="item.interfaceId"
                        :columnOptions="item.columnOptions" :propsValue="item.propsValue"
                        :relationField="item.relationField" :hasPage="item.hasPage"
                        :pageSize="item.pageSize" :popupType="item.popupType"
                        :popupTitle="item.popupTitle" :popupWidth="item.popupWidth"
                        :filterable="item.filterable" :multiple="item.multiple"
                        :clearable="item.clearable" :disabled="item.disabled"
                        :templateJson="item.templateJson" />
                    </template>
                    <template v-else-if="item.jnpfKey==='sign'">
                      <JnpfSign v-model="scope.row[item.prop]" :disabled="item.disabled" />
                    </template>
                    <template v-else-if="item.jnpfKey==='rate'">
                      <JnpfRate v-model="scope.row[item.prop]" :disabled="item.disabled"
                        :count="item.count" />
                    </template>
                    <template v-else-if="item.jnpfKey==='slider'">
                      <JnpfSlider v-model="scope.row[item.prop]" :disabled="item.disabled"
                        :min="item.min" :max="item.max" :step="item.step" />
                    </template>
                    <template v-else-if="item.jnpfKey==='location'">
                      <JnpfLocation v-model="scope.row[item.prop]" :disabled="item.disabled"
                        :autoLocation='item.autoLocation' :adjustmentScope='item.adjustmentScope'
                        :enableDesktopLocation='item.enableDesktopLocation'
                        :locationScope='item.locationScope'
                        :enableLocationScope='item.enableLocationScope'
                        :clearable='item.clearable' />
                    </template>
                    <template v-else-if="item.jnpfKey==='uploadFile'">
                      <JnpfUploadFile v-model="scope.row[item.prop]" :disabled="item.disabled"
                        simple :limit="item.limit" />
                    </template>
                    <template v-else-if="item.jnpfKey==='uploadImg'">
                      <JnpfUploadImg v-model="scope.row[item.prop]" :disabled="item.disabled"
                        :sizeUnit="item.sizeUnit" :limit="item.limit" :fileSize="item.fileSize"
                        :isAccount="item.isAccount" :folder="item.folder" :tipText="item.tipText"
                        :accept="item.accept" />
                    </template>
                    <template v-else-if="['input','textarea'].includes(item.jnpfKey)">
                      <JnpfInput v-model="scope.row[item.prop]" :placeholder="item.placeholder"
                        :readonly="item.readonly" :prefixIcon="item.prefixIcon"
                        :suffixIcon="item.suffixIcon" :clearable="item.clearable"
                        :showWordLimit="item.showWordLimit" :maxlength="item.maxlength"
                        :showPassword="item.showPassword" :addonBefore="item.addonBefore"
                        :addonAfter="item.addonAfter" :disabled="item.disabled">
                      </JnpfInput>
                    </template>
                    <template v-else-if="systemComponentsList.includes(item.jnpfKey)">
                      {{scope.row[item.prop+'_name']||scope.row[item.prop]}}
                    </template>
                    <template v-else>
                      {{scope.row[item.prop]}}
                    </template>
                  </template>
                  <template v-else>
                    <template v-if="['relationForm'].includes(item.jnpfKey)">
                      <el-link :underline="false"
                        @click.native="toDetail(item.modelId,scope.row[`${item.prop}`])"
                        type="primary">
                        {{scope.row[item.prop+'_name']||scope.row[item.prop]}}</el-link>
                    </template>
                    <template v-else-if="['inputNumber','calculate'].includes(item.jnpfKey)">
                      <template v-if="scope.row[item.prop+'_name']">
                        <JnpfNumber v-model="scope.row[item.prop+'_name']"
                          :thousands="item.thousands" :precision="item.precision">
                        </JnpfNumber>
                      </template>
                      <template v-else>
                        <JnpfNumber v-model="scope.row[item.prop]" :thousands="item.thousands"
                          :precision="item.precision">
                        </JnpfNumber>
                      </template>
                    </template>
                    <template v-else-if="item.jnpfKey=='input'">
                      <template v-if="scope.row[item.prop+'_name']">
                        <jnpf-input v-model="scope.row[item.prop+'_name']"
                          :addonBefore="item.addonBefore" :addonAfter="item.addonAfter"
                          :showOverflow='getTooltip(item.jnpfKey)' :useMask="item.useMask"
                          :maskConfig="item.maskConfig" detailed />
                      </template>
                      <template v-else>
                        <jnpf-input v-model="scope.row[item.prop]" :addonBefore="item.addonBefore"
                          :addonAfter="item.addonAfter" :useMask="item.useMask"
                          :showOverflow='getTooltip(item.jnpfKey)' :maskConfig="item.maskConfig"
                          detailed />
                      </template>
                    </template>
                    <template v-else-if="item.jnpfKey=='sign'">
                      <template v-if="scope.row[item.prop+'_name']">
                        <JnpfSign v-model="scope.row[item.prop+'_name']" detailed />
                      </template>
                      <template v-else>
                        <JnpfSign v-model="scope.row[item.prop]" detailed />
                      </template>
                    </template>
                    <template v-else-if="item.jnpfKey=='location'&&!!columnData.showOverflow">
                      <template v-if="scope.row[item.prop+'_name']">
                        <el-tooltip effect="dark" :content="scope.row[item.prop+'_name']"
                          placement="top">
                          <div class="showOverflow">{{scope.row[item.prop+'_name']}}</div>
                        </el-tooltip>
                      </template>
                      <template v-else>
                        <el-tooltip effect="dark" :content="scope.row[item.prop]" placement="top">
                          <div class="showOverflow">{{scope.row[item.prop]}}</div>
                        </el-tooltip>
                      </template>
                    </template>
                    <template v-else-if="item.jnpfKey=='rate'">
                      <template v-if="scope.row[item.prop+'_name']">
                        <JnpfRate v-model="scope.row[item.prop+'_name']" disabled
                          :count="item.count" />
                      </template>
                      <template v-else>
                        <JnpfRate v-model="scope.row[item.prop]" disabled :count="item.count" />
                      </template>
                    </template>
                    <template v-else-if="item.jnpfKey=='slider'">
                      <template v-if="scope.row[item.prop+'_name']">
                        <JnpfSlider v-model="scope.row[item.prop+'_name']" disabled />
                      </template>
                      <template v-else>
                        <JnpfSlider v-model="scope.row[item.prop]" disabled />
                      </template>
                    </template>
                    <template v-else-if="item.jnpfKey=='uploadFile'">
                      <template v-if="scope.row[item.prop+'_name']">
                        <JnpfUploadFile v-model="scope.row[item.prop+'_name']" disabled simple
                          detailed
                          v-if="scope.row[item.prop+'_name'] && scope.row[item.prop+'_name'].length" />
                      </template>
                      <template v-else>
                        <JnpfUploadFile v-model="scope.row[item.prop]" disabled simple detailed
                          v-if="scope.row[item.prop] && scope.row[item.prop].length" />
                      </template>
                    </template>
                    <template v-else-if="item.jnpfKey=='uploadImg'">
                      <template v-if="scope.row[item.prop+'_name']">
                        <JnpfUploadImg v-model="scope.row[item.prop+'_name']" disabled simple
                          detailed
                          v-if="scope.row[item.prop+'_name'] && scope.row[item.prop+'_name'].length" />
                      </template>
                      <template v-else>
                        <JnpfUploadImg v-model="scope.row[item.prop]" disabled simple detailed
                          v-if="scope.row[item.prop] && scope.row[item.prop].length" />
                      </template>
                    </template>
                    <template v-else>
                      {{scope.row[item.prop+'_name']||scope.row[item.prop]}}
                    </template>
                  </template>
                </template>
              </el-table-column>
            </template>
          </template>
          <template v-else>
            <template
              v-if="columnData.childTableStyle==2&&childColumnList.length&&(columnData.type ==1||columnData.type == 2)">
              <el-table-column width="0" />
              <el-table-column type="expand" width="40">
                <template slot-scope="scope">
                  <el-tabs>
                    <el-tab-pane :label="child.label" v-for="(child,cIndex) in childColumnList"
                      :key="cIndex">
                      <el-table :data="scope.row[child.prop]" size='mini'>
                        <el-table-column :prop="childTable.vModel" :label="childTable.childLabel"
                          :align="childTable.align" :width="childTable.width"
                          :show-overflow-tooltip='!!columnData.showOverflow'
                          v-for="(childTable,iii) in child.children" :key="iii">
                          <template slot-scope="scope">
                            <template v-if="childTable.jnpfKey==='relationForm'">
                              <el-link :underline="false"
                                @click.native="toDetail(childTable.modelId,scope.row[`${childTable.vModel}_id`])"
                                type="primary">
                                {{ scope.row[childTable.vModel]}}
                              </el-link>
                            </template>
                            <template
                              v-else-if="childTable.jnpfKey==='inputNumber' ||childTable.jnpfKey==='calculate'">
                              <JnpfNumber v-model="scope.row[childTable.vModel]"
                                :thousands="childTable.thousands" :precision="childTable.precision">
                              </JnpfNumber>
                            </template>
                            <template v-else-if="childTable.jnpfKey==='input'">
                              <jnpf-input v-model="scope.row[childTable.vModel]"
                                :showOverflow='getTooltip(childTable.jnpfKey)'
                                :addonBefore="childTable.addonBefore" :addonAfter="childTable.addonAfter"
                                :useMask="childTable.useMask" :maskConfig="childTable.maskConfig" detailed />
                            </template>
                            <template v-else-if="childTable.jnpfKey==='sign'">
                              <JnpfSign v-model="scope.row[childTable.vModel]" detailed />
                            </template>
                            <template v-else-if="childTable.jnpfKey==='rate'">
                              <JnpfRate v-model="scope.row[childTable.vModel]" disabled
                                :count="childTable.count" />
                            </template>
                            <template v-else-if="childTable.jnpfKey==='slider'">
                              <JnpfSlider v-model="scope.row[childTable.vModel]" disabled />
                            </template>
                            <template v-else-if="childTable.jnpfKey==='uploadFile'">
                              <JnpfUploadFile v-model="scope.row[childTable.vModel]" detailed
                                disabled
                                v-if="scope.row[childTable.vModel] && scope.row[childTable.vModel].length"
                                simple />
                            </template>
                            <template v-else-if="childTable.jnpfKey==='uploadImg'">
                              <JnpfUploadImg v-model="scope.row[childTable.vModel]" detailed
                                disabled
                                v-if="scope.row[childTable.vModel] && scope.row[childTable.vModel].length"
                                simple />
                            </template>
                            <template v-else>
                              {{ scope.row[childTable.vModel]}}
                            </template>
                          </template>
                        </el-table-column>
                      </el-table>
                    </el-tab-pane>
                  </el-tabs>
                </template>
              </el-table-column>
              <el-table-column type="index" width="50" label="序号" align="center" />
            </template>
            <template v-for="(item, i) in columnList">
              <template v-if="item.jnpfKey==='table'">
                <el-table-column :prop="item.prop" :label="item.label" :align="item.align" :key="i"
                  v-if="columnData.childTableStyle!=2||columnData.type == 3">
                  <el-table-column :prop="child.prop" :label="child.childLabel" :align="child.align"
                    :width="child.width" :key="ii"
                    :sortable="child.sortable?'custom':child.sortable"
                    v-for="(child, ii) in item.children" class-name="child-table-box">
                    <template slot-scope="scope">
                      <child-table-column :data="scope.row[item.prop]" :head="item.children"
                        @toggleExpand="toggleExpand(scope.row,`${item.prop}Expand`)"
                        :showOverflow='!!columnData.showOverflow' @toDetail="toDetail"
                        :expand="scope.row[`${item.prop}Expand`]" v-if="!ii" />
                    </template>
                  </el-table-column>
                </el-table-column>
              </template>
              <template v-else-if="item.jnpfKey==='relationForm'">
                <el-table-column :prop="item.prop" :label="item.label" :align="item.align"
                  :fixed="getFixed(item, i)" :width="item.width" :key="i"
                  :show-overflow-tooltip='!!columnData.showOverflow'
                  :sortable="item.sortable?'custom':item.sortable">
                  <template slot-scope="scope">
                    <el-link :underline="false"
                      @click.native="toDetail(item.modelId,scope.row[`${item.prop}_id`])"
                      type="primary">
                      {{ scope.row[item.prop] }}</el-link>
                  </template>
                </el-table-column>
              </template>
              <template v-else-if="item.jnpfKey==='inputNumber' ||item.jnpfKey==='calculate'">
                <el-table-column :prop="item.prop" :label="item.label" :align="item.align"
                  :fixed="getFixed(item, i)" :width="item.width" :key="i"
                  :show-overflow-tooltip='!!columnData.showOverflow'
                  :sortable="item.sortable?'custom':item.sortable">
                  <template slot-scope="scope">
                    <template v-if="scope.row[item.id]">
                      <JnpfNumber v-model="scope.row[item.id]" :thousands="item.thousands"
                        :precision="item.precision">
                      </JnpfNumber>
                    </template>
                  </template>
                </el-table-column>
              </template>
              <template v-else-if="item.jnpfKey==='input'">
                <el-table-column :prop="item.prop" :label="item.label" :align="item.align"
                  :fixed="getFixed(item, i)" :width="item.width" :key="i"
                  :show-overflow-tooltip='!!columnData.showOverflow'
                  :sortable="item.sortable?'custom':item.sortable">
                  <template slot-scope="scope">
                    <template v-if="scope.row[item.id] &&!scope.row.top">
                      <jnpf-input v-model="scope.row[item.id]" :addonBefore="item.addonBefore"
                        :addonAfter="item.addonAfter" :useMask="item.useMask"
                        :maskConfig="item.maskConfig" detailed
                        :showOverflow='getTooltip(item.jnpfKey)' />
                    </template>
                    <template v-else>{{scope.row[item.id]}}</template>
                  </template>
                </el-table-column>
              </template>
              <template v-else-if="item.jnpfKey==='sign'">
                <el-table-column :prop="item.prop" :label="item.label" :align="item.align"
                  :fixed="getFixed(item, i)" :width="item.width" :key="i"
                  :show-overflow-tooltip='!!columnData.showOverflow'
                  :sortable="item.sortable?'custom':item.sortable">
                  <template slot-scope="scope">
                    <template v-if="scope.row[item.id] &&!scope.row.top">
                      <JnpfSign v-model="scope.row[item.id]" detailed />
                    </template>
                    <template v-else>{{scope.row[item.id]}}</template>
                  </template>
                </el-table-column>
              </template>
              <template v-else-if="item.jnpfKey==='rate'">
                <el-table-column :prop="item.prop" :label="item.label" :align="item.align"
                  :fixed="getFixed(item, i)" :width="item.width" :key="i"
                  :sortable="item.sortable?'custom':item.sortable">
                  <template slot-scope="scope">
                    <template v-if="!scope.row.top">
                      <JnpfRate v-model="scope.row[item.id]" disabled :count="item.count" />
                    </template>
                    <template v-else>{{scope.row[item.id]}}</template>
                  </template>
                </el-table-column>
              </template>
              <template v-else-if="item.jnpfKey==='slider'">
                <el-table-column :prop="item.prop" :label="item.label" :align="item.align"
                  :fixed="getFixed(item, i)" :width="item.width" :key="i"
                  :sortable="item.sortable?'custom':item.sortable">
                  <template slot-scope="scope">
                    <template v-if="!scope.row.top">
                      <JnpfSlider v-model="scope.row[item.id]" disabled />
                    </template>
                    <template v-else>{{scope.row[item.id]}}</template>
                  </template>
                </el-table-column>
              </template>
              <template v-else-if="item.jnpfKey==='uploadFile'">
                <el-table-column :prop="item.prop" :label="item.label" :align="item.align"
                  :fixed="getFixed(item, i)" :width="item.width" :key="i"
                  :sortable="item.sortable?'custom':item.sortable">
                  <template slot-scope="scope">
                    <template v-if="scope.row[item.id]">
                      <JnpfUploadFile v-model="scope.row[item.id]" disabled detailed
                        v-if="scope.row[item.id] && scope.row[item.id].length" simple />
                    </template>
                  </template>
                </el-table-column>
              </template>
              <template v-else-if="item.jnpfKey==='uploadImg'">
                <el-table-column :prop="item.prop" :label="item.label" :align="item.align"
                  :fixed="getFixed(item, i)" :width="item.width" :key="i"
                  :sortable="item.sortable?'custom':item.sortable">
                  <template slot-scope="scope">
                    <template v-if="scope.row[item.id]">
                      <JnpfUploadImg v-model="scope.row[item.id]" disabled simple detailed
                        v-if="scope.row[item.id] && scope.row[item.id].length" />
                    </template>
                  </template>
                </el-table-column>
              </template>
              <template v-else-if="item.jnpfKey=='complexHeader'">
                <el-table-column :prop="item.prop" :label="item.label" :align="item.align"
                  :fixed="getFixed(item, i)" :width="item.width" :key="i"
                  :sortable="item.sortable?'custom':item.sortable">
                  <template v-if="item.children&&item.children.length">
                    <template v-for="child in item.children">
                      <el-table-column v-if="child.jnpfKey=='relationForm'" :align="child.align"
                        :prop="child.prop" :label="child.label" :key="child.prop"
                        :show-overflow-tooltip='!!columnData.showOverflow' :width="child.width"
                        :sortable="child.sortable?'custom':child.sortable">
                        <template slot-scope="scope">
                          <el-link :underline="false"
                            @click.native="toDetail(child.modelId,scope.row[`${child.prop}_id`])"
                            type="primary">
                            {{ scope.row[child.prop] }}</el-link>
                        </template>
                      </el-table-column>
                      <el-table-column :prop="child.prop" :label="child.label" :align="child.align"
                        :width="child.width" :key="child.prop"
                        :show-overflow-tooltip='!!columnData.showOverflow'
                        :sortable="child.sortable?'custom':child.sortable"
                        v-else-if="child.jnpfKey==='inputNumber' ||child.jnpfKey==='calculate'">
                        <template slot-scope="scope">
                          <template v-if="scope.row[child.id]">
                            <JnpfNumber v-model="scope.row[child.id]" :thousands="child.thousands"
                              :precision="child.precision">
                            </JnpfNumber>
                          </template>
                        </template>
                      </el-table-column>
                      <el-table-column :prop="child.prop" :label="child.label" :align="child.align"
                        :width="child.width" :key="child.prop"
                        :show-overflow-tooltip='!!columnData.showOverflow'
                        :sortable="child.sortable?'custom':child.sortable"
                        v-else-if="child.jnpfKey==='input'">
                        <template slot-scope="scope">
                          <template v-if="scope.row[child.id]">
                            <jnpf-input v-model="scope.row[child.id]"
                              :showOverflow='getTooltip(child.jnpfKey)'
                              :addonBefore="child.addonBefore" :addonAfter="child.addonAfter"
                              :useMask="child.useMask" :maskConfig="child.maskConfig" detailed />
                          </template>
                        </template>
                      </el-table-column>
                      <el-table-column :prop="child.prop" :label="child.label" :align="child.align"
                        :width="child.width" :key="child.prop"
                        :show-overflow-tooltip='!!columnData.showOverflow'
                        :sortable="child.sortable?'custom':child.sortable"
                        v-else-if="child.jnpfKey==='sign'">
                        <template slot-scope="scope">
                          <template v-if="scope.row[child.id]">
                            <JnpfSign v-model="scope.row[child.id]" detailed />
                          </template>
                        </template>
                      </el-table-column>
                      <el-table-column :prop="child.prop" :label="child.label" :align="child.align"
                        :width="child.width" :key="child.prop"
                        :sortable="child.sortable?'custom':child.sortable"
                        v-else-if="child.jnpfKey==='rate'">
                        <template slot-scope="scope">
                          <JnpfRate v-model="scope.row[child.id]" disabled :count="child.count" />
                        </template>
                      </el-table-column>
                      <el-table-column :prop="child.prop" :label="child.label" :align="child.align"
                        :width="child.width" :key="child.prop"
                        :sortable="child.sortable?'custom':child.sortable"
                        v-else-if="child.jnpfKey==='slider'">
                        <template slot-scope="scope">
                          <JnpfSlider v-model="scope.row[child.id]" disabled />
                        </template>
                      </el-table-column>
                      <el-table-column :prop="child.prop" :label="child.label" :align="child.align"
                        :width="child.width" :key="child.prop"
                        :sortable="child.sortable?'custom':child.sortable"
                        v-else-if="child.jnpfKey==='uploadFile'">
                        <template slot-scope="scope">
                          <template v-if="scope.row[child.id]">
                            <JnpfUploadFile v-model="scope.row[child.id]" disabled
                              v-if="scope.row[child.id] && scope.row[child.id].length" />
                          </template>
                        </template>
                      </el-table-column>
                      <el-table-column :prop="child.prop" :label="child.label" :align="child.align"
                        :width="child.width" :key="child.prop"
                        :sortable="child.sortable?'custom':child.sortable"
                        v-else-if="child.jnpfKey==='uploadImg'">
                        <template slot-scope="scope">
                          <template v-if="scope.row[child.id]">
                            <JnpfUploadImg v-model="scope.row[child.id]" detailed simple disabled
                              v-if="scope.row[child.id] && scope.row[child.id].length" />
                          </template>
                        </template>
                      </el-table-column>
                      <el-table-column :prop="child.prop" :label="child.label" :key="child.prop"
                        :width="child.width" :align="child.align"
                        :show-overflow-tooltip='!!columnData.showOverflow'
                        :sortable="child.sortable?'custom':child.sortable" v-else />
                    </template>
                  </template>
                </el-table-column>
              </template>
              <el-table-column :prop="item.prop" :label="item.label" :align="item.align"
                :fixed="getFixed(item, i)" :width="item.width" :key="i"
                :show-overflow-tooltip='!!columnData.showOverflow'
                :sortable="item.sortable?'custom':item.sortable" v-else />
            </template>
          </template>
          <el-table-column
            :fixed="columnList.some(o=>o.fixed == 'right')&&columnData.childTableStyle!=2?'right':false"
            prop="flowState" label="状态" width="100" v-if="config.enableFlow==1">
            <template slot-scope="scope" v-if="!scope.row.top||columnData.type==5">
              <el-tag v-if="scope.row.flowState==1">等待审核</el-tag>
              <el-tag type="success" v-else-if="scope.row.flowState==2">审核通过</el-tag>
              <el-tag type="danger" v-else-if="scope.row.flowState==3">审核退回</el-tag>
              <el-tag type="info"
                v-else-if="scope.row.flowState==4 || scope.row.flowState == 7">流程撤回</el-tag>
              <el-tag type="info" v-else-if="scope.row.flowState==5">审核终止</el-tag>
              <el-tag type="danger" v-else-if="scope.row.flowState==6">已被挂起</el-tag>
              <el-tag type="warning" v-else>等待提交</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作"
            :fixed="columnData.childTableStyle==2&&childColumnList.length?false:'right'"
            :width="operationWidth" v-if="columnBtnsList.length || customBtnsList.length">
            <template slot-scope="scope" v-if="!scope.row.top||columnData.type==5">
              <template v-if="scope.row.rowEdit">
                <el-button size="mini" type="text" @click="saveForRowEdit(scope.row,1)">
                  保存</el-button>
                <el-button size="mini" type="text" class="JNPF-table-delBtn"
                  @click="cancelRowEdit(scope.row,scope.$index)">取消</el-button>
                <el-button size="mini" type="text" @click="submitForRowEdit(scope.row)"
                  v-if="config.enableFlow==1">提交</el-button>
              </template>
              <template v-else>
                <template v-if="isPreview || !columnData.useBtnPermission">
                  <template v-for="(item, i) in columnBtnsList">
                    <template v-if="item.value=='edit'">
                      <template v-if="columnData.type === 4">
                        <el-button size="mini" type="text" :key="i"
                          :disabled="config.enableFlow==1 && [1,2,4,5,6].indexOf(scope.row.flowState)>-1"
                          @click="editForRowEdit(scope.row)">
                          {{item.label}}</el-button>
                      </template>
                      <template v-else>
                        <el-button size="mini" type="text" :key="i"
                          :disabled="config.enableFlow==1 && [1,2,4,5,6].indexOf(scope.row.flowState)>-1"
                          @click="columnBtnsHandel(item.value,scope.row)">
                          {{item.label}}</el-button>
                      </template>
                    </template>
                    <template v-else-if="item.value=='remove'">
                      <el-button size="mini" type="text" :key="i" class="JNPF-table-delBtn"
                        :disabled="config.enableFlow==1 && [1,2,3,5,6].indexOf(scope.row.flowState)>-1"
                        @click="columnBtnsHandel(item.value,scope.row,scope.$index)">
                        {{item.label}}</el-button>
                    </template>
                    <template v-else-if="item.value=='detail'">
                      <el-button size="mini" type="text" :key="i"
                        :disabled="config.enableFlow==1 && !scope.row.flowState"
                        @click="columnBtnsHandel(item.value,scope.row)" v-if="scope.row.id">
                        {{item.label}}</el-button>
                    </template>
                  </template>
                  <template v-if="customBtnsList.length">
                    <el-dropdown hide-on-click>
                      <span class="el-dropdown-link">
                        <el-button type="text" size="mini">
                          {{$t('common.moreBtn')}}<i class="el-icon-arrow-down el-icon--right"></i>
                        </el-button>
                      </span>
                      <el-dropdown-menu slot="dropdown">
                        <el-dropdown-item v-for="(item, i) in customBtnsList" :key="i"
                          @click.native="customBtnsHandel(item,scope.row,scope.$index)">
                          {{item.label}}</el-dropdown-item>
                      </el-dropdown-menu>
                    </el-dropdown>
                  </template>
                </template>
                <template v-else>
                  <template v-for="(item, i) in columnBtnsList">
                    <template v-if="item.value=='edit'">
                      <template v-if="columnData.type === 4">
                        <el-button size="mini" type="text" :key="i"
                          :disabled="config.enableFlow==1 && [1,2,4,5].indexOf(scope.row.flowState)>-1"
                          @click="scope.row.rowEdit=true" v-has="'btn_'+item.value">
                          {{item.label}}</el-button>
                      </template>
                      <template v-else>
                        <el-button size="mini" type="text" :key="i"
                          :disabled="config.enableFlow==1 && [1,2,4,5].indexOf(scope.row.flowState)>-1"
                          @click="columnBtnsHandel(item.value,scope.row)" v-has="'btn_'+item.value">
                          {{item.label}}</el-button>
                      </template>
                    </template>
                    <template v-else-if="item.value=='remove'">
                      <el-button size="mini" type="text" :key="i" class="JNPF-table-delBtn"
                        :disabled="config.enableFlow==1 && [1,2,3,5].indexOf(scope.row.flowState)>-1"
                        @click="columnBtnsHandel(item.value,scope.row,scope.$index)"
                        v-has="'btn_'+item.value">{{item.label}}</el-button>
                    </template>
                    <template v-else-if="item.value=='detail'">
                      <el-button size="mini" type="text" :key="i"
                        :disabled="config.enableFlow==1 && !scope.row.flowState"
                        @click="columnBtnsHandel(item.value,scope.row)" v-has="'btn_'+item.value">
                        {{item.label}}</el-button>
                    </template>
                  </template>
                  <template v-if="showMoreBtn">
                    <el-dropdown hide-on-click>
                      <span class="el-dropdown-link">
                        <el-button type="text" size="mini">
                          {{$t('common.moreBtn')}}
                          <i class="el-icon-arrow-down el-icon--right"></i>
                        </el-button>
                      </span>
                      <el-dropdown-menu slot="dropdown">
                        <el-dropdown-item v-for="(item, i) in customBtnsList" :key="i"
                          @click.native="customBtnsHandel(item,scope.row,scope.$index)"
                          v-has="item.value">{{item.label}}</el-dropdown-item>
                      </el-dropdown-menu>
                    </el-dropdown>
                  </template>
                </template>
              </template>
            </template>
          </el-table-column>
        </JNPF-table>
        <template
          v-if="columnData.type !== 3 &&columnData.type !== 5&& columnData.hasPage&&refreshTable">
          <pagination :total="total" :page.sync="listQuery.currentPage"
            :limit.sync="listQuery.pageSize" @pagination="initData" />
        </template>
      </div>
    </div>
    <print-browse :visible.sync="printBrowseVisible" :id="printId"
      :batchIds="multipleSelection.join()" />
    <FlowBox v-show="flowVisible" ref="FlowBox" @close="closeFlow" />
    <Form v-show="formVisible" ref="Form" @refreshDataList="refresh" />
    <extraForm v-show="extraFormVisible" ref="extraForm" @refreshDataList="refresh" />
    <Detail v-if="detailVisible" ref="Detail" @close="detailVisible = false" />
    <RelevanceDetail v-if="detailsVisible" ref="RelevanceDetail" @close="detailsVisible = false" />
    <ExportBox v-if="exportBoxVisible" ref="ExportBox" @download="download" />
    <ImportBox v-if="uploadBoxVisible" ref="UploadBox" @refresh="initData" />
    <CustomBox v-if="customBoxVisible" ref="CustomBox" @close="customBoxVisible= false" />
    <SuperQuery v-if="superQueryVisible" ref="SuperQuery" :columnOptions="columnOptions"
      @superQuery="superQuery" />
    <candidate-form :visible.sync="candidateVisible" :candidateList="candidateList"
      :branchList="branchList" taskId="0" :formData="workFlowFormData"
      @submitCandidate="submitCandidate" :isCustomCopy="isCustomCopy" />
    <PrintDialog v-if="printDialogVisible" ref="printDialog" @change="printBrowseHandle">
    </PrintDialog>
    <SelectFlow ref="selectFlow" v-if="flowListVisible" @selectFlow='selectFlow' />
  </div>
</template>
<script>
import PrintDialog from '@/components/PrintDialog'
import PrintBrowse from "@/components/PrintBrowse/batch";
import { getModelList, getModelSubList, deleteModel, batchDelete, exportModel, createModel, updateModel, getConfigData, getModelInfo } from '@/api/onlineDev/visualDev'
import { Create, Update } from '@/api/workFlow/workFlowForm'
import { printOptionsApi } from '@/api/system/printDev'
import { getDataInterfaceRes } from '@/api/systemData/dataInterface'
import { getColumnsByModuleId } from '@/api/common'
import { dyOptionsList, systemComponentsList, noGroupList } from '@/components/Generator/generator/comConfig'
import { Candidates } from '@/api/workFlow/FlowBefore'
import { getFlowList } from '@/api/workFlow/FlowEngine'
import Form from './Form'
import { deepClone } from '@/components/Generator/utils'
import extraForm from './extraForm'
import FlowBox from '@/views/workFlow/components/FlowBox'
import Detail from './detail'
import RelevanceDetail from './RelevanceDetail'
import ExportBox from '@/components/ExportBox'
import Search from './Search'
import ChildTableColumn from './child-table-column'
import SuperQuery from '@/components/SuperQuery'
import CandidateForm from '@/views/workFlow/components/CandidateForm'
import CustomBox from '@/components/JNPFCustom'
import { mapGetters } from "vuex";
import { getDateDay, getLaterData, getBeforeData, getBeforeTime, getLaterTime, thousandsFormat } from '@/components/Generator/utils/index.js'
import SelectFlow from '@/components/SelectFlowDialog'
export default {
  name: 'dynamicModel',
  components: { PrintDialog, PrintBrowse, Form, extraForm, ExportBox, Search, Detail, FlowBox, ChildTableColumn, SuperQuery, CandidateForm, CustomBox, RelevanceDetail, SelectFlow },
  props: ['config', 'modelId', 'isPreview'],
  data() {
    return {
      printDialogVisible: false,
      printBrowseVisible: false,
      printId: "",
      printListOptions: [],
      systemComponentsList,
      keyword: '',
      treeProps: {
        children: 'children',
        label: 'fullName',
        value: 'id',
        isLeaf: 'isLeaf'
      },
      list: [],
      cacheList: [],
      total: 0,
      listLoading: false,
      listQuery: {
        currentPage: 1,
        pageSize: 20,
        sort: 'desc',
        sidx: '',
        menuId: '',
        queryJson: '',
        superQueryJson: ''
      },
      defListQuery: {
        sort: 'desc',
        sidx: '',
      },
      defaultListQuery: {
        pageSize: 20,
        sort: 'desc',
        sidx: '',
      },
      flowVisible: false,
      formVisible: false,
      extraFormVisible: false,
      detailVisible: false,
      detailsVisible: false,
      importBoxVisible: false,
      exportBoxVisible: false,
      uploadBoxVisible: false,
      customBoxVisible: false,
      superQueryVisible: false,
      treeData: [],
      expandsTree: true,
      treeActiveId: '',
      columnData: {
        columnBtnsList: []
      },
      ordersList: [],
      formData: {},
      columnList: [],
      childColumnList: [],
      columnOptions: [],
      exportList: [],
      columnBtnsList: [],
      customBtnsList: [],
      hasBatchBtn: false,
      refreshTable: false,
      expandsTable: true,
      multipleSelection: [],
      settingsColumnList: [],
      mergeList: [],
      expandObj: {},
      flowTemplateJson: {},
      isCustomCopy: false,
      candidateVisible: false,
      candidateType: 1,
      branchList: [],
      candidateList: [],
      currRow: {},
      workFlowFormData: {},
      rowStyle: null,
      cellStyle: null,
      refreshTree: true,
      flowList: [],
      flowListVisible: false,
      currFlow: {},
      treeLoading: false,
      treeRelationObj: {},
      getLeftTreeActiveInfo: {},
      countersignOver: false
    }
  },
  computed: {
    ...mapGetters(['userInfo']),
    operationWidth() {
      let customWidth = this.customBtnsList.length ? 50 : 0
      if (this.columnData.type == 4 && this.config.enableFlow) customWidth += 50
      return this.columnBtnsList.length * 50 + customWidth
    },
    showMoreBtn() {
      return this.customBtnsList.length && this.customBtnsList.some(o => this.jnpf.hasBtnP(o.value))
    }
  },
  watch: {
    keyword(val) {
      if (this.columnData.treeSynType == 0) this.$refs.treeBox.filter(val)
    }
  },
  created() {
    this.init()
  },
  methods: {
    async init() {
      this.listLoading = true
      this.listQuery.menuId = this.$route.meta.modelId
      this.refreshTable = false
      if (!this.config.columnData || !this.config.formData) return
      this.columnData = JSON.parse(this.config.columnData)
      if (this.columnData.printIds && this.columnData.printIds.length > 0) {
        this.getPrintListOptions(this.columnData.printIds)
      }
      if (this.columnData.type === 3) {
        this.columnData.columnList = this.columnData.columnList.filter(o => o.prop != this.columnData.groupField)
      }
      if (this.config.enableFlow == 1) {
        if (this.config.flowId) this.getFlowList()
      }
      this.hasBatchBtn = this.columnData.btnsList.some(o => ['batchRemove', 'batchPrint', 'download'].includes(o.value))
      if (this.columnData.useBtnPermission) {
        const permissionList = this.$store.getters.permissionList
        const modelId = this.$route.meta.modelId
        const list = permissionList.filter(o => o.modelId === modelId)
        const btnsList = list[0] && list[0].button ? list[0].button : []
        this.hasBatchBtn = btnsList.some(o => ['btn_batchRemove', 'btn_batchPrint', 'btn_download'].includes(o.enCode))
      }
      this.formData = JSON.parse(this.config.formData)
      this.customBtnsList = this.columnData.customBtnsList || []
      this.columnBtnsList = this.columnData.columnBtnsList || []
      this.columnOptions = this.columnData.columnOptions || []
      this.listLoading = true
      if (this.isPreview) this.listQuery.menuId = "270579315303777093"
      let res = await getColumnsByModuleId(this.listQuery.menuId)
      this.settingsColumnList = res.data || []
      this.rowStyle = this.jnpf.getScriptFunc.call(this, this.columnData.funcs && this.columnData.funcs.rowStyle)
      this.cellStyle = this.jnpf.getScriptFunc.call(this, this.columnData.funcs && this.columnData.funcs.cellStyle)
      this.getSearchSchemas()
      this.getColumnList()
      this.$nextTick(() => {
        this.refreshTable = true
      })
      if (this.columnData.type === 4) this.buildOptions()
      if (this.isPreview) return this.listLoading = false
      this.initDefaultSearchData()
      this.listQuery.pageSize = this.columnData.pageSize
      this.listQuery.sort = this.columnData.sort
      this.listQuery.sidx = this.columnData.defaultSidx
      this.defaultListQuery.pageSize = this.columnData.pageSize
      this.defaultListQuery.sort = this.columnData.sort
      this.defaultListQuery.sidx = this.columnData.defaultSidx
      this.setDefaultQuery(this.columnData.defaultSortConfig)
      if (this.columnData.type === 3 || !this.columnData.hasPage) this.listQuery.pageSize = 10000
      if (this.columnData.type === 2) {
        this.treeProps.value = this.columnData.treePropsValue || 'id'
        this.treeProps.label = this.columnData.treePropsLabel || 'fullName'
        this.treeProps.children = this.columnData.treePropsChildren || 'children'
        this.getTreeView()
      } else {
        this.initData()
      }
      this.addSearchList()
    },
    getTooltip(jnpfKey) {
      return ["slider", "rate", ...noGroupList].includes(jnpfKey) ? false : !!this.columnData.showOverflow
    },
    setDefaultQuery(defaultSortList) {
      const defaultSortConfig = (defaultSortList || []).map(o =>
        (o.sort === 'desc' ? '-' : '') + o.field);
      this.defListQuery.sidx = defaultSortConfig.join(',')
    },
    addSearchList() {
      const searchList = this.columnData.searchList;
      const obj = {
        label: '关键词',
        __config__: { jnpfKey: 'input' },
        value: undefined,
        id: 'jnpfKeyword',
        prop: 'jnpfKeyword',
      };
      for (let i = 0; i < searchList.length; i++) {
        const item = searchList[i];
        if (item.isKeyword) return searchList.unshift(obj);
      }
    },
    async getPrintListOptions(ids) {
      printOptionsApi({ ids }).then(res => {
        this.printListOptions = res.data
      })
    },
    treeRefresh() {
      this.keyword = ''
      this.treeActiveId = ''
      this.$refs.treeBox.setCurrentKey(null)
      this.getTreeView()
    },
    printBrowseHandle(id) {
      this.printDialogVisible = false
      this.handleBatchPrint(id)
    },
    printDialog() {
      this.printDialogVisible = true
      this.$nextTick(() => {
        if (this.printListOptions.length == 1) {
          this.printBrowseHandle(this.printListOptions[0].id)
          return
        }
        this.$refs.printDialog.initOptions(this.printListOptions)
      })
    },
    handleBatchPrint(id) {
      if (!id) {
        this.$message({
          type: "warning",
          message: "请配置打印模板",
          duration: 1500
        });
        return;
      }
      this.printId = id;
      this.printBrowseVisible = true;
    },
    initData() {
      if (this.isPreview) return
      this.listLoading = true
      let query = {
        ...this.listQuery,
        ...this.defListQuery
      }
      getModelList(this.modelId, query).then(res => {
        if (this.columnData.type === 4) {
          this.list = res.data.list.map(o => ({
            ...o,
            ...this.expandObj,
            rowEdit: false
          }))
          this.cacheList = JSON.parse(JSON.stringify(this.list))
        } else {
          this.list = res.data.list.map(o => ({
            ...o,
            ...this.expandObj,
            hasChildren: true
          }))
          if (this.columnData.type === 3) {
            this.list.map(o => {
              if (o.children && o.children.length) {
                o.children = o.children.map(e => ({
                  ...e,
                  ...this.expandObj
                }))
              }
            })
          }
        }
        if (this.columnData.type !== 3 && this.columnData.hasPage) this.total = res.data.pagination.total
        this.listLoading = false
        this.$nextTick(() => {
          if (this.columnData.funcs && this.columnData.funcs.afterOnload) this.setTableLoadFunc()
        })
      })
    },
    /**
    * 对表格进行合计 目前只支持数字，金额，滑块
    */
    getTableSummaries(param) {
      const thousandsField = []
      this.columnList.forEach(res => {
        if (res.thousands) thousandsField.push(res.id)
      })
      const { columns, data } = param;
      const sums = [];
      columns.forEach((column, index) => {
        if (index === 0) {
          sums[index] = '合计';
          return;
        } else if (this.columnData.summaryField.includes(column.property)) {
          const values = data.map(item => Number(item[column.property]));
          if (!values.every(value => isNaN(value))) {
            sums[index] = values.reduce((prev, curr) => {
              const value = Number(curr);
              if (!isNaN(value)) {
                return prev + curr;
              } else {
                return prev;
              }
            }, 0).toFixed(2);
            if (thousandsField.includes(column.property)) sums[index] = thousandsFormat(sums[index])
          } else {
            sums[index] = '';
          }
        }
      })
      return sums;
    },
    toDetail(modelId, id) {
      if (!id) return
      this.mainLoading = true
      getConfigData(modelId).then(res => {
        this.mainLoading = false
        if (!res.data || !res.data.formData) return
        let formData = JSON.parse(res.data.formData)
        formData.popupType = 'general'
        this.detailsVisible = true
        this.$nextTick(() => {
          this.$refs.RelevanceDetail.init(formData, modelId, id)
        })
      }).catch(() => { this.mainLoading = false })
    },
    getTreeView() {
      this.treeLoading = true
      if (this.columnData.treeDataSource === "dictionary") {
        if (!this.columnData.treeDictionary) return this.treeLoading = false
        this.$store.dispatch('base/getDicDataSelector', this.columnData.treeDictionary).then(res => {
          this.treeData = res
          this.treeLoading = false
          this.initData()
        })
      }
      if (this.columnData.treeDataSource === "organize") {
        this.$store.dispatch('generator/getDepTree').then(res => {
          this.treeData = res
          this.treeLoading = false
          this.initData()
        })
      }
      if (this.columnData.treeDataSource === "department") {
        this.$store.dispatch('generator/getDepTree').then(res => {
          this.treeData = res
          this.treeLoading = false
          this.initData()
        })
      }
      if (this.columnData.treeDataSource === "api") {
        if (!this.columnData.treePropsUrl) return this.treeLoading = false
        getDataInterfaceRes(this.columnData.treePropsUrl).then(res => {
          this.treeData = Array.isArray(res.data) ? res.data : [];
          this.treeLoading = false
          this.initData()
        })
      }
    },
    getSearchSchemas() {
      if (this.columnData.treeRelation) {
        for (let i = 0; i < this.columnData.columnOptions.length; i++) {
          const e = this.columnData.columnOptions[i];
          if (e.id === this.columnData.treeRelation) {
            this.treeRelationObj = { ...e, searchMultiple: false, jnpfKey: e.__config__.jnpfKey };
            break;
          }
        }
      }
    },
    getColumnList() {
      if (this.isPreview) {
        const columnList = this.columnData.columnList
        this.columnList = this.transformColumnList(columnList)
        return
      }
      let columnPermissionList = []
      if (!this.columnData.useColumnPermission) {
        columnPermissionList = this.columnData.columnList
      } else {
        const permissionList = this.$store.getters.permissionList
        const modelId = this.$route.meta.modelId
        const list = permissionList.filter(o => o.modelId === modelId)
        const columnList = list[0] && list[0].column ? list[0].column : []
        for (let i = 0; i < this.columnData.columnList.length; i++) {
          inner: for (let j = 0; j < columnList.length; j++) {
            if (this.columnData.columnList[i].prop === columnList[j].enCode) {
              columnPermissionList.push(this.columnData.columnList[i])
              break inner
            }
          }
        }
      }
      if (![3, 5].includes(this.columnData.type)) columnPermissionList = this.getComplexColumns(columnPermissionList)
      this.columnList = this.transformColumnList(columnPermissionList)
    },
    getComplexColumns(columns) {
      let complexHeaderList = this.columnData.complexHeaderList || [];
      if (!complexHeaderList.length) return columns;
      let childColumns = [];
      for (let i = 0; i < complexHeaderList.length; i++) {
        const e = complexHeaderList[i];
        e.label = e.fullName;
        e.align = e.align;
        e.dataIndex = e.id;
        e.prop = e.id;
        e.children = [];
        e.jnpfKey = 'complexHeader';
        e.sortable = false;
        if (e.childColumns && e.childColumns.length) {
          childColumns.push(...e.childColumns);
          for (let j = 0; j < columns.length; j++) {
            const o = columns[j];
            if (e.childColumns.includes(o.id) && o.fixed !== 'left' && o.fixed !== 'right') e.children.push({ ...o });
          }
        }
      }
      complexHeaderList = complexHeaderList.filter(o => o.children.length);
      for (let i = 0; i < columns.length; i++) {
        const item = columns[i];
        if (!childColumns.includes(item.id)) complexHeaderList.push(item);
      }
      return complexHeaderList;
    },
    transformColumnList(columnList) {
      let list = []
      for (let i = 0; i < columnList.length; i++) {
        const e = columnList[i];
        if (!e.prop.includes('-')) {
          list.push(e)
        } else {
          let prop = e.prop.split('-')[0]
          let vModel = e.prop.split('-')[1]
          let label = e.label.split('-')[0]
          let childLabel = e.label.replace(label + '-', '');
          let newItem = {
            align: "center",
            jnpfKey: "table",
            prop,
            label,
            children: []
          }
          e.vModel = vModel
          e.childLabel = childLabel
          if (!this.expandObj.hasOwnProperty(`${prop}Expand`)) this.$set(this.expandObj, `${prop}Expand`, false)
          if (!list.some(o => o.prop === prop)) list.push(newItem)
          for (let i = 0; i < list.length; i++) {
            if (list[i].prop === prop) {
              list[i].children.push(e)
              break
            }
          }
        }
      }

      this.getMergeList(list)
      this.getExportList(list)
      this.childColumnList = list.filter(o => o.jnpfKey === 'table')
      return list
    },
    getExportList(list) {
      let exportList = []
      for (let i = 0; i < list.length; i++) {
        if (list[i].jnpfKey === 'table') {
          if (this.columnData.type != 4) exportList.push(...list[i].children)
        } else if (list[i].jnpfKey === 'complexHeader') {
          exportList.push(...list[i].children)
        } else {
          exportList.push(list[i])
        }
      }
      this.exportList = exportList.filter(o => !noGroupList.includes(o.__config__.jnpfKey))
    },
    initDefaultSearchData() {
      let searchList = this.columnData.searchList
      //处理搜索条件中的默认值
      if (searchList != null && searchList.length > 0) {
        let obj = {}
        for (let i = 0; i < searchList.length; i++) {
          const e = searchList[i]
          if (e.value) {
            if (Array.isArray(e.value)) {
              if (e.value.length) obj[e.id] = e.value
            } else {
              obj[e.id] = e.value
            }
          }
          //if ((Array.isArray(e.value) && e.value.length) || e.value || e.value === 0 || e.value === false) e.defaultValue = e.value;
        }
        this.listQuery.queryJson = JSON.stringify(obj) === "{}" ? '' : JSON.stringify(obj)
      }
    },
    getMergeList(list) {
      list.forEach(item => {
        if (item.children && item.children.length > 0 && item.jnpfKey == 'table') {
          item.children.forEach((child, index) => {
            if (index == 0) {
              this.mergeList.push({
                prop: child.prop,
                rowspan: 1,
                colspan: item.children.length
              })
            } else {
              this.mergeList.push({
                prop: child.prop,
                rowspan: 0,
                colspan: 0
              })
            }
          })
        } else {
          this.mergeList.push({
            prop: item.prop,
            rowspan: 1,
            colspan: 1
          })
        }
      })
    },
    arraySpanMethod({ column }) {
      for (let i = 0; i < this.mergeList.length; i++) {
        if (column.property == this.mergeList[i].prop) {
          return [this.mergeList[i].rowspan, this.mergeList[i].colspan]
        }
      }
    },
    toggleExpand(row, field) {
      row[field] = !row[field]
    },
    getNodePath(node) {
      let fullPath = []
      const loop = (node) => {
        if (node.level) fullPath.unshift(node.data)
        if (node.parent) loop(node.parent)
      }
      loop(node)
      return fullPath
    },
    handleNodeClick(data, node) {
      if (this.treeActiveId == data[this.treeProps.value]) return
      this.treeActiveId = data[this.treeProps.value]
      this.$refs.Search.treeReset()
      let queryJson = {}
      const isMultiple = !this.treeRelationObj ? false : this.treeRelationObj.searchMultiple;
      if (this.treeRelationObj && this.treeRelationObj.jnpfKey && ['organizeSelect', 'cascader', 'areaSelect'].includes(this.treeRelationObj.jnpfKey)) {
        const nodePath = this.getNodePath(node)
        const currValue = nodePath.map(o => o[this.treeProps.value])
        this.treeActiveId = currValue
        queryJson = { [this.columnData.treeRelation]: isMultiple ? [currValue] : currValue };
      } else {
        queryJson = { [this.columnData.treeRelation]: isMultiple ? [this.treeActiveId] : this.treeActiveId };
      }
      const loop = (data, parent) => {
        if (!data) return
        if (data.__config__ && data.__config__.children && Array.isArray(data.__config__.children)) {
          loop(data.__config__.children, data)
        }
        if (Array.isArray(data)) data.forEach(d => loop(d, parent))
        if (data.__config__ && data.__config__.jnpfKey) {
          if (data.__vModel__ == this.columnData.treeRelation) {
            data.__config__.defaultCurrent = false
            data.__config__.defaultValue = data.multiple ? [this.treeActiveId] : this.treeActiveId
            this.getLeftTreeActiveInfo = { [this.columnData.treeRelation]: data.__config__.defaultValue }
          }
        }
      }
      loop(this.formData.fields)
      this.search(JSON.stringify(queryJson))
    },
    handleDel(id, index) {
      this.$confirm(this.$t('common.delTip'), this.$t('common.tipTitle'), {
        type: 'warning'
      }).then(() => {
        if (id) {
          deleteModel(this.modelId, id).then(res => {
            this.$message({
              type: 'success',
              message: res.msg,
              duration: 1000,
              onClose: () => { this.initData() }
            });
          })
        } else {
          this.list.splice(index, 1)
        }
      }).catch(() => { });
    },
    editForRowEdit(row) {
      row.rowEdit = true
      this.addHandleForRowEdit(true)
      if (this.columnData.type === 4) return
      const flowId = row.flowId || this.flowList[0].id;
      if (!flowId) return
      const list = this.flowList.filter(o => o.id === flowId)
      this.currFlow = !list.length ? this.flowList[0] : list[0]
      let flowTemplateJson = this.currFlow.flowTemplateJson ? JSON.parse(this.currFlow.flowTemplateJson) : {}
      this.isCustomCopy = flowTemplateJson.properties && flowTemplateJson.properties.isCustomCopy
    },
    saveForRowEdit(row, status, candidateData) {
      if (this.isPreview) return this.$message({ message: '功能预览不支持数据保存', type: 'warning' })
      if (this.config.enableFlow == 1) {
        let query = {
          id: row.id,
          status: status || "1",
          candidateType: this.candidateType,
          countersignOver: this.countersignOver,
          formData: row,
          flowId: this.currFlow.id,
          flowUrgent: 1
        }
        if (candidateData) query = { ...query, ...candidateData }
        const formMethod = query.id ? Update : Create
        formMethod(query).then(res => {
          this.$message({
            message: res.msg,
            type: 'success',
            duration: 1500,
            onClose: () => {
              this.candidateVisible = false
              this.initData()
            }
          })
        })
      } else {
        let query = {
          id: row.id,
          data: JSON.stringify(row)
        }
        const formMethod = query.id ? updateModel : createModel
        formMethod(this.modelId, query).then(res => {
          this.$message({
            message: res.msg,
            type: 'success',
            duration: 1500,
            onClose: () => {
              this.candidateVisible = false
              this.initData()
            }
          })
        })
      }
    },
    submitForRowEdit(row) {
      this.currRow = row
      if (this.isPreview) return this.$message({ message: '功能预览不支持数据保存', type: 'warning' })
      this.workFlowFormData = {
        id: row.id,
        formData: row,
        flowId: this.currFlow.id
      }
      Candidates(0, this.workFlowFormData).then(res => {
        let data = res.data
        this.candidateType = data.type
        this.countersignOver = data.countersignOver
        if (data.type == 1) {
          this.branchList = res.data.list.filter(o => o.isBranchFlow)
          this.candidateList = res.data.list.filter(o => !o.isBranchFlow && o.isCandidates)
          this.candidateVisible = true
        } else if (data.type == 2) {
          this.branchList = []
          this.candidateList = res.data.list.filter(o => o.isCandidates)
          this.candidateVisible = true
        } else {
          if (this.isCustomCopy) {
            this.branchList = []
            this.candidateList = []
            this.candidateVisible = true
            return
          }
          this.$confirm('您确定要提交当前流程吗, 是否继续?', '提示', {
            type: 'warning'
          }).then(() => {
            this.saveForRowEdit(row, '0')
          }).catch(() => { })
        }
      }).catch(() => { })
    },
    submitCandidate(data) {
      this.saveForRowEdit(this.currRow, '0', data)
    },
    cancelRowEdit(row, index) {
      if (!row.id) return this.list.splice(index, 1)
      row.rowEdit = false
      let item = JSON.parse(JSON.stringify(this.cacheList[index]))
      this.$set(this.list, index, item)
    },
    addHandleForRowEdit(flag) {
      let item = {
        rowEdit: true
      }
      for (let i = 0; i < this.columnData.columnList.length; i++) {
        let e = this.columnData.columnList[i]
        item[e.id] = e.__config__.defaultValue
        const config = e.__config__
        if (e.__config__.defaultCurrent) {
          if (e.__config__.jnpfKey === 'datePicker') {
            if (e.__config__.defaultCurrent == true) item[e.id] = new Date().getTime()
            if (config.startTimeRule) {
              if (config.startTimeType == 1) {
                e.startTime = config.startTimeValue
              } else if (config.startTimeType == 3) {
                e.startTime = new Date().getTime()
              } else if (config.startTimeType == 4) {
                let previousDate = '';
                if (config.startTimeTarget == 1 || config.startTimeTarget == 2) {
                  previousDate = getDateDay(config.startTimeTarget, config.startTimeType, config.startTimeValue)
                  e.startTime = new Date(previousDate).getTime()
                } else if (config.startTimeTarget == 3) {
                  previousDate = getBeforeData(config.startTimeValue)
                  e.startTime = new Date(previousDate).getTime()
                } else {
                  e.startTime = getBeforeTime(config.startTimeTarget, config.startTimeValue).getTime()
                }
              } else if (config.startTimeType == 5) {
                let previousDate = '';
                if (config.startTimeTarget == 1 || config.startTimeTarget == 2) {
                  previousDate = getDateDay(config.startTimeTarget, config.startTimeType, config.startTimeValue)
                  e.startTime = new Date(previousDate).getTime()
                } else if (config.startTimeTarget == 3) {
                  previousDate = getLaterData(config.startTimeValue)
                  e.startTime = new Date(previousDate).getTime()
                } else {
                  e.startTime = getLaterTime(config.startTimeTarget, config.startTimeValue).getTime()
                }
              }
            }
            if (config.endTimeRule) {
              if (config.endTimeType == 1) {
                e.endTime = config.endTimeValue
              } else if (config.endTimeType == 3) {
                e.endTime = new Date().getTime()
              } else if (config.endTimeType == 4) {
                let previousDate = '';
                if (config.endTimeTarget == 1 || config.endTimeTarget == 2) {
                  previousDate = getDateDay(config.endTimeTarget, config.endTimeType, config.endTimeValue)
                  e.endTime = new Date(previousDate).getTime()
                } else if (config.endTimeTarget == 3) {
                  previousDate = getBeforeData(config.endTimeValue)
                  e.endTime = new Date(previousDate).getTime()
                } else {
                  e.endTime = getBeforeTime(config.endTimeTarget, config.endTimeValue).getTime()
                }
              } else if (config.endTimeType == 5) {
                let previousDate = '';
                if (config.endTimeTarget == 1 || config.endTimeTarget == 2) {
                  previousDate = getDateDay(config.endTimeTarget, config.endTimeType, config.endTimeValue)
                  e.endTime = new Date(previousDate).getTime()
                } else if (config.endTimeTarget == 3) {
                  previousDate = getLaterData(config.endTimeValue)
                  e.endTime = new Date(previousDate).getTime()
                } else {
                  e.endTime = getLaterTime(config.endTimeTarget, config.endTimeValue).getTime()
                }
              }
            }
          } else if (e.__config__.jnpfKey === 'organizeSelect' && e.__config__.defaultCurrent == true) {
            item[e.id] = e.multiple ? [this.userInfo.organizeIdList] : this.userInfo.organizeIdList
          } else if (config.jnpfKey === 'depSelect' && this.userInfo.departmentId) {
            item[e.id] = e.multiple ? [this.userInfo.departmentId] : this.userInfo.departmentId;
          }
          else if (config.jnpfKey === 'userSelect' && this.userInfo.userId) {
            item[e.id] = e.multiple ? [this.userInfo.userId] : this.userInfo.userId;
          }
          else if (config.jnpfKey === 'usersSelect' && this.userInfo.userId) {
            item[e.id] = e.multiple ? [this.userInfo.userId + '--user'] : this.userInfo.userId + '--user';
          }
          else if (config.jnpfKey === 'posSelect' && this.userInfo.positionIds.length) {
            item[e.id] = e.multiple ? this.userInfo.positionIds.map(o => o.id) : this.userInfo.positionIds[0].id;
          }
          else if (config.jnpfKey === 'roleSelect' && this.userInfo.roleIds.length) {
            item[e.id] = e.multiple ? this.userInfo.roleIds : this.userInfo.roleIds[0];
          }
          else if (config.jnpfKey === 'groupSelect' && this.userInfo.groupIds.length) {
            item[e.id] = e.multiple ? this.userInfo.groupIds : this.userInfo.groupIds[0];
          } else if (e.__config__.jnpfKey === 'timePicker') {
            if (e.__config__.defaultCurrent == true) item[e.id] = this.jnpf.toDate(new Date(), e.format)
            let format = e.format === 'HH:mm' ? 'HH:mm:00' : e.format
            if (config.startTimeRule) {
              if (config.startTimeType == 1) {
                e.startTime = config.startTimeValue || '00:00:00'
                if (e.startTime.split(':').length == 3) {
                  e.startTime = e.startTime
                } else {
                  e.startTime = e.startTime + ':00'
                }
              } else if (config.startTimeType == 3) {
                e.startTime = this.jnpf.toDate(new Date(), format)
              } else if (config.startTimeType == 4) {
                let previousDate = '';
                previousDate = getBeforeTime(config.startTimeTarget, config.startTimeValue)
                e.startTime = this.jnpf.toDate(previousDate, format)
              } else if (config.startTimeType == 5) {
                let previousDate = '';
                previousDate = getLaterTime(config.startTimeTarget, config.startTimeValue)
                e.startTime = this.jnpf.toDate(previousDate, format)
              }
            }
            if (config.endTimeRule) {
              if (config.endTimeType == 1) {
                e.endTime = config.endTimeValue || '23:59:59'
                if (e.endTime.split(':').length == 3) {
                  e.endTime = e.endTime
                } else {
                  e.endTime = e.endTime + ':00'
                }
              } else if (config.endTimeType == 3) {
                e.endTime = this.jnpf.toDate(new Date(), format)
              } else if (config.endTimeType == 4) {
                let previousDate = '';
                previousDate = getBeforeTime(config.endTimeTarget, config.endTimeValue)
                e.endTime = this.jnpf.toDate(previousDate, format)
              } else if (config.endTimeType == 5) {
                let previousDate = '';
                previousDate = getLaterTime(config.endTimeTarget, config.endTimeValue)
                e.endTime = this.jnpf.toDate(previousDate, format)
              }
            }
          } else if (e.__config__.jnpfKey === 'sign' && this.userInfo.signImg) item[e.id] = this.userInfo.signImg
        }
      }
      if (!flag) this.list.unshift(deepClone(item))
    },
    addHandle() {
      if (this.config.enableFlow == 1) {
        if (!this.flowList.length) {
          this.$message({
            type: 'error',
            message: '流程不存在'
          });
        } else if (this.flowList.length === 1) {
          this.selectFlow(this.flowList[0])
        } else {
          this.flowListVisible = true
          this.$nextTick(() => {
            this.$refs.selectFlow.init(this.flowList)
          })
        }
      } else {
        if (this.columnData.type === 4) {
          this.addHandleForRowEdit()
        } else {
          this.formVisible = true
          this.$nextTick(() => {
            this.$refs.Form.init(this.formData, this.modelId, '', this.isPreview, this.columnData.useFormPermission, this.list, this.columnData.type)
          })
        }
      }
    },
    selectFlow(item) {
      this.currFlow = item
      if (this.columnData.type === 4) {
        let flowTemplateJson = item.flowTemplateJson ? JSON.parse(item.flowTemplateJson) : {}
        this.isCustomCopy = flowTemplateJson.properties && flowTemplateJson.properties.isCustomCopy
        this.flowListVisible = false
        this.addHandleForRowEdit()
      } else {
        let data = {
          id: '',
          flowId: item.id,
          opType: '-1',
          type: 1,
          modelId: this.modelId,
          isPreview: this.isPreview,
        }
        let treeRelation = this.columnData.treeRelation
        if (treeRelation) {
          data = { ...data, 'getLeftTreeActiveInfo': this.getLeftTreeActiveInfo }
        }
        this.flowListVisible = false
        this.flowVisible = true
        this.$nextTick(() => {
          this.$refs.FlowBox.init(data)
        })
      }
    },
    updateHandle(row) {
      if (this.config.enableFlow == 1) {
        let data = {
          id: row.id,
          flowId: row.flowId || this.flowList[0].id,
          type: 1,
          opType: '-1',
          modelId: this.modelId,
          isPreview: this.isPreview,
        }
        this.flowVisible = true
        this.$nextTick(() => {
          this.$refs.FlowBox.init(data)
        })
      } else {
        this.formVisible = true
        this.$nextTick(() => {
          this.$refs.Form.init(this.formData, this.modelId, row.id, this.isPreview, this.columnData.useFormPermission, this.list, this.columnData.type)
        })
      }
    },
    headBtnsHandel(key) {
      if (key === 'add') {
        this.addHandle()
      }
      if (key == 'download') {
        this.exportBoxVisible = true
        this.$nextTick(() => {
          this.$refs.ExportBox.init(this.exportList, this.multipleSelection)
        })
      }
      if (key == 'upload') {
        this.uploadBoxVisible = true
        this.$nextTick(() => {
          this.$refs.UploadBox.init(this.modelId, '', this.config.enableFlow, this.flowList)
        })
      }
      if (this.isPreview) return
      if (key === 'batchRemove') {
        this.batchRemove()
      }

      if (key === 'batchPrint') {
        if (!this.multipleSelection.length) {
          this.$message({
            type: 'error',
            message: '请选择一条数据',
            duration: 1500,
          })
          return
        }
        this.printDialog()
      }
    },
    handleSelectionChange(val) {
      const res = val.map(item => item.id)
      this.multipleSelection = res
    },
    batchRemove() {
      if (!this.multipleSelection.length) {
        this.$message({
          type: 'error',
          message: '请选择一条数据',
          duration: 1500,
        })
        return
      }
      this.$confirm('您确定要删除这些数据吗, 是否继续?', '提示', {
        type: 'warning'
      }).then(() => {
        batchDelete(this.modelId, this.multipleSelection).then(res => {
          this.$message({
            type: 'success',
            message: res.msg,
            duration: 1500,
            onClose: () => {
              this.initData()
            }
          })
        })
      }).catch(() => { })
    },
    download(data) {
      if (this.isPreview) return this.$message({ message: '功能预览不支持数据导出', type: 'warning' })
      const defaultSortConfig = (this.columnData.defaultSortConfig || []).map(o =>
        (o.sort === 'desc' ? '-' : '') + o.field);
      this.listQuery.sidx = defaultSortConfig.join(',')
      let query = { ...this.listQuery, ...data }
      exportModel(this.modelId, query).then(res => {
        if (!res.data.url) return
        this.jnpf.downloadFile(res.data.url)
        this.$refs.ExportBox.visible = false
        this.exportBoxVisible = false
      })
    },
    columnBtnsHandel(key, row, index) {
      if (key === 'edit') {
        this.updateHandle(row)
      }
      if (key === 'detail') {
        this.goDetail(row)
      }
      if (key == 'remove') {
        this.handleDel(row.id, index)
      }
    },
    goDetail(row) {
      if (this.config.enableFlow == 1) {
        let data = {
          id: row.id,
          flowId: row.flowId,
          type: 1,
          opType: 0,
          modelId: this.modelId,
          isPreview: this.isPreview,
        }
        this.flowVisible = true
        this.$nextTick(() => {
          this.$refs.FlowBox.init(data)
        })
      } else {
        this.detailVisible = true
        this.$nextTick(() => {
          this.$refs.Detail.init(this.formData, this.modelId, row.id, this.columnData.useFormPermission)
        })
      }
    },
    handleHeaderClass({ column }) {
      column.order = column.multiOrder
    },
    handleOrderChange(orderColumn, orderState) {
      let index = this.ordersList.findIndex(e => e.field === orderColumn);
      let sort = orderState === 'ascending' ? 'asc' : orderState === 'descending' ? 'desc' : '';
      if (index > -1) {
        this.ordersList[index].sort = orderState;
      } else {
        this.ordersList.push({ field: orderColumn, sort });
      }
      this.ordersList = this.ordersList.filter(e => e.sort);
      this.ordersList.length ? this.setDefaultQuery(this.ordersList) : this.setDefaultQuery(this.columnData.defaultSortConfig)
      this.initData()
    },
    handleTableSort({ column }) {
      if (column.sortable !== 'custom') return
      column.multiOrder = column.multiOrder === 'descending' ? 'ascending' : column.multiOrder ? '' : 'descending';
      this.handleOrderChange(column.property, column.multiOrder)
    },
    refresh(isRefresh) {
      this.formVisible = false
      this.extraFormVisible = false
      if (isRefresh) this.initData()
    },
    closeFlow(isRefresh) {
      this.flowVisible = false
      if (isRefresh) this.initData()
    },
    async reset() {
      if (this.columnData.type === 2 && this.treeActiveId) {
        let obj = { [this.columnData.treeRelation]: this.treeActiveId }
        let queryJson = this.treeActiveId ? JSON.stringify(obj) : ''
        this.search(queryJson)
      } else {
        await this.initDefaultSearchData()
        this.search(this.listQuery.queryJson)
      }
    },
    searchData(queryJson) {
      if (this.columnData.type === 2 && this.treeActiveId) {
        queryJson = queryJson ? JSON.parse(queryJson) : ''
        queryJson = { [this.columnData.treeRelation]: this.treeActiveId, ...queryJson }
        queryJson = JSON.stringify(queryJson)
      }
      this.search(queryJson)
    },
    search(queryJson) {
      if (this.isPreview) return
      if (!queryJson) this.$refs.treeBox && this.$refs.treeBox.setCurrentKey(null);
      this.listQuery.queryJson = queryJson
      this.listQuery.currentPage = 1
      this.initData()
    },
    openSuperQuery() {
      this.superQueryVisible = true
      this.$nextTick(() => {
        this.$refs.SuperQuery.init()
      })
    },
    superQuery(queryJson) {
      if (this.isPreview) return
      this.listQuery.superQueryJson = queryJson
      this.listQuery.currentPage = 1
      this.initData()
    },
    customBtnsHandel(item, row, index) {
      if (item.event.btnType == 1) this.handlePopup(item.event, row, index)
      if (item.event.btnType == 2) this.handleScriptFunc(item.event, row, index)
      if (item.event.btnType == 3) this.handleInterface(item.event, row, index)
    },
    handlePopup(item, row, index) {
      this.customBoxVisible = true
      this.$nextTick(() => {
        this.$refs.CustomBox.init(item, this.modelId, row.id, this.isPreview)
      })
    },
    handleScriptFunc(item, row, index) {
      const parameter = {
        data: row,
        index,
        refresh: this.initData,
        onlineUtils: this.jnpf.onlineUtils,
      }
      const func = this.jnpf.getScriptFunc.call(this, item.func)
      if (!func) return
      func.call(this, parameter)
    },
    handleInterface(item, row) {
      const handlerInterface = () => {
        getModelInfo(this.modelId, row.id).then(res => {
          const dataForm = res.data || {};
          if (!dataForm.data) return;
          const data = { ...JSON.parse(dataForm.data), id: row.id };
          if (item.templateJson && item.templateJson.length) {
            item.templateJson.forEach(e => {
              const value = data[e.relationField] || data[e.relationField] == 0 || data[e.relationField] == false ? data[e.relationField] : '';
              e.defaultValue = (e.relationField === '@formId' && e.sourceType == 1) ? data.id : e.sourceType == 1 ? value : e.relationField;
            });
          }
          let query = { paramList: item.templateJson || [] }
          getDataInterfaceRes(item.interfaceId, query).then(res => {
            this.$message({ message: res.msg, type: 'success' });
          })
        })
      }
      if (!item.useConfirm) return handlerInterface()
      this.$confirm(item.confirmTitle || '确认执行此操作', '提示', { type: 'warning' }).then(() => {
        handlerInterface()
      }).catch(() => { })
    },
    setTableLoadFunc() {
      const JNPFTable = this.$refs.tableRef.$refs.JNPFTable
      const parameter = {
        data: this.list,
        tableRef: JNPFTable,
        onlineUtils: this.jnpf.onlineUtils,
      }
      const func = this.jnpf.getScriptFunc.call(this, this.columnData.funcs.afterOnload)
      if (!func) return
      func.call(this, parameter)
    },
    buildOptions() {
      this.columnData.columnList.forEach(cur => {
        const config = cur.__config__
        if (dyOptionsList.indexOf(config.jnpfKey) > -1) {
          if (config.dataType === 'dictionary' && config.dictionaryType) {
            this.$store.dispatch('base/getDicDataSelector', config.dictionaryType).then(res => {
              cur.options = res
            })
          }
          if (config.dataType === 'dynamic' && config.propsUrl) {
            let query = {
              paramList: config.templateJson || [],
            }
            getDataInterfaceRes(config.propsUrl, query).then(res => {
              let data = res.data
              cur.options = Array.isArray(data) ? data : []
            })
          }
        }
      })
    },
    filterNode(value, data) {
      if (!value) return true;
      return data[this.treeProps.label].indexOf(value) !== -1;
    },
    toggleTreeExpand(expands) {
      this.keyword = ''
      this.refreshTree = false
      this.expandsTree = expands
      this.$nextTick(() => {
        this.refreshTree = true
        this.$nextTick(() => {
          this.$refs.treeBox.setCurrentKey(null)
        })
      })
    },
    toggleExpandList() {
      this.refreshTable = false;
      this.expandsTable = !this.expandsTable;
      this.$nextTick(() => {
        this.refreshTable = true;
      });
    },
    loadNode(node, resolve) {
      const nodeData = node.data
      const config = this.columnData
      if (config.treeInterfaceId) {
        if (config.treeTemplateJson && config.treeTemplateJson.length) {
          for (let i = 0; i < config.treeTemplateJson.length; i++) {
            const element = config.treeTemplateJson[i];
            element.defaultValue = nodeData[element.relationField] || ''
          }
        }
        let query = {
          paramList: config.treeTemplateJson || [],
        }
        getDataInterfaceRes(config.treeInterfaceId, query).then(res => {
          let data = res.data
          if (Array.isArray(data)) {
            resolve(data);
          } else {
            resolve([]);
          }
        })
      }
    },
    treeLoad(tree, treeNode, resolve) {
      getModelSubList(this.modelId, tree.id, this.listQuery).then(res => {
        if (res.data.list && Array.isArray(res.data.list)) {
          const list = res.data.list.map(o => ({
            ...o,
            ...this.expandObj,
            hasChildren: true
          }))
          resolve(list);
        } else {
          resolve([]);
        }
      })
    },
    getFlowList() {
      getFlowList(this.config.flowId, '1').then(res => {
        this.flowList = res.data
      })
    },
    handleRowForm(index) {
      this.extraFormVisible = true
      let list = []
      this.$nextTick(() => {
        const fields = this.columnList.filter(o => o.jnpfKey != 'table')
        fields.forEach(res => {
          if (res.jnpfKey === 'complexHeader') {
            list.push(...res.children)
          } else {
            list.push(res)
          }
        })
        list = list.map(o => {
          o.__config__.span = 24;
          o.__config__.label = o.label;
          if (o.__config__.templateJson && o.__config__.templateJson.length) {
            o.__config__.templateJson = o.__config__.templateJson.map(o => ({ ...o, relationField: '' }));
          }
          if (o.templateJson && o.templateJson.length) {
            o.templateJson = o.templateJson.map(o => ({ ...o, relationField: '' }));
          }
          return o;
        });
        const formData = { ...this.formData, fields: list }
        this.$refs.extraForm.init(formData, this.modelId, this.isPreview, this.columnData.useFormPermission, this.list[index])
      })
    },
    getFixed(item, i) {
      if (i == 0 && this.columnData.groupField && this.columnData.type == 3) {
        if (this.columnList.some(o => o.fixed == 'left')) return 'left'
        return false
      } else if (this.columnData.childTableStyle != 2) {
        return item.fixed == 'none' ? false : item.fixed
      } else {
        return false
      }
    }
  }
}
</script>
<style scoped lang="scss">
.JNPF-common-head {
  >>> .el-button span {
    margin-left: 0px !important;
  }
}
.showOverflow {
  white-space: nowrap;
  text-overflow: ellipsis;
  overflow: hidden;
}
</style>
