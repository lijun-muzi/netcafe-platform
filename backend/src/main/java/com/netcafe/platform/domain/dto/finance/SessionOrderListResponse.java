package com.netcafe.platform.domain.dto.finance;

import java.util.List;

public class SessionOrderListResponse {
  private long total;
  private long page;
  private long size;
  private List<SessionOrderView> items;

  public SessionOrderListResponse() {
  }

  public SessionOrderListResponse(long total, long page, long size, List<SessionOrderView> items) {
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

  public List<SessionOrderView> getItems() {
    return items;
  }

  public void setItems(List<SessionOrderView> items) {
    this.items = items;
  }
}
