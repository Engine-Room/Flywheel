package com.jozufozu.flywheel.glsl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import com.jozufozu.flywheel.glsl.error.ErrorBuilder;
import com.jozufozu.flywheel.glsl.span.Span;
import com.jozufozu.flywheel.util.Pair;

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

	record MalformedInclude(ResourceLocationException exception) implements LoadError {
		@Override
		public ErrorBuilder generateMessage() {
			return ErrorBuilder.create()
					.error(exception.toString());
		}
	}
}
