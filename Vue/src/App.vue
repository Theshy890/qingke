<template>
  <div id="app">
    <!-- 简单的布局，直接显示路由视图 -->
    <router-view />
  </div>
</template>

<script>
import store from './store'

export default {
  name: 'App',
  computed: {
    isLoggedIn() {
      return store.isLoggedIn
    }
  }
}
</script>

<style>
/* 全局样式重置 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #333;
}

#app {
  min-height: 100vh;
}
</style>