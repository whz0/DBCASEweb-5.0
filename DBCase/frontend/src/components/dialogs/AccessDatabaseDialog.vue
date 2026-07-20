<script setup lang="ts">
import { useToast } from 'primevue'
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'

import { DatabaseType, useDatabaseStore } from '@/stores/databaseStore.ts'

const props = defineProps<{
  visible: boolean
  databaseType: string
  code: string
}>()

const emits = defineEmits<{
  (e: 'update:visible', value: boolean): void
}>()

const { t } = useI18n()
const toast = useToast()
const toastMessage = (message: string, severity: 'error' | 'warn' | 'info' | 'success') =>
  toast.add({ severity, detail: message, life: 3000 })

const databaseStore = useDatabaseStore()

const host = ref('')
const port = ref<number | null>(null)
const dbName = ref('')
const username = ref('')
const password = ref('')

const handleDeploy = () => {
  const dbType = props.databaseType?.toLowerCase() as DatabaseType
  if (!dbType) return
  databaseStore.deploy(
    {
      databaseType: dbType,
      host: host.value,
      port: port.value,
      databaseName: dbName.value,
    },
    username.value,
    password.value,
    props.code,
    toastMessage,
  )
}

const handleTest = () => {
  const dbType = props.databaseType?.toLowerCase() as DatabaseType
  if (!dbType) return
  databaseStore.test(
    {
      databaseType: dbType,
      host: host.value,
      port: port.value,
      databaseName: dbName.value,
    },
    username.value,
    password.value,
    toastMessage,
  )
}
</script>

<template>
  <Dialog
    :visible="props.visible"
    @update:visible="emits('update:visible', false)"
    :header="t('database.deployTitle')"
    modal
    :style="{ width: '28rem' }"
  >
    <div class="flex flex-col gap-3 pt-2">
      <div class="flex flex-col gap-1">
        <label class="text-sm font-medium">{{ t('database.host') }}</label>
        <InputText v-model="host" />
      </div>
      <div class="flex flex-col gap-1">
        <label class="text-sm font-medium">{{ t('database.port') }}</label>
        <InputNumber v-model="port" :useGrouping="false" />
      </div>
      <div class="flex flex-col gap-1">
        <label class="text-sm font-medium">{{ t('database.databaseName') }}</label>
        <InputText v-model="dbName" />
      </div>
      <div class="flex flex-col gap-1">
        <label class="text-sm font-medium">{{ t('database.username') }}</label>
        <InputText v-model="username" />
      </div>
      <div class="flex flex-col gap-1">
        <label class="text-sm font-medium">{{ t('database.password') }}</label>
        <Password v-model="password" toggleMask />
      </div>
    </div>
    <template #footer>
      <Button :label="t('database.test')" @click="handleTest" />
      <Button :label="t('database.deploy')" icon="bi bi-play-circle" @click="handleDeploy" />
    </template>
  </Dialog>
</template>

<style scoped></style>
