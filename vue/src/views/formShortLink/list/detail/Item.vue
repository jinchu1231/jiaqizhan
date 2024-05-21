<template>
  <el-col :span="item.__config__.span"
    :class="{'item-card':item.__config__.jnpfKey==='card','item-table':item.__config__.jnpfKey==='table'}"
    v-if="!item.__config__.noShow && (!item.__config__.visibility || (Array.isArray(item.__config__.visibility) && item.__config__.visibility.includes('pc')))">
    <template v-if="item.__config__.layout==='colFormItem'">
      <template v-if="item.__config__.jnpfKey==='divider'">
        <el-divider :content-position="item.contentPosition">{{item.content}}
        </el-divider>
      </template>
      <template v-else-if="item.__config__.jnpfKey==='text'">
        <jnpf-form-tip-item label-width="0"
          :tipLabel="item.__config__.label?item.__config__.tipLabel:''">
          <JnpfText :content="item.content" :textStyle="item.textStyle" />
        </jnpf-form-tip-item>
      </template>
      <template v-else-if="item.__config__.jnpfKey==='link'">
        <jnpf-form-tip-item label-width="0"
          :tipLabel="item.__config__.label?item.__config__.tipLabel:''">
          <jnpf-link :content="item.content" :href="item.href" :target="item.target"
            :textStyle="item.textStyle" />
        </jnpf-form-tip-item>
      </template>
      <template v-else-if="item.__config__.jnpfKey==='alert'">
        <jnpf-form-tip-item label-width="0"
          :tipLabel="item.__config__.label?item.__config__.tipLabel:''">
          <el-alert :title="item.title" :type="item.type" :closable="item.closable"
            :description="item.description" :show-icon="item.showIcon" style="word-break: break-all"
            :closeText="item.closeText" />
        </jnpf-form-tip-item>
      </template>
      <template v-else-if="item.__config__.jnpfKey==='groupTitle'">
        <jnpf-form-tip-item label-width="0">
          <JnpfGroupTitle :content="item.content" :tipLabel="item.tipLabel"
            :content-position="item.contentPosition" />
        </jnpf-form-tip-item>
      </template>
      <template v-else-if="item.__config__.jnpfKey==='button'">
        <jnpf-form-tip-item label-width="0"
          :tipLabel="item.__config__.label?item.__config__.tipLabel:''">
          <jnpf-button :align="item.align" :buttonText="item.buttonText" :type="item.type"
            :disabled="item.disabled" />
        </jnpf-form-tip-item>
      </template>
      <template v-else>
        <jnpf-form-tip-item :prop="item.__vModel__"
          :label-width="item.__config__.showLabel?`${item.__config__.labelWidth||formConf.labelWidth}px`:'0'"
          :label="item.__config__.showLabel ? item.__config__.label+ (formConf.labelSuffix || '') : '' "
          :tipLabel="item.__config__.label&&item.__config__.showLabel?item.__config__.tipLabel:''">
          <template v-if="item.__config__.jnpfKey==='uploadFile'">
            <JnpfUploadFile v-model="item.__config__.defaultValue" detailed disabled />
          </template>
          <template v-else-if="item.__config__.jnpfKey==='uploadImg'">
            <el-image :src="define.comUrl+cItem.url" class="dy-img"
              v-for="(cItem,ci) in item.__config__.defaultValue" :key="ci"
              :preview-src-list="getImgList(item.__config__.defaultValue)" :z-index="10000">
            </el-image>
          </template>
          <template v-else-if="item.__config__.jnpfKey==='colorPicker'">
            <el-color-picker v-model="item.__config__.defaultValue" :show-alpha="item.showAlpha"
              :color-format="item.colorFormat" :size="item.size" disabled />
          </template>
          <template v-else-if="item.__config__.jnpfKey==='rate'">
            <el-rate v-model="item.__config__.defaultValue" :max="item.count"
              :allow-half="item.allowHalf" :show-text="item.showText" :show-score="item.showScore"
              disabled />
          </template>
          <template v-else-if="item.__config__.jnpfKey==='slider'">
            <div class="slider-box">
              <el-slider v-model="item.__config__.defaultValue" :range='item.range'
                :show-stops="item.showStops" />
              <div class="slider-mark"></div>
            </div>
          </template>
          <template v-else-if="item.__config__.jnpfKey==='editor'">
            <div v-html="item.__config__.defaultValue"></div>
          </template>
          <template v-else-if="item.__config__.jnpfKey==='relationForm'">
            <a @click="toDetail(item)" style="color:#1890ff">{{ item.name }}</a>
          </template>
          <template v-else-if="item.__config__.jnpfKey==='popupSelect'">
            <p>{{ item.name }}</p>
          </template>
          <template v-else-if="item.__config__.jnpfKey==='barcode'">
            <jnpf-barcode :format="item.format" :lineColor="item.lineColor"
              :background="item.background" :width="item.width" :height="item.height"
              :staticText="item.staticText" :dataType="item.dataType"
              :relationField="item.relationField+'_id'" :formData="formValue"></jnpf-barcode>
          </template>
          <template v-else-if="item.__config__.jnpfKey==='qrcode'">
            <jnpf-qrcode :format="item.format" :colorLight="item.colorLight"
              :colorDark="item.colorDark" :width="item.width" :staticText="item.staticText"
              :dataType="item.dataType" :relationField="item.relationField+'_id'"
              :formData="formValue"></jnpf-qrcode>
          </template>
          <template v-else-if="item.__config__.jnpfKey==='calculate'&&item.isStorage==0">
            <JnpfCalculate :expression="item.expression" :isStorage="0" :formData="formValue"
              :isAmountChinese="item.isAmountChinese" :thousands="item.thousands"
              :precision="item.precision" :detailed="true" />
          </template>
          <template v-else-if="item.__config__.jnpfKey==='inputNumber'">
            <JnpfNumber v-model="item.__config__.defaultValue" :thousands="item.thousands"
              :isAmountChinese="item.isAmountChinese" :addonBefore="item.addonBefore"
              :addonAfter="item.addonAfter" :precision="item.precision"></JnpfNumber>
          </template>
          <template v-else-if="item.__config__.jnpfKey === 'input'">
            <jnpf-input v-model="item.__config__.defaultValue" :addonBefore="item.addonBefore"
              class="jnpf-detail-text" :addonAfter="item.addonAfter" :useMask="item.useMask"
              :maskConfig="item.maskConfig" detailed />
          </template>
          <template v-else-if="item.__config__.jnpfKey === 'sign'">
            <JnpfSign v-model="item.__config__.defaultValue" detailed />
          </template>
          <template v-else-if="item.__config__.jnpfKey === 'iframe'">
            <JnpfIframe :href='item.href' :height='item.height' :borderType='item.borderType'
              :borderColor='item.borderColor' :borderWidth='item.borderWidth' />
          </template>
          <template v-else>
            <p class="jnpf-detail-text">
              <span v-if="item.addonBefore">{{item.addonBefore}}</span>{{getValue(item)}}<span
                v-if="item.addonAfter">{{item.addonAfter}}</span>
            </p>
            <p v-if="item.isAmountChinese" class="isAmountChinese jnpf-detail-text">
              {{ amountChinese(item.__config__.defaultValue) }}</p>
          </template>
        </jnpf-form-tip-item>
      </template>
    </template>
    <template v-else>
      <template v-if="item.__config__.jnpfKey==='card'">
        <el-card :shadow="item.shadow" :header="item.header" class="mb-20">
          <template slot="header">
            <span slot="label" v-if="item.__config__.tipLabel && item.header">{{item.header}}
              <el-tooltip placement="top" :content=item.__config__.tipLabel>
                <a class='el-icon-question tooltip-question'></a>
              </el-tooltip>
            </span>
            <span v-else>{{ item.header}}</span>
          </template>
          <el-row>
            <Item v-for="(childItem, childIndex) in item.__config__.children" :key="childIndex"
              :item="childItem" v-bind="$props" v-on="$listeners" />
          </el-row>
        </el-card>
      </template>
      <template v-if="item.__config__.jnpfKey==='row'">
        <el-row :gutter="formConf.gutter">
          <Item v-for="(childItem, childIndex) in item.__config__.children" :key="childIndex"
            :item="childItem" v-bind="$props" v-on="$listeners" />
        </el-row>
      </template>
      <template v-if="item.__config__.jnpfKey==='table'">
        <jnpf-form-tip-item label-width="0" v-if="!item.__config__.noShow">
          <div class="JNPF-common-title" v-if="item.__config__.showTitle">
            <span slot="label"
              v-if="item.__config__.tipLabel && item.__config__.label">{{item.__config__.label}}
              <el-tooltip placement="top" :content=item.__config__.tipLabel>
                <a class='el-icon-question tooltip-question'></a>
              </el-tooltip>
            </span>
            <h2 v-else>{{item.__config__.label}}</h2>
          </div>
          <el-table :data="item.__config__.defaultValue"
            :showSummary="!!item.__config__.defaultValue.length && !!item.showSummary && (item.summaryField && !!item.summaryField.length)"
            :summary-method="getSummaries">
            <el-table-column prop="index" type="index" width="50" label="序号" align="center"
              fixed="left" />
            <template v-for="(item,index) in complexHeaderList">
              <template v-if="item.jnpfKey==='complexHeader'">
                <el-table-column :key="index" :label="item.label" :align="item.align">
                  <template v-for="(column,columnIndex) in item.children">
                    <template>
                      <el-table-column :key="columnIndex" :prop="column.__vModel__"
                        :label="column.__config__.label" :align="item.align"
                        v-if="column.__config__.jnpfKey==='relationForm'">
                        <template slot="header">
                          <span slot="label"
                            v-if="column.__config__.tipLabel &&column.__config__.label">{{column.__config__['label']}}
                            <el-tooltip placement="top" :content=column.__config__.tipLabel>
                              <a class='el-icon-question tooltip-question'></a>
                            </el-tooltip>
                          </span>
                          <span v-else>{{ column.__config__['label'] }}</span>
                        </template>
                        <template slot-scope="scope">
                          <el-link :underline="false" type="primary"
                            @click.native="toTableDetail(column,scope.row[column.__vModel__+'_id'])">
                            {{ scope.row[column.__vModel__] }}</el-link>
                        </template>
                      </el-table-column>
                      <el-table-column :key="columnIndex" :label="column.__config__.label"
                        :prop="column.__vModel__" :align="item.align"
                        v-else-if="['relationFormAttr','popupAttr'].includes(column.__config__.jnpfKey)">
                        <template slot="header">
                          <span slot="label"
                            v-if="column.__config__.tipLabel &&column.__config__.label">{{column.__config__['label']}}
                            <el-tooltip placement="top" :content=column.__config__.tipLabel>
                              <a class='el-icon-question tooltip-question'></a>
                            </el-tooltip>
                          </span>
                          <span v-else>{{ column.__config__['label'] }}</span>
                        </template>
                        <template slot-scope="scope">
                          <p v-if="!column.__vModel__">
                            {{ scope.row[column.relationField.split('_jnpfTable_')[0]+'_'+column.showField] }}
                          </p>
                          <p v-else>
                            {{scope.row[column.__vModel__]}}
                          </p>
                        </template>
                      </el-table-column>
                      <el-table-column :key="columnIndex" :label="column.__config__.label"
                        :prop="column.__vModel__" min-width="250px" :align="item.align"
                        v-else-if="column.__config__.jnpfKey==='uploadFile'">
                        <template slot="header">
                          <span slot="label"
                            v-if="column.__config__.tipLabel &&column.__config__.label">{{column.__config__['label']}}
                            <el-tooltip placement="top" :content=column.__config__.tipLabel>
                              <a class='el-icon-question tooltip-question'></a>
                            </el-tooltip>
                          </span>
                          <span v-else>{{ column.__config__['label'] }}</span>
                        </template>
                        <template slot-scope="scope">
                          <JnpfUploadFile v-model="scope.row[column.__vModel__]" detailed
                            disabled />
                        </template>
                      </el-table-column>
                      <el-table-column :key="columnIndex" :label="column.__config__.label"
                        :prop="column.__vModel__" :align="item.align"
                        v-else-if="column.__config__.jnpfKey==='calculate' &&column.isStorage==0">
                        <template slot="header">
                          <span slot="label"
                            v-if="column.__config__.tipLabel &&column.__config__.label">{{column.__config__['label']}}
                            <el-tooltip placement="top" :content=column.__config__.tipLabel>
                              <a class='el-icon-question tooltip-question'></a>
                            </el-tooltip>
                          </span>
                          <span v-else>{{ column.__config__['label'] }}</span>
                        </template>
                        <template slot-scope="scope">
                          <JnpfCalculate :rowIndex="scope.$index" :expression="column.expression"
                            :isStorage="0" :formData="formValue"
                            :isAmountChinese="column.isAmountChinese" :thousands="column.thousands"
                            :precision="column.precision" :detailed="true" />
                        </template>
                      </el-table-column>
                      <el-table-column :key="columnIndex" :label="column.__config__.label"
                        :align="item.align" :prop="column.__vModel__"
                        v-else-if="column.__config__.jnpfKey==='uploadImg'">
                        <template slot="header">
                          <span slot="label"
                            v-if="column.__config__.tipLabel &&column.__config__.label">{{column.__config__['label']}}
                            <el-tooltip placement="top" :content=column.__config__.tipLabel>
                              <a class='el-icon-question tooltip-question'></a>
                            </el-tooltip>
                          </span>
                          <span v-else>{{ column.__config__['label'] }}</span>
                        </template>
                        <template slot-scope="scope">
                          <JnpfUploadImg v-model="scope.row[column.__vModel__]" detailed disabled />
                        </template>
                      </el-table-column>
                      <el-table-column :key="columnIndex" :label="column.__config__.label"
                        :prop="column.__vModel__" :align="item.align"
                        v-else-if="column.__config__.jnpfKey==='inputNumber'">
                        <template slot="header">
                          <span slot="label"
                            v-if="column.__config__.tipLabel &&column.__config__.label">{{column.__config__['label']}}
                            <el-tooltip placement="top" :content=column.__config__.tipLabel>
                              <a class='el-icon-question tooltip-question'></a>
                            </el-tooltip>
                          </span>
                          <span v-else>{{ column.__config__['label'] }}</span>
                        </template>
                        <template slot-scope="scope">
                          <JnpfNumber v-model="scope.row[column.__vModel__]"
                            :thousands="column.thousands" :isAmountChinese="column.isAmountChinese"
                            :addonBefore="column.addonBefore" :addonAfter="column.addonAfter"
                            :precision="column.precision"></JnpfNumber>
                        </template>
                      </el-table-column>
                      <el-table-column :key="columnIndex" :label="column.__config__.label"
                        :prop="column.__vModel__" :align="item.align"
                        v-else-if="column.__config__.jnpfKey==='sign'">
                        <template slot="header">
                          <span slot="label"
                            v-if="column.__config__.tipLabel &&column.__config__.label">{{column.__config__['label']}}
                            <el-tooltip placement="top" :content=column.__config__.tipLabel>
                              <a class='el-icon-question tooltip-question'></a>
                            </el-tooltip>
                          </span>
                          <span v-else>{{ column.__config__['label'] }}</span>
                        </template>
                        <template slot-scope="scope">
                          <JnpfSign v-model="scope.row[column.__vModel__]" detailed />
                        </template>
                      </el-table-column>
                      <el-table-column :key="columnIndex" :label="column.__config__.label"
                        :prop="column.__vModel__" :align="item.align"
                        v-else-if="column.__config__.jnpfKey==='rate'">
                        <template slot="header">
                          <span slot="label"
                            v-if="column.__config__.tipLabel &&column.__config__.label">{{column.__config__['label']}}
                            <el-tooltip placement="top" :content=column.__config__.tipLabel>
                              <a class='el-icon-question tooltip-question'></a>
                            </el-tooltip>
                          </span>
                          <span v-else>{{ column.__config__['label'] }}</span>
                        </template>
                        <template slot-scope="scope">
                          <JnpfRate v-model="scope.row[column.__vModel__]" detailed />
                        </template>
                      </el-table-column>
                      <el-table-column :key="columnIndex" :label="column.__config__.label"
                        :prop="column.__vModel__" :align="item.align"
                        v-else-if="column.__config__.jnpfKey==='slider'">
                        <template slot="header">
                          <span slot="label"
                            v-if="column.__config__.tipLabel &&column.__config__.label">{{column.__config__['label']}}
                            <el-tooltip placement="top" :content=column.__config__.tipLabel>
                              <a class='el-icon-question tooltip-question'></a>
                            </el-tooltip>
                          </span>
                          <span v-else>{{ column.__config__['label'] }}</span>
                        </template>
                        <template slot-scope="scope">
                          <JnpfSlider v-model="scope.row[column.__vModel__]" detailed />
                        </template>
                      </el-table-column>
                      <el-table-column :key="columnIndex" :label="column.__config__.label"
                        :align="column.align" :prop="column.__vModel__"
                        v-else-if="column.__config__.jnpfKey==='input'">
                        <template slot="header">
                          <span slot="label"
                            v-if="column.__config__.tipLabel &&column.__config__.label">{{column.__config__['label']}}
                            <el-tooltip placement="top" :content=column.__config__.tipLabel>
                              <a class='el-icon-question tooltip-question'></a>
                            </el-tooltip>
                          </span>
                          <span v-else>{{ column.__config__['label'] }}</span>
                        </template>
                        <template slot-scope="scope">
                          <jnpf-input v-model="scope.row[column.__vModel__]"
                            class="jnpf-detail-text" :addonBefore="column.addonBefore"
                            :addonAfter="column.addonAfter" :useMask="column.useMask"
                            :maskConfig="column.maskConfig" detailed />
                        </template>
                      </el-table-column>
                      <el-table-column :key="columnIndex" :prop="column.__vModel__"
                        :label="column.__config__.label" :align="item.align" v-else>
                        <template slot="header">
                          <span slot="label"
                            v-if="column.__config__.tipLabel &&column.__config__.label">{{column.__config__['label']}}
                            <el-tooltip placement="top" :content=column.__config__.tipLabel>
                              <a class='el-icon-question tooltip-question'></a>
                            </el-tooltip>
                          </span>
                          <span v-else>{{ column.__config__['label'] }}</span>
                        </template>
                        <template slot-scope="scope">
                          <span
                            v-if="column.addonBefore">{{column.addonBefore}}</span>{{getThousandsValue(column,scope.row[column.__vModel__])}}<span
                            v-if="column.addonAfter">{{column.addonAfter}}</span>
                          <p v-if="column.isAmountChinese" class="isAmountChinese jnpf-detail-text">
                            {{ amountChinese(scope.row[column.__vModel__]) }}</p>
                        </template>
                      </el-table-column>
                    </template>
                  </template>
                </el-table-column>
              </template>
            </template>
            <template v-for="(column,columnIndex) in item.__config__.children">
              <template
                v-if="!column.__config__.noShow && (!column.__config__.visibility || (Array.isArray(column.__config__.visibility) && column.__config__.visibility.includes('pc')))">
                <template v-if="!childColumns.includes(column.__vModel__)">
                  <el-table-column :key="columnIndex" :prop="column.__vModel__"
                    :label="column.__config__.label" :align="column.__config__.tableAlign"
                    :fixed="column.__config__.tableFixed!='none'?column.__config__.tableFixed:false"
                    v-if="column.__config__.jnpfKey==='relationForm'">
                    <template slot="header">
                      <span slot="label"
                        v-if="column.__config__.tipLabel &&column.__config__.label">{{column.__config__['label']}}
                        <el-tooltip placement="top" :content=column.__config__.tipLabel>
                          <a class='el-icon-question tooltip-question'></a>
                        </el-tooltip>
                      </span>
                      <span v-else>{{ column.__config__['label'] }}</span>
                    </template>
                    <template slot-scope="scope">
                      <el-link :underline="false" type="primary"
                        @click.native="toTableDetail(column,scope.row[column.__vModel__+'_id'])">
                        {{ scope.row[column.__vModel__] }}</el-link>
                    </template>
                  </el-table-column>
                  <el-table-column :key="columnIndex" :label="column.__config__.label"
                    :prop="column.__vModel__" :align="column.__config__.tableAlign"
                    :fixed="column.__config__.tableFixed!='none'?column.__config__.tableFixed:false"
                    v-else-if="['relationFormAttr','popupAttr'].includes(column.__config__.jnpfKey)">
                    <template slot="header">
                      <span slot="label"
                        v-if="column.__config__.tipLabel &&column.__config__.label">{{column.__config__['label']}}
                        <el-tooltip placement="top" :content=column.__config__.tipLabel>
                          <a class='el-icon-question tooltip-question'></a>
                        </el-tooltip>
                      </span>
                      <span v-else>{{ column.__config__['label'] }}</span>
                    </template>
                    <template slot-scope="scope">
                      <p v-if="!column.__vModel__">
                        {{ scope.row[column.relationField.split('_jnpfTable_')[0]+'_'+column.showField] }}
                      </p>
                      <p v-else>
                        {{scope.row[column.__vModel__]}}
                      </p>
                    </template>
                  </el-table-column>
                  <el-table-column :key="columnIndex" :label="column.__config__.label"
                    :prop="column.__vModel__" min-width="250px"
                    :align="column.__config__.tableAlign"
                    :fixed="column.__config__.tableFixed!='none'?column.__config__.tableFixed:false"
                    v-else-if="column.__config__.jnpfKey==='uploadFile'">
                    <template slot="header">
                      <span slot="label"
                        v-if="column.__config__.tipLabel &&column.__config__.label">{{column.__config__['label']}}
                        <el-tooltip placement="top" :content=column.__config__.tipLabel>
                          <a class='el-icon-question tooltip-question'></a>
                        </el-tooltip>
                      </span>
                      <span v-else>{{ column.__config__['label'] }}</span>
                    </template>
                    <template slot-scope="scope">
                      <JnpfUploadFile v-model="scope.row[column.__vModel__]" detailed disabled />
                    </template>
                  </el-table-column>
                  <el-table-column :key="columnIndex" :label="column.__config__.label"
                    :prop="column.__vModel__" :align="column.__config__.tableAlign"
                    :fixed="column.__config__.tableFixed!='none'?column.__config__.tableFixed:false"
                    v-else-if="column.__config__.jnpfKey==='calculate' &&column.isStorage==0">
                    <template slot="header">
                      <span slot="label"
                        v-if="column.__config__.tipLabel &&column.__config__.label">{{column.__config__['label']}}
                        <el-tooltip placement="top" :content=column.__config__.tipLabel>
                          <a class='el-icon-question tooltip-question'></a>
                        </el-tooltip>
                      </span>
                      <span v-else>{{ column.__config__['label'] }}</span>
                    </template>
                    <template slot-scope="scope">
                      <JnpfCalculate :rowIndex="scope.$index" :expression="column.expression"
                        :isStorage="0" :formData="formValue"
                        :isAmountChinese="column.isAmountChinese" :thousands="column.thousands"
                        :precision="column.precision" :detailed="true" />
                    </template>
                  </el-table-column>
                  <el-table-column :key="columnIndex" :label="column.__config__.label"
                    :align="column.__config__.tableAlign"
                    :fixed="column.__config__.tableFixed!='none'?column.__config__.tableFixed:false"
                    :prop="column.__vModel__" v-else-if="column.__config__.jnpfKey==='uploadImg'">
                    <template slot="header">
                      <span slot="label"
                        v-if="column.__config__.tipLabel &&column.__config__.label">{{column.__config__['label']}}
                        <el-tooltip placement="top" :content=column.__config__.tipLabel>
                          <a class='el-icon-question tooltip-question'></a>
                        </el-tooltip>
                      </span>
                      <span v-else>{{ column.__config__['label'] }}</span>
                    </template>
                    <template slot-scope="scope">
                      <JnpfUploadImg v-model="scope.row[column.__vModel__]" detailed disabled />
                    </template>
                  </el-table-column>
                  <el-table-column :key="columnIndex" :label="column.__config__.label"
                    :align="column.__config__.tableAlign"
                    :fixed="column.__config__.tableFixed!='none'?column.__config__.tableFixed:false"
                    :prop="column.__vModel__" v-else-if="column.__config__.jnpfKey==='inputNumber'">
                    <template slot="header">
                      <span slot="label"
                        v-if="column.__config__.tipLabel &&column.__config__.label">{{column.__config__['label']}}
                        <el-tooltip placement="top" :content=column.__config__.tipLabel>
                          <a class='el-icon-question tooltip-question'></a>
                        </el-tooltip>
                      </span>
                      <span v-else>{{ column.__config__['label'] }}</span>
                    </template>
                    <template slot-scope="scope">
                      <JnpfNumber v-model="scope.row[column.__vModel__]"
                        :thousands="column.thousands" :isAmountChinese="column.isAmountChinese"
                        :addonBefore="column.addonBefore" :addonAfter="column.addonAfter"
                        :precision="column.precision"></JnpfNumber>
                    </template>
                  </el-table-column>
                  <el-table-column :key="columnIndex" :label="column.__config__.label"
                    :prop="column.__vModel__" :align="column.__config__.tableAlign"
                    :fixed="column.__config__.tableFixed!='none'?column.__config__.tableFixed:false"
                    v-else-if="column.__config__.jnpfKey==='input'">
                    <template slot="header">
                      <span slot="label"
                        v-if="column.__config__.tipLabel &&column.__config__.label">{{column.__config__['label']}}
                        <el-tooltip placement="top" :content=column.__config__.tipLabel>
                          <a class='el-icon-question tooltip-question'></a>
                        </el-tooltip>
                      </span>
                      <span v-else>{{ column.__config__['label'] }}
                      </span>
                    </template>
                    <template slot-scope="scope">
                      <jnpf-input v-model="scope.row[column.__vModel__]" class="jnpf-detail-text"
                        :addonBefore="column.addonBefore" :addonAfter="column.addonAfter"
                        :useMask="column.useMask" :maskConfig="column.maskConfig" detailed />
                    </template>
                  </el-table-column>
                  <el-table-column :key="columnIndex" :label="column.__config__.label"
                    :align="column.__config__.tableAlign"
                    :fixed="column.__config__.tableFixed!='none'?column.__config__.tableFixed:false"
                    :prop="column.__vModel__" v-else-if="column.__config__.jnpfKey==='sign'">
                    <template slot="header">
                      <span slot="label"
                        v-if="column.__config__.tipLabel &&column.__config__.label">{{column.__config__['label']}}
                        <el-tooltip placement="top" :content='column.__config__.tipLabel'>
                          <a class='el-icon-question tooltip-question'></a>
                        </el-tooltip>
                      </span>
                      <span v-else>{{ column.__config__['label'] }}</span>
                    </template>
                    <template slot-scope="scope">
                      <JnpfSign v-model="scope.row[column.__vModel__]" detailed />
                    </template>
                  </el-table-column>
                  <el-table-column :key="columnIndex" :label="column.__config__.label"
                    :align="column.__config__.tableAlign"
                    :fixed="column.__config__.tableFixed!='none'?column.__config__.tableFixed:false"
                    :prop="column.__vModel__" v-else-if="column.__config__.jnpfKey==='rate'">
                    <template slot="header">
                      <span slot="label"
                        v-if="column.__config__.tipLabel &&column.__config__.label">{{column.__config__['label']}}
                        <el-tooltip placement="top" :content='column.__config__.tipLabel'>
                          <a class='el-icon-question tooltip-question'></a>
                        </el-tooltip>
                      </span>
                      <span v-else>{{ column.__config__['label'] }}</span>
                    </template>
                    <template slot-scope="scope">
                      <JnpfRate v-model="scope.row[column.__vModel__]" detailed />
                    </template>
                  </el-table-column>
                  <el-table-column :key="columnIndex" :label="column.__config__.label"
                    :align="column.__config__.tableAlign"
                    :fixed="column.__config__.tableFixed!='none'?column.__config__.tableFixed:false"
                    :prop="column.__vModel__" v-else-if="column.__config__.jnpfKey==='slider'">
                    <template slot="header">
                      <span slot="label"
                        v-if="column.__config__.tipLabel &&column.__config__.label">{{column.__config__['label']}}
                        <el-tooltip placement="top" :content='column.__config__.tipLabel'>
                          <a class='el-icon-question tooltip-question'></a>
                        </el-tooltip>
                      </span>
                      <span v-else>{{ column.__config__['label'] }}</span>
                    </template>
                    <template slot-scope="scope">
                      <JnpfSlider v-model="scope.row[column.__vModel__]" detailed />
                    </template>
                  </el-table-column>
                  <el-table-column :key="columnIndex" :prop="column.__vModel__"
                    :align="column.__config__.tableAlign"
                    :fixed="column.__config__.tableFixed!='none'?column.__config__.tableFixed:false"
                    :label="column.__config__.label" v-else>
                    <template slot="header">
                      <span slot="label"
                        v-if="column.__config__.tipLabel &&column.__config__.label">{{column.__config__['label']}}
                        <el-tooltip placement="top" :content=column.__config__.tipLabel>
                          <a class='el-icon-question tooltip-question'></a>
                        </el-tooltip>
                      </span>
                      <span v-else>{{ column.__config__['label'] }}</span>
                    </template>
                    <template slot-scope="scope">
                      <span
                        v-if="column.addonBefore">{{column.addonBefore}}</span>{{getThousandsValue(column,scope.row[column.__vModel__])}}<span
                        v-if="column.addonAfter">{{column.addonAfter}}</span>
                      <p v-if="column.isAmountChinese" class="isAmountChinese jnpf-detail-text">
                        {{ amountChinese(scope.row[column.__vModel__]) }}</p>
                    </template>
                  </el-table-column>
                </template>
              </template>
            </template>
          </el-table>
        </jnpf-form-tip-item>
      </template>
      <template v-if="item.__config__.jnpfKey==='tab'">
        <el-tabs :type="item.type" :tab-position="item.tabPosition" v-model="item.__config__.active"
          class="mb-10">
          <el-tab-pane v-for="(pane,i) in item.__config__.children" :key="i" :label="pane.title"
            :name="pane.name">
            <el-row>
              <Item v-for="(childItem, childIndex) in pane.__config__.children" :key="childIndex"
                :item="childItem" v-bind="$props" v-on="$listeners" />
            </el-row>
          </el-tab-pane>
        </el-tabs>
      </template>
      <template v-if="item.__config__.jnpfKey==='collapse'">
        <el-collapse :accordion="item.accordion" v-model="item.__config__.active" class="mb-20">
          <el-collapse-item v-for="(pane,i) in item.__config__.children" :key="i"
            :title="pane.title" :name="pane.name">
            <el-row>
              <Item v-for="(childItem, childIndex) in pane.__config__.children" :key="childIndex"
                :item="childItem" v-bind="$props" v-on="$listeners" />
            </el-row>
          </el-collapse-item>
        </el-collapse>
      </template>
      <template v-if="item.__config__.jnpfKey==='tableGrid'">
        <table class="table-grid-box"
          :style="{'--borderType':item.__config__.borderType,'--borderColor':item.__config__.borderColor,'--borderWidth':item.__config__.borderWidth+ 'px'}">
          <tbody>
            <tr v-for="child,index in item.__config__.children" :key="index">
              <td v-for="(it,i) in child.__config__.children" :key="i"
                :colspan="it.__config__.colspan" :rowspan="it.__config__.rowspan"
                v-show="!it.__config__.merged" :style="{
                  '--backgroundColor': it.__config__.backgroundColor,
                }">
                <Item v-for="(childItem, childIndex) in it.__config__.children" :key="childIndex"
                  :item="childItem" v-bind="$props" v-on="$listeners" />
              </td>
            </tr>
          </tbody>
        </table>
      </template>
    </template>
  </el-col>
</template>
<script>
import { getDownloadUrl } from '@/api/common'
import { getAmountChinese, thousandsFormat } from '@/components/Generator/utils/index.js'

export default {
  name: 'Item',
  props: {
    item: {
      type: Object,
      required: true
    },
    formConf: {
      type: Object,
      required: true
    },
    formValue: {
      type: Object,
    },
  },
  data() {
    return {
      complexHeaderList: [],
      childColumns: [],
    }
  },
  created() {
    if (this.item.__config__.jnpfKey === 'table') {
      this.item.__config__.children = this.item.__config__.children.filter(column => !column.__config__.noShow && (!column.__config__.visibility || (Array.isArray(column.__config__.visibility) && column.__config__.visibility.includes('pc'))))
      this.complexHeaderList = this.getComplexColumns(this.item.__config__.children)
    }
  },
  methods: {
    toDetail(item) {
      this.$emit('toDetail', item)
    },
    toTableDetail(item, value) {
      item.__config__.defaultValue = value
      this.$emit('toDetail', item)
    },
    downloadFile(file) {
      if (!file.fileId) return
      getDownloadUrl('annex', file.fileId).then(res => {
        this.jnpf.downloadFile(res.data.url, file.name)
      })
    },
    getImgList(list) {
      const newList = list.map(o => this.define.comUrl + o.url)
      return newList
    },
    getValue(item) {
      if (Array.isArray(item.__config__.defaultValue)) {
        if (['timeRange', 'dateRange'].includes(item.__config__.jnpfKey)) {
          return item.__config__.defaultValue.join('')
        }
        return item.__config__.defaultValue.join()
      }
      if (item.thousands) return thousandsFormat(item.__config__.defaultValue)
      return item.__config__.defaultValue
    },
    amountChinese(val) {
      return getAmountChinese(val)
    },
    getThousandsValue(item, val) {
      if (item.thousands) return thousandsFormat(val)
      return val
    },
    getSummaries(param) {
      const summaryField = this.item.summaryField
      const thousandsField = []
      this.item.__config__.children.forEach(res => {
        if (res.thousands) thousandsField.push(res.__vModel__)
      })
      const { columns, data } = param;
      const sums = [];
      columns.forEach((column, index) => {
        if (index === 0) {
          sums[index] = '合计';
          return;
        }
        if (!summaryField.includes(column.property)) {
          sums[index] = '';
          return;
        }
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
      });
      return sums
    },
    getComplexColumns(columns) {
      let complexHeaderList = this.item.__config__.complexHeaderList || [];
      if (!complexHeaderList.length) return columns;
      let childColumns = [];
      for (let i = 0; i < complexHeaderList.length; i++) {
        const e = complexHeaderList[i];
        e.align = e.align;
        e.children = [];
        e.label = e.fullName
        e.jnpfKey = 'complexHeader'
        if (e.childColumns && e.childColumns.length) {
          e.childColumns = e.childColumns.filter(o => columns.some(item => item.__config__.tableFixed == 'none' && item.__vModel__ == o))
          childColumns.push(...e.childColumns);
          for (let j = 0; j < columns.length; j++) {
            const o = columns[j];
            if (e.childColumns.includes(o.__vModel__)) e.children.push({ ...o, align: o.__config__.tableAlign });
          }
        }
      }
      complexHeaderList = complexHeaderList.filter(o => o.children.length);
      // this.complexHeaderList = complexHeaderList
      this.childColumns = childColumns
      return complexHeaderList;
    },
  }
}
</script>
