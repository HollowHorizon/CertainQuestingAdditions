package ru.hollowhorizon.additions.questing.registry;

//? if >= 1.21.11 {
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.VertexFormats;
//?} else {
/*import net.minecraft.client.gl.ShaderProgram;
*///?}
import net.minecraft.util.Identifier;
import ru.hollowhorizon.additions.questing.CertainQuestingAdditions;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class ModShaders {
    //? if >= 1.21.11 {
    private static final Map<Identifier, RenderPipeline> PROGRAMS = new LinkedHashMap<>();
    //?} else {
    /*private static final Map<Identifier, ShaderProgram> PROGRAMS = new LinkedHashMap<>();
    *///?}
    public static final Identifier DEFAULT_BACKGROUND_ID = Identifier.of(CertainQuestingAdditions.MOD_ID, "custom_background");

    private ModShaders() {
    }

    //? if >= 1.21.11 {
    public static RenderPipeline createPipeline(Identifier id) {
        Identifier shaderId = id == null ? DEFAULT_BACKGROUND_ID : id;
        return RenderPipeline.builder(RenderPipelines.TRANSFORMS_AND_PROJECTION_SNIPPET, RenderPipelines.GLOBALS_SNIPPET)
                .withLocation(pipelineId(shaderId))
                .withVertexShader(shaderPath(DEFAULT_BACKGROUND_ID))
                .withFragmentShader(shaderPath(shaderId))
                .withShaderDefine("CQA_RENDER_PIPELINE")
                .withBlend(BlendFunction.TRANSLUCENT)
                .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                .withVertexFormat(VertexFormats.POSITION_TEXTURE_COLOR, VertexFormat.DrawMode.QUADS)
                .build();
    }

    public static void register(Identifier id, RenderPipeline pipeline) {
        if (id == null || pipeline == null) {
            return;
        }

        PROGRAMS.put(id, pipeline);
    }

    public static RenderPipeline get(Identifier id) {
        if (id == null) {
            return PROGRAMS.get(DEFAULT_BACKGROUND_ID);
        }

        RenderPipeline pipeline = PROGRAMS.get(id);
        if (pipeline != null) {
            return pipeline;
        }

        return PROGRAMS.get(DEFAULT_BACKGROUND_ID);
    }

    private static Identifier pipelineId(Identifier shaderId) {
        return shaderId.withPath(path -> "pipeline/" + path);
    }

    private static Identifier shaderPath(Identifier shaderId) {
        return shaderId.withPath(path -> "core/" + path);
    }
    //?} else {
    /*public static void register(Identifier id, ShaderProgram program) {
        if (id == null || program == null) {
            return;
        }

        PROGRAMS.put(id, program);
    }

    public static ShaderProgram get(Identifier id) {
        if (id == null) {
            return PROGRAMS.get(DEFAULT_BACKGROUND_ID);
        }

        ShaderProgram shader = PROGRAMS.get(id);
        if (shader != null) {
            return shader;
        }

        return PROGRAMS.get(DEFAULT_BACKGROUND_ID);
    }
    *///?}

    public static boolean contains(Identifier id) {
        return PROGRAMS.containsKey(id);
    }

    public static Collection<Identifier> registeredIds() {
        return PROGRAMS.keySet();
    }
}
