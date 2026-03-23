package com.netcafe.platform.domain.dto.account;

import java.util.List;

public class AdminListResponse {
  private long total;
  private List<AdminView> items;

  public AdminListResponse(long total, List<AdminView> items) {
    this.total = total;
    this.items = items;
  }

  public long getTotal() {
    return total;
  }

  public void setTotal(long total) {
    this.total = total;
  }

  public List<AdminView> getItems() {
    return items;
  }

  public void setItems(List<AdminView> items) {
    this.items = items;
  }
}
