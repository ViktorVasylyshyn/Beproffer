package com.beproffer.beproffer.presentation.swimg.search_sheet;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.beproffer.beproffer.R;
import com.beproffer.beproffer.databinding.SearchSheetLayoutBinding;
import com.beproffer.beproffer.presentation.swimg.SwipeImagesViewModel;
import com.beproffer.beproffer.util.Const;
import com.beproffer.beproffer.util.DefineServiceType;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public class SearchSheetDialog extends BottomSheetDialogFragment {

//    private SearchSheetListener mListener;

    private SearchSheetLayoutBinding mBinding;

    private Map<String, String> mTypeMap;

    private String mGender;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle saveInstantState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.search_sheet_layout, container, false);
        mBinding.setLifecycleOwner(this);
        /*пока что так, потому что хуй его знает, как его по другому сделать :(*/
        mBinding.sslHaircutIcon.setOnClickListener(v -> defineServiceType(mBinding.sslHaircutIcon));
        mBinding.sslNailsIcon.setOnClickListener(v -> defineServiceType(mBinding.sslNailsIcon));
        mBinding.sslMakeupIcon.setOnClickListener(v -> defineServiceType(mBinding.sslMakeupIcon));
        mBinding.sslTattooPiercingIcon.setOnClickListener(v -> defineServiceType(mBinding.sslTattooPiercingIcon));
        mBinding.sslBarberIcon.setOnClickListener(v -> defineServiceType(mBinding.sslBarberIcon));
        mBinding.sslFitnessIcon.setOnClickListener(v -> defineServiceType(mBinding.sslFitnessIcon));

        mBinding.sslMaleTextView.setOnClickListener(v -> handleGenderClick(mBinding.sslMaleTextView));
        mBinding.sslFemaleTextView.setOnClickListener(v -> handleGenderClick(mBinding.sslFemaleTextView));
        mBinding.sslBothTextView.setOnClickListener(v -> handleGenderClick(mBinding.sslBothTextView));

        mBinding.sslApply.setOnClickListener(v -> {
            if (mGender == null || mTypeMap == null) {
                Toast.makeText(requireContext(), R.string.toast_define_search_params, Toast.LENGTH_SHORT).show();
                return;
            }
            applySearchRequest();
        });

        mBinding.sslDeny.setOnClickListener(v -> dismiss());

        return mBinding.getRoot();
    }

//    public interface SearchSheetListener {
//        void apply();
//    }

    private void defineServiceType(ImageView view) {
        int requiredMenuRes = 0;
        switch (view.getId()) {
            case R.id.ssl_haircut_icon:
                requiredMenuRes = R.menu.nenu_hair_services;
                break;
            case R.id.ssl_nails_icon:
                requiredMenuRes = R.menu.nenu_nails_services;
                break;
            case R.id.ssl_makeup_icon:
                requiredMenuRes = R.menu.nenu_makeup_services;
                break;
            case R.id.ssl_tattoo_piercing_icon:
                requiredMenuRes = R.menu.nenu_tattoo_services;
                break;
            case R.id.ssl_barber_icon:
                requiredMenuRes = R.menu.nenu_barber_services;
                break;
            case R.id.ssl_fitness_icon:
                requiredMenuRes = R.menu.nenu_fitness_services;
                break;
            default:
                throw new IllegalArgumentException(Const.UNKNSTAT);
        }

        PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
        popupMenu.getMenuInflater().inflate(requiredMenuRes, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(menuItem -> {
            mTypeMap = new DefineServiceType(view.getContext()).setRequest(menuItem);

            if (mTypeMap == null || !mTypeMap.containsKey(Const.SERVTYPE) ||
                    !mTypeMap.containsKey(Const.SERVSBTP)  || !mTypeMap.containsKey(Const.SERVTITL)) {
                Toast.makeText(requireContext(), R.string.toast_error_has_occurred, Toast.LENGTH_SHORT).show();
            } else {
                mBinding.sslTypeTitle.setText(mTypeMap.get(Const.SERVTITL));

                mBinding.sslHaircutIcon.setBackgroundResource(R.drawable.button_background_grey_stroke_rectangle);
                mBinding.sslNailsIcon.setBackgroundResource(R.drawable.button_background_grey_stroke_rectangle);
                mBinding.sslMakeupIcon.setBackgroundResource(R.drawable.button_background_grey_stroke_rectangle);
                mBinding.sslTattooPiercingIcon.setBackgroundResource(R.drawable.button_background_grey_stroke_rectangle);
                mBinding.sslBarberIcon.setBackgroundResource(R.drawable.button_background_grey_stroke_rectangle);
                mBinding.sslFitnessIcon.setBackgroundResource(R.drawable.button_background_grey_stroke_rectangle);

                view.setBackground(getResources().getDrawable(R.drawable.button_background_green_stroke_rectangle));
                /*this map have service's type, subtype and title*/
            }
            return true;
        });
        popupMenu.show();
    }

    public void handleGenderClick(View view) {
        switch (view.getId()) {
            case R.id.ssl_male_text_view:
                mGender = Const.MALE;
                break;
            case R.id.ssl_female_text_view:
                mGender = Const.FEMALE;
                break;
            case R.id.ssl_both_text_view:
                mGender = Const.ALLGEND;
                break;
            default:
                throw new IllegalArgumentException(Const.UNKNSTAT);
        }
        mBinding.sslMaleTextView.setBackgroundResource(R.drawable.background_transparent);
        mBinding.sslFemaleTextView.setBackgroundResource(R.drawable.background_transparent);
        mBinding.sslBothTextView.setBackgroundResource(R.drawable.background_transparent);

        view.setBackground(getResources().getDrawable(R.drawable.button_background_green_stroke_rectangle));
    }

    public void applySearchRequest() {
        if (mTypeMap != null && mGender != null) {

            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                SharedPreferences.Editor editor = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid(), MODE_PRIVATE).edit();
                editor.putString(Const.GENDER, mGender);
                editor.putString(Const.SERVTYPE, mTypeMap.get(Const.SERVTYPE));
                editor.putString(Const.SERVSBTP, mTypeMap.get(Const.SERVSBTP));
                editor.apply();
            } else {
                SharedPreferences.Editor editor = requireActivity().getSharedPreferences(Const.UNKNOWN_USER_REQUEST, MODE_PRIVATE).edit();
                editor.putString(Const.GENDER, mGender);
                editor.putString(Const.SERVTYPE, mTypeMap.get(Const.SERVTYPE));
                editor.putString(Const.SERVSBTP, mTypeMap.get(Const.SERVSBTP));
                editor.apply();
            }

            ViewModelProviders.of(requireActivity()).get(SwipeImagesViewModel.class).refreshItems();

            dismiss();
        } else {
            Toast.makeText(requireContext(), R.string.toast_error_has_occurred, Toast.LENGTH_SHORT).show();
        }
    }

//    @Override
//    public void onAttach(Context context) {
//        super.onAttach(context);
//
//        try {
//            mListener = (SearchSheetListener) context;
//        } catch (ClassCastException e) {
//            throw new ClassCastException(context.toString() + "must implement SearchSheetListener");
//        }
//    }

}
