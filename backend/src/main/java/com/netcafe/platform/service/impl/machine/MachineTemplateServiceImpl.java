package com.netcafe.platform.service.impl.machine;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.netcafe.platform.domain.entity.machine.MachineTemplate;
import com.netcafe.platform.mapper.machine.MachineTemplateMapper;
import com.netcafe.platform.service.machine.MachineTemplateService;
import org.springframework.stereotype.Service;

@Service
public class MachineTemplateServiceImpl extends ServiceImpl<MachineTemplateMapper, MachineTemplate> implements MachineTemplateService {
}
