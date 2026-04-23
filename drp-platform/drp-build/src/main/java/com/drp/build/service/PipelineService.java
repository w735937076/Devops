package com.drp.build.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.drp.build.entity.Pipeline;
import com.drp.build.repository.PipelineRepository;
import com.drp.common.exception.BusinessException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PipelineService {

    private final PipelineRepository pipelineRepository;

    public PipelineService(PipelineRepository pipelineRepository) {
        this.pipelineRepository = pipelineRepository;
    }

    public List<Pipeline> getPipelineList(Long projectId) {
        LambdaQueryWrapper<Pipeline> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Pipeline::getProjectId, projectId)
               .eq(Pipeline::getStatus, 1)
               .orderByDesc(Pipeline::getIsDefault)
               .orderByDesc(Pipeline::getId);
        return pipelineRepository.selectList(wrapper);
    }

    public Pipeline getDefaultPipeline(Long projectId) {
        LambdaQueryWrapper<Pipeline> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Pipeline::getProjectId, projectId)
               .eq(Pipeline::getIsDefault, 1)
               .eq(Pipeline::getStatus, 1);
        Pipeline pipeline = pipelineRepository.selectOne(wrapper);

        if (pipeline == null) {
            throw new BusinessException("项目未配置默认流水线");
        }
        return pipeline;
    }

    public Pipeline getById(Long id) {
        Pipeline pipeline = pipelineRepository.selectById(id);
        if (pipeline == null) {
            throw new BusinessException("流水线不存在");
        }
        return pipeline;
    }

    public Pipeline create(Pipeline pipeline) {
        pipelineRepository.insert(pipeline);
        return pipeline;
    }

    public Pipeline update(Pipeline pipeline) {
        pipelineRepository.updateById(pipeline);
        return pipeline;
    }

    public void delete(Long id) {
        pipelineRepository.deleteById(id);
    }
}
