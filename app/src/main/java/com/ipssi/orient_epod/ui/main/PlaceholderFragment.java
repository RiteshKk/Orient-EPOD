package com.ipssi.orient_epod.ui.main;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.snackbar.Snackbar;
import com.ipssi.orient_epod.R;
import com.ipssi.orient_epod.adapter.ImageAdapter;
import com.ipssi.orient_epod.capturesignature.OnSignedCaptureListener;
import com.ipssi.orient_epod.capturesignature.SignatureDialogFragment;
import com.ipssi.orient_epod.databinding.FragmentTabbedBinding;
import com.ipssi.orient_epod.location.LocationAPI;
import com.ipssi.orient_epod.location.OnLocationChangeCallBack;
import com.ipssi.orient_epod.model.Receiver;

import static android.Manifest.permission.CAMERA;
import static android.app.Activity.RESULT_OK;

/**
 * A placeholder fragment containing a simple view.
 */
public class PlaceholderFragment extends Fragment implements OnSignedCaptureListener, OnLocationChangeCallBack {

    private FragmentTabbedBinding binding;
    private LocationAPI locationApi;
    private ImageAdapter adapter;

    public static PlaceholderFragment newInstance(Receiver receiver, boolean isEditable) {
        PlaceholderFragment fragment = new PlaceholderFragment();
       /* Bundle bundle = new Bundle();
        bundle.putParcelable(AppConstant.MODEL, receiver);
        bundle.putBoolean(AppConstant.IS_EDITABLE, isEditable);
        fragment.setArguments(bundle);*/
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
//        Receiver model = Objects.requireNonNull(getArguments()).getParcelable(AppConstant.MODEL);
//        boolean isEditable = Objects.requireNonNull(getArguments()).getBoolean(AppConstant.IS_EDITABLE);
        /*binding.setIsEditable(isEditable);
        binding.setModel(model);*/

        locationApi = new LocationAPI(requireActivity(), this);

        binding.imageList.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        adapter = new ImageAdapter();
        binding.imageList.setAdapter(adapter);
        binding.sign.setOnClickListener(v -> {
            SignatureDialogFragment dialogFragment = new SignatureDialogFragment(PlaceholderFragment.this);
            dialogFragment.show(getChildFragmentManager(), "signature");
        });

        binding.btnCaptureLocation.setOnClickListener(v -> {

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
        });

        binding.btnUploadImage.setOnClickListener(v -> checkPermissionAndCaptureImage());
    }


    @RequiresApi(Build.VERSION_CODES.M)
    private void requestPermissions() {
        boolean shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION);

        if (shouldProvideRationale) {
            showSnackbar(R.string.permission_rationale,
                android.R.string.ok, v->
                requestPermissions(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    102));
        } else {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", requireActivity().getPackageName(), null);
            intent.setData(uri);
            startActivityForResult(intent, 102);
        }
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
        Snackbar.make(binding.getRoot(), "Lat : " + loc.getLatitude() + " ,Lng : " + loc.getLongitude(), Snackbar.LENGTH_SHORT).show();
        locationApi.onStop();
    }

    public void checkPermissionAndCaptureImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(requireContext(), CAMERA) == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), CAMERA)) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(requireContext());
                    alert.setMessage("Camera permission required to capture image")
                        .setPositiveButton("Allow", (DialogInterface dialogInterface, int i) -> requestPermissions(new String[]{CAMERA}, 101))
                        .setNegativeButton("cancel", (dialogInterface, i) -> dialogInterface.dismiss()).show();
                } else {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", requireActivity().getPackageName(), null);
                    intent.setData(uri);
                    startActivityForResult(intent, 101);
                }
            }
        } else {
            dispatchTakePictureIntent();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            dispatchTakePictureIntent();
        }else if(requestCode == 102 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
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
        } else if (requestCode == 101 && resultCode == RESULT_OK) {
            checkPermissionAndCaptureImage();
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