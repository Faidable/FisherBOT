import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.model.Item;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;
import org.osbot.rs07.utility.ConditionalSleep;
import org.osbot.rs07.api.map.Position;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

@ScriptManifest(author = "YourName", name = "Lumbridge Fishing Bot", info = "A basic fishing bot for Lumbridge", version = 1.0, logo = "")
public class LumbridgeFishingBot extends Script {

    private final Area fishingArea = new Area(3240, 3150, 3245, 3155); // Adjust as needed
    private final List<String> fishNames = Arrays.asList("Raw shrimps", "Raw anchovies"); // Adjusted to include raw anchovies
    private int fishCaught = 0;
    private long startTime;
    private int startXP;
    private String status = "Initializing...";

    @Override
    public void onStart() {
        log("Bot Started - Lumbridge Fishing Bot");
        startTime = System.currentTimeMillis();
        startXP = getSkills().getExperience(Skill.FISHING);
    }

    @Override
    public int onLoop() throws InterruptedException {
        // Check if the inventory is full
        if (getInventory().isFull()) {
            setStatus("Dropping fish...");
            dropFish();
            return random(2000, 3000);
        }

        // Check if the player is currently fishing
        if (myPlayer().isAnimating()) {
            setStatus("Fishing...");
            return random(2000, 3000);
        }

        // Find the nearest fishing spot
        NPC fishingSpot = getNpcs().closest(npc -> npc != null && npc.hasAction("Net") && fishingArea.contains(npc));

        if (fishingSpot != null && !myPlayer().isAnimating()) {
            setStatus("Fishing...");
            if (fishingSpot.interact("Net")) {
                new ConditionalSleep(5000) {
                    @Override
                    public boolean condition() throws InterruptedException {
                        return myPlayer().isAnimating(); // Wait until the player starts fishing
                    }
                }.sleep();
            }
        } else {
            setStatus("No fishing spot found, moving or repositioning...");
            getWalking().webWalk(fishingArea); // Walk to the fishing area

            // Random movement to refresh fishing spot visibility
            Position currentPos = myPlayer().getPosition();
            Position newPos = currentPos.translate(random(-1, 1), random(-1, 1));
            getWalking().walk(newPos);
        }

        return random(500, 1000);
    }

    private void dropFish() throws InterruptedException {
        if (myPlayer().isAnimating()) {
            Position currentPos = myPlayer().getPosition();
            Position newPos = currentPos.translate(random(-2, 2), random(-2, 2));
            getWalking().walk(newPos);
            new ConditionalSleep(3000) {
                @Override
                public boolean condition() throws InterruptedException {
                    return !myPlayer().isAnimating();
                }
            }.sleep();
        }

        for (Item item : getInventory().getItems()) {
            if (item != null && fishNames.contains(item.getName())) {
                fishCaught++;
                item.interact("Drop");
                sleep(random(500, 1000));
            }
        }
    }

    private void setStatus(String newStatus) {
        status = newStatus;
    }

    private int getFishPerHour() {
        long runTimeMillis = System.currentTimeMillis() - startTime;
        double hours = runTimeMillis / (1000.0 * 60.0 * 60.0);
        return (int) (fishCaught / hours);
    }

    private int getXPPerHour() {
        int xpGained = getSkills().getExperience(Skill.FISHING) - startXP;
        long runTimeMillis = System.currentTimeMillis() - startTime;
        double hours = runTimeMillis / (1000.0 * 60.0 * 60.0);
        return (int) (xpGained / hours);
    }

    @Override
    public void onPaint(Graphics2D g) {
        long runTimeMillis = System.currentTimeMillis() - startTime;
        String runTime = formatTime(runTimeMillis);
        int fishPerHour = getFishPerHour();
        int xpPerHour = getXPPerHour();

        // Set the position and dimensions of the HUD (keeping it down)
        int x = 10;
        int y = 50; // Adjusted y to move the HUD down
        int width = 300;
        int height = 120;

        // Draw gradient background for the HUD with a border for a premium feel
        Color gradientStart = new Color(50, 50, 50, 220);
        Color gradientEnd = new Color(30, 30, 30, 220);
        g.setPaint(new GradientPaint(x, y, gradientStart, x, y + height, gradientEnd));
        g.fillRoundRect(x, y, width, height, 15, 15); // Rounded corners for a modern look

        // Draw border around the HUD
        g.setColor(new Color(100, 100, 100, 180)); // Slightly transparent border
        g.drawRoundRect(x, y, width, height, 15, 15);

        // Draw a subtle shadow for the HUD
        g.setColor(new Color(0, 0, 0, 100));
        g.fillRoundRect(x + 5, y + 5, width, height, 15, 15); // Shadow offset

        // Draw XP bar background (above the HUD)
        int xpBarY = y - 30; // Position the XP bar above the HUD
        g.setColor(Color.DARK_GRAY);
        g.fillRoundRect(x, xpBarY, width, 15, 10, 10);

        // Calculate the XP progress bar fill percentage
        int currentXP = getSkills().getExperience(Skill.FISHING);
        int nextLevelXP = getSkills().getExperienceForLevel(getSkills().getStatic(Skill.FISHING) + 1);
        int currentLevelXP = getSkills().getExperienceForLevel(getSkills().getStatic(Skill.FISHING));
        double progress = (double) (currentXP - currentLevelXP) / (nextLevelXP - currentLevelXP);

        // Draw the XP progress bar
        g.setColor(new Color(50, 205, 50)); // Lighter, premium green
        g.fillRoundRect(x, xpBarY, (int) (progress * width), 15, 10, 10);

        // Draw XP text on the bar with a more luxurious font
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 12));
        g.drawString("XP: " + currentXP + "/" + nextLevelXP + " (Level: " + getSkills().getStatic(Skill.FISHING) + ")", x + 10, xpBarY + 12);

        // Draw the bot status and stats with premium fonts and spacing
        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        g.setColor(Color.WHITE);
        g.drawString("Lumbridge Fishing Bot", x + 10, y + 25);

        g.setFont(new Font("SansSerif", Font.PLAIN, 13)); // Slimmer, more elegant font for details
        g.drawString("Status: " + status, x + 10, y + 45);
        g.drawString("Runtime: " + runTime, x + 10, y + 65);
        g.drawString("Fish/Hour: " + fishPerHour, x + 10, y + 85);
        g.drawString("XP/Hour: " + xpPerHour, x + 10, y + 105);
    }

    private String formatTime(long timeMillis) {
        long seconds = (timeMillis / 1000) % 60;
        long minutes = (timeMillis / (1000 * 60)) % 60;
        long hours = (timeMillis / (1000 * 60 * 60)) % 24;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    @Override
    public void onExit() {
        log("Bot Stopped");
    }
}