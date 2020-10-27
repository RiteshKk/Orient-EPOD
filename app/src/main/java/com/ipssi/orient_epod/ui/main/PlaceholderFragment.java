package com.ipssi.orient_epod.ui.main;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.snackbar.Snackbar;
import com.ipssi.orient_epod.R;
import com.ipssi.orient_epod.adapter.ImageAdapter;
import com.ipssi.orient_epod.capturesignature.OnSignedCaptureListener;
import com.ipssi.orient_epod.capturesignature.SignatureDialogFragment;
import com.ipssi.orient_epod.databinding.FragmentTabbedBinding;
import com.ipssi.orient_epod.location.LocationAPI;
import com.ipssi.orient_epod.location.OnLocationChangeCallBack;

import java.util.Objects;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

/**
 * A placeholder fragment containing a simple view.
 */
public class PlaceholderFragment extends Fragment implements OnSignedCaptureListener, OnLocationChangeCallBack {

    private FragmentTabbedBinding binding;
    private LocationAPI locationApi;
    private ImageAdapter adapter;
    private SharedPreferences permissionStatus;
    private PageViewModel viewModel;
    private boolean isSubmit = false;

    public static PlaceholderFragment newInstance(String invoiceNumber) {
        Bundle bundle = new Bundle();
        bundle.putString("invoice", invoiceNumber);
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

        viewModel = new ViewModelProvider(this).get(PageViewModel.class);

        locationApi = new LocationAPI(requireActivity(), this);

        handleClickListener();

        setTextWatcher();
        binding.imageList.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        adapter = new ImageAdapter();
        binding.imageList.setAdapter(adapter);
        binding.sign.setOnClickListener(v -> {
            SignatureDialogFragment dialogFragment = new SignatureDialogFragment(PlaceholderFragment.this);
            dialogFragment.show(getChildFragmentManager(), "signature");
        });
    }

    private void setTextWatcher() {
        binding.layoutName.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.layoutName.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
        binding.layoutMobile.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.layoutMobile.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
        binding.layoutBags.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.layoutBags.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
        binding.layoutDamageBags.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.layoutDamageBags.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }

    private void handleClickListener() {
        binding.btnSubmit.setOnClickListener(v -> {
            isSubmit = true;
            binding.getRoot().clearFocus();
            String name = Objects.requireNonNull(binding.layoutName.getEditText()).getText().toString();
            if (name.isEmpty()) {
                binding.layoutName.setError("Please fill name");
                return;
            }
            String mobile = Objects.requireNonNull(binding.layoutMobile.getEditText()).getText().toString();
            if (mobile.isEmpty()) {
                binding.layoutMobile.setError("Please fill number");
                return;
            }
            String bagsReceived = Objects.requireNonNull(binding.layoutBags.getEditText()).getText().toString();
            if (bagsReceived.isEmpty()) {
                binding.layoutBags.setError("Please fill bags count");
                return;
            }
            String shortage = Objects.requireNonNull(binding.layoutDamageBags.getEditText()).getText().toString();
            if (shortage.isEmpty()) {
                binding.layoutDamageBags.setError("Please fill shortage/damages(fill 0 if no shortage/damage)");
                return;
            }

            Bitmap image = ((BitmapDrawable) binding.sign.getDrawable()).getBitmap();
            if (image == null) {
                Snackbar.make(binding.getRoot(), "Please provide Signature", Snackbar.LENGTH_SHORT).show();
                return;
            }
            checkPermissionAndGetLocation();
        });

        viewModel.getSaveReceiverResponse().observe(getViewLifecycleOwner(), saveReceiverResponse -> {
            Snackbar.make(binding.getRoot(), saveReceiverResponse.getMessage(), Snackbar.LENGTH_SHORT).show();
        });

        binding.btnCaptureLocation.setOnClickListener(v -> {
            checkPermissionAndGetLocation();
        });
        binding.btnUploadImage.setOnClickListener(v -> dispatchTakePictureIntent());
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


    @RequiresApi(Build.VERSION_CODES.M)
    private void requestPermissions() {
        boolean shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION);

        if (shouldProvideRationale) {
            showSnackbar(R.string.permission_rationale,
                android.R.string.ok, v ->
                    requestPermissions(
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        102));
        } else if (permissionStatus.getBoolean(Manifest.permission.WRITE_EXTERNAL_STORAGE, false)) {
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
        editor.putBoolean(Manifest.permission.WRITE_EXTERNAL_STORAGE, true);
        editor.apply();
    }

    private void showSnackbar(int mainTextStringId, int actionStringId,
                              View.OnClickListener listener) {
        Snackbar.make(binding.getRoot(), getString(mainTextStringId), Snackbar.LENGTH_INDEFINITE).setAction(getString(actionStringId), listener).show();
    }

    @Override
    public void onSignatureCaptured(Bitmap bitmap, String fileUri) {
        binding.sign.setImageBitmap(bitmap);
    }

    @Override
    public void onLocationChange(Location loc) {
        if (isSubmit) {
            if (getArguments() != null) {
                String invoiceNumber = getArguments().getString("invoice", "");
                viewModel.saveReceiver(binding.layoutName.getEditText().getText().toString(), binding.layoutMobile.getEditText().getText().toString(), invoiceNumber, binding.layoutBags.getEditText().getText().toString(), binding.layoutDamageBags.getEditText().getText().toString(), binding.bagsSpinner.getSelectedItemPosition(), binding.layoutNotes.getEditText().getText().toString(), ((BitmapDrawable) binding.sign.getDrawable()).getBitmap(),loc);
            }
            isSubmit = false;
        } else {
            Snackbar.make(binding.getRoot(), "Lat : " + loc.getLatitude() + " ,Lng : " + loc.getLongitude(), Snackbar.LENGTH_SHORT).show();
        }
        locationApi.onStop();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 102 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            locationApi.onStart();
        }
    }

    static final int REQUEST_TAKE_PHOTO = 1;

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(requireContext().getPackageManager()) != null) {


            // Create the File where the photo should go
          /*  File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(requireContext(),
                    "com.ipssi.driver_opod.fileprovider",
                    photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                }*/
            startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            adapter.setImages(imageBitmap);
        } else if (requestCode == 102 && resultCode == RESULT_OK) {
            locationApi.onStart();
        }
    }

//    String currentPhotoPath;

    /*private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
            imageFileName,  *//* prefix *//*
            ".jpg",         *//* suffix *//*
            storageDir      *//* directory *//*
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }*/


//    private void setPic() {
//        // Get the dimensions of the View
//        int targetW = imageView.getWidth();
//        int targetH = imageView.getHeight();
//
//        // Get the dimensions of the bitmap
//        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
//        bmOptions.inJustDecodeBounds = true;
//
//        BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
//
//        int photoW = bmOptions.outWidth;
//        int photoH = bmOptions.outHeight;
//
//        // Determine how much to scale down the image
//        int scaleFactor = Math.max(1, Math.min(photoW / targetW, photoH / targetH));
//
//        // Decode the image file into a Bitmap sized to fill the View
//        bmOptions.inJustDecodeBounds = false;
//        bmOptions.inSampleSize = scaleFactor;
//        bmOptions.inPurgeable = true;
//
//        Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
//        imageView.setImageBitmap(bitmap);
//    }
}