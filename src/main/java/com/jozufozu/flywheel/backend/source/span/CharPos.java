package com.jozufozu.flywheel.backend.source.span;

/**
 * A position in a file.
 */
public record CharPos(int pos, int line, int col) {
}
