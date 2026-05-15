package id.project.lab.ui.auth;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import id.project.lab.R;
import id.project.lab.ui.OnboardingActivity;
import id.project.lab.MainActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Configure Google Sign In using auto-generated Web Client ID from google-services plugin
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Initialize ActivityResultLauncher
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    // Handle the result regardless of result code — Google Sign-In
                    // can return account data even with non-OK codes on some devices
                    Intent data = result.getData();
                    if (data != null) {
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                        try {
                            GoogleSignInAccount account = task.getResult(ApiException.class);
                            if (account != null) {
                                Log.d(TAG, "Google Sign-In success, authenticating with Firebase...");
                                firebaseAuthWithGoogle(account.getIdToken());
                            }
                        } catch (ApiException e) {
                            // Common error codes: 10 (Developer Error/SHA-1 issue),
                            // 12500 (sign-in attempt didn't succeed), 12501 (user cancelled)
                            Log.e(TAG, "Google Sign-In ApiException: code=" + e.getStatusCode() + " msg=" + e.getMessage(), e);
                            String errorMsg;
                            switch (e.getStatusCode()) {
                                case 10:
                                    errorMsg = "Developer Error: Periksa SHA-1 di Firebase Console";
                                    break;
                                case 12501:
                                    errorMsg = "Login dibatalkan";
                                    break;
                                default:
                                    errorMsg = "Google Error (" + e.getStatusCode() + "): " + e.getMessage();
                                    break;
                            }
                            Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Log.w(TAG, "Google Sign-In returned null data, resultCode=" + result.getResultCode());
                        Toast.makeText(this, "Login gagal, coba lagi", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        LinearLayout googleBtn = findViewById(R.id.google_login_button);
        if (googleBtn != null) {
            googleBtn.setOnClickListener(v -> signIn());
        }

        LinearLayout githubBtn = findViewById(R.id.github_login_button);
        if (githubBtn != null) {
            githubBtn.setOnClickListener(v -> {
                Toast.makeText(this, "GitHub Login Clicked (Not implemented yet)", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void signIn() {
        // Sign out first to clear cached credentials — fixes "result 0" issue
        // where the account picker doesn't appear due to stale state
        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        updateUI(user);
                    } else {
                        Toast.makeText(this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
                        updateUI(null);
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            checkOnboardingStatus(currentUser);
        }
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            checkOnboardingStatus(user);
        }
    }

    private void checkOnboardingStatus(FirebaseUser user) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(user.getUid()).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists() && document.contains("role")) {
                            // Sync profile picture if missing or changed
                            if (user.getPhotoUrl() != null) {
                                String currentPhoto = document.getString("profilePicture");
                                if (currentPhoto == null || !currentPhoto.equals(user.getPhotoUrl().toString())) {
                                    db.collection("users").document(user.getUid())
                                            .update("profilePicture", user.getPhotoUrl().toString());
                                }
                            }
                            // User has completed onboarding
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            // User is new or hasn't completed onboarding
                            Intent intent = new Intent(LoginActivity.this, OnboardingActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    } else {
                        // Error, fallback to onboarding just in case
                        Intent intent = new Intent(LoginActivity.this, OnboardingActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
    }
}
