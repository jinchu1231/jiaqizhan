<template>
  <div class="app-container map-container">
    <div id="allmap" style="width: 100%;height: 100% ;"></div>
  </div>
</template>

<script>
import AMapLoader from '@amap/amap-jsapi-loader';

export default {
  name: 'extend-map',
  data() {
    return {
      key: ''
    }
  },
  mounted() {
    this.init()
  },
  methods: {
    init() {
      AMapLoader.load({
        key: this.define.aMapJsKey,
        version: '2.0',
      }).then(AMap => {
        const query = {
          viewMode: '3D',
          zoom: 11,
          center: [116.397428, 39.90923],
        };
        new AMap.Map('allmap', query);
      });
    }
  }
}
</script>
<style lang="scss" scoped>
.map-container {
  padding: 0;
}
</style>
