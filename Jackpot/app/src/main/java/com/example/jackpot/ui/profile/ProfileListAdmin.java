package com.example.jackpot.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jackpot.R;
import com.example.jackpot.User;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ProfileListAdmin extends Fragment {

    private RecyclerView recyclerView;
    private ProfileAdapter adapter;
    private Button buttonSelectAll, buttonDelete;
    private SearchView searchView;
    private FirebaseFirestore db;

    private final List<User> allUsers = new ArrayList<>();
    private final List<User> filteredUsers = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_profile_list_admin, container, false);

        recyclerView = root.findViewById(R.id.profiles_recycler_view);
        searchView = root.findViewById(R.id.search_view);
        buttonSelectAll = root.findViewById(R.id.button_select_all);
        buttonDelete = root.findViewById(R.id.button_delete_profile);

        db = FirebaseFirestore.getInstance();

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ProfileAdapter(filteredUsers);
        recyclerView.setAdapter(adapter);

        loadUsers();

        buttonSelectAll.setOnClickListener(v -> adapter.toggleSelectAll());
        buttonDelete.setOnClickListener(v -> deleteSelectedUsers());

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterUsers(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterUsers(newText);
                return true;
            }
        });

        return root;
    }

    private void loadUsers() {
        db.collection("users")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    allUsers.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        User user = doc.toObject(User.class);
                        if (user.getRole() != User.Role.ADMIN) {
                            allUsers.add(user);
                        }
                    }
                    filteredUsers.clear();
                    filteredUsers.addAll(allUsers);
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(requireContext(),
                        "Failed to load users: " + e.getMessage(),
                        Toast.LENGTH_LONG).show());
    }

    private void filterUsers(String query) {
        String lower = query.toLowerCase();
        List<User> filtered = allUsers.stream()
                .filter(u -> u.getName().toLowerCase().contains(lower)
                        || u.getEmail().toLowerCase().contains(lower)
                        || u.getRole().toString().toLowerCase().contains(lower))
                .collect(Collectors.toList());

        filteredUsers.clear();
        filteredUsers.addAll(filtered);
        adapter.notifyDataSetChanged();
    }

    private void deleteSelectedUsers() {
        List<User> selected = adapter.getSelectedUsers();
        if (selected.isEmpty()) {
            Toast.makeText(requireContext(), "No users selected", Toast.LENGTH_SHORT).show();
            return;
        }

        for (User user : selected) {
            db.collection("users")
                    .document(user.getId())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        filteredUsers.remove(user);
                        allUsers.remove(user);
                        adapter.notifyDataSetChanged();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(requireContext(),
                                    "Failed to delete " + user.getName() + ": " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show());
        }

        Toast.makeText(requireContext(), "Selected users deleted", Toast.LENGTH_SHORT).show();
    }
}

