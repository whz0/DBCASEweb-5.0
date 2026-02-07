<script setup lang="ts">
  import { ref } from 'vue';
  import { useDialog } from '@/composables/useDialogs';
  import HelpDialog from "@/components/dialogs/HelpDialog.vue";
  import AccessibilityDialog from "@/components/dialogs/AccessibilityDialog.vue";
  import AboutUsDialog from "@/components/dialogs/AboutUsDialog.vue";
  import SaveSchemaDialog from "@/components/dialogs/SaveSchemaDialog.vue";
  import AddEntityDialog from "@/components/dialogs/AddEntityDialog.vue";

  import TieredMenu from 'primevue/tieredmenu';

  const items = ref([
    {icon: 'bi bi-person', label: 'Nombre'},
    {icon: 'bi bi-box-arrow-right', label: 'Logout'}
  ])

  const drawMenu = ref();
  const addEntityDialog = useDialog('addEntity');
  const drawMenuItems = ref([
    {
      label: 'Entidad',
      icon: 'bi bi-square',
      items: [
        {
          label: 'Add Entity',
          command: addEntityDialog.open
        }
      ]
    },
    {
      label: 'Relación',
      icon: 'bi bi-diamond',
      items: [
        {
          label: 'Simple',
          command: () => {
            console.log('Add Simple Relación');
          }
        },
        {
          label: 'IsA',
          command: () => {
            console.log('Add IsA Relación');
          }
        }
      ]
    },
    {
      label: 'Dominio',
      icon: 'bi bi-collection',
      items: [
        {
          label: 'Compuesto',
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
        <Button type="button" icon="bi bi-pencil" label="Draw" @click="toggleDrawMenu" aria-haspopup="true" aria-controls="overlay_menu" severity="secondary" text/>
        <TieredMenu ref="drawMenu" :model="drawMenuItems" popup />
        <Button icon="bi bi-folder" severity="secondary" text />
        <Button icon="bi bi-grid-1x2" severity="secondary" text />
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
            <span>Hola Santi</span>
          </span>
        </SplitButton>
      </template>
    </Toolbar>
  </div>
</template>

<style scoped>

</style>
