package com.example.notesapp.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.notesapp.R;
import com.example.notesapp.adapters.NotesAdapter;
import com.example.notesapp.entities.Note;
import com.example.notesapp.entities.User;
import com.example.notesapp.listeners.NotesListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivity extends AppCompatActivity implements NotesListener {

    public static final int REQUEST_CODE_ADD_NOTE = 1;
    public static final int REQUEST_CODE_UPDATE_NOTE = 2;
    public static final String TAG_STORAGE = "STORE";


    private RecyclerView notesRecyclerView;
    private final List<Note> noteList = new ArrayList<>();

    private NotesAdapter notesAdapter;

    private int noteClickedPosition = -1;

    private AlertDialog dialogShare;

    private EditText textDestinationName;
    private EditText textDestinationEmail;
    private TextView textShareNoteMessage;
    private TextView textShareNote;

    private DatabaseReference databaseReference, newDatabaseReference;

    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    FirebaseFirestore fireStore = FirebaseFirestore.getInstance();
    String currentUser = firebaseAuth.getCurrentUser().getUid();

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView textMyNotes = findViewById(R.id.textMyNotes);

        DocumentReference documentReference = fireStore.collection("users").document(currentUser);

        databaseReference = FirebaseDatabase.getInstance().getReference("AllUsersNotes/" + currentUser);

        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                assert documentSnapshot != null;
                textMyNotes.setText(documentSnapshot.getString("fullName") + "`s Notes ");
            }
        });

        ImageView imageAddNoteMain = findViewById(R.id.imageAddNoteMain);

        imageAddNoteMain.setOnClickListener(v -> startActivityForResult(
                new Intent(getApplicationContext(), CreateNoteActivity.class),
                REQUEST_CODE_ADD_NOTE
        ));

        notesRecyclerView = findViewById(R.id.notesRecyclerView);
        notesRecyclerView.setLayoutManager(
                new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)
        );

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                noteList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Note note = dataSnapshot.getValue(Note.class);
                    noteList.add(note);
                }
                notesAdapter = new NotesAdapter(noteList, MainActivity.this);
                notesRecyclerView.setAdapter(notesAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onNoteClicked(Note note, int position) {
        noteClickedPosition = position;
        Intent intent = new Intent(getApplicationContext(), CreateNoteActivity.class);
        intent.putExtra("isViewOrUpdate", true);
        intent.putExtra("note", note);
        startActivityForResult(intent, REQUEST_CODE_UPDATE_NOTE);
    }

    public void uploadNoteInNewUserAccount(Note note, String userId) {

        //todo upload the note to that path with that id -- DONE
        //todo push the note with the Mariana's notes -- NOT DONE

        Log.e("Ce Contine:", "DocumentSnapshot data: " + userId);

        newDatabaseReference = FirebaseDatabase.getInstance().getReference("AllUsersNotes/" + userId);

        newDatabaseReference.child(Objects.requireNonNull(note.getId())).setValue(note);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_UPDATE_NOTE) {
            notesAdapter.notifyDataSetChanged();
        }
    }

    public void logout(View view) {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getApplicationContext(), Login.class));
        finish();
    }

    @Override
    public void onShareButtonClicked(Note note, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        View view = LayoutInflater.from(this).inflate(R.layout.layout_share_note, findViewById(R.id.layoutShareNoteContainer));
        builder.setView(view);
        dialogShare = builder.create();

        if (dialogShare.getWindow() != null) {
            dialogShare.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }

        textDestinationName = view.findViewById(R.id.textDestinationName);
        textDestinationEmail = view.findViewById(R.id.textDestinationEmail);
        textShareNoteMessage = view.findViewById(R.id.textShareNoteMessage);
        textShareNote = view.findViewById(R.id.textShareNote);

        textShareNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textDestinationName.setVisibility(View.VISIBLE);
                textDestinationEmail.setVisibility(View.VISIBLE);
                textShareNoteMessage.setVisibility(View.GONE);
                textShareNote.setText("Send");

                textShareNote.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        CollectionReference docRef = fireStore.collection("users");
                        docRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @RequiresApi(api = Build.VERSION_CODES.N)
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    QuerySnapshot document = task.getResult();
                                    if (!document.isEmpty()) {
                                        String name = textDestinationName.getText().toString().trim();
                                        String email = textDestinationEmail.getText().toString().trim();
                                        if (TextUtils.isEmpty(name)) {
                                            textDestinationName.setError("Name is Required");
                                            return;
                                        }
                                        if (TextUtils.isEmpty(email)) {
                                            textDestinationEmail.setError("Email is Required");
                                            return;
                                        }
                                        AtomicBoolean nameOk = new AtomicBoolean(false);
                                        AtomicBoolean emailOk = new AtomicBoolean(false);
                                        document.getDocuments().forEach((temp) -> {
                                            Gson gson = new Gson();
                                            JsonElement jsonElement = gson.toJsonTree(temp.getData());
                                            User user = gson.fromJson(jsonElement, User.class);

                                            if (name.toLowerCase().equals(user.getFullName().toLowerCase()) && email.equals(user.getEmail())) {
                                                uploadNoteInNewUserAccount(note, temp.getId());
                                                dialogShare.dismiss();
                                            }

                                            if (name.toLowerCase().equals(user.getFullName().toLowerCase())) {
                                                nameOk.set(true);
                                            }
                                            if (email.equals(user.getEmail())) {
                                                emailOk.set(true);
                                            }

                                        });
                                        if (!nameOk.get())
                                            textDestinationName.setError("Name is incorrect");
                                        if (!emailOk.get())
                                            textDestinationEmail.setError("Email is incorrect");

                                        Log.e(TAG_STORAGE, "DocumentSnapshot data: " + document.toString());
                                    } else {
                                        Log.e(TAG_STORAGE, "No such document");
                                    }
                                } else {
                                    Log.e(TAG_STORAGE, "get failed with ", task.getException());
                                }
                            }
                        });
                    }
                });
            }
        });

        view.findViewById(R.id.textCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textDestinationName.setVisibility(View.GONE);
                textDestinationEmail.setVisibility(View.GONE);
                textShareNoteMessage.setVisibility(View.VISIBLE);
                textShareNote.setText("Send note");
                dialogShare.dismiss();
            }
        });

        dialogShare.show();
    }
}