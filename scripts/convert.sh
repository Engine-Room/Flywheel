#!/bin/sh

for file in $(find ../src/main/java -type f -name "*.java"); do
	sed -i \
		-e 's/@OnlyIn/@Environment/g' \
		-e 's/Dist\.CLIENT/EnvType\.CLIENT/g' \
		-e 's/EnvType\.DEDICATED_SERVER/EnvType\.SERVER/g' \
		-e 's/import net\.minecraftforge\.api\.distmarker\.OnlyIn;/import net\.fabricmc\.api\.Environment;/g' \
		-e 's/import net\.minecraftforge\.api\.distmarker\.Dist;/import net\.fabricmc\.api\.EnvType;/g' \
		\
		-e 's/@Nonnull/@NotNull/g' \
		-e 's/import javax\.annotation\.Nullable;/import org\.jetbrains\.annotations\.Nullable;/g' \
		-e 's/import javax\.annotation\.Nonnull;/import org\.jetbrains\.annotations\.NotNull;/g' \
	$file

	echo "Converted $file"
done

echo 'Done'
