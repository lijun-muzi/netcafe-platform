package com.netcafe.platform.controller.admin.machine;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.netcafe.platform.common.ApiResponse;
import com.netcafe.platform.common.BusinessException;
import com.netcafe.platform.common.ResultCode;
import com.netcafe.platform.domain.dto.system.MachineTemplateCreateRequest;
import com.netcafe.platform.domain.dto.system.MachineTemplateUpdateRequest;
import com.netcafe.platform.domain.dto.system.MachineTemplateView;
import com.netcafe.platform.domain.entity.machine.Machine;
import com.netcafe.platform.domain.entity.machine.MachineTemplate;
import com.netcafe.platform.service.machine.MachineService;
import com.netcafe.platform.service.machine.MachineTemplateService;
import com.netcafe.platform.service.system.AuditLogService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/machine-templates")
public class MachineTemplateController {
  private final MachineTemplateService machineTemplateService;
  private final MachineService machineService;
  private final AuditLogService auditLogService;
  private final ObjectMapper objectMapper;

  public MachineTemplateController(
      MachineTemplateService machineTemplateService,
      MachineService machineService,
      AuditLogService auditLogService,
      ObjectMapper objectMapper
  ) {
    this.machineTemplateService = machineTemplateService;
    this.machineService = machineService;
    this.auditLogService = auditLogService;
    this.objectMapper = objectMapper;
  }

  @GetMapping
  public ApiResponse<List<MachineTemplateView>> list() {
    List<MachineTemplateView> items = machineTemplateService.list(new LambdaQueryWrapper<MachineTemplate>()
            .gt(MachineTemplate::getId, 0L)
            .orderByDesc(MachineTemplate::getId))
        .stream()
        .map(this::toView)
        .toList();
    return ApiResponse.success(items);
  }

  @GetMapping("/{id}")
  public ApiResponse<MachineTemplateView> detail(@PathVariable Long id) {
    return ApiResponse.success(toView(requireTemplate(id)));
  }

  @PostMapping
  public ApiResponse<Boolean> create(@Valid @RequestBody MachineTemplateCreateRequest request) {
    ensureTemplateNameAvailable(request.getName(), null);
    LocalDateTime now = LocalDateTime.now();
    MachineTemplate template = new MachineTemplate();
    template.setName(request.getName().trim());
    template.setConfigJson(request.getConfigJson().trim());
    template.setCreatedAt(now);
    template.setUpdatedAt(now);
    boolean saved = machineTemplateService.save(template);
    auditLogService.record(
        requireCurrentAdminId(),
        resolveCurrentAdminRole(),
        AuditLogService.ACTION_CREATE_MACHINE_TEMPLATE,
        "MACHINE_TEMPLATE",
        template.getId(),
        null,
        buildSnapshot(template)
    );
    return ApiResponse.success(saved);
  }

  @PutMapping("/{id}")
  public ApiResponse<Boolean> update(@PathVariable Long id, @Valid @RequestBody MachineTemplateUpdateRequest request) {
    MachineTemplate existing = requireTemplate(id);
    ensureTemplateNameAvailable(request.getName(), id);
    MachineTemplate template = new MachineTemplate();
    template.setId(id);
    template.setName(request.getName().trim());
    template.setConfigJson(request.getConfigJson().trim());
    template.setUpdatedAt(LocalDateTime.now());
    boolean updated = machineTemplateService.updateById(template);
    MachineTemplate latest = requireTemplate(id);
    auditLogService.record(
        requireCurrentAdminId(),
        resolveCurrentAdminRole(),
        AuditLogService.ACTION_UPDATE_MACHINE_TEMPLATE,
        "MACHINE_TEMPLATE",
        id,
        buildSnapshot(existing),
        buildSnapshot(latest)
    );
    return ApiResponse.success(updated);
  }

  @DeleteMapping("/{id}")
  public ApiResponse<Boolean> delete(@PathVariable Long id) {
    MachineTemplate existing = requireTemplate(id);
    if (machineService.count(new LambdaQueryWrapper<Machine>().eq(Machine::getTemplateId, id)) > 0) {
      throw new BusinessException(ResultCode.BAD_REQUEST, "模板已被机位使用，不能删除");
    }
    boolean removed = machineTemplateService.removeById(id);
    auditLogService.record(
        requireCurrentAdminId(),
        resolveCurrentAdminRole(),
        AuditLogService.ACTION_DELETE_MACHINE_TEMPLATE,
        "MACHINE_TEMPLATE",
        id,
        buildSnapshot(existing),
        null
    );
    return ApiResponse.success(removed);
  }

  private MachineTemplate requireTemplate(Long id) {
    MachineTemplate template = machineTemplateService.getById(id);
    if (template == null) {
      throw new BusinessException(ResultCode.NOT_FOUND, "机位模板不存在");
    }
    return template;
  }

  private void ensureTemplateNameAvailable(String name, Long excludeId) {
    if (!StringUtils.hasText(name)) {
      throw new BusinessException(ResultCode.BAD_REQUEST, "模板名称不能为空");
    }
    LambdaQueryWrapper<MachineTemplate> wrapper = new LambdaQueryWrapper<MachineTemplate>()
        .eq(MachineTemplate::getName, name.trim());
    if (excludeId != null) {
      wrapper.ne(MachineTemplate::getId, excludeId);
    }
    if (machineTemplateService.count(wrapper) > 0) {
      throw new BusinessException(ResultCode.BAD_REQUEST, "模板名称已存在");
    }
  }

  private MachineTemplateView toView(MachineTemplate template) {
    MachineTemplateView view = new MachineTemplateView();
    view.setId(template.getId());
    view.setName(template.getName());
    view.setConfigJson(template.getConfigJson());
    view.setConfigSummary(resolveConfigSummary(template.getConfigJson()));
    view.setCreatedAt(template.getCreatedAt());
    view.setUpdatedAt(template.getUpdatedAt());
    return view;
  }

  private String resolveConfigSummary(String configJson) {
    if (!StringUtils.hasText(configJson)) {
      return "";
    }
    try {
      Map<String, Object> map = objectMapper.readValue(configJson, new TypeReference<>() {
      });
      Object spec = map.get("spec");
      if (spec != null && StringUtils.hasText(String.valueOf(spec))) {
        return String.valueOf(spec);
      }
      List<String> parts = new ArrayList<>();
      addConfigPart(parts, map.get("cpu"));
      addConfigPart(parts, map.get("gpu"));
      addConfigPart(parts, map.get("memory"));
      if (!parts.isEmpty()) {
        return String.join(" / ", parts);
      }
    } catch (Exception ignored) {
      // 模板摘要仅用于页面展示，解析失败时回退原文。
    }
    return configJson;
  }

  private void addConfigPart(List<String> parts, Object value) {
    if (value != null && StringUtils.hasText(String.valueOf(value))) {
      parts.add(String.valueOf(value));
    }
  }

  private Map<String, Object> buildSnapshot(MachineTemplate template) {
    Map<String, Object> snapshot = new LinkedHashMap<>();
    snapshot.put("targetLabel", template.getName());
    snapshot.put("name", template.getName());
    snapshot.put("configJson", template.getConfigJson());
    snapshot.put("changeSummary", resolveConfigSummary(template.getConfigJson()));
    return snapshot;
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

  private String resolveCurrentAdminRole() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null) {
      throw new BusinessException(ResultCode.UNAUTHORIZED, "无法识别当前管理员");
    }
    return authentication.getAuthorities().stream()
        .map(authority -> authority.getAuthority())
        .filter(role -> role.startsWith("ROLE_"))
        .map(role -> role.substring(5))
        .findFirst()
        .orElseThrow(() -> new BusinessException(ResultCode.UNAUTHORIZED, "无法识别当前管理员"));
  }
}
