package com.example.testeableapp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class RestaurantViewModelTest {

    private lateinit var viewModel: RestaurantViewModel

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = RestaurantViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun agregarItemAlPedido() = runTest {
        val job = backgroundScope.launch(testDispatcher) { viewModel.uiState.collect {} }

        viewModel.addItem(1) // Patatas Bravas

        assertEquals(1, viewModel.quantities.value[1])
        assertEquals(1, viewModel.orderedItems.value.size)
        assertEquals(5.50, viewModel.total.value, 0.0)
    }

    @Test
    fun incrementarYDecrementarCantidad() = runTest {
        backgroundScope.launch(testDispatcher) { viewModel.uiState.collect {} }

        viewModel.addItem(2) // Croquetas
        viewModel.incrementItem(2)

        assertEquals(2, viewModel.quantities.value[2])
        assertEquals(12.00, viewModel.total.value, 0.0)

        viewModel.decrementItem(2)

        assertEquals(1, viewModel.quantities.value[2])
        assertEquals(6.00, viewModel.total.value, 0.0)
    }

    @Test
    fun eliminarItemAlDecrementarDesdeUno() = runTest {
        backgroundScope.launch(testDispatcher) { viewModel.uiState.collect {} }

        viewModel.addItem(3) // Calamares
        assertEquals(1, viewModel.quantities.value[3])

        viewModel.decrementItem(3)

        assertTrue("El mapa de cantidades debería estar vacío", viewModel.quantities.value.isEmpty())
        assertTrue("La lista de items pedidos debería estar vacía", viewModel.orderedItems.value.isEmpty())
    }

    @Test
    fun calculoDelTotalAPagar() = runTest {
        backgroundScope.launch(testDispatcher) { viewModel.uiState.collect {} }

        viewModel.addItem(1) // 5.50
        viewModel.addItem(2) // 6.00
        viewModel.incrementItem(2) // 6.00
        viewModel.addItem(3) // 7.50

        val totalEsperado = 5.50 + 6.00 + 6.00 + 7.50 // 25.0
        assertEquals(totalEsperado, viewModel.total.value, 0.001)
    }

    @Test
    fun placeOrder_calculaCorrectamenteLaCantidadTotalDeArticulos() = runTest {
        // Mantenemos la suscripcion activa para que los StateFlows se actualicen
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect {} }

        // Agregar 2 Patatas Bravas (ID: 1) y 1 Croquetas (ID: 2)
        viewModel.addItem(1)
        viewModel.incrementItem(1)
        viewModel.addItem(2)

        // Ejecutar la accion de pedir
        viewModel.placeOrder()

        val confirmation = viewModel.confirmation.value

        // Verificaciones
        assertNotNull("La confirmación no debería ser nula", confirmation)
        // Existen 2 items distintos en la lista, pero la suma de las cantidades es 3
        assertEquals(2, viewModel.orderedItems.value.size)
        assertEquals(3, confirmation?.itemCount)
    }

    @Test
    fun dismissConfirmation_limpiaElDialogoYElEstadoDelPedido() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect {} }

        // Preparar un estado con items
        viewModel.addItem(3) // Calamares
        viewModel.placeOrder()

        // Asegurar de que el estado no esta vacio antes de limpiar
        assertFalse(viewModel.isEmpty.value)
        assertNotNull(viewModel.confirmation.value)

        // Ejecutar la accion de descartar/aceptar confirmacion
        viewModel.dismissConfirmation()

        // Verificaciones del reinicio completo del ViewModel
        assertNull("La confirmación debe ser nula tras descartarse", viewModel.confirmation.value)
        assertTrue("El mapa de cantidades debe estar vacío", viewModel.quantities.value.isEmpty())
        assertTrue("El pedido debe reportarse como vacío", viewModel.isEmpty.value)
        assertEquals("El total debe reiniciarse a 0.0", 0.0, viewModel.total.value, 0.0)
    }
}