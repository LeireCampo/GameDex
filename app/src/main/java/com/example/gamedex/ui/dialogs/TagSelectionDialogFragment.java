package com.example.gamedex.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gamedex.R;
import com.example.gamedex.data.local.entity.CustomTag;
import com.example.gamedex.ui.adapters.CustomTagSelectionAdapter;
import com.example.gamedex.ui.viewmodels.ProfileTagsViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TagSelectionDialogFragment extends DialogFragment {

    private static final String ARG_GAME_ID = "game_id";

    private String gameId;
    private OnTagsSelectedListener listener;

    private RecyclerView recyclerTags;
    private TextInputEditText editNewTag;
    private MaterialButton buttonAddTag;
    private MaterialButton buttonSave;
    private MaterialButton buttonCancel;

    private CustomTagSelectionAdapter adapter;
    private ProfileTagsViewModel viewModel;
    private List<CustomTag> currentSelectedTags = new ArrayList<>();

    public interface OnTagsSelectedListener {
        void onTagsSelected(List<CustomTag> selectedTags, List<CustomTag> tagsToRemove);
    }

    public static TagSelectionDialogFragment newInstance(String gameId) {
        TagSelectionDialogFragment fragment = new TagSelectionDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_GAME_ID, gameId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            gameId = getArguments().getString(ARG_GAME_ID);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_custom_tags, null);

        initViews(view);
        setupViewModel();
        setupRecyclerView();
        setupClickListeners();

        return new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Gestionar Etiquetas")
                .setView(view)
                .create();
    }

    private void initViews(View view) {
        recyclerTags = view.findViewById(R.id.recycler_tags);
        editNewTag = view.findViewById(R.id.edit_new_tag);
        buttonAddTag = view.findViewById(R.id.button_add_tag);
        buttonSave = view.findViewById(R.id.button_save);
        buttonCancel = view.findViewById(R.id.button_cancel);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(ProfileTagsViewModel.class);

        // Cargar las etiquetas actuales del juego
        viewModel.getTagsForGame(gameId).observe(this, currentTags -> {
            if (currentTags != null) {
                currentSelectedTags.clear();
                currentSelectedTags.addAll(currentTags);

                // Actualizar el adaptador si ya está configurado
                if (adapter != null) {
                    List<Integer> selectedIds = currentTags.stream()
                            .map(CustomTag::getId)
                            .collect(Collectors.toList());
                    adapter.setSelectedTagIds(selectedIds);
                }
            }
        });

        // Observar todas las etiquetas disponibles
        viewModel.getUserTags().observe(this, allTags -> {
            if (allTags != null) {
                List<Integer> selectedIds = currentSelectedTags.stream()
                        .map(CustomTag::getId)
                        .collect(Collectors.toList());
                adapter.updateTags(allTags, selectedIds);
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new CustomTagSelectionAdapter(requireContext(), new ArrayList<>(), new ArrayList<>());
        recyclerTags.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerTags.setAdapter(adapter);
    }

    private void setupClickListeners() {
        buttonAddTag.setOnClickListener(v -> createNewTag());
        buttonSave.setOnClickListener(v -> saveTagChanges());
        buttonCancel.setOnClickListener(v -> dismiss());
    }

    private void createNewTag() {
        String tagName = editNewTag.getText().toString().trim();

        if (tagName.isEmpty()) {
            editNewTag.setError("Ingresa un nombre para la etiqueta");
            return;
        }

        if (tagName.length() > 20) {
            editNewTag.setError("El nombre no puede superar los 20 caracteres");
            return;
        }

        // Generar color aleatorio
        String[] colors = {
                "#FF6B35", "#F7931E", "#FFD23F", "#06FFA5",
                "#00D2FF", "#3A86FF", "#8338EC", "#FF006E",
                "#FF5722", "#E91E63", "#9C27B0", "#673AB7",
                "#3F51B5", "#2196F3", "#03A9F4", "#00BCD4",
                "#009688", "#4CAF50", "#8BC34A", "#CDDC39"
        };
        String randomColor = colors[(int) (Math.random() * colors.length)];

        // Crear nueva etiqueta
        viewModel.createTag(tagName, randomColor);
        editNewTag.setText("");

        Toast.makeText(getContext(), "Etiqueta '" + tagName + "' creada", Toast.LENGTH_SHORT).show();
    }

    private void saveTagChanges() {
        List<CustomTag> selectedTags = adapter.getSelectedTags();
        List<CustomTag> tagsToRemove = new ArrayList<>();

        // Encontrar etiquetas que se han deseleccionado
        for (CustomTag currentTag : currentSelectedTags) {
            boolean stillSelected = selectedTags.stream()
                    .anyMatch(tag -> tag.getId() == currentTag.getId());
            if (!stillSelected) {
                tagsToRemove.add(currentTag);
            }
        }

        // Encontrar etiquetas nuevas que se han seleccionado
        List<CustomTag> tagsToAdd = new ArrayList<>();
        for (CustomTag selectedTag : selectedTags) {
            boolean wasAlreadySelected = currentSelectedTags.stream()
                    .anyMatch(tag -> tag.getId() == selectedTag.getId());
            if (!wasAlreadySelected) {
                tagsToAdd.add(selectedTag);
            }
        }

        // Aplicar cambios a través del ViewModel
        for (CustomTag tagToAdd : tagsToAdd) {
            viewModel.addTagToGame(gameId, tagToAdd.getId());
        }

        for (CustomTag tagToRemove : tagsToRemove) {
            viewModel.removeTagFromGame(gameId, tagToRemove.getId());
        }

        // Notificar al listener
        if (listener != null) {
            listener.onTagsSelected(tagsToAdd, tagsToRemove);
        }

        dismiss();
    }

    public void setOnTagsSelectedListener(OnTagsSelectedListener listener) {
        this.listener = listener;
    }
}