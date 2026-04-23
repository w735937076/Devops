package com.drp.build.service.executor;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ExecutorFactory {

    private final Map<String, BuildExecutor> executors;

    public ExecutorFactory(List<BuildExecutor> executorList) {
        this.executors = executorList.stream()
                .collect(Collectors.toMap(BuildExecutor::getSupportedType, Function.identity()));
    }

    public BuildExecutor getExecutor(String projectType) {
        BuildExecutor executor = executors.get(projectType);
        if (executor == null) {
            throw new IllegalArgumentException("Unsupported project type: " + projectType);
        }
        return executor;
    }
}
