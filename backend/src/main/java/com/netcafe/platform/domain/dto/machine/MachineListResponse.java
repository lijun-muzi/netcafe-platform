package com.netcafe.platform.domain.dto.machine;

import java.util.List;

public class MachineListResponse {
  private final long total;
  private final long page;
  private final long size;
  private final List<MachineView> items;

  public MachineListResponse(long total, long page, long size, List<MachineView> items) {
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

  public List<MachineView> getItems() {
    return items;
  }
}
