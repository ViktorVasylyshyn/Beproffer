package com.beproffer.beproffer.presentation.binding;

import android.widget.ImageView;
import android.widget.TextView;

import androidx.databinding.BindingAdapter;

import com.beproffer.beproffer.R;
import com.beproffer.beproffer.util.Const;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

/*may be public for binding*/
public class AppBindingAdapter {

    private AppBindingAdapter() {
    }

    /*user profile image loading with circle crop */
    @BindingAdapter("loadProfileImageFromDb")
    public static void loadProfileImage(ImageView imageView, String url) {
        final RequestOptions options = new RequestOptions()
                .placeholder(R.drawable.profile_image_ph)
                .apply(RequestOptions.circleCropTransform())
                .diskCacheStrategy(DiskCacheStrategy.ALL);

        Glide.with(imageView.getContext())
                .load(url)
                .apply(options)
                .into(imageView);
    }

    /*service image loading with rectangle crop*/
    @BindingAdapter("loadServiceImageFromDb")
    public static void loadServiceImage(ImageView imageView, String url) {
        final RequestOptions options = new RequestOptions()
                .placeholder(R.drawable.service_image_ph)
                .apply(RequestOptions.centerCropTransform())
                .diskCacheStrategy(DiskCacheStrategy.ALL);

        Glide.with(imageView.getContext())
                .load(url)
                .apply(options)
                .into(imageView);
    }

    @BindingAdapter({"setGenderImage"})
    public static void showGenderImage(ImageView view, String gender) {
        int resId;
        if (gender == null) {
            resId = R.drawable.ic_question_sign;
        } else {
            switch (gender) {
                case Const.MALE:
                    resId = R.drawable.ic_gender_sign_male;
                    break;
                case Const.FEMALE:
                    resId = R.drawable.ic_gender_sign_female;
                    break;
                case Const.BOTHGEND:
                    resId = R.drawable.ic_gender_sign_any;
                    break;
                default:
                    throw new IllegalArgumentException(Const.UNKNSTAT);
            }
        }
        view.setImageResource(resId);
    }

    @BindingAdapter({"setSpecialistTypeTitle"})
    public static void showSpecialistTypeTitle(TextView view, String specialistType) {
        int textResId;
        if (specialistType != null) {
            switch (specialistType) {
                case Const.HAI:
                    textResId = R.string.title_specialization_hai;
                    break;
                case Const.NAI:
                    textResId = R.string.title_specialization_nai;
                    break;
                case Const.MAK:
                    textResId = R.string.title_specialization_mak;
                    break;
                case Const.TAT:
                    textResId = R.string.title_specialization_tat;
                    break;
                default:
                    throw new IllegalArgumentException(Const.UNKNSTAT);
            }
            view.setText(textResId);
        }
    }

    @BindingAdapter({"setSpecialistTypeImage"})
    public static void showSpecialistTypeImage(ImageView view, String specialistType) {
        int imageResId;
        if (specialistType != null) {
            switch (specialistType) {
                case Const.HAI:
                    imageResId = R.drawable.ic_haircut;
                    break;
                case Const.NAI:
                    imageResId = R.drawable.ic_nails;
                    break;
                case Const.MAK:
                    imageResId = R.drawable.ic_makeup;
                    break;
                case Const.TAT:
                    imageResId = R.drawable.ic_tattoo;
                    break;
                default:
                    return;
            }
            view.setImageResource(imageResId);
        }
    }

    @BindingAdapter({"setTypeImage"})
    public static void showTypeImage(ImageView view, String type) {
        int resId;
        if (type == null) {
            resId = R.drawable.ic_question_sign;
        } else {
            switch (type) {
                case Const.HAI:
                    resId = R.drawable.ic_haircut;
                    break;
                case Const.NAI:
                    resId = R.drawable.ic_nails;
                    break;
                case Const.MAK:
                    resId = R.drawable.ic_makeup;
                    break;
                case Const.TAT:
                    resId = R.drawable.ic_tattoo;
                    break;
                default:
                    throw new IllegalArgumentException(Const.UNKNSTAT);
            }
        }
        view.setImageResource(resId);
    }

    @BindingAdapter({"setTypeTitle"})
    public static void showTypeTitle(TextView view, String subtype) {
        int resId;
        if (subtype == null) {
            resId = R.string.title_service_type;
        } else {
            switch (subtype) {
                /*hair*/
                case Const.HAICUT:
                    resId = R.string.hair_title_haircut;
                    break;
                case Const.HAIEXT:
                    resId = R.string.hair_title_extension;
                    break;
                case Const.HAISTY:
                    resId = R.string.hair_title_styling;
                    break;
                case Const.HAIDYE:
                    resId = R.string.hair_title_dyeing;
                    break;
                case Const.HAIBRA:
                    resId = R.string.hair_title_braids;
                    break;
                case Const.HAIOTH:
                    resId = R.string.hair_title_other;
                    break;
                /*nails*/
                case Const.NAIMAN:
                    resId = R.string.nails_title_manicure;
                    break;
                case Const.NAIPED:
                    resId = R.string.nails_title_pedicure;
                    break;
                case Const.NAIEXT:
                    resId = R.string.nails_title_extension;
                    break;
                case Const.NAIOTH:
                    resId = R.string.nails_title_other;
                    break;
                /*makeup*/
                case Const.MAKMAK:
                    resId = R.string.makeup_title_makeup;
                    break;
                case Const.MAKPER:
                    resId = R.string.makeup_title_permanent_makeup;
                    break;
                case Const.MAKEEX:
                    resId = R.string.makeup_title_eyelash_extension;
                    break;
                case Const.MAKEST:
                    resId = R.string.makeup_title_eyebrow_styling;
                    break;
                case Const.MAKOTH:
                    resId = R.string.makeup_title_other;
                    break;
                /*tattoo*/
                case Const.TATTAT:
                    resId = R.string.tattoo_title_tattoo;
                    break;
                case Const.TATOTH:
                    resId = R.string.tattoo_title_other;
                    break;
                default:
                    throw new IllegalArgumentException(Const.UNKNSTAT);
            }
        }
        view.setText(resId);
    }

    @BindingAdapter({"setDurationImage"})
    public static void showDurationImage(ImageView view, String duration) {
        int resId;
        if (duration == null) {
            resId = R.drawable.ic_question_sign;
        } else {
            switch (duration) {
                case Const.MIN30:
                    resId = R.drawable.ic_duration_30_min;
                    break;
                case Const.MIN45:
                    resId = R.drawable.ic_duration_45_min;
                    break;
                case Const.MIN60:
                    resId = R.drawable.ic_duration_60_min;
                    break;
                case Const.MIN90:
                    resId = R.drawable.ic_duration_90_min;
                    break;
                case Const.MIN120:
                    resId = R.drawable.ic_duration_120_min;
                    break;
                default:
                    throw new IllegalArgumentException(Const.UNKNSTAT);
            }
        }
        view.setImageResource(resId);
    }

    /*загрузка изображения специалиста, имея его id*/
    @BindingAdapter("loadSpecialistsProfileImage")
    public static void loadSpecialistsProfileImage(ImageView imageView, String url) {
        if (url == null)
            return;
        final RequestOptions options = new RequestOptions()
                .placeholder(R.drawable.profile_image_ph)
                .apply(RequestOptions.circleCropTransform())
                .diskCacheStrategy(DiskCacheStrategy.ALL);

        Glide.with(imageView.getContext())
                .load(url)
                .apply(options)
                .into(imageView);

    }

    @BindingAdapter("loadSpecialistsName")
    public static void loadSpecialistsName(TextView textView, String name) {
        if (name == null)
            return;
        textView.setText(name);

    }
}
