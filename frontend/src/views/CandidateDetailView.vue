<template>
  <div class="detail-page" v-loading="loading">
    <section v-if="detail" class="detail-layout">
      <main class="detail-main">
        <section class="panel profile-panel">
          <div class="profile-head">
            <div class="profile-copy">
              <div class="eyebrow">候选人档案</div>
              <h2>{{ detail.candidate.name }}</h2>
              <p>{{ detail.candidate.position }} · {{ detail.candidate.phone }}</p>
            </div>
            <div class="profile-side">
              <span class="profile-status" :class="detail.candidate.status.toLowerCase()">
                {{ detail.candidate.statusLabel }}
              </span>
              <button
                class="resume-card"
                :class="{ empty: !detail.candidate.hasResume }"
                :disabled="!detail.candidate.hasResume"
                type="button"
                @click="previewResume"
              >
                <span class="resume-icon">
                  <el-icon><Document /></el-icon>
                </span>
                <span class="resume-copy">
                  <span>PDF 简历</span>
                  <strong>{{ detail.candidate.resumeOriginalName || '暂无简历文件' }}</strong>
                </span>
                <span class="resume-action">
                  <el-icon><View /></el-icon>
                  预览
                </span>
              </button>
            </div>
          </div>

          <div class="profile-metrics">
            <div class="metric-cell">
              <span>工作年限</span>
              <strong>{{ detail.candidate.workYears }} 年</strong>
            </div>
            <div class="metric-cell">
              <span>综合评分</span>
              <strong>{{ detail.candidate.rating ?? '-' }}</strong>
            </div>
            <div class="metric-cell">
              <span>创建时间</span>
              <strong>{{ detail.candidate.createdAt }}</strong>
            </div>
            <div class="metric-cell">
              <span>更新时间</span>
              <strong>{{ detail.candidate.updatedAt }}</strong>
            </div>
          </div>

          <div class="remark-box">
            <span>备注</span>
            <p>{{ detail.candidate.remark || '暂无备注' }}</p>
          </div>
        </section>

        <section class="panel flow-panel">
          <div class="section-head compact">
            <div>
              <h3>招聘流程</h3>
              <p>面试记录会自动推动“面试中 / 待 offer”阶段</p>
            </div>
          </div>
          <div class="status-steps">
            <div
              v-for="(step, index) in flowSteps"
              :key="step.value"
              class="status-step"
              :class="{ done: index < currentStepIndex, current: index === currentStepIndex }"
            >
              <span class="step-dot">{{ index + 1 }}</span>
              <span>{{ step.label }}</span>
            </div>
          </div>
        </section>

        <section class="panel interview-panel">
          <div class="section-head compact">
            <div>
              <h3>面试记录</h3>
              <p>添加面试安排会进入面试中；补充评分达到 4 分及以上时进入待 offer</p>
            </div>
            <el-button type="primary" :icon="Plus" @click="openScheduleDialog">添加面试安排</el-button>
          </div>

          <el-table :data="detail.interviews" empty-text="暂无面试记录" class="interview-table">
            <el-table-column prop="interviewTime" label="时间" min-width="170" />
            <el-table-column prop="interviewer" label="面试官" width="120" />
            <el-table-column prop="score" label="评分" width="100">
              <template #default="{ row }">
                <el-tag :type="row.score !== null && Number(row.score) >= 4 ? 'success' : 'info'" effect="plain">
                  {{ row.score ?? '待反馈' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="评价" min-width="260" show-overflow-tooltip>
              <template #default="{ row }">{{ row.content || '待补充评价' }}</template>
            </el-table-column>
            <el-table-column label="操作" width="230" fixed="right" align="center" header-align="center">
              <template #default="{ row }">
                <div class="table-actions">
                  <el-button text @click="editSchedule(row)">改安排</el-button>
                  <el-button text type="primary" @click="openFeedback(row)">
                    {{ row.score === null && !row.content ? '录反馈' : '改反馈' }}
                  </el-button>
                  <el-popconfirm title="确认删除该记录？" @confirm="removeInterview(row.id)">
                    <template #reference>
                      <el-button text type="danger">删除</el-button>
                    </template>
                  </el-popconfirm>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </section>
      </main>

      <aside class="detail-side">
        <section class="panel action-panel">
          <div class="section-head compact">
            <div>
              <h3>当前阶段操作</h3>
              <p>{{ currentActionHint }}</p>
            </div>
          </div>

          <div v-if="isTerminal" class="terminal-state" :class="detail.candidate.status.toLowerCase()">
            <el-icon><Check v-if="detail.candidate.status === 'HIRED'" /><Close v-else /></el-icon>
            <strong>{{ detail.candidate.statusLabel }}</strong>
            <span>该候选人流程已结束，无需继续流转。</span>
          </div>

          <template v-else>
            <div class="action-buttons">
              <el-button
                v-for="item in nextStatusOptions"
                :key="item.value"
                :type="item.value === statusForm.status ? actionButtonType(item.value) : 'default'"
                :plain="item.value !== statusForm.status"
                @click="statusForm.status = item.value"
              >
                {{ item.label }}
              </el-button>
            </div>
            <el-input
              v-model="statusForm.note"
              class="mt-12"
              type="textarea"
              :rows="3"
              placeholder="填写本次流转说明"
            />
            <el-button
              type="primary"
              class="mt-12 full-button"
              :disabled="!statusForm.status"
              @click="submitStatus"
            >
              确认更新
            </el-button>
          </template>
        </section>

        <section class="panel history-panel">
          <div class="section-head compact">
            <div>
              <h3>状态历史</h3>
              <p>按最新操作排序</p>
            </div>
          </div>
          <el-timeline>
            <el-timeline-item
              v-for="log in detail.statusLogs"
              :key="log.id"
              :timestamp="log.createdAt"
              :type="historyType(log.toStatus)"
            >
              <strong>{{ log.fromStatusLabel }} → {{ log.toStatusLabel }}</strong>
              <p>{{ log.note || '无说明' }}</p>
            </el-timeline-item>
          </el-timeline>
        </section>
      </aside>
    </section>

    <el-dialog
      v-model="scheduleDialog"
      :title="scheduleId ? '编辑面试安排' : '添加面试安排'"
      width="520px"
      @closed="resetScheduleDialog"
    >
      <el-alert
        class="dialog-tip"
        type="info"
        show-icon
        :closable="false"
        title="这里只保存面试时间和面试官，面试结果在反馈里单独填写。"
      />
      <el-form ref="scheduleFormRef" :model="scheduleForm" :rules="scheduleRules" label-width="96px">
        <el-form-item label="面试时间" prop="interviewTime">
          <el-date-picker
            v-model="scheduleForm.interviewTime"
            type="datetime"
            value-format="YYYY-MM-DDTHH:mm:ss"
            class="full"
          />
        </el-form-item>
        <el-form-item label="面试官" prop="interviewer">
          <el-input v-model="scheduleForm.interviewer" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="scheduleDialog = false">取消</el-button>
        <el-button type="primary" @click="saveSchedule">保存安排</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="feedbackDialog" title="录入面试反馈" width="560px" @closed="resetFeedbackDialog">
      <el-alert
        class="dialog-tip"
        type="info"
        show-icon
        :closable="false"
        title="面试完成后填写评分和评价；评分达到 4 分及以上会同步推进流程。"
      />
      <el-form ref="feedbackFormRef" :model="feedbackForm" :rules="feedbackRules" label-width="96px">
        <el-form-item label="评分" prop="score">
          <el-input-number v-model="feedbackForm.score" :min="0" :max="5" :precision="1" />
        </el-form-item>
        <el-form-item label="评价内容" prop="content">
          <el-input v-model="feedbackForm.content" type="textarea" :rows="4" maxlength="2000" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="feedbackDialog = false">取消</el-button>
        <el-button type="primary" @click="saveFeedback">保存反馈</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="resumeDialog" title="简历预览" width="78vw" top="5vh" @closed="clearResumePreview">
      <div v-loading="resumePreviewLoading" class="resume-preview">
        <iframe v-if="resumePreviewUrl" :src="resumePreviewUrl" class="resume-frame" title="简历预览"></iframe>
        <el-empty v-else-if="!resumePreviewLoading" description="暂无可预览简历" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { Check, Close, Document, Plus, View } from '@element-plus/icons-vue'
import {
  changeCandidateStatus,
  createInterview,
  deleteInterview,
  fetchCandidateResume,
  getCandidate,
  getOptions,
  updateInterview,
} from '@/api/candidates'
import type { CandidateDetail, CandidateStatus, InterviewRecord, OptionItem } from '@/types'

const route = useRoute()
const candidateId = Number(route.params.id)
const loading = ref(false)
const detail = ref<CandidateDetail>()
const statuses = ref<OptionItem[]>([])
const statusForm = reactive<{ status: CandidateStatus | ''; note: string }>({ status: '', note: '' })
const scheduleDialog = ref(false)
const scheduleId = ref<number | null>(null)
const scheduleFormRef = ref<FormInstance>()
const feedbackDialog = ref(false)
const feedbackId = ref<number | null>(null)
const feedbackFormRef = ref<FormInstance>()
const selectedInterview = ref<InterviewRecord | null>(null)
const resumeDialog = ref(false)
const resumePreviewUrl = ref('')
const resumePreviewLoading = ref(false)
const scheduleForm = reactive({
  interviewTime: '',
  interviewer: '',
})
const feedbackForm = reactive({
  score: null as number | null,
  content: '',
})

const scheduleRules: FormRules = {
  interviewTime: [{ required: true, message: '请选择面试时间', trigger: 'change' }],
  interviewer: [{ required: true, message: '请输入面试官', trigger: 'blur' }],
}

const feedbackRules: FormRules = {
  score: [{ required: true, message: '请输入评分', trigger: 'change' }],
  content: [
    { required: true, message: '请输入评价内容', trigger: 'blur' },
    { max: 2000, message: '评价内容不能超过 2000 个字符', trigger: 'blur' },
  ],
}

const allowedTransitions: Record<CandidateStatus, CandidateStatus[]> = {
  APPLIED: ['SCREEN_PASSED', 'REJECTED'],
  SCREEN_PASSED: ['INTERVIEWING', 'REJECTED'],
  INTERVIEWING: ['OFFER_PENDING', 'REJECTED'],
  OFFER_PENDING: ['HIRED', 'REJECTED'],
  HIRED: [],
  REJECTED: [],
}

const flowSteps: OptionItem[] = [
  { value: 'APPLIED', label: '已投递' },
  { value: 'SCREEN_PASSED', label: '初筛通过' },
  { value: 'INTERVIEWING', label: '面试中' },
  { value: 'OFFER_PENDING', label: '待 offer' },
  { value: 'HIRED', label: '已录用' },
]

const isTerminal = computed(() => {
  const status = detail.value?.candidate.status
  return status === 'HIRED' || status === 'REJECTED'
})

const currentStepIndex = computed(() => {
  const current = detail.value?.candidate.status
  if (current === 'REJECTED') return -1
  return flowSteps.findIndex((step) => step.value === current)
})

const nextStatusOptions = computed(() => {
  const current = detail.value?.candidate.status
  if (!current) return []
  return statuses.value.filter((item) => allowedTransitions[current].includes(item.value))
})

const currentActionHint = computed(() => {
  const status = detail.value?.candidate.status
  if (status === 'HIRED') return '录用完成'
  if (status === 'REJECTED') return '流程已终止'
  if (status === 'SCREEN_PASSED') return '可以安排面试，也可以直接淘汰'
  if (status === 'INTERVIEWING') return '补充高评分面试反馈可自动进入待 offer'
  if (status === 'OFFER_PENDING') return '确认 offer 结果'
  return '先完成简历初筛'
})

onMounted(async () => {
  const options = await getOptions()
  statuses.value = options.statuses
  await load()
})

onBeforeUnmount(() => {
  clearResumePreview()
})

async function load() {
  loading.value = true
  try {
    detail.value = await getCandidate(candidateId)
    statusForm.status = ''
    statusForm.note = ''
  } finally {
    loading.value = false
  }
}

async function submitStatus() {
  if (!statusForm.status) return
  await changeCandidateStatus(candidateId, statusForm.status, statusForm.note)
  ElMessage.success('状态已更新')
  await load()
}

function openScheduleDialog() {
  scheduleId.value = null
  selectedInterview.value = null
  Object.assign(scheduleForm, {
    interviewTime: '',
    interviewer: '',
  })
  scheduleDialog.value = true
}

function resetScheduleDialog() {
  scheduleId.value = null
  selectedInterview.value = null
  Object.assign(scheduleForm, {
    interviewTime: '',
    interviewer: '',
  })
  scheduleFormRef.value?.clearValidate()
}

function editSchedule(row: InterviewRecord) {
  scheduleId.value = row.id
  selectedInterview.value = row
  Object.assign(scheduleForm, {
    interviewTime: row.interviewTime.replace(' ', 'T'),
    interviewer: row.interviewer,
  })
  scheduleDialog.value = true
}

async function saveSchedule() {
  await scheduleFormRef.value?.validate()
  if (scheduleId.value) {
    await updateInterview(scheduleId.value, {
      ...scheduleForm,
      score: selectedInterview.value?.score ?? null,
      content: selectedInterview.value?.content || '',
    })
  } else {
    await createInterview(candidateId, {
      ...scheduleForm,
      score: null,
      content: '',
    })
  }
  ElMessage.success('面试安排已保存')
  scheduleDialog.value = false
  await load()
}

function openFeedback(row: InterviewRecord) {
  feedbackId.value = row.id
  selectedInterview.value = row
  Object.assign(feedbackForm, {
    score: row.score === null ? null : Number(row.score),
    content: row.content || '',
  })
  feedbackDialog.value = true
}

function resetFeedbackDialog() {
  feedbackId.value = null
  selectedInterview.value = null
  Object.assign(feedbackForm, {
    score: null,
    content: '',
  })
  feedbackFormRef.value?.clearValidate()
}

async function saveFeedback() {
  await feedbackFormRef.value?.validate()
  if (!feedbackId.value || !selectedInterview.value) return
  await updateInterview(feedbackId.value, {
    interviewTime: selectedInterview.value.interviewTime.replace(' ', 'T'),
    interviewer: selectedInterview.value.interviewer,
    score: feedbackForm.score,
    content: feedbackForm.content,
  })
  ElMessage.success('面试反馈已保存，流程已同步')
  feedbackDialog.value = false
  await load()
}

async function removeInterview(id: number) {
  await deleteInterview(id)
  ElMessage.success('面试记录已删除')
  await load()
}

async function previewResume() {
  if (!detail.value?.candidate.hasResume) return
  clearResumePreview()
  resumeDialog.value = true
  resumePreviewLoading.value = true
  try {
    const blob = await fetchCandidateResume(candidateId)
    resumePreviewUrl.value = URL.createObjectURL(new Blob([blob], { type: 'application/pdf' }))
  } finally {
    resumePreviewLoading.value = false
  }
}

function clearResumePreview() {
  if (resumePreviewUrl.value) {
    URL.revokeObjectURL(resumePreviewUrl.value)
    resumePreviewUrl.value = ''
  }
}

function statusType(status: CandidateStatus) {
  if (status === 'HIRED') return 'success'
  if (status === 'REJECTED') return 'danger'
  if (status === 'OFFER_PENDING') return 'warning'
  return 'primary'
}

function historyType(status: CandidateStatus) {
  if (status === 'HIRED') return 'success'
  if (status === 'REJECTED') return 'danger'
  if (status === 'OFFER_PENDING') return 'warning'
  return 'primary'
}

function actionButtonType(status: CandidateStatus) {
  if (status === 'REJECTED') return 'danger'
  if (status === 'HIRED') return 'success'
  return 'primary'
}
</script>

<style scoped>
.detail-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 340px;
  gap: 16px;
}

.detail-main,
.detail-side {
  display: grid;
  align-content: start;
  gap: 16px;
  min-width: 0;
}

.profile-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}

.profile-head h2 {
  margin: 4px 0 6px;
  color: #1f354a;
  font-size: 24px;
}

.profile-head p,
.section-head p {
  margin: 4px 0 0;
  color: #718092;
}

.profile-copy {
  min-width: 0;
}

.profile-side {
  display: grid;
  flex: 0 0 auto;
  justify-items: end;
  gap: 10px;
  width: min(340px, 36%);
}

.profile-status {
  display: inline-flex;
  min-width: 78px;
  align-items: center;
  justify-content: center;
  padding: 7px 14px;
  border: 1px solid #b9dccf;
  border-radius: 999px;
  color: #1d6f6f;
  background: #f1f8f5;
  box-shadow: 0 6px 14px rgb(29 111 111 / 10%);
  font-size: 13px;
  font-weight: 800;
}

.profile-status.offer_pending {
  border-color: #f0d8a4;
  color: #8a5a12;
  background: #fff8e8;
  box-shadow: 0 6px 14px rgb(138 90 18 / 9%);
}

.profile-status.hired {
  border-color: #a8d8b3;
  color: #2f6f45;
  background: #eef9f1;
  box-shadow: 0 6px 14px rgb(47 111 69 / 10%);
}

.profile-status.rejected {
  border-color: #f1bcbc;
  color: #a33c3c;
  background: #fff2f2;
  box-shadow: 0 6px 14px rgb(163 60 60 / 8%);
}

.resume-card {
  position: relative;
  display: grid;
  width: 100%;
  min-height: 76px;
  grid-template-columns: 46px minmax(0, 1fr) auto;
  align-items: center;
  gap: 12px;
  overflow: hidden;
  padding: 12px 14px;
  border: 1px solid #c9dedb;
  border-radius: 8px;
  color: #1f354a;
  background: linear-gradient(135deg, #ffffff, #f3faf8);
  box-shadow: 0 8px 20px rgb(31 53 74 / 8%);
  cursor: pointer;
  text-align: left;
  transition:
    transform 160ms ease,
    box-shadow 160ms ease,
    filter 160ms ease;
}

.resume-card::before {
  position: absolute;
  inset: 0;
  content: "";
  background: linear-gradient(100deg, transparent, rgb(29 111 111 / 8%), transparent);
  transform: translateX(-120%);
  transition: transform 520ms ease;
}

.resume-card:hover {
  border-color: #8fc9bd;
  box-shadow: 0 12px 26px rgb(31 53 74 / 12%);
  filter: none;
  transform: translateY(-2px);
}

.resume-card:hover::before {
  transform: translateX(120%);
}

.resume-card:focus-visible {
  outline: 3px solid rgb(64 158 255 / 28%);
  outline-offset: 3px;
}

.resume-card.empty {
  color: #718092;
  background: #f7f9fc;
  box-shadow: inset 0 0 0 1px #dce3eb;
  cursor: not-allowed;
  filter: none;
}

.resume-card.empty::before {
  display: none;
}

.resume-card.empty:hover {
  box-shadow: inset 0 0 0 1px #dce3eb;
  transform: none;
}

.resume-icon {
  display: inline-flex;
  width: 46px;
  height: 46px;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  background: #e4f2ef;
  color: #1d6f6f;
  font-size: 24px;
}

.resume-card.empty .resume-icon {
  background: #edf2f7;
}

.resume-copy {
  display: grid;
  min-width: 0;
  gap: 5px;
}

.resume-copy span {
  color: #6b7785;
  font-size: 12px;
  font-weight: 700;
}

.resume-card.empty .resume-copy span {
  color: #8995a3;
}

.resume-copy strong {
  overflow: hidden;
  color: inherit;
  font-size: 14px;
  font-weight: 800;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.resume-action {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 6px 9px;
  border-radius: 999px;
  background: #e4f2ef;
  color: #1d6f6f;
  font-size: 12px;
  font-weight: 800;
}

.resume-card.empty .resume-action {
  background: #edf2f7;
  color: #8995a3;
}

.eyebrow {
  color: #1d6f6f;
  font-size: 12px;
  font-weight: 700;
}

.profile-metrics {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  overflow: hidden;
  border: 1px solid #e1e8f0;
  border-radius: 8px;
}

.metric-cell {
  min-width: 0;
  padding: 14px;
  border-right: 1px solid #e1e8f0;
  background: #fbfcfe;
}

.metric-cell:last-child {
  border-right: 0;
}

.metric-cell span,
.remark-box span {
  display: block;
  color: #718092;
  font-size: 12px;
}

.metric-cell strong {
  display: block;
  margin-top: 8px;
  overflow-wrap: anywhere;
  color: #21384f;
  font-size: 15px;
}

.remark-box {
  margin-top: 14px;
  padding: 14px;
  border: 1px solid #e1e8f0;
  border-radius: 8px;
  background: #ffffff;
}

.remark-box p {
  margin: 8px 0 0;
  color: #30465b;
}

.status-steps {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 8px;
}

.status-step {
  display: flex;
  min-height: 58px;
  align-items: center;
  gap: 8px;
  padding: 12px;
  border: 1px solid #dce3eb;
  border-radius: 8px;
  color: #6b7785;
  background: #ffffff;
}

.step-dot {
  display: inline-flex;
  width: 24px;
  height: 24px;
  flex: 0 0 auto;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: #eef2f6;
  color: #506071;
  font-size: 12px;
  font-weight: 700;
}

.status-step.done {
  border-color: #b9dccf;
  color: #1d6f6f;
  background: #f1f8f5;
}

.status-step.done .step-dot,
.status-step.current .step-dot {
  background: #1d6f6f;
  color: #ffffff;
}

.status-step.current {
  border-color: #1d6f6f;
  color: #1f354a;
  background: #ffffff;
  box-shadow: 0 6px 18px rgb(29 111 111 / 10%);
}

.action-buttons {
  display: grid;
  grid-template-columns: 1fr;
  gap: 10px;
}

.action-buttons .el-button {
  width: 100%;
  margin-left: 0;
}

.terminal-state {
  display: grid;
  justify-items: center;
  gap: 8px;
  padding: 22px 14px;
  border: 1px solid #cfe8d6;
  border-radius: 8px;
  color: #2f6f45;
  background: #f3faf5;
  text-align: center;
}

.terminal-state.rejected {
  border-color: #f2c8c8;
  color: #a33c3c;
  background: #fff6f6;
}

.terminal-state .el-icon {
  font-size: 28px;
}

.terminal-state span {
  color: #718092;
  line-height: 1.5;
}

.history-panel :deep(.el-timeline) {
  padding-left: 6px;
}

.history-panel strong {
  color: #1f354a;
}

.history-panel p {
  margin: 6px 0 0;
  color: #6b7785;
  line-height: 1.5;
}

.dialog-tip {
  margin-bottom: 16px;
}

.interview-table {
  width: 100%;
}

.resume-preview {
  min-height: 70vh;
}

.resume-frame {
  display: block;
  width: 100%;
  height: 70vh;
  border: 1px solid #dce3eb;
  border-radius: 8px;
  background: #ffffff;
}

@media (max-width: 1180px) {
  .detail-layout {
    grid-template-columns: 1fr;
  }

  .detail-side {
    grid-template-columns: 1fr 1fr;
  }
}

@media (max-width: 760px) {
  .profile-metrics,
  .status-steps,
  .detail-side {
    grid-template-columns: 1fr;
  }

  .profile-head {
    align-items: stretch;
    flex-direction: column;
  }

  .profile-side {
    align-items: stretch;
    justify-items: stretch;
    width: 100%;
  }
}
</style>
