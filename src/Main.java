import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;
import org.osbot.rs07.utility.ConditionalSleep;

import java.awt.*;

@ScriptManifest(author = "YourName", name = "Lumbridge Fishing Bot", info = "A basic fishing bot in Lumbridge", version = 1.0, logo = "")
public class Main extends Script {

    private Area fishingArea = new Area(3241, 3150, 3244, 3145);  // Define the area for fishing spot
    private Area bankArea = new Area(3092, 3245, 3094, 3240);     // Define the bank area (Example area: Draynor bank)
    private String fishAction = "Net"; // Change to "Bait" or "Cage" depending on the type of fishing
    private int fishCaught = 0;        // Track number of fish caught
    private long startTime;            // Track when the script started

    @Override
    public void onStart() {
        log("Fishing Bot Started");
        startTime = System.currentTimeMillis();  // Record the start time
        getExperienceTracker().start(Skill.FISHING);  // Start tracking fishing XP
    }

    @Override
    public int onLoop() throws InterruptedException {
        if (getInventory().isFull()) {
            bankFish();
        } else {
            fish();
        }
        return random(200, 300);  // Sleep interval between loops
    }

    private void fish() {
        if (!fishingArea.contains(myPlayer())) {
            getWalking().webWalk(fishingArea);
        } else {
            NPC fishingSpot = getNpcs().closest("Fishing spot");
            if (fishingSpot != null && fishingSpot.interact(fishAction)) {
                new ConditionalSleep(10000) {  // Wait for 10 seconds max
                    @Override
                    public boolean condition() {
                        return getInventory().isFull() || !myPlayer().isAnimating();
                    }
                }.sleep();
            }
        }
    }

    private void bankFish() throws InterruptedException {
        if (!bankArea.contains(myPlayer())) {
            getWalking().webWalk(bankArea);
        } else {
            if (getBank().isOpen()) {
                fishCaught += getInventory().getAmount("Raw shrimp"); // Update the number of fish caught (adjust for your fish type)
                getBank().depositAllExcept(item -> item.getName().contains("net"));  // Deposit all except the fishing net
            } else {
                getBank().open();
            }
        }
    }

    @Override
    public void onExit() {
        log("Fishing Bot Stopped");
    }

    @Override
    public void onPaint(Graphics2D g) {
        // Calculate runtime
        long runTime = System.currentTimeMillis() - startTime;
        int hours = (int) (runTime / 3600000);
        int minutes = (int) ((runTime / 60000) % 60);
        int seconds = (int) ((runTime / 1000) % 60);

        // Draw runtime, fish caught, and XP per hour
        g.setColor(Color.WHITE);
        g.drawString("Time Running: " + hours + ":" + minutes + ":" + seconds, 10, 50);
        g.drawString("Fish Caught: " + fishCaught, 10, 70);
        g.drawString("Fishing XP Gained: " + getExperienceTracker().getGainedXP(Skill.FISHING), 10, 90);
        g.drawString("XP per Hour: " + getExperienceTracker().getGainedXPPerHour(Skill.FISHING), 10, 110);
        g.drawString("Fish per Hour: " + (int) (fishCaught / (runTime / 3600000.0)), 10, 130);
    }
}
