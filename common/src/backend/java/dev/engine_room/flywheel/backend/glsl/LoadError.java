package dev.engine_room.flywheel.backend.glsl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import com.mojang.datafixers.util.Pair;

import dev.engine_room.flywheel.backend.glsl.error.ErrorBuilder;
import dev.engine_room.flywheel.backend.glsl.span.Span;
import net.minecraft.IdentifierException;
import net.minecraft.resources.Identifier;

sealed public interface LoadError {
	ErrorBuilder generateMessage();

	record CircularDependency(Identifier offender, List<Identifier> stack) implements LoadError {
		public String format() {
			return stack.stream()
					.dropWhile(id -> !id.equals(offender))
					.map(Identifier::toString)
					.collect(Collectors.joining(" -> "));
		}

		@Override
		public ErrorBuilder generateMessage() {
			return ErrorBuilder.create()
					.error("files are circularly dependent")
					.note(format());
		}
	}

	record IncludeError(Identifier id, List<Pair<Span, LoadError>> innerErrors) implements LoadError {
		@Override
		public ErrorBuilder generateMessage() {
			var out = ErrorBuilder.create()
					.error("could not load \"" + id + "\"")
					.pointAtFile(id);

			for (var innerError : innerErrors) {
				var err = innerError.getSecond()
						.generateMessage();
				out.pointAt(innerError.getFirst())
						.nested(err);
			}

			return out;
		}
	}

	record IOError(Identifier id, IOException exception) implements LoadError {
		@Override
		public ErrorBuilder generateMessage() {
			if (exception instanceof FileNotFoundException) {
				return ErrorBuilder.create()
						.error("\"" + id + "\" was not found");
			} else {
				return ErrorBuilder.create()
						.error("could not load \"" + id + "\" due to an IO error")
						.note(exception.toString());
			}
		}
	}

	record ResourceError(Identifier id) implements LoadError {
		@Override
		public ErrorBuilder generateMessage() {
			return ErrorBuilder.create()
					.error("\"" + id + "\" was not found");
		}
	}

	record MalformedInclude(IdentifierException exception) implements LoadError {
		@Override
		public ErrorBuilder generateMessage() {
			return ErrorBuilder.create()
					.error(exception.toString());
		}
	}
}
