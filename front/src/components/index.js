export default {
  install(app) {
    console.log('🔧 AI审核插件安装中...');
    
    if (typeof window === 'undefined') {
      return;
    }
    
    setTimeout(async () => {
      try {
        // 检查是否在审核页面
        const isAuditPage = window.location.pathname.includes('/admin/audit');
        
        if (!isAuditPage) {
          return;
        }
        
        // 动态导入组件
        const module = await import('./AiAuditSidebar.vue');
        const AiAuditSidebar = module.default;
        
        // 创建挂载点
        const mountPoint = document.createElement('div');
        mountPoint.id = 'ai-audit-sidebar-container';
        document.body.appendChild(mountPoint);
        
        // 创建Vue应用
        const Vue = await import('vue');
        const sidebarApp = Vue.createApp(AiAuditSidebar);
        
        // 使用Element Plus
        const ElementPlus = await import('element-plus');
        sidebarApp.use(ElementPlus.default);
        
        // 挂载
        sidebarApp.mount('#ai-audit-sidebar-container');
        console.log('✅ AI侧边栏加载成功');
        
      } catch (error) {
        console.error('❌ AI侧边栏加载失败:', error);
      }
    }, 1000);
  }
}
