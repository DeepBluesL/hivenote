<template>
  <div class="page-container">
    <NavBar />
    <div class="main-content">
      <el-card class="create-card" v-loading="pageLoading">
        <template #header>
          <div class="card-header">
            <el-button link @click="$router.back()"><el-icon><ArrowLeft /></el-icon> 返回</el-button>
            <h2>编辑帖子</h2>
            <div></div>
          </div>
        </template>

        <el-form v-if="!pageLoading" :model="form" :rules="rules" ref="formRef" label-width="80px" size="large">
          <el-form-item label="标题" prop="title">
            <el-input v-model="form.title" placeholder="请输入帖子标题（2-50字）" maxlength="50" show-word-limit />
          </el-form-item>

          <el-form-item label="内容" prop="content">
            <el-input
              v-model="form.content"
              type="textarea"
              placeholder="请输入帖子内容..."
              :autosize="{ minRows: 8, maxRows: 20 }"
              maxlength="5000"
              show-word-limit
            />
          </el-form-item>

          <el-form-item label="图片">
            <div class="image-upload-area">
              <div class="image-preview-list">
                <div v-for="(img, index) in uploadedImages" :key="index" class="image-preview-item">
                  <el-image :src="img" fit="cover" class="preview-img" />
                  <div class="remove-btn" @click="removeImage(index)">
                    <el-icon><Close /></el-icon>
                  </div>
                </div>
                <div v-if="uploadedImages.length < 9" class="upload-trigger" @click="triggerUpload">
                  <el-icon :size="28" color="#c0c4cc"><Plus /></el-icon>
                  <span>上传图片</span>
                </div>
              </div>
              <input
                ref="fileInputRef"
                type="file"
                accept="image/jpeg,image/png,image/gif,image/webp"
                style="display: none"
                @change="handleFileSelect"
              />
              <p class="upload-hint">最多上传 9 张图片，支持 JPG/PNG/GIF/WEBP，单张不超过 10MB</p>
            </div>
          </el-form-item>

          <el-form-item>
            <div class="submit-bar">
              <el-button @click="$router.back()">取消</el-button>
              <el-button type="primary" @click="handleSubmit" :loading="submitting" size="large">
                保存修改
              </el-button>
            </div>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import NavBar from '../../components/NavBar.vue'
import { ArrowLeft, Close, Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getPostDetail, editPost, uploadPostImage } from '@/api/post'
import { getToken } from '@/utils/auth'

const router = useRouter()
const route = useRoute()
const formRef = ref(null)
const fileInputRef = ref(null)
const submitting = ref(false)
const pageLoading = ref(true)
const uploadedImages = ref([])
const postId = ref(null)

const form = ref({
  title: '',
  content: ''
})

const rules = {
  title: [
    { required: true, message: '请输入帖子标题', trigger: 'blur' },
    { min: 2, max: 50, message: '标题长度 2-50 字', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入帖子内容', trigger: 'blur' },
    { min: 2, message: '内容至少 2 个字', trigger: 'blur' }
  ]
}

const loadPost = async () => {
  pageLoading.value = true
  try {
    if (!getToken()) {
      ElMessage.warning('请先登录')
      router.push('/login')
      return
    }

    const res = await getPostDetail(route.params.id)
    if (res && res.success && res.result) {
      const post = res.result
      postId.value = post.id
      form.value.title = post.title || ''
      form.value.content = post.content || ''
      if (post.images) {
        uploadedImages.value = post.images.split(',').filter(s => s.trim())
      }
    } else {
      ElMessage.error('帖子不存在或已被删除')
      router.push('/user/profile')
    }
  } catch (e) {
    ElMessage.error('加载帖子失败')
    router.push('/user/profile')
  } finally {
    pageLoading.value = false
  }
}

const triggerUpload = () => {
  fileInputRef.value?.click()
}

const handleFileSelect = async (e) => {
  const file = e.target.files[0]
  if (!file) return
  e.target.value = ''

  if (file.size > 10 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过 10MB')
    return
  }

  const allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
  if (!allowedTypes.includes(file.type)) {
    ElMessage.error('只支持 JPG、PNG、GIF、WEBP 格式')
    return
  }

  try {
    const res = await uploadPostImage(file)
    if (res && res.success) {
      uploadedImages.value.push(res.result.url)
      ElMessage.success('图片上传成功')
    }
  } catch (err) {
    ElMessage.error('图片上传失败')
  }
}

const removeImage = (index) => {
  uploadedImages.value.splice(index, 1)
}

const handleSubmit = async () => {
  if (!formRef.value) return
  try {
    const valid = await formRef.value.validate()
    if (!valid) return

    submitting.value = true
    const data = {
      id: postId.value,
      title: form.value.title,
      content: form.value.content,
      images: uploadedImages.value.join(',')
    }

    const res = await editPost(data)
    if (res && res.success) {
      ElMessage.success('帖子修改成功！')
      router.push(`/community/post/${postId.value}`)
    } else {
      ElMessage.error(res?.message || '修改失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('修改失败，请稍后重试')
    }
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadPost()
})
</script>

<style scoped>
.page-container { min-height: 100vh; background: #f0f2f5; }
.main-content { max-width: 800px; margin: 30px auto; padding: 0 20px; }

.create-card { border-radius: 12px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.card-header h2 { margin: 0; font-size: 20px; }

.image-upload-area { width: 100%; }
.image-preview-list { display: flex; flex-wrap: wrap; gap: 12px; }
.image-preview-item {
  position: relative; width: 100px; height: 100px;
  border-radius: 8px; overflow: hidden; border: 1px solid #e4e7ed;
}
.preview-img { width: 100%; height: 100%; }
.remove-btn {
  position: absolute; top: 2px; right: 2px;
  width: 22px; height: 22px; border-radius: 50%;
  background: rgba(0,0,0,0.5); color: #fff;
  display: flex; align-items: center; justify-content: center;
  cursor: pointer; font-size: 12px; transition: background 0.2s;
}
.remove-btn:hover { background: #f56c6c; }

.upload-trigger {
  width: 100px; height: 100px; border: 2px dashed #dcdfe6;
  border-radius: 8px; display: flex; flex-direction: column;
  align-items: center; justify-content: center; gap: 4px;
  cursor: pointer; transition: border-color 0.3s; font-size: 12px; color: #c0c4cc;
}
.upload-trigger:hover { border-color: #409EFF; color: #409EFF; }

.upload-hint { color: #909399; font-size: 12px; margin: 8px 0 0; }

.submit-bar { display: flex; gap: 12px; justify-content: flex-end; width: 100%; }
</style>
