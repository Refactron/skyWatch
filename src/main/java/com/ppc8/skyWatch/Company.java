package com.ppc8.skyWatch;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Company {
    @Id
    private String companyId;
    private String companyName;

    public Company() {}
    public Company(String tickerSymbol, String companyName) {
        this.companyId = tickerSymbol.toUpperCase();
        this.companyName = companyName;
    }

    public String getCompanyId() { return companyId; }
    public String getCompanyName() { return companyName; }
    public void setCompanyId(String ticker) { this.companyId = ticker.toUpperCase(); }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
}