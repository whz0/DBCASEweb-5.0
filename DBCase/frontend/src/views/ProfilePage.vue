<script setup lang="ts">
import { useToast } from 'primevue'
import { computed, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'

import ContentUnavailableView from '@/components/ContentUnavailableView.vue'
import { useAuthStore } from '@/stores/authStore'
import { useErSchemaStore } from '@/stores/erSchemaStore'
import { getAvatarColor, getUserInitial } from '@/utils/user'

const { t } = useI18n()
const router = useRouter()
const authStore = useAuthStore()
const erSchemaStore = useErSchemaStore()
const toast = useToast()

const user = computed(() => authStore.user)
const avatarError = ref(false)

const hasChart = computed(() => {
  const chart = authStore.user.chart
  return chart && chart !== 'null' && chart !== ''
})

const handleLoadDiagram = () => {
  if (erSchemaStore.loadFromProfile()) {
    toast.add({
      severity: 'success',
      summary: t('common.save'),
      detail: t('profile.loadDiagram'),
      life: 3000,
    })
    router.push('/')
  }
}

const handleSaveDiagram = async () => {
  const success = await erSchemaStore.saveToProfile()
  if (success) {
    toast.add({
      severity: 'success',
      summary: t('common.save'),
      detail: t('common.save'),
      life: 3000,
    })
  } else {
    toast.add({ severity: 'error', summary: 'Error', detail: 'Failed to save', life: 3000 })
  }
}

const handleLogout = () => {
  authStore.logout()
  router.replace('/login')
}

const isEditorEmpty = computed(() => {
  return (
    erSchemaStore.entities.length === 0 &&
    erSchemaStore.relationships.length === 0 &&
    erSchemaStore.attributes.length === 0
  )
})

const profileActions = computed(() => {
  if (isEditorEmpty.value) {
    return [
      {
        label: t('profile.backToEditor'),
        icon: 'bi bi-pencil-square',
        onClick: () => router.push('/'),
      },
    ]
  } else {
    return [
      {
        label: t('profile.saveCurrent'),
        icon: 'bi bi-cloud-upload',
        onClick: handleSaveDiagram,
      },
      {
        label: t('profile.backToEditor'),
        icon: 'bi bi-arrow-left',
        severity: 'secondary' as const,
        onClick: () => router.push('/'),
      },
    ]
  }
})
</script>

<template>
  <div class="min-h-screen bg-surface-50 dark:bg-surface-950 p-6 lg:p-12">
    <div class="max-w-5xl mx-auto flex flex-col gap-8">
      <div class="flex items-center">
        <Button
          icon="bi bi-arrow-left"
          :label="t('profile.backToEditor')"
          text
          plain
          @click="router.push('/')"
          class="hover:bg-surface-100! dark:hover:bg-surface-900!"
        />
      </div>

      <Card
        class="rounded-3xl! border! border-(--p-button-outlined-secondary-border-color) shadow-none! overflow-hidden"
      >
        <template #content>
          <div class="flex flex-col md:flex-row items-center gap-8 p-4">
            <div class="relative">
              <img
                v-if="user.pictureUrl && !avatarError"
                :src="user.pictureUrl"
                @error="avatarError = true"
                alt="Profile"
                class="w-32 h-32 rounded-3xl object-cover shadow-lg ring-4 ring-surface-100 dark:ring-surface-800"
              />
              <div
                v-else
                class="w-32 h-32 rounded-3xl flex items-center justify-center text-white font-bold text-5xl shadow-lg ring-4 ring-surface-100 dark:ring-surface-800"
                :style="{ backgroundColor: getAvatarColor(user.username || 'U') }"
              >
                {{ getUserInitial(user.username) }}
              </div>
              <div
                class="absolute -bottom-2 -right-2 w-8 h-8 bg-green-500 border-4 border-white dark:border-surface-900 rounded-full shadow-md"
              ></div>
            </div>

            <div class="flex-1 text-center md:text-left">
              <div class="flex items-center justify-center md:justify-start gap-3 mb-2">
                <h1 class="text-4xl font-bold tracking-tight">{{ user.username }}</h1>
                <Tag severity="info" value="Active" class="rounded-full! px-3!" />
              </div>
              <p class="text-xl text-muted-color mb-6">{{ t('profile.subtitle') }}</p>

              <div class="flex flex-wrap items-center justify-center md:justify-start gap-3">
                <Button
                  :label="t('common.logout')"
                  severity="danger"
                  outlined
                  icon="bi bi-box-arrow-right"
                  @click="handleLogout"
                  class="rounded-xl!"
                />
                <Button
                  :label="t('profile.saveCurrent')"
                  icon="bi bi-cloud-upload"
                  @click="handleSaveDiagram"
                  class="rounded-xl!"
                />
              </div>
            </div>
          </div>
        </template>
      </Card>

      <div class="flex flex-col gap-8">
        <!-- Saved Diagram Section -->
        <div class="flex flex-col gap-6">
          <h2 class="text-xl font-bold ml-2">{{ t('profile.savedDiagrams') }}</h2>
          <Card
            class="rounded-3xl! border! border-(--p-button-outlined-secondary-border-color) shadow-none! h-full"
          >
            <template #content>
              <div v-if="hasChart" class="flex flex-col items-center justify-center gap-6 py-12">
                <div class="w-24 h-24 rounded-full bg-primary/10 flex items-center justify-center">
                  <i class="bi bi-file-earmark-code text-5xl text-primary"></i>
                </div>
                <div class="text-center max-w-md">
                  <h3 class="text-2xl font-bold mb-2">{{ t('profile.erSnapshot') }}</h3>
                  <p class="text-muted-color mb-8">{{ t('profile.diagramInfo') }}</p>
                  <Button
                    :label="t('profile.loadDiagram')"
                    icon="bi bi-arrow-repeat"
                    size="large"
                    @click="handleLoadDiagram"
                    class="rounded-2xl! px-8!"
                  />
                </div>
              </div>
              <ContentUnavailableView
                v-else
                icon="bi bi-cloud-slash"
                :title="t('profile.noDiagrams')"
                :message="
                  isEditorEmpty
                    ? t('profile.noDiagramsMessage')
                    : t('profile.noDiagramsCloudMessage')
                "
                :actions="profileActions"
              />
            </template>
          </Card>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped></style>
