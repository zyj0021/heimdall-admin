/*
 *
 *  *
 *  *
 *  *      Copyright 2020-2021 Luter.me
 *  *
 *  *      Licensed under the Apache License, Version 2.0 (the "License");
 *  *      you may not use this file except in compliance with the License.
 *  *      You may obtain a copy of the License at
 *  *
 *  *        http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *      Unless required by applicable law or agreed to in writing, software
 *  *      distributed under the License is distributed on an "AS IS" BASIS,
 *  *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *      See the License for the specific language governing permissions and
 *  *      limitations under the License.
 *  *
 *  *
 *
 */

package com.luter.heimdall.admin.module.sys.service.impl;

import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Sets;
import com.luter.heimdall.admin.base.service.impl.BaseServiceImpl;
import com.luter.heimdall.admin.module.security.details.AppUserDetails;
import com.luter.heimdall.admin.module.security.encoder.PasswordEncoder;
import com.luter.heimdall.admin.module.sys.dto.SysUserDTO;
import com.luter.heimdall.admin.module.sys.entity.SysUserEntity;
import com.luter.heimdall.admin.module.sys.entity.SysUserEntity_;
import com.luter.heimdall.admin.module.sys.mapper.SysDepartmentMapper;
import com.luter.heimdall.admin.module.sys.mapper.SysPostMapper;
import com.luter.heimdall.admin.module.sys.mapper.SysRoleMapper;
import com.luter.heimdall.admin.module.sys.mapper.SysUserMapper;
import com.luter.heimdall.admin.module.sys.repository.SysUserRepository;
import com.luter.heimdall.admin.module.sys.service.SysUserService;
import com.luter.heimdall.admin.module.sys.vo.SysUserVO;
import com.luter.heimdall.core.exception.AccountException;
import com.luter.heimdall.core.exception.HeimdallException;
import com.luter.heimdall.core.manager.AuthenticationManager;
import com.luter.heimdall.core.manager.limiter.LoginPasswordRetryLimit;
import com.luter.heimdall.core.session.SimpleSession;
import com.luter.heimdall.starter.captcha.config.CaptchaConfig;
import com.luter.heimdall.starter.captcha.service.CaptchaService;
import com.luter.heimdall.starter.model.pagination.PageDTO;
import com.luter.heimdall.starter.model.pagination.PagerVO;
import com.luter.heimdall.starter.utils.context.BaseContextHolder;
import com.luter.heimdall.starter.utils.exception.LuterIllegalParameterException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class SysUserServiceImpl extends BaseServiceImpl implements SysUserService {
    private final SysUserRepository repository;
    private final SysUserMapper mapper;
    private final SysRoleMapper roleMapper;
    private final SysPostMapper postMapper;
    private final SysDepartmentMapper departmentMapper;
    private final CaptchaConfig captchaConfig;
    private final CaptchaService captchaService;
    private final LoginPasswordRetryLimit retryLimit;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;


    @Override
    public PageDTO<SysUserDTO> list(SysUserVO param, PagerVO pagerVO) {
        Page<SysUserEntity> page = repository.findAll((Specification<SysUserEntity>) (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StrUtil.isNotEmpty(param.getUsername())) {
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(root.get(SysUserEntity_.USERNAME), "%" + param.getUsername() + "%"),
                        criteriaBuilder.like(root.get(SysUserEntity_.NICK_NAME), "%" + param.getUsername() + "%"),
                        criteriaBuilder.like(root.get(SysUserEntity_.REAL_NAME), "%" + param.getUsername() + "%")
                ));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        }, getPager(pagerVO));
        return toPageData(page, SysUserDTO.class);
    }

    @Override
    public SysUserDTO getById(Long id) {
        return mapper.toDto(getEntityById(SysUserEntity.class, id));
    }


    @Override
    public SysUserDTO save(SysUserVO param) {
        SysUserEntity toSave = mapper.voToEntity(param);
        toSave.setPassword(passwordEncoder.encode(toSave.getPassword()));
        if (null != param.getUserRoles() && !param.getUserRoles().isEmpty()) {
            toSave.setRoles(Sets.newHashSet(roleMapper.voListToEntityList(param.getUserRoles())));
        }
        if (null != param.getPosts() && !param.getPosts().isEmpty()) {
            toSave.setPosts(Sets.newHashSet(postMapper.voListToEntityList(param.getPosts())));
        }
        if (null != param.getDepartment()) {
            toSave.setDepartment(departmentMapper.voToEntity(param.getDepartment()));
        }
        final SysUserEntity entity = repository.saveAndFlush(toSave);
        return mapper.toDto(entity);
    }

    @Override

    public void update(SysUserVO param) {
        SysUserEntity toUpdate = getEntityById(SysUserEntity.class, param.getId());
        toUpdate.setNickName(param.getNickName());
        toUpdate.setRealName(param.getRealName());
        toUpdate.setRemarks(param.getRemarks());
        toUpdate.setCellPhone(param.getCellPhone());
        toUpdate.setGender(param.getGender());
        if (null != param.getUserRoles() && !param.getUserRoles().isEmpty()) {
            toUpdate.setRoles(Sets.newHashSet(roleMapper.voListToEntityList(param.getUserRoles())));
            authenticationManager.getSessionDAO().clearAllUserAuthorities();
        }
        if (null != param.getPosts() && !param.getPosts().isEmpty()) {
            toUpdate.setPosts(Sets.newHashSet(postMapper.voListToEntityList(param.getPosts())));
        }
        if (null != param.getDepartment()) {
            toUpdate.setDepartment(departmentMapper.voToEntity(param.getDepartment()));
        }
        updateEntity(toUpdate);
    }

    @Override
    public void updateUserInfo(SysUserEntity user) {
        updateEntity(user);
    }

    @Override
    public int deleteById(Long id) {
        if (null == id) {
            throw new LuterIllegalParameterException("ID????????????");
        }
        return deleteEntityById(SysUserEntity.class, id);
    }

    @Override
    public void resetPassword(SysUserVO param) {
        if (StrUtil.isEmpty(param.getPassword())) {
            throw new LuterIllegalParameterException("???????????????");
        }
        SysUserEntity user = getEntityById(SysUserEntity.class, param.getId());
        user.setPassword(passwordEncoder.encode(param.getPassword()));
        updateEntity(user);
    }

    @Override
    public Serializable login(SysUserVO user) {
        if (StrUtil.isBlank(user.getUsername()) || StrUtil.isBlank(user.getPassword())) {
            throw new AccountException("?????????????????????????????????,?????????");
        }
        final SysUserEntity sysUserEntityByUsername = repository.findSysUserEntityByUsername(user.getUsername());
        if (null == sysUserEntityByUsername) {
            throw new AccountException("?????????????????????:Non");
        }
        if (sysUserEntityByUsername.getLocked()) {
            throw new HeimdallException("???????????????,????????????????????????");
        }
        if (captchaConfig.isEnabled()) {
            if (!captchaService.checkCaptcha(user.getUuid(), user.getCaptcha())) {
                throw new HeimdallException("???????????????");
            }
        }
        if (!passwordEncoder.matches(user.getPassword(), sysUserEntityByUsername.getPassword())) {
            retryLimit.increase(sysUserEntityByUsername.getUsername());
            final int leftCount = retryLimit.leftCount(sysUserEntityByUsername.getUsername());
            if (leftCount == 0) {
                throw new HeimdallException("?????????????????????,????????????????????????????????????,?????????????????????....");
            }
            throw new HeimdallException("?????????????????????");
        }
        final SysUserDTO userDTO = mapper.toDto(sysUserEntityByUsername);
        userDTO.setRoles(null);
        AppUserDetails userDetails = new AppUserDetails(userDTO);
        final SimpleSession session = authenticationManager.login(userDetails);
        retryLimit.remove(sysUserEntityByUsername.getUsername());
        BaseContextHolder.setUserId(userDTO.getId());
        return session.getId();
    }

    @Override
    public void logout() {
        authenticationManager.logout();
        BaseContextHolder.setUserId(null);
    }
}
