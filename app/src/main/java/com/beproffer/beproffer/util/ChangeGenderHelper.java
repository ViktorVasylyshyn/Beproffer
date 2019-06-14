package com.beproffer.beproffer.util;

import com.beproffer.beproffer.util.Const;

public class ChangeGenderHelper {
    
    public String changeGender(String currentGender){
        if (currentGender == null) {
            currentGender = Const.MALE;
            return currentGender;
        }
        switch (currentGender) {
            case Const.MALE:
                currentGender = Const.FEMALE;
                break;
            case Const.FEMALE:
                currentGender = Const.MALE;
                break;
            default:
                throw new IllegalArgumentException(Const.UNKNSTAT);
        }
        return currentGender;
    }
}
