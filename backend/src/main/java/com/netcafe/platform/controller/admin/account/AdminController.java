package com.netcafe.platform.controller.admin.account;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.netcafe.platform.common.ApiResponse;
import com.netcafe.platform.common.BusinessException;
import com.netcafe.platform.common.ResultCode;
import com.netcafe.platform.domain.dto.account.AdminCreateRequest;
import com.netcafe.platform.domain.dto.account.AdminListResponse;
import com.netcafe.platform.domain.dto.account.AdminUpdateRequest;
import com.netcafe.platform.domain.dto.account.AdminView;
import com.netcafe.platform.domain.entity.account.Admin;
import com.netcafe.platform.service.account.AdminService;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
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
  private final AdminService adminService;

  public AdminController(AdminService adminService) {
    this.adminService = adminService;
  }

  @GetMapping
  public ApiResponse<AdminListResponse> list(
      @RequestParam(required = false) String username,
      @RequestParam(required = false) Integer status,
      @RequestParam(defaultValue = "1") long page,
      @RequestParam(defaultValue = "20") long size,
      @RequestParam(defaultValue = "0") long minId
  ) {
    long pageSize = Math.max(1, Math.min(size, 200));
    Page<Admin> pageRequest = new Page<>(page, pageSize);
    LambdaQueryWrapper<Admin> wrapper = new LambdaQueryWrapper<>();
    wrapper.ge(Admin::getId, minId);
    if (StringUtils.hasText(username)) {
      wrapper.eq(Admin::getUsername, username);
    }
    if (status != null) {
      wrapper.eq(Admin::getStatus, status);
    }
    wrapper.orderByAsc(Admin::getId);
    Page<Admin> result = adminService.page(pageRequest, wrapper);
    List<AdminView> views = result.getRecords().stream()
        .map(this::toView)
        .collect(Collectors.toList());
    return ApiResponse.success(new AdminListResponse(result.getTotal(), views));
  }

  @PostMapping
  public ApiResponse<Boolean> create(@Valid @RequestBody AdminCreateRequest request) {
    ensureUsernameAvailable(request.getUsername());
    Admin admin = new Admin();
    admin.setUsername(request.getUsername());
    admin.setPassword(request.getPassword());
    admin.setName(request.getName());
    admin.setRole(request.getRole());
    admin.setStatus(request.getStatus() == null ? 1 : request.getStatus());
    admin.setCreatedAt(LocalDateTime.now());
    admin.setUpdatedAt(LocalDateTime.now());
    return ApiResponse.success(adminService.save(admin));
  }

  @PutMapping("/{id}")
  public ApiResponse<Boolean> update(@PathVariable Long id, @Valid @RequestBody AdminUpdateRequest request) {
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
    Admin admin = new Admin();
    admin.setId(id);
    admin.setPassword(request.getPassword());
    admin.setUpdatedAt(LocalDateTime.now());
    return ApiResponse.success(adminService.updateById(admin));
  }

  @DeleteMapping("/{id}")
  public ApiResponse<Boolean> delete(@PathVariable Long id) {
    return ApiResponse.success(adminService.removeById(id));
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

  private AdminView toView(Admin admin) {
    if (admin == null) {
      return null;
    }
    AdminView view = new AdminView();
    view.setId(admin.getId());
    view.setUsername(admin.getUsername());
    view.setName(admin.getName());
    view.setRole(admin.getRole());
    view.setStatus(admin.getStatus());
    view.setLastLoginTime(admin.getLastLoginTime());
    view.setCreatedAt(admin.getCreatedAt());
    view.setUpdatedAt(admin.getUpdatedAt());
    return view;
  }
}
