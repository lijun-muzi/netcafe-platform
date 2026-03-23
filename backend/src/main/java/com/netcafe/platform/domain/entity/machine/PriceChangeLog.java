package com.netcafe.platform.domain.entity.machine;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("price_change_log")
public class PriceChangeLog {
  @TableId(type = IdType.AUTO)
  private Long id;
  private Long machineId;
  private BigDecimal oldPrice;
  private BigDecimal newPrice;
  private Long operatorAdminId;
  private LocalDateTime createdAt;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getMachineId() {
    return machineId;
  }

  public void setMachineId(Long machineId) {
    this.machineId = machineId;
  }

  public BigDecimal getOldPrice() {
    return oldPrice;
  }

  public void setOldPrice(BigDecimal oldPrice) {
    this.oldPrice = oldPrice;
  }

  public BigDecimal getNewPrice() {
    return newPrice;
  }

  public void setNewPrice(BigDecimal newPrice) {
    this.newPrice = newPrice;
  }

  public Long getOperatorAdminId() {
    return operatorAdminId;
  }

  public void setOperatorAdminId(Long operatorAdminId) {
    this.operatorAdminId = operatorAdminId;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }
}
