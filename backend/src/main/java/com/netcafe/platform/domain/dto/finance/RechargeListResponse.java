package com.netcafe.platform.domain.dto.finance;

import java.util.List;

public class RechargeListResponse {
  private long total;
  private long page;
  private long size;
  private List<RechargeView> items;

  public RechargeListResponse() {
  }

  public RechargeListResponse(long total, long page, long size, List<RechargeView> items) {
    this.total = total;
    this.page = page;
    this.size = size;
    this.items = items;
  }

  public long getTotal() {
    return total;
  }

  public void setTotal(long total) {
    this.total = total;
  }

  public long getPage() {
    return page;
  }

  public void setPage(long page) {
    this.page = page;
  }

  public long getSize() {
    return size;
  }

  public void setSize(long size) {
    this.size = size;
  }

  public List<RechargeView> getItems() {
    return items;
  }

  public void setItems(List<RechargeView> items) {
    this.items = items;
  }
}
