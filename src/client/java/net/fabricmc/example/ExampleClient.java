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

    private static final Set<String> EXCLUDED_TRANSLATION_KEYS = Set.of(
            "key.chat",
            "key.command",
            "key.playerlist",
            "key.listPlayers",
            "key.socialInteractions",
            "key.advancements",
            "key.spectatorOutlines",
            "key.screenshot",
            "key.smoothCamera",
            "key.fullscreen",
            "key.togglePerspective"
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
        Set<InputUtil.Key> forbidden = new HashSet<>();
        List<KeyBinding> targets = new ArrayList<>();

        for (KeyBinding kb : client.options.allKeys) {
            InputUtil.Key boundKey = InputUtil.fromTranslationKey(kb.getBoundKeyTranslationKey());

            if (EXCLUDED_TRANSLATION_KEYS.contains(kb.getTranslationKey())) {
                forbidden.add(boundKey);
            } else {
                targets.add(kb);
            }
        }

        List<InputUtil.Key> pool = new ArrayList<>();
        for (int code = GLFW.GLFW_KEY_A; code <= GLFW.GLFW_KEY_Z; code++) {
            InputUtil.Key key = InputUtil.fromKeyCode(code, 0);
            if (!forbidden.contains(key)) pool.add(key);
        }
        for (int code = GLFW.GLFW_KEY_0; code <= GLFW.GLFW_KEY_9; code++) {
            InputUtil.Key key = InputUtil.fromKeyCode(code, 0);
            if (!forbidden.contains(key)) pool.add(key);
        }

        Collections.shuffle(pool);

        if (pool.size() < targets.size()) {
            System.out.println("[KeyShuffle] ⚠ Not enough free keys; some bindings unchanged.");
        }

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