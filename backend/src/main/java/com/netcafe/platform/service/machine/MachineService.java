package com.netcafe.platform.service.machine;

import com.baomidou.mybatisplus.extension.service.IService;
import com.netcafe.platform.domain.dto.machine.MachineBatchCreateRequest;
import com.netcafe.platform.domain.entity.machine.Machine;
import java.math.BigDecimal;

public interface MachineService extends IService<Machine> {
  boolean updatePrice(Long machineId, BigDecimal newPrice, Long operatorAdminId);

  boolean batchCreate(MachineBatchCreateRequest request);
}
