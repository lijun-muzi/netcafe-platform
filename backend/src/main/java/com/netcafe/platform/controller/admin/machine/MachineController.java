package com.netcafe.platform.controller.admin.machine;

import com.netcafe.platform.common.ApiResponse;
import com.netcafe.platform.domain.entity.machine.Machine;
import com.netcafe.platform.service.machine.MachineService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/machines")
public class MachineController {
  private final MachineService machineService;

  public MachineController(MachineService machineService) {
    this.machineService = machineService;
  }

  @GetMapping
  public ApiResponse<List<Machine>> list() {
    return ApiResponse.success(machineService.list());
  }

  @PostMapping
  public ApiResponse<Boolean> create(@RequestBody Machine machine) {
    return ApiResponse.success(machineService.save(machine));
  }

  @PutMapping("/{id}")
  public ApiResponse<Boolean> update(@PathVariable Long id, @RequestBody Machine machine) {
    machine.setId(id);
    return ApiResponse.success(machineService.updateById(machine));
  }

  @PutMapping("/{id}/status")
  public ApiResponse<Boolean> updateStatus(@PathVariable Long id, @RequestBody Machine machine) {
    machine.setId(id);
    return ApiResponse.success(machineService.updateById(machine));
  }

  @PutMapping("/{id}/price")
  public ApiResponse<Boolean> updatePrice(@PathVariable Long id, @RequestBody Machine machine) {
    machine.setId(id);
    return ApiResponse.success(machineService.updateById(machine));
  }

  @PostMapping("/batch-create")
  public ApiResponse<Boolean> batchCreate(@RequestBody List<Machine> machines) {
    return ApiResponse.success(machineService.saveBatch(machines));
  }
}
