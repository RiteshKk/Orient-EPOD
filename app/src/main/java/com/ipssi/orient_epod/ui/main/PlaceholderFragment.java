package com.ipssi.orient_epod.ui.main;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.snackbar.Snackbar;
import com.ipssi.orient_epod.InvoiceDetailsActivity;
import com.ipssi.orient_epod.R;
import com.ipssi.orient_epod.adapter.ImageAdapter;
import com.ipssi.orient_epod.callbacks.OnImageDeleteClickListener;
import com.ipssi.orient_epod.capturesignature.OnSignedCaptureListener;
import com.ipssi.orient_epod.capturesignature.SignatureDialogFragment;
import com.ipssi.orient_epod.databinding.FragmentTabbedBinding;
import com.ipssi.orient_epod.location.LocationAPI;
import com.ipssi.orient_epod.location.OnLocationChangeCallBack;
import com.ipssi.orient_epod.model.Invoice;
import com.ipssi.orient_epod.model.Receiver;
import com.ipssi.orient_epod.model.UploadDocumentEntity;
import com.ipssi.orient_epod.remote.remote.util.Resource;
import com.ipssi.orient_epod.remote.util.AppConstant;

import java.util.ArrayList;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;
import static com.ipssi.orient_epod.UtilKt.hideKeyboard;
import static com.ipssi.orient_epod.UtilKt.showAlertDialog;

/**
 * A placeholder fragment containing a simple view.
 */
public class PlaceholderFragment extends Fragment implements OnSignedCaptureListener, OnLocationChangeCallBack, OnImageDeleteClickListener {

    private FragmentTabbedBinding binding;
    private LocationAPI locationApi;
    private ImageAdapter adapter;
    private SharedPreferences permissionStatus;
    private PageViewModel viewModel;
    private boolean isFinalSubmit = false;
    private boolean hasError = false;
    private Invoice invoice;
    private int receiverId = 0;

    public static PlaceholderFragment newInstance(int index, Invoice invoice, Receiver receiver) {
        Bundle bundle = new Bundle();
        bundle.putInt(AppConstant.INDEX, index);
        bundle.putParcelable(AppConstant.INVOICE, invoice);
        bundle.putParcelable(AppConstant.RECEIVER, receiver);
        PlaceholderFragment fragment = new PlaceholderFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        binding = FragmentTabbedBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.setLifecycleOwner(this);
        permissionStatus = requireContext().getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);

        if (getArguments() != null) {
            invoice = getArguments().getParcelable(AppConstant.INVOICE);
        }
        viewModel = new ViewModelProvider(this).get(PageViewModel.class);
        binding.setViewModel(viewModel);
        locationApi = new LocationAPI(requireActivity(), this);

        init();

        setupForLastReceiver();

        handleClickListener();

        setObserver();
        binding.imageList.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        adapter = new ImageAdapter(this);
        binding.imageList.setAdapter(adapter);

        getUploadedDocs();
    }

    private void setupForLastReceiver() {
        int index = getArguments().getInt(AppConstant.INDEX, 0);
        if (index == 3) {
            viewModel.isCompleteTripChecked().setValue(true);
            binding.chechBoxCompleteTrip.setOnClickListener((buttonView) -> {
                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext());
                builder.setTitle("Alert").setMessage("No More Receiver").setPositiveButton(android.R.string.ok, (dialog, which) -> {
                            dialog.dismiss();
                            viewModel.isCompleteTripChecked().setValue(true);
                        }
                ).show();
            });
        }else{
            binding.chechBoxCompleteTrip.setOnCheckedChangeListener(((buttonView, isChecked) -> {
                viewModel.isCompleteTripChecked().setValue(isChecked);
            }));
        }
    }

    private void getUploadedDocs() {
        viewModel.fetchUploadedImage(invoice);
    }

    private void init() {
        Receiver receiver = Objects.requireNonNull(getArguments()).getParcelable(AppConstant.RECEIVER);
        binding.totalDamage.getEditText().setText(String.valueOf(InvoiceDetailsActivity.totalDamage));
        binding.loadType.getEditText().setText(invoice.getLoadType().equalsIgnoreCase("Standard") ? "Bags" : "MT");
        if (receiver != null) {
            viewModel.getName().setValue(receiver.getName());
            viewModel.getMobile().setValue(receiver.getMobile());
            if (!receiver.getInvoiceNumber().isEmpty()) {
                viewModel.getBagsReceived().setValue(receiver.getBagsRecv());
                viewModel.getDamageBags().setValue(receiver.getShortage());
                viewModel.getRemarks().setValue(receiver.getRemarks());
                byte[] decode = Base64.decode(receiver.getSign(), Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decode, 0, decode.length);
                viewModel.getSignature().setValue(bitmap);
                viewModel.isEditable().setValue(false);
            }
        }
    }

    private void setObserver() {
        viewModel.isEditable().observe(getViewLifecycleOwner(), (isEnabled) -> {
            binding.btnSubmit.setEnabled(isEnabled);
        });
        viewModel.isCompleteTripChecked().observe(getViewLifecycleOwner(), (isChecked) -> {
            if (isChecked) {
                binding.btnSubmit.setEnabled(true);
            } else {
                binding.btnSubmit.setEnabled(viewModel.isEditable().getValue());
            }
        });
        viewModel.getSignature().observe(getViewLifecycleOwner(), (bitmap) -> binding.sign.setImageBitmap(bitmap));
        viewModel.getName().observe(getViewLifecycleOwner(), s -> binding.layoutName.setError(null));
        viewModel.getMobile().observe(getViewLifecycleOwner(), s -> binding.layoutMobile.setError(null));
        viewModel.getBagsReceived().observe(getViewLifecycleOwner(), s -> {
            hasError = false;
            binding.layoutBags.setError(null);
            if (viewModel.isEditable().getValue()) {
                if (s.length() > 0) {
                    String damageBagsValue = viewModel.getDamageBags().getValue();
                    if (!damageBagsValue.isEmpty()) {
                        int damagedBags = Integer.parseInt(damageBagsValue.trim());
                        int receivedBags = Integer.parseInt(s);
                        if (damagedBags > receivedBags) {
                            hasError = true;
                            binding.layoutBags.setError("Damage bags quantity can not exceed Received bags quantity");
                        }
                    }
                }

                try {
                    if (InvoiceDetailsActivity.totalQuantity < InvoiceDetailsActivity.inputQuantity + Integer.parseInt(s)) {
                        hasError = true;
                        binding.layoutBags.setError("Bags quantity can not exceed total quantity");
                    }
                } catch (Exception e) {
                }
            }

        });
        viewModel.getDamageBags().observe(getViewLifecycleOwner(), s -> {
            binding.layoutDamageBags.setError(null);
            hasError = false;
            if (s.length() > 0) {
                String receivedBagsValue = viewModel.getBagsReceived().getValue();
                if (!receivedBagsValue.isEmpty()) {
                    int receivedBags = Integer.parseInt(receivedBagsValue);
                    int damageBags = Integer.parseInt(s);
                    if (damageBags > receivedBags) {
                        hasError = true;
                        binding.layoutDamageBags.setError("Damage bags quantity can not exceed Received bags quantity");
                    }
                } else {
                    hasError = true;
                    binding.layoutBags.setError("Enter Received bags quantity");
                }
            }
        });
        viewModel.getUploadStatus().observe(getViewLifecycleOwner(), (imageResponseResource) -> {
            switch (imageResponseResource.getStatus()) {
                case LOADING:
                    showSnackbar(getString(R.string.uploading_image));
                    break;
                case ERROR:
                    showSnackbar(imageResponseResource.getMessage());
                    break;
                case OFFLINE:
                    showAlertDialog(requireActivity(), imageResponseResource.getMessage());
                    break;
                case SUCCESS:
                    showSnackbar(getString(R.string.uploaded));
                    adapter.setImages(viewModel.getImageList().getValue().getData());
                    break;
            }
        });
        viewModel.getImageDeleteObserver().observe(getViewLifecycleOwner(), (epodResponseResource) -> {
            switch (epodResponseResource.getStatus()) {
                case ERROR:
                case OFFLINE:
                    showAlertDialog(requireActivity(), epodResponseResource.getMessage());
                    break;
                case LOADING:
                    break;
                case SUCCESS:
                    Snackbar.make(binding.getRoot(), epodResponseResource.getData().getMessage(), Snackbar.LENGTH_SHORT).show();
                    break;


            }
        });
        viewModel.getImageList().observe(getViewLifecycleOwner(), (imageList) -> {
            switch (imageList.getStatus()) {
                case LOADING:
                    viewModel.isLoading().setValue(true);
                    break;
                case ERROR:
                case OFFLINE:
                    viewModel.isLoading().setValue(false);
                    showAlertDialog(requireActivity(), imageList.getMessage());
                    break;
                case SUCCESS:
                    viewModel.isLoading().setValue(false);
                    adapter.setImages(imageList.getData());
                    break;
            }
        });
        viewModel.getSaveReceiverResponse().observe(getViewLifecycleOwner(), saveReceiverResponse -> {
            switch (saveReceiverResponse.getStatus()) {
                case SUCCESS:
                    Snackbar.make(binding.getRoot(), saveReceiverResponse.getData().getMessage(), Snackbar.LENGTH_SHORT).show();
                    try {
                        receiverId = saveReceiverResponse.getData().getStatus();
                        InvoiceDetailsActivity.totalDamage += Integer.parseInt(binding.layoutDamageBags.getEditText().getText().toString().trim());
                        InvoiceDetailsActivity.inputQuantity += Integer.parseInt(binding.layoutBags.getEditText().getText().toString().trim());
                    } catch (NumberFormatException e) {
                    }
                    if (isFinalSubmit) {
                        new Handler().postDelayed(() -> requireActivity().finish(), 2000
                        );
                    }
                    Objects.requireNonNull(binding.totalDamage.getEditText()).setText(String.valueOf(InvoiceDetailsActivity.totalDamage));
                    viewModel.isEditable().setValue(false);
                    viewModel.isLoading().setValue(false);
                    break;
                case ERROR:
                case OFFLINE:
                    viewModel.isLoading().setValue(false);
                    showAlertDialog(requireActivity(), saveReceiverResponse.getMessage());
                    break;
                case LOADING:
                    viewModel.isLoading().setValue(true);
                    break;
            }
        });
    }


    private void handleClickListener() {
        binding.sign.setOnClickListener(v -> {
            hideKeyboard(binding.getRoot(), requireContext());
            SignatureDialogFragment dialogFragment = new SignatureDialogFragment(PlaceholderFragment.this);
            dialogFragment.show(getChildFragmentManager(), "signature");
        });

        binding.btnSubmit.setOnClickListener(v -> {
            if (viewModel.isCompleteTripChecked().getValue()) {
                isFinalSubmit = true;
            }
            hideKeyboard(binding.getRoot(), requireContext());
            onSubmitClick();
        });

        binding.btnCaptureImage.setOnClickListener(v -> {
            Resource<ArrayList<UploadDocumentEntity>> value = viewModel.getImageList().getValue();
            if (value == null || value.getData() == null || value.getData().size() < 3) {
                checkPermissionAndCaptureImage();
            } else {
                showSnackbar(getString(R.string.max_3_allowed));
            }
        });
    }

    public void checkPermissionAndCaptureImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(requireContext());
                    alert.setMessage(R.string.storage_required)
                            .setPositiveButton(R.string.allow, (DialogInterface dialogInterface, int i) -> requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2))
                            .setNegativeButton(android.R.string.cancel, (dialogInterface, i) -> dialogInterface.dismiss()).show();
                } else if (permissionStatus.getBoolean(Manifest.permission.WRITE_EXTERNAL_STORAGE, false)) {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", requireActivity().getPackageName(), null);
                    intent.setData(uri);
                    startActivityForResult(intent, 2);
                } else {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
                }
                SharedPreferences.Editor editor = permissionStatus.edit();
                editor.putBoolean(Manifest.permission.WRITE_EXTERNAL_STORAGE, true);
                editor.apply();
            }
        } else {
            dispatchTakePictureIntent();
        }
    }

    private void checkPermissionAndGetLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationApi.onStart();
            } else {
                requestPermissions();
            }
        } else {
            locationApi.onStart();
        }
    }

    private void onSubmitClick() {
        binding.getRoot().clearFocus();
        String name = Objects.requireNonNull(binding.layoutName.getEditText()).getText().toString();
        if (name.isEmpty()) {
            binding.layoutName.setError(getString(R.string.field_cant_be_empty));
            return;
        }
        String mobile = Objects.requireNonNull(binding.layoutMobile.getEditText()).getText().toString();
        if (mobile.isEmpty()) {
            binding.layoutMobile.setError(getString(R.string.field_cant_be_empty));
            return;
        } else if (mobile.length() != 10) {
            binding.layoutMobile.setError(getString(R.string.enter_valid_number));
            return;
        }
        String bagsReceived = Objects.requireNonNull(binding.layoutBags.getEditText()).getText().toString();
        if (bagsReceived.isEmpty()) {
            binding.layoutBags.setError(getString(R.string.field_cant_be_empty));
            return;
        }
        if (hasError) {
            return;
        }

        BitmapDrawable bitmapDrawable = (BitmapDrawable) binding.sign.getDrawable();
        if (bitmapDrawable == null) {
            Snackbar.make(binding.getRoot(), R.string.provide_signature, Snackbar.LENGTH_SHORT).show();
            return;
        } else {
            Bitmap image = (bitmapDrawable).getBitmap();
            if (image == null) {
                Snackbar.make(binding.getRoot(), R.string.provide_signature, Snackbar.LENGTH_SHORT).show();
                return;
            }
        }
        checkPermissionAndGetLocation();
    }


    @RequiresApi(Build.VERSION_CODES.M)
    private void requestPermissions() {
        boolean shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION);

        if (shouldProvideRationale) {
            showSnackbar(R.string.permission_rationale,
                    android.R.string.ok, v ->
                            requestPermissions(
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    102));
        } else if (permissionStatus.getBoolean(Manifest.permission.ACCESS_FINE_LOCATION, false)) {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", requireActivity().getPackageName(), null);
            intent.setData(uri);
            startActivityForResult(intent, 102);
        } else {
            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    102);
        }

        SharedPreferences.Editor editor = permissionStatus.edit();
        editor.putBoolean(Manifest.permission.ACCESS_FINE_LOCATION, true);
        editor.apply();
    }

    private void showSnackbar(int mainTextStringId, int actionStringId,
                              View.OnClickListener listener) {
        Snackbar.make(binding.getRoot(), getString(mainTextStringId), Snackbar.LENGTH_INDEFINITE).setAction(getString(actionStringId), listener).show();
    }

    private void showSnackbar(String mainTextString) {
        Snackbar.make(binding.getRoot(), mainTextString, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onSignatureCaptured(Bitmap bitmap, String fileUri) {
        viewModel.getSignature().setValue(bitmap);
    }

    @Override
    public void onLocationChange(Location loc) {
        if (getArguments() != null) {
            Invoice invoice = getArguments().getParcelable(AppConstant.INVOICE);
            Receiver receiver = Objects.requireNonNull(getArguments()).getParcelable(AppConstant.RECEIVER);
            int id = -1;
            if (receiver != null && receiver.getId() > 0) {
                id = receiver.getId();
            } else if (receiverId > 0) {
                id = receiverId;
            }
            viewModel.saveReceiver(id, invoice.getInvoiceNumber(), invoice.getLoadType().equalsIgnoreCase("standard") ? 0 : 1, loc, isFinalSubmit);
        }
        locationApi.onStop();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 102 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            locationApi.onStart();
        } else if (requestCode == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            dispatchTakePictureIntent();
        }
    }

    static final int REQUEST_TAKE_PHOTO = 1;

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(requireContext().getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            Invoice invoice = Objects.requireNonNull(getArguments()).getParcelable(AppConstant.INVOICE);
            viewModel.uploadImage(bitmap, invoice);
        } else if (requestCode == 102 && resultCode == RESULT_OK) {
            locationApi.onStart();
        }
    }

    @Override
    public void onImageDeleteClick(UploadDocumentEntity entity) {
        viewModel.deleteUploadedImage(entity);
    }
}