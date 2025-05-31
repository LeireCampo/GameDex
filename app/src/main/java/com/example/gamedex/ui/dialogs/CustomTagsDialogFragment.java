package com.example.gamedex.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.example.gamedex.ui.viewmodels.CustomTagViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class CustomTagsDialogFragment extends DialogFragment {

    private static final String ARG_GAME_ID = "game_id";
    private static final String ARG_SELECTED_TAG_IDS = "selected_tag_ids";

    private String gameId;
    private List<Integer> selectedTagIds;
    private OnTagsSelectedListener listener;

    private RecyclerView recyclerTags;
    private TextInputEditText editNewTag;
    private MaterialButton buttonAddTag;
    private MaterialButton buttonSave;
    private MaterialButton buttonCancel;

    private CustomTagSelectionAdapter adapter;
    private CustomTagViewModel viewModel;

    public interface OnTagsSelectedListener {
        void onTagsSelected(List<CustomTag> selectedTags);
    }

    public static CustomTagsDialogFragment newInstance(String gameId, List<Integer> selectedTagIds) {
        CustomTagsDialogFragment fragment = new CustomTagsDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_GAME_ID, gameId);
        args.putIntegerArrayList(ARG_SELECTED_TAG_IDS, new ArrayList<>(selectedTagIds));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            gameId = getArguments().getString(ARG_GAME_ID);
            selectedTagIds = getArguments().getIntegerArrayList(ARG_SELECTED_TAG_IDS);
        }
        if (selectedTagIds == null) {
            selectedTagIds = new ArrayList<>();
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
        viewModel = new ViewModelProvider(this).get(CustomTagViewModel.class);
        viewModel.init(requireActivity().getApplication());

        // Observar etiquetas disponibles
        viewModel.getAllCustomTags().observe(this, tags -> {
            if (tags != null) {
                adapter.updateTags(tags, selectedTagIds);
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new CustomTagSelectionAdapter(requireContext(),
                new ArrayList<>(), selectedTagIds);
        recyclerTags.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerTags.setAdapter(adapter);
    }

    private void setupClickListeners() {
        buttonAddTag.setOnClickListener(v -> addNewTag());
        buttonSave.setOnClickListener(v -> saveSelectedTags());
        buttonCancel.setOnClickListener(v -> dismiss());
    }

    private void addNewTag() {
        String tagName = editNewTag.getText().toString().trim();
        if (tagName.isEmpty()) {
            editNewTag.setError("Ingresa un nombre para la etiqueta");
            return;
        }

        // Generar color aleatorio para la nueva etiqueta
        String[] colors = {
                "#FF6B35", "#F7931E", "#FFD23F", "#06FFA5",
                "#00D2FF", "#3A86FF", "#8338EC", "#FF006E"
        };
        String randomColor = colors[(int) (Math.random() * colors.length)];

        CustomTag newTag = new CustomTag(tagName, randomColor);
        viewModel.addCustomTag(newTag);

        editNewTag.setText("");
        Toast.makeText(getContext(), "Etiqueta '" + tagName + "' creada", Toast.LENGTH_SHORT).show();
    }

    private void saveSelectedTags() {
        List<CustomTag> selectedTags = adapter.getSelectedTags();

        if (listener != null) {
            listener.onTagsSelected(selectedTags);
        }

        dismiss();
    }

    public void setOnTagsSelectedListener(OnTagsSelectedListener listener) {
        this.listener = listener;
    }
}