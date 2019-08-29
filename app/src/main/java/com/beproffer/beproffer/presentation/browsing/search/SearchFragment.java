package com.beproffer.beproffer.presentation.browsing.search;

import androidx.lifecycle.ViewModelProviders;
import android.content.SharedPreferences;
import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.beproffer.beproffer.R;
import com.beproffer.beproffer.databinding.SearchFragmentLayoutBinding;
import com.beproffer.beproffer.presentation.base.BaseFragment;
import com.beproffer.beproffer.presentation.browsing.BrowsingViewModel;
import com.beproffer.beproffer.util.Const;
import com.beproffer.beproffer.util.DefineServiceType;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public class SearchFragment extends BaseFragment {

    private SearchFragmentLayoutBinding mBinding;

    private SearchFragmentCallback mCallback = new SearchFragmentCallback() {
        @Override
        public void onServiceTypeIconClicked(View view) {
            defineServiceType(view);
        }

        @Override
        public void onGenderClicked(View view) {
            handleGenderClick(view);
        }

        @Override
        public void onApplyClicked() {
            if (mGender == null || mTypeMap == null) {
                mBinding.searchFragmentApply.setClickable(false);
                Toast.makeText(requireContext(), R.string.toast_define_search_params, Toast.LENGTH_SHORT).show();
                Handler handler = new Handler();
                handler.postDelayed(() -> mBinding.searchFragmentApply.setClickable(true), Const.COOLDOWNDUR_SHORT);
                return;
            }
            applySearchRequest();
        }

        @Override
        public void onDenyClicked() {
            popBackStack();
        }

        @Override
        public void onFreeAreaClicked() {
            /*должен быть пустой, чтобы не кликало на BrowsingFragment, так как эти фрагменты
            * одновременно добавлены в контейнер*/
        }
    };

    private Map<String, String> mTypeMap;

    private String mGender;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle saveInstantState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.search_fragment_layout, container, false);
        mBinding.setLifecycleOwner(this);
        mBinding.setFragmentCallback(mCallback);

        return mBinding.getRoot();
    }

    private void defineServiceType(View view) {
        int requiredMenuRes;
        switch (view.getId()) {
            case R.id.search_fragment_haircut_icon:
                requiredMenuRes = R.menu.menu_hair_services;
                break;
            case R.id.search_fragment_nails_icon:
                requiredMenuRes = R.menu.menu_nails_services;
                break;
            case R.id.search_fragment_makeup_icon:
                requiredMenuRes = R.menu.menu_makeup_services;
                break;
            case R.id.search_fragment_tattoo_icon:
                requiredMenuRes = R.menu.menu_tattoo_services;
                break;
            case R.id.search_fragment_barber_icon:
                requiredMenuRes = R.menu.menu_barber_services;
                break;
            case R.id.search_fragment_fitness_icon:
                requiredMenuRes = R.menu.menu_fitness_services;
                break;
            default:
                throw new IllegalArgumentException(Const.UNKNSTAT);
        }

        PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
        popupMenu.getMenuInflater().inflate(requiredMenuRes, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(menuItem -> {
            mTypeMap = new DefineServiceType(view.getContext()).setRequest(menuItem);

            if (mTypeMap == null || !mTypeMap.containsKey(Const.SERVTYPE) ||
                    !mTypeMap.containsKey(Const.SERVSBTP) || !mTypeMap.containsKey(Const.SERVTITL)) {
                Toast.makeText(requireContext(), R.string.toast_error_has_occurred, Toast.LENGTH_SHORT).show();
            } else {
                mBinding.searchFragmentTypeTitle.setText(mTypeMap.get(Const.SERVTITL));

                mBinding.searchFragmentHaircutIcon.setBackgroundResource(R.drawable.button_grey_stroke_rectangle);
                mBinding.searchFragmentNailsIcon.setBackgroundResource(R.drawable.button_grey_stroke_rectangle);
                mBinding.searchFragmentMakeupIcon.setBackgroundResource(R.drawable.button_grey_stroke_rectangle);
                mBinding.searchFragmentTattooIcon.setBackgroundResource(R.drawable.button_grey_stroke_rectangle);
                mBinding.searchFragmentBarberIcon.setBackgroundResource(R.drawable.button_grey_stroke_rectangle);
                mBinding.searchFragmentFitnessIcon.setBackgroundResource(R.drawable.button_grey_stroke_rectangle);

                view.setBackground(getResources().getDrawable(R.drawable.button_background_green_stroke_rectangle));
                /*this map have service's type, subtype and title*/
            }
            return true;
        });
        popupMenu.show();
    }

    private void handleGenderClick(View view) {
        switch (view.getId()) {
            case R.id.search_fragment_male_text_view:
                mGender = Const.MALE;
                break;
            case R.id.search_fragment_female_text_view:
                mGender = Const.FEMALE;
                break;
            case R.id.search_fragment_both_text_view:
                mGender = Const.BOTHGEND;
                break;
            default:
                throw new IllegalArgumentException(Const.UNKNSTAT);
        }
        mBinding.searchFragmentMaleTextView.setBackgroundResource(R.drawable.background_transparent);
        mBinding.searchFragmentFemaleTextView.setBackgroundResource(R.drawable.background_transparent);
        mBinding.searchFragmentBothTextView.setBackgroundResource(R.drawable.background_transparent);

        view.setBackground(getResources().getDrawable(R.drawable.button_background_green_stroke_rectangle));
    }

    private void applySearchRequest() {
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
            ViewModelProviders.of(requireActivity()).get(BrowsingViewModel.class).refreshAdapter();
            popBackStack();
        } else {
            Toast.makeText(requireContext(), R.string.toast_error_has_occurred, Toast.LENGTH_SHORT).show();
        }
    }
}
