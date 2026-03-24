package com.netcafe.platform.domain.dto.session;

import java.util.List;

public class SessionListResponse {
  private final long total;
  private final long page;
  private final long size;
  private final List<SessionView> items;

  public SessionListResponse(long total, long page, long size, List<SessionView> items) {
    this.total = total;
    this.page = page;
    this.size = size;
    this.items = items;
  }

  public long getTotal() {
    return total;
  }

  public long getPage() {
    return page;
  }

  public long getSize() {
    return size;
  }

  public List<SessionView> getItems() {
    return items;
  }
}
