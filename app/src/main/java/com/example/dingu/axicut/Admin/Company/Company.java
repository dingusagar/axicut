package com.example.dingu.axicut.Admin.Company;

import java.io.Serializable;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by root on 14/5/17.
 */

public class Company implements Serializable{
    private String companyName;
    private String companyId;
    private String description;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Company(){}
    public Company(String name,String ID){
        this.companyName=name;
        this.companyId=ID;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    @Override
    public String toString() {
        return getCompanyId();
    }

//    @Override
//    public boolean equals(Object v) {
//        boolean retVal = false;
//
//        if (v instanceof Company){
//            Company ptr = (Company) v;
//            retVal = (ptr.companyId.equals(this.companyId));
//        }
//
//        return retVal;
//    }
}
