package com.beproffer.beproffer.util;

import android.content.Context;
import android.view.MenuItem;

import com.beproffer.beproffer.R;

import java.util.HashMap;
import java.util.Map;

public class DefineServiceType {

    private final Context context;

    public DefineServiceType(Context context) {
        this.context = context;
    }

    public Map<String, String> setRequest(MenuItem menuItem) {
        String serviceType;
        String serviceSubtype;
        String serviceTitle;
        switch (menuItem.getItemId()) {
            case R.id.menu_hair_haircut:
                serviceType = Const.HAI;
                serviceSubtype = Const.HAICUT;
                serviceTitle = context.getResources().getString(R.string.hair_title_haircut);
                break;
            case R.id.menu_hair_extension:
                serviceType = Const.HAI;
                serviceSubtype = Const.HAIEXT;
                serviceTitle = context.getResources().getString(R.string.hair_title_extension);
                break;
            case R.id.menu_hair_styling:
                serviceType = Const.HAI;
                serviceSubtype = Const.HAISTY;
                serviceTitle = context.getResources().getString(R.string.hair_title_styling);
                break;
            case R.id.menu_hair_straightening:
                serviceType = Const.HAI;
                serviceSubtype = Const.HAISTR;
                serviceTitle = context.getResources().getString(R.string.hair_title_straightening);
                break;
            case R.id.menu_hair_dyeing:
                serviceType = Const.HAI;
                serviceSubtype = Const.HAIDYE;
                serviceTitle = context.getResources().getString(R.string.hair_title_dyeing);
                break;
            case R.id.menu_hair_braids:
                serviceType = Const.HAI;
                serviceSubtype = Const.HAIBRA;
                serviceTitle = context.getResources().getString(R.string.hair_title_braids);
                break;
            case R.id.menu_nails_manicure:
                serviceType = Const.NAI;
                serviceSubtype = Const.NAIMAN;
                serviceTitle = context.getResources().getString(R.string.nails_title_manicure);
                break;
            case R.id.menu_nails_pedicure:
                serviceType = Const.NAI;
                serviceSubtype = Const.NAIPED;
                serviceTitle = context.getResources().getString(R.string.nails_title_pedicure);
                break;
            case R.id.menu_nails_extension:
                serviceType = Const.NAI;
                serviceSubtype = Const.NAIEXT;
                serviceTitle = context.getResources().getString(R.string.nails_title_extension);
                break;
            case R.id.menu_makeup:
                serviceType = Const.MAK;
                serviceSubtype = Const.MAKMAK;
                serviceTitle = context.getResources().getString(R.string.makeup_title_makeup);
                break;
            case R.id.menu_makeup_permanent:
                serviceType = Const.MAK;
                serviceSubtype = Const.MAKPER;
                serviceTitle = context.getResources().getString(R.string.makeup_title_permanent_makeup);
                break;
            case R.id.menu_makeup_eyelash_extension:
                serviceType = Const.MAK;
                serviceSubtype = Const.MAKEEX;
                serviceTitle = context.getResources().getString(R.string.makeup_title_eyelash_extension);
                break;
            case R.id.menu_makeup_eyebrow_styling:
                serviceType = Const.MAK;
                serviceSubtype = Const.MAKEST;
                serviceTitle = context.getResources().getString(R.string.makeup_title_eyebrow_styling);
                break;
            case R.id.menu_tattoo_tattoo:
                serviceType = Const.TAT;
                serviceSubtype = Const.TATTAT;
                serviceTitle = context.getResources().getString(R.string.tattoo_title_tattoo);
                break;
            case R.id.menu_tattoo_temporary_tattoo:
                serviceType = Const.TAT;
                serviceSubtype = Const.TATTEM;
                serviceTitle = context.getResources().getString(R.string.tattoo_title_temporary_tattoo);
                break;
            case R.id.menu_barber:
                serviceType = Const.BAR;
                serviceSubtype = Const.BARSTY;
                serviceTitle = context.getResources().getString(R.string.barber_title_barber);
                break;
            case R.id.menu_fitness:
                serviceType = Const.FIT;
                serviceSubtype = Const.FITTRA;
                serviceTitle = context.getResources().getString(R.string.fitness_title_trainer);
                break;
            default:
                throw new IllegalArgumentException("unknown menu item id");
        }
        Map<String, String> requestParams = new HashMap<>();
        requestParams.put(Const.SERVTYPE, serviceType);
        requestParams.put(Const.SERVSBTP, serviceSubtype);
        requestParams.put(Const.SERVTITL, serviceTitle);
        return requestParams;
    }
}
