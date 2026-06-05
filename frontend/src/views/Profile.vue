<template>
  <div :class="['profile-page', { 'profile-page--mobile': isMobile }]">
    <!-- 移动端：顶部积分概览 -->
    <div v-if="isMobile" class="mobile-balance">
      <div class="mobile-balance__num">{{ account.balance || '0.0' }}</div>
      <div class="mobile-balance__label">当前积分</div>
    </div>

    <el-row :gutter="isMobile ? 0 : 20">
      <!-- 左侧：积分概览（PC） -->
      <el-col v-if="!isMobile" :span="8">
        <el-card style="text-align: center; margin-bottom: 20px">
          <div style="font-size: 48px; color: #E6A23C; font-weight: bold">{{ account.balance || '0.0' }}</div>
          <div style="color: #999; margin-top: 8px">当前积分</div>
        </el-card>

        <el-card style="margin-bottom: 20px">
          <template #header><span>年度统计 ({{ stats.year }})</span></template>
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item label="年度获得">{{ stats.yearEarned || '0.0' }}</el-descriptions-item>
            <el-descriptions-item label="年度消费">{{ stats.yearSpent || '0.0' }}</el-descriptions-item>
            <el-descriptions-item label="累计获得">{{ stats.totalEarned || '0.0' }}</el-descriptions-item>
          </el-descriptions>
          <div style="margin-top: 12px; text-align: center">
            <el-select v-model="selectedYear" size="small" style="width: 120px" @change="changeYear">
              <el-option v-for="y in years" :key="y" :label="y + '年'" :value="y" />
            </el-select>
          </div>
        </el-card>

        <el-card>
          <template #header><span>积分规则</span></template>
          <el-timeline>
            <el-timeline-item>每学习 <b>10分钟</b> 获得 <b>1积分</b></el-timeline-item>
            <el-timeline-item>视频可断点续学，累积计分</el-timeline-item>
            <el-timeline-item>完成课程全部课时额外 <b>10%奖励</b></el-timeline-item>
            <el-timeline-item>积分每年1月1日清零</el-timeline-item>
            <el-timeline-item>积分可打赏讲师或兑换奖励</el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>

      <!-- 右侧：明细+商城 -->
      <el-col :span="isMobile ? 24 : 16">
        <!-- 移动端：年度统计折叠 -->
        <el-collapse v-if="isMobile" style="margin-bottom: 12px">
          <el-collapse-item title="📊 年度统计" name="stats">
            <el-descriptions :column="1" border size="small">
              <el-descriptions-item label="年度获得">{{ stats.yearEarned || '0.0' }}</el-descriptions-item>
              <el-descriptions-item label="年度消费">{{ stats.yearSpent || '0.0' }}</el-descriptions-item>
              <el-descriptions-item label="累计获得">{{ stats.totalEarned || '0.0' }}</el-descriptions-item>
            </el-descriptions>
            <div style="margin-top: 8px; text-align: center">
              <el-select v-model="selectedYear" size="small" style="width: 100px" @change="changeYear">
                <el-option v-for="y in years" :key="y" :label="y + '年'" :value="y" />
              </el-select>
            </div>
          </el-collapse-item>
        </el-collapse>

        <el-tabs v-model="tab">
          <!-- 积分明细 -->
          <el-tab-pane label="积分明细" name="logs">
            <!-- PC表格 -->
            <el-table v-if="!isMobile" :data="logs" stripe size="small">
              <el-table-column prop="createTime" label="时间" width="170" />
              <el-table-column prop="type" label="类型" width="100">
                <template #default="{ row }">
                  <el-tag :type="typeColor(row.type)" size="small">{{ typeLabel(row.type) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="amount" label="积分" width="100">
                <template #default="{ row }">
                  <span :style="{ color: row.amount > 0 ? '#67C23A' : '#F56C6C', fontWeight: 'bold' }">
                    {{ row.amount > 0 ? '+' : '' }}{{ row.amount }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column prop="balanceAfter" label="余额" width="80" />
              <el-table-column prop="description" label="说明" show-overflow-tooltip />
            </el-table>

            <!-- 移动端：卡片列表 -->
            <div v-if="isMobile">
              <div v-for="log in logs" :key="log.id" class="log-card">
                <div class="log-card__top">
                  <el-tag :type="typeColor(log.type)" size="small">{{ typeLabel(log.type) }}</el-tag>
                  <span class="log-card__time">{{ log.createTime?.slice(5, 16) }}</span>
                </div>
                <div class="log-card__desc">{{ log.description }}</div>
                <div :class="['log-card__amount', log.amount > 0 ? 'log-card__amount--plus' : 'log-card__amount--minus']">
                  {{ log.amount > 0 ? '+' : '' }}{{ log.amount }}
                  <span style="font-size: 12px; color: #999; margin-left: 4px">余额 {{ log.balanceAfter }}</span>
                </div>
              </div>
              <el-empty v-if="!logs.length" description="暂无积分记录" />
            </div>

            <el-empty v-if="!isMobile && !logs.length" description="暂无积分记录" />
          </el-tab-pane>

          <!-- 积分商城 -->
          <el-tab-pane label="积分商城" name="shop">
            <el-alert type="info" :closable="false" style="margin-bottom: 12px">
              <template #title>积分商城功能已就绪，暂无可兑换商品，敬请期待！</template>
            </el-alert>
            <el-row :gutter="isMobile ? 8 : 16">
              <el-col :span="isMobile ? 12 : 8" v-for="goods in goodsList" :key="goods.id">
                <el-card shadow="hover" style="margin-bottom: 12px">
                  <div style="height: 80px; background: linear-gradient(135deg, #E6A23C, #f5c97e); border-radius: 6px; display: flex; align-items: center; justify-content: center; margin-bottom: 8px">
                    <el-icon style="font-size: 32px; color: #fff"><Present /></el-icon>
                  </div>
                  <div style="font-size: 13px; font-weight: bold; margin-bottom: 4px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap">{{ goods.name }}</div>
                  <div style="display: flex; justify-content: space-between; align-items: center">
                    <span style="color: #E6A23C; font-weight: bold; font-size: 13px">{{ goods.pointsRequired }} 积分</span>
                    <el-button type="warning" size="small" @click="handleRedeem(goods)">兑换</el-button>
                  </div>
                </el-card>
              </el-col>
            </el-row>
            <el-empty v-if="!goodsList.length" description="暂无商品" />
          </el-tab-pane>

          <!-- 兑换记录 -->
          <el-tab-pane label="兑换记录" name="exchange">
            <el-table :data="exchanges" stripe size="small">
              <el-table-column prop="createTime" label="兑换时间" width="170" />
              <el-table-column prop="pointsCost" label="消耗积分" width="100" />
              <el-table-column prop="status" label="状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="['warning', 'success', 'info'][row.status]" size="small">
                    {{ ['待发放', '已发放', '已取消'][row.status] }}
                  </el-tag>
                </template>
              </el-table-column>
            </el-table>
            <el-empty v-if="!exchanges.length" description="暂无兑换记录" />
          </el-tab-pane>
        </el-tabs>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { getPointAccount, getPointStats, getPointLogs, getPointGoods, redeemGoods, getMyExchanges } from '../api/point'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Present } from '@element-plus/icons-vue'

const tab = ref('logs')
const account = ref({})
const stats = ref({})
const logs = ref([])
const goodsList = ref([])
const exchanges = ref([])
const selectedYear = ref(new Date().getFullYear())
const years = [2025, 2026, 2027]
const isMobile = ref(false)

function checkMobile() { isMobile.value = window.innerWidth < 768 }

async function loadAccount() {
  try { const res = await getPointAccount(); account.value = res.data || {} } catch {}
}

async function loadStats() {
  try { const res = await getPointStats(selectedYear.value); stats.value = res.data || {} } catch {}
}

async function loadLogs() {
  try { const res = await getPointLogs(selectedYear.value); logs.value = res.data || [] } catch {}
}

async function loadGoods() {
  try { const res = await getPointGoods(); goodsList.value = res.data || [] } catch {}
}

async function loadExchanges() {
  try { const res = await getMyExchanges(); exchanges.value = res.data || [] } catch {}
}

function changeYear() {
  loadStats()
  loadLogs()
}

async function handleRedeem(goods) {
  try {
    await ElMessageBox.confirm(`确认消耗 ${goods.pointsRequired} 积分兑换「${goods.name}」？`, '兑换确认')
    await redeemGoods({ goodsId: goods.id })
    ElMessage.success('兑换成功')
    loadAccount()
    loadExchanges()
    loadGoods()
  } catch {}
}

function typeLabel(type) {
  const map = { earn_learn: '学习', earn_complete: '完成奖励', tip_out: '打赏', tip_in: '收到打赏', redeem: '兑换', year_reset: '年度清零' }
  return map[type] || type
}

function typeColor(type) {
  if (type.startsWith('earn') || type === 'tip_in') return 'success'
  if (type === 'tip_out' || type === 'redeem') return 'warning'
  if (type === 'year_reset') return 'danger'
  return 'info'
}

onMounted(() => {
  checkMobile()
  window.addEventListener('resize', checkMobile)
  loadAccount()
  loadStats()
  loadLogs()
  loadGoods()
  loadExchanges()
})

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile)
})
</script>

<style scoped>
.profile-page { padding-bottom: 20px; }
.profile-page--mobile { padding-bottom: 70px; }

.mobile-balance {
  background: linear-gradient(135deg, #E6A23C, #f5c97e);
  border-radius: 12px;
  padding: 24px 16px;
  text-align: center;
  color: #fff;
  margin-bottom: 12px;
}
.mobile-balance__num { font-size: 40px; font-weight: bold; }
.mobile-balance__label { font-size: 13px; opacity: 0.9; margin-top: 4px; }

.log-card {
  background: #fff;
  border-radius: 8px;
  padding: 10px 12px;
  margin-bottom: 8px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06);
}
.log-card__top {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}
.log-card__time { font-size: 12px; color: #999; }
.log-card__desc { font-size: 13px; color: #333; margin-bottom: 4px; }
.log-card__amount { font-weight: bold; font-size: 14px; }
.log-card__amount--plus { color: #67C23A; }
.log-card__amount--minus { color: #F56C6C; }
</style>
