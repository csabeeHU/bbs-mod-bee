package mchorse.bbs_mod.forms.forms;

import mchorse.bbs_mod.forms.properties.BooleanProperty;
import mchorse.bbs_mod.forms.properties.ColorProperty;
import mchorse.bbs_mod.forms.properties.FloatProperty;
import mchorse.bbs_mod.forms.properties.LinkProperty;
import mchorse.bbs_mod.forms.properties.StringProperty;
import mchorse.bbs_mod.resources.Link;
import mchorse.bbs_mod.utils.colors.Color;

/**
 * Bee form that allows players to morph into bee-like creatures
 * with configurable wing speed, buzz sound, and honey trail effects
 */
public class BeeForm extends Form
{
    public final LinkProperty texture = new LinkProperty(this, "texture", Link.assets("textures/bee/default.png"));
    public final StringProperty beeType = new StringProperty(this, "bee_type", "honey");
    public final FloatProperty wingSpeed = new FloatProperty(this, "wing_speed", 1.0F);
    public final FloatProperty size = new FloatProperty(this, "size", 1.0F);
    public final ColorProperty stripeColor = new ColorProperty(this, "stripe_color", Color.create(255, 255, 0));
    public final BooleanProperty canFly = new BooleanProperty(this, "can_fly", true);
    public final BooleanProperty makesHoney = new BooleanProperty(this, "makes_honey", true);
    public final BooleanProperty buzzSound = new BooleanProperty(this, "buzz_sound", true);
    public final FloatProperty pollentTrailDensity = new FloatProperty(this, "pollen_trail_density", 0.3F);

    public BeeForm()
    {
        super();

        this.register(this.texture);
        this.register(this.beeType);
        this.register(this.wingSpeed);
        this.register(this.size);
        this.register(this.stripeColor);
        this.register(this.canFly);
        this.register(this.makesHoney);
        this.register(this.buzzSound);
        this.register(this.pollentTrailDensity);
    }

    @Override
    public String getDefaultDisplayName()
    {
        return "Bee (" + this.beeType.get() + ")";
    }

    @Override
    public boolean canRenderGuizmo()
    {
        return true;
    }

    /**
     * Get the bee type (honey, bumble, carpenter, etc.)
     */
    public String getBeeType()
    {
        return this.beeType.get();
    }

    /**
     * Check if this bee can fly
     */
    public boolean canFly()
    {
        return this.canFly.get();
    }

    /**
     * Get wing animation speed
     */
    public float getWingSpeed()
    {
        return this.wingSpeed.get();
    }

    /**
     * Get bee size multiplier
     */
    public float getSize()
    {
        return this.size.get();
    }
}