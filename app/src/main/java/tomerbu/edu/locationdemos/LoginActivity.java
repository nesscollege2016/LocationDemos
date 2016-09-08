package tomerbu.edu.locationdemos;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {


    // UI references.
    private AutoCompleteTextView etEmail;
    private EditText etPassword;
    private ProgressDialog dialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        etEmail = (AutoCompleteTextView) findViewById(R.id.email);


        etPassword = (EditText) findViewById(R.id.password);


        Button btnSignIn = (Button) findViewById(R.id.email_sign_in_button);
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginWithFirebase();
            }
        });

        Button btnRegister = (Button) findViewById(R.id.email_register_button);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerWithFirebase();
            }
        });

    }

    private void registerWithFirebase() {
        showProgressDialog();
        FirebaseAuth.getInstance().
                createUserWithEmailAndPassword(
                        getEmail(), getPassword()
                ).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                gotoMain();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                dialog.dismiss();
                showSnackbarError(e);

            }
        });
    }

    private void showSnackbarError(Exception e) {
        Snackbar.make(findViewById(R.id.email_sign_in_button), e.getLocalizedMessage(), Snackbar.LENGTH_INDEFINITE).setAction("dismiss", new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        }).show();
    }

    private void gotoMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void showProgressDialog() {
        if (dialog == null) {
            dialog = new ProgressDialog(this);
            dialog.setTitle("Signing you in");
            dialog.setMessage("Please wait");
            //dialog.setCancelable(false);
        }
        dialog.show();
    }

    private String getEmail() {
        return etEmail.getText().toString();
    }

    private String getPassword() {
        return etPassword.getText().toString();
    }

    private void loginWithFirebase() {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(getEmail(), getPassword()).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                gotoMain();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                dialog.dismiss();
                showSnackbarError(e);
            }
        });
    }

}