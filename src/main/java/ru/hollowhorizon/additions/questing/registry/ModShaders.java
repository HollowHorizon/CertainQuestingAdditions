package ru.hollowhorizon.additions.questing.registry;

import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.util.Identifier;
import ru.hollowhorizon.additions.questing.CertainQuestingAdditions;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;


public class ModShaders {
    private static final Map<Identifier, ShaderProgram> PROGRAMS = new LinkedHashMap<>();
    public static final Identifier DEFAULT_BACKGROUND_ID = Identifier.of(CertainQuestingAdditions.MOD_ID, "custom_background");

    private ModShaders() {
    }

    public static void register(Identifier id, ShaderProgram program) {
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

    public static boolean contains(Identifier id) {
        return PROGRAMS.containsKey(id);
    }

    public static Collection<Identifier> registeredIds() {
        return PROGRAMS.keySet();
    }


}
