package com.beproffer.beproffer.presentation.specstorage.edit;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.beproffer.beproffer.R;
import com.beproffer.beproffer.data.firebase.SpecialistStorageSaveImage;
import com.beproffer.beproffer.data.models.StorageImageItem;
import com.beproffer.beproffer.databinding.SpecialistStorageEditFragmentBinding;
import com.beproffer.beproffer.presentation.base.BaseFragment;
import com.beproffer.beproffer.presentation.specstorage.SpecialistStorageImageViewModel;
import com.beproffer.beproffer.util.Const;
import com.crashlytics.android.Crashlytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import static android.content.Context.INPUT_METHOD_SERVICE;

public class SpecialistStorageEditFragment extends BaseFragment {

    private SpecialistStorageEditFragmentViewModel mViewModel;

    private SpecialistStorageEditFragmentBinding mBinding;
    private NavController navController;
    private StorageImageItem mImageItem;
    private Uri mResultUri;

    private String mUserId;

    private SpecialistStorageEditFragmentCallback mCallback = new SpecialistStorageEditFragmentCallback() {
        @Override
        public void setImage() {
            setServiceImage();
        }

        @Override
        public void confirmImageData() {
            checkServiceImageData();
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(
                inflater, R.layout.specialist_storage_edit_fragment, container, false);
        mBinding.setLifecycleOwner(this);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mBinding.setShowProgress(mShowProgress);
        showProgress(true);
        mViewModel = ViewModelProviders.of(requireActivity()).get(SpecialistStorageEditFragmentViewModel.class);
        mBinding.setViewModel(mViewModel);
        mBinding.setFragmentCallback(mCallback);

        try {
            mUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } catch (Exception e) {
            showToast(R.string.toast_error_has_occurred);
        }

        navController = Navigation.findNavController(requireActivity(), R.id.navigation_fragment_container);

        obtainStorageImageItem();
    }

    private void obtainStorageImageItem() {
        mViewModel.getStorageImageItem().observe(this, item -> {
            mImageItem = item;
            showProgress(false);
            mBinding.setItem(mImageItem);
        });
    }

    private void checkServiceImageData() {
        if (mImageItem.getUrl() == null && mResultUri == null) {
            showToast(R.string.toast_select_image);
            return;
        }
        if (mImageItem.getSubtype() == null) {
            showToast(R.string.toast_select_type);
            return;
        }
        if (mImageItem.getGender() == null) {
            showToast(R.string.toast_determine_gender);
            return;
        }
        if (mImageItem.getDuration() == null) {
            showToast(R.string.toast_select_duration);
            return;
        }
        if (mBinding.specialistStorageEditPrice.getText().toString().isEmpty() && mImageItem.getPrice() == null) {
            showToast(R.string.toast_enter_price);
            return;
        }
        if (mBinding.specialistStorageEditDescription.getText().toString().isEmpty() && mImageItem.getDescription() == null) {
            showToast(R.string.toast_enter_description);
            return;
        }
        saveNewImageData();
    }

    public void hideSoftKeyboard(Activity activity) {
        if(activity.getCurrentFocus()!=null) {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }
    }

    private void saveNewImageData() {
        showProgress(true);

        if (mResultUri != null) {
            StorageReference filepath = FirebaseStorage.getInstance().getReference()
                    .child(Const.SERV)
                    .child(mUserId)
                    .child(mImageItem.getKey());
            Activity activity = requireActivity();
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(activity.getApplication().getContentResolver(), mResultUri);
            } catch (IOException e) {
                Crashlytics.getInstance().crash();
                showToast(R.string.toast_error_has_occurred);
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
            } catch (NullPointerException e) {
                Crashlytics.getInstance().crash();
                showToast(R.string.toast_error_has_occurred);
            }
            byte[] data = baos.toByteArray();
            UploadTask uploadTask = filepath.putBytes(data);
            uploadTask.addOnFailureListener(e -> showToast(R.string.toast_error_image_dbkey));
            uploadTask.addOnSuccessListener(taskSnapshot -> filepath.getDownloadUrl()
                    .addOnSuccessListener(url -> completeSaving(url.toString())));
        } else {
            completeSaving(null);
        }
    }

    private void completeSaving(@Nullable String url) {
        if (url != null) {
            mImageItem.setUrl(url);
        }
        if (mImageItem.getUid() == null) {
            mImageItem.setUid(mUserId);
        }
        new SpecialistStorageSaveImage().saveImageData(mUserId, mImageItem, mShowProgress);
        hideSoftKeyboard(requireActivity());
        ViewModelProviders.of(requireActivity()).get(SpecialistStorageImageViewModel.class).addSingleStorageImageItem(mImageItem);
        navController.popBackStack();
    }

    private void setServiceImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, Const.REQUEST_CODE_1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == Const.REQUEST_CODE_1) {
            try {
                mResultUri = data.getData();
                if (mResultUri != null) {
                    mImageItem.setUrl(mResultUri.toString());
                }
            } catch (NullPointerException e) {
                Crashlytics.getInstance().crash();
                showToast(R.string.toast_error_has_occurred);
            }
            mBinding.setItem(mImageItem);
        }
    }
}
