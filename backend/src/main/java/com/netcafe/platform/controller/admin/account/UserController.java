package com.netcafe.platform.controller.admin.account;

import com.netcafe.platform.common.ApiResponse;
import com.netcafe.platform.domain.entity.account.User;
import com.netcafe.platform.service.account.UserService;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {
  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping
  public ApiResponse<List<User>> list() {
    return ApiResponse.success(userService.list());
  }

  @PostMapping
  public ApiResponse<Boolean> create(@RequestBody User user) {
    return ApiResponse.success(userService.save(user));
  }

  @PutMapping("/{id}")
  public ApiResponse<Boolean> update(@PathVariable Long id, @RequestBody User user) {
    user.setId(id);
    return ApiResponse.success(userService.updateById(user));
  }

  @PutMapping("/{id}/status")
  public ApiResponse<Boolean> updateStatus(@PathVariable Long id, @RequestBody User user) {
    user.setId(id);
    return ApiResponse.success(userService.updateById(user));
  }

  @DeleteMapping("/{id}")
  public ApiResponse<Boolean> delete(@PathVariable Long id) {
    return ApiResponse.success(userService.removeById(id));
  }
}
