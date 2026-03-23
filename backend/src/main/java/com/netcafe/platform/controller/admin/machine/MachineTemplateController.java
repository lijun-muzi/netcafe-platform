package com.netcafe.platform.controller.admin.machine;

import com.netcafe.platform.common.ApiResponse;
import com.netcafe.platform.domain.entity.machine.MachineTemplate;
import com.netcafe.platform.service.machine.MachineTemplateService;
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
@RequestMapping("/machine-templates")
public class MachineTemplateController {
  private final MachineTemplateService machineTemplateService;

  public MachineTemplateController(MachineTemplateService machineTemplateService) {
    this.machineTemplateService = machineTemplateService;
  }

  @GetMapping
  public ApiResponse<List<MachineTemplate>> list() {
    return ApiResponse.success(machineTemplateService.list());
  }

  @PostMapping
  public ApiResponse<Boolean> create(@RequestBody MachineTemplate template) {
    return ApiResponse.success(machineTemplateService.save(template));
  }

  @PutMapping("/{id}")
  public ApiResponse<Boolean> update(@PathVariable Long id, @RequestBody MachineTemplate template) {
    template.setId(id);
    return ApiResponse.success(machineTemplateService.updateById(template));
  }

  @DeleteMapping("/{id}")
  public ApiResponse<Boolean> delete(@PathVariable Long id) {
    return ApiResponse.success(machineTemplateService.removeById(id));
  }
}
