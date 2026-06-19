package com.example.testeableapp
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.example.testeableapp.model.MenuData
import org.junit.Rule
import org.junit.Test

class RestaurantAppTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun mensajeDePedidoVacioVisibleAlInicio() {
        composeTestRule.setContent {
            RestaurantOrderApp(viewModel = RestaurantViewModel())
        }

        composeTestRule.onNodeWithTag("emptyOrderMessage").assertIsDisplayed()
        composeTestRule.onNodeWithText("El pedido está vacío. Añade productos del menú.").assertIsDisplayed()
    }

    @Test
    fun todosLosItemsDelMenuVisibles() {
        composeTestRule.setContent {
            RestaurantOrderApp(viewModel = RestaurantViewModel())
        }

        MenuData.items.forEach { item ->
            composeTestRule.onNodeWithTag("menuItem_${item.id}")
                .performScrollTo()
                .assertIsDisplayed()

            composeTestRule.onNodeWithTag("menuItemName_${item.id}")
                .assertTextEquals(item.name)
        }
    }

    @Test
    fun elTotalGeneralSeActualiza() {
        composeTestRule.setContent {
            RestaurantOrderApp(viewModel = RestaurantViewModel())
        }

        composeTestRule.onNodeWithTag("addButton_1").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("totalValue").performScrollTo().assertTextEquals("5.50 €")

        composeTestRule.onNodeWithTag("addButton_2").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("totalValue").performScrollTo().assertTextEquals("11.50 €")
    }

    @Test
    fun realizarPedidoMuestraDialogoConfirmacion() {
        composeTestRule.setContent {
            RestaurantOrderApp(viewModel = RestaurantViewModel())
        }

        // 1. Agregar un producto al carrito
        composeTestRule.onNodeWithTag("addButton_1").performScrollTo().performClick()

        // 2. Hacer click en 'Realizar Pedido'
        composeTestRule.onNodeWithTag("placeOrderButton").performScrollTo().performClick()

        // 3. Verificar que el dialogo se muestra con la info correcta
        composeTestRule.onNodeWithTag("confirmationDialog").assertIsDisplayed()
        composeTestRule.onNodeWithTag("confirmationTitle").assertTextEquals("Pedido Confirmado")
    }

    @Test
    fun aceptarDialogoDeConfirmacionLimpiaElPedido() {
        composeTestRule.setContent {
            RestaurantOrderApp(viewModel = RestaurantViewModel())
        }

        // 1. Agregar un producto y abrir el diálogo de pedido
        composeTestRule.onNodeWithTag("addButton_1").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("placeOrderButton").performScrollTo().performClick()

        // 2. Hacer click en 'Aceptar' dentro del diálogo
        composeTestRule.onNodeWithTag("confirmationOkButton").performClick()

        // 3. Verificar que el dialogo desaparece y se muestra el mensaje de pedido vacio
        composeTestRule.onNodeWithTag("confirmationDialog").assertDoesNotExist()
        composeTestRule.onNodeWithTag("emptyOrderMessage").performScrollTo().assertIsDisplayed()
    }
}