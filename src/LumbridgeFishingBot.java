import org.osbot.rs07.api.model.Item;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;
import org.osbot.rs07.utility.ConditionalSleep;

@ScriptManifest(author = "YourName", name = "Lumbridge Fishing Bot", info = "A basic fishing bot for Lumbridge", version = 1.0, logo = "")
public class LumbridgeFishingBot extends Script {

    // Define the Lumbridge fishing area
    private final Area fishingArea = new Area(3240, 3150, 3245, 3155); // Adjust as needed

    @Override
    public void onStart() {
        log("Bot Started - Lumbridge Fishing Bot");
    }

    @Override
    public int onLoop() throws InterruptedException {
        // Check if the inventory is full
        if (getInventory().isFull()) {
            log("Inventory full, dropping fish...");
            dropFish(); // Drop all fish
            return random(500, 1000); // Small delay after dropping items
        }

        // Check if the player is currently fishing
        if (myPlayer().isAnimating()) {
            log("Currently fishing...");
            return random(2000, 3000); // Wait before checking again
        }

        // Find the nearest fishing spot
        NPC fishingSpot = getNpcs().closest(npc -> npc != null && npc.hasAction("Net") && fishingArea.contains(npc));

        if (fishingSpot != null && !myPlayer().isAnimating()) {
            log("Fishing spot found, starting to fish...");
            if (fishingSpot.interact("Net")) {
                new ConditionalSleep(5000) {
                    @Override
                    public boolean condition() throws InterruptedException {
                        return myPlayer().isAnimating(); // Wait until the player starts fishing
                    }
                }.sleep();
            }
        } else {
            log("No fishing spot found, moving to fishing area...");
            getWalking().webWalk(fishingArea); // Walk to the fishing area
        }

        return random(500, 1000); // Main loop sleep time
    }

    // Method to drop all fish when the inventory is full
    private void dropFish() throws InterruptedException {
        for (Item item : getInventory().getItems()) {
            // Check if the item is a fish (e.g., Shrimp or Anchovies)
            if (item != null && (item.getName().contains("Shrimp") || item.getName().contains("Anchovies"))) {
                item.interact("Drop"); // Drop the fish
                sleep(random(150, 300)); // Delay between drops
            }
        }
    }

    @Override
    public void onExit() {
        log("Bot Stopped");
    }
}
