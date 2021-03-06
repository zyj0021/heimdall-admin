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

import com.luter.heimdall.admin.base.service.impl.BaseServiceImpl;
import com.luter.heimdall.admin.module.sys.dto.SysPostDTO;
import com.luter.heimdall.admin.module.sys.entity.SysPostEntity;
import com.luter.heimdall.admin.module.sys.mapper.SysPostMapper;
import com.luter.heimdall.admin.module.sys.repository.SysPostRepository;
import com.luter.heimdall.admin.module.sys.service.SysPostService;
import com.luter.heimdall.admin.module.sys.vo.SysPostVO;
import com.luter.heimdall.starter.model.pagination.PageDTO;
import com.luter.heimdall.starter.model.pagination.PagerVO;
import com.luter.heimdall.starter.utils.exception.LuterIllegalParameterException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class SysPostServiceImpl extends BaseServiceImpl implements SysPostService {

    private final SysPostRepository repository;
    private final SysPostMapper mapper;

    public SysPostServiceImpl(SysPostRepository repository, SysPostMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }


    @Override
    public PageDTO<SysPostDTO> list(SysPostVO param, PagerVO pagerVO) {
        Page<SysPostEntity> page = repository.findAll((Specification<SysPostEntity>) (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            //            if (null != param.getId()) {
            //                predicates.add(criteriaBuilder.equal(root.get(SysPostEntity_.ID), param.getId()));
            //            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        }, getPager(pagerVO));
        return toPageData(page.map(mapper::toDto));
    }

    @Override
    public SysPostDTO getById(Long id) {
        return mapper.toDto(getEntityById(SysPostEntity.class, id));
    }

    @Override
    public SysPostDTO save(SysPostVO param) {
        final SysPostEntity entity = repository.saveAndFlush(mapper.voToEntity(param));
        return mapper.toDto(entity);


    }

    @Override
    public void update(SysPostVO param) {
        SysPostEntity toUpdate = getEntityById(SysPostEntity.class, param.getId());
        toUpdate.setSeqNo(param.getSeqNo());
        updateEntity(toUpdate);
    }

    @Override
    public int deleteById(Long id) {
        if (null == id) {
            throw new LuterIllegalParameterException("ID????????????");
        }
        return deleteEntityById(SysPostEntity.class, id);
    }
}
