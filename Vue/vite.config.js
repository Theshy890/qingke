import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'
import AutoImport from 'unplugin-auto-import/vite'//自动导入vue组件
import Components from 'unplugin-vue-components/vite'//自动导入ui组件，比如element等
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'//对应组件库引入

export default defineConfig({
  plugins: [vue(),
    
    //按需导入
    AutoImport({
      resolvers: [ElementPlusResolver()],
    }),
    Components({
      //采用sass样式配置
      resolvers: [ElementPlusResolver({importStyle:"sass"})], 
    }),
  ],
  css: {
    preprocessorOptions: {
      scss: {
        additionalData: `@use "@/assets/css/index.scss" as *;`, 
      }
    }
  },
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src') 
    },
    // 解决Windows系统上的路径大小写不匹配问题
    preserveSymlinks: true
  },
  server: {
    host: 'localhost',
    port: 5173,
    open: true, // 启动后自动打开浏览器
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})