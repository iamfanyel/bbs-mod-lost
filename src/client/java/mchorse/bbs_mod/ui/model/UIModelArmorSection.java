package mchorse.bbs_mod.ui.model;

import mchorse.bbs_mod.cubic.model.ArmorSlot;
import mchorse.bbs_mod.cubic.model.ArmorType;
import mchorse.bbs_mod.cubic.model.ModelConfig;
import mchorse.bbs_mod.l10n.keys.IKey;
import mchorse.bbs_mod.ui.UIKeys;
import mchorse.bbs_mod.ui.framework.UIContext;
import mchorse.bbs_mod.ui.framework.elements.UIElement;
import mchorse.bbs_mod.ui.framework.elements.input.UIPropTransform;
import mchorse.bbs_mod.ui.framework.elements.input.list.UIStringList;
import mchorse.bbs_mod.ui.framework.elements.input.text.UITextbox;
import mchorse.bbs_mod.ui.framework.elements.utils.UILabel;
import mchorse.bbs_mod.BBSModClient;
import mchorse.bbs_mod.cubic.ModelInstance;
import mchorse.bbs_mod.ui.framework.elements.buttons.UIIcon;
import mchorse.bbs_mod.ui.utils.UI;
import mchorse.bbs_mod.ui.utils.icons.Icons;
import mchorse.bbs_mod.ui.utils.pose.UIPoseEditor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UIModelArmorSection extends UIModelSection
{
    public UIStringList types;
    public UILabel limbLabel;
    public UIIcon pickBone;

    private ArmorType type;

    public UIModelArmorSection(UIModelPanel editor)
    {
        super(editor);

        this.limbLabel = UI.label(IKey.constant("..."));
        this.limbLabel.w(1F);

        this.pickBone = new UIIcon(Icons.SEARCH, (b) -> this.openLimbMenu());
        this.pickBone.w(20);

        this.types = new UIStringList((l) -> this.fillData());
        this.types.background = 0x88000000;

        for (ArmorType type : ArmorType.values())
        {
            this.types.add(type.name().toLowerCase());
        }

        this.types.sort();

        this.fields.row(5);
        this.types.w(1F, -25).h(9 * 16);
        this.pickBone.wh(20, 20);
        this.fields.add(this.types, this.pickBone);
    }

    @Override
    public void render(UIContext context)
    {
        if (this.editor.getPoseEditor() != null)
        {
            String group = this.editor.getPoseEditor().getGroup();

            this.pickBone.setEnabled(group == null || group.isEmpty());
        }

        super.render(context);

        if (!this.pickBone.isEnabled())
        {
            context.batcher.icon(Icons.LOCKED, this.pickBone.area.x + 2, this.pickBone.area.y + 2);
        }
    }

    private void openLimbMenu()
    {
        if (this.config == null)
        {
            return;
        }

        ModelInstance model = BBSModClient.getModels().getModel(this.config.getId());

        if (model == null)
        {
            return;
        }

        List<String> groups = new ArrayList<>(model.getModel().getAllGroupKeys());
        Collections.sort(groups);
        groups.add(0, "<none>");

        UIModelItemsSection.UIStringListContextMenu menu = new UIModelItemsSection.UIStringListContextMenu(groups, () ->
        {
            String label = this.limbLabel.label.get();

            return Collections.singleton(label.isEmpty() ? "<none>" : label);
        }, (group) ->
        {
            if (group.equals("<none>"))
            {
                group = "";
            }

            this.limbLabel.label = IKey.constant(group.isEmpty() ? "<none>" : group);

            ArmorSlot slot = this.getSlot();

            if (slot != null)
            {
                slot.group.set(group);
                this.editor.dirty();
            }
        });

        this.getContext().replaceContextMenu(menu);
    }

    private ArmorSlot getSlot()
    {
        if (this.config == null || this.type == null)
        {
            return null;
        }

        return this.config.armorSlots.get(this.type);
    }

    private void fillData()
    {
        if (this.types == null)
        {
            return;
        }

        String selected = this.types.getIndex() >= 0 ? this.types.getList().get(this.types.getIndex()) : null;

        if (selected == null)
        {
            this.type = null;
            return;
        }

        try
        {
            this.type = ArmorType.valueOf(selected.toUpperCase());
        }
        catch (Exception e)
        {
            this.type = null;
        }

        ArmorSlot slot = this.getSlot();

        if (slot != null)
        {
            String group = slot.group.get();

            this.limbLabel.label = IKey.constant(group.isEmpty() ? "<none>" : group);

            UIPoseEditor poseEditor = this.editor.getPoseEditor();

            if (poseEditor != null)
            {
                poseEditor.setTransform(slot.transform);
                poseEditor.onChange = this.editor::dirty;
                poseEditor.transform.callbacks(() -> slot.preNotify(0), () ->
                {
                    slot.postNotify(0);
                    this.editor.dirty();
                });

                this.editor.setRight(poseEditor);
            }
        }
    }

    @Override
    public IKey getTitle()
    {
        return UIKeys.MODELS_ARMOR;
    }

    @Override
    public void deselect()
    {
        this.types.deselect();
    }

    @Override
    public void setConfig(ModelConfig config)
    {
        super.setConfig(config);
        this.fillData();
    }
}
