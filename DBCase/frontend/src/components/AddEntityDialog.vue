<script setup lang="ts">
import { ref } from 'vue';
import Dialog from 'primevue/dialog';
import Button from 'primevue/button';
import InputText from 'primevue/inputtext';
import Checkbox from 'primevue/checkbox';
import Dropdown from 'primevue/dropdown';

const visible = defineModel('visible', { type: Boolean, default: false });
const entityName = ref('');
const isWeakEntity = ref(false);
const relationName = ref('');
const selectedStrongEntity = ref(null);

const strongEntities = ref([
    { label: 'Strong Entity A', value: 'entityA' },
    { label: 'Strong Entity B', value: 'entityB' },
]);
</script>

<template>
    <Dialog
        v-model:visible="visible"
        modal
        :dismissable-mask="true"
        :draggable="false"
        :style="{ width: '30rem' }"
        header="Add Entity"
    >
        <div class="flex flex-col gap-3">
            <label for="entityName" class="font-semibold">Entity Name</label>
            <InputText id="entityName" v-model="entityName" placeholder="Enter entity name" autofocus />

            <div class="flex items-center gap-2 mt-2">
                <Checkbox v-model="isWeakEntity" inputId="isWeak" :binary="true" />
                <label for="isWeak">Is Weak Entity?</label>
            </div>

            <template v-if="isWeakEntity">
                <label for="relationName" class="font-semibold mt-2">Weak Relationship Name</label>
                <InputText id="relationName" v-model="relationName" placeholder="Enter relation name" />

                <label for="strongEntity" class="font-semibold mt-2">Strong Entity</label>
                <Dropdown
                    id="strongEntity"
                    v-model="selectedStrongEntity"
                    :options="strongEntities"
                    optionLabel="label"
                    placeholder="Select a Strong Entity"
                />
            </template>
        </div>

        <template #footer>
            <Button label="Cancel" icon="bi bi-x-lg" severity="secondary" @click="visible = false" />
            <Button label="Add Entity" icon="bi bi-check-lg" />
        </template>
    </Dialog>
</template>

<style scoped>

</style>
