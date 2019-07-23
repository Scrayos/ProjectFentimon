package net.scrayos.projectfentimon.lavastabilization;

import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.EarthAbility;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Lava-Stabilisierung verwandelt alle Lava-Blöcke zu den Füßen eines Erdbändigers in Magma, solange er diese Fähigkeit
 * aktiviert hat. Er ist jedoch nicht resistent gegen die Hitze der Magma-Blöcke. Diese Fähigkeit ähnelt sehr der
 * FrostWalker-Verzauberung (Treasure-Enchantment), die alle Wasserblöcke zu den Füßen des Spielers temporär in Eis
 * verwandelt, damit er auf dem Wasser laufen kann.
 */
public class LavaStabilization extends EarthAbility implements AddonAbility {

    //<editor-fold desc="CONSTANTS">

    //<editor-fold desc="meta">
    /** Der Name der Fähigkeit für die Anzeige innerhalb der ProjectKorra Menüs und Hilfe-Befehlen. */
    @NotNull
    private static final String ABILITY_NAME = "Lava-Stabilisierung";
    /** Der Name des Autors für die Anzeige innerhalb der ProjectKorra Menüs und Hilfe-Befehlen. */
    @NotNull
    private static final String AUTHOR_NAME = "Scrayos";
    /** Die Version der Fähigkeit für die Anzeige innerhalb der ProjectKorra Menüs und Hilfe-Befehlen. */
    @NotNull
    private static final String VERSION = "1.0.0";
    //</editor-fold>

    //<editor-fold desc="gameplay">
    /** Der Standard-Cooldown, der zwischen dem Deaktivieren und Reaktivieren dieser Fähigkeit vergehen muss. */
    private static final long DEFAULT_COOLDOWN = 0;
    /** Die Standard-Quadrat-Distanz der Blöcke (ohne Höhe), die von Lava zu Magma verwandelt werden sollen. */
    private static final int DEFAULT_RANGE = 3;
    //</editor-fold>

    //</editor-fold>


    //<editor-fold desc="LOCAL FIELDS">
    /** Das {@link Set} der aktuell noch in Magma verwandelten Lava-Blöcke, die zurück verwandelt werden müssen. */
    @NotNull
    private final Set<Block> magmaBlocks = new HashSet<>();
    //</editor-fold>


    //<editor-fold desc="CONSTRUCTORS">
    /**
     * Erstellt eine neue Instanz der {@link LavaStabilization} für einen spezifischen Spieler. In dieser Instanz wird
     * der lokale Status der Ausführung gespeichert und abgerufen. Es wird für jeden ausführenden Spieler eine eigene
     * Instanz dieser Fähigkeit erstellt und der Zustand separat verwaltet.
     *
     * @param player Der Spieler, der diese Fähigkeit gerade aktivieren möchte und für den diese Fähigkeit läuft.
     */
    public LavaStabilization(@NotNull final Player player) {
        super(player);

        // check if the player is able to use this
        if (!bPlayer.canBend(this)) {
            return;
        }

        // add the cooldown for this exact moment
        bPlayer.addCooldown(this);

        // start the execution of this ability
        start();
    }
    //</editor-fold>


    //<editor-fold desc="meta">
    @Override
    public String getName() {
        return ABILITY_NAME;
    }

    @Override
    public String getAuthor() {
        return AUTHOR_NAME;
    }

    @Override
    public String getVersion() {
        return VERSION;
    }
    //</editor-fold>

    //<editor-fold desc="activation">
    @Override
    public void load() {
    }

    @Override
    public void stop() {
        final Iterator<Block> oldIt = magmaBlocks.iterator();
        while (oldIt.hasNext()) {
            final Block oldBlock = oldIt.next();

            // transform back and remove
            oldIt.remove();
            oldBlock.setType(Material.LAVA);
        }
    }
    //</editor-fold>


    //<editor-fold desc="state">
    @Override
    public boolean isSneakAbility() {
        return false;
    }

    @Override
    public boolean isHarmlessAbility() {
        return true;
    }
    //</editor-fold>

    //<editor-fold desc="gameplay">
    @Override
    public long getCooldown() {
        return DEFAULT_COOLDOWN;
    }

    @Override
    public Location getLocation() {
        return getPlayer().getLocation();
    }
    //</editor-fold>

    //<editor-fold desc="execution">
    @Override
    public void progress() {
        // get location at players feet
        final Block feetBlock = getLocation().getBlock().getRelative(BlockFace.DOWN);

        // get the blocks in range
        final Set<Block> newMagmaBlocks = new HashSet<>();
        for (int x = -DEFAULT_RANGE; x <= DEFAULT_RANGE; x++) {
            for (int z = -DEFAULT_RANGE; z <= DEFAULT_RANGE; z++) {
                // only add blocks in a diagonal shape
                if ((x + z) <= DEFAULT_RANGE) newMagmaBlocks.add(feetBlock.getRelative(x, 0, z));
            }
        }

        // set old magma blocks back to lava
        final Iterator<Block> oldIt = magmaBlocks.iterator();
        while (oldIt.hasNext()) {
            final Block oldBlock = oldIt.next();

            // check if this block shall not be transformed anymore
            if (!newMagmaBlocks.contains(oldBlock)) {
                oldIt.remove();
                oldBlock.setType(Material.LAVA);
            }
        }

        // add the new magma blocks
        magmaBlocks.addAll(newMagmaBlocks);

        // set all current magma blocks to lava
        for (final Block magmaBlock : magmaBlocks) {
            magmaBlock.setType(Material.MAGMA_BLOCK);
        }
    }
    //</editor-fold>
}
