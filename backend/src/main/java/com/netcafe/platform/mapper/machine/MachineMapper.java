package com.netcafe.platform.mapper.machine;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.netcafe.platform.domain.entity.machine.Machine;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MachineMapper extends BaseMapper<Machine> {
}
