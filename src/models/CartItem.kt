package src.models


// Sirve para validar cuántos de un producto se han agregado
class CartItem(val product: Product, var quantity: Int) {
    // Calcula el precio total del ítem basado en precio unitario y cantidad
    fun totalPrice(): Double = product.price * quantity
}