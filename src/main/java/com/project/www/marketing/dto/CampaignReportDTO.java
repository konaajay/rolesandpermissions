package com.project.www.marketing.dto;

import com.project.www.marketing.dto.CampaignReportDTO;

import com.project.www.enums.*;

import java.math.BigDecimal;
import java.util.Map;

public class CampaignReportDTO {
    private Long campaignId;
    private String campaignName;
    private long impressions;
    private long visits;
    private long leads;
    private long signups;
    private long purchases;
    private double conversionRate;
    private double dropOffRate;
    private BigDecimal budget;
    private BigDecimal spent;
    private BigDecimal revenue;
    private BigDecimal refunds;
    private double budgetBurnRate;
    private double revenueLeakage;
    private double roi;
    private Map<String, Long> funnelEfficiency;

    public CampaignReportDTO() {}

    // Manual Getters & Setters 🛡️🏁
    public Long getCampaignId() { return campaignId; }
    public void setCampaignId(Long id) { this.campaignId = id; }

    public String getCampaignName() { return campaignName; }
    public void setCampaignName(String name) { this.campaignName = name; }

    public long getImpressions() { return impressions; }
    public void setImpressions(long i) { this.impressions = i; }

    public long getVisits() { return visits; }
    public void setVisits(long v) { this.visits = v; }

    public long getLeads() { return leads; }
    public void setLeads(long l) { this.leads = l; }

    public long getSignups() { return signups; }
    public void setSignups(long s) { this.signups = s; }

    public long getPurchases() { return purchases; }
    public void setPurchases(long p) { this.purchases = p; }

    public double getConversionRate() { return conversionRate; }
    public void setConversionRate(double cr) { this.conversionRate = cr; }

    public double getDropOffRate() { return dropOffRate; }
    public void setDropOffRate(double dr) { this.dropOffRate = dr; }

    public BigDecimal getBudget() { return budget; }
    public void setBudget(BigDecimal b) { this.budget = b; }

    public BigDecimal getSpent() { return spent; }
    public void setSpent(BigDecimal s) { this.spent = s; }

    public BigDecimal getRevenue() { return revenue; }
    public void setRevenue(BigDecimal r) { this.revenue = r; }

    public BigDecimal getRefunds() { return refunds; }
    public void setRefunds(BigDecimal rf) { this.refunds = rf; }

    public double getBudgetBurnRate() { return budgetBurnRate; }
    public void setBudgetBurnRate(double bbr) { this.budgetBurnRate = bbr; }

    public double getRevenueLeakage() { return revenueLeakage; }
    public void setRevenueLeakage(double rl) { this.revenueLeakage = rl; }

    public double getRoi() { return roi; }
    public void setRoi(double r) { this.roi = r; }

    public Map<String, Long> getFunnelEfficiency() { return funnelEfficiency; }
    public void setFunnelEfficiency(Map<String, Long> fe) { this.funnelEfficiency = fe; }

    // Manual Builder Pattern 🛠️🏁
    public static class Builder {
        private final CampaignReportDTO dto = new CampaignReportDTO();
        public Builder campaignId(Long id) { dto.setCampaignId(id); return this; }
        public Builder campaignName(String name) { dto.setCampaignName(name); return this; }
        public Builder leads(long l) { dto.setLeads(l); return this; }
        public Builder visits(long v) { dto.setVisits(v); return this; }
        public Builder signups(long s) { dto.setSignups(s); return this; }
        public Builder revenue(BigDecimal r) { dto.setRevenue(r); return this; }
        public Builder spent(BigDecimal s) { dto.setSpent(s); return this; }
        public Builder budget(BigDecimal b) { dto.setBudget(b); return this; }
        public Builder budgetBurnRate(double bbr) { dto.setBudgetBurnRate(bbr); return this; }
        public Builder revenueLeakage(double rl) { dto.setRevenueLeakage(rl); return this; }
        public Builder funnelEfficiency(Map<String, Long> fe) { dto.setFunnelEfficiency(fe); return this; }
        public CampaignReportDTO build() { return dto; }
    }
    public static Builder builder() { return new Builder(); }
}
