<script setup lang="ts">
import { ref} from "vue";
import { login } from "@/plugins/axios.ts";
import {useToast} from "primevue";

interface User {
  username: string,
  password: string,
  diagrama: string
}

const formData = ref<User>({
  username: '',
  password:'',
  diagrama: ''
})

const toast = useToast()

</script>

<template>
  <div class="col-span-3">
    <form>
      <div class="flex flex-col justify-center">
        <InputGroup>
          <InputGroupAddon>
            <i class="bi bi-person" />
          </InputGroupAddon>
          <InputText placeholder="Username" v-model="formData.username"/>
        </InputGroup>
        <InputGroup>
          <InputGroupAddon>
            <i class="bi bi-lock"/>
          </InputGroupAddon>
          <Password v-model="formData.password" :feedback="false"/>
        </InputGroup>
      </div>
      <Button
        label="Submit"
        @click="login(formData, (error) => {
          toast.add({severity: 'warn', detail: error, life: 3000});
        })"
      />
      <Button icon="bi bi-github" severity="secondary" text/>
    </form>
  </div>
</template>

<style lang="ts" scoped>

</style>
