<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { uploadUserAvatar } from '../api'

const router = useRouter()
const loading = ref(false)
const avatarUrl = ref('')
const loginFormRef = ref(null)
const currentUser = ref(readStoredUser())

const loginForm = reactive({
  id: '',
  no: '',
  password: '',
  name: '',
  account: '',
  roleid: '',
  image: ''
})

const avatarSrc = computed(() => fixAvatarUrl(avatarUrl.value || loginForm.image || currentUser.value?.avatarUrl))

const rules = {
  no: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    { min: 1, max: 20, message: '用户名长度在1-20之间', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 1, message: '密码至少1位', trigger: 'blur' }
  ]
}

onMounted(() => {
  initUserData()
})

function readStoredUser() {
  const raw = sessionStorage.getItem('User') || localStorage.getItem('nju-market-user')
  if (!raw) return null

  try {
    return JSON.parse(raw)
  } catch {
    return null
  }
}

function persistUser(user) {
  if (!user) return
  currentUser.value = user
  sessionStorage.setItem('User', JSON.stringify(user))
  localStorage.setItem('nju-market-user', JSON.stringify(user))
}

async function handleAvatarChange(uploadFile) {

  const file = uploadFile.raw
  if (!file) {
    ElMessage.error('miss')
    console.log('上传文件错误:', error)
     return
  }
  console.log('我是分割线！')
  if (!file.type.startsWith('image/')) {
    ElMessage.error('请选择图片文件')
    return
  }

  if (file.size > 2 * 1024 * 1024) {
    ElMessage.error('头像不能超过 2MB')
    return
  }

  const userId = currentUser.value?.userId || currentUser.value?.id || loginForm.id
  if (!userId) {
    ElMessage.error('用户信息无效，请重新登录')
    return
  }

  loading.value = true
  try {
    const result = await uploadUserAvatar(userId, file)
    const updatedUser = {
      ...currentUser.value,
      ...result.data,
    }
    loginForm.image = updatedUser.avatarUrl || ''
    avatarUrl.value = updatedUser.avatarUrl || ''
    persistUser(updatedUser)
    ElMessage.success('头像更新成功')
  } catch (error) {
    ElMessage.error(error.message || '头像上传失败')
  } finally {
    loading.value = false
  }
}

function initUserData() {
  const user = readStoredUser()
  if (user) {
    currentUser.value = user
    loginForm.id = user.id || user.userId || ''
    loginForm.no = user.username || user.name || user.no || ''
    loginForm.name = user.username || user.name || ''
    loginForm.account = user.account || ''
    loginForm.roleid = user.roleid || ''

    const avatar = user.avatarUrl || user.avatar || user.image || ''
    loginForm.image = avatar
    avatarUrl.value = avatar

    console.log('初始化用户数据:', loginForm)
  }
}

function fixAvatarUrl(url) {
  if (!url) return ''
  if (url.startsWith('http') || url.startsWith('blob:') || url.startsWith('data:')) return url
  if (url.startsWith('/uploads/')) return `http://localhost:8080${url}`
  if (url.startsWith('/')) return url
  return `http://localhost:8080/uploads/avatars/${url}`
}

function toHome() {
  router.push('/FrontPage')
}



async function confirm() {
  if (!loginFormRef.value) return

  try {
    await loginFormRef.value.validate()
  } catch (error) {
    ElMessage.warning('请完善表单信息')
    return
  }

  loading.value = true

  const user = readStoredUser()
  if (!user || (!user.id && !user.userId)) {
    ElMessage.error('用户信息无效，请重新登录')
    loading.value = false
    return
  }

  const userId = user.id || user.userId
  const updateData = {
    username: loginForm.no,
    password: loginForm.password || undefined,
  }

  console.log('提交的用户数据:', updateData)

  try {
    const response = await fetch(`/api/users/${userId}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(updateData)
    })

    const res = await response.json()
    console.log('用户信息修改响应:', res)

    if (res.success === true) {
      const updatedUser = {
        ...user,
        ...(res.data || {}),
        username: loginForm.no,
        avatarUrl: res.data?.avatarUrl || currentUser.value?.avatarUrl || user.avatarUrl || loginForm.image,
      }
      persistUser(updatedUser)

      ElMessage.success('信息修改成功！')
      router.push('/FrontPage')
    } else {
      ElMessage.error(res.message || '修改失败，请重试')
    }
  } catch (error) {
    console.error('请求失败:', error)
    ElMessage.error('网络错误，请稍后重试')
  } finally {
    loading.value = false
  }
}
</script>





<template>
  <div class="update-body">
    <div class="update-div">
      <div class="update-content">
        <div class="header-section">
          <div class="avatar-section">
            <el-upload
                :auto-upload="false"
                :show-file-list="false"
                accept="image/png,image/jpeg,image/gif,image/webp"
                :on-change="handleAvatarChange"
            >
              <el-avatar :size="80" :src="avatarSrc">U</el-avatar>
              <el-button>更换头像</el-button>
            </el-upload>

          </div>
          <h1 class="update-title">修改账户信息</h1>
          <p class="update-subtitle">更新您的账号、密码和头像</p>
        </div>

        <el-form
            :model="loginForm"
            label-width="80px"
            :rules="rules"
            ref="loginFormRef"
            class="update-form"
        >
          <el-form-item label="账号" prop="no">
            <el-input
                v-model="loginForm.no"
                placeholder="请输入新账号"
                size="large"
                clearable
            >
              <template #prefix>
                <el-icon><User /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item label="密码" prop="password">
            <el-input
                v-model="loginForm.password"
                type="password"
                show-password
                placeholder="请输入新密码"
                size="large"
                clearable
                @keyup.enter="confirm"
            >
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item class="button-group">
            <el-button
                type="primary"
                @click="confirm"
                :loading="loading"
                class="confirm-btn"
                size="large"
            >
              <el-icon><Check /></el-icon>
              确认修改
            </el-button>
            <el-button @click="toHome" class="back-btn" size="large">
              <el-icon><Back /></el-icon>
              返回
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<style scoped>
.update-body {
  position: absolute;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  justify-content: center;
  align-items: center;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', Arial, sans-serif;
}

.update-div {
  width: 500px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.15);
  overflow: hidden;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.update-div:hover {
  transform: translateY(-5px);
  box-shadow: 0 25px 50px rgba(0, 0, 0, 0.2);
}

.update-content {
  padding: 40px;
}

.header-section {
  text-align: center;
  margin-bottom: 30px;
}

.avatar-section {
  display: flex;
  justify-content: center;
  margin-bottom: 20px;
}

.avatar-uploader {
  text-align: center;
  cursor: pointer;
}

.avatar-container {
  position: relative;
  width: 120px;
  height: 120px;
  border-radius: 50%;
  overflow: hidden;
  border: 2px dashed #dcdfe6;
  transition: all 0.3s ease;
}

.avatar-container:hover {
  border-color: #667eea;
  transform: scale(1.05);
}

.user-avatar {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.avatar-container:hover .avatar-overlay {
  opacity: 1;
}

.avatar-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #c0c4cc;
  transition: color 0.3s ease;
  width: 120px;
  height: 120px;
  border: 2px dashed #dcdfe6;
  border-radius: 50%;
}

.avatar-uploader:hover .avatar-placeholder {
  color: #667eea;
  border-color: #667eea;
}

.avatar-icon {
  margin-bottom: 8px;
}

.avatar-text {
  font-size: 12px;
  text-align: center;
}

.update-title {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.update-subtitle {
  margin: 0;
  font-size: 14px;
  color: #909399;
}

.update-form {
  margin-top: 20px;
}

.el-form-item {
  margin-bottom: 24px;
}

.button-group {
  display: flex;
  justify-content: center;
  gap: 15px;
  margin-top: 30px;
}

.confirm-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 8px;
  padding: 12px 32px;
  font-weight: 500;
  transition: all 0.3s ease;
  min-width: 120px;
}

.confirm-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(102, 126, 234, 0.4);
}

.back-btn {
  border-radius: 8px;
  padding: 12px 32px;
  font-weight: 500;
  transition: all 0.3s ease;
  min-width: 120px;
  border: 1px solid #dcdfe6;
}

.back-btn:hover {
  background-color: #f5f7fa;
  transform: translateY(-2px);
  border-color: #c0c4cc;
}

:deep(.el-input__wrapper) {
  border-radius: 8px;
  transition: all 0.3s ease;
  box-shadow: 0 0 0 1px #e4e7ed inset;
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #c0c4cc inset;
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #667eea inset;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: #606266;
}

@media (max-width: 768px) {
  .update-div {
    width: 90%;
    margin: 0 auto;
  }

  .update-content {
    padding: 30px 25px;
  }

  .button-group {
    flex-direction: column;
    align-items: center;
  }

  .confirm-btn, .back-btn {
    width: 100%;
    max-width: 200px;
  }

  .avatar-container, .avatar-placeholder {
    width: 100px;
    height: 100px;
  }
}
</style>
