<script setup lang="ts">
import { ref } from 'vue'
import Dialog from 'primevue/dialog'
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'

const visible = ref(false)
const filename = ref('')

defineExpose({
  visible,
})
</script>

<template>
  <Button severity="secondary" class="bi bi-box-arrow-down" @click="visible = true" text v-tooltip.bottom="'Save schema'" />

  <Dialog
    :dismissable-mask="true"
    header="Save schema"
    :draggable="false"
    v-model:visible="visible"
    modal
    :style="{ width: '30rem' }"
  >
    <form @submit.prevent="filename.trim() && (console.log(filename), visible = false, filename = '')">
      <div class="flex flex-col gap-3">
        <label for="filename">Schema Name</label>
        <InputText id="filename" v-model="filename" placeholder="Enter schema name" autofocus />
      </div>
    </form>
    <template #footer>
      <Button
        label="Cancel"
        icon="bi bi-x-lg"
        severity="secondary"
        @click="
          visible = false;
          filename = '';
        "
      />
      <Button
        label="Save (.dbw)"
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

<style scoped></style>
