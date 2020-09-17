package dev.tonysp.plugindata.data.pipelines;

import dev.tonysp.plugindata.data.exceptions.WrongPipelineManagerException;
import dev.tonysp.plugindata.data.pipelines.jedis.BatchPipelineManager;
import dev.tonysp.plugindata.data.pipelines.jedis.PubSubPipelineManager;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public enum Pipeline {
    PUBSUB  (PubSubPipelineManager.class),
    BATCH   (BatchPipelineManager.class),
    ;

    private final Class<? extends PipelineManager> pipelineManagerClass;
    private PipelineManager pipelineManagerObject;

    private static final Map<Class<? extends PipelineManager>, Pipeline> PIPELINE_MANAGER_CLASSES;
    static {
        Map<Class<? extends PipelineManager>, Pipeline> pipelineClassMap = new HashMap<>();
        for (Pipeline pipeline : Pipeline.values()) {
            pipelineClassMap.put(pipeline.getPipelineManagerClass(), pipeline);
        }
        PIPELINE_MANAGER_CLASSES = Collections.unmodifiableMap(pipelineClassMap);
    }

    <T extends PipelineManager> Pipeline (Class<T> pipelineManagerClass) {
        this.pipelineManagerClass = pipelineManagerClass;
    }

    public Class<? extends PipelineManager> getPipelineManagerClass () {
        return pipelineManagerClass;
    }

    public PipelineManager getPipelineManager () {
        return pipelineManagerObject;
    }

    public void setPipelineManagerObject (PipelineManager pipelineManagerObject) throws WrongPipelineManagerException {
        if (!pipelineManagerClass.isInstance(pipelineManagerObject)) {
            throw new WrongPipelineManagerException("This pipeline is only for " + pipelineManagerClass.getSimpleName());
        }
        this.pipelineManagerObject = pipelineManagerObject;
    }

    public static <T extends PipelineManager> Optional<Pipeline> getPipelineByManager (Class<T> pipelineManagerClass) {
        if (PIPELINE_MANAGER_CLASSES.containsKey(pipelineManagerClass)) {
            return Optional.of(PIPELINE_MANAGER_CLASSES.get(pipelineManagerClass));
        } else {
            return Optional.empty();
        }
    }
}
