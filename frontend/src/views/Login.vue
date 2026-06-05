<template>
  <div style="height: 100vh; display: flex; align-items: center; justify-content: center; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%)">
    <el-card style="width: 400px; border-radius: 12px" shadow="always">
      <h2 style="text-align: center; margin-bottom: 30px; color: #303133">e学网+ 培训平台</h2>
      <el-tabs v-model="activeTab">
        <el-tab-pane label="登录" name="login">
          <el-form :model="loginForm" :rules="rules" ref="loginRef" @submit.prevent="handleLogin">
            <el-form-item prop="username">
              <el-input v-model="loginForm.username" prefix-icon="User" placeholder="用户名" size="large" />
            </el-form-item>
            <el-form-item prop="password">
              <el-input v-model="loginForm.password" prefix-icon="Lock" type="password" placeholder="密码" size="large" show-password @keyup.enter="handleLogin" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" size="large" style="width: 100%" :loading="loading" @click="handleLogin">登 录</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="注册" name="register">
          <el-form :model="regForm" :rules="rules" ref="regRef" @submit.prevent="handleRegister">
            <el-form-item prop="username">
              <el-input v-model="regForm.username" prefix-icon="User" placeholder="用户名" size="large" />
            </el-form-item>
            <el-form-item prop="password">
              <el-input v-model="regForm.password" prefix-icon="Lock" type="password" placeholder="密码" size="large" show-password />
            </el-form-item>
            <el-form-item prop="realName">
              <el-input v-model="regForm.realName" prefix-icon="UserFilled" placeholder="真实姓名" size="large" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" size="large" style="width: 100%" :loading="loading" @click="handleRegister">注 册</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login, register } from '../api/auth'

const router = useRouter()
const activeTab = ref('login')
const loading = ref(false)
const loginRef = ref()
const regRef = ref()

const loginForm = ref({ username: '', password: '' })
const regForm = ref({ username: '', password: '', realName: '' })

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }, { min: 6, message: '密码至少6位', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }]
}

async function handleLogin() {
  await loginRef.value.validate()
  loading.value = true
  try {
    const res = await login(loginForm.value)
    localStorage.setItem('token', res.data.token)
    localStorage.setItem('userInfo', JSON.stringify(res.data))
    localStorage.setItem('userPermissions', JSON.stringify(res.data.permissions || []))
    localStorage.setItem('userRoles', JSON.stringify(res.data.roles || []))
    ElMessage.success('登录成功')
    router.push('/')
  } finally {
    loading.value = false
  }
}

async function handleRegister() {
  await regRef.value.validate()
  loading.value = true
  try {
    await register(regForm.value)
    ElMessage.success('注册成功，请登录')
    activeTab.value = 'login'
    loginForm.value.username = regForm.value.username
  } finally {
    loading.value = false
  }
}
</script>
