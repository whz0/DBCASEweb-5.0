<script setup lang="ts">
import { nextTick, ref, watch } from 'vue'

const props = defineProps<{
  title: string
  heigh: string
  flex?: number
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
  editable.value?.focus()
  const sel = window.getSelection()
  sel?.removeAllRanges()
  sel?.addRange(savedRange.value)
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
  <div class="flex flex-col min-h-0" :style="flex ? { flex: `${flex} 1 0%` } : {}">
    <label class="shrink-0 text-sm font-medium mb-1">{{ title }}</label>
    <div class="flex-1 min-h-0 overflow-y-auto text-left w-full border rounded-md">
      <div
        ref="editable"
        contenteditable="true"
        class="w-full min-h-full outline-none p-1"
        @contextmenu.prevent="onContextMenu($event)"
        @focus="isEditing = true"
        @blur="isEditing = false"
        @input="(e) => emit('update:modelValue', (e.target as HTMLElement).innerHTML)"
      />
    </div>
  </div>
</template>
