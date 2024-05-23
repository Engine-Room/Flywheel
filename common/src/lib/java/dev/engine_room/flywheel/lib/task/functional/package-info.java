/**
 * Functional interfaces accepting a context object for use with {@link dev.engine_room.flywheel.api.task.Plan Plans}.
 * <br>
 * Each interface in this package has a subinterface that ignores the context object. Plans then call the parent
 * interface, but do not need to create additional closure objects to translate when the consumer wishes to ignore
 * the context object.
 */
@ParametersAreNonnullByDefault
@FieldsAreNonnullByDefault
@MethodsReturnNonnullByDefault
package dev.engine_room.flywheel.lib.task.functional;

import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
