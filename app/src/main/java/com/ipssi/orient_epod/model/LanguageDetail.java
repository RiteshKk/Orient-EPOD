package com.ipssi.orient_epod.model;

public class LanguageDetail {
    private String langCode;
    private String langName;
    private String langEnglishName;

    public LanguageDetail(String langCode, String langName, String langEnglishName) {
        this.langCode = langCode;
        this.langName = langName;
        this.langEnglishName = langEnglishName;
    }

    public String getLangCode() {
        return langCode;
    }

    public String getLangName() {
        return langName;
    }

    public String getLangEnglishName() {
        return langEnglishName;
    }
}
