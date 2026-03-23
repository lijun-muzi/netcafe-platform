<template>
  <div class="kiosk-card reveal">
    <p class="eyebrow">用户登录</p>
    <h2>请输入身份证号码</h2>
    <p class="subtitle">仅用于核验上机订单，不会保存明文。</p>
    <input class="input" placeholder="身份证号码" />
    <button class="solid-btn">开始上机</button>
    <div class="alert">余额不足 60 分钟时将自动弹窗提示续费。</div>
  </div>

  <div class="kiosk-card reveal" style="animation-delay: 0.08s">
    <p class="eyebrow">自助平台</p>
    <h2>机位控制提示</h2>
    <div class="info-row">
      <span>机位编号</span>
      <span>A-027</span>
    </div>
    <div class="info-row">
      <span>当前单价</span>
      <span>¥0.12/分钟</span>
    </div>
    <div class="info-row">
      <span>休息模式</span>
      <span>暂停计费</span>
    </div>
  </div>

  <div class="kiosk-card reveal" style="animation-delay: 0.14s">
    <p class="eyebrow">上机说明</p>
    <ul class="kiosk-list">
      <li>上机前余额需 ≥ 单价。</li>
      <li>暂停期间计费停止，需身份证解锁继续。</li>
      <li>余额不足将自动下机并结算。</li>
      <li>如需续费请联系管理员。</li>
    </ul>
  </div>
</template>

<style scoped>
.kiosk-list {
  display: grid;
  gap: 8px;
  color: var(--muted);
  font-size: 14px;
  padding-left: 16px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: var(--muted);
}
</style>
