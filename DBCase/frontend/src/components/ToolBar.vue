<script setup lang="ts">
import { computed, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'

import AboutUsDialog from '@/components/dialogs/AboutUsDialog.vue'
import AccessibilityDialog from '@/components/dialogs/AccessibilityDialog.vue'
import AddAttributeDialog from '@/components/dialogs/AddAttributeDialog.vue'
import AddDomainDialog from '@/components/dialogs/AddDomainDialog.vue'
import AddEntityDialog from '@/components/dialogs/AddEntityDialog.vue'
import AddEntityToRelationshipDialog from '@/components/dialogs/AddEntityToRelationshipDialog.vue'
import AddIsARelationshipDialog from '@/components/dialogs/AddIsARelationshipDialog.vue'
import AddRelationshipDialog from '@/components/dialogs/AddRelationshipDialog.vue'
import HelpDialog from '@/components/dialogs/HelpDialog.vue'
import LayoutDialog from '@/components/dialogs/LayoutDialog.vue'
import OpenSchemaDialog from '@/components/dialogs/OpenSchemaDialog.vue'
import SaveSchemaDialog from '@/components/dialogs/SaveSchemaDialog.vue'
import { DialogId, useDialogStore } from '@/stores/dialogStore'

const router = useRouter()

import { useAuthStore } from '@/stores/authStore.ts'
const { user, logout } = useAuthStore()

import Menu from 'primevue/menu'
import TieredMenu from 'primevue/tieredmenu'

import GenerateSchemeDialog from '@/components/dialogs/GenerateSchemeDialog.vue'

const { t } = useI18n()
const dialogStore = useDialogStore()

const userMenu = ref()
const userMenuItems = computed(() => [
  {
    label: t('common.profile'),
    icon: 'bi bi-person',
    command: () => router.push('/profile'),
  },
  {
    separator: true,
  },
  {
    label: t('common.logout'),
    icon: 'bi bi-box-arrow-right',
    command: () => {
      logout()
      router.replace('/login')
    },
  },
])

const toggleUserMenu = (event: Event) => {
  userMenu.value.toggle(event)
}

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
        command: () => dialogStore.open(DialogId.AddIsARelationship),
      },
    ],
  },
  {
    label: t('toolbar.drawMenuItems.domain'),
    icon: 'bi bi-collection',
    command: () => dialogStore.open(DialogId.AddDomain),
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
          class="bi bi-gear"
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
        <AddEntityToRelationshipDialog />
        <AddIsARelationshipDialog />
        <AddDomainDialog />
      </template>

      <template #end>
        <div class="flex items-center">
          <Button
            type="button"
            @click="toggleUserMenu"
            aria-haspopup="true"
            aria-controls="user_menu"
            severity="secondary"
            text
            class="!flex !items-center !gap-3 !px-4 !py-2 !rounded-xl hover:!bg-surface-100 dark:hover:!bg-surface-800 !transition-all !border !border-transparent hover:!border-surface-200 dark:hover:!border-surface-700"
          >
            <div class="relative">
              <img
                src="@/assets/logo.png"
                alt="User Avatar"
                class="w-8 h-8 rounded-full ring-2 ring-surface-100 dark:ring-surface-800 object-cover"
              />
              <div
                class="absolute bottom-0 right-0 w-2.5 h-2.5 bg-green-500 border-2 border-white dark:border-surface-900 rounded-full"
              ></div>
            </div>
            <div class="flex flex-col items-start leading-tight hidden md:flex">
              <span class="text-sm font-bold">{{ user.username }}</span>
              <span class="text-[10px] opacity-50 uppercase tracking-tighter">{{
                t('common.user')
              }}</span>
            </div>
            <i class="bi bi-chevron-down text-[10px] opacity-40 ml-1"></i>
          </Button>
          <Menu
            ref="userMenu"
            id="user_menu"
            :model="userMenuItems"
            popup
            :style="{ width: '200px', borderRadius: '12px' }"
            class="!mt-2 !shadow-xl !border-surface-100 dark:!border-surface-800"
          >
            <template #item="{ item, props }">
              <a
                v-ripple
                class="flex items-center px-3 py-2.5 group"
                v-bind="props.action"
                :class="{ 'text-red-500!': item.label === t('common.logout') }"
              >
                <span
                  :class="[
                    item.icon,
                    'text-sm transition-transform group-hover:scale-110',
                    item.label === t('common.logout') ? 'text-red-500' : 'text-primary',
                  ]"
                />
                <span class="ml-3 font-medium">{{ item.label }}</span>
              </a>
            </template>
          </Menu>
        </div>
      </template>
    </Toolbar>
  </div>
</template>

<style scoped></style>
