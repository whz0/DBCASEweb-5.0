<script setup lang="ts">
import {computed, ref} from 'vue'
import {useI18n} from 'vue-i18n'
import {DialogId, useDialogStore} from '@/stores/dialogStore'
import HelpDialog from '@/components/dialogs/HelpDialog.vue'
import AccessibilityDialog from '@/components/dialogs/AccessibilityDialog.vue'
import AboutUsDialog from '@/components/dialogs/AboutUsDialog.vue'
import SaveSchemaDialog from '@/components/dialogs/SaveSchemaDialog.vue'
import OpenSchemaDialog from '@/components/dialogs/OpenSchemaDialog.vue'
import AddEntityDialog from '@/components/dialogs/AddEntityDialog.vue'
import AddAttributeDialog from '@/components/dialogs/AddAttributeDialog.vue'
import AddRelationshipDialog from '@/components/dialogs/AddRelationshipDialog.vue'
import LayoutDialog from '@/components/dialogs/LayoutDialog.vue'

import TieredMenu from 'primevue/tieredmenu'
import GenerateSchemeDialog from "@/components/dialogs/GenerateSchemeDialog.vue";

const { t } = useI18n()
const dialogStore = useDialogStore()

const items = computed(() => [
  { icon: 'bi bi-person', label: t('common.name') },
  { icon: 'bi bi-box-arrow-right', label: t('common.logout') },
])

const drawMenu = ref()
const drawMenuItems = computed(() => [
  {
    label: t('toolbar.drawMenuItems.entity'),
    icon: 'bi bi-square',
    items: [
      {
        label: t('entity.addEntity'),
        command: () => dialogStore.open(DialogId.AddEntity),
      },
    ],
  },
  {
    label: t('toolbar.drawMenuItems.attribute'),
    icon: 'bi bi-circle',
    command: () => dialogStore.open(DialogId.AddAttribute),
  },
  {
    label: t('toolbar.drawMenuItems.relationship'),
    icon: 'bi bi-diamond',
    items: [
      {
        label: t('toolbar.drawMenuItems.simple'),
        command: () => dialogStore.open(DialogId.AddRelationship),
      },
      {
        label: t('toolbar.drawMenuItems.isA'),
        command: () => {
          console.log('Add IsA Relación')
        },
      },
    ],
  },
  {
    label: t('toolbar.drawMenuItems.domain'),
    icon: 'bi bi-collection',
    items: [
      {
        label: t('toolbar.drawMenuItems.composite'),
        command: () => {
          console.log('Add Compuesto Dominio')
        },
      },
    ],
  },
])

const toggleDrawMenu = (event: Event) => {
  drawMenu.value.toggle(event)
}
</script>

<template>
  <div class="p-4">
    <Toolbar class="border-round-lg shadow-3">
      <template #start>
        <Button
          type="button"
          icon="bi bi-pencil"
          :label="t('toolbar.draw')"
          @click="toggleDrawMenu"
          aria-haspopup="true"
          aria-controls="overlay_menu"
          severity="secondary"
          text
          v-tooltip.bottom="t('toolbar.draw')"
        />
        <TieredMenu ref="drawMenu" :model="drawMenuItems" popup />
        <Button
          icon="bi bi-folder"
          severity="secondary"
          text
          v-tooltip.bottom="t('toolbar.openFile')"
          @click="dialogStore.open(DialogId.OpenSchema)"
        />
        <Button
          severity="secondary"
          class="bi bi-box-arrow-down"
          @click="dialogStore.open(DialogId.SaveSchema)"
          text
          v-tooltip.bottom="t('toolbar.saveSchema')"
        />
        <Button
          severity="secondary"
          class="bi bi-grid-1x2"
          @click="dialogStore.open(DialogId.Layout)"
          text
          v-tooltip.bottom="t('toolbar.layout')"
        />
        <Button
          severity="secondary"
          class="bi bi-universal-access-circle"
          @click="dialogStore.open(DialogId.Accessibility)"
          text
          v-tooltip.bottom="t('toolbar.settings')"
        />
        <Button
          severity="secondary"
          class="bi bi-question-circle"
          @click="dialogStore.open(DialogId.Help)"
          text
          v-tooltip.bottom="t('help.title')"
        />
        <Button
          severity="secondary"
          class="bi bi-info-circle"
          @click="dialogStore.open(DialogId.About)"
          text
          v-tooltip.bottom="t('aboutUs.title')"
        />
        <Button
          severity="secondary"
          class="bi bi-window"
          @click="dialogStore.open(DialogId.GenerateScheme)"
          text
          v-tooltip.bottom="t('schema.generate')"
        />
        <LayoutDialog />
        <AccessibilityDialog />
        <HelpDialog />
        <AboutUsDialog />
        <SaveSchemaDialog />
        <OpenSchemaDialog />
        <AddEntityDialog />
        <AddAttributeDialog />
        <GenerateSchemeDialog />
        <AddRelationshipDialog />
      </template>

      <template #end>
        <SplitButton :model="items">
          <span class="flex items-center font-bold">
            <img
              src="@/assets/logo.png"
              alt="Imagen Usuario"
              style="height: 1rem; margin-right: 0.5rem"
            />
            <span>{{ t('toolbar.greeting', { name: 'Santi' }) }}</span>
          </span>
        </SplitButton>
      </template>
    </Toolbar>
  </div>
</template>

<style scoped></style>
