package models

class ShoppingCart {

    private val items = mutableListOf<CartItem>()

    // Agregar producto al carrito
    fun addProduct(product: Product, quantity: Int) {
        val existing = items.find { it.product.productCode == product.productCode }

        if (existing != null) {
            existing.quantity += quantity
        } else {
            items.add(CartItem(product, quantity))
        }
    }

    // 🔥 ESTA ES TU PARTE (visualización)
    fun showCart() {
        if (items.isEmpty()) {
            println("El carrito está vacío 🛒")
            return
        }

        println("\n=== CARRITO DE COMPRAS ===")

        var totalGeneral = 0.0

        for (item in items) {
            val nombre = item.product.name
            val cantidad = item.quantity
            val precioUnitario = item.product.price
            val totalProducto = cantidad * precioUnitario

            println("Producto: $nombre")
            println("Cantidad: $cantidad")
            println("Precio Unitario: $${"%.2f".format(precioUnitario)}")
            println("Total Producto: $${"%.2f".format(totalProducto)}")
            println("------------------------------")

            totalGeneral += totalProducto
        }

        println("TOTAL GENERAL: $${"%.2f".format(totalGeneral)} 💰")
    }
}