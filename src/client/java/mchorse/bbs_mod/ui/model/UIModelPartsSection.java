package mchorse.bbs_mod.ui.model;

import mchorse.bbs_mod.BBSModClient;
import mchorse.bbs_mod.cubic.ModelInstance;
import mchorse.bbs_mod.cubic.model.ModelConfig;
import mchorse.bbs_mod.l10n.keys.IKey;
import mchorse.bbs_mod.ui.UIKeys;
import mchorse.bbs_mod.ui.framework.UIContext;
import mchorse.bbs_mod.ui.framework.elements.buttons.UIButton;
import mchorse.bbs_mod.ui.framework.elements.input.UIColor;
import mchorse.bbs_mod.ui.framework.elements.input.UITexturePicker;
import mchorse.bbs_mod.BBSSettings;
import mchorse.bbs_mod.ui.framework.elements.utils.UILabel;
import mchorse.bbs_mod.ui.utils.UI;
import mchorse.bbs_mod.utils.colors.Colors;
import mchorse.bbs_mod.ui.utils.pose.UIPoseEditor;

public class UIModelPartsSection extends UIModelSection
{
    public UIButton texture;
    public UIColor color;
    public UIPoseEditor poseEditor;

    public UIModelPartsSection(UIModelPanel editor)
    {
        super(editor);
        
        this.texture = new UIButton(UIKeys.FORMS_EDITOR_MODEL_PICK_TEXTURE, (b) ->
        {
            if (this.config != null)
            {
                UITexturePicker.open(b.getContext(), this.config.texture.get(), (l) ->
                {
                    this.config.texture.set(l);
                    this.editor.dirty();
                });
            }
        });

        this.color = new UIColor((c) ->
        {
            if (this.config != null)
            {
                this.config.color.set(Colors.A100 | c);
                this.editor.dirty();
            }
        });
        
        this.poseEditor = new UIPoseEditor();
        this.poseEditor.onChange = this.editor::dirty;
        this.poseEditor.pickCallback = (bone) ->
        {
            this.editor.renderer.setSelectedBone(bone);

            for (UIModelSection section : this.editor.sections)
            {
                if (section != this)
                {
                    section.deselect();
                }
            }
        };
        this.poseEditor.prepend(this.color);
        this.poseEditor.prepend(this.texture);

        UILabel partsLabel = UI.label(UIKeys.MODELS_PARTS).background(() -> Colors.A50 | BBSSettings.primaryColor.get());
        this.poseEditor.prepend(partsLabel);
    }
    
    @Override
    public boolean subMouseClicked(UIContext context)
    {
        if (this.title.area.isInside(context) && context.mouseButton == 0)
        {
            this.editor.setRight(this.poseEditor);
        }
        
        return super.subMouseClicked(context);
    }

    public void selectBone(String bone)
    {
        this.poseEditor.selectBone(bone);
    }

    @Override
    public IKey getTitle()
    {
        return UIKeys.MODELS_PARTS;
    }

    @Override
    public void setConfig(ModelConfig config)
    {
        super.setConfig(config);
        
        if (config != null)
        {
            this.color.setColor(config.color.get());
            this.poseEditor.setPose(config.parts.get(), config.getId());
            
            ModelInstance model = BBSModClient.getModels().getModel(config.getId());
            
            if (model != null)
            {
                this.poseEditor.fillGroups(model.getModel().getAllGroupKeys(), true);
                this.poseEditor.setDefaultTextureSupplier(() -> model.texture);
            }
        }
    }
}
