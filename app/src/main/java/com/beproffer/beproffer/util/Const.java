package com.beproffer.beproffer.util;

public class Const {

    private Const() {
    }

    /*Firebase children*/
    public static final String USERS = "users";
    public static final String SPEC = "specialist"; /*specialist*/
    public static final String CUST = "customer"; /*customer*/
    public static final String DELETED = "deleted";

    public static final String INFO = "userInfo"; /*global child*/
    public static final String CONTACT = "contact";
    public static final String INREQUEST = "inrequest";
    public static final String SERVICES = "services"; /*service image's data*/

    public static final String USERTYPE = "userType";

    public static final String GENDER = "gender";
    public static final String IMAGES = "images"; /*child with user images' urls*/
    public static final String VOTES = "votes"; /*directory for claims about prohibited content*/

    /*Firebase storage constants*/
    public static final String PROF = "prof"; /*profile*/
    public static final String SERV = "serv"; /*service*/

    /*services database code*/
    public static final String SERVTYPE = "type"; /*service's type*/
    public static final String SERVSBTP = "subtype"; /*service's subtype*/
    public static final String SERVTITL = "servTitle"; /*service's title*/

    public static final String HAI = "HAI"; /*menu_hair*/
    public static final String HAICUT = "HAICUT"; /*submenu_haircut*/
    public static final String HAIEXT = "HAIEXT"; /*submenu_hair_extension*/
    public static final String HAISTY = "HAISTY"; /*submenu_styling*/
    public static final String HAISTR = "HAISTR"; /*submenu_straightening*/
    public static final String HAIDYE = "HAIDYE"; /*submenu_dyeing*/
    public static final String HAIBRA = "HAIBRA"; /*submenu_braids*/

    public static final String NAI = "NAI"; /*menu_nails*/
    public static final String NAIMAN = "NAIMAN"; /*submenu_manicure*/
    public static final String NAIPED = "NAIPED"; /*submenu_pedicure*/
    public static final String NAIEXT = "NAIEXT"; /*submenu_nails_extension*/

    public static final String MAK = "MAK"; /*menu_makeup*/
    public static final String MAKMAK = "MAKMAK"; /*submenu_makeup*/
    public static final String MAKPER = "MAKPER"; /*permanent makeup*/
    public static final String MAKEEX = "MAKEEX"; /*submenu_eyelash_extensions*/
    public static final String MAKEST = "MAKEST"; /*submenu_eyebrow_styling*/


    public static final String TAT = "TAT"; /*menu_tattoo*/
    public static final String TATTAT = "TATTAT"; /*submenu_tattoo*/
    public static final String TATTEM = "TATTEM"; /*submenu_temporary_tattoo*/

    public static final String BAR = "BAR"; /*menu_beards*/
    public static final String BARSTY = "BARSTY"; /*submenu_beards_styling*/

    public static final String FIT = "FIT"; /*menu_fitness*/
    public static final String FITTRA = "FITTRA"; /*submenu_personal_trainer*/


    /*genders*/
    public static final String MALE = "male"; /*male*/
    public static final String FEMALE = "female"; /*female*/
    public static final String BOTHGEND = "both"; /*all genders*/


    /*duration*/
    public static final String MIN30 = "30"; /*30 min*/
    public static final String MIN45 = "45"; /*45 min*/
    public static final String MIN60 = "60"; /*60 min*/
    public static final String MIN90 = "90"; /*90 min*/
    public static final String MIN120 = "120"; /*120 min*/


    /*Log and system title*/
    public static final String UNKNSTAT = "unknown state";
    public static final String UNKNOWN_USER_REQUEST = "unknownUserRequest";/*ключ для получения
    параметров запроса из SharedPref для не зарегистрированного пользователя*/

    /*votes*/

    public static final String PROHCONT = "prohcont";
    public static final String WRONSECT = "wronsect";

    /*request code*/
    public static final int REQUEST_CODE_1 = 1;

    public static final int CONTACTS_NUM = 10; /*max num of available contacts*/
    public static final int SEARCH_PARAMS_NUM = 3; /*num of search params*/
    public static final int IMAGES_BASE_SET_COUNT = 5; /**/


    public static final int MAX_NUM_OF_IMAGES_IN_ADAPTER = 7; /*max num of image items in swipe card adapter*/

    public static final int COOLDOWNDUR_SHORT = 1500;
    public static final int COOLDOWNDUR_LONG = 3000; /*заморозка повторного нажатия вью элементов после предыдущего.
    в основном применимо к вью элементам, которые не посылают запрос на сервер*/

    public static final int POPBACKSTACK_WAITING = 300; /*система не успевает сбросить параметр true при
    переходе назад, и иногда происходят перескоки сразу на два шага назад. так как предыдущий фрагмент
    хватает из репозитория значение true, а оно должно было быть false*/

    /*индексы иконок на панели навигации*/
    public static final int CONTBNBINDEX = 1;/*contacts section(browsing image - 0, profile - 2)*/

}
