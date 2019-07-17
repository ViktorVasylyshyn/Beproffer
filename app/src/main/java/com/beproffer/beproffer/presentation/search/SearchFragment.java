package com.beproffer.beproffer.presentation.search;

import android.arch.lifecycle.ViewModelProviders;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.beproffer.beproffer.R;
import com.beproffer.beproffer.databinding.SearchFragmentBinding;
import com.beproffer.beproffer.presentation.MainActivity;
import com.beproffer.beproffer.presentation.base.BaseFragment;
import com.beproffer.beproffer.util.Const;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public class SearchFragment extends BaseFragment {

    private String mRequestGender = null;

    private Map<String, String> mSearchRequestMap;

    private SearchFragmentBinding mBinding;

    private SearchFragmentCallback mCallback = this::applySearchRequest;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.search_fragment, container, false);
        mBinding.setLifecycleOwner(this);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        SearchFragmentViewModel mSearchFragmentViewModel = ViewModelProviders.of(requireActivity()).get(SearchFragmentViewModel.class);
        mBinding.setSearchFragmentViewModel(mSearchFragmentViewModel);
        mBinding.setFragmentCallback(mCallback);

        mSearchFragmentViewModel.getSearchRequest().observe(getViewLifecycleOwner(), request -> {
            mSearchRequestMap = request;
            updateImageService();
        });
        mSearchFragmentViewModel.getSearchRequestGender().observe(getViewLifecycleOwner(), gend -> {
            mRequestGender = gend;
            setGender();
        });
    }

    private void updateImageService() {
        int imageId = 0;
        try {
            switch (mSearchRequestMap.get(Const.SERVTYPE)) { /*когда все будет работать попробовавть заменить ворнинг на то что он предлагает*/
                case Const.HAI:
                    imageId = R.drawable.ic_haircut;
                    break;
                case Const.NAI:
                    imageId = R.drawable.ic_nails;
                    break;
                case Const.MAK:
                    imageId = R.drawable.ic_makeup;
                    break;
                case Const.TAT:
                    imageId = R.drawable.ic_tattoo_piercing;
                    break;
                case Const.BAR:
                    imageId = R.drawable.ic_barbershop;
                    break;
                case Const.FIT:
                    imageId = R.drawable.ic_fitness;
                    break;
                default:
                    throw new IllegalArgumentException("unknown service's type");
            }
            mBinding.searchTypeImage.setImageResource(imageId);
            mBinding.searchTypeTitle.setText(mSearchRequestMap.get(Const.SERVTITL));

        } catch (NullPointerException e) {
            showToast(R.string.toast_error_has_occurred);
        }
    }


    public void setGender() {
        if (mRequestGender != null) {
            int titleId = 0;
            int imageId = 0;
            switch (mRequestGender) {
                case Const.MALE:
                    titleId = R.string.title_gender_male;
                    imageId = R.drawable.ic_gender_sign_male;
                    break;
                case Const.FEMALE:
                    titleId = R.string.title_gender_female;
                    imageId = R.drawable.ic_gender_sign_female;
                    break;
                case Const.ALLGEND:
                    titleId = R.string.title_gender_all_genders;
                    imageId = R.drawable.ic_gender_sign_any;
                    break;
                default:
                    throw new IllegalArgumentException(Const.ERROR);

            }
            mBinding.searchGenderImage.setImageResource(imageId);
            mBinding.searchGenderTitle.setText(titleId);
        }
    }

    public void applySearchRequest() {
        if (mSearchRequestMap != null) {
            mSearchRequestMap.put(Const.GENDER, mRequestGender);

            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                SharedPreferences.Editor editor = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid(), MODE_PRIVATE).edit();
                editor.putString(Const.GENDER, mRequestGender);
                editor.putString(Const.SERVTYPE, mSearchRequestMap.get(Const.SERVTYPE));
                editor.putString(Const.SERVSBTP, mSearchRequestMap.get(Const.SERVSBTP));
                editor.apply();
            } else {
                SharedPreferences.Editor editor = requireActivity().getSharedPreferences(Const.UNKNOWN_USER_REQUEST, MODE_PRIVATE).edit();
                editor.putString(Const.GENDER, mRequestGender);
                editor.putString(Const.SERVTYPE, mSearchRequestMap.get(Const.SERVTYPE));
                editor.putString(Const.SERVSBTP, mSearchRequestMap.get(Const.SERVSBTP));
                editor.apply();
            }

            BottomNavigationView bottomNavigationView = requireActivity().findViewById(R.id.bottomNavigationView);
            bottomNavigationView.getMenu().findItem(R.id.bnm_images_gallery).setChecked(true);

            ((MainActivity) requireActivity()).performNavigation(R.id.action_global_swipeImageFragment, null);
        } else {
            showToast(R.string.toast_define_search_params);
        }
    }
}
