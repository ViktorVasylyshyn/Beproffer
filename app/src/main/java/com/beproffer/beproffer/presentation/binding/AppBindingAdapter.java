package com.beproffer.beproffer.presentation.binding;

import android.databinding.BindingAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.beproffer.beproffer.R;
import com.beproffer.beproffer.util.Const;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;

public class AppBindingAdapter {

    private AppBindingAdapter() {
    }

    /*user profile image loading with circle crop */
    @BindingAdapter("android:src")
    public static void loadImage(ImageView view, String param) {
        Glide.with(view.getContext())
                .load(param)
                .apply(RequestOptions.circleCropTransform().placeholder(R.drawable.profile_image_place_holder))
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(view);
    }

    /*service image loading with rectangle crop*/
    @BindingAdapter("loadImageFromDb")
    public static void loadProfileImage(ImageView view, String url) {
        /*если картинка в списке будет плохо выгллядеть или не полностью показываться - помудровать с способами трансформации*/
        Glide.with(view.getContext())
                .load(url)
                .apply(RequestOptions.centerCropTransform().placeholder(R.drawable.service_image_rectangle_place_holder))
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(view);
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

    @BindingAdapter({"setGenderTitle"})
    public static void showGenderTitle(TextView view, String gender) {
        int resId;
        if (gender == null) {
            resId = R.string.title_gender_unknown;
        } else {
            switch (gender) {
                case Const.MALE:
                    resId = R.string.title_gender_male;
                    break;
                case Const.FEMALE:
                    resId = R.string.title_gender_female;
                    break;
                case Const.BOTHGEND:
                    resId = R.string.title_gender_all_genders;
                    break;
                default:
                    throw new IllegalArgumentException(Const.UNKNSTAT);
            }
        }
        view.setText(resId);
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
                case Const.BAR:
                    resId = R.drawable.ic_barbershop;
                    break;
                case Const.FIT:
                    resId = R.drawable.ic_fitness;
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
                case Const.HAISTR:
                    resId = R.string.hair_title_straightening;
                    break;
                case Const.HAIDYE:
                    resId = R.string.hair_title_dyeing;
                    break;
                case Const.HAIBRA:
                    resId = R.string.hair_title_braids;
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
                /*tattoo*/
                case Const.TATTAT:
                    resId = R.string.tattoo_title_tattoo;
                    break;
                case Const.TATTEM:
                    resId = R.string.tattoo_title_temporary_tattoo;
                    break;
                /*barber*/
                case Const.BARSTY:
                    resId = R.string.barber_title_barber;
                    break;
                /*fitness*/
                case Const.FITTRA:
                    resId = R.string.fitness_title_trainer;
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

    @BindingAdapter({"setDurationTitle"})
    public static void showDurationTitle(TextView view, String type) {
        int resId;
        if (type == null) {
            resId = R.string.title_duration;
        } else {
            switch (type) {
                case Const.MIN30:
                    resId = R.string.title_duration_30_min;
                    break;
                case Const.MIN45:
                    resId = R.string.title_duration_45_min;
                    break;
                case Const.MIN60:
                    resId = R.string.title_duration_60_min;
                    break;
                case Const.MIN90:
                    resId = R.string.title_duration_90_min;
                    break;
                case Const.MIN120:
                    resId = R.string.title_duration_120_min;
                    break;
                default:
                    throw new IllegalArgumentException(Const.UNKNSTAT);
            }
        }
        view.setText(resId);
    }

    @BindingAdapter({"showUserType"})
    public static void showUserType(TextView view, String type) {
        int resId;
        if (type == null) {
            return;
        }
        switch (type) {
            case Const.CUST:
                resId = R.string.title_customer_type;
                break;
            case Const.SPEC:
                resId = R.string.title_specialist_type;
                break;
            default:
                throw new IllegalArgumentException(Const.UNKNSTAT);
        }
        view.setText(resId);
    }
}
