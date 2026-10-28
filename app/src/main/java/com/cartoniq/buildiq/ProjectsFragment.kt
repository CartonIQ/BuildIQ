package com.cartoniq.buildiq

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.cartoniq.buildiq.data.ProjectEntity
import com.cartoniq.buildiq.databinding.DialogEditProjectBinding
import com.cartoniq.buildiq.databinding.FragmentProjectsBinding
import com.cartoniq.buildiq.model.Calculators
import com.cartoniq.buildiq.quotation.QuoteGenerator
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ProjectsFragment : Fragment() {

    private var _binding: FragmentProjectsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProjectsViewModel by viewModels()
    private var allProjects: List<ProjectEntity> = emptyList()
    private var filterQuery: String = ""

    private lateinit var adapter: ProjectAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProjectsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ProjectAdapter(
            onPdf = { project ->
                try {
                    QuoteGenerator.shareProject(requireContext(), project, asPdf = true)
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Failed to generate PDF: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            },
            onShareText = { project ->
                QuoteGenerator.shareProject(requireContext(), project, asPdf = false)
            },
            onDuplicate = { project ->
                viewModel.duplicate(project)
                Toast.makeText(requireContext(), "Duplicated '${project.title}'", Toast.LENGTH_SHORT).show()
            },
            onEdit = { project ->
                showEditProjectDialog(project)
            },
            onDelete = { project ->
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Delete Project")
                    .setMessage("Are you sure you want to delete '${project.title}'? This cannot be undone.")
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Delete") { _, _ ->
                        viewModel.delete(project)
                        Toast.makeText(requireContext(), "Project deleted", Toast.LENGTH_SHORT).show()
                    }
                    .show()
            }
        )

        binding.projectsList.layoutManager = LinearLayoutManager(requireContext())
        binding.projectsList.adapter = adapter

        binding.projectSearchEdit.doAfterTextChanged {
            filterQuery = it?.toString()?.trim() ?: ""
            updateFilteredList()
        }

        viewModel.projects.observe(viewLifecycleOwner) { list ->
            allProjects = list
            updateFilteredList()
        }
    }

    private fun updateFilteredList() {
        val filtered = if (filterQuery.isBlank()) {
            allProjects
        } else {
            allProjects.filter {
                it.title.contains(filterQuery, ignoreCase = true) ||
                        it.clientName.contains(filterQuery, ignoreCase = true) ||
                        it.summary.contains(filterQuery, ignoreCase = true)
            }
        }
        adapter.submitList(filtered)
        val empty = filtered.isEmpty()
        binding.emptyState.visibility = if (empty) View.VISIBLE else View.GONE
        binding.projectsList.visibility = if (empty) View.GONE else View.VISIBLE
    }

    private fun showEditProjectDialog(project: ProjectEntity) {
        val dialogBinding = DialogEditProjectBinding.inflate(LayoutInflater.from(requireContext()))

        dialogBinding.editTitle.setText(project.title)
        dialogBinding.editClientName.setText(project.clientName)
        dialogBinding.editClientAddress.setText(project.clientAddress)
        dialogBinding.editClientPhone.setText(project.clientPhone)
        dialogBinding.editClientEmail.setText(project.clientEmail)
        dialogBinding.editNotes.setText(project.notes)

        if (project.laborRate > 0) dialogBinding.editLaborRate.setText(Calculators.f2(project.laborRate))
        if (project.laborHours > 0) dialogBinding.editLaborHours.setText(Calculators.f1(project.laborHours))
        if (project.contingencyRate > 0) dialogBinding.editContingencyRate.setText(Calculators.f1(project.contingencyRate))
        if (project.markupRate > 0) dialogBinding.editMarkupRate.setText(Calculators.f1(project.markupRate))
        if (project.taxRate > 0) dialogBinding.editTaxRate.setText(Calculators.f1(project.taxRate))

        MaterialAlertDialogBuilder(requireContext())
            .setView(dialogBinding.root)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Save Changes") { _, _ ->
                val newTitle = dialogBinding.editTitle.text.toString().trim().ifBlank { project.title }
                val clientName = dialogBinding.editClientName.text.toString().trim()
                val clientAddress = dialogBinding.editClientAddress.text.toString().trim()
                val clientPhone = dialogBinding.editClientPhone.text.toString().trim()
                val clientEmail = dialogBinding.editClientEmail.text.toString().trim()
                val notes = dialogBinding.editNotes.text.toString().trim()

                val laborRate = dialogBinding.editLaborRate.text.toString().toDoubleOrNull() ?: 0.0
                val laborHours = dialogBinding.editLaborHours.text.toString().toDoubleOrNull() ?: 0.0
                val laborType = if (laborHours > 0) "HOURLY" else if (laborRate > 0) "FLAT" else "NONE"

                val contingency = dialogBinding.editContingencyRate.text.toString().toDoubleOrNull() ?: 0.0
                val markup = dialogBinding.editMarkupRate.text.toString().toDoubleOrNull() ?: 0.0
                val tax = dialogBinding.editTaxRate.text.toString().toDoubleOrNull() ?: 0.0

                val updated = ProjectEntity.createWithItems(
                    id = project.id,
                    title = newTitle,
                    summary = project.summary,
                    items = project.getItems(),
                    clientName = clientName,
                    clientAddress = clientAddress,
                    clientPhone = clientPhone,
                    clientEmail = clientEmail,
                    notes = notes,
                    laborType = laborType,
                    laborRate = laborRate,
                    laborHours = laborHours,
                    taxRate = tax,
                    contingencyRate = contingency,
                    markupRate = markup,
                    currencySymbol = project.currencySymbol,
                    createdAt = project.createdAt
                )

                viewModel.update(updated)
                Toast.makeText(requireContext(), "Project updated!", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
