package com.netcafe.platform.domain.dto.user;

import java.util.List;

public class UserListResponse {
  private long total;
  private long page;
  private long size;
  private List<UserView> items;

  public UserListResponse(long total, long page, long size, List<UserView> items) {
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

  public List<UserView> getItems() {
    return items;
  }

  public void setItems(List<UserView> items) {
    this.items = items;
  }
}
