package com.cartoniq.buildiq

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.cartoniq.buildiq.data.AppDatabase
import com.cartoniq.buildiq.data.ProjectEntity
import com.cartoniq.buildiq.model.CalculationItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProjectsViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.get(application).projectDao()

    val projects: LiveData<List<ProjectEntity>> = dao.observeProjects()

    fun delete(project: ProjectEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.deleteProject(project)
        }
    }

    fun duplicate(project: ProjectEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val copy = project.copy(
                id = 0,
                title = "${project.title} (Copy)",
                createdAt = System.currentTimeMillis()
            )
            dao.insertProject(copy)
        }
    }

    fun rename(project: ProjectEntity, newTitle: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = project.copy(title = newTitle)
            dao.updateProject(updated)
        }
    }

    fun update(project: ProjectEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.updateProject(project)
        }
    }

    fun addCalculationToProject(projectId: Long, newItem: CalculationItem, onComplete: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val existing = dao.getProjectById(projectId) ?: return@launch
            val currentItems = existing.getItems().toMutableList()
            currentItems.add(newItem)

            val updatedProject = ProjectEntity.createWithItems(
                id = existing.id,
                title = existing.title,
                summary = "${currentItems.size} calculations combined",
                items = currentItems,
                clientName = existing.clientName,
                clientAddress = existing.clientAddress,
                clientPhone = existing.clientPhone,
                clientEmail = existing.clientEmail,
                notes = existing.notes,
                laborType = existing.laborType,
                laborRate = existing.laborRate,
                laborHours = existing.laborHours,
                taxRate = existing.taxRate,
                contingencyRate = existing.contingencyRate,
                markupRate = existing.markupRate,
                currencySymbol = existing.currencySymbol,
                createdAt = existing.createdAt
            )
            dao.updateProject(updatedProject)
            withContext(Dispatchers.Main) {
                onComplete()
            }
        }
    }
}
