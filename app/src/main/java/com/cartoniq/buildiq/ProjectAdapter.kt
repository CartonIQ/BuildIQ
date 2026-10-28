package com.cartoniq.buildiq

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.cartoniq.buildiq.data.ProjectEntity
import com.cartoniq.buildiq.databinding.ItemProjectBinding
import com.cartoniq.buildiq.model.Calculators
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private object ProjectDiff : DiffUtil.ItemCallback<ProjectEntity>() {
    override fun areItemsTheSame(oldItem: ProjectEntity, newItem: ProjectEntity): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: ProjectEntity, newItem: ProjectEntity): Boolean =
        oldItem == newItem
}

class ProjectAdapter(
    private val onPdf: (ProjectEntity) -> Unit,
    private val onShareText: (ProjectEntity) -> Unit,
    private val onDuplicate: (ProjectEntity) -> Unit,
    private val onEdit: (ProjectEntity) -> Unit,
    private val onDelete: (ProjectEntity) -> Unit
) : ListAdapter<ProjectEntity, ProjectAdapter.VH>(ProjectDiff) {

    class VH(val b: ItemProjectBinding) : RecyclerView.ViewHolder(b.root)

    private val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.US)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(ItemProjectBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val project = getItem(position)
        val cur = project.currencySymbol
        val items = project.getItems()

        holder.b.projectTitle.text = project.title
        val summaryText = if (items.isNotEmpty()) {
            "${items.size} calculation${if (items.size > 1) "s" else ""}: " + items.joinToString(", ") { it.title }
        } else {
            project.summary
        }
        holder.b.projectSummary.text = summaryText

        val clientSuffix = if (project.clientName.isNotBlank()) " • Client: ${project.clientName}" else ""
        holder.b.projectDate.text = dateFormat.format(Date(project.createdAt)) + clientSuffix
        holder.b.projectCost.text = Calculators.money(project.totalCost, cur)

        holder.b.btnExportPdf.setOnClickListener { onPdf(project) }
        holder.b.btnShareText.setOnClickListener { onShareText(project) }
        holder.b.btnDuplicate.setOnClickListener { onDuplicate(project) }
        holder.b.btnEditProject.setOnClickListener { onEdit(project) }
        holder.b.deleteButton.setOnClickListener { onDelete(project) }
        holder.b.root.setOnClickListener { onEdit(project) }
    }
}
