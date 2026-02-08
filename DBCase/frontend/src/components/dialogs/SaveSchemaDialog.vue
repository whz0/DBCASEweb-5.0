<script setup lang="ts">
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
import Dialog from 'primevue/dialog'
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'

const { t } = useI18n()
const visible = ref(false)
const filename = ref('')

defineExpose({
  visible,
})
</script>

<template>
  <Button severity="secondary" class="bi bi-box-arrow-down" @click="visible = true" text v-tooltip.bottom="t('toolbar.saveSchema')" />

  <Dialog
    :dismissable-mask="true"
    :header="t('toolbar.saveSchema')"
    :draggable="false"
    v-model:visible="visible"
    modal
    :style="{ width: '30rem' }"
  >
    <form @submit.prevent="filename.trim() && (console.log(filename), visible = false, filename = '')">
      <div class="flex flex-col gap-3">
        <label for="filename">{{ t('schema.schemaName') }}</label>
        <InputText id="filename" v-model="filename" :placeholder="t('schema.enterSchemaName')" autofocus />
      </div>
    </form>
    <template #footer>
      <Button
        :label="t('common.cancel')"
        icon="bi bi-x-lg"
        severity="secondary"
        @click="
          visible = false;
          filename = '';
        "
      />
      <Button
        :label="t('schema.save')"
        :disabled="!filename.trim()"
        icon="bi bi-check-lg"
        @click="
          console.log(filename);
          visible = false;
          filename = '';
        "
      />
    </template>
  </Dialog>
</template>
