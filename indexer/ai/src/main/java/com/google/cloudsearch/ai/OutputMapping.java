package com.google.cloudsearch.ai;

import java.util.List;

class OutputMapping {
    private String propertyName;
    private String skillOutputField;

    public void setPropertyNames(String property){

        this.propertyName = property;
    }

    public String getPropertyName(){
        return this.propertyName;
    }

    public void setSkillOutputField(String name){

        this.skillOutputField = name;
    }

    public String getSkillOutputField(){
        return this.skillOutputField;
    }
}
