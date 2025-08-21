# BBS Mod Bee Extension

This extension adds bee-themed features to the BBS mod, allowing players to transform into bees and use bee-related magical items.

## New Features

### 🐝 Bee Forms
Transform into different types of bees using the new BeeForm morph:
- **Honey Bee**: Standard bee with yellow and black stripes
- **Bumble Bee**: Larger, fuzzy bee with enhanced size
- **Carpenter Bee**: Dark metallic bee with unique appearance

Each bee form has configurable properties:
- Wing animation speed
- Size multiplier
- Stripe colors
- Flight capabilities
- Honey production settings
- Buzz sound effects

### 🍯 Magical Items

#### Honey Wand
A magical wand that grants bee-related powers:
- **Right-click**: Create honey and pollen particle effects around flowers
- **Shift + Right-click**: Transform yourself into a honey bee
- Always shows enchantment glint to indicate its magical nature

#### Magical Pollen
A consumable item that provides bee-like abilities:
- **Speed II** for 1 minute
- **Jump Boost II** for 1 minute  
- **Slow Falling** for 30 seconds
- **Night Vision** for 1 minute
- Creates beautiful pollen particle effects when consumed

### 🎮 Commands

All bee commands require operator permissions (level 2) and use the `/bbs bee` prefix:

#### `/bbs bee buzz [player]`
Makes the player buzz like a bee with particle effects and sounds.
- Without arguments: Makes you buzz
- With player argument: Makes target player buzz

#### `/bbs bee swarm <players>`
Arranges multiple players in a bee swarm formation.
- Requires at least 2 players
- First player becomes the swarm leader
- Other players are teleported around the leader in a circle
- All players get buzzing effects

#### `/bbs bee pollinate [radius]`
Creates pollen effects around flowers in the area.
- Default radius: 10 blocks
- Optional radius argument: 1-20 blocks
- Affects flowers, roses, tulips, dandelions, and poppies
- Creates beautiful particle effects and plays pollination sounds

#### `/bbs bee transform <player> [bee_type]`
Transforms a player into a specific bee type.
- Available types: `honey`, `bumble`, `carpenter`
- Default type: `honey` if not specified
- Creates immediate buzzing effects after transformation

## Usage Examples

```bash
# Transform yourself into a honey bee
/bbs bee transform @s honey

# Make all nearby players buzz
/bbs bee buzz @a[distance=..10]

# Create a bee swarm with specific players
/bbs bee swarm @a[team=bees]

# Pollinate a large area around you
/bbs bee pollinate 15
```

## Integration

This extension seamlessly integrates with the existing BBS mod:
- Uses the same morph system as other forms
- Commands follow the same `/bbs` prefix pattern
- Items appear in the BBS creative tab
- Forms can be used with all existing BBS features (animations, triggers, etc.)

## Technical Details

- **Form ID**: `bbs:bee`
- **Items**: `bbs:honey_wand`, `bbs:pollen`
- **Permissions**: Commands require level 2 (operator)
- **Dependencies**: Requires BBS mod base functionality

The extension adds minimal overhead and follows BBS mod's established patterns for maximum compatibility.