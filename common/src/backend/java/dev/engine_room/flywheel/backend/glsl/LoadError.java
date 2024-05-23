package dev.engine_room.flywheel.backend.glsl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import dev.engine_room.flywheel.backend.glsl.error.ErrorBuilder;
import dev.engine_room.flywheel.backend.glsl.span.Span;
import dev.engine_room.flywheel.lib.util.Pair;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;

sealed public interface LoadError {
	ErrorBuilder generateMessage();

	record CircularDependency(ResourceLocation offender, List<ResourceLocation> stack) implements LoadError {
		public String format() {
			return stack.stream()
					.dropWhile(l -> !l.equals(offender))
					.map(ResourceLocation::toString)
					.collect(Collectors.joining(" -> "));
		}

		@Override
		public ErrorBuilder generateMessage() {
			return ErrorBuilder.create()
					.error("files are circularly dependent")
					.note(format());
		}
	}

	record IncludeError(ResourceLocation location, List<Pair<Span, LoadError>> innerErrors) implements LoadError {
		@Override
		public ErrorBuilder generateMessage() {
			var out = ErrorBuilder.create()
					.error("could not load \"" + location + "\"")
					.pointAtFile(location);

			for (var innerError : innerErrors) {
				var err = innerError.second()
						.generateMessage();
				out.pointAt(innerError.first())
						.nested(err);
			}

			return out;
		}
	}

	record IOError(ResourceLocation location, IOException exception) implements LoadError {
		@Override
		public ErrorBuilder generateMessage() {
			if (exception instanceof FileNotFoundException) {
				return ErrorBuilder.create()
						.error("\"" + location + "\" was not found");
			} else {
				return ErrorBuilder.create()
						.error("could not load \"" + location + "\" due to an IO error")
						.note(exception.toString());
			}
		}
	}

	record ResourceError(ResourceLocation location) implements LoadError {
		@Override
		public ErrorBuilder generateMessage() {
			return ErrorBuilder.create()
					.error("\"" + location + "\" was not found");
		}
	}

	record MalformedInclude(ResourceLocationException exception) implements LoadError {
		@Override
		public ErrorBuilder generateMessage() {
			return ErrorBuilder.create()
					.error(exception.toString());
		}
	}
}
