package com.midnightbits.scanner.modmenu;

import com.midnightbits.scanner.modmenu.gui.SingleColumnOptions;
import com.midnightbits.scanner.modmenu.gui.WarningWidget;
import com.midnightbits.scanner.rt.core.Id;
import com.midnightbits.scanner.rt.core.ScannerMod;
import com.midnightbits.scanner.utils.Options;
import com.midnightbits.scanner.utils.Settings;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@Environment(value = EnvType.CLIENT)
public class OptionsScreen extends SingleColumnOptions {
    static final SettingSliderOption DISTANCE_OPTION = new SettingSliderOption(
            "distance", 4, 64,
            Settings::blockDistance, Settings::withBlockDistance,
            (meters) -> translatableOption("distance/measure", meters));
    static final SettingSliderOption WIDTH_OPTION = new SettingSliderOption(
            "width", 0, 10,
            Settings::blockRadius, Settings::withBlockRadius, makeRadiusLabeler());
    static final SettingSliderOption ECHOES_OPTION = new SettingSliderOption(
            "echoes", 10, 200,
            Settings::echoesSize, Settings::withEchoesSize,
            (value) -> Text.literal(String.valueOf(value)));
    static final SettingSliderOption[] OPTIONS = new SettingSliderOption[]{
            DISTANCE_OPTION, WIDTH_OPTION, ECHOES_OPTION
    };

    public static final String TAG = ScannerMod.MOD_ID;
    private static final Logger LOGGER = LoggerFactory.getLogger(TAG);
    Settings settings = null;

    public OptionsScreen(Screen parent) {
        super(parent, Text.translatable(ScannerMod.translationKey("screens", "options")));
        final var settings = Options.getInstance().settings();
        assert settings != null;

        final var echoesSize = settings.echoesSize();
        final var blockDistance = settings.blockDistance();
        final var blockRadius = settings.blockRadius();
        final var interestingIds = settings.interestingIds();

        this.settings = new Settings(echoesSize, blockDistance, blockRadius, interestingIds);
    }

    static String optionKey(String id) {
        return ScannerMod.translationKey("option", id);
    }

    static Text translatableOption(String optionId, Object... args) {
        return Text.translatable(optionKey(optionId), args);
    }

    static Function<Integer, Text> makeRadiusLabeler() {
        return (radius) -> {
            if (radius < 1)
                return translatableOption("width/zero", 1);
            final var width = radius * 2 + 1;
            return translatableOption("width/circle", width);
        };
    }

    @Override
    protected void addOptions() {
        if (body == null)
            return;

        Consumer<Settings> assign = (s) -> settings = s;
        for (final var option : OPTIONS) {
            body.addOption(option.slider(settings, assign));
        }

        final var link = makeInventoryLink();
        if (link != null)
            body.addWidget(link);
    }

    private ClickableWidget makeInventoryLink() {
        if (client != null && client.world != null) {
            return ButtonWidget.builder(translatableOption("interesting_ids"), button -> {
                client.setScreen(new InventoryScreen(settings.interestingIds(),
                        this, this::onUpdateIds));
            }).build();
        }

        if (client != null) {
            return new WarningWidget(client, width, layout.getContentHeight(),
                    optionKey("inventory_needs_world"));
        }

        return null;
    }

    void onUpdateIds(Set<Id> ids) {
        LOGGER.warn(">>> ({})", ids.stream().map(Id::toString).collect(Collectors.joining(", ")));
        settings = settings.withIds(ids);
    }

    @Override
    public void removed() {
        final var echoesSize = settings.echoesSize();
        final var blockDistance = settings.blockDistance();
        final var blockRadius = settings.blockRadius();
        final var interestingIds = settings.interestingIds();

        Options.getInstance().setAll(echoesSize, blockDistance, blockRadius, interestingIds);
        super.removed();
    }

    interface SettingsModifier<T> {
        Settings withValue(Settings settings, T val);
    }

    interface SettingsSupplier<T> {
        T get(Settings settings);
    }

    private record SettingSliderOption(
            String key,
            int inclMin,
            int inclMax,
            SettingsSupplier<Integer> defaultValueSupplier,
            SettingsModifier<Integer> changeCallback,
            Function<Integer, Text> labeler) {

        public SimpleOption<Integer> slider(Settings settings, Consumer<Settings> setter) {
            final var callbacks = new SimpleOption.ValidatingIntSliderCallbacks(inclMin, inclMax);
            Consumer<Integer> sliderChangeCallback = (value) -> {
                final var changed = changeCallback.withValue(settings, value);
                setter.accept(changed);
            };
            final var defaultValue = defaultValueSupplier.get(settings);
            return new SimpleOption<Integer>(
                    optionKey(key),
                    SimpleOption.emptyTooltip(),
                    (text, value) -> ScreenTexts.composeGenericOptionText(text, labeler.apply(value)),
                    callbacks, defaultValue, sliderChangeCallback);
        }
    }
}