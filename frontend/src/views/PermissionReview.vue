<template>
  <div>
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span>权限审批</span>
          <el-radio-group v-model="tab" @change="loadData">
            <el-radio-button value="pending">待审批</el-radio-button>
            <el-radio-button value="all">全部</el-radio-button>
          </el-radio-group>
        </div>
      </template>

      <el-table :data="applies" stripe>
        <el-table-column prop="realName" label="申请人" width="100" />
        <el-table-column prop="userName" label="账号" width="100" />
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
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 0">
              <el-button type="success" size="small" @click="handleReview(row, 1)">通过</el-button>
              <el-button type="danger" size="small" @click="handleReview(row, 2)">拒绝</el-button>
            </template>
            <span v-else style="color: #999">已处理</span>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!applies.length" description="暂无申请" />
    </el-card>

    <!-- 审批对话框 -->
    <el-dialog v-model="dialogVisible" title="审批" width="400px">
      <el-form label-width="80px">
        <el-form-item label="审批结果">
          <el-tag :type="reviewStatus === 1 ? 'success' : 'danger'">
            {{ reviewStatus === 1 ? '通过' : '拒绝' }}
          </el-tag>
        </el-form-item>
        <el-form-item label="审批备注">
          <el-input v-model="reviewRemark" type="textarea" :rows="3" placeholder="可选" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmReview" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getPendingApplies, getAllApplies, reviewApply } from '../api/rbac'
import { ElMessage } from 'element-plus'

const tab = ref('pending')
const applies = ref([])
const dialogVisible = ref(false)
const currentApply = ref(null)
const reviewStatus = ref(1)
const reviewRemark = ref('')
const submitting = ref(false)

async function loadData() {
  const res = tab.value === 'pending' ? await getPendingApplies() : await getAllApplies()
  applies.value = res.data || []
}

function handleReview(row, status) {
  currentApply.value = row
  reviewStatus.value = status
  reviewRemark.value = ''
  dialogVisible.value = true
}

async function confirmReview() {
  submitting.value = true
  try {
    await reviewApply({
      applyId: currentApply.value.id,
      status: reviewStatus.value,
      reviewRemark: reviewRemark.value
    })
    ElMessage.success(reviewStatus.value === 1 ? '已通过' : '已拒绝')
    dialogVisible.value = false
    loadData()
  } finally {
    submitting.value = false
  }
}

onMounted(loadData)
</script>
