# Concept Reference

The Flywheel API has a few key differences from vanilla rendering that are important to understand.

This page serves as a reference for concepts unique to Flywheel.

[[toc]]

## Game Objects

A game object is an Entity, BlockEntity, or Effect in the level. This is not a unique concept to Flywheel, but Flywheel
provides a unified rendering path for all game objects.

### Effects

An effect is a "free" game object. It is a concept unique to Flywheel and as such does not exist on the server.
Effects allow users of Flywheel to access the Flywheel rendering system without needing to depend on a BlockEntity or
Entity. The caveat is that the implementor of an Effect is responsible for managing its own lifecycle and
client/server synchronization.

## Visuals

Visuals are the analog to Renderers in vanilla Minecraft. Each game object that is rendered will have a
corresponding Visual object. This way, Visuals can maintain state independent of the client representation of the game
object,
and update in parallel to other Visuals of the same type.

Statefulness and parallelism are the core motivations behind this abstraction.

::: danger IMPLEMENTATION CONTRACT
Your Visual _must_ be in a correct state upon construction, where all created Instances are be at their expected
position. The most common bug when implementing Visuals is having your instances appear at the render origin.
:::

## Instances

An Instance is a single renderable object that is uploaded to the GPU. Visuals create, update, and delete Instances.

Instances are stateful. If an Instance is not updated, it will continue to be rendered in its existing configuration,
without being re-uploaded to the GPU. This allows for significant performance improvements and CPU->GPU bandwidth
savings when Instances do not move (e.g. in the case of BlockEntities), or when the animation can be expressed in the
instance shader.

Users of Flywheel are encouraged to only update Instances when necessary.

::: warning
Ensure you `.delete()` all Instances you create when your Visual is deleted. Failure to do so will result in orphaned
instances that continue to be rendered.
:::

## Render Origin

The render origin is an integer coordinate in world space which serves as the origin for rendering.

This is different from vanilla entity/block entity rendering which uses the camera position as the origin.
Such an approach is not viable for Flywheel, as that would require every instance to update every frame. Instead,
Flywheel maintains a render origin that is nearby, but not necessarily exactly at, the camera position. As the camera
moves in the level Flywheel will update the render origin once it gets too far away. All instances are deleted and all
visuals are recreated when the render origin updates.

::: tip
To map from world space to render space, subtract the render origin from the world space position. e.g.

```java
BlockPos renderSpacePos = worldSpacePos.subtract(visualizationContext.renderOrigin());
```

:::

## Plans

Plans are Flywheel's way to expose the thread pool to Visuals. Plans are created once, and executed many times.
When executed, Plans receive a generic context object, a reference to the Executor they're running on, and a Runnable
to call when all the work in the Plan is complete. This simple abstraction allows for incredibly complex composition
of tasks and allows for easy parallelization of work.

Implementing `DynamicVisual` and `TickableVisual` requires you to construct Plans that will be executed every frame or
tick, respectively.

_Most_ Visuals will not need the full complexity of Plans, so the Flywheel lib provides `Simple` counterparts to both
interfaces.
