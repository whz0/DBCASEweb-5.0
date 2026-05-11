<script setup lang="ts">
import { nextTick, ref, watch } from 'vue'

const props = defineProps<{
  title: string
  heigh: string
  modelValue?: string
}>()

const emit = defineEmits<{ 'update:modelValue': [value: string] }>()

const editable = ref<HTMLElement>()
const isEditing = ref(false)

watch(
  () => props.modelValue,
  (val) => {
    if (isEditing.value) return
    nextTick(() => {
      if (editable.value && editable.value.innerHTML !== (val ?? ''))
        editable.value.innerHTML = val ?? ''
    })
  },
  { immediate: true },
)

const contextMenu = ref()
const savedRange = ref<Range | null>(null)

const toggleUnderline = (underline: boolean) => {
  if (!savedRange.value || savedRange.value.collapsed) return
  if (underline === document.queryCommandState('underline')) return
  document.execCommand('underline', false)
}

const contextMenuItems = [
  { label: 'Subrayar', icon: 'bi bi-type-underline', command: () => toggleUnderline(true) },
  { label: 'Desubrayar', icon: 'bi bi-type', command: () => toggleUnderline(false) },
]

const onContextMenu = (e: MouseEvent) => {
  const sel = window.getSelection()
  savedRange.value = sel && sel.rangeCount > 0 ? sel.getRangeAt(0).cloneRange() : null
  contextMenu.value.show(e)
}
</script>

<template>
  <ContextMenu ref="contextMenu" :model="contextMenuItems" />
  <label>{{ title }}</label>
  <ScrollPanel :class="['text-left', 'w-full', heigh, 'border', 'rounded-md', 'mb-2']">
    <div
      ref="editable"
      contenteditable="true"
      class="w-full h-full outline-none p-1"
      @contextmenu.prevent="onContextMenu($event)"
      @focus="isEditing = true"
      @blur="isEditing = false"
      @input="(e) => emit('update:modelValue', (e.target as HTMLElement).innerHTML)"
    />
  </ScrollPanel>
</template>
