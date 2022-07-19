package android.example.journal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.example.journal.util.JournalApi;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class loginActivity extends AppCompatActivity {

    private Button loginButton;
    private Button createAcctButton;
    private ProgressBar progreessBar;

    private AutoCompleteTextView emailAddress;
    private EditText password;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;

    private FirebaseFirestore db=FirebaseFirestore.getInstance();
    private CollectionReference collectionReference=db.collection("Users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseAuth=FirebaseAuth.getInstance();
        progreessBar=findViewById(R.id.login_progress);

        loginButton=findViewById(R.id.email_sign_in_button);
        createAcctButton=findViewById(R.id.create_acct_button);
        emailAddress=findViewById(R.id.email);
        password=findViewById(R.id.password);
        createAcctButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(loginActivity.this,CreateAccountActivity.class));
            }
        });
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginEmailPasswordUser(emailAddress.getText().toString().trim(),password.getText().toString().trim());
            }
        });
    }

    private void loginEmailPasswordUser(String email, String pwd) {
        progreessBar.setVisibility(View.VISIBLE);

        if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(pwd))
        {
                firebaseAuth.signInWithEmailAndPassword(email,pwd).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        FirebaseUser user=firebaseAuth.getCurrentUser();
                        if(user!=null)
                        {
                            String currentUserId=user.getUid();
                            collectionReference.whereEqualTo("userId",currentUserId).addSnapshotListener(new EventListener<QuerySnapshot>() {
                                @Override
                                public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                                    if(error!=null)
                                    {

                                    }
                                    else{
                                        if(value!=null)
                                        {
                                            progreessBar.setVisibility(View.INVISIBLE);
                                            if(!value.isEmpty())
                                            {
                                                for(QueryDocumentSnapshot snapshot : value)
                                                {
                                                    JournalApi journalApi=JournalApi .getInstance();
                                                    journalApi.setUsername(snapshot.getString("username"));
                                                    journalApi.setUserId(snapshot.getString("userId"));
                                                    startActivity(new Intent(loginActivity.this,PostJournalActivity.class));
                                                }


                                            }
                                        }

                                    }

                                }


                            });
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progreessBar.setVisibility(View.INVISIBLE);

                    }

                });
        }
        else
        {
            Toast.makeText(loginActivity.this,"Please Enter correct email and password",Toast.LENGTH_LONG).show();

            progreessBar.setVisibility(View.INVISIBLE);
        }
    }
}