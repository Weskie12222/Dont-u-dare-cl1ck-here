# House Builder (Fabric, Minecraft 26.2)

Place a **House Builder** block, right-click it, and a house appears.

- **Wooden House Builder** - 9x7 oak cabin, dark oak gable roof, windows, bed, crafting table, furnace, chest, lanterns.
- **Modern House Builder** - 11x9 two-storey house, glass walls, flat roof, staircase, bed, bookshelves, lanterns.

- **Military Barracks Builder** - 15x13 stone-brick fortress: four corner watchtowers with battlements, iron gate with buttons, arrow slits, an entry hall (lectern + bell), a barracks hall with 8 beds and footlockers, and a back armory (smithing table, anvil, grindstone, blast furnace, chests, barrels). Outside: flagpole and sandbag barricade.

All are in the **Building Blocks** creative tab and craftable in survival
(8 planks around 1 log / 8 white concrete around 1 glass / 8 stone bricks around 1 iron ingot).

## How it works

The block you place becomes the **front door**. The house is built **away from
the direction you face when you click** (door toward you). Trees and terrain in
the footprint are cleared and gaps under the floor are filled.

## Build

Requires **JDK 25**.

    ./gradlew build          # jar ends up in build/libs/  (use the one WITHOUT -sources)
    ./gradlew runClient      # test in a dev client

Copy the jar into your `mods` folder along with Fabric API for 26.2.

## Customise

All house layouts live in `HouseGenerator.java`. Walls, roof, size and furniture
are plain loops over local coordinates (u = sideways, f = forward, y = up), so
changing a block type or adding a room is a one-line edit. To add a new style,
add a value to `HouseStyle`, a `buildXxx` method, and register another block in
`ModBlocks` (plus blockstate/model/item/lang/loot files).
