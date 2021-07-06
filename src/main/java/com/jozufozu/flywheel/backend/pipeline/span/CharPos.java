package com.jozufozu.flywheel.backend.pipeline.span;

import com.jozufozu.flywheel.backend.pipeline.SourceFile;

public class CharPos {

	private final int idx;
	private final int line;
	private final int col;

	public CharPos(int idx, int line, int col) {
		this.idx = idx;
		this.line = line;
		this.col = col;
	}

	public int getPos() {
		return idx;
	}

	public int getLine() {
		return line;
	}

	public int getCol() {
		return col;
	}
}
