package ydkim2110.com.androidbarberbooking.Fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.jaredrummler.materialspinner.MaterialSpinner;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;
import ydkim2110.com.androidbarberbooking.Adapter.MySalonAdapter;
import ydkim2110.com.androidbarberbooking.Common.Common;
import ydkim2110.com.androidbarberbooking.Common.SpaceItemDecoration;
import ydkim2110.com.androidbarberbooking.Interface.IAllSalonLoadListener;
import ydkim2110.com.androidbarberbooking.Interface.IBranchLoadListener;
import ydkim2110.com.androidbarberbooking.Model.Salon;
import ydkim2110.com.androidbarberbooking.R;

public class BookingStep1Fragment extends Fragment implements IAllSalonLoadListener, IBranchLoadListener {

    private static final String TAG = BookingStep1Fragment.class.getSimpleName();

    private LocalBroadcastManager mLocalBroadcastManager;
    // Variable
    CollectionReference allSalonRef;
    CollectionReference branchRef;

    IAllSalonLoadListener iAllSalonLoadListener;
    IBranchLoadListener iBranchLoadListener;

    @BindView(R.id.spinner)
    MaterialSpinner spinner;
    @BindView(R.id.recycler_salon)
    RecyclerView recycler_salon;
    @BindView(R.id.no_item)
    TextView no_item;

    Unbinder unBinder;

    AlertDialog dialog;

    private static BookingStep1Fragment instance;

    public static BookingStep1Fragment getInstance() {
        if (instance == null) {
            instance = new BookingStep1Fragment();
        }
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        allSalonRef = FirebaseFirestore.getInstance().collection("AllSalon");

        iAllSalonLoadListener = this;
        iBranchLoadListener = this;

        dialog = new SpotsDialog.Builder().setContext(getActivity()).build();

        mLocalBroadcastManager = LocalBroadcastManager.getInstance(getContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_booking_step_one, container,false);
        unBinder = ButterKnife.bind(this, view);

        initView();
        loadAllSalon();

        return view;
    }

    private void initView() {
        Log.d(TAG, "initView: called!!");
        recycler_salon.setHasFixedSize(true);
        recycler_salon.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        recycler_salon.addItemDecoration(new SpaceItemDecoration(4));
    }

    private void loadAllSalon() {
        Log.d(TAG, "loadAllSalon: called!!");
        allSalonRef.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<String> list = new ArrayList<>();
                            list.add("지역을 선택하세요.");
                            for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                list.add(documentSnapshot.getId());
                            }
                            iAllSalonLoadListener.onAllSalonLoadSuccess(list);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        iAllSalonLoadListener.onAllSalonLoadFailed(e.getMessage());
                    }
                });
    }

    @Override
    public void onAllSalonLoadSuccess(List<String> areaNameList) {
        Log.d(TAG, "onAllSalonLoadSuccess: called!!");
        spinner.setItems(areaNameList);
        spinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
                Log.d(TAG, "onItemSelected: position: "+position);
                if (position > 0) {
                    makeNextBtnEnableFalse();
                    loadBranchOfCity(item.toString());
                } else {
                    makeNextBtnEnableFalse();
                    recycler_salon.setVisibility(View.GONE);
                }
            }
        });
    }

    private void makeNextBtnEnableFalse() {
        Log.d(TAG, "makeNextBtnEnableFalse: called!!");
        Intent intent = new Intent(Common.KEY_UNABLE_BUTTON_NEXT);
        mLocalBroadcastManager.sendBroadcast(intent);
    }

    @Override
    public void onAllSalonLoadFailed(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    private void loadBranchOfCity(String cityName) {
        Log.d(TAG, "loadBranchOfCity: called!! passed item is "+cityName);
        dialog.show();

        Common.city = cityName;

        branchRef = FirebaseFirestore.getInstance()
                .collection("AllSalon")
                .document(cityName)
                .collection("Branch");

        branchRef.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        List<Salon> list = new ArrayList<>();
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                Salon salon = documentSnapshot.toObject(Salon.class);
                                // add salonId
                                salon.setSalonId(documentSnapshot.getId());
                                list.add(salon);
                            }
                            iBranchLoadListener.onAllBranchLoadSuccess(list);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        iBranchLoadListener.onAllBranchLoadFailed(e.getMessage());
                    }
                });
    }

    @Override
    public void onAllBranchLoadSuccess(List<Salon> salonList) {
        Log.d(TAG, "onAllBranchLoadSuccess: called!!");
        if (salonList.size() == 0) {
            no_item.setVisibility(View.VISIBLE);
            recycler_salon.setVisibility(View.GONE);
        } else {
            MySalonAdapter adapter = new MySalonAdapter(getActivity(), salonList);
            recycler_salon.setAdapter(adapter);
            no_item.setVisibility(View.GONE);
            recycler_salon.setVisibility(View.VISIBLE);
        }

        dialog.dismiss();
    }

    @Override
    public void onAllBranchLoadFailed(String message) {
        Log.d(TAG, "onAllBranchLoadFailed: called!!");
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
        dialog.dismiss();
    }
}
