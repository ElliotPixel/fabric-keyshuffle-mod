package net.fabricmc.example;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.InputUtil;

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
            "key.use",
            "key.inventory"
    );

    private float lastHealth = -1f;
    private boolean keysJustSwapped = false;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ClientPlayerEntity player = client.player;
            if (player == null) return;

            float currentHealth = player.getHealth();
            if (lastHealth >= 0 && currentHealth < lastHealth) {
                swapKeyBindings(client);
                keysJustSwapped = true;
            }

            // Reset pressed states after swap
            if (keysJustSwapped) {
                for (KeyBinding kb : client.options.allKeys) {
                    kb.setPressed(kb.isPressed());
                }
                keysJustSwapped = false;
            }

            lastHealth = currentHealth;
        });
    }

    private void swapKeyBindings(MinecraftClient client) {
        List<KeyBinding> targets = new ArrayList<>();

        // Collect all target bindings
        for (KeyBinding kb : client.options.allKeys) {
            if (TARGET_TRANSLATION_KEYS.contains(kb.getTranslationKey())) {
                targets.add(kb);
            }
        }

        // Create a copy of current bindings for swapping
        Map<KeyBinding, InputUtil.Key> originalKeys = new HashMap<>();
        for (KeyBinding kb : targets) {
            originalKeys.put(kb, InputUtil.fromTranslationKey(kb.getBoundKeyTranslationKey()));
        }

        // Shuffle the list to determine swapping order
        Collections.shuffle(targets);

        // Perform pairwise swaps
        for (int i = 0; i < targets.size() - 1; i += 2) {
            KeyBinding first = targets.get(i);
            KeyBinding second = targets.get(i + 1);

            // Get the original keys
            InputUtil.Key firstKey = originalKeys.get(first);
            InputUtil.Key secondKey = originalKeys.get(second);

            // Swap the keys
            first.setBoundKey(secondKey);
            second.setBoundKey(firstKey);

            System.out.println("[KeySwap] Swapped " + first.getTranslationKey() + " (" +
                    InputUtil.fromTranslationKey(first.getBoundKeyTranslationKey()).getLocalizedText().getString() +
                    ") with " + second.getTranslationKey() + " (" +
                    InputUtil.fromTranslationKey(second.getBoundKeyTranslationKey()).getLocalizedText().getString() + ")");
        }

        // If there's an odd number of keys, the last one remains unchanged
        if (targets.size() % 2 != 0) {
            KeyBinding last = targets.getLast();
            System.out.println("[KeySwap] " + last.getTranslationKey() + " (" +
                    InputUtil.fromTranslationKey(last.getBoundKeyTranslationKey()).getLocalizedText().getString() +
                    ") was not swapped (odd number of bindings)");
        }

        KeyBinding.updateKeysByCode();
        System.out.println("[KeySwap] Key-bindings swapped successfully.");
    }
}