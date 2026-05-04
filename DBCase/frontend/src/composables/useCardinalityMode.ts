import { ref } from 'vue'

// 'classic-number': double/single line + max cardinality number
// 'classic-arrow': double/single line + arrow pointing to "1" side
// 'minmax': (min,max) notation only, no double lines
export type CardinalityMode = 'classic-number' | 'classic-arrow' | 'minmax'

const saved = localStorage.getItem('cardinalityMode') as CardinalityMode | null
const cardinalityMode = ref<CardinalityMode>(saved || 'classic-number')

export function useCardinalityMode() {
  const setCardinalityMode = (mode: CardinalityMode) => {
    cardinalityMode.value = mode
    localStorage.setItem('cardinalityMode', mode)
  }

  return { cardinalityMode, setCardinalityMode }
}
