package com.netcafe.platform.service.impl.account;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.netcafe.platform.domain.entity.account.User;
import com.netcafe.platform.mapper.account.UserMapper;
import com.netcafe.platform.service.account.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
