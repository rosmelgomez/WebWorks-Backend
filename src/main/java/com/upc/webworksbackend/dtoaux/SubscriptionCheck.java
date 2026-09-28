package com.upc.webworksbackend.dtoaux;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionCheck {
    private boolean status = false;
    private float amount = -1;
    private int maxNumberRepository;
    private int maxNumberProject;
    private String planName;

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public int getMaxNumberRepository() {
        return maxNumberRepository;
    }

    public void setMaxNumberRepository(int maxNumberRepository) {
        this.maxNumberRepository = maxNumberRepository;
    }

    public int getMaxNumberProject() {
        return maxNumberProject;
    }

    public void setMaxNumberProject(int maxNumberProject) {
        this.maxNumberProject = maxNumberProject;
    }

    public String getPlanName() {
        return planName;
    }

    public void setPlanName(String planName) {
        this.planName = planName;
    }
}
