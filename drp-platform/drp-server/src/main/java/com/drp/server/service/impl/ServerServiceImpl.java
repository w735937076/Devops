package com.drp.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.drp.common.constant.ResultCode;
import com.drp.common.dto.PageResponse;
import com.drp.common.exception.BusinessException;
import com.drp.common.util.AesEncryptUtil;
import com.drp.common.util.BeanUtil;
import com.drp.server.dto.*;
import com.drp.server.entity.Server;
import com.drp.server.enums.ServerStatus;
import com.drp.server.repository.ServerRepository;
import com.drp.server.service.ServerService;
import com.drp.server.service.SshService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServerServiceImpl implements ServerService {

    private final ServerRepository serverRepository;
    private final SshService sshService;

    @Override
    public PageResponse<ServerDTO> list(ServerQueryRequest request) {
        Page<Server> page = new Page<>(request.getPage(), request.getPageSize());
        LambdaQueryWrapper<Server> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.isNotBlank(request.getKeyword())) {
            wrapper.and(w -> w.like(Server::getName, request.getKeyword())
                    .or().like(Server::getHostname, request.getKeyword()));
        }
        if (StringUtils.isNotBlank(request.getGroup())) {
            wrapper.like(Server::getGroups, request.getGroup());
        }
        if (request.getStatus() != null) {
            wrapper.eq(Server::getStatus, request.getStatus());
        }

        wrapper.orderByDesc(Server::getCreateTime);
        IPage<Server> pageResult = serverRepository.selectPage(page, wrapper);

        List<ServerDTO> records = pageResult.getRecords().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return PageResponse.of(records, pageResult.getTotal(), (int) pageResult.getCurrent(), (int) pageResult.getSize());
    }

    @Override
    public ServerDTO getById(Integer id) {
        Server server = serverRepository.selectById(id);
        if (server == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "服务器不存在");
        }
        return toDTO(server);
    }

    @Override
    @Transactional
    public ServerDTO create(ServerCreateRequest request) {
        Server server = new Server();
        BeanUtil.copyProperties(request, server);

        if (StringUtils.isNotBlank(request.getPassword())) {
            server.setPassword(AesEncryptUtil.encrypt(request.getPassword()));
        }
        if (StringUtils.isNotBlank(request.getPrivateKey())) {
            server.setPrivateKey(AesEncryptUtil.encrypt(request.getPrivateKey()));
        }
        if (StringUtils.isNotBlank(request.getPrivateKeyPassphrase())) {
            server.setPrivateKeyPassphrase(AesEncryptUtil.encrypt(request.getPrivateKeyPassphrase()));
        }

        server.setStatus(ServerStatus.ONLINE.getValue());
        serverRepository.insert(server);
        return toDTO(server);
    }

    @Override
    @Transactional
    public ServerDTO update(Integer id, ServerUpdateRequest request) {
        Server server = serverRepository.selectById(id);
        if (server == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "服务器不存在");
        }

        BeanUtil.copyProperties(request, server);

        if (StringUtils.isNotBlank(request.getPassword()) && !request.getPassword().startsWith("encrypted:")) {
            server.setPassword(AesEncryptUtil.encrypt(request.getPassword()));
        }
        if (StringUtils.isNotBlank(request.getPrivateKey()) && !request.getPrivateKey().startsWith("encrypted:")) {
            server.setPrivateKey(AesEncryptUtil.encrypt(request.getPrivateKey()));
        }
        if (StringUtils.isNotBlank(request.getPrivateKeyPassphrase()) && !request.getPrivateKeyPassphrase().startsWith("encrypted:")) {
            server.setPrivateKeyPassphrase(AesEncryptUtil.encrypt(request.getPrivateKeyPassphrase()));
        }

        serverRepository.updateById(server);
        return toDTO(server);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        serverRepository.deleteById(id);
    }

    @Override
    public ConnectionTestResult testConnection(Integer id) {
        Server server = serverRepository.selectById(id);
        if (server == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "服务器不存在");
        }
        return sshService.testConnection(server);
    }

    @Override
    @Transactional
    public void updateHeartbeat(Integer id) {
        Server server = serverRepository.selectById(id);
        if (server != null) {
            server.setLastHeartbeat(LocalDateTime.now());
            server.setStatus(ServerStatus.ONLINE.getValue());
            serverRepository.updateById(server);
        }
    }

    @Override
    public List<ServerDTO> listAll() {
        return serverRepository.selectList(new LambdaQueryWrapper<Server>()
                        .orderByDesc(Server::getCreateTime))
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private ServerDTO toDTO(Server server) {
        ServerDTO dto = new ServerDTO();
        BeanUtil.copyProperties(server, dto);
        dto.setStatusDesc(ServerStatus.fromValue(server.getStatus()).getDesc());
        return dto;
    }
}
