# Lucidity

Lucidity is a client-side Fabric visual assistance mod.

## Minecraft 26.3

The repository now has a clean Minecraft 26.3 build using Fabric Loom 1.19, Java 25, Fabric Loader 0.20.1, and Fabric API 0.165.0+26.3. The 26.3 client entrypoint and resource-pack registration are build-tested by GitHub Actions.

The original 1.21.x implementation remains in `src/main/java` as legacy source while its renderer and GUI features are migrated to the 26.3 extraction pipeline. The active 26.3 source is under `src/main/java26`.
