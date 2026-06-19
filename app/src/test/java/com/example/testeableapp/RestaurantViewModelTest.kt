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
}