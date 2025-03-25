package dev.necauqua.mods.mira.data;

import com.mojang.datafixers.util.Pair;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.w3c.dom.Attr;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
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

    public static final RegistryObject<SimpleAttribute> UNIFORM = register(
            "mira.scale.uniform",
            () -> new SimpleAttribute("mira.scale.uniform", 1.0)
    );
    public static final RegistryObject<SimpleAttribute> EAT = register(
            "mira.scale.eat",
            () -> new SimpleAttribute("mira.scale.eat", 1.0)
    );
    public static final RegistryObject<SimpleAttribute> ITEM = register(
            "mira.scale.item",
            () -> new SimpleAttribute("mira.scale.item", 1.0)
    );

    public static final Pair<RegistryObject<InfluencedAttribute>, RegistryObject<InfluencedAttribute>> WIDTH = registerScalePair(
            "mira.hitbox",
            "mira.visual",
            "width",
            (name) -> new InfluencedAttribute(name, 1.0)
                    .addEvaluation((value, entity) -> value * UNIFORM.get().getValue(entity))
    );

    public static final Pair<RegistryObject<InfluencedAttribute>, RegistryObject<InfluencedAttribute>> HEIGHT = registerScalePair(
            "mira.hitbox",
            "mira.visual",
            "height",
            (name) -> new InfluencedAttribute(name, 1.0)
                    .addEvaluation((value, entity) -> value * UNIFORM.get().getValue(entity))
    );

    private static <T extends IForgeRegistryEntry<? super T>> Pair<RegistryObject<T>, RegistryObject<T>> registerScalePair(
            String baseNameFirst,
            String baseNameSecond,
            String specificName,
            Function<String, T> gen
    ) {
        //noinspection unchecked
        return Pair.of(
                (RegistryObject<T>) register(baseNameFirst + "." + specificName, () -> (Attribute) gen.apply(baseNameFirst + "." + specificName)),
                (RegistryObject<T>) register(baseNameSecond + "." + specificName, () -> (Attribute) gen.apply(baseNameSecond + "." + specificName))
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
