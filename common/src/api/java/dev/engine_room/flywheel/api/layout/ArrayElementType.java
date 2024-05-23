package dev.engine_room.flywheel.api.layout;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Range;

@ApiStatus.NonExtendable
public non-sealed interface ArrayElementType extends ElementType {
	ElementType innerType();

	@Range(from = 1, to = 256)
	int length();
}
