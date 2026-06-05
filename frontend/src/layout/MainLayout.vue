<template>
  <el-container style="height: 100vh">
    <!-- 侧边栏（PC） -->
    <el-aside v-if="!isMobile" :width="isCollapse ? '64px' : '200px'" style="transition: width 0.3s; background: #304156">
      <div style="height: 50px; display: flex; align-items: center; justify-content: center; color: #fff; font-size: 18px; font-weight: bold; white-space: nowrap; overflow: hidden">
        <span v-if="!isCollapse">e学网+</span>
        <span v-else>e+</span>
      </div>
      <el-menu
        :default-active="$route.path"
        :collapse="isCollapse"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
        router
      >
        <el-menu-item index="/course">
          <el-icon><Reading /></el-icon>
          <template #title>课程中心</template>
        </el-menu-item>
        <el-menu-item v-if="hasPermission('course:upload')" index="/course/upload">
          <el-icon><Upload /></el-icon>
          <template #title>上传课程</template>
        </el-menu-item>
        <el-menu-item index="/task">
          <el-icon><List /></el-icon>
          <template #title>我的任务</template>
        </el-menu-item>
        <el-menu-item index="/exam">
          <el-icon><EditPen /></el-icon>
          <template #title>在线考试</template>
        </el-menu-item>
        <el-menu-item index="/community">
          <el-icon><ChatDotSquare /></el-icon>
          <template #title>学习社区</template>
        </el-menu-item>
        <el-menu-item index="/notification">
          <el-icon><Bell /></el-icon>
          <template #title>
            消息通知
            <el-badge v-if="unreadCount > 0" :value="unreadCount" :max="99" style="margin-left: 6px" />
          </template>
        </el-menu-item>
        <el-menu-item index="/permission/apply">
          <el-icon><Key /></el-icon>
          <template #title>权限申请</template>
        </el-menu-item>
        <el-menu-item v-if="hasPermission('permission:approve')" index="/permission/review">
          <el-icon><Stamp /></el-icon>
          <template #title>权限审批</template>
        </el-menu-item>
        <el-menu-item index="/profile">
          <el-icon><User /></el-icon>
          <template #title>个人中心</template>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <!-- 顶栏 -->
      <el-header :class="['app-header', { 'app-header--mobile': isMobile }]">
        <div style="display: flex; align-items: center; gap: 12px">
          <el-icon v-if="!isMobile" style="cursor: pointer; font-size: 20px" @click="isCollapse = !isCollapse">
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
          <span v-if="isMobile" style="font-weight: bold; color: #409EFF; font-size: 16px">e学网+</span>
          <el-breadcrumb v-if="!isMobile" separator="/">
            <el-breadcrumb-item>{{ $route.meta.title || '首页' }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div style="display: flex; align-items: center; gap: 8px">
          <el-badge v-if="isMobile && unreadCount > 0" :value="unreadCount" :max="99" style="margin-right: 4px">
            <el-icon :size="18" style="cursor: pointer" @click="$router.push('/notification')"><Bell /></el-icon>
          </el-badge>
          <el-dropdown trigger="click">
            <span style="cursor: pointer; display: flex; align-items: center; gap: 6px">
              <el-avatar :size="isMobile ? 26 : 28" style="background: #409EFF">{{ userInfo.realName?.[0] || 'U' }}</el-avatar>
              <span v-if="!isMobile">{{ userInfo.realName || '用户' }}</span>
              <el-tag v-if="!isMobile && roleLabel()" size="small" :type="userRoles.includes('admin') || userRoles.includes('SUPER_ADMIN') ? 'danger' : userRoles.includes('teacher') || userRoles.includes('TRAIN_ADMIN') ? 'warning' : 'info'" style="margin-left: 4px">{{ roleLabel() }}</el-tag>
              <el-icon v-if="!isMobile"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="$router.push('/profile')">个人中心</el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 主内容 -->
      <el-main :class="['app-main', { 'app-main--mobile': isMobile }]">
        <router-view />
      </el-main>
    </el-container>

    <!-- 移动端底部导航 -->
    <nav v-if="isMobile" class="mobile-tabbar">
      <div
        v-for="item in mobileNav"
        :key="item.path"
        class="mobile-tabbar__item"
        :class="{ 'mobile-tabbar__item--active': $route.path.startsWith(item.path) }"
        @click="$router.push(item.path)"
      >
        <el-icon :size="22"><component :is="item.icon" /></el-icon>
        <span class="mobile-tabbar__label">{{ item.label }}</span>
      </div>
    </nav>
  </el-container>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { getUnreadCount } from '../api/community'

const router = useRouter()
const isCollapse = ref(false)
const isMobile = ref(false)
const unreadCount = ref(0)

const mobileNav = [
  { path: '/course', label: '课程', icon: 'Reading' },
  { path: '/task', label: '任务', icon: 'List' },
  { path: '/exam', label: '考试', icon: 'EditPen' },
  { path: '/community', label: '社区', icon: 'ChatDotSquare' },
  { path: '/profile', label: '我的', icon: 'User' }
]

function checkMobile() {
  isMobile.value = window.innerWidth < 768
}
const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))
const userPermissions = ref(JSON.parse(localStorage.getItem('userPermissions') || '[]'))
const userRoles = ref(JSON.parse(localStorage.getItem('userRoles') || '[]'))

function hasPermission(code) {
  return userPermissions.value.includes(code)
}

function roleLabel() {
  if (!userRoles.value.length) return ''
  const nameMap = { 'admin': '系统管理员', 'SUPER_ADMIN': '超级管理员', 'TRAIN_ADMIN': '培训管理员', 'teacher': '讲师', 'student': '学员', 'STUDENT': '学员' }
  return userRoles.value.map(r => nameMap[r] || r).join(' / ')
}

let timer = null

async function fetchUnread() {
  try {
    const res = await getUnreadCount()
    unreadCount.value = res.data.count
  } catch {}
}

function handleLogout() {
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  router.push('/login')
}

onMounted(() => {
  checkMobile()
  window.addEventListener('resize', checkMobile)
  fetchUnread()
  timer = setInterval(fetchUnread, 60000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
  window.removeEventListener('resize', checkMobile)
})
</script>

<style scoped>
.app-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #e6e6e6;
  background: #fff;
  height: 50px;
  padding: 0 20px;
}
.app-header--mobile {
  padding: 0 12px;
  height: 44px;
}
.app-main {
  background: #f5f7fa;
  padding: 20px;
}
.app-main--mobile {
  padding: 12px;
  padding-bottom: 70px;
}

/* 底部导航栏 */
.mobile-tabbar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 56px;
  background: #fff;
  border-top: 1px solid #eee;
  display: flex;
  z-index: 999;
  box-shadow: 0 -2px 8px rgba(0,0,0,0.06);
}
.mobile-tabbar__item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #999;
  transition: color 0.2s;
  -webkit-tap-highlight-color: transparent;
  padding: 4px 0;
}
.mobile-tabbar__item--active {
  color: #409EFF;
}
.mobile-tabbar__label {
  font-size: 11px;
  margin-top: 2px;
  line-height: 1;
}
</style>
