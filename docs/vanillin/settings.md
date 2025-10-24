---
title: Vanillin Settings
---

# Vanillin Settings

[[toc]]

## Tags

Vanillin provides tags for mod authors and modpack curators to control behavior and help avoid conflicts.

### `no_instancing` {#no_instancing}

Adding an item to the tag `vanillin:no_instancing` will prevent that item from using instanced rendering in the world.

This is necessary for items that use extra tricks for rendering that Vanillin can not automatically detect.

::: code-group
```json [data/vanillin/tags/items/no_instancing.json]
{
  "replace" : false,
  "values" : [
    "mymodid:my_fancy_item",
    "#minecraft:anvil"
  ]
}
```
::: code-group
