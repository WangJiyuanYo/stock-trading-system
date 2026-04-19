<template>
  <div class="app-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>📊 股票管理系统</span>
          <el-button type="primary" @click="handleRefresh" :loading="loading">
            🔄 刷新数据
          </el-button>
        </div>
      </template>

      <!-- 统计卡片 -->
      <el-row :gutter="20" style="margin-bottom: 20px;">
        <el-col :span="6">
          <el-statistic title="持仓股票数" :value="stockCount" />
        </el-col>
        <el-col :span="6">
          <el-statistic title="总投入 (元)" :value="totalInvestment" :precision="2" />
        </el-col>
        <el-col :span="6">
          <el-statistic title="总市值 (元)" :value="totalMarketValue" :precision="2" />
        </el-col>
        <el-col :span="6">
          <el-statistic 
            title="总盈亏 (元)" 
            :value="totalProfit" 
            :precision="2"
            :value-style="totalProfit >= 0 ? { color: '#f56c6c' } : { color: '#67c23a' }"
          >
            <template #prefix>
              {{ totalProfit >= 0 ? '📈' : '📉' }}
            </template>
          </el-statistic>
        </el-col>
      </el-row>

      <!-- 盈亏占比饼图 -->
      <el-card style="margin-bottom: 20px;">
        <template #header>
          <span>📊 个股盈亏占比分布</span>
        </template>
        <div ref="chartRef" style="width: 100%; height: 400px;"></div>
      </el-card>

      <!-- 操作按钮 -->
      <div style="margin-bottom: 20px;">
        <el-button type="primary" @click="dialogVisible = true">
          ➕ 添加股票
        </el-button>
        <el-button type="success" @click="handleExecuteTask" :loading="executing">
          🚀 手动获取行情
        </el-button>
      </div>

      <!-- 股票列表表格 -->
      <el-table :data="tableData" stripe style="width: 100%" v-loading="loading">
        <el-table-column prop="name" label="股票名称" width="150" />
        <el-table-column prop="stockCode" label="股票代码" width="120">
          <template #default="scope">
            {{ formatStockCodeDisplay(scope.row.stockCode) }}
          </template>
        </el-table-column>
        <el-table-column prop="holdingQuantity" label="持仓数量" width="120" />
        <el-table-column prop="holdingPrice" label="持仓价格" width="120" :formatter="formatPrice" />
        <el-table-column prop="currentPrice" label="当前价格" width="120">
          <template #default="scope">
            <span :style="getColorStyle(scope.row.currentPrice, scope.row.holdingPrice)">
              {{ formatPriceValue(scope.row.currentPrice) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="浮动盈亏" width="150">
          <template #default="scope">
            <span :style="{ color: (scope.row.profitLoss || 0) >= 0 ? '#f56c6c' : '#67c23a', fontWeight: 'bold' }">
              {{ (scope.row.profitLoss || 0) >= 0 ? '+' : '' }}{{ (scope.row.profitLoss || 0).toFixed(2) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="今日盈亏" width="150">
          <template #default="scope">
            <span :style="{ color: (scope.row.todayProfitLoss || 0) >= 0 ? '#f56c6c' : '#67c23a', fontWeight: 'bold' }">
              {{ (scope.row.todayProfitLoss || 0) >= 0 ? '+' : '' }}{{ (scope.row.todayProfitLoss || 0).toFixed(2) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button size="small" @click="handleEdit(scope.row)">✏️ 编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row)">🗑️ 删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加/编辑股票对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑股票' : '添加股票'"
      width="500px"
    >
      <el-form :model="form" label-width="100px">
        <el-form-item label="股票类型">
          <el-select v-model="form.stockType" placeholder="请选择">
            <el-option label="A 股" value="A 股" />
            <el-option label="港股" value="港股" />
            <el-option label="美股" value="美股" />
            <el-option label="英股" value="英股" />
            <el-option label="贵金属" value="贵金属" />
          </el-select>
        </el-form-item>
        <el-form-item label="股票代码">
          <el-input v-model="form.stockCode" placeholder="例如：600000" />
        </el-form-item>
        <el-form-item label="持仓数量">
          <el-input-number v-model="form.holdingQuantity" :min="0" :step="100" />
        </el-form-item>
        <el-form-item label="持仓价格">
          <el-input-number v-model="form.holdingPrice" :min="0" :precision="4" :step="0.0001" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as echarts from 'echarts'
import { getStockList, addStock, updateStock, deleteStock, executeManualTask, getMarketDataWithProfit } from '@/api/stock'

const loading = ref(false)
const executing = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const tableData = ref([])
const chartRef = ref(null)
let myChart = null

const form = ref({
  stockType: 'A 股',
  stockCode: '',
  holdingQuantity: 0,
  holdingPrice: 0
})

// 重置表单
const resetForm = () => {
  form.value = {
    stockType: 'A 股',
    stockCode: '',
    holdingQuantity: 0,
    holdingPrice: 0
  }
}

// 统计数据
const stockCount = computed(() => tableData.value.length)
const totalInvestment = computed(() => {
  return tableData.value.reduce((sum, item) => {
    const cost = (item.holdingPrice && item.holdingQuantity) ? 
                 (parseFloat(item.holdingPrice) * parseFloat(item.holdingQuantity)) : 0
    return sum + cost
  }, 0)
})
const totalMarketValue = computed(() => {
  return tableData.value.reduce((sum, item) => {
    const value = (item.currentPrice && item.holdingQuantity) ? 
                  (parseFloat(item.currentPrice) * parseFloat(item.holdingQuantity)) : 0
    return sum + value
  }, 0)
})
const totalProfit = computed(() => {
  // 方式 1：总市值 - 总投入
  const profit1 = totalMarketValue.value - totalInvestment.value
  
  // 方式 2：累加所有股票的浮动盈亏
  const profit2 = tableData.value.reduce((sum, item) => {
    return sum + (item.profitLoss || 0)
  }, 0)
  
  console.log('总盈亏计算:', {
    'profit_market_minus_cost': profit1,
    'profit_sum_all_stocks': profit2,
    totalMarketValue: totalMarketValue.value,
    totalInvestment: totalInvestment.value
  })
  
  // 使用方式 2（更准确），并保留 2 位小数
  return Math.round(profit2 * 100) / 100
})

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    // 获取实时行情数据（包含盈亏计算）
    const res = await getMarketDataWithProfit()
    tableData.value = res.data || []
    
    console.log('股票数据:', tableData.value)
    console.log('第一只股票的浮动盈亏:', tableData.value[0]?.profitLoss)
    console.log('第一只股票的今日盈亏:', tableData.value[0]?.todayProfitLoss)
    
    // 初始化饼图
    await nextTick()
    initChart()
  } catch (error) {
    ElMessage.error('加载数据失败：' + error.message)
  } finally {
    loading.value = false
  }
}

// 初始化盈亏占比饼图
const initChart = () => {
  if (!chartRef.value) return
  
  // 销毁旧图表
  if (myChart) {
    myChart.dispose()
  }
  
  // 创建新图表
  myChart = echarts.init(chartRef.value)
  
  // 准备数据：过滤掉盈亏为 null 的股票
  const chartData = tableData.value
    .filter(item => item.profitLoss !== null && item.profitLoss !== undefined)
    .map(item => ({
      name: item.name || item.stockCode,
      value: Math.abs(parseFloat(item.profitLoss)),
      profitLoss: item.profitLoss,
      itemStyle: {
        color: item.profitLoss >= 0 ? '#f56c6c' : '#67c23a' // 红涨绿跌
      }
    }))
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: (params) => {
        const percent = params.percent.toFixed(1)
        const profitLoss = params.data.profitLoss >= 0 ? '+' : ''
        return `${params.name}<br/>盈亏：${profitLoss}${params.data.profitLoss.toFixed(2)} 元<br/>占比：${percent}%`
      }
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      top: 'middle'
    },
    title: {
      text: '个股盈亏分布',
      left: 'center',
      top: 20,
      textStyle: {
        fontSize: 14
      }
    },
    series: [
      {
        name: '盈亏金额',
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['60%', '50%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 5,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: true,
          formatter: '{b}: {d}%'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 14,
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: chartData
      }
    ]
  }
  
  myChart.setOption(option)
  
  // 响应窗口大小变化
  window.addEventListener('resize', () => {
    myChart.resize()
  })
}

// 刷新数据
const handleRefresh = () => {
  loadData()
}

// 执行手动任务
const handleExecuteTask = async () => {
  executing.value = true
  try {
    await executeManualTask()
    ElMessage.success('已手动获取最新行情数据')
    await loadData()
  } catch (error) {
    ElMessage.error('获取行情失败：' + error.message)
  } finally {
    executing.value = false
  }
}

// 提交表单
const handleSubmit = async () => {
  submitting.value = true
  try {
    // 验证股票代码
    if (!form.value.stockCode || form.value.stockCode.trim() === '') {
      ElMessage.error('请输入股票代码')
      return
    }
    
    if (isEdit.value) {
      // 更新时需要传递股票代码
      await updateStock(form.value.stockCode, form.value)
      ElMessage.success('更新成功')
    } else {
      await addStock(form.value)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    resetForm()
    await loadData()
  } catch (error) {
    ElMessage.error('操作失败：' + error.message)
  } finally {
    submitting.value = false
  }
}

// 编辑
const handleEdit = (row) => {
  isEdit.value = true
  // 移除股票代码的市场前缀（sh/sz），保持与后端 JSON 一致
  const pureCode = row.stockCode.replace(/^(sh|sz|hk|gb_)/i, '')
  form.value = {
    stockType: row.stockType,
    stockCode: pureCode,
    holdingQuantity: row.holdingQuantity,
    holdingPrice: row.holdingPrice
  }
  dialogVisible.value = true
}

// 删除
const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该股票吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteStock(row.stockCode)
      ElMessage.success('删除成功')
      await loadData()
    } catch (error) {
      ElMessage.error('删除失败：' + error.message)
    }
  })
}

// 格式化价格
const formatPrice = (row) => {
  return row.holdingPrice ? Number(row.holdingPrice).toFixed(4) : '-'
}

const formatPriceValue = (price) => {
  return price ? Number(price).toFixed(2) : '-'
}

// 格式化股票代码显示（添加市场前缀）
const formatStockCodeDisplay = (code) => {
  if (!code) return '-'
  // 如果已经有前缀，直接返回
  if (/^(sh|sz|hk|gb_)/i.test(code)) {
    return code.toUpperCase()
  }
  // 根据代码规则添加前缀
  const pureCode = code.replace(/^(sh|sz|hk|gb_)/i, '')
  if (/^6/.test(pureCode) || /^5/.test(pureCode)) {
    return 'SH' + pureCode
  } else if (/^9/.test(pureCode)) {
    return 'SH' + pureCode
  } else {
    return 'SZ' + pureCode
  }
}

// 颜色样式
const getColorStyle = (current, holding) => {
  if (!current || !holding) return {}
  const diff = current - holding
  return { color: diff >= 0 ? '#f56c6c' : '#67c23a', fontWeight: 'bold' }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.app-container {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.box-card {
  margin-bottom: 20px;
}
</style>
