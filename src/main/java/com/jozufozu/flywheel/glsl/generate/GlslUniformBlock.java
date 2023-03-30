package com.jozufozu.flywheel.glsl.generate;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.jozufozu.flywheel.util.Pair;
import com.jozufozu.flywheel.util.StringUtil;

public class GlslUniformBlock implements GlslBuilder.Declaration {


    private String qualifier;
    private int binding;
    private String name;
    private final List<Pair<String, String>> members = new ArrayList<>();

    @Override
    public String prettyPrint() {
        return """
            layout(%s, binding = %d) uniform %s {
            %s
            };
            """.formatted(qualifier, binding, name, StringUtil.indent(formatMembers(), 4));
    }

    private String formatMembers() {
        return members.stream()
            .map(p -> p.first() + " " + p.second() + ";")
            .collect(Collectors.joining("\n"));
    }

    public GlslUniformBlock layout(String qualifier) {
        this.qualifier = qualifier;
        return this;
    }

    public GlslUniformBlock binding(int i) {
        binding = i;
        return this;
    }

    public GlslUniformBlock name(String name) {
        this.name = name;
        return this;
    }

    public GlslUniformBlock member(String typeName, String variableName) {
        members.add(Pair.of(typeName, variableName));
        return this;
    }
}
