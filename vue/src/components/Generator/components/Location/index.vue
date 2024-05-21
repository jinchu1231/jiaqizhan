<template>
  <div>
    <el-button icon="icon-ym icon-ym-daka" v-if="!detailed" size="mini"
      @click="handleLocation()">{{ innerValue.fullAddress ? '重新定位' : '添加定位' }}</el-button>
    <div class="location-card" v-if="innerValue.fullAddress" @click="handleClick()">
      <div class="location-card-info">
        <img class="location-card-static-map" :src="staticMapUrl" v-if="enableLocationScope" />
        <div class="location-card-address">{{ innerValue.fullAddress }}</div>
      </div>
      <div class="el-icon-error" @click.stop="handleClear()"
        v-if="!detailed && !disabled && clearable" />
    </div>
    <el-dialog title="选择位置" :close-on-click-modal="false" append-to-body :visible.sync="visible"
      @close="handleCancel"
      class="JNPF-dialog template-dialog JNPF-dialog_center location-container-modal" lock-scroll
      width="600px" destroy-on-close>
      <div class="location-contain">
        <el-input v-model="keyword" placeholder="搜索或直接在地图上点选" @keyup.enter.native="onSearch">
          <el-button slot="append" @click="onSearch" icon="el-icon-search"></el-button>
        </el-input>
        <div class="map-contain">
          <div :id="containerId" class="map"></div>
          <img class="map-marker" src="@/assets/images/mark.png" />
        </div>
        <div class="around-contain" ref="bodyRef" v-loading="loading && listQuery.currentPage == 1">
          <el-radio-group v-model="selectValue" @change="onSelectValueChange">
            <el-radio class="around-contain-item" :label="index" v-for="(item, index) in list"
              :key="index">
              <div class="around-item-title" :title="item.name"> {{ item.name }}</div>
              <div class="around-item-sub-title"
                :title="item.address.length ? item.address : item.name">
                {{ item.address.length ? item.address : item.name }}</div>
            </el-radio>
          </el-radio-group>
          <!-- 请选择允许微调范围内的地点 -->
          <el-empty class="around-contain-empty" v-if="!list.length && !loading"
            description="暂无数据"></el-empty>
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="handleCancel">取消</el-button>
        <el-button type="primary" :loading="loading" @click="handleSubmit()">确定</el-button>
      </span>
    </el-dialog>
  </div>
</template>
<script>
import AMapLoader from '@amap/amap-jsapi-loader';
import { getAroundList, getTextList } from '@/api/common'
import define from '@/utils/define'
const defaultValue = {
  lat: 25.416422,
  lng: 118.990923,
  name: '',
  fullAddress: '',
};
window._AMapSecurityConfig = {
  securityJsCode: define.aMapSecurityJsCode, // 安全密钥
};

export default {
  name: 'JnpfLocation',
  props: {
    value: {
      type: String,
      default: ''
    },
    autoLocation: {
      type: Boolean,
      default: false
    },
    enableLocationScope: {
      type: Boolean,
      default: false
    },
    adjustmentScope: {
      type: [String, Number],
      default: 500
    },
    enableDesktopLocation: {
      type: Boolean,
      default: false
    },
    locationScope: {
      type: Array,
      default: () => []
    },
    detailed: {
      type: Boolean,
      default: false
    },
    disabled: {
      type: Boolean,
      default: false
    },
    clearable: {
      type: Boolean,
      default: true
    },
  },
  data() {
    return {
      visible: false,
      keyword: '',
      location:
      {
        lat: '',
        lng: ''
      },
      currentLocation: {
        lat: '',
        lng: ''
      },
      AMap: '',
      map: '',
      list: [],
      loading: false,
      finish: false,
      listQuery: {
        currentPage: 1,
        pageSize: 50,
      },
      selectValue: -1,
      innerValue: '',
      setCenterLoading: false,
      dragLoading: false,
      containerId: ""
    }
  },
  watch: {
    value: {
      handler(val) {
        this.innerValue = val ? JSON.parse(val) : defaultValue;
      },
      immediate: true
    },
  },
  computed: {
    staticMapUrl() {
      if (!this.enableLocationScope) return ' ';
      const location = this.innerValue.lng + ',' + this.innerValue.lat;
      const url = `${this.define.APIURl}/api/system/Location/staticmap?location=${location}&zoom=19&size=80*80&key=${this.define.aMapWebKey}`;
      return url;
    }
  },
  methods: {
    handleClick() {
      if (this.detailed) return this.openAMap();
      if (this.disabled) return;
      if (this.enableLocationScope) this.handleLocation();
    },
    openAMap() {
      const position = this.innerValue.lng + ',' + this.innerValue.lat;
      const url = `http://uri.amap.com/marker?position=${position}&name=${this.innerValue.name}&coordinate=gaode&callnative=0`;
      window.open(url, '_blank');
    },
    handleLocation() {
      if (this.disabled) return;
      this.visible = true;
      this.selectValue = -1;
      this.list = [];
      this.keyword = '';
      this.$nextTick(() => {
        this.initMap();
      });
    },
    async initMap() {
      this.containerId = 'container' + this.jnpf.idGenerator()
      AMapLoader.reset()
      await AMapLoader.load({
        key: this.define.aMapJsKey,
        version: '2.0',
        plugins: ['AMap.PlaceSearch', 'AMap.Geolocation'],
      }).then(AMap => {
        this.AMap = AMap;
        const query = {
          viewMode: '3D',
          resizeEnable: true,
          zoom: 16,
        };
        if (this.innerValue && this.innerValue.lng && this.innerValue.lat) {
          this.location = { lat: this.innerValue.lat, lng: this.innerValue.lng };
          query.center = [this.location.lng, this.location.lat];
        }
        this.map = new AMap.Map(this.containerId, query);

        //获取中心位置
        this.handleGetCenter();
        //查询附近地点
        this.getList();
        this.bindScroll();
        //添加可选区域圆形
        this.handelCircle();
        //添加微调区域圆形
        this.handleScopeCircle();
        //地图事件
        this.handleListener();
        //添加定位按钮
        this.map.addControl(new AMap.Geolocation());
      });
    },
    onSelectValueChange() {
      this.setCenterLoading = true;
      this.$nextTick(() => {
        const location = this.list[this.selectValue].location || '';
        const [lng, lat] = location.split(',');
        this.location = { lat, lng };
        this.map.setCenter([lng, lat]);
        setTimeout(() => {
          this.setCenterLoading = false;
        }, 500);
      });
    },
    handleCenter(e) {
      this.map.setCenter(e.lnglat);
    },
    handleListener() {
      this.map.on('click', this.handleCenter);
      this.map.on('moveend', this.handleMapChange);
      this.map.on('movestart', this.handleSetLoading);
    },
    handleOffListener() {
      this.map.off('click', this.handleCenter);
      this.map.off('moveend', this.handleMapChange);
      this.map.off('movestart', this.handleSetLoading);
    },
    handleMapChange() {
      if (this.dragLoading) return (this.dragLoading = false);
      this.handleGetCenter();
      this.selectValue = -1;
      this.listQuery.currentPage = 1;
      this.list = [];
      if (this.enableLocationScope && this.adjustmentScope) {
        const discount = this.getDiscount(this.currentLocation.lat, this.currentLocation.lng, this.location.lat, this.location.lng) || 0;
        if (discount > this.adjustmentScope) return this.$message.warning('超出微调范围');
      }
      this.getList();
    },
    handleSetLoading() {
      if (this.setCenterLoading) this.dragLoading = true;
    },
    handleGetCenter() {
      const { lat, lng } = this.map.getCenter();
      this.location = { lat, lng };
    },
    handleResult(res) {
      if (res.pois.length < this.listQuery.pageSize) this.finish = true;
      this.list = [...this.list, ...res.pois];
      this.loading = false;
    },
    bindScroll() {
      let _this = this,
        vBody = _this.$refs.bodyRef;
      vBody.addEventListener("scroll", function () {
        if (vBody.scrollHeight - vBody.clientHeight - vBody.scrollTop <= 50 && !_this.loading && !_this.finish) {
          _this.listQuery.currentPage += 1;
          _this.keyword ? _this.handleSearch() : _this.getList();
        }
      });
    },
    getList() {
      this.loading = true;
      const query = {
        key: this.define.aMapWebKey,
        location: this.location.lng + ',' + this.location.lat,
        radius: -1,
        offset: this.listQuery.pageSize,
        page: this.listQuery.currentPage,
      };
      getAroundList(query).then(res => {
        this.handleResult(res.data);
      }).catch(() => { this.loading = false })
    },
    handelCircle() {
      if (!this.enableDesktopLocation || !this.locationScope.length) return;
      for (let i = 0; i < this.locationScope.length; i++) {
        const o = this.locationScope[i];
        if (!o.lng || !o.lat || !o.radius) continue;
        this.addCircle({ ...o, fillColor: '#7ad98f' });
      }
    },
    handleScopeCircle() {
      if (!this.enableLocationScope || !this.adjustmentScope) return;
      this.currentLocation = this.location;
      this.addCircle({ ...this.location, radius: this.adjustmentScope, fillColor: '#1791fc' });
    },
    addCircle(o) {
      var circle = new this.AMap.Circle({
        center: [o.lng, o.lat],
        radius: o.radius,
        strokeOpacity: 0.2,
        fillOpacity: 0.4,
        fillColor: o.fillColor,
        bubble: false,
      });
      circle.on('click', this.handleCenter);
      this.map.add(circle);
    },
    handleSearch() {
      this.loading = true;
      const query = {
        key: this.define.aMapWebKey,
        keywords: this.keyword,
        radius: this.enableLocationScope && this.adjustmentScope ? this.adjustmentScope : -1,
        offset: this.listQuery.pageSize,
        page: this.listQuery.currentPage,
      };
      getTextList(query).then(res => {
        this.handleResult(res.data);
      });
    },
    onSearch() {
      this.selectValue = -1;
      this.listQuery.currentPage = 1;
      this.list = [];
      this.keyword ? this.handleSearch() : this.getList();
    },
    getDiscount(lat1, lng1, lat2, lng2) {
      const p1 = new this.AMap.LngLat(lng1, lat1);
      const p2 = new this.AMap.LngLat(lng2, lat2);
      return p1.distance(p2) || 0;
    },
    handleAutoLocation() {
      if (!this.autoLocation || this.innerValue.fullAddress) return;
      AMapLoader.load({
        key: this.define.aMapJsKey,
        version: '2.0',
        plugins: ['AMap.Geolocation', 'AMap.Geocoder'],
      }).then(AMap => {
        this.AMap = AMap;
        var geolocation = new AMap.Geolocation({
          enableHighAccuracy: true,
          timeout: 1500,
        });
        let that = this
        const getAddress = position => {
          var geocoder = new AMap.Geocoder();
          geocoder.getAddress(position, function (status, res) {
            if (status === 'complete' && res.info === 'OK') {
              const data = res.regeocode.addressComponent;
              that.innerValue = {
                pName: data.province,
                cName: data.city,
                adName: data.district,
                address: data.street + data.streetNumber,
                name: '',
                lng: position[0],
                lat: position[1],
                fullAddress: res.regeocode.formattedAddress,
              };

              that.$emit('input', JSON.stringify(that.innerValue));
              that.$emit('change', JSON.stringify(that.innerValue));
            }
          });
        };
        geolocation.getCityInfo((_status, res) => {
          if (res.status == 0) {
            if (this.enableDesktopLocation && this.locationScope.length) {
              let list = [];
              for (let i = 0; i < this.locationScope.length; i++) {
                const o = this.locationScope[i];
                const discount = this.getDiscount(o.lat, o.lng, res.position[1], res.position[0]) || 0;
                list.push(discount > o.radius);
              }
              if (list.every(o => o === true)) return;
            }
            getAddress(res.position);
          } else {
            console.error('定位失败');
          }
        });
      });
    },
    handleSubmit() {
      if ((!this.selectValue && this.selectValue != 0) || this.selectValue == -1) return this.$message.error('请选择地址');
      const data = this.list[this.selectValue];
      const [lng, lat] = data.location.split(',');
      //判断微调范围
      if (this.enableLocationScope) {
        const discount = this.getDiscount(this.currentLocation.lat, this.currentLocation.lng, lat, lng) || 0;
        if (discount > (this.adjustmentScope || 500)) return this.$message.warning('超出微调范围');
      }
      //判断可选范围
      if (this.enableDesktopLocation && this.locationScope.length) {
        let list = [];
        for (let i = 0; i < this.locationScope.length; i++) {
          const o = this.locationScope[i];
          const discount = this.getDiscount(o.lat, o.lng, lat, lng) || 0;
          list.push(discount > o.radius);
        }
        if (list.every(o => o === true)) return this.$message.warning('超出规定范围');
      }
      const address = data.address && data.address.length ? data.address : '';
      //台湾、北京、上海、重庆、深圳地址特殊处理
      let fullAddress = data.pname + data.cityname + data.adname + address + data.name;
      if (data.pname == data.cityname) fullAddress = data.pname + data.adname + address + data.name;
      if (data.pname == data.cityname && data.pname == data.adname) fullAddress = data.pname + address + data.name;
      this.innerValue = {
        pName: data.pname,
        cName: data.cityname,
        adName: data.adname,
        address,
        name: data.name,
        lng,
        lat,
        fullAddress,
      };
      this.$emit('input', JSON.stringify(this.innerValue));
      this.$emit('change', JSON.stringify(this.innerValue));
      this.handleCancel();
    },
    handleCancel() {
      this.visible = false;
      this.destroyMap()
    },
    destroyMap() {
      this.handleOffListener();
      this.$nextTick(() => {
        this.map && this.map.destroy();
      })
    },
    handleClear() {
      this.innerValue = defaultValue;
      this.$emit('input', '');
      this.$emit('change', '');
    }
  }
}

</script>

<style lang="scss" scoped>
.location-container-modal {
  >>> .el-dialog {
    .el-dialog__body {
      height: 62vh !important;
      overflow: hidden;
    }
    .location-contain {
      display: flex;
      flex-direction: column;
      height: 100%;
      padding: 0px 20px 0 20px;

      .map-contain {
        position: relative;
        .map {
          margin: 10px 0;
          width: 100%;
          height: 230px;
        }
        .map-marker {
          width: 19px;
          height: 32px;
          position: absolute;
          top: 50%;
          left: 50%;
          transform: translate(-50%, calc(-50% - 9.5px));
        }
      }

      .around-contain {
        width: 100%;
        height: 100%;
        overflow: auto;
        .el-radio-group {
          width: 100% !important;
        }
        .around-contain-item {
          width: 100%;
          padding: 8px;
          border-bottom: 1px solid #f2f2f6;
          display: flex;
          height: 60px;
          line-height: 22px;
          align-items: center;
          width: 100%;
          .around-item-title {
            color: #171a1d;
            width: 512px;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }
          .around-item-sub-title {
            color: #b9babb;
            width: 512px;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }
        }
        .around-contain-empty {
          height: 90%;
        }
      }
    }
  }
}
.location-card {
  display: flex;
  align-items: center;
  margin-top: 8px;
  background: #f2f2f6;
  padding: 12px;
  border-radius: 8px;
  justify-content: space-between;
  cursor: pointer;
  .location-card-info {
    flex: 1;
    display: flex;
    align-items: center;
    .location-card-static-map {
      width: 48px;
      height: 48px;
      margin-right: 4px;
    }
    .location-card-address {
      line-height: 1.5;
      padding: 0 4px;
      word-break: normal;
      white-space: normal;
    }
  }
  .location-card-actions {
    color: rgb(135, 143, 149);
    cursor: pointer;
    flex-shrink: 0;
  }
}
</style>