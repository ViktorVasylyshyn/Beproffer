package com.beproffer.beproffer.data.models;

/**
 * ссылка, на тело сервис объекта, которая сохраняется в раздел с сервсами.
 * согласно задумкам, ссылка имеет в себе только те поля, которые учавствуют в фильтрации
 * и определяют путь к отфильтрованному объекту. Эти объекты подаются в адапптер и согласно
 * им показываются изображения пользователям.
 */

public class BrowsingItemRef {

    private String id;
    private String key;
    private String gender;
    private String url;

    public BrowsingItemRef() {
        //public no-arg constructor for firebase
    }

    public BrowsingItemRef(String id, String key, String gender, String url) {
        this.id = id;
        this.gender = gender;
        this.key = key;
        this.url = url;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getGender() {
        return gender;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
