package com.ppc8.skyWatch;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "financials")
public class Financials {
    @Id
    private String id; // e.g., AAPL-2022-Q1

    @ManyToOne
    private Company company;

    private LocalDate startDate;
    private LocalDate endDate;
    private String fiscalYear;
    private String fiscalPeriod;

    private Double revenues;
    private Double net_income_loss;
    private Double current_assets;
    private Double gross_profit;
    private Double operating_income_loss;
    private Double basic_earnings_per_share;
    private Long basic_average_shares;
    private Double inventory;
    private Double wages;
    private Double cost_of_revenue;

    public Financials() {}

    public Financials(String id, Company company, LocalDate startDate, LocalDate endDate, String fiscalYear, String fiscalPeriod,
                      Double revenues, Double net_income_loss, Double current_assets,
                      Double gross_profit, Double operating_income_loss, Double basic_earnings_per_share,
                      Long basic_average_shares, Double inventory, Double wages, Double cost_of_revenue) {
        this.id = id;
        this.company = company;
        this.startDate = startDate;
        this.endDate = endDate;
        this.fiscalYear = fiscalYear;
        this.fiscalPeriod = fiscalPeriod;
        this.revenues = revenues;
        this.net_income_loss = net_income_loss;
        this.current_assets = current_assets;
        this.gross_profit = gross_profit;
        this.operating_income_loss = operating_income_loss;
        this.basic_earnings_per_share = basic_earnings_per_share;
        this.basic_average_shares = basic_average_shares;
        this.inventory = inventory;
        this.wages = wages;
        this.cost_of_revenue = cost_of_revenue;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Company getCompany() { return company; }
    public void setCompany(Company company) { this.company = company; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public String getFiscalYear() { return fiscalYear; }
    public void setFiscalYear(String fiscalYear) { this.fiscalYear = fiscalYear; }

    public String getFiscalPeriod() { return fiscalPeriod; }
    public void setFiscalPeriod(String fiscalPeriod) { this.fiscalPeriod = fiscalPeriod; }

    public Double getRevenues() { return revenues; }
    public void setRevenues(Double revenues) { this.revenues = revenues; }

    public Double getNet_income_loss() { return net_income_loss; }
    public void setNet_income_loss(Double net_income_loss) { this.net_income_loss = net_income_loss; }

    public Double getCurrent_assets() { return current_assets; }
    public void setCurrent_assets(Double current_assets) { this.current_assets = current_assets; }

    public Double getGross_profit() { return gross_profit; }
    public void setGross_profit(Double gross_profit) { this.gross_profit = gross_profit; }

    public Double getOperating_income_loss() { return operating_income_loss; }
    public void setOperating_income_loss(Double operating_income_loss) { this.operating_income_loss = operating_income_loss; }

    public Double getBasic_earnings_per_share() { return basic_earnings_per_share; }
    public void setBasic_earnings_per_share(Double basic_earnings_per_share) { this.basic_earnings_per_share = basic_earnings_per_share; }

    public Long getBasic_average_shares() { return basic_average_shares; }
    public void setBasic_average_shares(Long basic_average_shares) { this.basic_average_shares = basic_average_shares; }

    public Double getInventory() { return inventory; }
    public void setInventory(Double inventory) { this.inventory = inventory; }

    public Double getWages() { return wages; }
    public void setWages(Double wages) { this.wages = wages; }

    public Double getCost_of_revenue() { return cost_of_revenue; }
    public void setCost_of_revenue(Double cost_of_revenue) { this.cost_of_revenue = cost_of_revenue; }
}