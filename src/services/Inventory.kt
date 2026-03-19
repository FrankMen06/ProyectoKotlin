package services

import models.Product

object Inventory {
    private val products: MutableList<Product> = mutableListOf( // Lista inicial de 10 productos con códigos únicos
        Product("P001", "Laptop Dell", 800.00, 5),
        Product("P002", "Mouse Logitech", 25.00, 15),
        Product("P003", "Teclado Mecánico", 60.00, 10),
        Product("P004", "Monitor LG 24'", 150.00, 8),
        Product("P005", "Auriculares Sony", 90.00, 12),
        Product("P006", "Disco SSD 500GB", 70.00, 7),
        Product("P007", "Impresora HP", 120.00, 4),
        Product("P008", "Webcam Logitech", 45.00, 9),
        Product("P009", "Router TP-Link", 35.00, 6),
        Product("P010", "Cargador USB-C", 15.00, 20)
    )

    fun displayProducts(): String {
        if (products.isEmpty()) return "Inventario vacío"
        val sb = StringBuilder()
        sb.appendLine(String.format("%-6s  %-40s  %-10s  %-10s", "  Código  ", "  Nombre  ", "  Precio  ", "  Cantidad  "))
        sb.appendLine(String.format("%-6s  %-40s  %-10s  %-10s", "  ------  ", "  ------  ", "  -----  ", "  --------  "))
        products.forEach { product ->
            sb.appendLine(String.format("%-6s  %-40s  %-10.2f  %-10d", product.productCode, product.name, product.price, product.availableQuantity))
        }
        return sb.toString()
    }
    
    fun findProductByCode(code: String): Product? {
        return products.find { it.productCode == code }
    }
}
