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

package com.luter.heimdall.admin.module.security.service;

import cn.hutool.core.util.StrUtil;
import com.luter.heimdall.admin.module.security.details.AppUserDetails;
import com.luter.heimdall.admin.module.sys.dto.SysUserDTO;
import com.luter.heimdall.admin.module.sys.entity.SysResourceEntity;
import com.luter.heimdall.admin.module.sys.entity.SysUserEntity;
import com.luter.heimdall.admin.module.sys.repository.SysResourceRepository;
import com.luter.heimdall.admin.module.sys.repository.SysUserRepository;
import com.luter.heimdall.core.authorization.authority.GrantedAuthority;
import com.luter.heimdall.core.authorization.authority.SimpleGrantedAuthority;
import com.luter.heimdall.core.authorization.service.AuthorizationMetaDataService;
import com.luter.heimdall.core.exception.AccountException;
import com.luter.heimdall.core.session.SimpleSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthorizationMetaDataServiceImpl implements AuthorizationMetaDataService {
    private final SysResourceRepository sysResourceRepository;
    private final SysUserRepository sysUserRepository;

    @Override
    public Map<String, Collection<String>> loadSysAuthorities() {
        final List<SysResourceEntity> objects = sysResourceRepository.loadSysPermissions();
        if (null != objects && !objects.isEmpty()) {
            Map<String, Collection<String>> perms = new LinkedHashMap<>();
            for (SysResourceEntity resource : objects) {
                if (StrUtil.isNotBlank(resource.getUri()) && StrUtil.isNotBlank(resource.getPerm())) {
                    perms.put(resource.getUri(), Collections.singletonList(resource.getPerm()));
                }

            }
            return perms;
        }
        return new LinkedHashMap<>();
    }

    @Override
    public List<? extends GrantedAuthority> loadUserAuthorities(SimpleSession session) {
        final AppUserDetails details = (AppUserDetails) session.getDetails();
        final SysUserDTO user = details.getUser();
        final Optional<SysUserEntity> byId = sysUserRepository.findById(user.getId());
        if (!byId.isPresent()) {
            throw new AccountException("????????????????????????");
        }

        final List<SysResourceEntity> sysResourceEntities = sysResourceRepository.loadUserPermissionsByUserId(byId.get().getId());
        if (null != sysResourceEntities && !sysResourceEntities.isEmpty()) {
            return sysResourceEntities.stream().map(d -> new SimpleGrantedAuthority(d.getPerm())).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
