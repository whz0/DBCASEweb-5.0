import { ref } from 'vue'

const load = (key: string, def: boolean) =>
  localStorage.getItem(key) !== null ? localStorage.getItem(key) === 'true' : def

export const showArrow = ref(load('card.arrow', false))
export const showNumber = ref(load('card.number', true))
export const showMinMax = ref(load('card.minmax', false))

function save(key: string, val: boolean) {
  localStorage.setItem(key, String(val))
}

function atLeastOne(changing: typeof showArrow, val: boolean) {
  if (!val && !showArrow.value && !showNumber.value && !showMinMax.value) return
  if (
    !val &&
    [showArrow, showNumber, showMinMax].filter((r) => r !== changing).every((r) => !r.value)
  )
    return
  changing.value = val
}

export function useCardinalityMode() {
  const setArrow = (v: boolean) => {
    atLeastOne(showArrow, v)
    save('card.arrow', showArrow.value)
  }
  const setNumber = (v: boolean) => {
    atLeastOne(showNumber, v)
    save('card.number', showNumber.value)
  }
  const setMinMax = (v: boolean) => {
    atLeastOne(showMinMax, v)
    save('card.minmax', showMinMax.value)
  }
  return { showArrow, showNumber, showMinMax, setArrow, setNumber, setMinMax }
}
