<template>
  <div>
    <el-row :gutter="20">
      <!-- 左侧：申请权限 -->
      <el-col :span="10">
        <el-card>
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center">
              <span>申请权限</span>
            </div>
          </template>

          <el-alert v-if="hasUploadPerm" type="success" :closable="false" style="margin-bottom: 16px">
            <template #title>您已拥有课程上传权限</template>
          </el-alert>

          <el-form v-if="!hasUploadPerm" :model="applyForm" label-width="100px">
            <el-form-item label="申请权限">
              <el-select v-model="applyForm.permissionCode" placeholder="选择要申请的权限" style="width: 100%">
                <el-option label="上传课程" value="course:upload" />
                <el-option label="管理课程" value="course:manage" />
              </el-select>
            </el-form-item>
            <el-form-item label="申请理由">
              <el-input v-model="applyForm.reason" type="textarea" :rows="4" placeholder="请说明申请理由" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="submitApply" :loading="submitting">提交申请</el-button>
            </el-form-item>
          </el-form>

          <el-divider />

          <div>
            <h4>当前角色</h4>
            <el-tag v-for="role in roles" :key="role.id" style="margin: 4px" :type="role.roleCode === 'admin' ? 'danger' : role.roleCode === 'SUPER_ADMIN' ? 'danger' : 'info'">
              {{ role.roleName }}
            </el-tag>
            <el-empty v-if="!roles.length" description="暂无角色" :image-size="60" />
          </div>

          <el-divider />

          <div>
            <h4>当前权限</h4>
            <el-tag v-for="p in permissions" :key="p" style="margin: 4px" type="success">{{ p }}</el-tag>
            <el-empty v-if="!permissions.length" description="暂无特殊权限" :image-size="60" />
          </div>
        </el-card>
      </el-col>

      <!-- 右侧：我的申请记录 -->
      <el-col :span="14">
        <el-card>
          <template #header><span>我的申请记录</span></template>
          <el-table :data="applies" stripe>
            <el-table-column prop="permissionName" label="申请权限" width="120" />
            <el-table-column prop="reason" label="申请理由" show-overflow-tooltip />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === 0 ? 'warning' : row.status === 1 ? 'success' : 'danger'">
                  {{ ['待审批', '已通过', '已拒绝'][row.status] }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="reviewerName" label="审批人" width="100" />
            <el-table-column prop="reviewRemark" label="审批备注" show-overflow-tooltip />
            <el-table-column prop="createTime" label="申请时间" width="170" />
          </el-table>
          <el-empty v-if="!applies.length" description="暂无申请记录" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getMyPermissions, getMyRoles, applyPermission, getMyApplies } from '../api/rbac'
import { ElMessage } from 'element-plus'

const permissions = ref([])
const roles = ref([])
const applies = ref([])
const hasUploadPerm = ref(false)
const submitting = ref(false)
const applyForm = ref({
  permissionCode: 'course:upload',
  reason: ''
})

async function loadData() {
  const [permRes, roleRes, applyRes] = await Promise.all([
    getMyPermissions(),
    getMyRoles(),
    getMyApplies()
  ])
  permissions.value = permRes.data || []
  roles.value = roleRes.data || []
  applies.value = applyRes.data || []
  hasUploadPerm.value = permissions.value.includes('course:upload')
}

async function submitApply() {
  if (!applyForm.value.permissionCode) {
    ElMessage.warning('请选择要申请的权限')
    return
  }
  submitting.value = true
  try {
    await applyPermission(applyForm.value)
    ElMessage.success('申请已提交，请等待审批')
    applyForm.value.reason = ''
    loadData()
  } finally {
    submitting.value = false
  }
}

onMounted(loadData)
</script>
