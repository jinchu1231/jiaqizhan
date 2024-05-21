<template>
  <el-dialog :title="innerValue.id?'编辑':'新建'" :close-on-click-modal="false" append-to-body
    :visible.sync="visible"
    class="JNPF-dialog template-dialog JNPF-dialog_center map-center-select-modal" lock-scroll
    width="600px" destroy-on-close>
    <div class="map-contain">
      <div id="container" class="map"></div>
      <el-autocomplete class="search-contain" v-model="keyword" placeholder="输入搜索或直接在下图选择定位中心"
        :fetch-suggestions="onSearch" @select="onSelect">
        <template slot-scope="{ item }">
          <div class="sale-order-popper-item">
            <span @click="onSelect(item)"> {{ item.name }}</span>
          </div>
        </template>
      </el-autocomplete>
    </div>
    <div class="label-contain">
      <span class="label">定位范围</span>
      <span class="tips">以定位中心为圆心设置定位半径，建议范围500-1000米</span>
    </div>
    <el-input type='number' class="w-200" v-model="innerValue.radius" :min="1"
      @change="handleNumber">
      <template slot="append">米</template>
    </el-input>
    <span slot="footer" class="dialog-footer">
      <el-button @click="handleCancel">取消</el-button>
      <el-button type="primary" @click="handleSubmit()">确定</el-button>
    </span>
  </el-dialog>
</template>
<script>
import AMapLoader from '@amap/amap-jsapi-loader';
import { getInputTips } from '@/api/common'
export default {
  components: {},
  data() {
    return {
      visible: false,
      AMap: null,
      map: null,
      innerValue: {
        lng: '',
        lat: '',
        name: '',
        radius: 500,
      },
      options: [],
      marker: undefined,
      keyword: ''
    }
  },
  methods: {
    init(data) {
      this.visible = true
      this.keyword = ''
      this.innerValue = {
        lng: '',
        lat: '',
        radius: 500,
      };
      if (data.item) {
        this.innerValue = data.item;
        this.keyword = this.innerValue.name
      }
      this.$nextTick(() => {
        this.initMap();
      })
    },
    initMap() {
      AMapLoader.reset()
      AMapLoader.load({
        key: this.define.aMapJsKey,
        version: '2.0',
        plugins: ['AMap.PlaceSearch', 'AMap.Geolocation', 'AMap.Geocoder'],
      }).then((AMap) => {
        this.AMap = AMap;
        const query = {
          viewMode: '3D',
          zoom: 16,
        };
        const isEdit = this.innerValue && this.innerValue.lng && this.innerValue.lat;
        if (isEdit) query.center = [this.innerValue.lng, this.innerValue.lat];
        this.map = new AMap.Map('container', query);
        if (isEdit) this.handleMarker(this.innerValue.lng, this.innerValue.lat);
        this.map.on('click', this.handleCenter);
      });
    },
    handleCenter(e) {
      const { lng, lat } = e.lnglat;
      this.handleMarker(lng, lat);
      this.handleGetAddress([lng, lat]);
    },
    onSearch(searchText, cb) {
      const query = {
        key: this.define.aMapWebKey,
        keywords: searchText,
      };
      getInputTips(query).then((res) => {
        this.options = res.data.tips || [];
        cb(this.options)
      });
    },
    onSelect(item) {
      this.keyword = item.name;
      if (!item.location || !item.location.length) {
        this.handleGetLngLat(item.name);
      } else {
        const [lng, lat] = item.location.split(',');
        this.handleMarker(lng, lat);
      }
    },
    handleMarker(lng, lat) {
      this.innerValue.lng = lng;
      this.innerValue.lat = lat;
      this.marker && this.map.remove(this.marker);
      this.marker = new this.AMap.Marker({
        icon: 'https://webapi.amap.com/theme/v1.3/markers/n/mark_b.png',
        position: [lng, lat],
        offset: new this.AMap.Pixel(-12, -30),
      });
      this.map.add(this.marker);
      this.map.setFitView();
    },
    handleGetAddress(position) {
      var geocoder = new this.AMap.Geocoder();
      let that = this
      geocoder.getAddress(position, function (status, res) {
        if (status === 'complete' && res.info === 'OK') {
          that.keyword = res.regeocode.formattedAddress;
          that.$forceUpdate()
        }
      });
    },
    handleGetLngLat(address) {
      var geocoder = new this.AMap.Geocoder();
      let that = this
      geocoder.getLocation(address, function (status, res) {
        if (status === 'complete' && res.info === 'OK') {
          const { lng, lat } = res.geocodes[0].location;
          that.handleMarker(lng, lat);
        }
      });
    },
    handleSubmit() {
      this.innerValue.name = this.keyword
      if (!this.innerValue.lat || !this.innerValue.lng) return this.$message.error('请输入定位中心');
      if (!this.innerValue.radius) return this.$message.warning('定位范围不能为空');
      this.$emit('confirm', this.innerValue);
      this.handleCancel()
    },
    onDropdownVisibleChange(e) {
      if (e) options = [];
    },
    handleCancel() {
      this.visible = false
      this.$nextTick(() => {
        this.map && this.map.destroy();
      })
    },
    handleNumber(val) {
      this.$nextTick(() => { this.innerValue.radius = val && val > 0 ? Number(val) : 1 })
    },
  }
}
</script>
<style lang="scss" scoped>
.map-center-select-modal {
  >>> .el-dialog {
    .el-dialog__body {
      height: 45vh !important;
      padding: 10px !important;
      overflow: hidden;
    }
  }
  .map-contain {
    position: relative;
    .map {
      width: 100%;
      height: 300px;
    }
    .search-contain {
      width: 300px;
      position: absolute;
      top: 10px;
      left: 10px;
    }
  }
  .label-contain {
    margin: 15px 0 10px;
    .require {
      color: #ff5b5b;
      padding-right: 2px;
    }
    .tips {
      padding-left: 5px;
      color: #909399;
    }
  }
  .w-200 {
    width: 200px !important;
  }
}
</style>
