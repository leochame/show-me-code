package com.adam.strategy.autofill.domain;

import com.adam.strategy.autofill.annoation.DataDictionary;
import com.adam.strategy.autofill.annoation.FlagURL;

public class User {
    @DataDictionary(key = "gender", ref = "genderDescription")
    private String gender;  // gender字段会填充 genderDescription 字段

    @FlagURL(key = "US", ref = "flagUrl")
    private String countryCode;  // countryCode字段会填充 flagUrl 字段

    private String genderDescription;
    private String flagUrl;

    // Getters and Setters
    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getGenderDescription() {
        return genderDescription;
    }

    public void setGenderDescription(String genderDescription) {
        this.genderDescription = genderDescription;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getFlagUrl() {
        return flagUrl;
    }

    public void setFlagUrl(String flagUrl) {
        this.flagUrl = flagUrl;
    }
}
