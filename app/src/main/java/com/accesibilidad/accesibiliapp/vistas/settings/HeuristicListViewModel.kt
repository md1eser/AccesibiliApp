package com.accesibilidad.accesibiliapp.vistas.settings


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import com.accesibilidad.accesibiliapp.data.deteccion.Detector
import com.accesibilidad.accesibiliapp.data.heuristicas.Heuristic
import com.accesibilidad.accesibiliapp.data.repository.HeuristicRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HeuristicListViewModel @Inject constructor(
    private val repo: HeuristicRepository,
    private val detector: Detector
) : ViewModel() {

    // Flujos de datos directos del repositorio
    val getAll: Flow<List<Heuristic>> = repo.getAll()
    val labels: Flow<List<String>> = detector.labels

    fun getById(id: String): Flow<Heuristic?> = repo.getHeuristic(id)

    fun add(heuristic: Heuristic) {
        viewModelScope.launch {
            repo.add(heuristic)
        }
    }

    fun delete(id: String) {
        viewModelScope.launch {
            repo.deleteById(id)
        }
    }
}