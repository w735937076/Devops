package com.drp.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.drp.common.constant.ResultCode;
import com.drp.common.exception.BusinessException;
import com.drp.common.util.BeanUtil;
import com.drp.server.dto.ServerGroupCreateRequest;
import com.drp.server.dto.ServerGroupDTO;
import com.drp.server.dto.ServerGroupUpdateRequest;
import com.drp.server.entity.ServerGroup;
import com.drp.server.repository.ServerGroupRepository;
import com.drp.server.service.ServerGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServerGroupServiceImpl implements ServerGroupService {

    private final ServerGroupRepository serverGroupRepository;

    @Override
    public List<ServerGroupDTO> list() {
        return serverGroupRepository.selectList(new LambdaQueryWrapper<ServerGroup>()
                        .orderByAsc(ServerGroup::getSort)
                        .orderByDesc(ServerGroup::getCreateTime))
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ServerGroupDTO getById(Long id) {
        ServerGroup group = serverGroupRepository.selectById(id);
        if (group == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "服务器分组不存在");
        }
        return toDTO(group);
    }

    @Override
    @Transactional
    public ServerGroupDTO create(ServerGroupCreateRequest request) {
        ServerGroup group = new ServerGroup();
        BeanUtil.copyProperties(request, group);
        serverGroupRepository.insert(group);
        return toDTO(group);
    }

    @Override
    @Transactional
    public ServerGroupDTO update(Long id, ServerGroupUpdateRequest request) {
        ServerGroup group = serverGroupRepository.selectById(id);
        if (group == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "服务器分组不存在");
        }

        BeanUtil.copyProperties(request, group);
        serverGroupRepository.updateById(group);
        return toDTO(group);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        serverGroupRepository.deleteById(id);
    }

    private ServerGroupDTO toDTO(ServerGroup group) {
        ServerGroupDTO dto = new ServerGroupDTO();
        BeanUtil.copyProperties(group, dto);
        return dto;
    }
}
