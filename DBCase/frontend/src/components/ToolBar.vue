<script setup lang="ts">
  import { ref } from 'vue';
  import HelpDialog from "@/components/HelpDialog.vue";
  import AccessibilityDialog from "@/components/AccessibilityDialog.vue";
  import AboutUsDialog from "@/components/AboutUsDialog.vue";

  const items = ref([
    {icon: 'bi bi-person', label: 'Nombre'},
    {icon: 'bi bi-box-arrow-right', label: 'Logout'}
  ])

  const selectComponent = ref();
  const components = ref([
    {
      name: 'Entidad',
      types: [
        {cname: 'Simple'},
        {cname: 'Compuesto'},
      ]
    },
    {
      name: 'Relación',
      types: [
        {cname: 'Simple'},
        {cname: 'IsA'},
      ]
    },
    {
      name: 'Dominio',
      types:[
        {cname: 'Compuesto'},
      ]
    }
  ])
</script>

<template>
  <div>
    <Toolbar>
      <template #start>
        <CascadeSelect v-model="selectComponent" :options="components" optionLabel="cname" optionGroupLabel="name"
                       :optionGroupChildren="['types']" class="w-56" size="small" placeholder="Dibujar">
          <template #dropdownicon>
            <i class="bi bi-pencil"></i>
          </template>
        </CascadeSelect>
        <Button icon="bi bi-folder" severity="secondary" text />
        <Button icon="bi bi-grid-1x2" severity="secondary" text />
        <AccessibilityDialog />
        <HelpDialog />
        <AboutUsDialog />
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
