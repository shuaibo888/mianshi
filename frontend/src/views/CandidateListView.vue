<template>
  <div class="workbench">
    <section class="toolbar">
      <el-input v-model="query.keyword" clearable placeholder="姓名、电话、岗位" class="search-input" @keyup.enter="search" />
      <el-select v-model="query.status" clearable placeholder="状态" class="filter-input">
        <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
      </el-select>
      <el-button :icon="Search" @click="search">搜索</el-button>
      <el-button :icon="RefreshLeft" @click="resetSearch">重置</el-button>
      <el-button type="primary" :icon="Plus" @click="openCreate">新增候选人</el-button>
    </section>

    <section class="stats-strip">
      <div class="metric">
        <span>总候选人</span>
        <strong>{{ stats?.totalCandidates ?? '-' }}</strong>
      </div>
      <div class="metric">
        <span>平均评分</span>
        <strong>{{ stats?.averageRating ?? '-' }}</strong>
      </div>
      <div class="metric">
        <span>录用率</span>
        <strong>{{ stats ? `${stats.hireRate}%` : '-' }}</strong>
      </div>
    </section>

    <el-table
      ref="tableRef"
      v-loading="loading"
      :data="candidates"
      class="data-table"
      empty-text="暂无候选人"
      @sort-change="handleSortChange"
    >
      <el-table-column prop="name" label="姓名" min-width="120" sortable="custom" />
      <el-table-column prop="position" label="应聘岗位" min-width="160" sortable="custom" />
      <el-table-column prop="status" label="当前阶段" min-width="120" sortable="custom">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ row.statusLabel }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="rating" label="评分" width="90" sortable="custom">
        <template #default="{ row }">{{ row.rating ?? '-' }}</template>
      </el-table-column>
      <el-table-column prop="updatedAt" label="最后更新时间" min-width="180" sortable="custom" />
      <el-table-column fixed="right" label="操作" width="180" align="center" header-align="center">
        <template #default="{ row }">
          <div class="table-actions">
            <el-button text @click="router.push(`/candidates/${row.id}`)">详情</el-button>
            <el-button text @click="openEdit(row)">编辑</el-button>
            <el-popconfirm title="确认删除该候选人？" @confirm="remove(row.id)">
              <template #reference>
                <el-button text type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-bar">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[10, 20, 50]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handlePageSizeChange"
        @current-change="load"
      />
    </div>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑候选人' : '新增候选人'" width="560px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="96px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" maxlength="80" />
        </el-form-item>
        <el-form-item label="联系方式" prop="phone">
          <el-input v-model="form.phone" maxlength="11" />
        </el-form-item>
        <el-form-item label="应聘岗位" prop="position">
          <el-input v-model="form.position" maxlength="100" />
        </el-form-item>
        <el-form-item label="工作年限" prop="workYears">
          <el-input-number v-model="form.workYears" :min="0" :max="60" :precision="1" />
        </el-form-item>
        <el-form-item label="当前状态" prop="status">
          <el-select v-model="form.status" disabled>
            <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
          <div class="field-tip">候选人从已投递开始，后续状态在详情页按流程推进。</div>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="3" maxlength="1000" show-word-limit />
        </el-form-item>
        <el-form-item label="简历 PDF">
          <el-upload
            v-model:file-list="resumeFileList"
            accept="application/pdf,.pdf"
            :auto-upload="false"
            :limit="1"
            :on-change="handleResumeChange"
            :on-remove="removeSelectedResume"
            :on-exceed="handleResumeExceed"
          >
            <el-button>选择 PDF</el-button>
            <template #tip>
              <div class="field-tip">
                <span v-if="currentResumeName">当前简历：{{ currentResumeName }}。重新选择 PDF 后保存会替换。</span>
                <span v-else>仅支持 PDF 文件，保存候选人后会自动上传。</span>
              </div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import type { FormInstance, FormRules, UploadFile, UploadFiles, UploadUserFile } from 'element-plus'
import { ElMessage } from 'element-plus'
import { Plus, RefreshLeft, Search } from '@element-plus/icons-vue'
import {
  createCandidate,
  deleteCandidate,
  getOptions,
  listCandidates,
  updateCandidate,
  uploadCandidateResume,
} from '@/api/candidates'
import { getDashboardStats } from '@/api/dashboard'
import type { Candidate, CandidatePayload, CandidateStatus, DashboardStats, OptionItem } from '@/types'

const router = useRouter()
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const formRef = ref<FormInstance>()
const tableRef = ref()
const resumeFileList = ref<UploadUserFile[]>([])
const selectedResumeFile = ref<File | null>(null)
const currentResumeName = ref('')
const candidates = ref<Candidate[]>([])
const stats = ref<DashboardStats>()
const statusOptions = ref<OptionItem[]>([])
const query = reactive<{ keyword: string; status: CandidateStatus | '' }>({
  keyword: '',
  status: '',
})
const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
  sortBy: 'updatedAt',
  sortOrder: 'desc' as 'asc' | 'desc',
})
const form = reactive<CandidatePayload>({
  name: '',
  phone: '',
  position: '',
  workYears: 0,
  status: 'APPLIED',
  remark: '',
})

const rules: FormRules = {
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { max: 80, message: '姓名不能超过 80 个字符', trigger: 'blur' },
  ],
  phone: [
    { required: true, message: '请输入联系方式', trigger: 'blur' },
    { pattern: /^1\d{10}$/, message: '请输入 11 位手机号', trigger: 'blur' },
  ],
  position: [
    { required: true, message: '请输入岗位', trigger: 'blur' },
    { max: 100, message: '岗位不能超过 100 个字符', trigger: 'blur' },
  ],
  workYears: [{ required: true, message: '请输入工作年限', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
  remark: [{ max: 1000, message: '备注不能超过 1000 个字符', trigger: 'blur' }],
}

onMounted(async () => {
  const options = await getOptions()
  statusOptions.value = options.statuses
  await load()
})

async function load() {
  loading.value = true
  try {
    const params = {
      ...query,
      status: query.status || undefined,
      page: pagination.page,
      pageSize: pagination.pageSize,
      sortBy: pagination.sortBy,
      sortOrder: pagination.sortOrder,
    }
    const pageData = await listCandidates(params)
    candidates.value = pageData.records
    pagination.total = pageData.total
    pagination.page = pageData.page
    pagination.pageSize = pageData.pageSize
    stats.value = await getDashboardStats()
  } finally {
    loading.value = false
  }
}

async function search() {
  pagination.page = 1
  await load()
}

async function resetSearch() {
  query.keyword = ''
  query.status = ''
  pagination.page = 1
  pagination.sortBy = 'updatedAt'
  pagination.sortOrder = 'desc'
  tableRef.value?.clearSort()
  await load()
}

async function handlePageSizeChange() {
  pagination.page = 1
  await load()
}

async function handleSortChange({ prop, order }: { prop: string; order: 'ascending' | 'descending' | null }) {
  pagination.sortBy = prop || 'updatedAt'
  pagination.sortOrder = order === 'ascending' ? 'asc' : 'desc'
  pagination.page = 1
  await load()
}

function resetForm() {
  Object.assign(form, {
    name: '',
    phone: '',
    position: '',
    workYears: 0,
    status: 'APPLIED',
    remark: '',
  })
  resumeFileList.value = []
  selectedResumeFile.value = null
  currentResumeName.value = ''
}

function openCreate() {
  editingId.value = null
  resetForm()
  dialogVisible.value = true
}

function openEdit(row: Candidate) {
  editingId.value = row.id
  Object.assign(form, {
    name: row.name,
    phone: row.phone,
    position: row.position,
    workYears: Number(row.workYears),
    status: row.status,
    remark: row.remark || '',
  })
  resumeFileList.value = []
  selectedResumeFile.value = null
  currentResumeName.value = row.resumeOriginalName || ''
  dialogVisible.value = true
}

async function save() {
  await formRef.value?.validate()
  saving.value = true
  try {
    let saved: Candidate
    if (editingId.value) {
      saved = await updateCandidate(editingId.value, form)
    } else {
      saved = await createCandidate(form)
    }
    if (selectedResumeFile.value) {
      saved = await uploadCandidateResume(saved.id, selectedResumeFile.value)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    await load()
  } finally {
    saving.value = false
  }
}

function handleResumeChange(file: UploadFile) {
  const raw = file.raw
  if (!raw) return
  if (!isPdf(raw)) {
    ElMessage.error('简历仅支持 PDF 文件')
    resumeFileList.value = []
    selectedResumeFile.value = null
    return
  }
  selectedResumeFile.value = raw
}

function removeSelectedResume() {
  selectedResumeFile.value = null
}

function handleResumeExceed(files: File[], uploadFiles: UploadFiles) {
  const nextFile = files[0]
  if (!isPdf(nextFile)) {
    ElMessage.error('简历仅支持 PDF 文件')
    return
  }
  uploadFiles.splice(0, uploadFiles.length)
  resumeFileList.value = [{ name: nextFile.name }]
  selectedResumeFile.value = nextFile
}

function isPdf(file: File) {
  return file.type === 'application/pdf' && file.name.toLowerCase().endsWith('.pdf')
}

async function remove(id: number) {
  await deleteCandidate(id)
  ElMessage.success('删除成功')
  await load()
}

function statusType(status: CandidateStatus) {
  if (status === 'HIRED') return 'success'
  if (status === 'REJECTED') return 'danger'
  if (status === 'OFFER_PENDING') return 'warning'
  return 'primary'
}
</script>
