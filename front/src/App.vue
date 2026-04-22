<template>
  <!-- 页面就像幻灯片，都在这里切换 -->
  <router-view />
</template>

<style>
/* 全局样式 */
html, body {
  margin: 0;
  padding: 0;
  height: 100%;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', Arial, sans-serif;
  background-color: #f2f3f5;
}
#app {
  height: 100%;
}
</style>