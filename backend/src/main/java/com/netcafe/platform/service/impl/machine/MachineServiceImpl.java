package com.netcafe.platform.service.impl.machine;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.netcafe.platform.domain.entity.machine.Machine;
import com.netcafe.platform.mapper.machine.MachineMapper;
import com.netcafe.platform.service.machine.MachineService;
import org.springframework.stereotype.Service;

@Service
public class MachineServiceImpl extends ServiceImpl<MachineMapper, Machine> implements MachineService {
}
