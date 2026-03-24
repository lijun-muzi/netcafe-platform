package com.netcafe.platform.controller.admin.account;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.netcafe.platform.common.ApiResponse;
import com.netcafe.platform.common.BusinessException;
import com.netcafe.platform.common.ResultCode;
import com.netcafe.platform.domain.dto.user.UserCreateRequest;
import com.netcafe.platform.domain.dto.user.UserListResponse;
import com.netcafe.platform.domain.dto.user.UserRechargeRecordView;
import com.netcafe.platform.domain.dto.user.UserRechargeRequest;
import com.netcafe.platform.domain.dto.user.UserSessionRecordView;
import com.netcafe.platform.domain.dto.user.UserUpdateRequest;
import com.netcafe.platform.domain.dto.user.UserView;
import com.netcafe.platform.domain.entity.account.User;
import com.netcafe.platform.domain.entity.finance.RechargeRecord;
import com.netcafe.platform.domain.entity.session.SessionOrder;
import com.netcafe.platform.service.finance.RechargeService;
import com.netcafe.platform.service.account.UserService;
import com.netcafe.platform.service.session.SessionService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {
  private static final int STATUS_FROZEN = 0;
  private static final int STATUS_ACTIVE = 1;
  private static final Pattern DIGIT_PATTERN = Pattern.compile("^\\d+$");

  private final UserService userService;
  private final RechargeService rechargeService;
  private final SessionService sessionService;

  public UserController(
      UserService userService,
      RechargeService rechargeService,
      SessionService sessionService
  ) {
    this.userService = userService;
    this.rechargeService = rechargeService;
    this.sessionService = sessionService;
  }

  @GetMapping
  public ApiResponse<UserListResponse> list(
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) Integer status,
      @RequestParam(defaultValue = "1") long page,
      @RequestParam(defaultValue = "20") long size
  ) {
    validateStatus(status);
    long pageSize = Math.max(1, Math.min(size, 200));
    Page<User> pageRequest = new Page<>(page, pageSize);
    LambdaQueryWrapper<User> wrapper = buildListWrapper(keyword, status);
    Page<User> result = userService.page(pageRequest, wrapper);
    List<UserView> views = result.getRecords().stream()
        .map(this::toView)
        .collect(Collectors.toList());
    return ApiResponse.success(new UserListResponse(
        result.getTotal(),
        result.getCurrent(),
        result.getSize(),
        views
    ));
  }

  @GetMapping("/{id}")
  public ApiResponse<UserView> detail(@PathVariable Long id) {
    return ApiResponse.success(toView(requireUser(id)));
  }

  @PostMapping
  public ApiResponse<Boolean> create(@Valid @RequestBody UserCreateRequest request) {
    validateStatus(request.getStatus());
    ensureMobileAvailable(request.getMobile(), null);
    ensureIdCardAvailable(request.getIdCard(), null);
    User user = new User();
    user.setMobile(request.getMobile().trim());
    user.setIdCard(request.getIdCard().trim());
    user.setName(request.getName().trim());
    user.setBalance(defaultBalance(request.getBalance()));
    user.setStatus(request.getStatus() == null ? STATUS_ACTIVE : request.getStatus());
    user.setRegisterTime(LocalDateTime.now());
    user.setCreatedAt(LocalDateTime.now());
    user.setUpdatedAt(LocalDateTime.now());
    return ApiResponse.success(userService.save(user));
  }

  @PutMapping("/{id}")
  public ApiResponse<Boolean> update(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
    requireUser(id);
    validateStatus(request.getStatus());
    if (StringUtils.hasText(request.getMobile())) {
      ensureMobileAvailable(request.getMobile(), id);
    }
    if (StringUtils.hasText(request.getIdCard())) {
      ensureIdCardAvailable(request.getIdCard(), id);
    }

    User user = new User();
    user.setId(id);
    if (StringUtils.hasText(request.getMobile())) {
      user.setMobile(request.getMobile().trim());
    }
    if (StringUtils.hasText(request.getIdCard())) {
      user.setIdCard(request.getIdCard().trim());
    }
    if (StringUtils.hasText(request.getName())) {
      user.setName(request.getName().trim());
    }
    if (request.getStatus() != null) {
      user.setStatus(request.getStatus());
    }
    user.setUpdatedAt(LocalDateTime.now());
    return ApiResponse.success(userService.updateById(user));
  }

  @PutMapping("/{id}/status")
  public ApiResponse<Boolean> updateStatus(@PathVariable Long id, @RequestBody UserUpdateRequest request) {
    if (request.getStatus() == null) {
      throw new BusinessException(ResultCode.BAD_REQUEST, "status不能为空");
    }
    validateStatus(request.getStatus());
    requireUser(id);
    User user = new User();
    user.setId(id);
    user.setStatus(request.getStatus());
    user.setUpdatedAt(LocalDateTime.now());
    return ApiResponse.success(userService.updateById(user));
  }

  @PostMapping("/{id}/recharge")
  public ApiResponse<Boolean> recharge(@PathVariable Long id, @Valid @RequestBody UserRechargeRequest request) {
    requireUser(id);
    Long operatorAdminId = requireCurrentAdminId();
    return ApiResponse.success(userService.recharge(
        id,
        request.getAmount(),
        request.getChannel().trim(),
        StringUtils.hasText(request.getRemark()) ? request.getRemark().trim() : null,
        operatorAdminId
    ));
  }

  @GetMapping("/{id}/recharges")
  public ApiResponse<List<UserRechargeRecordView>> rechargeRecords(@PathVariable Long id) {
    requireUser(id);
    LambdaQueryWrapper<RechargeRecord> wrapper = new LambdaQueryWrapper<RechargeRecord>()
        .eq(RechargeRecord::getUserId, id)
        .orderByDesc(RechargeRecord::getCreatedAt);
    List<UserRechargeRecordView> items = rechargeService.list(wrapper).stream()
        .map(this::toRechargeRecordView)
        .collect(Collectors.toList());
    return ApiResponse.success(items);
  }

  @GetMapping("/{id}/sessions")
  public ApiResponse<List<UserSessionRecordView>> sessionRecords(@PathVariable Long id) {
    requireUser(id);
    LambdaQueryWrapper<SessionOrder> wrapper = new LambdaQueryWrapper<SessionOrder>()
        .eq(SessionOrder::getUserId, id)
        .orderByDesc(SessionOrder::getStartTime);
    List<UserSessionRecordView> items = sessionService.list(wrapper).stream()
        .map(this::toSessionRecordView)
        .collect(Collectors.toList());
    return ApiResponse.success(items);
  }

  @DeleteMapping("/{id}")
  public ApiResponse<Boolean> delete(@PathVariable Long id) {
    requireUser(id);
    return ApiResponse.success(userService.removeById(id));
  }

  private LambdaQueryWrapper<User> buildListWrapper(String keyword, Integer status) {
    LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
    if (StringUtils.hasText(keyword)) {
      String normalizedKeyword = keyword.trim();
      if (DIGIT_PATTERN.matcher(normalizedKeyword).matches()) {
        if (normalizedKeyword.length() <= 11) {
          wrapper.and(query -> query
              .likeRight(User::getMobile, normalizedKeyword)
              .or()
              .likeRight(User::getIdCard, normalizedKeyword)
          );
        } else {
          wrapper.likeRight(User::getIdCard, normalizedKeyword);
        }
      } else {
        wrapper.likeRight(User::getName, normalizedKeyword);
      }
    }
    if (status != null) {
      wrapper.eq(User::getStatus, status);
    }
    wrapper.orderByDesc(User::getId);
    return wrapper;
  }

  private void validateStatus(Integer status) {
    if (status == null) {
      return;
    }
    if (status != STATUS_FROZEN && status != STATUS_ACTIVE) {
      throw new BusinessException(ResultCode.BAD_REQUEST, "status只能为0或1");
    }
  }

  private User requireUser(Long id) {
    User user = userService.getById(id);
    if (user == null) {
      throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
    }
    return user;
  }

  private void ensureMobileAvailable(String mobile, Long excludeId) {
    if (!StringUtils.hasText(mobile)) {
      throw new BusinessException(ResultCode.BAD_REQUEST, "mobile不能为空");
    }
    LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
        .eq(User::getMobile, mobile.trim());
    if (excludeId != null) {
      wrapper.ne(User::getId, excludeId);
    }
    boolean exists = userService.count(wrapper) > 0;
    if (exists) {
      throw new BusinessException(ResultCode.BAD_REQUEST, "手机号已存在");
    }
  }

  private void ensureIdCardAvailable(String idCard, Long excludeId) {
    if (!StringUtils.hasText(idCard)) {
      throw new BusinessException(ResultCode.BAD_REQUEST, "idCard不能为空");
    }
    LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
        .eq(User::getIdCard, idCard.trim());
    if (excludeId != null) {
      wrapper.ne(User::getId, excludeId);
    }
    boolean exists = userService.count(wrapper) > 0;
    if (exists) {
      throw new BusinessException(ResultCode.BAD_REQUEST, "身份证已存在");
    }
  }

  private BigDecimal defaultBalance(BigDecimal balance) {
    return balance == null ? BigDecimal.ZERO : balance;
  }

  private Long requireCurrentAdminId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || authentication.getDetails() == null) {
      throw new BusinessException(ResultCode.UNAUTHORIZED, "无法识别当前管理员");
    }
    try {
      return Long.valueOf(String.valueOf(authentication.getDetails()));
    } catch (NumberFormatException ex) {
      throw new BusinessException(ResultCode.UNAUTHORIZED, "无法识别当前管理员");
    }
  }

  private UserView toView(User user) {
    UserView view = new UserView();
    view.setId(user.getId());
    view.setName(user.getName());
    view.setMobile(user.getMobile());
    view.setMobileMasked(maskMobile(user.getMobile()));
    view.setIdCard(user.getIdCard());
    view.setIdCardMasked(maskIdCard(user.getIdCard()));
    view.setBalance(user.getBalance() == null ? BigDecimal.ZERO : user.getBalance());
    view.setStatus(user.getStatus());
    view.setStatusLabel(user.getStatus() != null && user.getStatus() == STATUS_ACTIVE ? "可用" : "冻结");
    view.setRegisterTime(user.getRegisterTime());
    view.setLastSessionTime(user.getLastSessionTime());
    view.setCreatedAt(user.getCreatedAt());
    view.setUpdatedAt(user.getUpdatedAt());
    return view;
  }

  private UserRechargeRecordView toRechargeRecordView(RechargeRecord record) {
    UserRechargeRecordView view = new UserRechargeRecordView();
    view.setId(record.getId());
    view.setUserId(record.getUserId());
    view.setAmount(record.getAmount());
    view.setChannel(record.getChannel());
    view.setRemark(record.getRemark());
    view.setOperatorAdminId(record.getOperatorAdminId());
    view.setCreatedAt(record.getCreatedAt());
    return view;
  }

  private UserSessionRecordView toSessionRecordView(SessionOrder order) {
    UserSessionRecordView view = new UserSessionRecordView();
    view.setId(order.getId());
    view.setUserId(order.getUserId());
    view.setMachineId(order.getMachineId());
    view.setDurationMinutes(order.getDurationMinutes());
    view.setPriceSnapshot(order.getPriceSnapshot());
    view.setAmount(order.getAmount());
    view.setStatus(order.getStatus());
    view.setStatusLabel(resolveSessionStatusLabel(order.getStatus()));
    view.setStartTime(order.getStartTime());
    view.setEndTime(order.getEndTime());
    return view;
  }

  private String resolveSessionStatusLabel(Integer status) {
    if (Objects.equals(status, 2)) {
      return "强制结束";
    }
    if (Objects.equals(status, 1)) {
      return "完成";
    }
    return "进行中";
  }

  private String maskMobile(String mobile) {
    if (!StringUtils.hasText(mobile) || mobile.length() < 7) {
      return mobile;
    }
    return mobile.substring(0, 3) + "****" + mobile.substring(mobile.length() - 4);
  }

  private String maskIdCard(String idCard) {
    if (!StringUtils.hasText(idCard) || idCard.length() < 8) {
      return idCard;
    }
    return idCard.substring(0, 3) + "****" + idCard.substring(idCard.length() - 4);
  }
}
