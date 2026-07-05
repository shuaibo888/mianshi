package com.example.miniats.auth;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.miniats.auth.dto.LoginResponse;
import com.example.miniats.auth.entity.SysUser;
import com.example.miniats.auth.mapper.SysUserMapper;
import com.example.miniats.security.JwtService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(SysUserMapper sysUserMapper, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.sysUserMapper = sysUserMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(String username, String password) {
        SysUser user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException("bad credentials");
        }
        return new LoginResponse(jwtService.createToken(user.getUsername(), user.getDisplayName()),
                user.getUsername(), user.getDisplayName());
    }
}
