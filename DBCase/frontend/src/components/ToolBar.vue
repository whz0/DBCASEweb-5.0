<script setup lang="ts">
  import { ref, computed } from 'vue';
  import { useDialog } from '@/composables/useDialogs';
  import { useI18n } from 'vue-i18n';
  import HelpDialog from "@/components/dialogs/HelpDialog.vue";
  import AccessibilityDialog from "@/components/dialogs/AccessibilityDialog.vue";
  import AboutUsDialog from "@/components/dialogs/AboutUsDialog.vue";
  import SaveSchemaDialog from "@/components/dialogs/SaveSchemaDialog.vue";
  import AddEntityDialog from "@/components/dialogs/AddEntityDialog.vue";
  import LayoutDialog from "@/components/dialogs/LayoutDialog.vue";

  import TieredMenu from 'primevue/tieredmenu';

  const { t } = useI18n();

  const items = computed(() => [
    {icon: 'bi bi-person', label: t('common.name')},
    {icon: 'bi bi-box-arrow-right', label: t('common.logout')}
  ]);

  const drawMenu = ref();
  const addEntityDialog = useDialog('addEntity');
  const drawMenuItems = computed(() => [
    {
      label: t('toolbar.drawMenuItems.entity'),
      icon: 'bi bi-square',
      items: [
        {
          label: t('entity.addEntity'),
          command: addEntityDialog.open
        }
      ]
    },
    {
      label: t('toolbar.drawMenuItems.relationship'),
      icon: 'bi bi-diamond',
      items: [
        {
          label: t('toolbar.drawMenuItems.simple'),
          command: () => {
            console.log('Add Simple Relación');
          }
        },
        {
          label: t('toolbar.drawMenuItems.isA'),
          command: () => {
            console.log('Add IsA Relación');
          }
        }
      ]
    },
    {
      label: t('toolbar.drawMenuItems.domain'),
      icon: 'bi bi-collection',
      items: [
        {
          label: t('toolbar.drawMenuItems.composite'),
          command: () => {
            console.log('Add Compuesto Dominio');
          }
        }
      ]
    }
  ]);

  const toggleDrawMenu = (event: Event) => {
    drawMenu.value.toggle(event);
  };
</script>

<template>
  <div class="p-4">
    <Toolbar class="border-round-lg shadow-3">
      <template #start>
        <Button type="button" icon="bi bi-pencil" :label="t('toolbar.draw')" @click="toggleDrawMenu" aria-haspopup="true" aria-controls="overlay_menu" severity="secondary" text v-tooltip.bottom="t('toolbar.draw')"/>
        <TieredMenu ref="drawMenu" :model="drawMenuItems" popup />
        <Button icon="bi bi-folder" severity="secondary" text v-tooltip.bottom="t('toolbar.openFile')" />
        <LayoutDialog />
        <AccessibilityDialog />
        <HelpDialog />
        <AboutUsDialog />
        <SaveSchemaDialog />
        <AddEntityDialog v-model:visible="addEntityDialog.visible.value" />
      </template>

      <template #center>
      </template>

      <template #end>

        <SplitButton  :model="items">
          <span class="flex items-center font-bold">
            <img src="@/assets/logo.png" alt="Imagen Usuario" style="height: 1rem; margin-right: 0.5rem">
            <span>{{ t('toolbar.greeting', { name: 'Santi' }) }}</span>
          </span>
        </SplitButton>
      </template>
    </Toolbar>
  </div>
</template>

<style scoped>

</style>
