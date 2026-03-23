<template>
  <div class="kiosk-card reveal">
    <p class="eyebrow">下机确认</p>
    <h2>确认结束本次上机</h2>
    <p class="subtitle">确认后将停止计费并生成订单。</p>

    <div class="metric-row">
      <span>本次时长</span>
      <span>92 分钟</span>
    </div>
    <div class="metric-row">
      <span>预计扣费</span>
      <span>¥11.04</span>
    </div>
    <div class="metric-row">
      <span>余额变更</span>
      <span>¥22.10 → ¥11.06</span>
    </div>

    <div class="kiosk-actions">
      <button class="solid-btn">确认下机</button>
      <button class="ghost-btn">返回</button>
    </div>
  </div>

  <div class="kiosk-card reveal" style="animation-delay: 0.1s">
    <p class="eyebrow">温馨提示</p>
    <p class="subtitle">如需续费，请联系管理员。结算完成后 3 秒内自动退出。</p>
    <div class="alert">强制结束的订单将记录在审计日志中。</div>
  </div>
</template>
