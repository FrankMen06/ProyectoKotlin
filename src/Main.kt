import models.ShoppingCart
import services.Inventory
import java.io.FileWriter
import java.io.IOException
import java.util.Properties
import jakarta.mail.Authenticator
import jakarta.mail.Message
import jakarta.mail.PasswordAuthentication
import jakarta.mail.Session
import jakarta.mail.Transport
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage


// APP KEY
const val FROM_EMAIL = "fm920715@gmail.com"
const val APP_PASSWORD = "wohh tplr xpjw udkt"

// Función para loguear errores en un archivo
fun logError(message: String) {
    try {
        FileWriter("log/errors.log", true).use { writer ->
            writer.appendLine("${java.time.LocalDateTime.now()}: $message")
        }
    } catch (e: IOException) {
        println("Error al loguear: ${e.message}")
    }
}

// Función para validar input con un predicado personalizado
fun validateInput(prompt: String, validator: (String) -> Boolean, errorMsg: String): String? {
    print(prompt)
    val input = readLine()?.trim()
    return if (input != null && validator(input)) input else {
        println(errorMsg)
        logError("$errorMsg - Input: $input")
        null
    }
}

// Función para validar códigos de producto (alfanuméricos simples)
fun validateProductCode(code: String): Boolean {
    val regex = Regex("^[A-Za-z0-9]+$") // Solo alfanuméricos, sin espacios
    return code.isNotBlank() && regex.matches(code)
}

// Función para validar cantidades numéricas (enteros positivos)
fun validateQuantity(input: String): Int? {
    val qty = input.trim().toIntOrNull()
    return if (qty != null && qty > 0) qty else null
}

// Función para validar precio decimal (double positivo)
fun validatePrice(input: String): Double? {
    val price = input.trim().toDoubleOrNull()
    return if (price != null && price > 0.0) price else null
}

// Función para pedir confirmación con s/n
fun confirmAction(prompt: String): Boolean {
    print("$prompt (s/n): ")
    val response = readLine()?.trim()?.lowercase()
    return response == "s"
}

// Función para agregar un producto al carrito con validación y confirmación usando código
fun addToCart(cart: ShoppingCart) {
    println("\n===  Agregar  Producto  al  Carrito  ===")
    println(Inventory.displayProducts())
    val code = validateInput("Ingresa el código del producto: ", { validateProductCode(it) }, "Código inválido: solo letras y números permitidos") ?: return
    val product = Inventory.findProduct(code) ?: run {
        println("Producto con código $code no encontrado.")
        return
    }
    val qtyInput = validateInput("Ingresa la cantidad: ", { it.isNotBlank() }, "Cantidad no puede estar vacía") ?: return
    val qty = validateQuantity(qtyInput) ?: run {
        println("Cantidad inválida: debe ser un número entero positivo.")
        logError("Cantidad inválida: $qtyInput")
        return
    }
    if (confirmAction("¿Confirmar agregar $qty unidades del producto con código $code?")) {
        if (cart.addItem(product, qty)) {
            println("Producto agregado exitosamente.")
        } else {
            println("No se pudo agregar: cantidad no disponible.")
        }
    } else {
        println("Acción cancelada.")
    }
}

// Función para editar la cantidad de un producto en el carrito usando código
fun editCartItem(cart: ShoppingCart) {
    if (cart.getItems().isEmpty()) {
        println("Carrito vacío, nada que editar.")
        return
    }
    println("\n===  Editar  Producto  en  Carrito  ===")
    println(cart.display())
    val code = validateInput("Ingresa el código del producto a editar: ", { validateProductCode(it) }, "Código inválido") ?: return
    val item = cart.getItems().find { it.product.productCode == code } ?: run {
        println("Producto con código $code no encontrado en carrito.")
        return
    }
    val newQtyInput = validateInput("Ingresa la nueva cantidad: ", { it.isNotBlank() }, "Cantidad no puede estar vacía") ?: return
    val newQty = validateQuantity(newQtyInput) ?: run {
        println("Cantidad inválida: debe ser un número entero positivo.")
        logError("Cantidad inválida: $newQtyInput")
        return
    }
    if (confirmAction("¿Confirmar cambiar el producto con código $code a $newQty unidades?")) {
        item.quantity = newQty // Actualiza directamente; podría validar stock si se extiende
        println("Cantidad actualizada.")
    } else {
        println("Acción cancelada.")
    }
}

// Función para eliminar un producto del carrito con confirmación usando código
fun removeFromCart(cart: ShoppingCart) {
    if (cart.getItems().isEmpty()) {
        println("Carrito vacío, nada que eliminar.")
        return
    }
    println("\n===  Eliminar  Producto  del  Carrito  ===")
    println(cart.display())
    val code = validateInput("Ingresa el código del producto a eliminar: ", { validateProductCode(it) }, "Código inválido") ?: return
    if (confirmAction("¿Confirmar eliminar el producto con código $code del carrito?")) {
        if (cart.removeItem(code)) {
            println("Producto eliminado.")
        } else {
            println("Producto con código $code no encontrado.")
        }
    } else {
        println("Acción cancelada.")
    }
}

// Función para confirmar la compra y generar factura con tabulaciones fijas
fun confirmPurchase(cart: ShoppingCart) {
    if (cart.getItems().isEmpty()) {
        println("\nCarrito vacío. No hay nada que comprar.")
        return
    }

    val invoiceText = buildInvoiceText(cart)

    println("\n=== Vista previa de factura ===")
    println(invoiceText)

    var email: String

    while (true) {
        val result = validateInput(
            "Ingresa el correo para enviar la factura: ",
            { validateEmail(it) },
            "Correo inválido, intenta de nuevo."
        )

        if (result != null) {
            email = result
            break
        }
    }

    if (confirmAction("¿Confirmar compra y enviar factura a $email?")) {

        try {
            sendInvoiceEmail(email, invoiceText)
            println("Factura enviada a $email")
        } catch (e: Exception) {
            logError("Error enviando correo: ${e.message}")
            println("Error al enviar correo")
        }

        cart.getItems().forEach {
            Inventory.updateQuantity(it.product.productCode, it.quantity)
        }

        println("Compra confirmada. Inventario actualizado.")

        print("¿Deseas seguir comprando? (s/n): ")
        if (readLine()?.trim()?.lowercase() == "s") {
            cart.clear()
        } else {
            println("Sesión finalizada.")
            System.exit(0)
        }
    } else {
        println("Compra cancelada.")
    }
}
fun validateEmail(email: String): Boolean {
    val regex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
    return email.isNotBlank() && regex.matches(email)
}

fun buildInvoiceText(cart: ShoppingCart): String {
    val taxRate = 0.13
    val subtotal = cart.getTotal()
    val tax = subtotal * taxRate
    val totalWithTax = subtotal + tax

    val sb = StringBuilder()
    sb.appendLine("=========== FACTURA ELECTRONICA ===========")
    sb.appendLine(cart.display())
    sb.appendLine(String.format("%-20s  $%.2f", "Subtotal:", subtotal))
    sb.appendLine(String.format("%-20s  $%.2f", "IVA (13%):", tax))
    sb.appendLine(String.format("%-20s  $%.2f", "Total final:", totalWithTax))
    sb.appendLine("===========================================")

    return sb.toString()
}

fun sendInvoiceEmail(toEmail: String, invoiceText: String) {
    val props = Properties().apply {
        put("mail.smtp.host", "smtp.gmail.com")
        put("mail.smtp.port", "587")
        put("mail.smtp.auth", "true")
        put("mail.smtp.starttls.enable", "true")
    }

    val session = Session.getInstance(props, object : Authenticator() {
        override fun getPasswordAuthentication(): PasswordAuthentication {
            return PasswordAuthentication(FROM_EMAIL, APP_PASSWORD)
        }
    })

    val message = MimeMessage(session).apply {
        setFrom(InternetAddress(FROM_EMAIL))
        setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail))
        subject = "Factura de compra"
        setText(invoiceText)
    }

    Transport.send(message)
}

// Menu principal
fun main() {
    val cart = ShoppingCart() // Instancia del carrito
    var continueShopping = true // Controla el loop del menú

    while (continueShopping) {
        println("\n===  Sistema  de  Carrito  de  Compras  en  Consola  ===")
        println(String.format("%-40s  %-20s", "  1.  Ver  lista  de  productos  disponibles  ", "  "))
        println(String.format("%-40s  %-20s", "  2.  Agregar  producto  al  carrito  ", "  "))
        println(String.format("%-40s  %-20s", "  3.  Editar  cantidad  en  carrito  ", "  "))
        println(String.format("%-40s  %-20s", "  4.  Eliminar  producto  del  carrito  ", "  "))
        println(String.format("%-40s  %-20s", "  5.  Visualizar  carrito  ", "  "))
        println(String.format("%-40s  %-20s", "  6.  Confirmar  compra  y  generar  factura  ", "  "))
        println(String.format("%-40s  %-20s", "  7.  Salir  ", "  "))
        print("Elige una opción (1-7): ")

        val option = readLine()?.trim()?.toIntOrNull()
        if (option == null) {
            println("Entrada inválida. Intenta de nuevo.")
            logError("Entrada no numérica en menú principal")
            continue
        }

        try {
            when (option) {
                1 -> {
                    val productsList = Inventory.displayProducts()
                    if (productsList == "Inventario vacío") {
                        println("No hay productos disponibles en este momento. ¡Vuelve más tarde!")
                    } else {
                        println(productsList)
                    }
                }
                2 -> addToCart(cart)
                3 -> editCartItem(cart)
                4 -> removeFromCart(cart)
                5 -> println(cart.display())
                6 -> confirmPurchase(cart)
                7 -> continueShopping = false
                else -> println("Opción inválida. Elige entre 1 y 7.")
            }
        } catch (e: Exception) {
            logError(e.message ?: "Error desconocido")
            println("Ocurrió un error: ${e.message}. Intenta de nuevo.")
            println("Stack trace: ${e.stackTraceToString()}") // Para depuración
        }
    }
    println("Gracias por usar el sistema")
}
