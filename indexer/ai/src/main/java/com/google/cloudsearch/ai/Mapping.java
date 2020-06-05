package com.google.cloudsearch.ai;

import java.util.List;

class Mapping {
    private List<String> PropertyNames;
    private String  SkillOutputName;
   // private List<FilterObject> filter;

    public void setPropertyNames(List<String> list){

        this.PropertyNames = list;
    }
    public void setSkillOutputName(String name){

        this.SkillOutputName = name;
    }
   /* public void setFilter(List<FilterObject> filterList){

        this.filter = filterList;
    }
    public List<String> getPropertyNames(){
        return this.PropertyNames;
    }
    public String getSkillOutputName(){
        return this.SkillOutputName;
    }
    public List<FilterObject> getFilter(){
        return this.filter;
    }*/
}
