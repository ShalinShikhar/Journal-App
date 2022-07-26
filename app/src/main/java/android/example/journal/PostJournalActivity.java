package android.example.journal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.example.journal.model.Journal;
import android.example.journal.util.JournalApi;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Date;

public class PostJournalActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "PostJournalActivity";
    private Button saveButton;
    private ProgressBar progressBar;
    private ImageView addPhotoButton;
    private EditText titleEditText;
    private EditText thoughtEditText;
    private TextView currentUserTextView;
    private ImageView imageView;

    private String currentUserId;
    private String currentUserName;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;

    private FirebaseFirestore db=FirebaseFirestore.getInstance();
    private StorageReference storageReference;

    private CollectionReference collectionReference=db.collection("Journal");
    private static final int GALLERY_CODE=1;
    private Uri imageUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_journal);

        firebaseAuth=FirebaseAuth.getInstance();
        storageReference= FirebaseStorage.getInstance().getReference();
        progressBar=findViewById(R.id.post_progressBar);
        titleEditText=findViewById(R.id.post_title_et);
        thoughtEditText=findViewById(R.id.post_description_et);
        currentUserTextView=findViewById(R.id.post_username_text);
        imageView=findViewById(R.id.post_imageView);

        saveButton=findViewById(R.id.post_save_journal_button);
        addPhotoButton=findViewById(R.id.postCameraButton);
        saveButton.setOnClickListener(this);
        addPhotoButton.setOnClickListener(this);

        progressBar.setVisibility(View.INVISIBLE);

        if(JournalApi.getInstance()!=null)
        {
            currentUserId=JournalApi.getInstance().getUserId();

            currentUserName=JournalApi.getInstance().getUsername();

            currentUserTextView.setText(currentUserName);

        }

        authStateListener=new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                user=firebaseAuth.getCurrentUser();
                if(user!=null)
                {

                }
                else
                {

                }

            }
        };





    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){

            case R.id.post_save_journal_button:
                saveJournal();

                break;
            case R.id.postCameraButton:
                Intent galleryIntent=new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,GALLERY_CODE);
                break;
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==GALLERY_CODE && resultCode==RESULT_OK)
        {
            if(data!=null)
            {
                imageUri=data.getData();
                imageView.setImageURI(imageUri);

            }
        }
    }

    private void saveJournal() {

        String title=titleEditText.getText().toString().trim();
        String thoughts=thoughtEditText.getText().toString().trim();
        progressBar.setVisibility(View.VISIBLE);
        if(!TextUtils.isEmpty(title) && !TextUtils.isEmpty(thoughts) && imageUri!=null)
        {
            StorageReference filePath=storageReference.child("journal_image").child("my_image"+ Timestamp.now().getSeconds());
            filePath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    //create a journal object
                                    String imgUrl=uri.toString();
                                    Journal journal=new Journal();
                                    journal.setTitle(title);
                                    journal.setThought(thoughts);
                                    journal.setImageUrl(imgUrl);
                                    journal.setTimeAdded(new Timestamp(new Date()));
                                    journal.setUserName(currentUserName);
                                    journal.setUserId(currentUserId);
                                    // Invoke our collectionreference
                                    collectionReference.add(journal).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {
                                            progressBar.setVisibility(View.INVISIBLE);
                                            startActivity(new Intent(PostJournalActivity.this,JournalListActivity.class));
                                            finish();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {

                                            Log.d(TAG,"onFailure"+e.toString());
                                        }
                                    });
                                    //add and save a Journal object

                                }
                            });


                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressBar.setVisibility(View.INVISIBLE);

                }
            });
        }
        else
        {
            progressBar.setVisibility(View.INVISIBLE);
        }
    }



    @Override
    protected void onStart() {
        super.onStart();
        user=firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(firebaseAuth!=null)
        {
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }
}