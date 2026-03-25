import { computed, readonly, ref } from 'vue'

type ToolbarAction = {
  label: string
  disabled?: boolean
  handler: () => void | Promise<void>
}

const action = ref<ToolbarAction | null>(null)

export const setAdminToolbarAction = (nextAction?: ToolbarAction | null) => {
  action.value = nextAction ?? null
}

export const clearAdminToolbarAction = () => {
  action.value = null
}

export const useAdminToolbarAction = () => {
  const visible = computed(() => Boolean(action.value?.label && action.value?.handler))
  const label = computed(() => action.value?.label ?? '')
  const disabled = computed(() => Boolean(action.value?.disabled))

  const trigger = () => {
    if (!action.value?.handler || disabled.value) {
      return
    }
    void action.value.handler()
  }

  return {
    visible,
    label: readonly(label),
    disabled,
    trigger
  }
}
