package com.beproffer.beproffer.util;

public class Const {

    private Const() {
    }

    /*Firebase children*/
    public static final String USERS = "users";
    public static final String SPEC = "specialist"; /*specialist*/
    public static final String CUST = "customer"; /*customer*/
    public static final String DELETED = "deleted";

    public static final String INFO = "info"; /*global child*/
    public static final String CONTACT = "contact";
    public static final String POPULARITY = "popularity";
    public static final String SERVICES = "services"; /*service image's data*/

    public static final String USERTYPE = "userType";

    public static final String GENDER = "gender";
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
    public static final String HAISTY = "HAISTY"; /*submenu_styling*/
    public static final String HAIDYE = "HAIDYE"; /*submenu_dyeing*/
    public static final String HAIEXT = "HAIEXT"; /*submenu_extension*/
    public static final String HAIBRA = "HAIBRA"; /*submenu_braids*/
    public static final String HAIOTH = "HAIOTH"; /*submenu_other*/

    public static final String NAI = "NAI"; /*menu_nails*/
    public static final String NAIMAN = "NAIMAN"; /*submenu_manicure*/
    public static final String NAIPED = "NAIPED"; /*submenu_pedicure*/
    public static final String NAIEXT = "NAIEXT"; /*submenu_nails_extension*/
    public static final String NAIOTH = "NAIOTH"; /*submenu_other*/

    public static final String MAK = "MAK"; /*menu_makeup*/
    public static final String MAKMAK = "MAKMAK"; /*submenu_makeup*/
    public static final String MAKPER = "MAKPER"; /*permanent makeup*/
    public static final String MAKEEX = "MAKEEX"; /*submenu_eyelash_extensions*/
    public static final String MAKEST = "MAKEST"; /*submenu_eyebrow_styling*/
    public static final String MAKOTH = "MAKOTH"; /*submenu_other*/

    public static final String TAT = "TAT"; /*menu_tattoo*/
    public static final String TATTAT = "TATTAT"; /*submenu_tattoo*/
    public static final String TATOTH = "TATOTH"; /*submenu_other*/


    /*genders*/
    /*При выборке из Realtime Database услуг по полу, мы должны показывать пользователю объекты для
     того пола, который он выбрал + те, что валидны для обоих полов. Специфика фильтрации такова, что
     мы не можем указать два значения String объектов которые нам нужны. но мы можем применить
     лексикографическую сортировку.
     Получается при выборе "male" мы дадим "a" + "b" а при выборе "female" - "c" + "b"*/
    public static final String MALE = "male";
    public static final String BOTHGEND = "both";
    public static final String FEMALE = "female";


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
    public static final String NOT_DELETED = "notdeleted";

    /*request code*/
    public static final int REQUEST_CODE_1 = 1;

    public static final int CONTACTS_NUM = 10; /*max num of available contacts*/
    public static final int SEARCH_PARAMS_NUM = 3; /*num of search params*/
    public static final int IMAGES_BASE_SET_COUNT = 5; /**/


    /*TODO пока что, максимальное количество взято из головы. со временем определить
     * оптимальные цифры для константы*/

    public static final int COOLDOWNDUR_SHORT = 1500;
    public static final int COOLDOWNDUR_LONG = 3000; /*заморозка повторного нажатия вью элементов после предыдущего.
    в основном применимо к вью элементам, которые не посылают запрос на сервер*/

    public static final int POPBACKSTACK_WAITING = 500; /*система не успевает сбросить параметр true при
    переходе назад, и иногда происходят перескоки сразу на два шага назад. так как предыдущий фрагмент
    хватает из репозитория значение true, а оно должно было быть false*/

    public static final int PROFILE_IMAGE_BITMAP_QUALITY = 20;
    public static final int SERVICE_IMAGE_BITMAP_QUALITY = 20;

    /*TODO со временем определить, сколько изображений, после входа в приложение, просматривает
     * среднестатистический пользователь. Сделать константу BROWSING_REFS_LIST_MAX_SIZE на 10-20% больше,
     * чем количество просмотров за один старт приложения. Предполагается, что это ограничит количество
     * запросо в базу данных, и не так будет нагружать систему, но при этом и не держать в list
     * сотни объектов BrowsingImageSearchRef*/
    public static final int BROWSING_REFS_LIST_MAX_SIZE = 64;
    /*TODO список подающийся непосредственно в BrowsingFragment сделать 10-15шт*/
    public static final int BROWSING_REFS_LIST_SIZE = 16;

    public static final String ERROR = "ERROR"; /*the constant for the logcat filter*/

    /* макс размеры изображений */
    public static final int PROFILE_IMAGE_MAX_SIZE = 4000000;
    public static final int SERVICE_IMAGE_MAX_SIZE = 6000000;

}
