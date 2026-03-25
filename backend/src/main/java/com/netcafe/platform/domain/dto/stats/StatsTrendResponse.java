package com.netcafe.platform.domain.dto.stats;

import java.time.LocalDate;
import java.util.List;

public class StatsTrendResponse {
  private String granularity;
  private LocalDate startDate;
  private LocalDate endDate;
  private List<StatsTrendPointView> points;

  public String getGranularity() {
    return granularity;
  }

  public void setGranularity(String granularity) {
    this.granularity = granularity;
  }

  public LocalDate getStartDate() {
    return startDate;
  }

  public void setStartDate(LocalDate startDate) {
    this.startDate = startDate;
  }

  public LocalDate getEndDate() {
    return endDate;
  }

  public void setEndDate(LocalDate endDate) {
    this.endDate = endDate;
  }

  public List<StatsTrendPointView> getPoints() {
    return points;
  }

  public void setPoints(List<StatsTrendPointView> points) {
    this.points = points;
  }
}
