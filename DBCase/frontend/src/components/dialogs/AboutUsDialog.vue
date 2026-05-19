<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'

import ProjectInfo from '@/components/generic/ProjectInfo.vue'
import { DialogId, useDialogStore } from '@/stores/dialogStore'

const { t } = useI18n()
const dialogStore = useDialogStore()

const visible = computed(() => dialogStore.isOpen(DialogId.About))
const closeModal = () => dialogStore.close(DialogId.About)
</script>

<template>
  <Dialog
    :visible="visible"
    @update:visible="closeModal"
    modal
    :dismissable-mask="true"
    :header="t('aboutUs.header')"
    :style="{ width: '50rem' }"
    :breakpoints="{ '1199px': '75vw', '575px': '90vw' }"
    :footer="t('aboutUs.footer')"
  >
    <div class="text-center">
      <h1 class="text-3xl font-bold">DBCASE Web</h1>
      <p class="italic">{{ t('aboutUs.use') }}</p>
      <Divider />
      <h2 class="text-xl font-medium">{{ t('aboutUs.director') }}</h2>
      <Button
        type="button"
        icon="bi bi-envelope"
        label="Fernando Sáenz Pérez"
        @click="console.log('Hola')"
        aria-haspopup="true"
        aria-controls="overlay_menu"
        severity="secondary"
        text
        v-tooltip.bottom="t('aboutUs.mail')"
      />
      <h2 class="text-xl font-medium">{{ t('aboutUs.author') }}</h2>
      <p class="font-light">
        Ochoa de Zabalegui, Santiago <br />
        Zhu, Weihong
      </p>
      <Divider />
      <ProjectInfo title="DBCASE Web 4.0 (2024/2025)" :authors="['Barcenilla Hinchado, María']" />
      <ProjectInfo
        title="DBCASE 4.0 (2021/2022)"
        :authors="['Pisonero Díaz, Iván', 'Vicente Vázquez, Javier de']"
      />
      <h2 class="text-xl">{{ t('aboutUs.projects') }}</h2>
      <ProjectInfo title="DBCASE Web 3.0 (2020/2021)" :authors="['Caiafa Roxanne, Antonieta']" />
      <ProjectInfo
        title="DBCASE Web (2019/2020)"
        :authors="['Paucar Cerrón, Luis Eduardo', 'Conde Cubas, Yrving David']"
      />
      <ProjectInfo title="DBCASE 2.0 (2018/2019)" :authors="['Arriba García, Miguel']" />
      <ProjectInfo
        title="DBCASE 1.0 (2008/2009)"
        :authors="[
          'Denis Cepeda Mateos, Rodrigo',
          'Marco de Francisco, Cristina',
          'Serrano Gordillo, Tello',
        ]"
      />
      <ProjectInfo
        title="DBDT 1.0 (2007/2008)"
        :authors="[
          'Milán Gutiérrez, Alberto',
          'Martínez Segura, Miguel',
          'Cáceres González, Francisco Javier',
          t('aboutUs.director') + ' Yolanda García Ruiz',
        ]"
      />
    </div>
  </Dialog>
</template>

<style scoped></style>
