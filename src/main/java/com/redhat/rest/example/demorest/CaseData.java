package com.redhat.rest.example.demorest;


public class CaseData {

    private String customerName;
    private String customerAddress;
    private String customerPhone;
    private String category;
    private String businessUnit;
    private String complaintsDescription;

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerAddress() {
        return customerAddress;
    }

    public void setCustomerAddress(String customerAddress) {
        this.customerAddress = customerAddress;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getBusinessUnit() {
        return businessUnit;
    }

    public void setBusinessUnit(String businessUnit) {
        this.businessUnit = businessUnit;
    }

    public String getComplaintsDescription() {
        return complaintsDescription;
    }

    public void setComplaintsDescription(String complaintsDescription) {
        this.complaintsDescription = complaintsDescription;
    }
}
