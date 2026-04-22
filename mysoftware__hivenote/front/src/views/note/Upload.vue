<template>
  <div class="page-container">
    <NavBar />
    
    <div class="main-content">
      <el-card class="upload-card">
        <template #header>
          <div class="card-header">
            <span><el-icon><UploadFilled /></el-icon> 发布新笔记</span>
          </div>
        </template>

        <el-form :model="form" label-position="top" size="large">
          <el-form-item label="笔记标题">
            <el-input v-model="form.title" placeholder="请输入清晰的笔记标题，例如：CS101 期末复习大纲" />
          </el-form-item>

          <el-form-item label="所属分类">
            <el-select v-model="form.category" placeholder="请选择课程分类" style="width: 100%">
              <el-option label="CS - 计算机科学" value="CS" />
              <el-option label="SE - 软件工程" value="SE" />
              <el-option label="Math - 应用数学" value="Math" />
              <el-option label="GE - 通识教育" value="GE" />
            </el-select>
          </el-form-item>

          <el-form-item label="所属学校（可选）">
            <el-select
              v-model="form.school"
              placeholder="搜索并选择学校"
              style="width: 100%"
              filterable
              remote
              :remote-method="handleSchoolSearch"
              clearable
              :loading="schoolLoading"
            >
              <el-option
                v-for="s in schoolOptions"
                :key="s"
                :label="s"
                :value="s"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="笔记简介">
            <el-input v-model="form.summary" type="textarea" :rows="4" placeholder="简单介绍一下这份笔记包含的内容..." />
          </el-form-item>

          <el-form-item label="封面图片（可选）">
            <el-upload
              class="cover-upload"
              action="#"
              :auto-upload="false"
              :limit="1"
              :on-change="handleCoverChange"
              :on-remove="handleCoverRemove"
              accept=".png,.jpg,.jpeg,.gif,.webp"
              list-type="picture-card"
            >
              <el-icon class="el-icon--upload"><Plus /></el-icon>
              <template #tip>
                <div class="el-upload__tip">支持 PNG、JPG、GIF、WEBP 格式，建议 16:9 比例，不超过 5MB</div>
              </template>
            </el-upload>
          </el-form-item>

          <el-form-item label="上传文件 (PDF/MP4/DOCX/ZIP)">
            <el-upload
              class="upload-demo"
              drag
              action="#"
              :auto-upload="false"
              :limit="1"
              :on-change="handleFileChange"
              accept=".pdf,.mp4,.docx,.zip"
              :before-upload="beforeUpload"
            >
              <el-icon class="el-icon--upload"><upload-filled /></el-icon>
              <div class="el-upload__text">
                拖拽文件到这里 或 <em>点击上传</em>
              </div>
              <template #tip>
                <div class="el-upload__tip">支持 PDF、MP4、DOCX、ZIP 格式，单个文件不超过 50MB</div>
              </template>
            </el-upload>
          </el-form-item>

          <el-form-item>
            <el-button type="primary" class="submit-btn" @click="onSubmit" :loading="uploading">立即发布</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { UploadFilled, Plus } from '@element-plus/icons-vue'
import NavBar from '../../components/NavBar.vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { addNote } from '@/api/note'
import { getSchoolList } from '@/api/school'
import { getUserInfo as getStoredUserInfo } from '@/utils/auth'
import request from '@/utils/request'

const router = useRouter()
const uploading = ref(false)
const schoolLoading = ref(false)
const schoolOptions = ref([])

const form = reactive({
  title: '',
  category: '',
  school: '',
  summary: '',
  coverFile: null,
  file: null
})

const handleSchoolSearch = async (query) => {
  if (!query) {
    schoolOptions.value = []
    return
  }
  schoolLoading.value = true
  try {
    const res = await getSchoolList(query)
    if (res && res.success) {
      schoolOptions.value = res.result || []
    }
  } catch (e) {
    console.error('获取学校列表错误:', e)
  } finally {
    schoolLoading.value = false
  }
}

const initSchool = () => {
  const stored = getStoredUserInfo()
  if (stored && stored.school) {
    form.school = stored.school
    schoolOptions.value = [stored.school]
  }
}

// 上传文件到服务器
const uploadFile = async (file) => {
  const formData = new FormData()
  formData.append('file', file)
  
  return request({
    url: '/file/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

const handleCoverChange = (file) => {
  const allowedTypes = ['image/png', 'image/jpeg', 'image/gif', 'image/webp']
  if (!allowedTypes.includes(file.raw.type)) {
    ElMessage.error('封面只支持 PNG、JPG、GIF、WEBP 格式')
    return false
  }
  if (file.raw.size / 1024 / 1024 > 5) {
    ElMessage.error('封面图片大小不能超过 5MB')
    return false
  }
  form.coverFile = file.raw
}

const handleCoverRemove = () => {
  form.coverFile = null
}

const handleFileChange = (file) => {
  form.file = file.raw
}

// 文件上传前的校验
const beforeUpload = (file) => {
  // 检查文件类型
  const allowedTypes = [
    'application/pdf',                                                          // PDF
    'video/mp4',                                                                // MP4
    'application/vnd.openxmlformats-officedocument.wordprocessingml.document', // DOCX
    'application/zip',                                                          // ZIP
    'application/x-zip-compressed'                                              // ZIP (兼容)
  ]
  
  const isAllowedType = allowedTypes.includes(file.type)
  if (!isAllowedType) {
    ElMessage.error('只支持上传 PDF、MP4、DOCX、ZIP 格式的文件！')
    return false
  }
  
  // 检查文件大小（50MB）
  const isLt50M = file.size / 1024 / 1024 < 50
  if (!isLt50M) {
    ElMessage.error('文件大小不能超过 50MB！')
    return false
  }
  
  return true
}

const onSubmit = async () => {
  // 表单验证
  if (!form.title || !form.title.trim()) {
    return ElMessage.warning('请输入笔记标题')
  }
  if (!form.category) {
    return ElMessage.warning('请选择课程分类')
  }
  if (!form.file) {
    return ElMessage.warning('请上传文件')
  }

  uploading.value = true
  
  try {
    // 1. 先上传文件获取URL
    console.log('开始上传文件:', form.file.name)
    const fileRes = await uploadFile(form.file)
    
    if (!fileRes || !fileRes.success) {
      throw new Error(fileRes?.message || '文件上传失败')
    }
    
    console.log('文件上传成功:', fileRes.result.url)

    // 1.5 上传封面图片（如果有）
    let coverUrl = ''
    if (form.coverFile) {
      console.log('开始上传封面图片:', form.coverFile.name)
      const coverRes = await uploadFile(form.coverFile)
      if (coverRes && coverRes.success) {
        coverUrl = coverRes.result.url
        console.log('封面上传成功:', coverUrl)
      } else {
        console.warn('封面上传失败，继续发布笔记（无封面）')
      }
    }

    // 2. 提交笔记信息
    const noteData = {
      title: form.title,
      category: form.category,
      school: form.school || '',
      summary: form.summary || '',
      fileUrl: fileRes.result.url,
      coverUrl: coverUrl
    }
    
    console.log('提交笔记数据:', noteData)
    const res = await addNote(noteData)
    
    if (res && res.success) {
      ElMessage.success(res.message || '发布成功！')
      // 延迟跳转
      setTimeout(() => {
        router.push('/home')
      }, 1000)
    } else {
      ElMessage.error(res.message || '发布失败')
    }
  } catch (error) {
    console.error('发布笔记错误:', error)
    ElMessage.error(error.message || '发布失败，请稍后重试')
  } finally {
    uploading.value = false
  }
}

onMounted(() => {
  initSchool()
})
</script>

<style scoped>
.page-container { min-height: 100vh; background: #f5f7fa; }
.main-content { max-width: 800px; margin: 30px auto; padding: 0 20px; }
.card-header { font-size: 18px; font-weight: bold; display: flex; align-items: center; gap: 8px; }
.submit-btn { width: 100%; margin-top: 20px; }
.cover-upload :deep(.el-upload--picture-card) { width: 200px; height: 120px; }
.cover-upload :deep(.el-upload-list__item) { width: 200px; height: 120px; }
</style>