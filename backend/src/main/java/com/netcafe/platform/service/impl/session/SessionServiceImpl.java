package com.netcafe.platform.service.impl.session;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.netcafe.platform.domain.entity.session.SessionOrder;
import com.netcafe.platform.mapper.session.SessionOrderMapper;
import com.netcafe.platform.service.session.SessionService;
import org.springframework.stereotype.Service;

@Service
public class SessionServiceImpl extends ServiceImpl<SessionOrderMapper, SessionOrder> implements SessionService {
}
