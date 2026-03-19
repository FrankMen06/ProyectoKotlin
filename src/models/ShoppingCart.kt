package models
import models.CartItem
import models.Product

// Gestionar el carrito de compras, usando una lista de CartItem (agregar, eliminar y visualizar items)
class ShoppingCart {
    private val items: MutableList<CartItem> = mutableListOf() // Lista interna para almacenar ítems

    // Agrega un ítem al carrito, validando cantidad y disponibilidad
    fun addItem(product: Product, quantity: Int): Boolean {
        if (quantity <= 0) return false // Valida que la cantidad sea positiva
        if (product.isAvailable(quantity)) {
            val existing = items.find { it.product.productCode == product.productCode } // Busca por código
            if (existing != null) {
                existing.quantity += quantity // Incrementa si existe
            } else {
                items.add(CartItem(product, quantity)) // Agrega nuevo si no existe
            }
            return true
        }
        return false
    }

    // Elimina un ítem del carrito por código de producto
    fun removeItem(productCode: String): Boolean {
        val index = items.indexOfFirst { it.product.productCode == productCode }
        return if (index != -1) {
            items.removeAt(index) // Elimina por índice
            true
        } else false
    }
    // Calcula el total general del carrito sumando todos los totalPrice
    fun getTotal(): Double = items.sumOf { it.totalPrice() }

    // Genera una representación en string del carrito con anchos fijos
    fun display(): String {
        if (items.isEmpty()) return "Carrito vacío"
        val sb = StringBuilder()
        sb.appendLine(String.format("%-6s  %-40s  %-10s  %-12s  %-10s", "  Código  ", "  Nombre  ", "  Cantidad  ", "  Precio  Unitario  ", "  Subtotal  "))
        sb.appendLine(String.format("%-6s  %-40s  %-10s  %-12s  %-10s", "  ------  ", "  ------  ", "  --------  ", "  ------------  ", "  --------  "))
        items.forEach { item ->
            sb.appendLine(String.format("%-6s  %-40s  %-10d  %-12.2f  %-10.2f", item.product.productCode, item.product.name, item.quantity, item.product.price, item.totalPrice()))
        }
        sb.appendLine(String.format("%-86s  %-10.2f", "  Total  general  :", getTotal()))
        return sb.toString()
    }

    // Brinda una copia de la lista de ítems para evitar modificaciones externas
    fun getItems(): List<CartItem> = items.toList()

    // Limpia todos los ítems del carrito para caso de reiniciar el carrito despues de realizar una compra
    fun clear() {
        items.clear()
    }
}