package com.netcafe.platform.mapper.session;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.netcafe.platform.domain.entity.session.SessionOrder;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SessionOrderMapper extends BaseMapper<SessionOrder> {
}
