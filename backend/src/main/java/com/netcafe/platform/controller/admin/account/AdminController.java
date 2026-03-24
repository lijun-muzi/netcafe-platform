package com.netcafe.platform.controller.admin.account;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.netcafe.platform.common.ApiResponse;
import com.netcafe.platform.common.BusinessException;
import com.netcafe.platform.common.ResultCode;
import com.netcafe.platform.domain.dto.account.AdminCreateRequest;
import com.netcafe.platform.domain.dto.account.AdminListResponse;
import com.netcafe.platform.domain.dto.account.AdminRoleOptionView;
import com.netcafe.platform.domain.dto.account.AdminUpdateRequest;
import com.netcafe.platform.domain.dto.account.AdminView;
import com.netcafe.platform.domain.entity.account.Admin;
import com.netcafe.platform.service.account.AdminService;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
@RequestMapping("/admins")
public class AdminController {
  private static final String ROLE_SUPER_ADMIN = "SUPER_ADMIN";
  private static final String ROLE_ADMIN = "ADMIN";
  private static final int STATUS_DISABLED = 0;
  private static final int STATUS_ENABLED = 1;
  private static final Map<String, String> ROLE_LABELS = Map.of(
      ROLE_SUPER_ADMIN, "超级管理员",
      ROLE_ADMIN, "管理员"
  );

  private final AdminService adminService;

  public AdminController(AdminService adminService) {
    this.adminService = adminService;
  }

  @GetMapping("/role-options")
  public ApiResponse<List<AdminRoleOptionView>> roleOptions() {
    return ApiResponse.success(List.of(
        new AdminRoleOptionView(ROLE_SUPER_ADMIN, "超级管理员", "拥有系统全部权限，可管理员工账号与系统配置"),
        new AdminRoleOptionView(ROLE_ADMIN, "管理员", "负责用户、机位和订单等日常运营管理")
    ));
  }

  @GetMapping("/{id}")
  public ApiResponse<AdminView> detail(@PathVariable Long id) {
    return ApiResponse.success(toView(requireAdmin(id)));
  }

  @GetMapping
  public ApiResponse<AdminListResponse> list(
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String role,
      @RequestParam(required = false) Integer status,
      @RequestParam(defaultValue = "1") long page,
      @RequestParam(defaultValue = "20") long size
  ) {
    validateRole(role);
    validateStatus(status);
    long pageSize = Math.max(1, Math.min(size, 200));
    Page<Admin> pageRequest = new Page<>(page, pageSize);
    LambdaQueryWrapper<Admin> wrapper = new LambdaQueryWrapper<>();
    if (StringUtils.hasText(keyword)) {
      String normalizedKeyword = keyword.trim();
      wrapper.and(query -> query
          .likeRight(Admin::getUsername, normalizedKeyword)
          .or()
          .likeRight(Admin::getName, normalizedKeyword)
      );
    }
    if (StringUtils.hasText(role)) {
      wrapper.eq(Admin::getRole, role);
    }
    if (status != null) {
      wrapper.eq(Admin::getStatus, status);
    }
    wrapper.orderByDesc(Admin::getId);
    Page<Admin> result = adminService.page(pageRequest, wrapper);
    List<AdminView> views = result.getRecords().stream()
        .map(this::toView)
        .collect(Collectors.toList());
    return ApiResponse.success(new AdminListResponse(
        result.getTotal(),
        result.getCurrent(),
        result.getSize(),
        views
    ));
  }

  @PostMapping
  public ApiResponse<Boolean> create(@Valid @RequestBody AdminCreateRequest request) {
    validateRole(request.getRole());
    validateStatus(request.getStatus());
    ensureUsernameAvailable(request.getUsername());
    Admin admin = new Admin();
    admin.setUsername(request.getUsername());
    admin.setPassword(request.getPassword());
    admin.setName(request.getName());
    admin.setRole(request.getRole());
    admin.setStatus(request.getStatus() == null ? STATUS_ENABLED : request.getStatus());
    admin.setCreatedAt(LocalDateTime.now());
    admin.setUpdatedAt(LocalDateTime.now());
    return ApiResponse.success(adminService.save(admin));
  }

  @PutMapping("/{id}")
  public ApiResponse<Boolean> update(@PathVariable Long id, @Valid @RequestBody AdminUpdateRequest request) {
    Admin existingAdmin = requireAdmin(id);
    validateRole(request.getRole());
    validateStatus(request.getStatus());
    ensureEnabledSuperAdminRetained(existingAdmin, request.getRole(), request.getStatus());
    Admin admin = new Admin();
    admin.setId(id);
    if (StringUtils.hasText(request.getPassword())) {
      admin.setPassword(request.getPassword());
    }
    if (StringUtils.hasText(request.getName())) {
      admin.setName(request.getName());
    }
    if (StringUtils.hasText(request.getRole())) {
      admin.setRole(request.getRole());
    }
    if (request.getStatus() != null) {
      admin.setStatus(request.getStatus());
    }
    admin.setUpdatedAt(LocalDateTime.now());
    return ApiResponse.success(adminService.updateById(admin));
  }

  @PutMapping("/{id}/status")
  public ApiResponse<Boolean> updateStatus(@PathVariable Long id, @RequestBody AdminUpdateRequest request) {
    if (request.getStatus() == null) {
      throw new BusinessException(ResultCode.BAD_REQUEST, "status不能为空");
    }
    validateStatus(request.getStatus());
    Admin existingAdmin = requireAdmin(id);
    ensureEnabledSuperAdminRetained(existingAdmin, null, request.getStatus());
    Admin admin = new Admin();
    admin.setId(id);
    admin.setStatus(request.getStatus());
    admin.setUpdatedAt(LocalDateTime.now());
    return ApiResponse.success(adminService.updateById(admin));
  }

  @PutMapping("/{id}/password-reset")
  public ApiResponse<Boolean> resetPassword(@PathVariable Long id, @RequestBody AdminUpdateRequest request) {
    if (!StringUtils.hasText(request.getPassword())) {
      throw new BusinessException(ResultCode.BAD_REQUEST, "password不能为空");
    }
    requireAdmin(id);
    Admin admin = new Admin();
    admin.setId(id);
    admin.setPassword(request.getPassword());
    admin.setUpdatedAt(LocalDateTime.now());
    return ApiResponse.success(adminService.updateById(admin));
  }

  @DeleteMapping("/{id}")
  public ApiResponse<Boolean> delete(@PathVariable Long id) {
    Admin existingAdmin = requireAdmin(id);
    ensureEnabledSuperAdminRetained(existingAdmin, ROLE_ADMIN, STATUS_DISABLED);
    return ApiResponse.success(adminService.removeById(id));
  }

  private void validateRole(String role) {
    if (role == null) {
      return;
    }
    if (!ROLE_LABELS.containsKey(role)) {
      throw new BusinessException(ResultCode.BAD_REQUEST, "role不合法");
    }
  }

  private void validateStatus(Integer status) {
    if (status == null) {
      return;
    }
    if (status != STATUS_DISABLED && status != STATUS_ENABLED) {
      throw new BusinessException(ResultCode.BAD_REQUEST, "status只能为0或1");
    }
  }

  private void ensureUsernameAvailable(String username) {
    if (!StringUtils.hasText(username)) {
      throw new BusinessException(ResultCode.BAD_REQUEST, "username不能为空");
    }
    boolean exists = adminService.count(
        new LambdaQueryWrapper<Admin>().eq(Admin::getUsername, username)
    ) > 0;
    if (exists) {
      throw new BusinessException(ResultCode.BAD_REQUEST, "账号已存在");
    }
  }

  private Admin requireAdmin(Long id) {
    Admin admin = adminService.getById(id);
    if (admin == null) {
      throw new BusinessException(ResultCode.NOT_FOUND, "管理员不存在");
    }
    return admin;
  }

  private void ensureEnabledSuperAdminRetained(Admin existingAdmin, String newRole, Integer newStatus) {
    String targetRole = StringUtils.hasText(newRole) ? newRole : existingAdmin.getRole();
    Integer targetStatus = newStatus != null ? newStatus : existingAdmin.getStatus();
    boolean enabledSuperAdminBefore = ROLE_SUPER_ADMIN.equals(existingAdmin.getRole())
        && existingAdmin.getStatus() != null
        && existingAdmin.getStatus() == STATUS_ENABLED;
    boolean enabledSuperAdminAfter = ROLE_SUPER_ADMIN.equals(targetRole)
        && targetStatus != null
        && targetStatus == STATUS_ENABLED;
    if (!enabledSuperAdminBefore || enabledSuperAdminAfter) {
      return;
    }
    long remainCount = adminService.count(new LambdaQueryWrapper<Admin>()
        .eq(Admin::getRole, ROLE_SUPER_ADMIN)
        .eq(Admin::getStatus, STATUS_ENABLED)
        .ne(Admin::getId, existingAdmin.getId()));
    if (remainCount < 1) {
      throw new BusinessException(ResultCode.BAD_REQUEST, "至少保留一个启用中的超级管理员");
    }
  }

  private AdminView toView(Admin admin) {
    if (admin == null) {
      return null;
    }
    AdminView view = new AdminView();
    view.setId(admin.getId());
    view.setUsername(admin.getUsername());
    view.setName(admin.getName());
    view.setRole(admin.getRole());
    view.setRoleLabel(ROLE_LABELS.getOrDefault(admin.getRole(), admin.getRole()));
    view.setStatus(admin.getStatus());
    view.setStatusLabel(admin.getStatus() != null && admin.getStatus() == STATUS_ENABLED ? "启用" : "禁用");
    view.setLastLoginTime(admin.getLastLoginTime());
    view.setCreatedAt(admin.getCreatedAt());
    view.setUpdatedAt(admin.getUpdatedAt());
    return view;
  }
}
