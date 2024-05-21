import { mapGetters } from "vuex";
import QRCode from "qrcodejs2";
import JsBarcode from "jsbarcode";
import request from "@/utils/request";
import jnpf from "@/utils/jnpf";
import dayjs from 'dayjs'
import { getAmountChinese } from "@/components/Generator/utils/index"
import { getData } from "@/api/system/printDev";
import { getBatchData } from "@/api/system/printDev";
const printOptionApi = {
  computed: {
    ...mapGetters(['userInfo']),
    ...mapGetters(['dictionaryList'])
  },
  props: ["id", "formId", "fullName", "batchIds", "printTitle"],
  data() {
    return {
      data: {},
      batchData: [],
      printTemplate: "",
      recordList: [],
      loading: false,
    }
  },
  methods: {
    async initData(tag) {
      if (!this.id) return;
      await this.$store.dispatch('base/getDictionaryAll');
      this.data = {};
      this.batchData = [];
      this.printTemplate = "";
      this.loading = true;
      let query = {
        id: this.id,
        formId: this.formId,
      };
      let getDataApi = getData
      if (tag == 'batch') {
        this.pageIndex = 0;
        query.formId = this.batchIds
        getDataApi = getBatchData
      }
      getDataApi(query).then((res) => {
        if (!res.data) return;
        let array = tag == 'batch' ? res.data : [res.data]
        for (let index = 0; index < array.length; index++) {
          const element = array[index];
          this.batchData.push(element.printTemplate);
        }
        this.$nextTick(async () => {
          let dom = this.$refs["tsPrint"];
          for (let index = 0; index < array.length; index++) {
            const element = array[index];
            let domCurrent = dom.querySelectorAll(".print-content")[index];
            if (!element.printData) {
              this.batchData[index] = domCurrent.innerHTML;
            }
            await this.handleData(element, domCurrent);
            this.$set(this.batchData, index, this.printTemplate)
          }
        });
        this.loading = false;
      });
    },
    async handleData(data, domCurrent) {
      this.data = data.printData
      this.recordList = data.operatorRecordList || []
      this.printTemplate = domCurrent.innerHTML
      this.replaceSysValue()
      this.createTable(domCurrent, false)
      this.createTable(domCurrent, true)
      this.replaceContent(domCurrent, 'p')
      this.replaceCommonValue()
      this.replaceAmountThousands()
      this.$nextTick(() => {
        this.replaceEmptyTag()
        this.collapseBorder()
      })
      const pageBreak = '<p style="page-break-after:always;"></p>'
      this.printTemplate = this.printTemplate.replace('<p><!-- pagebreak --></p>', pageBreak)
    },
    replaceEmptyTag() {
      let barEmpty = /&lt;barCode(\S|\s)*?\/barCode&gt;/g
      let list = this.printTemplate.match(barEmpty) || []
      list.forEach(element => {
        this.replaceValue(element, '')
      });
      let qrEmpty = /&lt;qrCode(\S|\s)*?\/qrCode&gt;/g
      let list2 = this.printTemplate.match(qrEmpty) || []
      list2.forEach(element => {
        this.replaceValue(element, '')
      });
      let imgEmpty = /&lt;img(\S|\s)*?\/img&gt;/g
      let list3 = this.printTemplate.match(imgEmpty) || []
      list3.forEach(element => {
        this.replaceValue(element, '')
      });
    },
    // 当table是100%,且外层是td时合并border
    collapseBorder() {
      let strList = this.printTemplate.match(/<td.*>\n<table style="border-collapse: collapse; width: 100%; height: 100%;"/g)
      if (strList && strList.length > 0) {
        strList.forEach(str => {
          let newStr = str.substring(0, str.length - 1) + ' border-style:hidden;"'
          this.replaceValue(str, newStr)
        })
      }
    },
    getTdTrueValue(text) {
      if (!text.match(/tablekey="([^"]*)"/)) return
      let info = text.match(/tablekey="([^"]*)"/)[1]
      let dataTag = info.split('.')[0]
      let line = info.split('.')[1]
      let field = text.match(/{([^}]*)}/)[1]
      let value = ''
      if (dataTag == 'headTable') {
        value = this.data[field] || ''
      } else {
        value = this.data[dataTag][line][field] || ''
      }
      return value
    },
    getTrueValue(text) {
      let info = text.match(/data-tag="([^"]*)"/)[1];
      let dataTag = info.split('.')[0];
      let field = text.match(/{([^}]*)}/)[1];
      if (dataTag == 'headTable') return this.data[field] || '';
      let data = this.data[dataTag] || '';
      if (!data) return '';
      let val = '';
      for (const item of data) {
        val += item[field] || '' + '-';
      }
      return val;
    },
    // 替换普通值
    replaceCommonValue() {
      this.$nextTick(() => {
        let spanList = this.printTemplate.match(/<span class="wk-print-tag-wukong.*?[^}]}.*?<\/span>/g)
        if (!spanList) return
        for (let index = 0; index < spanList.length; index++) {
          const element = spanList[index];
          if (element.includes('{') && element.includes('data-tag')) {
            let value = this.getTrueValue(element)
            let spanText = element.match(/<span class="wk-print-tag-wukong.*?[^}]}.*?<\/span>/);
            if (spanText && spanText.length > 0) {
              spanText = spanText[0]
              let fieldStr = spanText.match(/{.*}/g)
              let newStr = spanText.replace(fieldStr[0], value)
              this.replaceValue(spanText, newStr)
            }
          }
        }
      })
    },
    replaceAmountThousands() {
      const list = this.printTemplate.match(/大写金额\((.*?)\)/g) || [];

      for (let i = 0; i < list.length; i++) {
        const element = list[i];
        let value = element.match(/大写金额\((.*?)\)/)[1];
        if (isNaN(Number(value))) continue
        this.replaceValue(element, getAmountChinese(value));
      }
      const list_ = this.printTemplate.match(/千位分隔符\((.*?)\)/g) || [];
      for (let i = 0; i < list_.length; i++) {
        const element = list_[i];
        let data = element.match(/千位分隔符\((.*?)\)/);
        let arr = data && data[1].split(',');
        let value = arr[0] ? arr[0] : '';
        if (isNaN(Number(value))) continue
        let place = arr[1] ? arr[1] : 0;
        this.replaceValue(element, this.getThousands(value, place));
      }
    },
    replaceContent(domCurrent, tag) {
      let getTrueValue = tag == 'td' ? this.getTdTrueValue : this.getTrueValue
      let domList = domCurrent.querySelectorAll(tag)
      for (const dom of domList) {
        try {
          let pcontent = dom.outerHTML
          if (pcontent.includes('{') && pcontent.includes('data-tag')) {
            // 替换千分符
            if (pcontent.includes('千位分隔符(')) {
              let text = pcontent.match(/千位分隔符\(\<span[^>]+"[^<]+\>[^)]+\)/)[0];
              let place = 0
              if (text.includes(`</span>,`)) place = text.split('</span>,')[1].replace(')', '') || 0
              let value = getTrueValue(tag == 'td' ? pcontent : text)
              let transValue = this.getThousands(value, place)
              this.replaceValue(text, transValue)
              continue
            }
            // 替换数字金额
            if (pcontent.includes('大写金额(')) {
              let text = pcontent.match(/大写金额\(\<span[^>]+"[^<]+\>[^)]+\)/)[0];
              let value = getTrueValue(tag == 'td' ? pcontent : text)
              let transValue = getAmountChinese(value)
              this.replaceValue(text, transValue)
              continue
            }
            //日期选择
            if (pcontent.includes('日期选择(')) {
              let text = pcontent.match(/日期选择\(\<span[^>]+"[^<]+\>[^)]+\)/)[0];
              let value = getTrueValue(tag == 'td' ? pcontent : text)
              let format = text.includes(`</span>,`) ? text.split('</span>,')[1].replace(')', '') || '' : '';
              format = format.replaceAll('"', '').replaceAll("'", '');
              this.replaceValue(text, this.getDateValue(value, format))
              continue
            }
            // 数据字典
            if (pcontent.includes('数据字典(')) {
              let text = pcontent.match(/数据字典\(\<span[^>]+"[^<]+\>[^)]+\)/)[0];
              let value = getTrueValue(tag == 'td' ? pcontent : text)
              let id = text.includes(`</span>,`) ? text.split('</span>,')[1].replace(')', '') || '' : '';
              id = id.replaceAll('"', '').replaceAll("'", '');
              this.replaceValue(text, this.getDictionaryValue(value, id))
              continue
            }
            // 替换图片
            if (pcontent.includes('&lt;img')) {
              let value = getTrueValue(pcontent)
              if (!value) {
                let cloneNode = dom.cloneNode(true)
                const height = this.getWidthHeight(dom.innerHTML, 'height')
                cloneNode.height = height
                cloneNode.innerText = ''
                this.replaceValue(pcontent, cloneNode.outerHTML)
                continue
              }
              this.replaceImage(dom, value || '[]')
              continue
            }
            if (pcontent.includes('&lt;qrCode')) {
              let value = getTrueValue(pcontent)
              if (!value) continue
              if (value.trim() == '') {
                let cloneNode = dom.cloneNode(true)
                cloneNode.innerText = ''
                this.replaceValue(pcontent, cloneNode.outerHTML)
                continue
              }
              this.replaceQrCode(dom, value)
              continue
            }
            if (pcontent.includes('&lt;barCode')) {
              let value = getTrueValue(pcontent)
              if (!value) return
              if (value.trim() == '') {
                this.replaceValue(dom.innerHTML, '')
                continue
              }
              this.replaceBarCode(dom, value)
              continue
            }
            setTimeout(() => {
              if (tag == 'td') {
                let value = getTrueValue(pcontent)
                let spanText = pcontent.match(/<span class="wk-print-tag-wukong.*?[^}]}.*?<\/span>/);
                if (spanText && spanText.length > 0) {
                  spanText = spanText[0]
                  let fieldStr = spanText.match(/{.*}/g)
                  let newStr = spanText.replace(fieldStr[0], value)
                  this.replaceValue(spanText, newStr)
                }
              }
            }, 0);
          } else {
            if (pcontent.includes('千位分隔符(')) {
              let data = pcontent.match(/千位分隔符\((.*?)\)/)
              let arr = data && data[1].split(',')
              let value = arr[0] ? arr[0] : ''
              if (isNaN(Number(value))) continue
              let place = arr[1] ? arr[1] : 0
              let transValue = this.getThousands(value, place)
              this.replaceValue(data, transValue)
              continue
            }
            if (pcontent.includes('大写金额(')) {
              let text = pcontent.match(/大写金额\((.*?)\)/) && pcontent.match(/大写金额\((.*?)\)/)[0];
              let value = pcontent.match(/大写金额\((.*?)\)/) && pcontent.match(/大写金额\((.*?)\)/)[1];
              if (!value || isNaN(Number(value))) continue
              let transValue = getAmountChinese(value)
              this.replaceValue(text, transValue)
              continue
            }
            // 替换二维码
            if (pcontent.includes('&lt;qrCode')) {
              let value = pcontent.match(/&gt;(.*?)&lt;/)[1]
              this.replaceQrCode(dom, value)
              continue
            }
            if (pcontent.includes('&lt;barCode')) {
              let value = pcontent.match(/&gt;(.*?)&lt;/)[1]
              this.replaceBarCode(dom, value)
              continue
            }
            if (pcontent.includes('&lt;img')) {
              let value = pcontent.match(/&gt;(http.*)&lt;/)[1]
              this.replaceImage(dom, JSON.stringify([{ url: value }]))
            }
          }
        } catch (error) {
          continue
        }
      }
    },
    /**
     * 创建表格行
     * @param {*} table 表格dom
     * @param {*} tableKey 表格的key
     * @param {*} index 记录下标
     * @param {*} removeTitleTr 是否移除标题行
     */
    createTr(table, tableKey, index, removeTitleTr, tableBody) {
      let cloneTable = table.cloneNode(true);
      let trs = cloneTable.querySelectorAll('tr')
      for (const tr of trs) {
        tr.style.height = '17px'
        if (tr.innerText.trim() == '') {
          tableBody.appendChild(tr)
          continue
        }
        if (removeTitleTr && !tr.innerHTML.includes('{')) {
          continue
        }
        let tds = tr.querySelectorAll('td')
        for (const td of tds) {
          if (td.innerHTML.includes('{')) {
            td.setAttribute('tableKey', tableKey + "." + index)
          }
        }
        tableBody.appendChild(tr)
      }
      return cloneTable
    },
    /**
     * 创建表格
     * @param {*} domCurrent
     */
    createTable(domCurrent, jump) {
      let tableList = domCurrent.querySelectorAll('table')
      for (let j = 0; j < tableList.length; j++) {
        let table = tableList[j];
        let tdMode = table.parentElement.tagName === 'TD'
        if ((tdMode && jump == false) || jump) {
          let tableWrap = document.createElement('table');
          let tableBody = tableWrap.appendChild(document.createElement('tbody'))
          let tableKey = this.getTableKey(table)
          let data = this.data[tableKey] || ''
          if (data && Array.isArray(data)) {
            // 子表
            let tableSize = (data && Array.isArray(data)) ? data.length : 0
            for (let index = 0; index < tableSize; index++) {
              if (index == 0) {
                this.createTr(table, tableKey, index, false, tableBody)
              } else {
                this.createTr(table, tableKey, index, true, tableBody)
              }
            }
          } else {
            this.createTr(table, 'headTable', '', false, tableBody)
          }
          this.replaceValue(table.innerHTML, tableWrap.innerHTML)
          this.replaceContent(tableWrap, 'td')
        }
      }
    },
    /**
     * 替换图片
     * @param {*} dom
     * @param {*} data 替换的值
     */
    replaceImage(dom, data) {
      if (!data) return
      let list = JSON.parse(data)
      if (JSON.stringify(list) == '[]') {
        let replaceDom = dom.cloneNode(true);
        replaceDom.innerHTML = ''
        this.replaceValue(dom.innerHTML, replaceDom.innerHTML)
      }
      let template = ''
      const width = this.getWidthHeight(dom.innerHTML)
      const height = this.getWidthHeight(dom.innerHTML, 'height')
      for (let index = 0; index < list.length; index++) {
        const element = list[index];
        if (element.url) {
          let value = element.url.includes('http') ? element.url : this.define.comUrl + element.url
          template += `<img style="width:${width}px;height:${height}px;display:inline-block;" src='${value}'/>`;
        }
      }
      let replaceDom = dom.cloneNode(true);
      replaceDom.innerHTML = template
      this.replaceValue(dom.innerHTML, replaceDom.innerHTML)
    },
    /**
     * 替换二维码
     * @param {*} dom
     * @param {*} value
     */
    replaceQrCode(dom, value) {
      let imgRegular = /&lt;qrCode(\S|\s)*?&lt;\/qrCode&gt;/g
      let imgList = dom.innerHTML.match(imgRegular)
      if (imgList && imgList.length) {
        for (var i = 0; i < imgList.length; i++) {
          const item = imgList[i]
          let width = this.getWidthHeight(item)
          const height = this.getWidthHeight(item, 'height')
          const id = this.jnpf.idGenerator()
          const imgdom = document.createElement('img');
          imgdom.width = width
          imgdom.height = height
          imgdom.id = id
          this.$nextTick(() => {
            let qrcode = new QRCode(imgdom, {
              width: width ? width : 265,
              height: height ? height : 265, // 高度
              text: value, // 二维码内容
              correctLevel: QRCode.CorrectLevel.H //容错级别 容错级别有：（1）QRCode.CorrectLevel.L （2）QRCode.CorrectLevel.M （3）QRCode.CorrectLevel.Q （4）QRCode.CorrectLevel.H
            })
            let canvas = qrcode._el.querySelector("canvas");//获取生成二维码中的canvas，并将canvas转换成base64
            let url = canvas.toDataURL("image/png");
            let template = `<img style="width:${width}px;height:${height}px;display:inline-block;" src=${url} />`;
            this.replaceValue(dom.innerHTML, template);
          })
        }
      }
    },
    /**
     * 替换条码
     * @param {*} dom
     * @param {*} value
     */
    replaceBarCode(dom, value) {
      let imgRegular = /&lt;barCode(\S|\s)*?&lt;\/barCode&gt;/g
      let imgList = dom.innerHTML.match(imgRegular)
      if (imgList && imgList.length) {
        for (var i = 0; i < imgList.length; i++) {
          const item = imgList[i]
          const width = this.getWidthHeight(item)
          const height = this.getWidthHeight(item, 'height')
          const id = this.jnpf.idGenerator()
          const imgdom = document.createElement('img');
          imgdom.width = width
          imgdom.height = height
          imgdom.id = 'barcode' + id
          let domContainer = this.$refs.barcodewrap
          domContainer.appendChild(imgdom)
          this.$nextTick(() => {
            JsBarcode('#' + imgdom.id, value, {
              width: width ? width : 4,
              height: height ? height : 80,
              displayValue: false
            });
            let imgDom = document.querySelector(`#barcode${id}`)
            domContainer.removeChild(imgdom)
            this.replaceValue(dom.innerHTML, imgDom.outerHTML)
          })
        }
      }
    },
    replaceValue(key, value) {
      this.printTemplate = this.printTemplate.replace(key, value)
    },
    getTableKey(dom) {
      let tdDoms = dom.querySelectorAll('span')
      for (let tdDom of tdDoms) {
        let dataTag = tdDom.getAttribute('data-tag')
        if (['thousands', 'isAmountChinese', 'jnpfDictionary', 'jnpfDate'].includes(dataTag)) {
          tdDom = tdDom.querySelector('span')
          dataTag = tdDom && tdDom.getAttribute('data-tag')
        }
        if (dataTag) {
          return dataTag.split(".")[0]
        }
      }
      return ''
    },
    replaceSysValue() {
      const recordList = this.recordList
      const systemPrinter = this.userInfo.userName + '/' + this.userInfo.userAccount
      const systemPrintTime = this.jnpf.toDate(new Date())
      let systemApprovalContent = ''
      if (recordList.length) {
        systemApprovalContent += '<table style="border-collapse: collapse; width: 100%;" border="1" data-mce-style="border-collapse: collapse; width: 100%;"><tbody><tr><td style="width:20%;" data-mce-style="width: 20%;">节点名称</td><td style="width:20%;" data-mce-style="width: 20%;">操作人员</td><td style="width:20%;" data-mce-style="width: 20%;">操作时间</td><td style="width:20%;" data-mce-style="width: 20%;">执行动作</td><td style="width: 20%;" data-mce-style="width: 20%;">备注</td></tr>'
        let content = ''
        for (let i = 0; i < recordList.length; i++) {
          const record = recordList[i];
          content += `<tr><td style="width: 20%;" data-mce-style="width: 20%;"><span class="wk-print-tag-wukong wk-tiny-color--common" contenteditable="false">${record.nodeName}</span></td><td style="width: 20%;" data-mce-style="width: 20%;"><span class="wk-print-tag-wukong wk-tiny-color--common" contenteditable="false">${record.userName}</span></td><td style="width: 20%;" data-mce-style="width: 20%;"><span class="wk-print-tag-wukong wk-tiny-color--common" contenteditable="false">${this.jnpf.toDate(record.handleTime)}</span></td><td style="width: 20%;" data-mce-style="width: 20%;"><span class="wk-print-tag-wukong wk-tiny-color--common" contenteditable="false">${this.getHandleName(record.handleStatus)}</span></td><td style="width: 20%;" data-mce-style="width: 20%;"><span class="wk-print-tag-wukong wk-tiny-color--common" contenteditable="false">${record.handleOpinion || ""}</span></td></tr>`
        }
        systemApprovalContent += content
        systemApprovalContent += '</tbody></table>'
      }
      this.printTemplate = this.printTemplate.replaceAll('{systemPrinter}', systemPrinter)
      this.printTemplate = this.printTemplate.replaceAll('{systemPrintTime}', systemPrintTime)
      this.printTemplate = this.printTemplate.replaceAll('{systemApprovalContent}', systemApprovalContent)
    },
    getThousands(value, place) {
      if (!value && value !== 0) return ''
      place = place ? place : this.getPlace(value)
      value = Number(value).toFixed(place)
      const numArr = value.toString().split('.');
      numArr[0] = numArr[0].replace(/\B(?=(\d{3})+(?!\d))/g, ',');
      numArr[1] = numArr[1] ? numArr[1].toString().substring(0, place) : ''
      return numArr[1] ? numArr.join('.') : numArr[0];
    },
    getPlace(value) {
      if (!value || value.toString().indexOf(".") == -1) return 0
      var index = value.toString().indexOf(".") + 1;
      var count = value.toString().length - index;
      return count
    },
    getWidthHeight(item, type = 'width') {
      let regular = ""
      if (type == 'width') regular = /width=[\"|'](.*?)[\"|']/gi;
      if (type == 'height') regular = /height=[\"|'](.*?)[\"|']/gi;
      let quotes = /["|'](.*)["|']/;
      let data = item.match(regular)
      let value = ""
      if (data && data.length) {
        let res = data[0].match(quotes)
        value = res && res.length ? res[1] : 100
      }
      return value
    },
    getJsBarcode(value, id, width, height) {
      if (!value) return
      JsBarcode(id, value, {
        width: width ? width : 4,
        height: height ? height : 80,
        displayValue: false
      });
    },
    getHandleName(handleStatus) {
      if (handleStatus == 0) return "退回"
      if (handleStatus == 1) return "通过"
      if (handleStatus == 2) return "发起"
      if (handleStatus == 3) return "撤回"
      if (handleStatus == 4) return "终止"
      if (handleStatus == 5) return "指派"
      if (handleStatus == 6) return "加签"
      if (handleStatus == 7) return "转审"
      if (handleStatus == 8) return "变更"
      if (handleStatus == 9) return "复活"
      return ''
    },
    word() {
      let print = this.$refs.tsPrint.innerHTML
      const blob = new Blob([print], {
        type: ''
      })
      const name = this.fullName ? `${this.fullName}.doc` : '下载文档.doc'
      this.downloadFile(blob, name)
    },
    downloadFile(data, name, type) {
      let blob = new Blob([data], {
        type: type || ''
      })
      let downloadElement = document.createElement('a')
      let href = window.URL.createObjectURL(blob)
      downloadElement.href = href
      downloadElement.download = name
      document.body.appendChild(downloadElement)
      downloadElement.click()
      document.body.removeChild(downloadElement)
      window.URL.revokeObjectURL(href)
    },
    print(tag) {
      let print = this.$refs.tsPrint.innerHTML
      print = print + `<style>html * {word-break:break-all}</style>`
      let iframe = document.createElement('IFRAME');
      document.body.appendChild(iframe);
      iframe.setAttribute("style", "position:absolute;width:0px;height:0px;left:-500px;top:-500px;");
      iframe.contentWindow.focus();
      let doc = iframe.contentWindow.document
      let _this = this
      iframe.onload = function () {
        let oldTitle = document.title;
        iframe.contentWindow.onafterprint = function (e) {
          let title = oldTitle.split('-')[0]
          let data = {
            printTitle: _this.fullName ? _this.fullName : title,
            printNum: tag == 'batch' ? _this.batchIds.split(",").length : 1,
            printId: _this.id
          }
          request({
            url: `/api/system/printLog/save`,
            method: "post",
            data
          }).then((res) => {
          });
        };
        document.title = "JNPF快速开发平台";
        iframe.contentWindow.print();
        document.title = oldTitle;
        setTimeout(() => {
          document.body.removeChild(iframe);
        }, 0);
      }
      doc.write(print);
      doc.close();
    },
    closeDialog() {
      this.$emit('update:visible', false)
    },
    getDateValue(value, format) {
      if (!value) return '';
      return dayjs(value).format(jnpf.getDateFormat(format) || 'YYYY-MM-DD HH:mm:ss');
    },
    getDictionaryValue(value, id) {
      if (!value || !id) return '';
      const list = this.getDicDataSelector(id)
      if (!list.length) return '';
      try {
        let val = JSON.parse(value)
        if (Array.isArray(val)) {
          let txt = []
          for (let i = 0; i < list.length; i++) {
            for (let j = 0; j < val.length; j++) {
              if (list[i].id == val[j]) txt.push(list[i].fullName)
            }
          }
          return txt.join(',')
        } else {
          const arr = list.filter(o => o.id == value);
          return arr.length ? arr[0].fullName : '';
        }
      } catch {
        const arr = list.filter(o => o.id == value);
        return arr.length ? arr[0].fullName : '';
      }
    },
    getDicDataSelector(id) {
      const list = this.dictionaryList;
      let arr = list.filter(o => o.id === id);
      if (!arr.length) return [];
      if (!arr[0].isTree) return arr[0].dictionaryList;
      let rowData = [];
      const findData = list => {
        for (let i = 0; i < list.length; i++) {
          const e = list[i];
          rowData.push(e);
          if (e.children && e.children.length) findData(e.children);
        }
      };
      findData(arr[0].dictionaryList);
      return rowData;
    }
  }
}
export default printOptionApi;
