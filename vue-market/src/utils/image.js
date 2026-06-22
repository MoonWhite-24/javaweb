export const PRODUCT_PLACEHOLDER = '/static/images/placeholder.svg'

export const productImage = (src) => src || PRODUCT_PLACEHOLDER

export const usePlaceholderImage = (event) => {
  if (event.target.src.endsWith(PRODUCT_PLACEHOLDER)) return
  event.target.src = PRODUCT_PLACEHOLDER
}
