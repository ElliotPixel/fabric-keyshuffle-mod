package net.fabricmc.example;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.util.*;

public class ExampleClient implements ClientModInitializer {

    private static final Set<String> TARGET_TRANSLATION_KEYS = Set.of(
            "key.jump",
            "key.sneak",
            "key.sprint",
            "key.left",
            "key.right",
            "key.back",
            "key.forward",
            "key.attack",
            "key.use"
    );

    private float lastHealth = -1f;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ClientPlayerEntity player = client.player;
            if (player == null) return;

            float currentHealth = player.getHealth();
            if (lastHealth >= 0 && currentHealth < lastHealth) {
                shuffleKeyBindings(client);
            }

            lastHealth = currentHealth;
        });
    }

    private void shuffleKeyBindings(MinecraftClient client) {
        Set<InputUtil.Key> usedKeys = new HashSet<>();
        List<KeyBinding> targets = new ArrayList<>();

        // First collect all currently used keys and our target bindings
        for (KeyBinding kb : client.options.allKeys) {
            InputUtil.Key boundKey = InputUtil.fromTranslationKey(kb.getBoundKeyTranslationKey());
            usedKeys.add(boundKey);

            if (TARGET_TRANSLATION_KEYS.contains(kb.getTranslationKey())) {
                targets.add(kb);
            }
        }

        // Create pool of available keys (A-Z, 0-9)
        List<InputUtil.Key> pool = new ArrayList<>();
        for (int code = GLFW.GLFW_KEY_A; code <= GLFW.GLFW_KEY_Z; code++) {
            InputUtil.Key key = InputUtil.fromKeyCode(code, 0);
            if (!usedKeys.contains(key)) pool.add(key);
        }
        for (int code = GLFW.GLFW_KEY_0; code <= GLFW.GLFW_KEY_9; code++) {
            InputUtil.Key key = InputUtil.fromKeyCode(code, 0);
            if (!usedKeys.contains(key)) pool.add(key);
        }

        Collections.shuffle(pool);

        if (pool.size() < targets.size()) {
            System.out.println("[KeyShuffle] ⚠ Not enough free keys; some bindings unchanged.");
        }

        // Assign new unique keys to our target bindings
        Iterator<InputUtil.Key> iterator = pool.iterator();
        for (KeyBinding kb : targets) {
            if (!iterator.hasNext()) break;
            InputUtil.Key newKey = iterator.next();
            kb.setBoundKey(newKey);
            System.out.println("[KeyShuffle] " + kb.getTranslationKey() + " => " + newKey.getTranslationKey());
        }

        KeyBinding.updateKeysByCode();
        System.out.println("[KeyShuffle] Key-bindings shuffled without conflicts.");
    }
}