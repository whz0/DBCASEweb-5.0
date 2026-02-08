<script setup lang="ts">
import {ref} from 'vue';
import { useI18n } from 'vue-i18n';

const visible = ref(false);
const { t } = useI18n();

const emit = defineEmits(['generate'])

const props = defineProps({
  iAm: String,
});

const iAmER = ref(props.iAm == 'er')
const iAmLogical = ref(props.iAm == 'logical')
const iAmDB = ref(props.iAm == 'db')

</script>

<template>
  <Button severity="secondary" class="bi bi-magic" @click="visible = true" text v-tooltip.bottom="t('schema.generate')" />

  <Dialog v-model:visible="visible"  modal :header="t('schema.generate')" :style="{ width: '50rem' }" :breakpoints="{ '1199px': '75vw', '575px': '90vw' }">
    <div class="grid gap-2 grid-rows-2" >
      <Button v-show="!iAmER" @click="emit('generate', 'er')">{{ t('schema.generateER') }}</Button>
      <Button v-show="!iAmLogical" @click="emit('generate', 'logical')">{{ t('schema.generateLogical') }}</Button>
      <Button v-show="!iAmDB" @click="emit('generate', 'db')">{{ t('schema.generatePhysical') }}</Button>
    </div>
  </Dialog>
</template>

<script lang="ts">

</script>

<style scoped>

</style>
