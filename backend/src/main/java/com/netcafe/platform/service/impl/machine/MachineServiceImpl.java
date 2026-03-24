package com.netcafe.platform.service.impl.machine;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.netcafe.platform.common.BusinessException;
import com.netcafe.platform.common.ResultCode;
import com.netcafe.platform.domain.dto.machine.MachineBatchCreateRequest;
import com.netcafe.platform.domain.entity.machine.Machine;
import com.netcafe.platform.domain.entity.machine.MachineTemplate;
import com.netcafe.platform.domain.entity.machine.PriceChangeLog;
import com.netcafe.platform.mapper.machine.MachineMapper;
import com.netcafe.platform.mapper.machine.MachineTemplateMapper;
import com.netcafe.platform.mapper.machine.PriceChangeLogMapper;
import com.netcafe.platform.service.machine.MachineService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class MachineServiceImpl extends ServiceImpl<MachineMapper, Machine> implements MachineService {
  private static final int STATUS_IDLE = 0;
  private static final int STATUS_USING = 1;
  private static final BigDecimal DEFAULT_PRICE = new BigDecimal("0.1000");

  private final MachineTemplateMapper machineTemplateMapper;
  private final PriceChangeLogMapper priceChangeLogMapper;

  public MachineServiceImpl(
      MachineTemplateMapper machineTemplateMapper,
      PriceChangeLogMapper priceChangeLogMapper
  ) {
    this.machineTemplateMapper = machineTemplateMapper;
    this.priceChangeLogMapper = priceChangeLogMapper;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public boolean updatePrice(Long machineId, BigDecimal newPrice, Long operatorAdminId) {
    Machine machine = getById(machineId);
    if (machine == null) {
      throw new BusinessException(ResultCode.NOT_FOUND, "机位不存在");
    }
    if (newPrice == null || newPrice.compareTo(BigDecimal.ZERO) <= 0) {
      throw new BusinessException(ResultCode.BAD_REQUEST, "pricePerMin必须大于0");
    }
    BigDecimal oldPrice = machine.getPricePerMin();
    machine.setPricePerMin(newPrice);
    machine.setUpdatedAt(LocalDateTime.now());
    boolean updated = updateById(machine);
    if (!updated) {
      return false;
    }
    if (oldPrice == null || oldPrice.compareTo(newPrice) != 0) {
      PriceChangeLog log = new PriceChangeLog();
      log.setMachineId(machineId);
      log.setOldPrice(oldPrice);
      log.setNewPrice(newPrice);
      log.setOperatorAdminId(operatorAdminId);
      log.setCreatedAt(LocalDateTime.now());
      priceChangeLogMapper.insert(log);
    }
    return true;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public boolean batchCreate(MachineBatchCreateRequest request) {
    MachineTemplate template = machineTemplateMapper.selectById(request.getTemplateId());
    if (template == null) {
      throw new BusinessException(ResultCode.BAD_REQUEST, "机位模板不存在");
    }
    String codePrefix = request.getCodePrefix().trim();
    if (!StringUtils.hasText(codePrefix)) {
      throw new BusinessException(ResultCode.BAD_REQUEST, "codePrefix不能为空");
    }
    int codeWidth = request.getCodeWidth() == null ? 3 : request.getCodeWidth();
    List<String> codes = new ArrayList<>();
    Set<String> duplicateCheck = new HashSet<>();
    for (int i = 0; i < request.getCount(); i++) {
      String code = codePrefix + String.format("%0" + codeWidth + "d", request.getStartNo() + i);
      if (!duplicateCheck.add(code)) {
        throw new BusinessException(ResultCode.BAD_REQUEST, "生成了重复的机位编号");
      }
      codes.add(code);
    }

    long existsCount = count(new LambdaQueryWrapper<Machine>().in(Machine::getCode, codes));
    if (existsCount > 0) {
      throw new BusinessException(ResultCode.BAD_REQUEST, "批量建机位失败，存在重复编号");
    }

    BigDecimal price = request.getPricePerMin() == null ? DEFAULT_PRICE : request.getPricePerMin();
    LocalDateTime now = LocalDateTime.now();
    List<Machine> machines = new ArrayList<>();
    for (String code : codes) {
      Machine machine = new Machine();
      machine.setCode(code);
      machine.setStatus(STATUS_IDLE);
      machine.setPricePerMin(price);
      machine.setConfigJson(template.getConfigJson());
      machine.setTemplateId(template.getId());
      machine.setLastMaintainedAt(now);
      machine.setCreatedAt(now);
      machine.setUpdatedAt(now);
      machines.add(machine);
    }
    return saveBatch(machines);
  }
}
