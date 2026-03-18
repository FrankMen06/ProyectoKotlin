package models

//Para captura de datos básicos del inventario, con validación de disponibilidad y un código único para identificación
class Product(val productCode: String, val name: String, val price: Double, var availableQuantity: Int) {
    //Método para verificar si hay suficiente stock para una cantidad dada

    fun isAvailable(quantity: Int): Boolean = availableQuantity >= quantity
}