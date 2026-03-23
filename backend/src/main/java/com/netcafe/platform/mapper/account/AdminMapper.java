package com.netcafe.platform.mapper.account;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.netcafe.platform.domain.entity.account.Admin;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AdminMapper extends BaseMapper<Admin> {
}
