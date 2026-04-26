<script setup lang="ts">
import { useToast } from 'primevue'
import { ref } from 'vue'

import { DatabaseType, useDatabaseStore } from '@/stores/databaseStore.ts'

const props = defineProps<{
  visible: boolean
  databaseType: string
  code: string
}>()

const emits = defineEmits<{
  (e: 'update:visible', value: boolean): void
}>()

const toast = useToast()
const toastMessage = (message: string, severity: 'error' | 'warn' | 'info' | 'success') =>
  toast.add({ severity, detail: message, life: 3000 })

const databaseStore = useDatabaseStore()

const host = ref('')
const port = ref<number | null>(null)
const dbName = ref('')
const username = ref('')
const password = ref('')

const getUrl = () => {
  const dbType = props.databaseType?.toLowerCase()
  if (dbType === 'mysql') {
    return `jdbc:mysql://${host.value}:${port.value ?? 3306}/${dbName.value}`
  } else if (dbType === 'postgresql') {
    return `jdbc:postgresql://${host.value}:${port.value ?? 5432}/${dbName.value}`
  }
  return ''
}

const handleDeploy = () => {
  const dbType = props.databaseType?.toLowerCase() as DatabaseType
  if (!dbType) return
  databaseStore.desploy(dbType, getUrl(), username.value, password.value, props.code, toastMessage)
}

const handleTest = () => {
  databaseStore.test(getUrl(), username.value, password.value, toastMessage)
}
</script>

<template>
  <Dialog
    :visible="props.visible"
    @update:visible="emits('update:visible', false)"
    header="Deploy to Database"
    modal
    :style="{ width: '28rem' }"
  >
    <div class="flex flex-col gap-3 pt-2">
      <div class="flex flex-col gap-1">
        <label class="text-sm font-medium">Host</label>
        <InputText v-model="host" />
      </div>
      <div class="flex flex-col gap-1">
        <label class="text-sm font-medium">Port</label>
        <InputNumber v-model="port" />
      </div>
      <div class="flex flex-col gap-1">
        <label class="text-sm font-medium">Database name</label>
        <InputText v-model="dbName" />
      </div>
      <div class="flex flex-col gap-1">
        <label class="text-sm font-medium">Username</label>
        <InputText v-model="username" />
      </div>
      <div class="flex flex-col gap-1">
        <label class="text-sm font-medium">Password</label>
        <Password v-model="password" toggleMask />
      </div>
    </div>
    <template #footer>
      <Button label="Test" @click="handleTest" />
      <Button label="Deploy" icon="bi bi-play-circle" @click="handleDeploy" />
    </template>
  </Dialog>
</template>

<style scoped></style>
