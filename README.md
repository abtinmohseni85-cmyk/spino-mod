# JP3 Spinosaurus (Forge 1.20.1)

One dinosaur, done properly: the Jurassic Park III Spinosaurus.

## Build
1. Download the Forge **1.20.1 MDK (47.3.0)** from files.minecraftforge.net and unzip it.
2. Copy this project's `src/` folder, `build.gradle`, `gradle.properties` and `settings.gradle` into the MDK (replace the originals; delete the MDK's example `src/main/java/com/example` folder if it is still there).
3. Run `./gradlew build` (Windows: `gradlew build`). Needs JDK 17.
4. The mod jar is `build/libs/jp3_spinosaurus-1.0.0.jar`. Drop it into `.minecraft/mods` with Forge 1.20.1.
   To test directly: `./gradlew runClient`.

## Use
- Creative tab "Spawn Eggs" -> Spinosaurus Spawn Egg, or `/summon jp3_spinosaurus:spinosaurus`
- Spawns rarely in jungles and swamps.

## Features
- Fully animated code model: sail, jaws, claws, tail whip, walk cycle, bite lunge, roar pose
- Glowing green JP3 eyes, hand-painted texture (python script in `tools/` regenerates it)
- Roar shockwave + camera shake on stomps, boss bar, enrages under 50% HP, smashes leaves, swims fast
- 240 HP, heavy bite, drops beef and bones

## Tweaking
- Stats / behavior: `entity/SpinosaurusEntity.java`
- Model + animation: `client/SpinosaurusModel.java`
- Skin: edit `tools/make_texture.py` and run it, or paint the PNG directly
