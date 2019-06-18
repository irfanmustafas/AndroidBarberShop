package ydkim2110.com.androidbarberbooking;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import ydkim2110.com.androidbarberbooking.Common.Common;
import ydkim2110.com.androidbarberbooking.Fragments.HomeFragment;
import ydkim2110.com.androidbarberbooking.Fragments.ShoppingFragment;
import ydkim2110.com.androidbarberbooking.Model.User;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = HomeActivity.class.getSimpleName();

    @BindView(R.id.bottom_navigation)
    BottomNavigationView mBottomNavigationView;

    BottomSheetDialog mBottomSheetDialog;

    CollectionReference mUserRef;

    AlertDialog mAlertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Log.d(TAG, "onCreate: started!!");

        ButterKnife.bind(HomeActivity.this);

        // init
        mUserRef = FirebaseFirestore.getInstance().collection(getResources().getString(R.string.collection_user));

        mAlertDialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();

        // Check intent, if is login == true, enable full access
        // If is login == false, just let user around shopping to view
        if (getIntent() != null) {
            boolean isLogin = getIntent().getBooleanExtra(Common.IS_LOGIN, false);
            if (isLogin) {
                Log.d(TAG, "isLogin: called!!");
                mAlertDialog.show();

                // Check if user is exists
                AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                    @Override
                    public void onSuccess(Account account) {
                        Log.d(TAG, "onSuccess: called!!");
                        if (account != null) {
                            DocumentReference currentUser = mUserRef.document(account.getPhoneNumber().toString());
                            currentUser.get()
                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                DocumentSnapshot userSnapshot = task.getResult();
                                                if (!userSnapshot.exists()) {
                                                    Log.d(TAG, "onComplete: called!");
                                                    showUpdateDialog(account.getPhoneNumber().toString());
                                                }
                                                else {
                                                    // If user already available in out system.
                                                    Common.currentUser = userSnapshot.toObject(User.class);
                                                    mBottomNavigationView.setSelectedItemId(R.id.action_home);
                                                }
                                                if (mAlertDialog.isShowing()) {
                                                    mAlertDialog.dismiss();
                                                }
                                            }
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onError(AccountKitError accountKitError) {
                        Log.d(getClass().getSimpleName(), "onError: ");
                        Toast.makeText(HomeActivity.this, "" + accountKitError.getErrorType().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

        // View
        mBottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            Fragment fragment = null;

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.action_home) {
                    fragment = new HomeFragment();
                } else if (menuItem.getItemId() == R.id.action_shopping) {
                    fragment = new ShoppingFragment();
                }
                return loadFragment(fragment);
            }
        });
    }

    private boolean loadFragment(Fragment fragment) {
        Log.d(TAG, "loadFragment: called!!");
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }

    private void showUpdateDialog(String phoneNumber) {
        Log.d(TAG, "showUpdateDialog: ");

        // Init dialog
        mBottomSheetDialog = new BottomSheetDialog(this);
        mBottomSheetDialog.setTitle("One more step!");
        mBottomSheetDialog.setCanceledOnTouchOutside(false);
        mBottomSheetDialog.setCancelable(false);

        View sheetView = getLayoutInflater().inflate(R.layout.layout_update_information, null);

        Button btn_update = sheetView.findViewById(R.id.btn_update);
        TextInputEditText edt_name = sheetView.findViewById(R.id.edt_name);
        TextInputEditText edt_address = sheetView.findViewById(R.id.edt_address);

        btn_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mAlertDialog.isShowing()) {
                    mAlertDialog.show();
                }
                User user = new User(edt_name.getText().toString(),
                        edt_address.getText().toString(),
                        phoneNumber);

                mUserRef.document(phoneNumber)
                        .set(user)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                mBottomSheetDialog.dismiss();

                                if (mAlertDialog.isShowing()) {
                                    mAlertDialog.dismiss();
                                }

                                Common.currentUser = user;
                                mBottomNavigationView.setSelectedItemId(R.id.action_home);

                                Toast.makeText(HomeActivity.this, "Thank you",
                                        Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                mBottomSheetDialog.dismiss();

                                if (mAlertDialog.isShowing()) {
                                    mAlertDialog.dismiss();
                                }

                                Toast.makeText(HomeActivity.this, ""+e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        mBottomSheetDialog.setContentView(sheetView);
        mBottomSheetDialog.show();

    }
}
