# LodeStonePorter 

A Minecraft Plugin with which you can turn Lodestones into teleporter. Simply shift right-click the desired block with a item, which shall represented the teleport stone. 

## Settings

### Config

| Name                   | Description                                                                                                                                    | Default Value |
|------------------------|------------------------------------------------------------------------------------------------------------------------------------------------|---------------|
| dropItemOnBreak        | Whether the item that was input should be dropped when the teleporter is broken                                                                | true          |
| onlyAllowOwnerToBreak  | Whether or not the teleporter can be broken only by the creator (or by people with permissions). Also disables piston movement and explosions. | true          |
| interdimensionalTravel | Whether interdimensional travel is allowed. Eg: Overworld to Nether, End to Nether                                                             | true          |
| minTPDistance          | The minimum amount of blocks the destination has to be away.                                                                                   | 0             |
| maxTPDistance          | The maximum amount of blocks the destination has to be away. -1 for no maximum.                                                                | -1            |
| tpsOverworld           | Whether teleporter can be created in the overworld.                                                                                            | true          |
| tpsNether              | Whether teleporter can be created in the nether.                                                                                               | true          |
| tpsEnd                 | Whether teleporter can be created in the end.                                                                                                  | true          |
| tpsCustom              | Whether teleporter can be created in custom dimensions.                                                                                        | false         |
| privateTps             | Whether player can only teleport to teleporter created by them.                                                                                | false         |
| payForTP               | Whether player have to pay for teleporting.                                                                                                    | true          |
| currency               | The item used for payment.                                                                                                                     | GOLD_INGOT    |
| minPrice               | Minimum Price                                                                                                                                  | 1             |
| maxPrice               | Maximum Price                                                                                                                                  | 10            |
| priceStartDistance     | Distance at which teleporting starts to cost something.                                                                                        | 200           |
| priceEndDistance       | Distance at which teleporting prices stop increasing.                                                                                          | 10000         |
| interdimensionalCost   | Costs for traveling between dimensions.                                                                                                        | 20            |

#### Details on the Distances
```
     minTPDistance        priceEndDistance             x
|----------|----------|----------|----------|----------|
0             priceStartDistance      maxTPDistance     

|----------| No Teleportation in this range
           |----------| Free teleportation in this range
                      |----------| Teleportation costs between minPrice and maxPrice
                                 |----------| Teleportation costs maxPrice
                                            |----------|  No Teleportation in this range
```

### Permissions

There are four permission types to control the use of permissions.

| Name        | Description                                                                  | Default Value |
|-------------|------------------------------------------------------------------------------|---------------|
| createTP    | Allows you to create teleporters.                                            | true          |
| breakAllTP  | Allows you to break teleporters from other player, ignoring config settings. | op            |
| useTP       | Allows you to use teleporter.                                                | true          |
| ignoreCosts | Allows you to ignore the costs set in the config                             | false         |

## Collaborating

If you added a feature, create a pull request and make sure you added yourself to the authors in the yml.
