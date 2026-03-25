package com.netcafe.platform.domain.entity.session;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("session_order")
public class SessionOrder {
  @TableId(type = IdType.AUTO)
  private Long id;
  private Long userId;
  private Long machineId;
  private LocalDateTime startTime;
  private LocalDateTime endTime;
  private Integer durationMinutes;
  private BigDecimal priceSnapshot;
  private BigDecimal amount;
  private Integer billedMinutes;
  private LocalDateTime lastBilledTime;
  private LocalDateTime pausedAt;
  private Integer pausedDurationSeconds;
  private Integer status;
  private Long forceByAdminId;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public Long getMachineId() {
    return machineId;
  }

  public void setMachineId(Long machineId) {
    this.machineId = machineId;
  }

  public LocalDateTime getStartTime() {
    return startTime;
  }

  public void setStartTime(LocalDateTime startTime) {
    this.startTime = startTime;
  }

  public LocalDateTime getEndTime() {
    return endTime;
  }

  public void setEndTime(LocalDateTime endTime) {
    this.endTime = endTime;
  }

  public Integer getDurationMinutes() {
    return durationMinutes;
  }

  public void setDurationMinutes(Integer durationMinutes) {
    this.durationMinutes = durationMinutes;
  }

  public BigDecimal getPriceSnapshot() {
    return priceSnapshot;
  }

  public void setPriceSnapshot(BigDecimal priceSnapshot) {
    this.priceSnapshot = priceSnapshot;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public void setAmount(BigDecimal amount) {
    this.amount = amount;
  }

  public Integer getBilledMinutes() {
    return billedMinutes;
  }

  public void setBilledMinutes(Integer billedMinutes) {
    this.billedMinutes = billedMinutes;
  }

  public LocalDateTime getLastBilledTime() {
    return lastBilledTime;
  }

  public void setLastBilledTime(LocalDateTime lastBilledTime) {
    this.lastBilledTime = lastBilledTime;
  }

  public LocalDateTime getPausedAt() {
    return pausedAt;
  }

  public void setPausedAt(LocalDateTime pausedAt) {
    this.pausedAt = pausedAt;
  }

  public Integer getPausedDurationSeconds() {
    return pausedDurationSeconds;
  }

  public void setPausedDurationSeconds(Integer pausedDurationSeconds) {
    this.pausedDurationSeconds = pausedDurationSeconds;
  }

  public Integer getStatus() {
    return status;
  }

  public void setStatus(Integer status) {
    this.status = status;
  }

  public Long getForceByAdminId() {
    return forceByAdminId;
  }

  public void setForceByAdminId(Long forceByAdminId) {
    this.forceByAdminId = forceByAdminId;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }
}
