package com.postalservice14.jpa.usertypes;

public class JsonbUserType extends JsonUserTypeSupport {

    public String getType() {
        return "jsonb";
    }
}
