package dev.necauqua.mods.mira.data;

import com.mojang.datafixers.util.Pair;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;


// mira.scale.uniform (scales everything)
// mira.scale.eat (scales eat duration)
// mira.scale.item (scales item visual in hand; first and third person)
// mira.scale.base (scales everything except for what's above)
// mira.hitbox.width (scales hitbox)
// mira.hitbox.height (scales hitbox)
// mira.visual.width (scales render)
// mira.visual.height (scales render)
public class MiraAttributes {
    public static final List<RegistryObject<Attribute>> ALL = new ArrayList<>();

    private static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(Attribute.class, "mira");

    public static final RegistryObject<MiraAttribute> UNIFORM = register(
            "mira.scale.uniform",
            () -> new MiraAttribute("mira.scale.uniform", 1.0)
    );
    public static final RegistryObject<MiraAttribute> ITEM = register(
            "mira.scale.item",
            () -> new MiraAttribute("mira.scale.item", 1.0)
    );

    // stats
    public static final RegistryObject<MiraAttribute> EAT = register(
            "mira.stats.eat",
            () -> new InfluencedAttribute("mira.stats.eat", 1.0)
                    .addEvaluation((value, entity) -> value * UNIFORM.get().getValue(entity))
    );
    public static final RegistryObject<MiraAttribute> MOVEMENT = register(
            "mira.stats.movement",
            () -> new InfluencedAttribute("mira.stats.movement", 1.0)
                    .addEvaluation((value, entity) -> value * UNIFORM.get().getValue(entity))
    );


    public static final RegistryObject<MiraAttribute> HITBOX = register(
            "mira.hitbox",
            () -> new InfluencedAttribute("mira.hitbox", 1.0)
                    .addEvaluation((value, entity) -> value * UNIFORM.get().getValue(entity))
    );

    public static final RegistryObject<MiraAttribute> VISUAL = register(
            "mira.visual",
            () -> new InfluencedAttribute("mira.visual", 1.0)
                    .addEvaluation((value, entity) -> value * UNIFORM.get().getValue(entity))
    );
    public static final RegistryObject<MiraAttribute> PARTICLE = register(
            "mira.visual.particle",
            () -> new InfluencedAttribute("mira.visual.particle", 1.0)
                    .addEvaluation((value, entity) -> value * UNIFORM.get().getValue(entity))
    );

    public static final Pair<RegistryObject<InfluencedAttribute>, RegistryObject<InfluencedAttribute>> WIDTH = registerScalePair(
            "mira.hitbox", HITBOX,
            "mira.visual", VISUAL,
            "width",
            (name, base) -> new InfluencedAttribute(name, 1.0)
                    .addEvaluation((value, entity) -> value * base.get().getValue(entity))
    );
    public static final Pair<RegistryObject<InfluencedAttribute>, RegistryObject<InfluencedAttribute>> HEIGHT = registerScalePair(
            "mira.hitbox", HITBOX,
            "mira.visual", VISUAL,
            "height",
            (name, base) -> new InfluencedAttribute(name, 1.0)
                    .addEvaluation((value, entity) -> value * base.get().getValue(entity))
    );

    public static final RegistryObject<MiraAttribute> EYE_HEIGHT = register(
            "mira.visual.eye_height",
            () -> new InfluencedAttribute("mira.visual.eye_height", 1.0)
                    .addEvaluation((value, entity) -> value * HEIGHT.getSecond().get().getValue(entity))
    );

    // TODO: not exactly sure how this should be influenced?
    //       I think max of HEIGHT, WIDTH
    public static final RegistryObject<MiraAttribute> THIRD_PERSON = register(
            "mira.visual.third_person",
            () -> new InfluencedAttribute("mira.visual.third_person", 1.0)
                    .addEvaluation((value, entity) -> value * HEIGHT.getFirst().get().getValue(entity))
    );
    public static final RegistryObject<MiraAttribute> NEAR_PLANE = register(
            "mira.visual.near_plane",
            () -> new InfluencedAttribute("mira.visual.near_plane", 1.0)
                    .addEvaluation((value, entity) -> value * WIDTH.getFirst().get().getValue(entity))
    );
    // TODO: or should this be based on size?
    public static final RegistryObject<MiraAttribute> VIEW_BOB = register(
            "mira.visual.view_bob",
            () -> new InfluencedAttribute("mira.visual.view_bob", 1.0)
                    .addEvaluation((value, entity) -> value * MOVEMENT.get().getValue(entity))
    );

    public static final RegistryObject<MiraAttribute> RENDER_DISTANCE = register(
            "mira.visual.render_distance",
            () -> new InfluencedAttribute("mira.visual.render_distance", 1.0)
                    .addEvaluation((value, entity) -> value * VISUAL.get().getValue(entity))
    );

    private static <T extends IForgeRegistryEntry<? super T>> Pair<RegistryObject<T>, RegistryObject<T>> registerScalePair(
            String baseNameFirst, RegistryObject<MiraAttribute> firstBase,
            String baseNameSecond, RegistryObject<MiraAttribute> secondBase,
            String specificName,
            BiFunction<String, RegistryObject<MiraAttribute>, T> gen
    ) {
        //noinspection unchecked
        return Pair.of(
                (RegistryObject<T>) register(baseNameFirst + "." + specificName, () -> (Attribute) gen.apply(baseNameFirst + "." + specificName, firstBase)),
                (RegistryObject<T>) register(baseNameSecond + "." + specificName, () -> (Attribute) gen.apply(baseNameSecond + "." + specificName, secondBase))
        );
    }

    public static <T extends IForgeRegistryEntry<? super T>, I extends T> RegistryObject<I> register(final String name, final Supplier<T> sup) {
        final Attribute attr0 = ((Attribute) sup.get()).setSyncable(true);

        //noinspection unchecked
        RegistryObject<I> attr = (RegistryObject<I>) (Object) ATTRIBUTES.register(name, () -> attr0);
        //noinspection unchecked
        ALL.add((RegistryObject<Attribute>) (Object) attr);
        return attr;
    }

    public static void register(IEventBus modEventBus) {
        ATTRIBUTES.register(modEventBus);
    }
}
