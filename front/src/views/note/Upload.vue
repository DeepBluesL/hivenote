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

          <!-- 标签选择 -->
          <el-form-item label="笔记标签（可选，最多选5个）">
            <div class="tags-section">
              <!-- 已选标签展示 -->
              <div class="selected-tags" v-if="form.tags.length > 0">
                <el-tag
                  v-for="tag in form.tags"
                  :key="tag"
                  closable
                  type="primary"
                  effect="light"
                  @close="removeTag(tag)"
                  class="selected-tag"
                >{{ tag }}</el-tag>
              </div>

              <!-- 快捷标签分组 -->
              <div class="tag-groups">
                <div class="tag-group">
                  <span class="tag-group-label">📚 考试相关：</span>
                  <div class="tag-group-items">
                    <span
                      v-for="tag in examRelatedTags"
                      :key="tag"
                      class="quick-tag"
                      :class="{ selected: form.tags.includes(tag), disabled: !form.tags.includes(tag) && form.tags.length >= 5 }"
                      @click="toggleTag(tag)"
                    >{{ tag }}</span>
                  </div>
                </div>

                <div class="tag-group">
                  <span class="tag-group-label">💻 课程相关：</span>
                  <div class="tag-group-items">
                    <span
                      v-for="tag in courseRelatedTags"
                      :key="tag"
                      class="quick-tag"
                      :class="{ selected: form.tags.includes(tag), disabled: !form.tags.includes(tag) && form.tags.length >= 5 }"
                      @click="toggleTag(tag)"
                    >{{ tag }}</span>
                  </div>
                </div>

                <div class="tag-group">
                  <span class="tag-group-label">📝 内容类型：</span>
                  <div class="tag-group-items">
                    <span
                      v-for="tag in contentTypeTags"
                      :key="tag"
                      class="quick-tag"
                      :class="{ selected: form.tags.includes(tag), disabled: !form.tags.includes(tag) && form.tags.length >= 5 }"
                      @click="toggleTag(tag)"
                    >{{ tag }}</span>
                  </div>
                </div>
              </div>

              <!-- 自定义标签输入 -->
              <div class="custom-tag-input">
                <el-input
                  v-model="customTagInput"
                  placeholder="输入自定义标签，按回车添加"
                  size="default"
                  style="width: 260px"
                  @keyup.enter="addCustomTag"
                  :disabled="form.tags.length >= 5"
                >
                  <template #append>
                    <el-button @click="addCustomTag" :disabled="form.tags.length >= 5">添加</el-button>
                  </template>
                </el-input>
                <span class="tag-count">{{ form.tags.length }}/5</span>
              </div>
            </div>
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
const customTagInput = ref('')

const examRelatedTags = ['期末复习', '考试重点', '期末冲刺', '真题解析', '考前速记', '知识总结', '高分笔记']
const courseRelatedTags = ['数据结构', '算法', '操作系统', '计算机网络', '数据库', '高等数学', '线性代数', '概率统计', '离散数学', '编译原理', '软件工程', '人工智能']
const contentTypeTags = ['课堂笔记', '实验报告', '学习心得', '编程实践', '项目经验', '论文写作', '思维导图', '公式整理', '代码示例']

const form = reactive({
  title: '',
  category: '',
  school: '',
  summary: '',
  coverFile: null,
  tags: [],
  file: null
})

const toggleTag = (tag) => {
  const idx = form.tags.indexOf(tag)
  if (idx > -1) {
    form.tags.splice(idx, 1)
  } else if (form.tags.length < 5) {
    form.tags.push(tag)
  }
}

const removeTag = (tag) => {
  const idx = form.tags.indexOf(tag)
  if (idx > -1) {
    form.tags.splice(idx, 1)
  }
}

const addCustomTag = () => {
  const tag = customTagInput.value.trim()
  if (!tag) return
  if (form.tags.length >= 5) {
    ElMessage.warning('最多只能添加5个标签')
    return
  }
  if (form.tags.includes(tag)) {
    ElMessage.warning('该标签已存在')
    return
  }
  if (tag.length > 10) {
    ElMessage.warning('标签长度不能超过10个字符')
    return
  }
  form.tags.push(tag)
  customTagInput.value = ''
}

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

const beforeUpload = (file) => {
  const allowedTypes = [
    'application/pdf',
    'video/mp4',
    'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
    'application/zip',
    'application/x-zip-compressed'
  ]
  
  const isAllowedType = allowedTypes.includes(file.type)
  if (!isAllowedType) {
    ElMessage.error('只支持上传 PDF、MP4、DOCX、ZIP 格式的文件！')
    return false
  }
  
  const isLt50M = file.size / 1024 / 1024 < 50
  if (!isLt50M) {
    ElMessage.error('文件大小不能超过 50MB！')
    return false
  }
  
  return true
}

const onSubmit = async () => {
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
    console.log('开始上传文件:', form.file.name)
    const fileRes = await uploadFile(form.file)
    
    if (!fileRes || !fileRes.success) {
      throw new Error(fileRes?.message || '文件上传失败')
    }
    
    console.log('文件上传成功:', fileRes.result.url)

    // 上传封面图片（如果有）
    let coverUrl = ''
    if (form.coverFile) {
      const coverRes = await uploadFile(form.coverFile)
      if (coverRes && coverRes.success) {
        coverUrl = coverRes.result.url
      }
    }


    const noteData = {
      title: form.title,
      category: form.category,
      school: form.school || '',
      summary: form.summary || '',
      fileUrl: fileRes.result.url,
      coverUrl: coverUrl,
      tags: form.tags.join(',')
    }
    
    console.log('提交笔记数据:', noteData)
    const res = await addNote(noteData)
    
    if (res && res.success) {
      ElMessage.success(res.message || '发布成功！')
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

/* 标签选择区样式 */
.tags-section {
  width: 100%;
}

.selected-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
  padding: 12px;
  background: #f0f9ff;
  border-radius: 8px;
  border: 1px dashed #b3d8ff;
}
.selected-tag {
  font-size: 13px;
}

.tag-groups {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 16px;
}
.tag-group {
  display: flex;
  align-items: flex-start;
  gap: 8px;
}
.tag-group-label {
  font-size: 13px;
  color: #606266;
  white-space: nowrap;
  padding-top: 4px;
  min-width: 100px;
}
.tag-group-items {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}
.quick-tag {
  padding: 4px 12px;
  font-size: 12px;
  background: #f4f4f5;
  color: #606266;
  border-radius: 12px;
  cursor: pointer;
  border: 1px solid #e0e0e0;
  transition: all 0.2s;
  user-select: none;
}
.quick-tag:hover:not(.disabled) {
  border-color: #409EFF;
  color: #409EFF;
}
.quick-tag.selected {
  background: #409EFF;
  color: #fff;
  border-color: #409EFF;
}
.quick-tag.disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.custom-tag-input {
  display: flex;
  align-items: center;
  gap: 12px;
}
.tag-count {
  font-size: 13px;
  color: #909399;
}
</style>
